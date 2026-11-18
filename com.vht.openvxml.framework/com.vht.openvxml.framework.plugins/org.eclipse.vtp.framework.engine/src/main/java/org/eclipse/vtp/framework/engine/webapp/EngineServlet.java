/*--------------------------------------------------------------------------
 * Copyright (c) 2004, 2006-2007 OpenMethods, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Trip Gilman (OpenMethods), Lonnie G. Pryor (OpenMethods)
 *    - initial API and implementation
 -------------------------------------------------------------------------*/
package org.eclipse.vtp.framework.engine.webapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.vtp.framework.core.IReporter;
import org.eclipse.vtp.framework.engine.ActionDescriptor;
import org.eclipse.vtp.framework.engine.ConfigurationDescriptor;
import org.eclipse.vtp.framework.engine.DeploymentAdmin;
import org.eclipse.vtp.framework.engine.IdentifierDescriptor;
import org.eclipse.vtp.framework.engine.ObserverDescriptor;
import org.eclipse.vtp.framework.engine.ProcessDefinition;
import org.eclipse.vtp.framework.engine.ResourceGroup;
import org.eclipse.vtp.framework.engine.ServiceDescriptor;
import org.eclipse.vtp.framework.engine.http.HttpConnector;
import org.eclipse.vtp.framework.engine.http.HttpConnectorContext;
import org.eclipse.vtp.framework.engine.http.HttpConnectorServlet;
import org.eclipse.vtp.framework.engine.main.ProcessEngine;
import org.eclipse.vtp.framework.engine.osgi.OSGiUtils;
import org.eclipse.vtp.framework.engine.support.AbstractReporter;
import org.eclipse.vtp.framework.interactions.core.services.ExtendedActionEventManager;
import org.eclipse.vtp.framework.util.ConfigurationDictionary;
import org.eclipse.vtp.framework.util.ConfigurationElement;
import org.eclipse.vtp.framework.util.Contributor;
import org.eclipse.vtp.framework.util.Extension;
import org.eclipse.vtp.framework.util.ExtensionRegistry;
import org.eclipse.vtp.framework.util.Guid;
import org.eclipse.vtp.framework.util.Log;
import org.eclipse.vtp.framework.util.StaticConfiguration;
import org.w3c.dom.Document;

/**
 * The entry point of the flattened web application. Replaces the OSGi
 * activator and managers: {@link #init()} runs the engine bootstrap,
 * {@link #service(HttpServletRequest, HttpServletResponse)} dispatches to the
 * longest matching alias of the {@link HttpConnector}, and {@link #destroy()}
 * reverses the bootstrap.
 *
 * @author Lonnie Pryor
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EngineServlet extends HttpServlet {
	/** The serialization version. */
	private static final long serialVersionUID = 1L;
	/** The resource buffer size. */
	private static final int BUFFER_SIZE = 8192;
	/** The name of the static configuration service file (not loaded as a class, because it depends on OSGi). */
	private static final String STATIC_CONFIGURATION_NAME = "org.eclipse.vtp.framework.util.StaticConfigurationAdmin"; //$NON-NLS-1$
	/** The configuration PID of the HTTP connector. */
	private static final String HTTP_PID = "org.eclipse.vtp.framework.engine.http"; //$NON-NLS-1$
	/** The factory PID of the HTTP connector deployments. */
	private static final String DEPLOYMENTS_PID = "org.eclipse.vtp.framework.engine.http.deployments"; //$NON-NLS-1$

	/** The host that reports on behalf of the engine. */
	private Host host = null;
	/** The log to use. */
	private Log log = null;
	/** The extension registry. */
	private ExtensionRegistry registry = null;
	/** The static configuration, if any. */
	private StaticConfiguration configuration = null;
	/** The process engine. */
	private ProcessEngine processEngine = null;
	/** The HTTP connector. */
	private HttpConnector connector = null;
	/** The context the connector's resources are served from. */
	private HttpConnectorContext context = null;
	/** The servlet that serves everything but the resources. */
	private HttpConnectorServlet connectorServlet = null;
	/** The deployment administration. */
	private DeploymentAdmin deploymentAdmin = null;
	/** The document builder used to read process definitions. */
	private DocumentBuilder documentBuilder = null;

	/**
	 * Returns the deployment administration of the running engine.
	 *
	 * @return The deployment administration, or <code>null</code> if the
	 *         engine is not running.
	 */
	public DeploymentAdmin getDeploymentAdmin() {
		return deploymentAdmin;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		// 1. Start the log-level poll thread.
		host = new Host();
		host.startLogLevelPoll();
		// 2. Create the logger.
		log = new Log();
		try {
			// 3. Build the registry.
			ClassLoader classLoader = EngineServlet.class.getClassLoader();
			registry = new ExtensionRegistry(classLoader);
			registry.addClassPathContributions();
			registry.addProjects(getServletContext());
			ExtendedActionEventManager.setExtensionRegistry(registry);
			// 4. Load the static configuration.
			loadStaticConfiguration(classLoader);
			// 5. Create the process engine.
			log.log(Log.LOG_DEBUG, "Creating process engine...");
			processEngine = new ProcessEngine(registry);
			registerEngineExtensions();
			log.log(Log.LOG_DEBUG, "Process engine created.");
			// 6. Create the HTTP connector.
			log.log(Log.LOG_DEBUG, "Creating HTTP connector...");
			connector = new HttpConnector(processEngine, registry, host);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(false);
			documentBuilder = factory.newDocumentBuilder();
			registerConnectorExtensions();
			context = new HttpConnectorContext(getServletContext(), connector);
			connectorServlet = new HttpConnectorServlet(connector);
			connectorServlet.init(getServletConfig());
			deploymentAdmin = new HttpConnectorDeployments(connector);
			log.log(Log.LOG_DEBUG, "HTTP connector created.");
			// 7. Configure the connector.
			connector.configure(configuration == null ? null : configuration
					.getConfiguration(HTTP_PID));
			// 8. Deploy the configured processes.
			if (configuration != null) {
				for (ConfigurationDictionary deployment : configuration
						.getFactoryConfigurations(DEPLOYMENTS_PID)) {
					try {
						connector.deploy(deployment.getPid(), deployment);
					} catch (Exception e) {
						log.log(Log.LOG_ERROR, e.getMessage(), e);
					}
				}
			}
			// 9. Report that the host started.
			Dictionary report = new Hashtable();
			report.put("event", "host.started");
			host.report(IReporter.SEVERITY_INFO, "Host Started", report);
		} catch (Exception e) {
			log.log(Log.LOG_ERROR, e.getMessage(), e);
			releaseAll();
			throw new ServletException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		if (host != null) {
			Dictionary report = new Hashtable();
			report.put("event", "host.stopped");
			host.report(IReporter.SEVERITY_INFO, "Host Stopped", report);
		}
		releaseAll();
	}

	/**
	 * Releases everything the bootstrap created, in reverse order.
	 */
	private void releaseAll() {
		try {
			if (connectorServlet != null) {
				log.log(Log.LOG_DEBUG, "Releasing HTTP connector...");
				connectorServlet.destroy();
			}
		} finally {
			connectorServlet = null;
			context = null;
			deploymentAdmin = null;
			documentBuilder = null;
			connector = null;
			processEngine = null;
			configuration = null;
			registry = null;
			ExtendedActionEventManager.setExtensionRegistry(null);
			log = null;
			if (host != null) {
				host.stopLogLevelPoll();
				host = null;
			}
		}
	}

	/**
	 * Loads the static configuration, if present.
	 *
	 * @param classLoader
	 *            The engine class loader.
	 */
	private void loadStaticConfiguration(ClassLoader classLoader) {
		URL staticConfig = classLoader.getResource("META-INF/services/" //$NON-NLS-1$
				+ STATIC_CONFIGURATION_NAME);
		if (staticConfig == null) {
			return;
		}
		log.log(Log.LOG_DEBUG, "Loading static configuration...");
		Document document = null;
		InputStream input = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(false);
			dbf.setValidating(false);
			document = dbf.newDocumentBuilder().parse(
					input = staticConfig.openStream());
			log.log(Log.LOG_INFO, "Loaded static configuration from \""
					+ staticConfig.toExternalForm() + "\".");
		} catch (Exception e) {
			log.log(Log.LOG_WARNING, "Failed to load static configuration: "
					+ e.getMessage(), e);
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
			}
		}
		if (document != null) {
			configuration = new StaticConfiguration(
					document.getDocumentElement());
		}
	}

	/**
	 * Registers the actions, configurations, observers and services.
	 */
	private void registerEngineExtensions() {
		String[] points = { "org.eclipse.vtp.framework.core.actions", //$NON-NLS-1$
				"org.eclipse.vtp.framework.core.configurations", //$NON-NLS-1$
				"org.eclipse.vtp.framework.core.observers", //$NON-NLS-1$
				"org.eclipse.vtp.framework.core.services" }; //$NON-NLS-1$
		for (int point = 0; point < points.length; ++point) {
			for (Extension extension : registry.getExtensions(points[point])) {
				try {
					Contributor contributor = OSGiUtils.findBundle(
							extension.getContributor(),
							registry.getContributors());
					if (contributor == null) {
						continue;
					}
					for (ConfigurationElement element : extension
							.getConfigurationElements()) {
						switch (point) {
						case 0:
							processEngine.registerAction(loadAction(
									contributor, element));
							break;
						case 1:
							processEngine.registerConfiguration(loadConfiguration(
									contributor, element));
							break;
						case 2:
							processEngine.registerObserver(loadObserver(
									contributor, element));
							break;
						default:
							processEngine.registerService(loadService(
									contributor, element));
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Registers the process definitions and resources.
	 */
	private void registerConnectorExtensions() {
		for (Extension extension : registry
				.getExtensions("org.eclipse.vtp.framework.engine.definitions")) { //$NON-NLS-1$
			Contributor contributor = OSGiUtils.findBundle(
					extension.getContributor(), registry.getContributors());
			if (contributor == null) {
				continue;
			}
			for (ConfigurationElement element : extension
					.getConfigurationElements()) {
				String id = element.getAttribute("id"); //$NON-NLS-1$
				if (id == null) {
					continue;
				}
				String path = element.getAttribute("path"); //$NON-NLS-1$
				if (path == null) {
					continue;
				}
				InputStream input = null;
				try {
					connector.registerDefinition(id, new ProcessDefinition(
							documentBuilder.parse(input = contributor
									.getEntry(path).openStream())),
							contributor);
				} catch (Exception e) {
					e.printStackTrace();
					log.log(Log.LOG_ERROR, e.getMessage(), e);
					continue;
				} finally {
					try {
						if (input != null) {
							input.close();
						}
					} catch (IOException e) {
					}
				}
			}
		}
		for (Extension extension : registry
				.getExtensions("org.eclipse.vtp.framework.engine.resources")) { //$NON-NLS-1$
			Contributor contributor = OSGiUtils.findBundle(
					extension.getContributor(), registry.getContributors());
			if (contributor == null) {
				continue;
			}
			for (ConfigurationElement element : extension
					.getConfigurationElements()) {
				String id = element.getAttribute("id"); //$NON-NLS-1$
				if (id == null) {
					continue;
				}
				String path = element.getAttribute("path"); //$NON-NLS-1$
				if (path == null) {
					continue;
				}
				connector.registerResouces(id, new ResourceGroup(contributor,
						path));
			}
		}
	}

	/**
	 * Loads an action descriptor.
	 */
	private ActionDescriptor loadAction(Contributor contributor,
			ConfigurationElement element) {
		System.out.println("Loading action class: "
				+ element.getAttribute("type"));
		return new ActionDescriptor(element.getAttribute("id"), //$NON-NLS-1$
				element.getAttribute("name"), //$NON-NLS-1$
				loadClass(contributor, element.getAttribute("type")), //$NON-NLS-1$
				Boolean.TRUE.toString().equalsIgnoreCase(
						element.getAttribute("blocking")) //$NON-NLS-1$
		);
	}

	/**
	 * Loads a configuration descriptor.
	 */
	private ConfigurationDescriptor loadConfiguration(Contributor contributor,
			ConfigurationElement element) {
		return new ConfigurationDescriptor(element.getAttribute("id"), //$NON-NLS-1$
				element.getAttribute("name"), //$NON-NLS-1$
				element.getAttribute("xml-namespace"), //$NON-NLS-1$
				element.getAttribute("xml-tag"), //$NON-NLS-1$
				loadClass(contributor, element.getAttribute("type")) //$NON-NLS-1$
		);
	}

	/**
	 * Loads an observer descriptor.
	 */
	private ObserverDescriptor loadObserver(Contributor contributor,
			ConfigurationElement element) {
		return new ObserverDescriptor(element.getAttribute("id"), //$NON-NLS-1$
				element.getAttribute("name"), //$NON-NLS-1$
				loadClass(contributor, element.getAttribute("type")), //$NON-NLS-1$
				Boolean.TRUE.toString().equalsIgnoreCase(
						element.getAttribute("blocking")) //$NON-NLS-1$
		);
	}

	/**
	 * Loads a service descriptor.
	 */
	private ServiceDescriptor loadService(Contributor contributor,
			ConfigurationElement element) {
		ConfigurationElement[] identifierElements = element
				.getChildren("identifier"); //$NON-NLS-1$
		IdentifierDescriptor[] identifiers = new IdentifierDescriptor[identifierElements.length];
		for (int i = 0; i < identifierElements.length; ++i) {
			ConfigurationElement[] qualifierElements = identifierElements[i]
					.getChildren("qualifier"); //$NON-NLS-1$
			String[] qualifiers = new String[qualifierElements.length];
			for (int j = 0; j < qualifierElements.length; ++j) {
				qualifiers[j] = qualifierElements[j].getAttribute("name"); //$NON-NLS-1$
			}
			identifiers[i] = new IdentifierDescriptor(
					identifierElements[i].getAttribute("name"), qualifiers); //$NON-NLS-1$
		}
		return new ServiceDescriptor(element.getAttribute("id"), //$NON-NLS-1$
				element.getAttribute("name"), //$NON-NLS-1$
				element.getAttribute("scope"), //$NON-NLS-1$
				loadClass(contributor, element.getAttribute("type")), //$NON-NLS-1$
				identifiers);
	}

	/**
	 * Loads a class defined in the specified contributor.
	 *
	 * @throws IllegalStateException
	 *             If the specified class is not found.
	 */
	private Class loadClass(Contributor contributor, String className)
			throws IllegalStateException {
		try {
			return contributor.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.http.HttpServlet#service(
	 * javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		HttpConnector connector = this.connector;
		String servletPath = connector == null ? null : connector
				.getServletPath();
		String resourcesPath = connector == null ? null : connector
				.getResourcesPath();
		if (servletPath == null || resourcesPath == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String path = (req.getServletPath() == null ? "" : req.getServletPath())
				+ (req.getPathInfo() == null ? "" : req.getPathInfo());
		boolean resources = matches(resourcesPath, path);
		boolean servlet = matches(servletPath, path);
		if (resources
				&& (!servlet || resourcesPath.length() >= servletPath.length())) {
			serveResource(req, res, path.substring(resourcesPath.length()));
		} else if (servlet) {
			if ("/".equals(servletPath)) {
				connectorServlet.service(req, res);
			} else {
				connectorServlet.service(new AliasRequest(req, servletPath,
						path.substring(servletPath.length())), res);
			}
		} else {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
	 * Returns true if the specified alias matches the specified path.
	 */
	private static boolean matches(String alias, String path) {
		if ("/".equals(alias)) {
			return true;
		}
		return path.equals(alias) || path.startsWith(alias + "/");
	}

	/**
	 * Serves a resource like the Equinox resource servlet: GET and HEAD only,
	 * Content-Type from the context, Last-Modified and If-Modified-Since.
	 */
	private void serveResource(HttpServletRequest req,
			HttpServletResponse res, String resourcePath) throws IOException {
		String method = req.getMethod();
		boolean head = "HEAD".equals(method);
		if (!head && !"GET".equals(method)) {
			res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		if (!context.handleSecurity(req, res)) {
			return;
		}
		if (resourcePath.length() == 0 || resourcePath.endsWith("/")) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND, resourcePath);
			return;
		}
		URL resource = context.getResource(resourcePath);
		if (resource == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND, resourcePath);
			return;
		}
		URLConnection connection = resource.openConnection();
		long lastModified = connection.getLastModified();
		if (lastModified > 0) {
			long since = req.getDateHeader("If-Modified-Since");
			if (since != -1 && since >= (lastModified / 1000) * 1000) {
				res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			res.setDateHeader("Last-Modified", lastModified);
		}
		String mimeType = context.getMimeType(resourcePath);
		if (mimeType != null) {
			res.setContentType(mimeType);
		}
		int length = connection.getContentLength();
		if (length >= 0) {
			res.setContentLength(length);
		}
		res.setStatus(HttpServletResponse.SC_OK);
		if (head) {
			return;
		}
		InputStream input = connection.getInputStream();
		try {
			OutputStream output = res.getOutputStream();
			byte[] buffer = new byte[BUFFER_SIZE];
			for (int read = input.read(buffer); read >= 0; read = input
					.read(buffer)) {
				output.write(buffer, 0, read);
			}
			output.flush();
		} finally {
			input.close();
		}
	}

	/**
	 * Presents a request under a non-root alias the way the HTTP service did:
	 * the alias is the servlet path and the remainder is the path info.
	 */
	private static final class AliasRequest extends HttpServletRequestWrapper {
		/** The servlet path. */
		private final String servletPath;
		/** The path info. */
		private final String pathInfo;

		AliasRequest(HttpServletRequest request, String servletPath,
				String pathInfo) {
			super(request);
			this.servletPath = servletPath;
			this.pathInfo = pathInfo.length() == 0 ? null : pathInfo;
		}

		@Override
		public String getServletPath() {
			return servletPath;
		}

		@Override
		public String getPathInfo() {
			return pathInfo;
		}
	}

	/**
	 * Direct replacement for the HTTP connector's ManagedServiceFactory.
	 */
	private static final class HttpConnectorDeployments implements
			DeploymentAdmin {
		/** The connector to deploy to. */
		private final HttpConnector connector;

		HttpConnectorDeployments(HttpConnector connector) {
			this.connector = connector;
		}

		@Override
		public String deploy(Dictionary properties) {
			String id = Guid.createGUID();
			connector.deploy(id, properties);
			return id;
		}

		@Override
		public boolean update(String id, Dictionary properties) {
			connector.deploy(id, properties);
			return true;
		}

		@Override
		public boolean undelpoy(String id) {
			connector.undeploy(id);
			return true;
		}
	}

	/**
	 * The bootstrap host: reports to standard output and tracks the log level.
	 */
	private static final class Host extends AbstractReporter {
		private static final String[] REPORT_LEVELS = { "ERROR", "WARN",
				"INFO", "DEBUG" };

		/** The reporter services; always empty. */
		private final List<IReporter> reporters = new ArrayList<IReporter>();
		private volatile int activeLogLevel = 3;
		private Thread logLevelPoll = null;

		void startLogLevelPoll() {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (true) {
							String level = System.getProperty(
									"org.eclipse.vtp.loglevel", "INFO");
							boolean found = false;
							for (int i = 0; i < REPORT_LEVELS.length; i++) {
								String l = REPORT_LEVELS[i];
								if (l.equalsIgnoreCase(level)) {
									activeLogLevel = i + 1;
									found = true;
									break;
								}
							}
							if (!found) {
								activeLogLevel = 3;
							}
							Thread.sleep(10000);
						}
					} catch (InterruptedException ex) {
						// stopped
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			t.setDaemon(true);
			t.start();
			logLevelPoll = t;
		}

		void stopLogLevelPoll() {
			if (logLevelPoll != null) {
				logLevelPoll.interrupt();
				logLevelPoll = null;
			}
		}

		/**
		 * Builds a report .
		 * 
		 * @param severity
		 * @param categories
		 * @param message
		 * @param properties
		 * @return
		 */
		private String buildReport(int severity, String[] categories,
				String message, Dictionary properties) {
			// Create the line prefix.
			StringBuffer buffer = new StringBuffer(REPORT_LEVELS[Math.max(0,
					Math.min(REPORT_LEVELS.length - 1, severity - 1))]);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			buffer.append('|').append(calendar.get(Calendar.YEAR));
			buffer.append('/').append(format(calendar.get(Calendar.MONTH) + 1, 2));
			buffer.append('/').append(format(calendar.get(Calendar.DATE), 2));
			buffer.append('|')
					.append(format(calendar.get(Calendar.HOUR_OF_DAY), 2));
			buffer.append(':').append(format(calendar.get(Calendar.MINUTE), 2));
			buffer.append(':').append(format(calendar.get(Calendar.SECOND), 2));
			Object id = properties.remove("process.id"); //$NON-NLS-1$
			if (id != null) {
				buffer.append('|').append("p=").append(id); //$NON-NLS-1$
			}
			id = properties.remove("session.id"); //$NON-NLS-1$
			if (id != null) {
				buffer.append('|').append("s=").append(id); //$NON-NLS-1$
			}
			id = properties.remove("execution.id"); //$NON-NLS-1$
			if (id != null) {
				buffer.append('|').append("e=").append(format(id.toString(), 4)); //$NON-NLS-1$
			}
			id = properties.remove("action.id"); //$NON-NLS-1$
			// if (id != null)
			// buffer.append('|').append("a=").append(id); //$NON-NLS-1$
			buffer.append('>');
			String prefix = buffer.toString();
			// Write the report.
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			printWriter.print(prefix);
			printWriter.print(' ');
			printWriter.print(message);
			// Append a line listing the categories, if any.
			// if (categories != null)
			// {
			// printWriter.println();
			// printWriter.print(prefix);
			//			printWriter.print("\tcategories:"); //$NON-NLS-1$
			// for (int i = 0; i < categories.length; ++i)
			// {
			// if (categories[i] != null)
			// {
			// printWriter.print(' ');
			// printWriter.print(categories[i]);
			// }
			// }
			// }
			if (properties != null) {
				Object t = properties.get("cause"); //$NON-NLS-1$
				if (t instanceof Throwable) {
					properties.remove("cause"); //$NON-NLS-1$
				} else {
					t = null;
				}
				// if (!properties.isEmpty())
				// {
				// printWriter.println();
				// printWriter.print(prefix);
				//				printWriter.print("\tproperties:"); //$NON-NLS-1$
				// for (Enumeration e = properties.keys(); e.hasMoreElements();)
				// {
				// Object key = e.nextElement();
				// Object value = properties.get(key);
				// printWriter.println();
				// printWriter.print(prefix);
				//					printWriter.print("\t\t"); //$NON-NLS-1$
				// printWriter.print(key);
				// printWriter.print('=');
				// printWriter.print(value);
				// }
				// }
				if (t != null) {
					StringWriter causeStringWriter = new StringWriter();
					PrintWriter causePrintWriter = new PrintWriter(
							causeStringWriter);
					((Throwable) t).printStackTrace(causePrintWriter);
					causePrintWriter.flush();
					for (StringTokenizer st = new StringTokenizer(
							causeStringWriter.toString(), "\r\n"); st.hasMoreTokens();) //$NON-NLS-1$
					{
						printWriter.println();
						printWriter.print(prefix);
						printWriter.print('\t');
						printWriter.print(st.nextToken());
					}
				}
			}
			return stringWriter.toString();
		}

		private String format(int number, int minLength) {
			return format(String.valueOf(number), minLength);
		}

		private String format(String number, int minLength) {
			StringBuffer buffer = new StringBuffer(minLength);
			for (int i = number.length(); i < minLength; ++i) {
				buffer.append(0);
			}
			return buffer.append(number).toString();
		}

		@Override
		public boolean isSeverityEnabled(int severity) {
			return severity <= activeLogLevel;
		}

		@Override
		public boolean isReportingEnabled() {
			return reporters.size() > 0;
		}

		@Override
		protected void doReport(int severity, String[] categories,
				String message, Dictionary properties) {
			if (properties == null) {
				properties = new Hashtable();
			}
			if (properties.get("scope") == null) {
				properties.put("scope", "host"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			Object type = properties.get("type"); //$NON-NLS-1$
			if (type == null || "report".equalsIgnoreCase(type.toString())) { //$NON-NLS-1$
				properties.put("type", type = "report"); //$NON-NLS-1$ //$NON-NLS-2$
				if (properties.get("time") == null) {
					properties.put("time", //$NON-NLS-1$
							DateFormat.getDateTimeInstance().format(new Date()));
				}
				for (IReporter reporter : reporters) {
					reporter.report(severity, categories, message, properties);
				}
			} else if (!"log".equalsIgnoreCase(type.toString())) {
				return;
			}
			System.out.println(buildReport(severity, categories, message,
					properties));
		}
	}
}

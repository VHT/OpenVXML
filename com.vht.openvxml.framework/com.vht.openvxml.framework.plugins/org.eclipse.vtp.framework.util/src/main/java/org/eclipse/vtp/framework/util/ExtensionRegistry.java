package org.eclipse.vtp.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A plain replacement for the Eclipse extension registry. It is populated from
 * the <code>plugin.xml</code> files on the webapp class path and from the
 * workflow projects under <code>WEB-INF/vtp/projects/</code>.
 * <p>
 * Extensions are registered in a deterministic order: class path
 * <code>plugin.xml</code> files in the order the class loader returns them,
 * then project directories in sorted name order; within a file, in document
 * order.
 */
public class ExtensionRegistry {
	/** The path of the project directories in the web application. */
	public static final String PROJECTS_PATH = "/WEB-INF/vtp/projects/"; //$NON-NLS-1$

	/** The class loader that loads contributed classes. */
	private final ClassLoader classLoader;
	/** The contributors, in registration order. */
	private final List<Contributor> contributors = new ArrayList<Contributor>();
	/** The extensions, by extension point, in registration order. */
	private final Map<String, List<Extension>> extensions = new LinkedHashMap<String, List<Extension>>();
	/** The roots already registered. */
	private final Set<String> roots = new HashSet<String>();

	/**
	 * Creates a new, empty ExtensionRegistry.
	 *
	 * @param classLoader
	 *            The (webapp) class loader that loads contributed classes.
	 */
	public ExtensionRegistry(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Registers every <code>plugin.xml</code> found on the class loader's
	 * class path, using the sibling <code>META-INF/MANIFEST.MF</code> for the
	 * contributor's names.
	 *
	 * @throws IOException
	 *             If the class path cannot be searched.
	 */
	public synchronized void addClassPathContributions() throws IOException {
		Enumeration<URL> urls = classLoader.getResources("plugin.xml"); //$NON-NLS-1$
		while (urls.hasMoreElements()) {
			URL pluginXml = urls.nextElement();
			String text = pluginXml.toExternalForm();
			String root = text.substring(0,
					text.length() - "plugin.xml".length()); //$NON-NLS-1$
			try {
				String symbolicName = null;
				String bundleName = null;
				InputStream input = null;
				try {
					input = new URL(root + "META-INF/MANIFEST.MF").openStream(); //$NON-NLS-1$
					Manifest manifest = new Manifest(input);
					symbolicName = manifest.getMainAttributes().getValue(
							"Bundle-SymbolicName"); //$NON-NLS-1$
					bundleName = manifest.getMainAttributes().getValue(
							"Bundle-Name"); //$NON-NLS-1$
				} catch (IOException e) {
					// No manifest alongside the plugin.xml.
				} finally {
					if (input != null) {
						input.close();
					}
				}
				if (symbolicName != null) {
					int semicolon = symbolicName.indexOf(';');
					if (semicolon >= 0) {
						symbolicName = symbolicName.substring(0, semicolon);
					}
					symbolicName = symbolicName.trim();
				} else {
					symbolicName = root;
				}
				add(symbolicName, bundleName, new URL(root), pluginXml);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Registers the <code>plugin.xml</code> of every project directory under
	 * <code>WEB-INF/vtp/projects/</code>. A project's symbolic name is its
	 * directory name (<code>&lt;Name&gt;.&lt;guid&gt;</code>); projects have no
	 * manifest, so the <code>Bundle-Name</code> is the directory name with the
	 * trailing <code>.&lt;guid&gt;</code> (32 hex characters) removed. A
	 * directory without that suffix is reported and skipped.
	 *
	 * @param servletContext
	 *            The context of the web application.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void addProjects(ServletContext servletContext) {
		Set<String> paths = servletContext.getResourcePaths(PROJECTS_PATH);
		if (paths == null) {
			return;
		}
		String[] sorted = paths.toArray(new String[paths.size()]);
		Arrays.sort(sorted);
		for (String path : sorted) {
			if (!path.endsWith("/")) { //$NON-NLS-1$
				continue;
			}
			try {
				URL pluginXml = servletContext.getResource(path + "plugin.xml"); //$NON-NLS-1$
				URL root = servletContext.getResource(path);
				if (pluginXml == null || root == null) {
					continue;
				}
				String name = path.substring(0, path.length() - 1);
				name = name.substring(name.lastIndexOf('/') + 1);
				String rootText = root.toExternalForm();
				if (!rootText.endsWith("/")) { //$NON-NLS-1$
					root = new URL(rootText + "/"); //$NON-NLS-1$
				}
				String bundleName = getProjectBundleName(name);
				if (bundleName == null) {
					System.err.println("Skipping project without a .<guid> suffix: " //$NON-NLS-1$
							+ name);
					continue;
				}
				add(name, bundleName, root, pluginXml);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the <code>Bundle-Name</code> of a project directory: the name
	 * with the trailing <code>.&lt;guid&gt;</code> (32 hex characters) removed.
	 * 
	 * @param directoryName
	 *            The project directory name.
	 * @return The bundle name, or <code>null</code> if the name has no such
	 *         suffix.
	 */
	static String getProjectBundleName(String directoryName) {
		int dot = directoryName.length() - 33;
		if (dot < 1 || directoryName.charAt(dot) != '.') {
			return null;
		}
		for (int i = dot + 1; i < directoryName.length(); ++i) {
			if (Character.digit(directoryName.charAt(i), 16) < 0
					|| directoryName.charAt(i) > 'f') {
				return null;
			}
		}
		return directoryName.substring(0, dot);
	}

	private void add(String symbolicName, String bundleName, URL root,
			URL pluginXml) throws Exception {
		if (!roots.add(root.toExternalForm())) {
			return;
		}
		Document document;
		InputStream input = pluginXml.openStream();
		try {
			document = XMLUtilities.getDocumentBuilder().parse(input);
		} finally {
			input.close();
		}
		Contributor contributor = new Contributor(symbolicName, bundleName,
				classLoader, root);
		contributors.add(contributor);
		for (Element element : XMLUtilities.getChildElements(document
				.getDocumentElement())) {
			if (!"extension".equals(element.getTagName())) { //$NON-NLS-1$
				continue;
			}
			Extension extension = new Extension(element, contributor);
			List<Extension> list = extensions.get(extension
					.getExtensionPointUniqueIdentifier());
			if (list == null) {
				list = new ArrayList<Extension>();
				extensions.put(extension.getExtensionPointUniqueIdentifier(),
						list);
			}
			list.add(extension);
		}
	}

	/**
	 * Returns the extensions to the given extension point.
	 *
	 * @param extensionPointId
	 *            The unique identifier of the extension point.
	 * @return The extensions, in registration order.
	 */
	public synchronized Extension[] getExtensions(String extensionPointId) {
		List<Extension> list = extensions.get(extensionPointId);
		if (list == null) {
			return new Extension[0];
		}
		return list.toArray(new Extension[list.size()]);
	}

	/**
	 * Returns the configuration elements of every extension to the given
	 * extension point.
	 *
	 * @param extensionPointId
	 *            The unique identifier of the extension point.
	 * @return The configuration elements, in registration order.
	 */
	public synchronized ConfigurationElement[] getConfigurationElements(
			String extensionPointId) {
		List<ConfigurationElement> result = new ArrayList<ConfigurationElement>();
		for (Extension extension : getExtensions(extensionPointId)) {
			result.addAll(Arrays.asList(extension.getConfigurationElements()));
		}
		return result.toArray(new ConfigurationElement[result.size()]);
	}

	/**
	 * Returns the contributor with the given symbolic name.
	 *
	 * @param symbolicName
	 *            The symbolic name.
	 * @return The contributor, or <code>null</code> if there is none.
	 */
	public synchronized Contributor getContributor(String symbolicName) {
		for (Contributor contributor : contributors) {
			if (contributor.getName().equals(symbolicName)) {
				return contributor;
			}
		}
		return null;
	}

	/**
	 * Returns every contributor, in registration order.
	 *
	 * @return The contributors.
	 */
	public synchronized List<Contributor> getContributors() {
		return Collections.unmodifiableList(new ArrayList<Contributor>(
				contributors));
	}
}

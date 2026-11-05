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
package org.eclipse.vtp.framework.engine.http;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A context that forwards invocations to an {@link HttpConnector}, falling back
 * to the servlet context.
 * 
 * @author Lonnie Pryor
 */
public class HttpConnectorContext {
	/** The servlet context to fall back to. */
	private final ServletContext servletContext;
	/** The {@link HttpConnector} instance to use. */
	private final HttpConnector connector;

	/**
	 * Creates a new HttpConnectorContext.
	 * 
	 * @param servletContext
	 *            The servlet context to fall back to.
	 * @param connector
	 *            The {@link HttpConnector} instance to use.
	 */
	public HttpConnectorContext(ServletContext servletContext,
			HttpConnector connector) {
		this.servletContext = servletContext;
		this.connector = connector;
	}

	/**
	 * Returns the MIME type of the specified resource.
	 * 
	 * @param path
	 *            The path of the resource.
	 * @return The MIME type of the specified resource.
	 */
	public String getMimeType(String path) {
		String mimeType = connector.getMimeType(path);
		if (mimeType == null) {
			mimeType = servletContext.getMimeType(path);
		}
		return mimeType;
	}

	/**
	 * Returns the resource at the specified path.
	 * 
	 * @param path
	 *            The path of the resource.
	 * @return The resource at the specified path.
	 */
	public URL getResource(String path) {
		URL resource = connector.getResource(path);
		if (resource == null) {
			String name = HttpUtils.normalizePath(path).substring(1);
			resource = HttpConnectorContext.class.getClassLoader()
					.getResource(name);
		}
		return resource;
	}

	/**
	 * Handles security for the specified request.
	 * 
	 * @param req
	 *            The HTTP request.
	 * @param res
	 *            The HTTP response.
	 * @return Always <code>true</code>.
	 * @throws IOException
	 *             If the connection fails.
	 */
	public boolean handleSecurity(HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		return true;
	}
}

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
package org.eclipse.vtp.framework.common.services;

import org.eclipse.vtp.framework.common.IScriptable;
import org.eclipse.vtp.framework.core.IProcessContext;

/**
 * An {@link IScriptable} implementation that makes the {@link IProcessContext}
 * instance available as a scripting object.
 * 
 * <p>
 * This service will make available a "Process" object to all scripts in its
 * scope. The variable supports the following properties:
 * <ul>
 * <li><code>id</code>: a string containing the process ID</li>
 * <li><code>properties</code>: an object containing the process configuration
 * properties</li>
 * </ul>
 * The properties object listed above will have a property for each
 * configuration property defined in the process context. The properties object
 * will not have an implicit value.
 * </p>
 * 
 * <p>
 * The "Process" scripting object uses the process ID as the implicit value,
 * thus it can be compared to other string objects.
 * </p>
 * 
 * @author Lonnie Pryor
 * @see IProcessContext
 */
public class ScriptableProcessContext implements IScriptable {
	/**
	 * Scripted access to the process properties.
	 * 
	 * @author Lonnie Pryor
	 */
	private final class ScriptableProperties implements IScriptable {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#clearEntry(
		 * java.lang.String)
		 */
		@Override
		public boolean clearEntry(String name) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.vtp.framework.spi.scripting.IScriptable#clearItem(int)
		 */
		@Override
		public boolean clearItem(int index) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#getEntry(
		 * java.lang.String)
		 */
		@Override
		public Object getEntry(String name) {
			return context.getProperty(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#
		 * getFunctionNames()
		 */
		@Override
		public String[] getFunctionNames() {
			return new String[] {};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#getItem(int)
		 */
		@Override
		public Object getItem(int index) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#getName()
		 */
		@Override
		public String getName() {
			return "properties"; //$NON-NLS-1$
		}

		@Override
		public String[] getPropertyNames() {
			return new String[0];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#hasEntry(
		 * java.lang.String)
		 */
		@Override
		public boolean hasEntry(String name) {
			return context.getProperty(name) != null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#hasItem(int)
		 */
		@Override
		public boolean hasItem(int index) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#hasValue()
		 */
		@Override
		public boolean hasValue() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.vtp.framework.spi.scripting.IScriptable#invokeFunction(
		 * java.lang.String, java.lang.Object[])
		 */
		@Override
		public Object invokeFunction(String name, Object[] arguments) {
			return null;
		}

		@Override
		public boolean isMutable() {
			// TODO Auto-generated method stub
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#setEntry(
		 * java.lang.String, java.lang.Object)
		 */
		@Override
		public boolean setEntry(String name, Object value) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#setItem(int,
		 * java.lang.Object)
		 */
		@Override
		public boolean setItem(int index, Object value) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#toValue()
		 */
		@Override
		public Object toValue() {
			return null;
		}
	}

	/** The context to provide scripting services for. */
	private final IProcessContext context;

	/** The scripted view of the process properties. */
	private final ScriptableProperties properties = new ScriptableProperties();

	/**
	 * Creates a new ScriptableProcessContext.
	 * 
	 * @param context
	 *            The context to provide scripting services for.
	 */
	public ScriptableProcessContext(IProcessContext context) {
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#clearEntry(
	 * java.lang.String)
	 */
	@Override
	public final boolean clearEntry(String name) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#clearItem(int)
	 */
	@Override
	public final boolean clearItem(int index) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#getEntry(
	 * java.lang.String)
	 */
	@Override
	public final Object getEntry(String name) {
		if ("id".equals(name)) {
			return context.getProcessID();
		}
		if (properties.getName().equals(name)) {
			return properties;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.vtp.framework.spi.scripting.IScriptable#getFunctionNames()
	 */
	@Override
	public final String[] getFunctionNames() {
		return new String[] { "loadClass" }; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#getItem(int)
	 */
	@Override
	public final Object getItem(int index) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#getName()
	 */
	@Override
	public final String getName() {
		return "Process"; //$NON-NLS-1$
	}

	@Override
	public String[] getPropertyNames() {
		return new String[] { "id" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#hasEntry(
	 * java.lang.String)
	 */
	@Override
	public final boolean hasEntry(String name) {
		return "id".equals(name) || properties.getName().equals(name); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#hasItem(int)
	 */
	@Override
	public final boolean hasItem(int index) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#hasValue()
	 */
	@Override
	public boolean hasValue() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#invokeFunction(
	 * java.lang.String, java.lang.Object[])
	 */
	@Override
	public final Object invokeFunction(String name, Object[] arguments) {
		if ("loadClass".equals(name)) //$NON-NLS-1$
		{
			if (arguments.length != 1) {
				return null;
			}
			try {
				return context.loadClass(arguments[0].toString());
			} catch (final ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}
		return null;
	}

	@Override
	public boolean isMutable() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#setEntry(
	 * java.lang.String, java.lang.Object)
	 */
	@Override
	public final boolean setEntry(String name, Object value) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#setItem(int,
	 * java.lang.Object)
	 */
	@Override
	public final boolean setItem(int index, Object value) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.scripting.IScriptable#toValue()
	 */
	@Override
	public Object toValue() {
		return getEntry("id"); //$NON-NLS-1$
	}
}

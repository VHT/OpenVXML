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
package org.eclipse.vtp.framework.interactions.core.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A command that renders output to the user.
 * 
 * @author Lonnie Pryor
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class OutputMessageCommand extends ConversationCommand {
	/** The properties of the interaction. */
	private final Map properties = new HashMap();
	/** The name of the parameter to pass the result of the request as. */
	private String resultName = null;
	/** The value of the result parameter to pass if the input is valid. */
	private String filledResultValue = null;
	/** The value of the result parameter to pass if the caller hungup. */
	private String hangupResultValue = null;
	/** The output to be rendered. */
	private final List<Output> output = new ArrayList<Output>();
	/** The parameters to set when the process resumes. */
	private final Map parameters = new HashMap();

	/**
	 * Creates a new OutputMessageCommand.
	 */
	public OutputMessageCommand() {
	}

	/**
	 * Returns the names of the properties of the interaction.
	 * 
	 * @return The names of the properties of the interaction.
	 */
	public String[] getPropertyNames() {
		return (String[]) properties.keySet().toArray(
				new String[properties.size()]);
	}

	/**
	 * Returns the value of a property of the interaction.
	 * 
	 * @param name
	 *            The name of the property to be set.
	 * @return The value that the specified property will be set to.
	 */
	public String getPropertyValue(String name) {
		if (name == null) {
			return null;
		}
		return (String) properties.get(name);
	}

	/**
	 * Returns the name of the parameter to pass the result of the request as.
	 * 
	 * @return The name of the parameter to pass the result of the request as.
	 */
	public String getResultName() {
		return resultName;
	}

	/**
	 * Sets the name of the parameter to pass the result of the request as.
	 * 
	 * @param resultName
	 *            The name of the parameter to pass the result of the request
	 *            as.
	 */
	public void setResultName(String resultName) {
		this.resultName = resultName;
	}

	/**
	 * Returns the value of the result parameter to pass if the input is valid.
	 * 
	 * @return The value of the result parameter to pass if the input is valid.
	 */
	public String getFilledResultValue() {
		return filledResultValue;
	}

	/**
	 * Sets the value of the result parameter to pass if the input is valid.
	 * 
	 * @param filledResultValue
	 *            The value of the result parameter to pass if the input is
	 *            valid.
	 */
	public void setFilledResultValue(String filledResultValue) {
		this.filledResultValue = filledResultValue;
	}

	/**
	 * Returns the value of the result parameter to pass if the caller hungup.
	 * 
	 * @return The value of the result parameter to pass if the caller hungup.
	 */
	public String getHangupResultValue() {
		return hangupResultValue;
	}

	/**
	 * Sets the value of the result parameter to pass if the caller hungup.
	 * 
	 * @param noInputResultValue
	 *            The value of the result parameter to pass if the caller
	 *            hungup.
	 */
	public void setHangupResultValue(String hangupResultValue) {
		this.hangupResultValue = hangupResultValue;
	}

	/**
	 * Configures a property of the interaction.
	 * 
	 * @param name
	 *            The name of the property to set.
	 * @param value
	 *            The value to set the property to.
	 */
	public void setPropertyValue(String name, String value) {
		if (name == null) {
			return;
		}
		if (value == null) {
			properties.remove(name);
		} else {
			properties.put(name, value);
		}
	}

	/**
	 * Returns the number of output elements configured in this output message.
	 * 
	 * @return The number of output elements configured in this output message.
	 */
	public int getOutputCount() {
		return output.size();
	}

	/**
	 * Returns the type of the output at the specified index.
	 * 
	 * @param outputIndex
	 *            The index to check.
	 * @return The type of the output at the specified index.
	 */
	public int getOutputType(int outputIndex) {
		if (outputIndex < 0 || outputIndex >= output.size()) {
			return 0;
		}
		if (Output.TYPE_FILE.equals(output.get(outputIndex).getType())) {
			return OUTPUT_TYPE_FILE;
		}
		if (Output.TYPE_TEXT.equals(output.get(outputIndex).getType())) {
			return OUTPUT_TYPE_TEXT;
		}
		return 0;
	}

	/**
	 * Returns the value of the output at the specified index.
	 * 
	 * @param outputIndex
	 *            The index to check.
	 * @return The value of the output at the specified index.
	 */
	public String getOutputValue(int outputIndex) {
		if (outputIndex < 0 || outputIndex >= output.size()) {
			return null;
		}
		return output.get(outputIndex).getProperty("value");
	}

	/**
	 * Adds a file output item to this output message.
	 * 
	 * @param path
	 *            The path of the file to render.
	 */
	public void addOutput(Output o) {
		output.add(o);
	}

	/**
	 * Adds a file output item to this output message at the specified index.
	 * 
	 * @param outputIndex
	 *            The index to insert at.
	 * @param path
	 *            The path of the file to render.
	 */
	public void insertOutput(int outputIndex, Output o) {
		if (outputIndex >= 0 && outputIndex <= output.size()) {
			output.add(outputIndex, o);
		}
	}

	/**
	 * Sets the file output item in this output message at the specified index.
	 * 
	 * @param outputIndex
	 *            The index to set at.
	 * @param path
	 *            The path of the file to render.
	 */
	public void setOutput(int outputIndex, Output o) {
		if (outputIndex >= 0 && outputIndex < output.size()) {
			output.set(outputIndex, o);
		}
	}

	/**
	 * Removes the output item in this output message at the specified index.
	 * 
	 * @param outputIndex
	 *            The index to remove at.
	 */
	public void removeOutput(int outputIndex) {
		if (outputIndex >= 0 && outputIndex < output.size()) {
			output.remove(outputIndex);
		}
	}

	/**
	 * Returns the names of the parameters that will be returned from the
	 * interaction.
	 * 
	 * @return The names of the parameters that will be returned from the
	 *         interaction.
	 */
	public String[] getParameterNames() {
		return (String[]) parameters.keySet().toArray(
				new String[parameters.size()]);
	}

	/**
	 * Returns the values of a parameter to be set when the process resumes.
	 * 
	 * @param name
	 *            The name of the parameter to be set.
	 * @return The values that specified parameter will be set to.
	 */
	public String[] getParameterValues(String name) {
		if (name == null) {
			return null;
		}
		List list = (List) parameters.get(name);
		if (list == null) {
			return null;
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	/**
	 * Configures a parameter set when the current process resumes.
	 * 
	 * @param name
	 *            The name of the parameter to set.
	 * @param values
	 *            The values to set the parameter to.
	 */
	public void setParameterValues(String name, String[] values) {
		if (name == null) {
			return;
		}
		if (values == null) {
			parameters.remove(name);
		} else {
			List list = (List) parameters.get(name);
			if (list == null) {
				parameters.put(name, list = new LinkedList());
			} else {
				list.clear();
			}
			for (String value : values) {
				if (value != null) {
					list.add(value);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.interactions.core.commands.
	 * ConversationCommand#accept(
	 * org.eclipse.vtp.framework.interactions.core.commands.
	 * IConversationCommandVisitor)
	 */
	@Override
	Object accept(IConversationCommandVisitor visitor) {
		return visitor.visitOutputMessage(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.ICommand#exportContents()
	 */
	@Override
	public Object exportContents() {
		List properties = new ArrayList(this.properties.size() * 2);
		for (Iterator i = this.properties.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			properties.add(entry.getKey());
			properties.add(entry.getValue());
		}
		List<Object> output = new ArrayList<Object>(this.output.size() * 2);
		for (Output o : this.output) {
			output.add(o.getType());
			Set<String> propNames = o.getPropertyNames();
			String[] props = new String[propNames.size() * 2];
			int i = 0;
			for (String pn : propNames) {
				props[i] = pn;
				props[i + 1] = o.getProperty(pn);
				i++;
			}
			output.add(props);
		}
		List parameters = new ArrayList(this.parameters.size() * 2);
		for (Iterator i = this.parameters.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			parameters.add(entry.getKey());
			parameters.add(entry.getValue());
		}
		return new Object[] {
				properties.toArray(new String[properties.size()]),
				output.toArray(new Object[output.size()]),
				parameters.toArray(new String[parameters.size()]) };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vtp.framework.spi.ICommand#importContents(
	 * java.lang.Object)
	 */
	@Override
	public void importContents(Object contents) {
		Object[] array = (Object[]) contents;
		this.properties.clear();
		String[] properties = (String[]) array[0];
		for (int i = 0; i < properties.length; i += 2) {
			this.properties.put(properties[i], properties[i + 1]);
		}
		this.output.clear();
		Object[] output = (Object[]) array[1];
		for (int i = 0; i < output.length; i += 2) {
			Output o = new Output((String) output[i]);
			String[] props = (String[]) output[i + 1];
			for (int p = 0; p < props.length; p += 2) {
				o.setProperty(props[p], props[p + 1]);
			}
			this.output.add(o);
		}
		this.parameters.clear();
		String[] parameters = (String[]) array[2];
		for (int i = 0; i < parameters.length; i += 2) {
			this.parameters.put(parameters[i], parameters[i + 1]);
		}
	}
}

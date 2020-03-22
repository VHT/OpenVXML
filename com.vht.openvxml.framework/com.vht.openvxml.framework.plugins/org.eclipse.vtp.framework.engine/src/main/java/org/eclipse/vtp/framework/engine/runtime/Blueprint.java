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
package org.eclipse.vtp.framework.engine.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.vtp.framework.core.IContext;
import org.eclipse.vtp.framework.engine.ActionDescriptor;
import org.eclipse.vtp.framework.engine.ConfigurationDescriptor;
import org.eclipse.vtp.framework.engine.ObserverDescriptor;
import org.eclipse.vtp.framework.engine.ServiceDescriptor;
import org.eclipse.vtp.framework.spi.IProcessDefinition;
import org.w3c.dom.Element;

/**
 * The representation of the architecture of the process.
 * 
 * @author Lonnie Pryor
 */
public class Blueprint {
	private final Map<String, LinkedList<ConfigurationDescriptor>> configurationIndex;
	private final Map<String, Object> processServiceIndex;
	private final Map<String, Object> sessionServiceIndex;
	private final Map<String, Object> executionServiceIndex;
	private final Map<String, Object> actionServiceIndex;
	private final Map<String, Object> executableIndex;
	private final Map<String, Executable> entryPointIndex;
	private final IExtensionRegistry registry;

	public Blueprint(IProcessDefinition definition,
			ConfigurationDescriptor[] configurationDescriptors,
			ActionDescriptor[] actionDescriptors,
			ObserverDescriptor[] observerDescriptors,
			ServiceDescriptor[] serviceDescriptors, IExtensionRegistry registry) {
		this.configurationIndex = indexConfigurations(configurationDescriptors);
		List<ServiceDescriptor> processServiceDescriptors = new ArrayList<ServiceDescriptor>(
				serviceDescriptors.length);
		List<ServiceDescriptor> sessionServiceDescriptors = new ArrayList<ServiceDescriptor>(
				serviceDescriptors.length);
		List<ServiceDescriptor> executionServiceDescriptors = new ArrayList<ServiceDescriptor>(
				serviceDescriptors.length);
		List<ServiceDescriptor> actionServiceDescriptors = new ArrayList<ServiceDescriptor>(
				serviceDescriptors.length);
		for (ServiceDescriptor serviceDescriptor : serviceDescriptors) {
			if (ServiceDescriptor.SCOPE_PROCESS.equals(serviceDescriptor
					.getScope())) {
				processServiceDescriptors.add(serviceDescriptor);
			} else if (ServiceDescriptor.SCOPE_SESSION.equals(serviceDescriptor
					.getScope())) {
				sessionServiceDescriptors.add(serviceDescriptor);
			} else if (ServiceDescriptor.SCOPE_EXECUTION
					.equals(serviceDescriptor.getScope())) {
				executionServiceDescriptors.add(serviceDescriptor);
			} else if (ServiceDescriptor.SCOPE_ACTION.equals(serviceDescriptor
					.getScope())) {
				actionServiceDescriptors.add(serviceDescriptor);
			}
		}
		this.processServiceIndex = buildServices(definition,
				processServiceDescriptors);
		this.sessionServiceIndex = buildServices(definition,
				sessionServiceDescriptors);
		this.executionServiceIndex = buildServices(definition,
				executionServiceDescriptors);
		this.actionServiceIndex = buildServices(definition,
				actionServiceDescriptors);
		this.executableIndex = buildExecutables(definition, actionDescriptors,
				observerDescriptors);
		this.entryPointIndex = linkExecutables(definition);
		this.registry = registry;
	}

	public Collection<Configuration> createConfigurations(Element data) {
		String xmlIdentifier = RuntimeUtils.getXMLIdentifier(data);
		List<ConfigurationDescriptor> descriptors = configurationIndex
				.get(xmlIdentifier);
		if (descriptors == null || descriptors.isEmpty()) {
			return Collections.emptyList();
		}
		List<Configuration> results = new ArrayList<Configuration>(
				descriptors.size());
		for (Iterator<ConfigurationDescriptor> i = descriptors.iterator(); i
				.hasNext();) {
			results.add(new Configuration(this, i.next(), data));
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	public void solidifyConfigurations(IContext serviceRegistry) {
		Iterator<Object> i = processServiceIndex.values().iterator();
		while (i.hasNext()) {
			List<Configurable> list = (List<Configurable>) i.next();
			for (Configurable configurable : list) {
				configurable.solidifyConfigurations(serviceRegistry);
			}
		}
		i = sessionServiceIndex.values().iterator();
		while (i.hasNext()) {
			List<Configurable> list = (List<Configurable>) i.next();
			for (Configurable configurable : list) {
				configurable.solidifyConfigurations(serviceRegistry);
			}
		}
		i = executionServiceIndex.values().iterator();
		while (i.hasNext()) {
			List<Configurable> list = (List<Configurable>) i.next();
			for (Configurable configurable : list) {
				configurable.solidifyConfigurations(serviceRegistry);
			}
		}
		i = actionServiceIndex.values().iterator();
		while (i.hasNext()) {
			List<Configurable> list = (List<Configurable>) i.next();
			for (Configurable configurable : list) {
				configurable.solidifyConfigurations(serviceRegistry);
			}
		}
		i = executableIndex.values().iterator();
		while (i.hasNext()) {
			Configurable configurable = (Configurable) i.next();
			configurable.solidifyConfigurations(serviceRegistry);
		}
	}

	public Collection<?> getProcessServices(String identifier) {
		return (List<?>) processServiceIndex.get(identifier);
	}

	public Collection<?> getSessionServices(String identifier) {
		return (List<?>) sessionServiceIndex.get(identifier);
	}

	public Collection<?> getExecutionServices(String identifier) {
		return (List<?>) executionServiceIndex.get(identifier);
	}

	public Collection<?> getActionServices(String identifier) {
		return (List<?>) actionServiceIndex.get(identifier);
	}

	public Executable getExecutable(String instanceID) {
		return (Executable) executableIndex.get(instanceID);
	}

	public Executable getEntryPoint(String entryName) {
		return entryPointIndex.get(entryName);
	}

	public IExtensionRegistry getRegistry() {
		return registry;
	}

	/**
	 * Builds a map of configuration descriptor lists indexed by XML
	 * identifiers.
	 * 
	 * @param definition
	 *            The process definition.
	 * @param configurationDescriptors
	 *            The environment configuration descriptors.
	 * @return A Map of configuration descriptor lists indexed by XML
	 *         identifiers.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, LinkedList<ConfigurationDescriptor>> indexConfigurations(
			ConfigurationDescriptor[] configurationDescriptors) {
		Map<String, LinkedList<ConfigurationDescriptor>> configurationDescriptorIndex = new HashMap<String, LinkedList<ConfigurationDescriptor>>(
				configurationDescriptors.length);
		for (ConfigurationDescriptor configurationDescriptor : configurationDescriptors) {
			String xmlIdentifier = RuntimeUtils.getQualifiedIdentifier(
					configurationDescriptor.getXmlTag(),
					configurationDescriptor.getXmlNamespace());
			LinkedList<ConfigurationDescriptor> list = configurationDescriptorIndex
					.get(xmlIdentifier);
			if (list == null) {
				configurationDescriptorIndex.put(xmlIdentifier,
						list = new LinkedList<ConfigurationDescriptor>());
			}
			list.addLast(configurationDescriptor);
		}
		for (Iterator<?> i = configurationDescriptorIndex.entrySet().iterator(); i
				.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			entry.setValue(Collections.unmodifiableList(new ArrayList<Object>(
					(LinkedList<?>) entry.getValue())));
		}
		return Collections.unmodifiableMap(new HashMap<String, LinkedList>(
				configurationDescriptorIndex));
	}

	/**
	 * Builds a map of service lists indexed by identifiers.
	 * 
	 * @param definition
	 *            The process definition.
	 * @param serviceDescriptors
	 *            The environment service descriptors.
	 * @return A Map of service lists indexed by identifiers.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Object> buildServices(IProcessDefinition definition,
			List<ServiceDescriptor> serviceDescriptors) {
		Map<String, LinkedList> serviceIndex = new HashMap<String, LinkedList>(
				serviceDescriptors.size());
		for (Iterator<ServiceDescriptor> i = serviceDescriptors.iterator(); i
				.hasNext();) {
			ServiceDescriptor descriptor = i.next();
			Service service = new Service(this,
					definition.getServiceConfiguration(descriptor.getId()),
					descriptor);
			for (Iterator<?> j = service.getIdentifiers().iterator(); j
					.hasNext();) {
				String identifier = (String) j.next();
				LinkedList<Service> list = serviceIndex.get(identifier);
				if (list == null) {
					serviceIndex.put(identifier,
							list = new LinkedList<Service>());
				}
				list.addLast(service);
			}
		}
		return Collections.unmodifiableMap(new HashMap<String, LinkedList>(
				serviceIndex));
	}

	/**
	 * Builds a map of executable objects indexed by instance ID.
	 * 
	 * @param definition
	 *            The process definition.
	 * @param actionDescriptors
	 *            The environment action descriptors.
	 * @param observerDescriptors
	 *            The environment observer descriptors.
	 * @return A Map of service lists indexed by identifiers.
	 */
	private Map<String, Object> buildExecutables(IProcessDefinition definition,
			ActionDescriptor[] actionDescriptors,
			ObserverDescriptor[] observerDescriptors) {
		// Index the action descriptors.
		Map<String, ActionDescriptor> actionIndex = new HashMap<String, ActionDescriptor>();
		for (ActionDescriptor actionDescriptor : actionDescriptors) {
			actionIndex.put(actionDescriptor.getId(), actionDescriptor);
		}
		// Index the observer descriptors.
		Map<String, ObserverDescriptor> observerIndex = new HashMap<String, ObserverDescriptor>();
		for (ObserverDescriptor observerDescriptor : observerDescriptors) {
			observerIndex.put(observerDescriptor.getId(), observerDescriptor);
		}
		// Index the executable instances.
		Map<String, Executable> executableIndex = new HashMap<String, Executable>();
		// Index each action instance.
		String[] actionIDs = definition.getActionInstanceIDs();
		for (String actionID : actionIDs) {
			Action action = new Action(this,
					definition.getActionName(actionID),
					definition.getActionConfiguration(actionID), actionID,
					actionIndex.get(definition.getActionDescriptorID(actionID)));
			executableIndex.put(actionID, action);
			// Index the action's before observers.
			String[] observerIDs = definition
					.getBeforeObserverInstanceIDs(actionID);
			for (String observerID : observerIDs) {
				Observer observer = new Observer(this,
						definition.getObserverConfiguration(observerID),
						observerID, observerIndex.get(definition
								.getObserverDescriptorID(observerID)), action);
				executableIndex.put(observerID, observer);
			}
			// Index the action's after observers.
			String[] resultIDs = definition.getActionResultIDs(actionID);
			for (String resultID : resultIDs) {
				observerIDs = definition.getAfterObserverInstanceIDs(actionID,
						resultID);
				for (String observerID : observerIDs) {
					Observer observer = new Observer(this,
							definition.getObserverConfiguration(observerID),
							observerID, observerIndex.get(definition
									.getObserverDescriptorID(observerID)),
							action);
					executableIndex.put(observerID, observer);
				}
			}
		}
		return Collections.unmodifiableMap(new HashMap<String, Executable>(
				executableIndex));
	}

	/**
	 * Links the flow of the entire process and returns the entry point
	 * executable.
	 * 
	 * @param definition
	 *            The process definition.
	 * @return The entry point executable.
	 */
	private Map<String, Executable> linkExecutables(
			IProcessDefinition definition) {
		Map<String, Executable> index = new HashMap<String, Executable>();
		// Link each action instance's before observers to determine target IDs.
		Map<String, Executable> targetsByActionID = new HashMap<String, Executable>();
		String[] actionIDs = definition.getActionInstanceIDs();
		for (String actionID : actionIDs) {
			Action action = (Action) getExecutable(actionID);
			// Link the action's before observers.
			String[] observerIDs = definition
					.getBeforeObserverInstanceIDs(actionID);
			Observer previous = null;
			for (String observerID : observerIDs) {
				Observer observer = (Observer) getExecutable(observerID);
				if (previous == null) {
					targetsByActionID.put(actionID, observer);
				} else {
					previous.configure(observer);
				}
				previous = observer;
			}
			if (previous == null) {
				targetsByActionID.put(actionID, action);
			} else {
				previous.configure(action);
			}
		}
		// Link the action's results and after observers.
		for (String actionID : actionIDs) {
			Action action = (Action) getExecutable(actionID);
			String[] resultIDs = definition.getActionResultIDs(actionID);
			for (String resultID : resultIDs) {
				Executable result = null;
				String[] observerIDs = definition.getAfterObserverInstanceIDs(
						actionID, resultID);
				Observer previous = null;
				for (String observerID : observerIDs) {
					Observer observer = (Observer) getExecutable(observerID);
					if (previous == null) {
						result = observer;
					} else {
						previous.configure(observer);
					}
					previous = observer;
				}
				Executable target = targetsByActionID.get(definition
						.getActionResultTargetInstanceID(actionID, resultID));
				if (previous == null) {
					result = target;
				} else {
					previous.configure(target);
				}
				action.configure(resultID, result);
			}
		}
		String[] startActionIds = definition.getStartActionInstanceIDs();
		index.put("Default", targetsByActionID.get(startActionIds[0]));
		for (String startActionId : startActionIds) {
			System.out.println("Indexing start action: "
					+ definition.getActionName(startActionId) + "["
					+ startActionId + "]");
			index.put(definition.getActionName(startActionId),
					targetsByActionID.get(startActionId));
		}
		return index;
	}
}

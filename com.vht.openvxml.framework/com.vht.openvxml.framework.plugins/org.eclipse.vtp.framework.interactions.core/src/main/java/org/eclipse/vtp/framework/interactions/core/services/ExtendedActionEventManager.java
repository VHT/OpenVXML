package org.eclipse.vtp.framework.interactions.core.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.vtp.framework.util.ConfigurationElement;
import org.eclipse.vtp.framework.util.ExtensionRegistry;

public class ExtendedActionEventManager {
	/** The identifier for the event extension point */
	public static String eventExtensionPointId = "org.eclipse.vtp.framework.interactions.core.extendedEvents";
	/** The extension registry the shared instance reads from */
	private static ExtensionRegistry registry;
	/** The shared instance of the event manager */
	private static ExtendedActionEventManager instance;

	/**
	 * Sets the extension registry that the shared instance reads from. This is
	 * called by the engine bootstrap before the shared instance is first used.
	 * 
	 * @param extensionRegistry
	 *            The extension registry to use.
	 */
	public static synchronized void setExtensionRegistry(
			ExtensionRegistry extensionRegistry) {
		registry = extensionRegistry;
		instance = null;
	}

	/**
	 * @return The shared instance of the event manager
	 */
	public static synchronized ExtendedActionEventManager getDefault() {
		if (instance == null) {
			instance = new ExtendedActionEventManager();
		}
		return instance;
	}

	/** Indexes the currently registered events */
	private List<String> extendedEvents;

	/**
	 * Constructs a new <code>ExtendedActionEventManager</code>.
	 */
	public ExtendedActionEventManager() {
		super();
		extendedEvents = new ArrayList<String>();
		ConfigurationElement[] extensions = registry
				.getConfigurationElements(eventExtensionPointId);
		for (ConfigurationElement extension : extensions) {
			String name = extension.getAttribute("name");
			if (!extendedEvents.contains(name)) {
				extendedEvents.add(name);
			}
		}
	}

	/**
	 * Returns a list of extended events for the action with the given id
	 * 
	 * @return the list of events
	 */
	public List<String> getExtendedEvents() {
		List<String> ret = new ArrayList<String>();
		ret.addAll(extendedEvents);
		return ret;
	}

}

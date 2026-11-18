package org.eclipse.vtp.framework.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * An immutable set of configuration dictionaries loaded from the static
 * configuration XML, without any OSGi configuration admin service. The
 * <code>service.bundleLocation</code> of every dictionary is stripped.
 */
public class StaticConfiguration {
	/** The non-factory configurations, by pid. */
	private final Map<String, ConfigurationDictionary> services;
	/** The factory configurations, by factory pid then pid. */
	private final Map<String, Map<String, ConfigurationDictionary>> factories;

	/**
	 * Creates a new StaticConfiguration.
	 *
	 * @param configurationsData
	 *            The <code>configurations</code> element to load.
	 */
	public StaticConfiguration(Element configurationsData) {
		Map<String, ConfigurationDictionary> services = new LinkedHashMap<String, ConfigurationDictionary>();
		Map<String, Map<String, ConfigurationDictionary>> factories = new LinkedHashMap<String, Map<String, ConfigurationDictionary>>();
		ConfigurationDictionary[] dictionaries = ConfigurationDictionary
				.loadAll(configurationsData);
		if (dictionaries != null) {
			for (ConfigurationDictionary dictionary : dictionaries) {
				String pid = dictionary.getPid();
				if (pid == null) {
					continue;
				}
				dictionary.setBundleLocation(null);
				String factoryPid = dictionary.getFactoryPid();
				if (factoryPid == null) {
					services.put(pid, dictionary);
				} else {
					Map<String, ConfigurationDictionary> factoryIndex = factories
							.get(factoryPid);
					if (factoryIndex == null) {
						factories.put(factoryPid,
								factoryIndex = new LinkedHashMap<String, ConfigurationDictionary>());
					}
					factoryIndex.put(pid, dictionary);
				}
			}
		}
		this.services = Collections.unmodifiableMap(services);
		this.factories = Collections.unmodifiableMap(factories);
	}

	/**
	 * Returns the non-factory configuration with the specified pid.
	 *
	 * @param pid
	 *            The pid of the configuration.
	 * @return The configuration or <code>null</code> if there is none.
	 */
	public ConfigurationDictionary getConfiguration(String pid) {
		return services.get(pid);
	}

	/**
	 * Returns the configurations of the specified factory, in the order they
	 * were declared.
	 *
	 * @param factoryPid
	 *            The factory pid.
	 * @return The configurations of the factory, never <code>null</code>.
	 */
	public ConfigurationDictionary[] getFactoryConfigurations(
			String factoryPid) {
		Map<String, ConfigurationDictionary> factoryIndex = factories
				.get(factoryPid);
		if (factoryIndex == null) {
			return new ConfigurationDictionary[0];
		}
		return factoryIndex.values().toArray(
				new ConfigurationDictionary[factoryIndex.size()]);
	}
}

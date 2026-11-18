package org.eclipse.vtp.framework.util;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * An extension declared in a <code>plugin.xml</code>.
 */
public final class Extension {
	/** The extension point this extension contributes to. */
	private final String extensionPointUniqueIdentifier;
	/** The contributor of this extension. */
	private final Contributor contributor;
	/** The configuration elements of this extension. */
	private final ConfigurationElement[] elements;

	Extension(Element element, Contributor contributor) {
		this.extensionPointUniqueIdentifier = element.getAttribute("point"); //$NON-NLS-1$
		this.contributor = contributor;
		List<ConfigurationElement> list = new ArrayList<ConfigurationElement>();
		for (Element child : XMLUtilities.getChildElements(element)) {
			list.add(new ConfigurationElement(child, contributor));
		}
		this.elements = list.toArray(new ConfigurationElement[list.size()]);
	}

	/**
	 * Returns the identifier of the extension point this extension
	 * contributes to.
	 *
	 * @return The extension point identifier.
	 */
	public String getExtensionPointUniqueIdentifier() {
		return extensionPointUniqueIdentifier;
	}

	/**
	 * Returns the contributor of this extension.
	 *
	 * @return The contributor.
	 */
	public Contributor getContributor() {
		return contributor;
	}

	/**
	 * Returns the configuration elements of this extension.
	 *
	 * @return The configuration elements, in document order.
	 */
	public ConfigurationElement[] getConfigurationElements() {
		return elements.clone();
	}
}

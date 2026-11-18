package org.eclipse.vtp.framework.util;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * An element of an extension's configuration, read from a
 * <code>plugin.xml</code>.
 */
public final class ConfigurationElement {
	/** The XML element. */
	private final Element element;
	/** The contributor of the extension holding this element. */
	private final Contributor contributor;

	ConfigurationElement(Element element, Contributor contributor) {
		this.element = element;
		this.contributor = contributor;
	}

	/**
	 * Returns the name of this element.
	 *
	 * @return The name of this element.
	 */
	public String getName() {
		return element.getTagName();
	}

	/**
	 * Returns the value of the named attribute.
	 *
	 * @param name
	 *            The name of the attribute.
	 * @return The value, or <code>null</code> if the attribute is not set.
	 */
	public String getAttribute(String name) {
		return element.hasAttribute(name) ? element.getAttribute(name) : null;
	}

	/**
	 * Returns the child elements of this element.
	 *
	 * @return The child elements, in document order.
	 */
	public ConfigurationElement[] getChildren() {
		return getChildren(null);
	}

	/**
	 * Returns the child elements of this element with the given name.
	 *
	 * @param name
	 *            The name to match, or <code>null</code> for all children.
	 * @return The matching child elements, in document order.
	 */
	public ConfigurationElement[] getChildren(String name) {
		List<ConfigurationElement> children = new ArrayList<ConfigurationElement>();
		for (Element child : XMLUtilities.getChildElements(element)) {
			if (name == null || name.equals(child.getTagName())) {
				children.add(new ConfigurationElement(child, contributor));
			}
		}
		return children.toArray(new ConfigurationElement[children.size()]);
	}

	/**
	 * Returns the contributor of the extension holding this element.
	 *
	 * @return The contributor.
	 */
	public Contributor getContributor() {
		return contributor;
	}
}

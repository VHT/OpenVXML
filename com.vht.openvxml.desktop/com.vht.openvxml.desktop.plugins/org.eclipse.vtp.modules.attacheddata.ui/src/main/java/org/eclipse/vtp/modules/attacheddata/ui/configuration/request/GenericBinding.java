/*--------------------------------------------------------------------------
 * Copyright (c) 2004, 2006-2009 OpenMethods, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Trip Gilman (OpenMethods), Lonnie G. Pryor (OpenMethods)
 *    - initial API and implementation
 -------------------------------------------------------------------------*/
package org.eclipse.vtp.modules.attacheddata.ui.configuration.request;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.openmethods.openvxml.desktop.model.branding.BrandManagerListener;
import com.openmethods.openvxml.desktop.model.branding.IBrand;

/**
 * This is the base class for all bindings contained by this configuration
 * manager.
 * 
 * @author trip
 */
public abstract class GenericBinding implements BrandManagerListener {
	/** The name of this binding */
	private String name;

	/** The attached data manager that contains this binding */
	private AttachedDataManager manager = null;

	/** A map of the items of this binding */
	private Map<String, GenericBindingItem> bindingItems = new HashMap<String, GenericBindingItem>();

	/**
	 * Creates a new instance to be contained by the provided manager and has
	 * the given name. The binding initially has no binding items when created.
	 * 
	 * @param manager
	 *            The manager that contains this binding
	 * @param name
	 *            The name of this binding
	 */
	public GenericBinding(AttachedDataManager manager, String name) {
		super();
		this.manager = manager;
		manager.getBrandManager().addListener(this);
		this.name = name;
	}

	/**
	 * @return The name of this binding
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The manager that contains this binding
	 */
	public AttachedDataManager getAttachedDataManager() {
		return manager;
	}

	/**
	 * Retrieves the binding item associated with the given brand, interaction
	 * type, and language. The binding items are simply indexed in a map using
	 * the hash of the string that results from concatenating all three
	 * parameters. The given values should not be null.
	 * 
	 * @param brandId
	 *            The id of the brand associated with the item
	 * @param interactionType
	 *            The interaction type associated with the item
	 * @param language
	 *            The name of the language associated with the item
	 * @return The binding item associated with the given parameters
	 */
	protected GenericBindingItem getItem(String brandId,
			String interactionType, String language) {
		IBrand brandObject = manager.getBrandManager().getBrandById(brandId);
		GenericBindingItem bindingItemObject = bindingItems.get(brandId + ":"
				+ interactionType + ":" + language);
		while (bindingItemObject == null && brandObject.getParent() != null)
		// no binding for the requested brand, search for item within parent
		// brand
		{
			brandObject = brandObject.getParent();
			bindingItemObject = bindingItems.get(brandObject.getId() + ":"
					+ interactionType + ":" + language);
			if (bindingItemObject != null) {
				bindingItemObject = (GenericBindingItem) bindingItemObject
						.clone();
			}
		}
		return bindingItemObject;
	}

	/**
	 * Adds the provided binding item to this binding and associates it with the
	 * given brand id, interaction type, and language name. If there was already
	 * a binding item associated with the information, the item is removed from
	 * this binding and replaced with the new item.
	 * 
	 * @param brandId
	 *            The id of the brand associated with the item
	 * @param interactionType
	 *            The interaction type associated with the item
	 * @param language
	 *            The name of the language associated with the item
	 * @param item
	 *            The item being added to this binding
	 */
	protected void putItem(String brandId, String interactionType,
			String language, GenericBindingItem item) {
		bindingItems
				.put(brandId + ":" + interactionType + ":" + language, item);
		IBrand brandObject = manager.getBrandManager().getBrandById(brandId);
		while (brandObject.getParent() != null)
		// no binding for the requested brand parent, copy config down the stack
		{
			brandObject = brandObject.getParent();
			if (bindingItems.get(brandObject.getId() + ":" + interactionType
					+ ":" + language) == null) {
				bindingItems.put(brandObject.getId() + ":" + interactionType
						+ ":" + language, item);
			} else {
				break;
			}
		}
	}

	/**
	 * Loads this binding's data from the provided xml dom element.
	 * 
	 * @param bindingConfiguration
	 *            xml element containing configuration
	 */
	public void readBindingItems(Element bindingConfiguration) {
		NodeList itemList = bindingConfiguration.getElementsByTagNameNS(
				AttachedDataManager.NAMESPACE_URI, "item");
		for (int i = 0; i < itemList.getLength(); i++) {
			Element itemElement = (Element) itemList.item(i);
			bindingItems.put(itemElement.getAttribute("key"),
					readBindingItem(itemElement));
		}
	}

	/**
	 * Stores this binding's data into the given xml dom element.
	 * 
	 * @param bindingConfiguration
	 *            xml element to receive configuration
	 */
	public void writeBindingItems(Element bindingConfiguration) {
		for (Map.Entry<String, GenericBindingItem> entry : bindingItems
				.entrySet()) {
			Element itemElement = bindingConfiguration.getOwnerDocument()
					.createElementNS(AttachedDataManager.NAMESPACE_URI, "item");
			bindingConfiguration.appendChild(itemElement);
			itemElement.setAttribute("key", entry.getKey());
			writeBindingItem(entry.getValue(), itemElement);
		}
	}

	/**
	 * Subclasses should provide an implementation for this abstract method that
	 * properly reads the individual binding items this binding will contain.
	 * 
	 * @param itemConfiguration
	 *            xml element containing the item's data
	 * @return the retrieved binding item
	 */
	protected abstract GenericBindingItem readBindingItem(
			Element itemConfiguration);

	/**
	 * Subclasses should provide an implementation for this abstract method that
	 * propertly stores the individual binding items this binding will contain.
	 * 
	 * @param item
	 *            The binding item to be stored
	 * @param itemConfiguration
	 *            The xml element to store the item into
	 */
	protected abstract void writeBindingItem(GenericBindingItem item,
			Element itemConfiguration);

	@Override
	public void brandAdded(IBrand brand) {
	}

	@Override
	public void brandIdChanged(IBrand brand, String oldId) {
		List<String> toChange = new LinkedList<String>();
		for (String key : bindingItems.keySet()) {
			if (key.startsWith(oldId)) {
				toChange.add(key);
			}
		}
		for (String key : toChange) {
			GenericBindingItem item = bindingItems.remove(key);
			String[] parts = key.split(":");
			bindingItems.put(brand.getId() + ":" + parts[1] + ":" + parts[2],
					item);
		}
	}

	@Override
	public void brandNameChanged(IBrand brand, String oldName) {
	}

	@Override
	public void brandParentChanged(IBrand brand, IBrand oldParent) {
	}

	@Override
	public void brandRemoved(IBrand brand) {
		List<String> toRemove = new LinkedList<String>();
		for (String key : bindingItems.keySet()) {
			if (key.startsWith(brand.getId())) {
				toRemove.add(key);
			}
		}
		for (String key : toRemove) {
			bindingItems.remove(key);
		}
	}
}

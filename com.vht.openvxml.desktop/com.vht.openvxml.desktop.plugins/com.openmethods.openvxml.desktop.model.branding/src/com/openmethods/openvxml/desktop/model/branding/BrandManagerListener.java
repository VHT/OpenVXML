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
package com.openmethods.openvxml.desktop.model.branding;

public interface BrandManagerListener {
	public void brandAdded(IBrand brand);

	public void brandIdChanged(IBrand brand, String oldId);

	public void brandNameChanged(IBrand brand, String oldName);

	public void brandParentChanged(IBrand brand, IBrand oldParent);

	public void brandRemoved(IBrand brand);
}
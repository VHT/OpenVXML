/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.vtp.desktop.projects.interactive.core.view;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * @since 3.2
 *
 */
public class ReplicateActionProvider extends CommonActionProvider {

	private ReplicationActionGroup replicationGroup;

	private ICommonActionExtensionSite site;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator
	 * .ICommonActionExtensionSite)
	 */
	@Override
	public void init(ICommonActionExtensionSite anActionSite) {
		site = anActionSite;
		replicationGroup = new ReplicationActionGroup(site.getViewSite()
				.getShell());

	}

	@Override
	public void dispose() {
		replicationGroup.dispose();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		replicationGroup.fillActionBars(actionBars);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		replicationGroup.fillContextMenu(menu);
	}

	@Override
	public void setContext(ActionContext context) {
		replicationGroup.setContext(context);
	}

	@Override
	public void updateActionBars() {
		replicationGroup.updateActionBars();
	}
}

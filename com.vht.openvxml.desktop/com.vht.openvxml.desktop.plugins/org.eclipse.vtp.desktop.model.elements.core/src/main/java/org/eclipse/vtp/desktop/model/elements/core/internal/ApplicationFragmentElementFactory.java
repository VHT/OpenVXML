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
package org.eclipse.vtp.desktop.model.elements.core.internal;

import org.eclipse.vtp.desktop.model.core.IOpenVXMLProject;
import org.eclipse.vtp.desktop.model.core.WorkflowCore;

import com.openmethods.openvxml.desktop.model.workflow.internal.design.Design;
import com.openmethods.openvxml.desktop.model.workflow.internal.design.DesignElement;
import com.openmethods.openvxml.desktop.model.workflow.internal.design.ElementFactory;

public class ApplicationFragmentElementFactory implements ElementFactory {

	@Override
	public DesignElement createElement(Design design, Object data) {
		String specializationId = (String) data;
		IOpenVXMLProject workflowProject = WorkflowCore.getDefault().getWorkflowModel()
				.getWorkflowProject(specializationId);
		ApplicationFragmentElement afe = new ApplicationFragmentElement(specializationId,
				workflowProject.getName());
		return afe;
	}

}

/*--------------------------------------------------------------------------
f * Copyright (c) 2004, 2006-2007 OpenMethods, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Trip Gilman (OpenMethods), Lonnie G. Pryor (OpenMethods)
 *    - initial API and implementation
 -------------------------------------------------------------------------*/
package org.eclipse.vtp.desktop.media.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.vtp.framework.interactions.core.media.Content;
import org.eclipse.vtp.framework.interactions.core.media.NumberContent;

public class NumberContentCreatorPanel extends DynamicContentCreatorPanel {
	Text text = null;

	public NumberContentCreatorPanel() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.vtp.desktop.media.core.ContentCreatorPanel#createContent()
	 */
	@Override
	public Content createContent() {
		NumberContent content = new NumberContent();
		if (isDynamicSelected()) {
			content.setVariableValue(getDynamicSelection());
		} else {
			content.setStaticValue(text.getText());
		}
		content.setFormatName(getFormat());
		return content;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.vtp.desktop.media.core.DynamicContentCreatorPanel#
	 * createStaticControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createStaticControls(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		text = new Text(comp, SWT.BORDER | SWT.FLAT | SWT.SINGLE);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.vtp.desktop.media.core.DynamicContentCreatorPanel# setInitialContent
	 * (org.eclipse.vtp.framework.interactions.core.media.Content)
	 */
	@Override
	public void setInitialContent(Content content) {
		if (content instanceof NumberContent) {
			NumberContent numberContent = (NumberContent) content;
			if (numberContent.getValueType() == NumberContent.STATIC_VALUE) {
				setDynamicSelected(false);
				text.setText(numberContent.getValue());
			} else {
				setDynamicSelected(true);
				setDynamicSelection(numberContent.getValue());
			}
		}
		super.setInitialContent(content);
	}
}

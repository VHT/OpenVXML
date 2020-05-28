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
package org.eclipse.vtp.desktop.media.voice.creatorpanels;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.vtp.desktop.media.core.InputGrammarCreatorPanel;
import org.eclipse.vtp.framework.interactions.core.media.InputGrammar;
import org.eclipse.vtp.framework.interactions.voice.media.BuiltInCurrencyInputGrammar;

public class BuiltInCurrencyGrammarCreatorPanel extends InputGrammarCreatorPanel {
	BuiltInCurrencyInputGrammar grammar = new BuiltInCurrencyInputGrammar();

	public BuiltInCurrencyGrammarCreatorPanel() {
		super();
	}

	@Override
	public InputGrammar createGrammar() {
		return grammar;
	}

	@Override
	public void createControls(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setBackground(parent.getBackground());
		comp.setLayout(new GridLayout(2, false));

		this.setControl(comp);
	}

	@Override
	public void setInitialInput(InputGrammar content) {
		if (content instanceof BuiltInCurrencyInputGrammar) {
			grammar = (BuiltInCurrencyInputGrammar) content;
		}
	}

}

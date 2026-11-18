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
package org.eclipse.vtp.framework.engine.osgi;

import java.util.List;

import org.eclipse.vtp.framework.util.Contributor;

/**
 * Utilities for finding contributors.
 *
 * @author Lonnie Pryor
 */
public class OSGiUtils {
	/**
	 * Finds the contributor that matches the specified contributor.
	 * 
	 * @param contributor
	 *            The contributor to match.
	 * @param contributors
	 *            The contributors to search for a match.
	 * @return The contributor that matched the specified contributor.
	 */
	public static Contributor findBundle(Contributor contributor,
			List<Contributor> contributors) {
		String name = contributor.getName();
		for (Contributor candidate : contributors) {
			if (name.equals(candidate.getSymbolicName())) {
				return candidate;
			}
		}
		return null;
	}
}

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
package org.eclipse.vtp.desktop.model.interactive.voice.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.vtp.desktop.model.interactive.core.IMediaContainer;
import org.eclipse.vtp.desktop.model.interactive.core.IMediaFile;
import org.eclipse.vtp.desktop.model.interactive.core.internal.IMediaFileFactory;

public class AudioFileFactory implements IMediaFileFactory {

	@Override
	public IMediaFile createMediaFile(IMediaContainer container, IFile file) {
		return new AudioFile(container, file);
	}

}

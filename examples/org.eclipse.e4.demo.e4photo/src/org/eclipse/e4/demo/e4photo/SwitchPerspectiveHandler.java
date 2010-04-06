/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo;

import org.eclipse.e4.ui.model.application.MPerspective;
import org.eclipse.e4.ui.model.application.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.EList;

public class SwitchPerspectiveHandler {

	public void execute(EModelService modelService, MWindow window) {
		MPerspectiveStack ps = (MPerspectiveStack) modelService.find("DefaultPerspectiveStack", window); //$NON-NLS-1$
		EList<MPerspective> kids = ps.getChildren();
		if (ps.getSelectedElement() == kids.get(0)) {
			ps.setSelectedElement(kids.get(1));
		} else {
			ps.setSelectedElement(kids.get(0));	
		}
	}

}
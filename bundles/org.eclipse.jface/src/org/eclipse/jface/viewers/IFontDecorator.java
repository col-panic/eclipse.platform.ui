/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Font;

/**
 * The IFontDecorator is an interface for objects that return a font to
 * decorate an object.
 * 
 * If an IFontDecorator decorates a font in an object that also has
 * an IFontProvider the IFontDecorator will take precendence.
 * @see IFontProvider
 */
public interface IFontDecorator {
	
	/**
	 * Return the font for element or <code>null</code> if there
	 * is not one.
	 * 
	 * @param element
	 * @return Font or <code>null</code>
	 */
	public Font decorateFont(Object element);

}

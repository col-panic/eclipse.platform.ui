/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT, FHV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Descher <marc@descher.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.di;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to provide MMenuElements to the list of dynamically shown entries
 * within a DynamicMenuContributionItem. Usage in contribution class:
 * <p>
 * {@literal @}AboutToShow<br>
 * public void aboutToShow(List&lt;MMenuElement&gt; items) { }
 * 
 * @see org.eclipse.jface.action.IMenuListener
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AboutToShow {
	// intentionally left empty
}
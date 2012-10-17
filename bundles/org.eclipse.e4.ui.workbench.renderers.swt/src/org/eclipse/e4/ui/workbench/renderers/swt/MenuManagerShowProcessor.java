/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Marco Descher <descher@medevit.at> - Bug 389063 Dynamic Menu Contribution
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.Policy;
import org.eclipse.e4.ui.internal.workbench.swt.WorkbenchSWTActivator;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.menu.MDynamicMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;

/**
 * <code>MenuManagerShowProcessor</code> provides hooks for renderer processing
 * before and after the <code>MenuManager</code> calls out to its
 * <code>IMenuManagerListener2</code> for the <code>menuAboutToShow</code>
 * events.
 */
public class MenuManagerShowProcessor implements IMenuListener2 {

	private static void trace(String msg, Widget menu, MMenu menuModel) {
		WorkbenchSWTActivator.trace(Policy.MENUS, msg + ": " + menu + ": " //$NON-NLS-1$ //$NON-NLS-2$
				+ menuModel, null);
	}

	@Inject
	private EModelService modelService;

	@Inject
	private IRendererFactory rendererFactory;

	@Inject
	private MenuManagerRenderer renderer;

	@Inject
	private IContributionFactory contributionFactory;

	private HashMap<Menu, Runnable> pendingCleanup = new HashMap<Menu, Runnable>();

	/**
	 * DynamicContributionItem.elementId -> Contributed MMenuElement list
	 */
	private HashMap<String, ArrayList<MMenuElement>> dynamicMenuContributions = new HashMap<String, ArrayList<MMenuElement>>();

	/**
	 * DynamicContributionItem.elementId -> contribution object (instance)
	 */
	private HashMap<String, Object> dynamicMenuContributionObjects = new HashMap<String, Object>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface
	 * .action.IMenuManager)
	 */
	public void menuAboutToShow(IMenuManager manager) {
		if (!(manager instanceof MenuManager)) {
			return;
		}
		MenuManager menuManager = (MenuManager) manager;
		final MMenu menuModel = renderer.getMenuModel(menuManager);
		final Menu menu = menuManager.getMenu();

		MMenuElement[] ml = menuModel.getChildren().toArray(
				new MMenuElement[menuModel.getChildren().size()]);
		for (int i = 0; i < ml.length; i++) {

			MMenuElement currentMenuElement = ml[i];
			if (currentMenuElement instanceof MDynamicMenuContribution) {
				Object contribution = dynamicMenuContributionObjects
						.get(currentMenuElement.getElementId());
				if (contribution == null) {
					IEclipseContext context = modelService
							.getContainingContext(menuModel);
					contribution = contributionFactory.create(
							((MDynamicMenuContribution) currentMenuElement)
									.getContributionURI(), context);
					dynamicMenuContributionObjects.put(
							currentMenuElement.getElementId(), contribution);
				}

				IEclipseContext dynamicMenuContext = EclipseContextFactory
						.create();
				ArrayList<MMenuElement> mel = new ArrayList<MMenuElement>();
				dynamicMenuContext.set(List.class, mel);
				ContextInjectionFactory.invoke(contribution, AboutToShow.class,
						dynamicMenuContext);

				// remove existing entries for this dynamic contribution item if
				// there are any
				ArrayList<MMenuElement> dump = dynamicMenuContributions
						.get(currentMenuElement.getElementId());
				if (dump != null && dump.size() > 0)
					renderer.removeDynamicMenuContributions(menuManager,
							menuModel, dump);

				dynamicMenuContributions.remove(currentMenuElement
						.getElementId());

				// ensure that each element of the list has a valid element id
				// and set the parent of the entries
				for (int j = 0; j < mel.size(); j++) {
					MMenuElement menuElement = mel.get(j);
					if (menuElement.getElementId() == null
							|| menuElement.getElementId().length() < 1)
						menuElement.setElementId(currentMenuElement
								.getElementId() + "." + j); //$NON-NLS-1$
					menuElement.setParent(currentMenuElement.getParent());
					renderer.modelProcessSwitch(menuManager, menuElement);
				}
				dynamicMenuContributions.put(currentMenuElement.getElementId(),
						mel);
			}

		}

		if (menuModel != null && menuManager != null) {
			cleanUp(menu, menuModel, menuManager);
		}
		if (menuModel instanceof MPopupMenu) {
			showPopup(menu, (MPopupMenu) menuModel, menuManager);
		}
		AbstractPartRenderer obj = rendererFactory.getRenderer(menuModel,
				menu.getParent());
		if (!(obj instanceof MenuManagerRenderer)) {
			trace("Not the correct renderer: " + obj, menu, menuModel); //$NON-NLS-1$
			return;
		}
		MenuManagerRenderer renderer = (MenuManagerRenderer) obj;
		if (menuModel.getWidget() == null) {
			renderer.bindWidget(menuModel, menuManager.getMenu());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IMenuListener2#menuAboutToHide(org.eclipse.jface
	 * .action.IMenuManager)
	 */
	public void menuAboutToHide(IMenuManager manager) {
		if (!(manager instanceof MenuManager)) {
			return;
		}
		MenuManager menuManager = (MenuManager) manager;
		final MMenu menuModel = renderer.getMenuModel(menuManager);
		final Menu menu = menuManager.getMenu();

		MMenuElement[] ml = menuModel.getChildren().toArray(
				new MMenuElement[menuModel.getChildren().size()]);
		for (int i = 0; i < ml.length; i++) {

			MMenuElement currentMenuElement = ml[i];
			if (currentMenuElement instanceof MDynamicMenuContribution) {
				Object contribution = dynamicMenuContributionObjects
						.get(currentMenuElement.getElementId());

				IEclipseContext dynamicMenuContext = EclipseContextFactory
						.create();
				ArrayList<MMenuElement> mel = dynamicMenuContributions
						.get(currentMenuElement.getElementId());
				dynamicMenuContext.set(List.class, mel);
				ContextInjectionFactory.invoke(contribution, AboutToHide.class,
						dynamicMenuContext);
			}

		}

		if (menuModel != null) {
			showMenu(menu, menuModel, menuManager);
		}
	}

	private void cleanUp(final Menu menu, MMenu menuModel,
			MenuManager menuManager) {
		trace("cleanUp", menu, null); //$NON-NLS-1$
		if (pendingCleanup.isEmpty()) {
			return;
		}
		Runnable cleanUp = pendingCleanup.remove(menu);
		if (cleanUp != null) {
			trace("cleanUp.run()", menu, null); //$NON-NLS-1$
			cleanUp.run();
		}
	}

	private void showPopup(final Menu menu, final MPopupMenu menuModel,
			MenuManager menuManager) {
		// System.err.println("showPopup: " + menuModel + "\n\t" + menu);
		// we need some context foolery here
		final IEclipseContext popupContext = menuModel.getContext();
		final IEclipseContext parentContext = popupContext.getParent();
		final IEclipseContext originalChild = parentContext.getActiveChild();
		popupContext.activate();
		popupContext.set(MenuManagerRendererFilter.TMP_ORIGINAL_CONTEXT,
				originalChild);
	}

	private void showMenu(final Menu menu, final MMenu menuModel,
			MenuManager menuManager) {

		final IEclipseContext evalContext;
		if (menuModel instanceof MContext) {
			evalContext = ((MContext) menuModel).getContext();
		} else {
			evalContext = modelService.getContainingContext(menuModel);
		}
		MenuManagerRendererFilter.updateElementVisibility(menuModel, renderer,
				menuManager, evalContext, 2, true);
	}

}

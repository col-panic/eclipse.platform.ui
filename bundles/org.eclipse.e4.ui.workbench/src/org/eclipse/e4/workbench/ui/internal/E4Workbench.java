/*******************************************************************************
 * Copyright (c) 2008, 2009 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.workbench.ui.internal;

import java.util.ArrayList;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.IParameter;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MCommandParameter;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;

public class E4Workbench implements IWorkbench {
	public static final String LOCAL_ACTIVE_SHELL = "localActiveShell"; //$NON-NLS-1$
	public static final String XMI_URI_ARG = "applicationXMI"; //$NON-NLS-1$
	public static final String CSS_URI_ARG = "applicationCSS"; //$NON-NLS-1$
	public static final String CSS_RESOURCE_URI_ARG = "applicationCSSResources"; //$NON-NLS-1$
	public static final String PRESENTATION_URI_ARG = "presentationURI"; //$NON-NLS-1$

	IEclipseContext appContext;
	IPresentationEngine renderer;

	public IEclipseContext getContext() {
		return appContext;
	}

	public E4Workbench(MApplicationElement uiRoot, IEclipseContext applicationContext) {
		appContext = applicationContext;
		appContext.set(IWorkbench.class.getName(), this);

		if (uiRoot instanceof MApplication) {
			init((MApplication) uiRoot);
		}

		// Hook the global notifications
		((Notifier) uiRoot).eAdapters().add(new UIEventPublisher(appContext));

		// Create and run the UI (if any)
		createAndRunUI(uiRoot, appContext);
	}

	/**
	 * @param renderingEngineURI
	 * @param cssURI
	 * @param cssResourcesURI
	 */
	private void createAndRunUI(MApplicationElement uiRoot, IEclipseContext appContext) {
		// Has someone already created one ?
		renderer = (IPresentationEngine) appContext.get(IPresentationEngine.class.getName());
		if (renderer == null) {
			String presentationURI = (String) appContext.get(PRESENTATION_URI_ARG);
			if (presentationURI != null) {
				IContributionFactory factory = (IContributionFactory) appContext
						.get(IContributionFactory.class.getName());
				renderer = (IPresentationEngine) factory.create(presentationURI, appContext);
				appContext.set(IPresentationEngine.class.getName(), renderer);
			}
			if (renderer == null) {
				Logger logger = (Logger) appContext.get(Logger.class.getName());
				logger
						.error("Failed to create the presentation engine for URI: " + presentationURI); //$NON-NLS-1$
			}
		}

		if (renderer != null) {
			renderer.run(uiRoot, appContext);
		}
	}

	private void init(MApplication appElement) {
		Activator.trace(Policy.DEBUG_WORKBENCH, "init() workbench", null); //$NON-NLS-1$

		// fill in commands
		Activator.trace(Policy.DEBUG_CMDS, "Initialize service from model", null); //$NON-NLS-1$
		ECommandService cs = (ECommandService) appContext.get(ECommandService.class.getName());
		Category cat = cs
				.defineCategory(MApplication.class.getName(), "Application Category", null); //$NON-NLS-1$
		EList<MCommand> commands = appElement.getCommands();
		for (MCommand cmd : commands) {
			IParameter[] parms = null;
			String id = cmd.getId();
			String name = cmd.getCommandName();
			EList<MCommandParameter> modelParms = cmd.getParameters();
			if (modelParms != null && !modelParms.isEmpty()) {
				ArrayList<Parameter> parmList = new ArrayList<Parameter>();
				for (MCommandParameter cmdParm : modelParms) {
					parmList.add(new Parameter(cmdParm.getId(), cmdParm.getName(), null, null,
							cmdParm.isOptional()));
				}
				parms = parmList.toArray(new Parameter[parmList.size()]);
			}
			cs.defineCommand(id, name, null, cat, parms);
		}

		// Do a top level processHierarchy for the application?
		Workbench.processHierarchy(appElement);
	}

	/**
	 * @return
	 */
	public void close() {
		if (renderer != null) {
			renderer.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.ui.IWorkbench#run()
	 */
	public int run() {
		// TODO Auto-generated method stub
		return 0;
	}
}
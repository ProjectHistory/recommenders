/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.store;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.recommenders.internal.rcp.analysis.IRecommendersProjectLifeCycleListener;

import com.google.common.collect.Maps;

@Singleton
public class ProjectServices implements IRecommendersProjectLifeCycleListener {

    private final Map<IJavaProject, ProjectModelFacade> modelFacades = Maps.newHashMap();
    private final ProjectModelFacadeFactory facadeFactory;

    @Inject
    protected ProjectServices(final ProjectModelFacadeFactory facadeFactory) {
        this.facadeFactory = facadeFactory;
    }

    public IProjectModelFacade getModelFacade(final IJavaProject project) {
        if (project != null && modelFacades.containsKey(project)) {
            final ProjectModelFacade facade = modelFacades.get(project);
            return facade;
        } else {
            return IProjectModelFacade.NULL;
        }
    }

    @Override
    public void projectOpened(final IJavaProject project) {
        final ProjectModelFacade facade = facadeFactory.create(project);
        modelFacades.put(project, facade);
    }

    @Override
    public void projectClosed(final IJavaProject project) {
        if (!modelFacades.containsKey(project)) {
            return;
        }
        final ProjectModelFacade facade = modelFacades.remove(project);
        facade.dispose();
    }
}

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
package org.eclipse.recommenders.internal.rcp.analysis;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.recommenders.rcp.analysis.RecommendersNature;
import org.eclipse.recommenders.rcp.utils.RCPUtils;

public class RecommendersProjectLifeCycleService implements IElementChangedListener {

    private final Set<IRecommendersProjectLifeCycleListener> listeners;

    @Inject
    public RecommendersProjectLifeCycleService(final Set<IRecommendersProjectLifeCycleListener> listeners) {
        this.listeners = listeners;
        initialize();
    }

    protected void initialize() {
        registerElementChangedListener();
        simulateProjectOpenEvents();
    }

    private void registerElementChangedListener() {
        JavaCore.addElementChangedListener(this);
    }

    private void simulateProjectOpenEvents() {
        for (final IProject project : getAllOpenProjects()) {
            if (RecommendersNature.hasNature(project)) {
                final IJavaProject javaProject = toJavaProject(project);
                fireOpenEvent(javaProject);
            }
        }
    }

    protected Set<IProject> getAllOpenProjects() {
        return RCPUtils.getAllOpenProjects();
    }

    @Override
    public void elementChanged(final ElementChangedEvent event) {
        final IJavaProject javaProject = event.getDelta().getElement().getJavaProject();
        if (javaProject == null || !RecommendersNature.hasNature(javaProject.getProject())) {
            return;
        }

        switch (event.getDelta().getKind()) {
        case IJavaElementDelta.F_OPENED:
            fireOpenEvent(event.getDelta().getElement().getJavaProject());
            break;
        case IJavaElementDelta.F_CLOSED:
            fireCloseEvent(event.getDelta().getElement().getJavaProject());
            break;
        }
    }

    public IJavaProject toJavaProject(final IProject project) {
        return JavaCore.create(project);
    }

    public void fireOpenEvent(final IJavaProject javaProject) {
        for (final IRecommendersProjectLifeCycleListener listener : listeners) {
            listener.projectOpened(javaProject);
        }
    }

    public void fireCloseEvent(final IJavaProject javaProject) {
        for (final IRecommendersProjectLifeCycleListener listener : listeners) {
            listener.projectClosed(javaProject);
        }
    }

}
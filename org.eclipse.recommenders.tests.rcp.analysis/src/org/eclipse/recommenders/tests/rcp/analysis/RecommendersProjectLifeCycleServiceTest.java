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
package org.eclipse.recommenders.tests.rcp.analysis;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.recommenders.internal.rcp.analysis.IDs;
import org.eclipse.recommenders.internal.rcp.analysis.IRecommendersProjectLifeCycleListener;
import org.eclipse.recommenders.internal.rcp.analysis.RecommendersProjectLifeCycleService;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class RecommendersProjectLifeCycleServiceTest {

    private final IRecommendersProjectLifeCycleListener listener = Mockito
            .mock(IRecommendersProjectLifeCycleListener.class);
    private final IJavaProject simulateOpenProject = createProjectMock();
    private final IJavaProject javaProject = createProjectMock();
    private final RecommendersProjectLifeCycleService sut = createSut(listener);

    @Test
    public void testSimulateOpenEvents() {
        verify(listener).projectOpened(simulateOpenProject);
    }

    @Test
    public void testOpenEvent() {
        final ElementChangedEvent event = createEvent(IJavaElementDelta.F_OPENED);
        sut.elementChanged(event);

        verify(listener).projectOpened(javaProject);
        verify(listener, never()).projectClosed(any(IJavaProject.class));
    }

    @Test
    public void testClosedEvent() {
        final ElementChangedEvent event = createEvent(IJavaElementDelta.F_CLOSED);
        sut.elementChanged(event);

        verify(listener).projectClosed(javaProject);
        verify(listener, never()).projectOpened(javaProject);
    }

    @Test
    public void testUnobservedEvent() {
        final ElementChangedEvent event = createEvent(IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED);
        sut.elementChanged(event);

        verify(listener, never()).projectOpened(javaProject);
        verify(listener, never()).projectClosed(any(IJavaProject.class));
    }

    private ElementChangedEvent createEvent(final int kind) {
        final IJavaElement element = mock(IJavaElement.class);
        final IJavaElementDelta delta = mock(IJavaElementDelta.class);
        when(delta.getKind()).thenReturn(kind);
        when(delta.getElement()).thenReturn(element);
        when(element.getJavaProject()).thenReturn(javaProject);
        return new ElementChangedEvent(delta, 0);
    }

    private IProject createRecommendersProjectMock() {
        final IProject project = mock(IProject.class);
        final ProjectDescription description = new ProjectDescription();
        description.setNatureIds(ArrayUtils.toArray(IDs.NATURE_ID));
        try {
            when(project.getDescription()).thenReturn(description);
        } catch (final CoreException e) {
            Assert.fail();
        }

        return project;
    }

    private IJavaProject createProjectMock() {
        final IJavaProject javaProject = mock(IJavaProject.class);
        final IProject project = createRecommendersProjectMock();

        when(javaProject.getProject()).thenReturn(project);

        return javaProject;
    }

    public RecommendersProjectLifeCycleService createSut(final IRecommendersProjectLifeCycleListener... listeners) {
        return new RecommendersProjectLifeCycleService(Sets.newHashSet(listeners)) {

            @Override
            protected Set<IProject> getAllOpenProjects() {
                return Sets.newHashSet(createRecommendersProjectMock());
            }

            @Override
            public IJavaProject toJavaProject(final IProject project) {
                return simulateOpenProject;
            }
        };
    }

}
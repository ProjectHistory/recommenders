/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.IProjectModelFacade;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ProjectServices;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public final class CallsProviderTest {

    @Test
    public void testCallsProvider() throws JavaModelException {
        final IProjectModelFacade modelFacade = Mockito.mock(IProjectModelFacade.class);
        Mockito.when(modelFacade.hasModel(Matchers.any(ITypeName.class))).thenReturn(true);
        final IObjectMethodCallsNet model = Mockito.mock(IObjectMethodCallsNet.class);
        Mockito.when(modelFacade.acquireModel(Matchers.any(ITypeName.class))).thenReturn(model);
        final Tuple<IMethodName, Double> call = Tuple.create(null, 0.0);
        final SortedSet<Tuple<IMethodName, Double>> calls = new TreeSet<Tuple<IMethodName, Double>>();
        calls.add(call);
        Mockito.when(model.getRecommendedMethodCalls(Matchers.anyDouble(), Matchers.anyInt())).thenReturn(calls);
        final ProjectServices projectServices = Mockito.mock(ProjectServices.class);
        final IJavaElementSelection selection = ExtDocUtils.getSelection();
        IJavaProject javaProject = selection.getJavaElement().getJavaProject();
        Mockito.when(projectServices.getModelFacade(javaProject)).thenReturn(
                modelFacade);
        final CallsProvider provider = new CallsProvider(projectServices, null, ServerUtils.getGenericServer());

        final ILocalVariable variable = Mockito.mock(ILocalVariable.class);
        Mockito.when(variable.getTypeSignature()).thenReturn("Button;");
        final IJavaProject project = Mockito.mock(IJavaProject.class);
        final IType type = Mockito.mock(IType.class);
        Mockito.when(project.findType(Matchers.anyString())).thenReturn(type);
        Mockito.when(variable.getJavaProject()).thenReturn(project);
        // Mockito.when(selection.getJavaElement()).thenReturn(variable);

        provider.createContentControl(ExtDocUtils.getShell());
        Assert.assertTrue(provider.selectionChanged(selection));
    }
}
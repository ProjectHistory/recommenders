/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.commons.analysis.analyzers;

import static org.eclipse.recommenders.internal.commons.analysis.utils.WalaNameUtils.wala2recMethodName;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createEntryPointMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createMethod;
import static org.junit.Assert.assertEquals;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.NameMethodAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.junit.Before;
import org.junit.Test;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;

public class NameMethodAnalyzerTest {
    MethodDeclaration data;

    IMethodAnalyzer sut;

    @Before
    public void beforeTest() {
        data = MethodDeclaration.create();
        sut = new NameMethodAnalyzer();
    }

    @Test
    public void testAnalyzeMethod() {
        final IMethod method = createMethod("Lsome/Class", "someMethod", "()V");
        final Entrypoint entrypoint = createEntryPointMock(method);
        final IMethodName methodName = wala2recMethodName(method);
        // exercise
        sut.analyzeMethod(entrypoint, data, null);
        //
        assertEquals(methodName, data.name);
    }

    @Test(expected = Exception.class)
    public void testModule() {
        new NameMethodAnalyzerPluginModule().configure();
    }
}

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
package org.eclipse.recommenders.completion.rcp;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.internal.completion.rcp.IntelligentCompletionContext;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;

import com.google.inject.Inject;

public class IntelligentCompletionContextResolver {

    private final JavaElementResolver resolver;
    private JavaContentAssistInvocationContext cachedJavaContext;
    private IntelligentCompletionContext cachedIntelligentContext;

    @Inject
    public IntelligentCompletionContextResolver(final JavaElementResolver resolver) {
        this.resolver = resolver;
    }

    public IIntelligentCompletionContext resolveContext(final JavaContentAssistInvocationContext context) {
        if (!context.equals(cachedJavaContext)) {
            cachedJavaContext = context;
            cachedIntelligentContext = createCompletionContext(context);
        }

        return cachedIntelligentContext;
    }

    private IntelligentCompletionContext createCompletionContext(final JavaContentAssistInvocationContext jCtx) {
        final IntelligentCompletionContext iCtx = new IntelligentCompletionContext(jCtx, resolver);
        return iCtx;
    }

    // public boolean hasProjectRecommendersNature(final JavaContentAssistInvocationContext jCtx) {
    // final IProject project = getProjectFromContext(jCtx);
    // return RecommendersNature.hasNature(project);
    // }

    public IProject getProjectFromContext(final JavaContentAssistInvocationContext jCtx) {
        final ICompilationUnit cu = jCtx.getCompilationUnit();
        final IJavaProject javaProject = cu.getJavaProject();
        final IProject project = javaProject.getProject();
        return project;
    }
}
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
package org.eclipse.recommenders.internal.rcp;

import static org.eclipse.recommenders.commons.utils.Checks.cast;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.recommenders.rcp.IAstProvider;

import com.google.common.collect.MapMaker;

@Singleton
public class CachingAstProvider implements IAstProvider, IElementChangedListener {

    private final Map<ICompilationUnit, CompilationUnit> cache = new MapMaker().weakValues().weakKeys()
            .expiration(5, TimeUnit.MINUTES).makeMap();

    @Override
    public CompilationUnit get(final ICompilationUnit compilationUnit) {
        return cache.get(compilationUnit);
    }

    @Override
    public void elementChanged(final ElementChangedEvent event) {
        final IJavaElementDelta delta = event.getDelta();
        final CompilationUnit ast = delta.getCompilationUnitAST();
        final ICompilationUnit cu = cast(ast.getJavaElement());
        cache.put(cu, ast);
    }
}

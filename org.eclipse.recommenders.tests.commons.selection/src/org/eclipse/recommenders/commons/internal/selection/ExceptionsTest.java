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
package org.eclipse.recommenders.commons.internal.selection;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.Assert;
import org.junit.Test;

public final class ExceptionsTest {

    @Test
    // (expected = IllegalArgumentException.class)
    public void testJavaElementLocationResolver() {
        final ASTNode node = AST.newAST(AST.JLS3).createInstance(ASTNode.ANNOTATION_TYPE_DECLARATION);
        JavaElementLocationResolver.resolveLocation(null, node);
        Assert.assertTrue(true);
    }

}
/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommender.rcp.codecompletion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.CompilerBindings;
import org.junit.Test;

public class CompilerBindingsTest {

    @Test
    public void testCanParseSimpleType() {
        final ReferenceBinding mock = createSimpleBinding("Ltest/Class;");

        final Option<ITypeName> actual = CompilerBindings.toTypeName(mock);
        assertTrue(actual.hasValue());
    }

    @Test
    public void testCanParsePrimitiveType() {
        final ReferenceBinding mock = createSimpleBinding("J");

        final Option<ITypeName> actual = CompilerBindings.toTypeName(mock);
        assertEquals(VmTypeName.LONG, actual.get());
    }

    private ReferenceBinding createSimpleBinding(final String type) {
        final ReferenceBinding mock = mock(ReferenceBinding.class);
        when(mock.computeUniqueKey()).thenReturn("J".toCharArray());
        return mock;
    }
}
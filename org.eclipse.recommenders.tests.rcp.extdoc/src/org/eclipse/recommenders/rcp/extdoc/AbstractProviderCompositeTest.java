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
package org.eclipse.recommenders.rcp.extdoc;

import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.junit.Assert;
import org.junit.Test;

public final class AbstractProviderCompositeTest {

    @Test
    public void testCreateControl() {
        final AbstractProviderComposite composite = new ProviderComposite();
        final Composite control = (Composite) composite.createControl(ExtDocUtils.getShell(), null);

        Assert.assertEquals(3, control.getChildren().length);
        composite.disposeChildren(control);
        Assert.assertEquals(0, control.getChildren().length);
    }

    private static final class ProviderComposite extends AbstractProviderComposite {

        @Override
        public boolean isAvailableForLocation(final JavaElementLocation location) {
            return false;
        }

        @Override
        public boolean selectionChanged(final IJavaElementSelection context) {
            return false;
        }

        @Override
        protected Control createContentControl(final Composite parent) {
            return new Composite(parent, SWT.NONE);
        }

    }

}
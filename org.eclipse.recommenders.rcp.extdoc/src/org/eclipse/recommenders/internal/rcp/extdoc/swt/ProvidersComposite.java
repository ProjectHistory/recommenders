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
package org.eclipse.recommenders.internal.rcp.extdoc.swt;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

final class ProvidersComposite extends Composite {

    private final ScrolledComposite scrolledParent;

    ProvidersComposite(final ScrolledComposite parent, final int style) {
        super(parent, style);
        scrolledParent = parent;
        setLayout();
        setBackgroundColor(parent.getShell().getDisplay());
    }

    private void setLayout() {
        final GridLayout grid = new GridLayout(1, false);
        grid.verticalSpacing = 4;
        grid.marginWidth = 0;
        grid.marginHeight = 0;
        grid.horizontalSpacing = 0;
        setLayout(grid);
    }

    private void setBackgroundColor(final Display display) {
        final ColorRegistry registry = JFaceResources.getColorRegistry();
        final RGB backgroundColor = registry.getRGB("org.eclipse.jdt.ui.JavadocView.backgroundColor");
        if (backgroundColor == null) {
            setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        } else {
            setBackground(new Color(display, backgroundColor));
        }
        setBackgroundMode(SWT.INHERIT_FORCE);
    }

    @Override
    public void layout(final boolean changed) {
        super.layout(changed);
        scrolledParent.setMinHeight(computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y + 15);
    }

    @Override
    public boolean setFocus() {
        return scrolledParent.forceFocus();
    }

}
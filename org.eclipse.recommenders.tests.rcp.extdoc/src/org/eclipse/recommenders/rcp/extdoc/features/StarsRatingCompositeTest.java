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
package org.eclipse.recommenders.rcp.extdoc.features;

import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.server.extdoc.GenericServer;
import org.eclipse.recommenders.server.extdoc.types.RatingSummary;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.commons.extdoc.TestUtils;
import org.eclipse.swt.widgets.Shell;

import org.junit.Test;

public final class StarsRatingCompositeTest {

    @Test
    public void testCreate() {
        final Shell shell = ExtDocUtils.getShell();
        final IProvider provider = ExtDocUtils.getTestProvider();

        provider.createControl(shell, null);

        final GenericServer server = ServerUtils.getGenericServer();
        for (final IName name : TestUtils.getDefaultNames()) {
            final StarsRatingComposite composite = CommunityFeatures.create(name, provider, server)
                    .loadStarsRatingComposite(shell);
            composite.addRating(4, RatingSummary.create(0, 0, null));
        }
    }
}
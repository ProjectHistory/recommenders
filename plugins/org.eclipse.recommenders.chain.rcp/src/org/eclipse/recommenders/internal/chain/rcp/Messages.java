/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.chain.rcp;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.chain.rcp.messages"; //$NON-NLS-1$

    public static String PROPOSAL_LABEL_ELEMENTS;

    public static String PREFPAGE_DESCRIPTION_CHAINS;

    public static String PREFPAGE_FOOTER_IGNORED_TYPES_WARNING;

    public static String FIELD_LABEL_IGNORED_TYPES;
    public static String FIELD_LABEL_MAX_CHAINS;
    public static String FIELD_LABEL_MAX_CHAIN_LENGTH;
    public static String FIELD_LABEL_MIN_CHAIN_LENGTH;
    public static String FIELD_LABEL_TIMEOUT;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}

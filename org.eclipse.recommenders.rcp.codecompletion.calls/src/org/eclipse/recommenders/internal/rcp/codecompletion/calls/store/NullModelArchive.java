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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.store;

import static org.eclipse.recommenders.commons.utils.Throws.throwIllegalStateException;

import java.io.IOException;

import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;

public class NullModelArchive implements IModelArchive {

    @Override
    public void close() throws IOException {
    }

    @Override
    public Manifest getManifest() {
        return Manifest.NULL;
    }

    @Override
    public boolean hasModel(final ITypeName name) {
        return false;
    }

    @Override
    public IObjectMethodCallsNet loadModel(final ITypeName name) {
        throw throwIllegalStateException("Not allowed to load non existing model. Call hasModel() to check existance.");
    }
}
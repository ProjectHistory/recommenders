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
package org.eclipse.recommenders.commons.codesearch.client;

public class ServerErrorException extends ServerCommunicationException {

    private static final long serialVersionUID = 1L;

    public ServerErrorException(final Throwable e) {
        super(e);
    }
}
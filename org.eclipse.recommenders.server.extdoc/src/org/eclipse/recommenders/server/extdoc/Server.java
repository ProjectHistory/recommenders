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
package org.eclipse.recommenders.server.extdoc;

import java.util.Map;

final class Server {

    private static CouchDB database = new CouchDB("localhost", "extdoc");
    private static final CouchDBCache CACHE = new CouchDBCache();

    private Server() {
    }

    protected static Map<String, Object> getDocument(final String docId) {
        return CACHE.getDocument(docId);
    }

    protected static void storeOrUpdateDocument(final String docId, final Map<String, Object> attributes) {
        CACHE.storeOrUpdateDocument(docId, attributes);
        // TODO: Put in a better spot later.
        synchronize();
    }

    protected static void synchronize() {
        CACHE.synchronize(database);
    }

    protected static void setDatabase(final CouchDB database) {
        Server.database = database;
        CACHE.clear();
    }

}

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
package org.eclipse.recommenders.internal.server.console;

import static com.google.common.base.Charsets.UTF_8;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.apache.commons.io.filefilter.TrueFileFilter.INSTANCE;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.eclipse.recommenders.internal.server.console.Activator.BUNDLE_ID;
import static org.eclipse.recommenders.utils.rcp.LoggingUtils.newInfo;
import static org.eclipse.recommenders.utils.rcp.LoggingUtils.newWarning;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.recommenders.internal.server.console.ConsoleGuiceModule.LocalCouchdb;
import org.eclipse.recommenders.webclient.NotFoundException;
import org.eclipse.recommenders.webclient.TransactionResult;
import org.eclipse.recommenders.webclient.WebServiceClient;

import com.google.common.io.Files;
import com.google.inject.Inject;

public class InitalizeDatabaseCommand implements Callable<IStatus> {

    private final WebServiceClient client;
    private final File configArea;

    private final MultiStatus result = new MultiStatus("org.eclipse.recommenders.server.console", 0,
            "Operation Report", null);

    @Inject
    public InitalizeDatabaseCommand(@LocalCouchdb final WebServiceClient client,
            @LocalCouchdb final File couchConfigurationArea) {
        this.client = client;
        this.configArea = couchConfigurationArea;
    }

    @Override
    public IStatus call() throws Exception {
        for (final File db : configArea.listFiles()) {
            if (db.isFile()) {
                continue;
            }
            findOrCreateDatabase(db.getName());
            final Iterator<File> it = iterateFiles(db, INSTANCE, INSTANCE);
            while (it.hasNext()) {
                final File next = it.next();
                if (shouldIgnore(next)) {
                    continue;
                }
                putDocument(next);
            }
        }
        return result;
    }

    private boolean shouldIgnore(final File f) {
        final String name = f.getName();
        return name.startsWith(".") || !name.endsWith(".json");
    }

    private void findOrCreateDatabase(final String databaseName) {
        try {
            client.doGetRequest(databaseName, TransactionResult.class);
        } catch (final NotFoundException nfe) {
            client.doPutRequest(databaseName, "", TransactionResult.class);
        }
    }

    private void putDocument(final File contentFile) throws IOException {

        String path = removeStart(contentFile.getAbsolutePath(), configArea.getAbsolutePath() + "/");
        path = StringUtils.removeEnd(path, ".json");
        try {
            final String content = Files.toString(contentFile, UTF_8);
            client.createRequestBuilder(path).type(TEXT_PLAIN_TYPE).put(content);
            result.add(newInfo(BUNDLE_ID, "Put %s to %s", path, client.getBaseUrl()));
        } catch (final Exception e) {
            final IStatus warn = newWarning(e, BUNDLE_ID, "Didn't put contents of %s. May already exist?: %s",
                    contentFile, e.getMessage());
            result.add(warn);
        }
    }
}

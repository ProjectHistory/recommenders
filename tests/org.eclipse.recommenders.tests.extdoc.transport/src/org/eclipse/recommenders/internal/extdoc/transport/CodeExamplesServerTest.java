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
package org.eclipse.recommenders.internal.extdoc.transport;

import org.eclipse.recommenders.extdoc.transport.CodeExamplesServer;
import org.eclipse.recommenders.extdoc.transport.types.CodeExamples;
import org.eclipse.recommenders.tests.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.extdoc.TestTypeUtils;
import org.junit.Test;

public final class CodeExamplesServerTest {

    private final CodeExamplesServer server = new CodeExamplesServer(ServerUtils.getServer(),
            ServerUtils.getUsernameListener());

    @Test
    public void testGetOverridenMethodCodeExamples() {
        final CodeExamples examples = server.getOverridenMethodCodeExamples(TestTypeUtils.getDefaultMethod());
        examples.getExamples();
    }

    @Test
    public void testGetTypeCodeExamples() {
        final CodeExamples examples = server.getTypeCodeExamples(TestTypeUtils.getDefaultType());
        examples.getExamples();
    }
}

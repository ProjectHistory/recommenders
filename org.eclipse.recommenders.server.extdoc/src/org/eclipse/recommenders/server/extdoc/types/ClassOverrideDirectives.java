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
package org.eclipse.recommenders.server.extdoc.types;

import java.util.Map;

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.rcp.extdoc.IServerType;

public final class ClassOverrideDirectives implements IServerType {

    private final String providerId = getClass().getSimpleName();
    private ITypeName type;

    private int numberOfSubclasses;
    private Map<IMethodName, Integer> overrides;

    public static ClassOverrideDirectives create(final ITypeName type, final int numberOfSubclasses,
            final Map<IMethodName, Integer> overriddenMethods) {
        final ClassOverrideDirectives res = new ClassOverrideDirectives();
        res.type = type;
        res.numberOfSubclasses = numberOfSubclasses;
        res.overrides = overriddenMethods;
        res.validate();
        return res;
    }

    public int getNumberOfSubclasses() {
        return numberOfSubclasses;
    }

    public Map<IMethodName, Integer> getOverrides() {
        return overrides;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue("ClassOverrideDirectives".equals(providerId));
        Checks.ensureIsNotNull(type);
        Checks.ensureIsGreaterOrEqualTo(numberOfSubclasses, 1, null);
        Checks.ensureIsFalse(overrides.isEmpty(), "empty overrides not allowed.");
    }

}
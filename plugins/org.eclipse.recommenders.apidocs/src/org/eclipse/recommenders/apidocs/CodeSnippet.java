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
package org.eclipse.recommenders.apidocs;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.names.IMethodName;

public final class CodeSnippet {

    private IMethodName origin;
    private String code;

    public static CodeSnippet create(final IMethodName origin, final String code) {
        final CodeSnippet res = new CodeSnippet();
        res.origin = origin;
        res.code = code;
        res.validate();
        return res;
    }

    public IMethodName getOrigin() {
        return origin;
    }

    public String getCode() {
        return code;
    }

    public void validate() {
        Checks.ensureIsNotEmpty(code, "empty code fragments not allowed.");
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}

/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.apidocs;

import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

public interface IExtdocResource {

    String URL_CLASS_OVERRIDES = "class-overrides";
    String URL_CLASS_OVERRIDE_PATTERNS = "class-overrides-patterns";
    String URL_TYPE_USAGE_SNIPPETS = "type-usage-snippets";
    String URL_CLASS_SELF_CALLS = "class-selfcalls";
    String URL_METHOD_SELF_CALLS = "method-selfcalls";

    ClassOverrideDirectives findClassOverrideDirectives(final ITypeName type);

    ClassSelfcallDirectives findClassSelfcallDirectives(final ITypeName type);

    MethodSelfcallDirectives findMethodSelfcallDirectives(final IMethodName method);

    ClassOverridePatterns findClassOverridePatterns(final ITypeName type);

    CodeExamples findCodeExamples(final ITypeName type);
}

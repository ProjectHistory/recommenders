/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.commons.analysis.codeelements;

import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import com.google.common.collect.Sets;

public class TypeDeclaration implements INamedCodeElement {

    public static TypeDeclaration create() {
        final TypeDeclaration type = new TypeDeclaration();
        return type;
    }

    public static TypeDeclaration create(final ITypeName typeName, final ITypeName superclassName) {
        final TypeDeclaration type = new TypeDeclaration();
        type.name = typeName;
        type.superclass = superclassName;
        return type;
    }

    @Override
    public IName getName() {
        return name;
    }

    /**
     * use {@link #create(ITypeName, ITypeName)} to create an instance of this
     * class.
     */
    protected TypeDeclaration() {
        // no-one should instantiate this class directly
    }

    public ITypeName name;

    public ITypeName superclass;

    public Set<ITypeName> interfaces = Sets.newHashSet();

    public Set<ITypeName> fields = Sets.newHashSet();

    public Set<MethodDeclaration> methods = Sets.newHashSet();

    public int line;

    public Set<TypeDeclaration> memberTypes = Sets.newHashSet();

    public int modifiers;

    public void accept(final CompilationUnitVisitor v) {

        if (v.visit(this)) {
            for (final TypeDeclaration type : memberTypes) {
                type.accept(v);
            }
            for (final MethodDeclaration method : methods) {
                method.accept(v);
            }
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public MethodDeclaration findMethod(final IMethodName methodName) {
        for (final MethodDeclaration method : methods) {
            if (method.name == methodName) {
                return method;
            }
        }
        // its not one of our methods declared in here... check our nested types
        // next:
        for (final TypeDeclaration nestedType : memberTypes) {
            final MethodDeclaration res = nestedType.findMethod(methodName);
            if (res != null) {
                return res;
            }
        }
        // its not in one of our nested types. Check each method whether it
        // contains a nested type that declares this
        // method:
        for (final MethodDeclaration method : methods) {
            for (final TypeDeclaration nestedType : method.nestedTypes) {
                final MethodDeclaration res = nestedType.findMethod(methodName);
                if (res != null) {
                    return res;
                }
            }
        }
        // sorry, we couldn't find any matching method.
        return null;
    }
}

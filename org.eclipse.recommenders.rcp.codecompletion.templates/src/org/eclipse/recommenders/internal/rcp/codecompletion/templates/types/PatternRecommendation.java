/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.types;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

/**
 * Encapsulates one recommendation received from the models store.
 */
public final class PatternRecommendation implements Comparable<PatternRecommendation> {

    private String name;
    private ITypeName type;
    private ImmutableList<IMethodName> methods;
    private int probability;

    /**
     * @param name
     *            The name this pattern was given within the models store.
     * @param type
     *            The type of the variable this recommendation was created for.
     * @param methods
     *            The pattern's methods as obtained from the model store.
     * @param probability
     *            Probability that this pattern is used in the observed
     *            occasion.
     * @return The <code>PatternRecommendation</code> encapsulating the given
     *         parameters.
     */
    public static PatternRecommendation create(final String name, final ITypeName type,
            final List<IMethodName> methods, final int probability) {
        final PatternRecommendation recommendation = new PatternRecommendation();
        recommendation.name = Checks.ensureIsNotNull(name);
        recommendation.type = Checks.ensureIsNotNull(type);
        recommendation.methods = ImmutableList.copyOf(methods);
        recommendation.probability = probability;
        return recommendation;
    }

    /**
     * @return The name this pattern was given within the models store.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The type of the variable this recommendation was created for.
     */
    public ITypeName getType() {
        return type;
    }

    /**
     * @return The pattern's methods as obtained from the model store.
     */
    public ImmutableList<IMethodName> getMethods() {
        return methods;
    }

    /**
     * @return Probability that this pattern is used in the observed occasion.
     */
    public int getProbability() {
        return probability;
    }

    @Override
    public int hashCode() {
        return methods.hashCode();
    }

    @Override
    public int compareTo(final PatternRecommendation other) {
        return Integer.valueOf(probability).compareTo(other.probability);
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof PatternRecommendation && methods.equals(((PatternRecommendation) object).methods);
    }
}

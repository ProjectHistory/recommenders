/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.server.codesearch.lucene;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Explanation;
import org.eclipse.recommenders.commons.utils.Bag;
import org.eclipse.recommenders.commons.utils.HashBag;

public class GenericTermHitratioFeatureScorer implements SingleFeatureScorer {

    private final IndexReader reader;
    private final Bag<Integer> docIds = HashBag.newHashBag();
    private final List<Term> terms;

    public GenericTermHitratioFeatureScorer(final IndexReader reader, final List<Term> terms) throws IOException {
        this.reader = reader;
        this.terms = terms;
    }

    @Override
    public Set<Integer> findRelevantDocs() throws IOException {
        for (final Term term : terms) {
            final TermDocs termDocs = reader.termDocs(term);
            while (termDocs.next()) {
                final int docId = termDocs.doc();
                docIds.add(docId);
            }
        }
        return docIds.elements();
    }

    @Override
    public float scoreDoc(final int doc) throws IOException {
        if (terms.isEmpty()) {
            return 0;
        }
        final int numberOfTermsInExampleAndRequest = docIds.count(doc);
        final int numberOfTermsInRequest = terms.size();
        final float score = numberOfTermsInExampleAndRequest / (float) numberOfTermsInRequest;
        return score;
    }

    @Override
    public Explanation explainScore(final int doc) throws IOException {
        return new Explanation(scoreDoc(doc), getIdentifier());
    }

    @Override
    public String getIdentifier() {
        return this.getClass().getSimpleName();
    }

}
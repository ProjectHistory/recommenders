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
package org.eclipse.recommenders.tests.rcp.codecompletion.subwords;

import static org.eclipse.recommenders.tests.rcp.codecompletion.subwords.SubwordsMockUtils.mockCompletionProposal;
import static org.eclipse.recommenders.tests.rcp.codecompletion.subwords.SubwordsMockUtils.mockInvocationContext;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsJavaMethodCompletionProposal;
import org.junit.Before;
import org.junit.Test;

public class RegexMatcherTest {

    private static final String c1 = "setFilters(ViewerFilter[] filters) : void - StructuredViewer";
    private SubwordsJavaMethodCompletionProposal sut;

    @Before
    public void before() {
        sut = createJavaCompletionProposal();
    }

    @Test
    public void testLowerCaseRegexMatch() {
        assertTrue(sut.isPrefix("strs", c1));
    }

    @Test
    public void testEmptyPrefixInput() {
        assertTrue(sut.isPrefix("", c1));
    }

    @Test
    public void testExactMatch() {
        assertTrue(sut.isPrefix("setFilters", c1));
    }

    @Test
    public void testExactMatchButLowerCase() {
        assertTrue(sut.isPrefix("setfilters", c1));
    }

    @Test
    public void testPrefixMatchButLowerCase() {
        assertTrue(sut.isPrefix("setf", c1));
    }

    @Test
    public void testExactMatchPlusOneChar() {
        assertFalse(sut.isPrefix("setFilters1", c1));
    }

    @Test
    public void testUpperCaseLetterMatch() {
        assertTrue(sut.isPrefix("sFi", c1));
    }

    @Test
    public void testUpperCaseLetterFail() {
        assertFalse(sut.isPrefix("sLt", c1));
    }

    @Test
    public void testReturnTypeInProposalIgnored() {
        assertFalse(sut.isPrefix("void", c1));
    }

    private SubwordsJavaMethodCompletionProposal createJavaCompletionProposal() {
        final CompletionProposal dummyProposal = mockCompletionProposal();
        final JavaContentAssistInvocationContext dummyInvocationContext = mockInvocationContext();
        final SubwordsJavaMethodCompletionProposal someSubwordsProposal = new SubwordsJavaMethodCompletionProposal(
                dummyProposal, dummyInvocationContext);
        return someSubwordsProposal;
    }

}
/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.processable;

import static org.eclipse.recommenders.internal.completion.rcp.Constants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerRegistry;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.ICompletionContextFunction;
import org.eclipse.recommenders.internal.completion.rcp.EmptyCompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.EnableCompletionProposal;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.rcp.SharedImages;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class IntelligentCompletionProposalComputer extends ProcessableCompletionProposalComputer {

    private SessionProcessorDescriptor[] descriptors;
    private SharedImages images;

    @Inject
    public IntelligentCompletionProposalComputer(SessionProcessorDescriptor[] descriptors,
            ProcessableProposalFactory proposalFactory, IAstProvider astProvider, SharedImages images,
            Map<CompletionContextKey, ICompletionContextFunction> map) {
        super(new ProcessableProposalFactory(), Sets.<SessionProcessor>newLinkedHashSet(), astProvider, map);
        this.descriptors = descriptors;
        this.images = images;
    }

    @Override
    public void sessionStarted() {
        processors.clear();
        for (SessionProcessorDescriptor d : descriptors) {
            if (d.isEnabled()) {
                processors.add(d.getProcessor());
            }
        }
        super.sessionStarted();
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
            IProgressMonitor monitor) {
        List<ICompletionProposal> res = super.computeCompletionProposals(context, monitor);
        int offset = context.getInvocationOffset();
        if (!isContentAssistConfigurationOkay()) {
            EnableCompletionProposal config = new EnableCompletionProposal(images, offset);
            if (!res.isEmpty()) {
                // Return the configure proposal:
                return Collections.<ICompletionProposal>singletonList(config);
            }
            // make sure that we have at least two proposal to prevent auto-apply
            res.add(new EmptyCompletionProposal(offset));
            res.add(config);
            return res;
        }
        return res;
    }

    @VisibleForTesting
    protected boolean isContentAssistConfigurationOkay() {
        Set<String> cats = Sets.newHashSet(PreferenceConstants.getExcludedCompletionProposalCategories());
        if (cats.contains(RECOMMENDERS_ALL_CATEGORY_ID)) {
            // we are excluded on default tab?
            // then we are not on default tab NOW. We are on a subsequent tab.
            // then make completions:
            return true;
        }
        if (isJdtAllEnabled(cats) || isMylynInstalledAndEnabled(cats)) {
            return false;
        }
        return true;
    }

    private boolean isMylynInstalledAndEnabled(Set<String> cats) {
        return isMylynInstalled() && !cats.contains(MYLYN_ALL_CATEGORY);
    }

    private boolean isJdtAllEnabled(Set<String> cats) {
        return !cats.contains(JDT_ALL_CATEGORY);
    }

    public static boolean isMylynInstalled() {
        CompletionProposalComputerRegistry reg = CompletionProposalComputerRegistry.getDefault();
        for (CompletionProposalCategory cat : reg.getProposalCategories()) {
            if (cat.getId().equals(MYLYN_ALL_CATEGORY)) {
                return true;
            }
        }
        return false;
    }
}

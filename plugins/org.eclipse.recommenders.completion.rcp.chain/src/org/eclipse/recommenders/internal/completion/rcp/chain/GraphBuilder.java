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
package org.eclipse.recommenders.internal.completion.rcp.chain;

import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Throws.throwCancelationException;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.findAllPublicInstanceFieldsAndNonVoidNonPrimitiveInstanceMethods;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.google.common.base.Optional;

/**
 * A graph builder creates the call chain graph from a list of given entry points. It uses an internal
 * {@link ThreadPoolExecutor} to build the search graph in parallel.
 * 
 * @see MemberEdge
 * @see TypeNode
 */
public class GraphBuilder {

    /**
     * @see VisitEdgeJob#call() decrements the counter when done.
     * @see #scheduleNextIteration(List) increments the counter for a bunch of jobs. NOTE, if the counter is increased
     *      of for a single job one-by-one and not as a bulk operation, it might happen that the pool is shutdown before
     *      even the initial jobs were scheduled!
     */
    private final AtomicInteger numberOfScheduledJobs = new AtomicInteger();

    private final class VisitEdgeJob implements Callable<Void> {
        private final MemberEdge newEdge;

        private VisitEdgeJob(final MemberEdge newEdge) {
            this.newEdge = newEdge;
        }

        @Override
        public Void call() throws Exception {
            visitEdge(newEdge);
            final int count = numberOfScheduledJobs.decrementAndGet();
            if (count == 0) {
                pool.shutdown();
            }
            return null;
        }

        @Override
        public String toString() {
            return newEdge.toString();
        }
    }

    // private final ScheduledExecutorService timeout = Executors.newScheduledThreadPool(1);
    private final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime()
            .availableProcessors());

    private final int maxdepth = 5;

    private final Map<IType, TypeNode> nodes = Collections.synchronizedMap(new HashMap<IType, TypeNode>());
    private final List<List<MemberEdge>> chains = new CopyOnWriteArrayList<List<MemberEdge>>();

    public void build(final List<MemberEdge> entrypoints) {
        scheduleEntrypoints(entrypoints);
        awaitPoolTermination();
    }

    private void scheduleEntrypoints(final List<MemberEdge> entrypoints) {
        final LinkedList<VisitEdgeJob> iteration = new LinkedList<VisitEdgeJob>();
        for (final MemberEdge entrypoint : entrypoints) {
            iteration.add(new VisitEdgeJob(entrypoint));
        }
        scheduleNextIteration(iteration);

    }

    private void scheduleNextIteration(final List<VisitEdgeJob> iteration) {

        // ATTENTION: increment all at once! Otherwise, the pool may be shutdown before the last job has been executed!
        numberOfScheduledJobs.addAndGet(iteration.size());
        for (final VisitEdgeJob job : iteration) {
            pool.submit(job);
        }
    }

    private void awaitPoolTermination() {
        try {
            pool.awaitTermination(4, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            // no logging intended
        }
    }

    public List<List<MemberEdge>> findChains(final IType expectedType) {
        for (final TypeNode node : nodes.values()) {
            if (node.isAssignable(expectedType)) {
                for (final MemberEdge edge : node.incomingEdges) {
                    final LinkedHashSet<MemberEdge> anchor = new LinkedHashSet<MemberEdge>();
                    dsfTraverse(anchor, edge);
                }
            }
        }
        return chains;

    }

    private void dsfTraverse(final LinkedHashSet<MemberEdge> incompleteChain, final MemberEdge edgeToTest) {
        terminateIfInterrupted();

        if (introducesCycleIntoCallChain(incompleteChain, edgeToTest)) {
            return;
        }

        final LinkedHashSet<MemberEdge> workingCopy = createWorkingCopyWithNewEdge(incompleteChain, edgeToTest);

        if (reachedCallChainMaxLengthLimit(workingCopy)) {
            return;
        }

        if (edgeToTest.isChainAnchor()) {
            registerCopyOfSuccessfullyCompletedChain(workingCopy);
            return;
        }

        final IType accessedFrom = edgeToTest.getReceiverType().get();
        final TypeNode typeNode = nodes.get(accessedFrom);

        for (final MemberEdge nextEdgeToTest : typeNode.incomingEdges) {
            dsfTraverse(workingCopy, nextEdgeToTest);
        }
    }

    private boolean introducesCycleIntoCallChain(final LinkedHashSet<MemberEdge> incompleteChain,
            final MemberEdge edgeToTest) {
        return incompleteChain.contains(edgeToTest);
    }

    private void terminateIfInterrupted() {
        if (Thread.interrupted()) {
            throwCancelationException();
        }
    }

    private LinkedHashSet<MemberEdge> createWorkingCopyWithNewEdge(final LinkedHashSet<MemberEdge> incompleteChain,
            final MemberEdge edgeToTest) {
        final LinkedHashSet<MemberEdge> workingCopy = cast(incompleteChain.clone());
        workingCopy.add(edgeToTest);
        return workingCopy;
    }

    private boolean reachedCallChainMaxLengthLimit(final LinkedHashSet<MemberEdge> chain) {
        return chain.size() > maxdepth;
    }

    private void registerCopyOfSuccessfullyCompletedChain(final LinkedHashSet<MemberEdge> almostCompleteCallChain) {
        final List<MemberEdge> copy = new LinkedList<MemberEdge>(almostCompleteCallChain);
        Collections.reverse(copy);
        chains.add(copy);
    }

    private void visitEdge(final MemberEdge edge) throws JavaModelException {
        terminateIfInterrupted();
        final Optional<IType> opt = edge.getReturnType();
        if (opt.isPresent()) {
            inspectReturnTypeAndRegisterNewNodesAndEdges(edge, opt.get());
        }
    }

    private synchronized void inspectReturnTypeAndRegisterNewNodesAndEdges(final MemberEdge edge, final IType returnType)
            throws JavaModelException {
        if (nodes.containsKey(returnType)) {
            registerIncomingEdge(edge, returnType);
        } else {
            registerNewNode(returnType);
            registerIncomingEdge(edge, returnType);
            addNewEdgesIntoWorklist(edge, returnType);
        }
    }

    private void registerIncomingEdge(final MemberEdge edge, final IType returnType) {
        final TypeNode node = nodes.get(returnType);
        node.incomingEdges.add(edge);
    }

    private void registerNewNode(final IType returnType) {
        final TypeNode newNode = new TypeNode(returnType);
        nodes.put(returnType, newNode);
    }

    private void addNewEdgesIntoWorklist(final MemberEdge edge, final IType returnType) throws JavaModelException {
        if (edge.getIterationDepth() >= maxdepth) {
            return;
        }

        final int nextEdgeDepth = edge.getIterationDepth() + 1;
        final List<VisitEdgeJob> nextIteration = new LinkedList<VisitEdgeJob>();
        final Collection<IMember> allMethodsAndFields = findAllPublicInstanceFieldsAndNonVoidNonPrimitiveInstanceMethods(returnType);
        for (final IJavaElement element : allMethodsAndFields) {
            MemberEdge newEdge = null;
            switch (element.getElementType()) {
            case IJavaElement.METHOD:
                final IMethod m = (IMethod) element;
                newEdge = new MemberEdge(returnType, m, nextEdgeDepth);
                nextIteration.add(new VisitEdgeJob(newEdge));
                break;
            case IJavaElement.FIELD:
                final IField f = (IField) element;
                newEdge = new MemberEdge(returnType, f, nextEdgeDepth);
                nextIteration.add(new VisitEdgeJob(newEdge));
                break;
            default:
                break;
            }
        }
        scheduleNextIteration(nextIteration);
    }
}

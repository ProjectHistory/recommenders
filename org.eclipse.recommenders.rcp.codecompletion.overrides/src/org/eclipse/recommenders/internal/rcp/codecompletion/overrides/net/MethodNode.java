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
package org.eclipse.recommenders.internal.rcp.codecompletion.overrides.net;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

import smile.DocItemInfo;
import smile.Network;

public class MethodNode extends AbstractNode {
    private static int TRUE = 0;

    private static int FALSE = 1;

    protected MethodNode(final Network network, final String nodeId) {
        super(network, nodeId);
    }

    public void setEvidence(final boolean value) {
        final int outcomeIndex = value ? TRUE : FALSE;
        network.setEvidence(nodeId, outcomeIndex);
    }

    @Override
    public void clearEvidence() {
        network.clearEvidence(nodeId);
    }

    public IMethodName getMethod() {
        final DocItemInfo[] nodeDocumentation = network.getNodeDocumentation(getNodeId());
        final String methodIdentifier = nodeDocumentation[0].path;
        return VmMethodName.get(methodIdentifier);
    }

    public double getProbability() {
        try {
            final double[] values = network.getNodeValue(nodeId);
            return values[TRUE];
        } catch (final Exception x) {
            RecommendersPlugin.logError(x, "failed to compute method call probability for '%s'", nodeId);
            return 0.0d;
        }
    }
}

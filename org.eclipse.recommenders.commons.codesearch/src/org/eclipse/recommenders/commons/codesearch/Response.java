/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.commons.codesearch;

import java.util.List;

public class Response {

    public static Response newResponse(final String id, final List<Proposal> proposals) {
        final Response res = new Response();
        res.requestId = id;
        res.proposals = proposals;
        return res;
    }

    public static Response newResponse() {
        final Response res = new Response();
        return res;
    }

    public static Response newErrorResponse(final Exception e) {
        final Response res = new Response();
        res.error = e.toString();
        return res;
    }

    /**
     * Each request has a unique request id under which all feedback is stored.
     * This request id is generated by the server.
     */
    public String requestId;

    public List<Proposal> proposals;

    public String error;

    private Response() {
        // see #newResponse()
    }

}
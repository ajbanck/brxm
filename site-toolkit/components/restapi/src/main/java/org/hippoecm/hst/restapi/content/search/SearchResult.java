/*
 * Copyright 2016-2017 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.hst.restapi.content.search;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.restapi.NodeVisitor;
import org.hippoecm.hst.restapi.ResourceContext;
import org.hippoecm.hst.restapi.content.linking.RestApiLinkCreator;

import com.fasterxml.jackson.annotation.JsonProperty;

import static org.hippoecm.repository.api.HippoNodeType.NT_HANDLE;

public class SearchResult {

    @JsonProperty("offset")
    public long offset;

    @JsonProperty("max")
    public long max;

    @JsonProperty("count")
    public long count;

    @JsonProperty("total")
    public long total;

    @JsonProperty("more")
    public boolean more;

    @JsonProperty("items")
    public List<Map<String, Object>> items;

    public void populateFromDocument(final int offset, final int max,
                                     final HstQueryResult queryResult,
                                     final ResourceContext context) throws RepositoryException {
        final List<Map<String, Object>> itemArrayList = new ArrayList<>();
        final HippoBeanIterator iterator = queryResult.getHippoBeans();
        final RestApiLinkCreator restApiLinkCreator = context.getRestApiLinkCreator();
        while (iterator.hasNext()) {
            final HippoBean bean = iterator.nextHippoBean();
            final Node node = bean.getNode();
            final Node handleNode = node.getParent();
            if (!handleNode.isNodeType(NT_HANDLE)) {
                throw new IllegalStateException(String.format("Expected node of type 'NT_HANDLE' but was '%s'.",
                        handleNode.getPrimaryNodeType().getName()));
            }

            final Map<String, Object> response = new LinkedHashMap<>();
            response.put("name", handleNode.getName());
            response.put("id", handleNode.getIdentifier());
            final HstLink hstLink = context.getRequestContext().getHstLinkCreator().create(handleNode, context.getRequestContext());
            response.put("link", restApiLinkCreator.convert(context, handleNode.getIdentifier(), hstLink));
            final NodeVisitor visitor = context.getVisitor(node);
            visitor.visit(context, node, response);
            itemArrayList.add(response);
        }
        this.offset = offset;
        this.max = max;
        count = itemArrayList.size();
        total = queryResult.getTotalSize();
        more = (offset + count) < total;
        items = itemArrayList;
    }
}
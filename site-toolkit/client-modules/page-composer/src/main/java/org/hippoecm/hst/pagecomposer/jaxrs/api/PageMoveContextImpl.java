/*
 * Copyright 2019 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.hst.pagecomposer.jaxrs.api;

import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.core.request.HstRequestContext;

import javax.jcr.Node;

public class PageMoveContextImpl extends AbstractPageContext implements PageMoveContext {

    private transient final Node originalParentSiteMapNode;
    private transient final Node newParentSiteMapNode;
    private transient final Node newSiteMapItemNode;

    public PageMoveContextImpl(final HstRequestContext requestContext,
                               final Mount editingMount,
                               final Node originalParentSiteMapNode,
                               final Node newParentSiteMapNode,
                               final Node newSiteMapItemNode) {
        super(requestContext, editingMount);
        this.originalParentSiteMapNode = originalParentSiteMapNode;
        this.newParentSiteMapNode = newParentSiteMapNode;
        this.newSiteMapItemNode = newSiteMapItemNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getOriginalParentSiteMapNode() {
        return originalParentSiteMapNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNewParentSiteMapNode() {
        return newParentSiteMapNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNewSiteMapItemNode() {
        return newSiteMapItemNode;
    }


    @Override
    public String toString() {
        return "PageMoveContext{" +
                "requestContext=" + getRequestContext() +
                ", editingMount=" + getEditingMount() +
                ", sourceSiteMapNode=" + originalParentSiteMapNode +
                ", newParentSiteMapNode=" + newParentSiteMapNode +
                ", newSiteMapItemNode=" + newSiteMapItemNode +
                '}';
    }
}

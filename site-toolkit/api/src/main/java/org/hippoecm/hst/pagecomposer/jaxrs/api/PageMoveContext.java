/*
 *  Copyright 2019 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.hst.pagecomposer.jaxrs.api;

import javax.jcr.Node;

import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.core.request.HstRequestContext;

/**
 * Page moving context data used in {@link PageMoveEvent}.
 */
public interface PageMoveContext extends PageActionContext {

    /**
     * @return the {@link Node} belonging to the parent sitemap item of the page that is to be moved. This {@link HstSiteMapItem} instance always
     * belongs to the {@link #getEditingMount()}. This method never returns {@code null}.
     */
    public Node getOriginalParentSiteMapNode();

    /**
     * <p>
     * returns the new parent {@link Node} to which the page move action is targeted, or {@code null} in case
     * the target is a {@code root siteMapItem}
     * </p>
     * <p>
     * If it returns a non {@code null} value, the returned {@link Node} belongs to the {@link #getEditingMount()}.
     * </p>
     *
     * @return the target {@link HstSiteMapItem} to which the page move action is targeted, or {@code null}
     */
    public Node getNewParentSiteMapNode();

    /**
     * @return the in {@link HstRequestContext#getSession() session} created (but not yet persisted) site map item JCR {@link Node}
     * as a result of this move page action
     */
    public Node getNewSiteMapItemNode();

}

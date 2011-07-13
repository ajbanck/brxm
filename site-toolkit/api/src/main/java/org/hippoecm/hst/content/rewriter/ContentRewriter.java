/*
 *  Copyright 2008 Hippo.
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
package org.hippoecm.hst.content.rewriter;

import javax.jcr.Node;

import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;

/**
 * ContentRewriter to rewrite document content such as links.
 * 
 * @version $Id$
 */
public interface ContentRewriter<T> {
    
    /**
     * Rewrites the content of the content node.
     * @param content content object. It can be type of String or whatever, depending on the implementation and the context.
     * @param contentNode content node
     * @param requestContext
     * @return
     */
    T rewrite(T content, Node contentNode, HstRequestContext requestContext);
    
    /**
     * Rewrites the content of the content node.
     * @param content
     * @param contentNode
     * @param requestContext
     * @param targetMountAlias
     * @return
     */
    T rewrite(T content, Node contentNode, HstRequestContext requestContext, String targetMountAlias);
    
    /**
     * Rewrites the content of the content node.
     * @param content
     * @param contentNode
     * @param requestContext
     * @param targetMount
     * @return
     */
    T rewrite(T content, Node contentNode, HstRequestContext requestContext, Mount targetMount);
    
}

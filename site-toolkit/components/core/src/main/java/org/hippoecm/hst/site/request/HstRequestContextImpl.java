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
package org.hippoecm.hst.site.request;

import javax.jcr.Repository;

import org.hippoecm.hst.core.context.ContextBase;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.core.template.node.PageNode;

public class HstRequestContextImpl implements HstRequestContext {

    protected Repository repository;
    protected ContextBase contentContextBase;
    protected ContextBase hstConfigurationContextBase;
    protected PageNode pageNode;
    // TODO: remove this.
    protected String hstRequestUri;

    public HstRequestContextImpl() {
    }
    
    public PageNode getPageNode() {
        return pageNode;   
    }
    
    public ContextBase getContentContextBase() {
        return contentContextBase;
    }

    public ContextBase getHstConfigurationContextBase() {
        return hstConfigurationContextBase;
    }

    // TODO: remove this.
    public String getHstRequestUri() {
        return hstRequestUri;
    }
    
    public void setPageNode(PageNode pageNode) {
        this.pageNode = pageNode;
    }
    
    public void setContentContextBase(ContextBase contentContextBase) {
        this.contentContextBase = contentContextBase;
    }

    public void setHstConfigurationContextBase(ContextBase hstConfigurationContextBase) {
        this.hstConfigurationContextBase = hstConfigurationContextBase;
    }

    public void setHstRequestUri(String hstRequestUri) {
        this.hstRequestUri = hstRequestUri;
    }

    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }
    
    public Repository getRepository()
    {
        return this.repository;
    }

    // TODO: remove this.
    public String getServerName()
    {
        return null;
    }

    public String getUserID()
    {
        // TODO Auto-generated method stub
        return null;
    }

    // TODO: remove this.
    public String getRequestURI()
    {
        return null;
    }


}

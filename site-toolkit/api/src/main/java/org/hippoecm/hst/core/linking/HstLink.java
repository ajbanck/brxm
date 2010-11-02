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
package org.hippoecm.hst.core.linking;

import org.hippoecm.hst.configuration.hosting.SiteMount;
import org.hippoecm.hst.configuration.site.HstSite;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;

/**
 * HstLink is the object representing a link. The {@link #getPath()} return you the value of the link, and {@link #getPathElements()}
 * returns you the path splitted on "/"'s. The String[] version is more practical because the {@link javax.servlet.http.HttpServletResponse#encodeURL(String)}
 * also encodes slashes. 
 * 
 * Furthermore, the {@link HstSite} that the link is meant for is accessible through this HstLink, because it is needed if the link is
 * out of the scope of the current HstSite. The HstSite can access the {@link org.hippoecm.hst.configuration.hosting.VirtualHost} through which
 * in turn even links to different hosts can be created. 
 *
 */
public interface HstLink {

    final static String PATH_SUBPATH_DELIMITER = "./";
    
    /**
     * Note: This is *not* a url!
     * @return the path of this HstLink. Note: This is *not* a url!
     */
    String getPath();
    
    /**
     * (re)-sets the path of the HstLink
     * @param path
     */
    void setPath(String path);
    
    /**
     * Returns the subPath of this {@link HstLink} object. This part will be appended to the {@link #getPath()} and delimited by <code>./</code>. It will be before the queryString.
     * Note that an empty {@link String} <code>subPath</code> will result in a URL having a <code>./</code> appended: An empty
     * <code>subPath</code> is thus something different then a <code>null</code> <code>subPath</code>.
     * @return the subPath of this {@link HstLink} object. 
     */
    String getSubPath();
    
    /**
     * sets the <code>subPath</code> of this {@link HstLink}. Note that setting the <code>subPath</code> to an empty {@link String} will result in a URL having a <code>./</code> appended: An empty
     * <code>subPath</code> is thus something different then a <code>null</code> <code>subPath</code>.
     * @param subPath
     */
    void setSubPath(String subPath);
    
    /**
     * @return <code>true</code> when the HstLink represents a container resource, like a repository binary
     */
    boolean getContainerResource();
    
    /**
     * @param sets the containerResource
     */
    void setContainerResource(boolean containerResource);
    
    /**
     * @param request
     * @param external if true, the returned url is external, in other words including http/https etc
     * @return the url form of this HstLink, which is a url
     * @deprecated use {@link #toUrlForm(HstRequestContext, boolean)} instead
     */
    String toUrlForm(HstRequest request, HstResponse response, boolean external);
    
    /**
     * @param requestContext
     * @param external if true, the returned url is external, in other words including http/https etc
     * @return the url form of this HstLink, which is a url
     */
    String toUrlForm(HstRequestContext requestContext, boolean external);
    
    /**
     * @return the path elements of this HstLink, which is the {@link #getPath()} splitted on slashes
     */
    String[] getPathElements();
    
    
    /**
     * @return the HstSite that can represent this link. This might be an HstSite which is a different one then the
     * HstSite the link was created in. This could result in a cross-domain (different hostname) link being created, depending
     * on the backing {@link org.hippoecm.hst.configuration.hosting.VirtualHost}. If no HstSite is set, <code>null</code> can be returned
     * @deprecated use {@link #getSiteMount} instead
     */
    @Deprecated
    HstSite getHstSite();
    
    /**
     * @return the {@link SiteMount} that can represent this link. This might be an  {@link SiteMount} which is a different one then the
     *  {@link SiteMount} the link was created in. This could result in a cross-domain (different hostname) link being created, depending
     * on the backing {@link SiteMount#getVirtualHost()}. If no SiteMount is set, <code>null</code> can be returned
     * 
     */
    SiteMount getSiteMount();
    
    /**
     * When for example for some bean the (real) link cannot be created through the HstLinkCreator, a HstLink can be returned
     * with a path that is for example from some configured property like '/pagenotfound'. If this method returns <code>true</code> 
     * it indicates that the link is some hardcoded path for beans that cannot be linked to
     * @return <code>true</code> when this HstLink indicates to be a link that is actually a notFound link
     */
    boolean isNotFound();
    
    /**
     * @param notFound true whether this HstLink is actually a notFound link
     */
    void setNotFound(boolean notFound);
}

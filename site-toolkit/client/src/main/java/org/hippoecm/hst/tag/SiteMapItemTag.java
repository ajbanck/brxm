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
package org.hippoecm.hst.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.request.ResolvedSiteMapItem;
import org.hippoecm.hst.core.request.ResolvedSiteMount;
import org.hippoecm.hst.util.HstRequestUtils;
import org.hippoecm.hst.utils.PageContextPropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A tag handler for the <CODE>siteItemTag</CODE> tag. Defines a sitemap item that
 * can be added to a <CODE>HstLinkTag</CODE>. If a sitemap item is added to a HstLinkTag, it means that 
 * for linkrewriting, this sitemap item + descendants have precedence for rewriting the link. Depending on whether 
 * <code>fallback</code> is true or false, a fallback to original linkrewriting is done when the preferred sitemap item
 * cannot create a link.  
 * <BR>The following attributes are allowed:
 * 
 * To define the preferred sitemap item, one of the following attributes needs to be defined 
 *   <OL>
 *       <LI><CODE>preferItem : the preferred sitemap item ({@link HstSiteMapItem})</CODE></LI>
 *       <LI><CODE>preferItemId : the id of the preferred sitemap item (java.lang.String)</CODE></LI>
 *       <LI><CODE>preferPath : the pathInfo (url) of the preferred sitemap item (java.lang.String)</CODE></LI>
 *       <LI><CODE>preferItemByPath : the preferred sitemap item by path for freemarker (java.lang.String)</CODE></LI>
 *   </OL>
 * 
 * When fallback must be false, you can use attribute:
 * <UL>
 *    <LI><CODE>fallback : true|false (java.lang.String)</CODE></LI>
 * </UL>
 * Default fallback = true
 */
public class SiteMapItemTag extends TagSupport {

    private final static Logger log = LoggerFactory.getLogger(SiteMapItemTag.class);
    private static final long serialVersionUID = 1L;

    protected HstSiteMapItem siteMapItem = null;
    protected boolean fallback = true;
    protected String preferItemId;
    protected String preferPath;
    protected boolean skipTag; 

    /* (non-Javadoc)
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {
        if(skipTag){
            return SKIP_BODY;
        }
        
        HttpServletRequest servletRequest = (HttpServletRequest) pageContext.getRequest();
        HstRequest hstRequest = HstRequestUtils.getHstRequest(servletRequest);
        
        if(hstRequest == null) {
            log.warn("The request is not an HstRequest. Cannot create an preferred sitemap item outside the hst request processing. Return");
            return SKIP_BODY;
        }
        
        ResolvedSiteMount resolvedSiteMount = hstRequest.getRequestContext().getResolvedSiteMapItem().getResolvedSiteMount();
        if(preferItemId != null) {
            if(siteMapItem != null) {
                log.warn("preferItemId attr is added, but also 'preferItemByPath' or 'siteMapItem'. This is double. Skipping preferItemId attr");
            } else {
                siteMapItem =  hstRequest.getRequestContext().getResolvedSiteMapItem().getHstSiteMapItem().getHstSiteMap().getSiteMapItemById(preferItemId);
                if(siteMapItem == null) {
                    log.warn("Cannot find sitemap item with id '{}' for site '{}'", preferItemId, resolvedSiteMount.getSiteMount().getName());
                }
            }
        }
        
        if(preferPath != null) {
            if(siteMapItem != null) {
                log.warn("preferPath attr is added, but also 'preferItemByPath', 'siteMapItem' or 'preferItemId'. This is double. Skipping preferItemId attr");
            } else {
                ResolvedSiteMapItem resolvedItem = hstRequest.getRequestContext().getSiteMapMatcher().match(preferPath, resolvedSiteMount);
                if(resolvedItem == null) {
                    log.warn("Cannot resolve a sitemap item for '{}' for site '{}'", preferPath, resolvedSiteMount.getSiteMount().getName());
                } else {
                    siteMapItem = resolvedItem.getHstSiteMapItem();  
                }
            }
        }
        
        HstLinkTag hstLinkTag = (HstLinkTag)
                findAncestorWithClass(this, HstLinkTag.class);

        if (hstLinkTag == null) {
            throw new JspException("the 'SiteMapItemTag' Tag must have a HST's 'link' tag as a parent");
        }

        hstLinkTag.setPreferredSiteMapItem(siteMapItem);
        hstLinkTag.setFallback(fallback);

        siteMapItem = null;
        fallback = true;
        preferItemId = null;
        preferPath = null;
        skipTag = false;
        
        return SKIP_BODY;
    }

    /**
     * Returns the name.
     * @return String
     */
    public HstSiteMapItem getPreferItem() {
        return siteMapItem;
    }


    /**
     * Sets the siteMapItem.
     * @param the siteMapItem to set
     */
    public void setPreferItem(HstSiteMapItem siteMapItem) {
        this.siteMapItem = siteMapItem;
    }
    
    public void setPreferItemByPath(String preferItemByPath) {
        this.siteMapItem = (HstSiteMapItem)PageContextPropertyUtils.getProperty(pageContext, preferItemByPath);
        if(this.siteMapItem == null) {
            log.debug("No hstSiteMapItem for '{}'. The tag will be skipped.", preferItemByPath);
            skipTag = true;
        }
    }
    
    public void setPreferItemId(String preferItemId) {
        this.preferItemId = preferItemId;
    }
    
    public void setPreferPath(String preferPath){
        this.preferPath = preferPath;
    }
    
    
    /**
     * Sets the fallback
     * @param the fallback to set
     */
    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

}

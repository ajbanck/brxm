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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;
import javax.servlet.ServletContext;

import org.hippoecm.hst.configuration.hosting.SiteMount;
import org.hippoecm.hst.configuration.hosting.VirtualHost;
import org.hippoecm.hst.configuration.hosting.VirtualHosts;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstURLFactory;
import org.hippoecm.hst.core.container.ContainerConfiguration;
import org.hippoecm.hst.core.container.HstContainerURL;
import org.hippoecm.hst.core.container.HstContainerURLProvider;
import org.hippoecm.hst.core.internal.HstMutableRequestContext;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.ContextCredentialsProvider;
import org.hippoecm.hst.core.request.HstSiteMapMatcher;
import org.hippoecm.hst.core.request.ResolvedSiteMapItem;
import org.hippoecm.hst.core.request.ResolvedSiteMount;
import org.hippoecm.hst.core.search.HstQueryManagerFactory;
import org.hippoecm.hst.core.sitemenu.HstSiteMenus;

/**
 * HstRequestContextImpl
 * 
 * @version $Id$
 */
public class HstRequestContextImpl implements HstMutableRequestContext {

	protected ServletContext servletContext;
    protected Repository repository;
    protected ContextCredentialsProvider contextCredentialsProvider;
    protected Session session;
    protected ResolvedSiteMount resolvedSiteMount;
    protected ResolvedSiteMapItem resolvedSiteMapItem;
    protected String targetComponentPath;
    protected HstURLFactory urlFactory;
    protected HstContainerURL baseURL;
    protected String contextNamespace = "";
    protected HstLinkCreator linkCreator;
    protected HstSiteMapMatcher siteMapMatcher;
    protected HstSiteMenus siteMenus;
    protected HstQueryManagerFactory hstQueryManagerFactory;
    protected Map<String, Object> attributes;
    protected ContainerConfiguration containerConfiguration;
    protected String embeddingContextPath;
    protected ResolvedSiteMount resolvedEmbeddingSiteMount;  
    protected Subject subject;
    protected Locale preferredLocale;
    protected List<Locale> locales;
    protected String pathSuffix;
    
    private Map<String, Object> unmodifiableAttributes;
    
    public HstRequestContextImpl(Repository repository) {
        this(repository, null);
    }
    
    public HstRequestContextImpl(Repository repository, ContextCredentialsProvider contextCredentialsProvider) {
        this.repository = repository;
        this.contextCredentialsProvider = contextCredentialsProvider;
    }
    
    public boolean isPreview() {
    	return this.resolvedSiteMount.getSiteMount().isPreview();
    }    
    
    public ServletContext getServletContext() {
    	return servletContext;
    }
    
    public void setServletContext(ServletContext servletContext) {
    	this.servletContext = servletContext;
    }
    
    public void setContextNamespace(String contextNamespace) {
        this.contextNamespace = contextNamespace;
    }
    
    public String getContextNamespace() {
        return this.contextNamespace;
    }
    
    public Session getSession() throws LoginException, RepositoryException {
        if (this.session == null) {
            if (contextCredentialsProvider != null) {
                this.session = this.repository.login(contextCredentialsProvider.getDefaultCredentials(this));
            } else {
                this.session = this.repository.login();
            }
        } else if (!this.session.isLive()) {
            throw new HstComponentException("Invalid session.");
        }
        
        return this.session;
    }
    
    public void setSession(Session session) {
        this.session = session;
    }
 
    public void setResolvedSiteMount(ResolvedSiteMount resolvedSiteMount) {
        this.resolvedSiteMount = resolvedSiteMount;
    }

    public ResolvedSiteMount getResolvedSiteMount() {
        return this.resolvedSiteMount;
    }
    
    public void setResolvedSiteMapItem(ResolvedSiteMapItem resolvedSiteMapItem) {
        this.resolvedSiteMapItem = resolvedSiteMapItem;
    }

    public ResolvedSiteMapItem getResolvedSiteMapItem() {
        return this.resolvedSiteMapItem;
    }
    
    public void setTargetComponentPath(String targetComponentPath) {
    	this.targetComponentPath = targetComponentPath;
    }
    
    public String getTargetComponentPath() {
    	return this.targetComponentPath;
    }
    
    public void setBaseURL(HstContainerURL baseURL) {
        this.baseURL = baseURL;
    }
    
    public HstContainerURL getBaseURL() {
        return this.baseURL;
    }
    
    public void setURLFactory(HstURLFactory urlFactory) {
        this.urlFactory = urlFactory;
    }
    
    public HstURLFactory getURLFactory() {
        return this.urlFactory;
    }
    
    public HstContainerURLProvider getContainerURLProvider() {
        return urlFactory != null ? urlFactory.getContainerURLProvider() : null;
    }

    public void setSiteMapMatcher(HstSiteMapMatcher siteMapMatcher) {
        this.siteMapMatcher = siteMapMatcher;
    }
    
    public HstSiteMapMatcher getSiteMapMatcher(){
        return this.siteMapMatcher;
    }
    
    public void setLinkCreator(HstLinkCreator linkCreator) {
        this.linkCreator = linkCreator;
    }
    
    public HstLinkCreator getHstLinkCreator() {
        return this.linkCreator;
    }
    
    public void setHstSiteMenus(HstSiteMenus siteMenus) {
        this.siteMenus = siteMenus;
    }
    
    public HstSiteMenus getHstSiteMenus(){
        return this.siteMenus;
    }
    
    public HstQueryManagerFactory getHstQueryManagerFactory() {
        return hstQueryManagerFactory;
    }

    public void setHstQueryManagerFactory(HstQueryManagerFactory hstQueryManagerFactory) {
        this.hstQueryManagerFactory = hstQueryManagerFactory;
    }
   
    public Object getAttribute(String name) {
        if (name == null) {
            throw new IllegalArgumentException("attribute name cannot be null.");
        }
        
        Object value = null;
        
        if (this.attributes != null) {
            value = this.attributes.get(name);
        }
        
        return value;
    }

    public Enumeration<String> getAttributeNames() {
        
        if (this.attributes != null) {
            return Collections.enumeration(attributes.keySet());
        } else {
            List<String> emptyAttrNames = Collections.emptyList();
            return Collections.enumeration(emptyAttrNames);
        }
    }

    public void removeAttribute(String name) {
        if (name == null) {
            throw new IllegalArgumentException("attribute name cannot be null.");
        }
        
        if (this.attributes != null) {
            this.attributes.remove(name);
        }
    }

    public void setAttribute(String name, Object object) {
        if (name == null) {
            throw new IllegalArgumentException("attribute name cannot be null.");
        }
        
        if (object == null) {
            removeAttribute(name);
        }
        
        if (this.attributes == null) {
            synchronized (this) {
                if (this.attributes == null) {
                    this.attributes = Collections.synchronizedMap(new HashMap<String, Object>());
                }
            }
        }
        
        this.attributes.put(name, object);
    }
    
    public Map<String, Object> getAttributes() {
        if (unmodifiableAttributes == null && attributes != null) {
            unmodifiableAttributes = Collections.unmodifiableMap(attributes);
        }
        
        if (unmodifiableAttributes == null) {
            return Collections.emptyMap();
        }
        
        return unmodifiableAttributes;
    }
    
    public ContainerConfiguration getContainerConfiguration() {
        return this.containerConfiguration;
    }
    
    public void setContainerConfiguration(ContainerConfiguration containerConfiguration) {
        this.containerConfiguration = containerConfiguration;
    }

    public VirtualHost getVirtualHost() {
       return resolvedSiteMount.getSiteMount().getVirtualHost();
    }
    
    public boolean isEmbeddedRequest() {
        return resolvedEmbeddingSiteMount != null;
    }
    
    public void setEmbeddingContextPath(String embeddingContextPath) {
    	this.embeddingContextPath = embeddingContextPath;
    }
    
    public String getEmbeddingContextPath() {
    	return this.embeddingContextPath;
    }
    
    public void setResolvedEmbeddingSiteMount(ResolvedSiteMount resolvedEmbeddingSiteMount) {
    	this.resolvedEmbeddingSiteMount = resolvedEmbeddingSiteMount;
    }
    
    public ResolvedSiteMount getResolvedEmbeddingSiteMount() {
    	return this.resolvedEmbeddingSiteMount;
    }

    public boolean isPortletContext() {
        return false;
    }
    
    public ContextCredentialsProvider getContextCredentialsProvider() {
        return contextCredentialsProvider;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Subject getSubject() {
        return subject;
    }
    
    public void setPreferredLocale(Locale preferredLocale) {
        this.preferredLocale = preferredLocale;
    }
    
    public Locale getPreferredLocale() {
        return preferredLocale;
    }
    
    public void setLocales(List<Locale> locales) {
        this.locales = locales;
    }
    
    public Enumeration<Locale> getLocales() {
        if (locales != null) {
            return Collections.enumeration(locales);
        }
        
        return null;
    }
    
    public void setPathSuffix(String pathSuffix) {
        this.pathSuffix = pathSuffix;
    }
    
    public String getPathSuffix() {
        return pathSuffix;
    }
    
    public SiteMount getMount(String alias) {
        SiteMount curMount = getResolvedSiteMount().getSiteMount();
        VirtualHost curVhost = curMount.getVirtualHost();
        String hostGroupName = curVhost.getHostGroupName();
        
        VirtualHost vhost = getVirtualHost();
        VirtualHosts vhosts = vhost.getVirtualHosts();
        
        for (String type : curMount.getTypes()) {
            SiteMount targetMount = vhosts.getSiteMountByGroupAliasAndType(hostGroupName, alias, type);
            
            if (targetMount != null) {
                return targetMount;
            }
        }
        
        return null;
    }
    
    public SiteMount getMount(String type, String alias) {
        SiteMount curMount = getResolvedSiteMount().getSiteMount();
        VirtualHost curVhost = curMount.getVirtualHost();
        String hostGroupName = curVhost.getHostGroupName();
        
        VirtualHost vhost = getVirtualHost();
        VirtualHosts vhosts = vhost.getVirtualHosts();
        
        return vhosts.getSiteMountByGroupAliasAndType(hostGroupName, alias, type);
    }
}

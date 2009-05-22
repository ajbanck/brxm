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
package org.hippoecm.hst.container;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.hippoecm.hst.core.hosting.VirtualHosts;
import org.hippoecm.hst.core.hosting.VirtualHostsManager;
import org.hippoecm.hst.core.request.MatchedMapping;
import org.hippoecm.hst.logging.Logger;
import org.hippoecm.hst.site.HstServices;

public class HstVirtualHostsFilter implements Filter {

    private static final long serialVersionUID = 1L;
    
    private static final String LOGGER_CATEGORY_NAME = HstVirtualHostsFilter.class.getName();

    private final static String FILTER_DONE_KEY = "filter.done_"+HstVirtualHostsFilter.class.getName();
    private final static String REQUEST_START_TICK_KEY = "request.start_"+HstVirtualHostsFilter.class.getName();
    private FilterConfig filterConfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        
        HttpServletRequest req = (HttpServletRequest)request;
        
        Logger logger = HstServices.getLogger(LOGGER_CATEGORY_NAME);
        
        try {
            if (!HstServices.isAvailable()) {
                String msg = "The HST Container Services are not initialized yet.";
                response.getWriter().println(msg);
                response.flushBuffer();
                return;
            }
            
            if (logger.isDebugEnabled()) {request.setAttribute(REQUEST_START_TICK_KEY, System.nanoTime());}
            
            String pathInfo = req.getRequestURI().substring(req.getContextPath().length());
            
            VirtualHostsManager virtualHostManager = HstServices.getComponentManager().getComponent(VirtualHostsManager.class.getName());
            VirtualHosts vHosts = virtualHostManager.getVirtualHosts();
            if(vHosts.isExcluded(pathInfo)) {
                chain.doFilter(request, response);
                return;
            }
            
            if (request.getAttribute(FILTER_DONE_KEY) == null) {
                request.setAttribute(FILTER_DONE_KEY, Boolean.TRUE);
                
                
                
                MatchedMapping matchedMapping = vHosts.findMapping(req.getServerName(), pathInfo);
                
                 if(matchedMapping != null) {
                    /*
                     * put the matched Mapping temporarily on the request, as we do not yet have a HstRequestContext. When the
                     * HstRequestContext is created, and there is a Mapping on the request, we put it on the HstRequestContext.
                     */  
                    request.setAttribute(MatchedMapping.class.getName(), matchedMapping);
                    String mappedPath = matchedMapping.mapToInternalURI(pathInfo);
                    filterConfig.getServletContext().getRequestDispatcher(mappedPath).forward(request, response);
                   
                } else {
                    chain.doFilter(request, response);
                }
                
            } else {
                chain.doFilter(request, response);
            }
        } finally {
            if (logger != null && logger.isDebugEnabled()) {
                long starttick = request.getAttribute(REQUEST_START_TICK_KEY) == null ? 0 : (Long)request.getAttribute(REQUEST_START_TICK_KEY);
                if(starttick != 0) {
                    logger.debug( "Handling request took --({})-- ms for '{}'.", (System.nanoTime() - starttick)/1000000, req.getRequestURI());
                }
            }
        }
    }

    public void destroy() {

    }

}

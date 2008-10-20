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
package org.hippoecm.hst.core.mapping;

import java.util.List;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;

import org.hippoecm.hst.core.template.ContextBase;
import org.hippoecm.hst.core.template.node.PageNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelativeURLMappingImpl implements URLMapping{
    
    private static final Logger log = LoggerFactory.getLogger(URLMapping.class);
    
    private String currentRequestUri;
    private URLMapping delegatee;
    private boolean currentUriEndsWithSlash = false;
    
    public RelativeURLMappingImpl(String currentRequestUri, URLMapping mapping){
        this.delegatee = mapping;
        while(currentRequestUri.startsWith("/")) {
            currentRequestUri = currentRequestUri.substring(1);
        }
        while(currentRequestUri.endsWith("/")) {
            this.currentUriEndsWithSlash = true;
            currentRequestUri =  currentRequestUri.substring(0,currentRequestUri.length()-1);
        }
        this.currentRequestUri = currentRequestUri;
    }
    
    public List<String> getCanonicalPathsConfiguration() {
        return delegatee.getCanonicalPathsConfiguration();
    }

    public PageNode getMatchingPageNode(HttpServletRequest request, ContextBase contextBase) {
        return delegatee.getMatchingPageNode(request, contextBase);
    }

    public String rewriteLocation(Node node) {
        String absoluteLocation = delegatee.rewriteLocation(node);
        return computeRelativeUrl(absoluteLocation);
    }


    public String rewriteLocation(String path) {
        String absoluteLocation = delegatee.rewriteLocation(path);
        return computeRelativeUrl(absoluteLocation);
    }
    
    public String getLocation(String path) {
        path = delegatee.getLocation(path);
        return computeRelativeUrl(path);
    }
  
    private String computeRelativeUrl(String absoluteLocation) {
        while(absoluteLocation.startsWith("/")) {
            absoluteLocation = absoluteLocation.substring(1);
        }
        while(absoluteLocation.endsWith("/")) {
            absoluteLocation =  absoluteLocation.substring(0,absoluteLocation.length()-1);
        }

        String[] currentRequestUriParts = currentRequestUri.split("/");
        String[] absoluteLocationParts = absoluteLocation.split("/");
        StringBuffer relativeUrl = new StringBuffer("");
        boolean diverged = false;
        int matchDepth = 0;
        for(int i = 0 ;  i < currentRequestUriParts.length && !diverged; i++) {
            if(absoluteLocationParts.length > i) {
                if(absoluteLocationParts[i].equals(currentRequestUriParts[i])) {
                    matchDepth++;
                } else {
                    diverged = true;
                }
            }
        }
        int depth = matchDepth; 
        if(currentUriEndsWithSlash) {
            relativeUrl.append("../"); 
        }
        while( (++depth) < currentRequestUriParts.length) {
            relativeUrl.append("../");
        }
        
        boolean startsWith = false;
        if(matchDepth == currentRequestUriParts.length) {        	
        		 relativeUrl.append(currentRequestUriParts[matchDepth-1]);
        		 startsWith = true;
        }
        
        while( matchDepth < absoluteLocationParts.length) {
            if(!absoluteLocationParts[matchDepth].equals("")) {
            	if(startsWith) {
            	  relativeUrl.append("/");
            	  startsWith = false;
            	}            	
                relativeUrl.append(absoluteLocationParts[matchDepth]);
                matchDepth++;
                if(matchDepth < absoluteLocationParts.length) {
                    relativeUrl.append("/");
                }
            }
        }
        return relativeUrl.toString();
    }

    public String getContextPath() {
        return delegatee.getContextPath();
    }

}

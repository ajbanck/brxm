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
package org.hippoecm.hst.core.component;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public interface HstURL {
    
    String TYPE_ACTION = "action";
    String TYPE_RENDER = "render";
    String TYPE_RESOURCE = "resource";
    
    /**
     * Sets the url type: render, action or resource
     * @param type
     */
    void setType(String type);

    /**
     * Returns the url type: render, action or resource
     * @return
     */
    String getType();
    
    /**
     * Sets a parameter of this url.
     * @param name
     * @param value
     */
    void setParameter(String name, String value);

    /**
     * Sets a parameter array of this url.
     * @param name
     * @param values
     */
    void setParameter(String name, String[] values);
    
    /**
     * Sets parameter map of this url
     * @param parameters
     */
    void setParameters(Map<String, String[]> parameters);
    
    /**
     * Returns string representation of this url.
     * @return
     */
    String toString();
    
    /**
     * Returns the parameter map of this url.
     * @return
     */
    Map<String, String[]> getParameterMap();
    
    /**
     * Writes the string representation of this url.
     * @param out
     * @throws IOException
     */
    void write(Writer out) throws IOException;
    
    /**
     * Writes the string representation of this url, as xml-escaped.
     * @param out
     * @param escapeXML
     * @throws IOException
     */
    void write(Writer out, boolean escapeXML) throws IOException;
    
    /**
     * Allows setting a resource ID that can be retrieved when serving the resource
     * through HstRequest.getResourceID() method in a HstComponent instance. 
     * 
     * @param resourceID
     */
    void setResourceID(String resourceID);
    
    /**
     * Sets the base path of this url.
     * In normal case, this method does not need to be invoked because the container's
     * base url will be automatically detected.
     * However, if the base path should be set manually, then this method can be used.
     * If the basePath contains the context path, then the {@link #setBaseContext(String)}
     * should not be invoked. Otherwise, the {@link #setBaseContext(String)} should be invoked.
     * @param basePath
     */
    void setBasePath(String basePath);

    /**
     * Sets the context path of this url.
     * In normal case, this method does not need to be invoked because the container's
     * base context path will be automatically detected.
     * However, if the base context path should be set manullay, then this method can be used.
     * If the {@link #setBasePath(String)} was not invoked, this method should be invoked to
     * set the base context path.
     * @param baseContext
     */
    void setBaseContext(String baseContext);
    
}

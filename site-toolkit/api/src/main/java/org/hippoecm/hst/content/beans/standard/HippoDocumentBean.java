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
package org.hippoecm.hst.content.beans.standard;

import org.hippoecm.hst.content.beans.standard.HippoAvailableTranslationsBean.NoopTranslationsBean;

/**
 * This is a marker interface for all beans that represent a document. When developers implement their own bean which
 * does not extend the standard HippoDocument bean, they should implement this interface. This ensures that linkrewriting
 * works correctly for extensions like .html or .xml etc etc
 */
public interface HippoDocumentBean extends HippoBean {

    /**
     * 
     * @return the uuid of the canonical handle (the physical node) and <code>null</code> if some exception happened
     */
    String getCanonicalHandleUUID();
    
    
    /**
     * In case that there is no translation bean, a {@link NoopTranslationsBean} is returned, to make sure you do not need null checks
     * @return a {@link HippoAvailableTranslationsBean}. This method never returns <code>null</code>
     */
    HippoAvailableTranslationsBean<HippoDocumentBean> getAvailableTranslationsBean();
    
    /**
     * In case that there is no translation bean, a {@link NoopTranslationsBean} is returned, to make sure you do not need null checks
     * @param beanMappingClass
     * @return a {@link HippoAvailableTranslationsBean} where the translations must be of type <code>beanMappingClass</code>. This method never returns <code>null</code>
     */
    <T extends HippoBean> HippoAvailableTranslationsBean<T> getAvailableTranslationsBean(Class<T> beanMappingClass);
 
    
}

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
package org.hippoecm.hst.sitemenu;

import java.io.Serializable;
import java.util.List;

import org.hippoecm.hst.core.linking.HstLink;

/**
 * Interface for the implementation of a SiteMenuItem. Note that implementations are recommended to return an unmodifiable
 * (Sorted)List for the {@link #getChildMenuItems()}
 *
 */
public interface SiteMenuItem extends Serializable{
    /**
     * 
     * @return the name of this SiteMenuItem
     */
    String getName();
    
    /**
     * 
     * @return all direct child SiteMenuItem's of this item
     */
    List<SiteMenuItem> getChildMenuItems();
    
    /**
     * 
     * @return a HstLink that contains a proper link for this SiteMenuItem
     */
    HstLink getHstLink();
    
    /**
     * @return <code>true</code> is the SiteMenuItem is selected
     */
    boolean isSelected();
}

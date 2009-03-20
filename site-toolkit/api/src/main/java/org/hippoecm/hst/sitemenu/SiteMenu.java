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

/**
 * The interface for a SiteMenu implementation, containing possibly a tree of {@link SiteMenuItem}'s
 * 
 * Implementations of the interface are recommended to return an unmodifiable List for the {@link #getSiteMenuItems()} because
 * clients should not change the List, though this is up to the implementation 
 *
 */
public interface SiteMenu extends Serializable{
    /**
     * Returns the name of this SiteMenu. For example, you could have a "topmenu", "leftmenu" and "footermenu" on your site/portal,
     * where these names might be appropriate 
     * @return the name of this SiteMenu
     */
    String getName();
    
    /**
     * Based on the request, the implementation should be able to indicate whether this SiteMenu is selected (highlighted) or not
     * 
     * @return <code>true</code> when the current SiteMenu is selected
     */
    boolean isSelected();
    
    
    /**
     * @return returns all direct child {@link SiteMenuItem}'s of this SiteMenu
     */
    List<SiteMenuItem> getSiteMenuItems();
}

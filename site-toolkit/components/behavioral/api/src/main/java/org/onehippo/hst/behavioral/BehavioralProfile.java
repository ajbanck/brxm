/*
 *  Copyright 2011 Hippo.
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
package org.onehippo.hst.behavioral;

import java.util.List;

/**
 * A {@link BehavioralProfile} contains the behavioral information extracted from the current user
 * according to what browse behavior she exhibited. Based on this information a user can be
 * categorized as being one of several configured persona's.
 * <p>
 * A client of this class can inspect how a user scores on the different persona's.
 * </p>
 */
public interface BehavioralProfile {
    
    /**
     * @return  the list of {@link BehavioralPersonaScore}s sorted from highest to lowest score.
     */
    List<BehavioralPersonaScore> getPersonaScores();
    
    /**
     * @return  the <code>persona id</code> this user scores highest for or <code>null</code> if this
     * information is not available.
     */
    String getPrincipalPersonaId();
    
    /**
     * @return  the highest {@link BehavioralPersonaScore} for this user or <code>null</code> if this
     * information is not available.
     */
    BehavioralPersonaScore getPrincipalPersonaScore();
    
}

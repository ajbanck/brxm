/*
 *  Copyright 2008-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.hst.core.container;

import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;

/**
 * PageErrorHandler
 * <P>
 * When a valve meets component errors, error handling can be delegated to a PageErrorHandler.
 * </P>
 * 
 * @version $Id$
 */
public interface PageErrorHandler {
    
    /**
     * Processing status of page error handler.
     * <UL>
     * <LI><code>HANDLED_TO_STOP</code>: Value to return to stop the processing.</LI>
     * <LI><code>NOT_HANDLED</code>: Value to return to continue the processing, indicating no handling happened.</LI>
     * <LI><code>HANDLED_BUT_CONTINUE</code>: Value to return to continue the processing, indicating some handling happened.</LI>
     * </UL>
     */
    enum Status {
        HANDLED_TO_STOP,
        NOT_HANDLED,
        HANDLED_BUT_CONTINUE,
    }
    
    /**
     * Handles the exceptions generated by page or components.
     * <P>
     * An implementation should be aware of the current lifecycle phase by invoking {@link HstRequest#getLifecyclePhase()} method
     * if it wants to invoke some lifecycle dependent operations such as {@link HstResponse#forward(String)} or {@link HstResponse#sendRedirect(String)}.
     * </P>
     * 
     * @param pageErrors
     * @param hstRequest
     * @param hstResponse
     */
    Status handleComponentExceptions(PageErrors pageErrors, HstRequest hstRequest, HstResponse hstResponse);
    
}
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

/**
 * The <CODE>ContainerSecurityNotAuthorizedException</CODE> class defines a security exception
 * for unauthorized site resource access from an authenticated user.
 * 
 * @version $Id$
 */
public class ContainerSecurityNotAuthorizedException extends ContainerSecurityException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ContainerSecurityNotAuthorizedException exception.
     */
    public ContainerSecurityNotAuthorizedException() {
        super();
    }

    /**
     * Constructs a new ContainerSecurityNotAuthorizedException exception with the given message.
     *
     * @param   message
     *          the exception message
     */
    public ContainerSecurityNotAuthorizedException(String message) {
        super(message);
    }

    /**
     * Constructs a new ContainerSecurityNotAuthorizedException exception with the nested exception.
     *
     * @param   nested
     *          the nested exception
     */
    public ContainerSecurityNotAuthorizedException(Throwable nested) {
        super(nested);
    }

    /**
     * Constructs a new ContainerSecurityNotAuthorizedException exception when the container needs to do
     * the following:
     * <ul>
     * <li>throw an exception 
     * <li>include the "nested" exception
     * <li>include a description message
     * </ul>
     *
     * @param   msg
     *          the exception message
     * @param   nested
     *          the nested exception
     */
    public ContainerSecurityNotAuthorizedException(String msg, Throwable nested) {
        super(msg, nested);
    }
    
}
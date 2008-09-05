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
package org.hippoecm.repository.security.ldap;


public class LdapUserSearch {

    @SuppressWarnings("unused")
    final static String SVN_ID = "$Id$";
    
    /**
     * The search base: ou=Users,dc=onehippo,dc=org
     */
    public final static String PROPERTY_BASE_DN = "hippoldap:basedn";
    
    /**
     * The search filter: (objectClass=posixUser)
     */
    public final static String PROPERTY_FILTER = "hippoldap:filter";
    
    /**
     * The attribute that defines the name: uid
     */
    public final static String PROPERTY_NAME_ATTR = "hippoldap:nameattribute";
    

    private static final String DEFAULT_FILTER = "(objectclass=posixAccount)";

    // mandatory
    protected final String baseDn;
    protected final String nameAttr;
    
    protected String filter;

    public LdapUserSearch(String baseDn, String nameAttr) {
        this.baseDn = baseDn;
        this.nameAttr = nameAttr;
        setFilter(DEFAULT_FILTER);
    }

    public String getBaseDn() {
        return baseDn;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getNameAttr() {
        return nameAttr;
    }

}

/*
 * Copyright 2007 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.repository.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.security.principals.FacetAuthPrincipal;

public class RepositoryLoginHelper {
    

    public static Set<FacetAuthPrincipal> getFacetAuths(Node facetAuthPath) throws RepositoryException {
        NodeIterator nodeIter = facetAuthPath.getNodes();
        Set<FacetAuthPrincipal> principals = new HashSet<FacetAuthPrincipal>();
        while (nodeIter.hasNext()) {
            Node fa = (Node) nodeIter.next();
            Long permissions = fa.getProperty(HippoNodeType.HIPPO_PERMISSIONS).getLong();
            String facet = fa.getProperty(HippoNodeType.HIPPO_FACET).getString();
            Value[] facetValues = fa.getProperty(HippoNodeType.HIPPO_VALUES).getValues();
            String[] values = new String[facetValues.length];
            for (int i = 0; i < facetValues.length; i++) {
                values[i] = facetValues[i].getString();
            }
            principals.add(new FacetAuthPrincipal(facet, values, permissions));
        }
        return Collections.unmodifiableSet(principals);
    
    }

}

/*
 *  Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cms7.crisp.core.resource;

import org.onehippo.cms7.crisp.api.resource.ResourceLink;

/**
 * Simple {@link ResourceLink} implementation.
 */
public class SimpleResourceLink implements ResourceLink {

    /**
     * Link URI.
     */
    private String uri;

    /**
     * Constructs with URI string.
     * @param uri URI string
     */
    public SimpleResourceLink(String uri) {
        this.uri = uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUri() {
        return uri;
    }
}

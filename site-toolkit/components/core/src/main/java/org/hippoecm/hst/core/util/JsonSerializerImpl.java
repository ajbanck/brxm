/*
 *  Copyright 2016 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.hst.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hippoecm.hst.util.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonSerializerImpl implements JsonSerializer {

    private static final Logger log = LoggerFactory.getLogger(JsonSerializerImpl.class);

    @Override
    public String toJson(final Object value) {
        try {
            return new ObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            if (log.isDebugEnabled()) {
                log.warn("Could not process value to Json.", e);
            } else {
                log.warn("Could not process value to Json : {}", e.toString());
            }
            throw new JsonSerializationException("Could not process value to Json.", e);
        }
    }
}

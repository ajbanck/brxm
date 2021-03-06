/*
 *  Copyright 2019 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cms.services.validation;

import org.onehippo.cms.services.validation.api.internal.ValidationService;
import org.onehippo.cms.services.validation.api.internal.ValidatorInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ValidationServiceImpl implements ValidationService {

    public static final Logger log = LoggerFactory.getLogger(ValidationServiceImpl.class);

    private final ValidationServiceConfig config;

    ValidationServiceImpl(final ValidationServiceConfig config) {
        this.config = config;
    }

    @Override
    public ValidatorInstance getValidator(final String name) {
        return config.getValidatorInstance(name);
    }

}

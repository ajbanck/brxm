/*
 * Copyright 2019 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.cms.services.validation;

import java.util.Locale;
import java.util.Optional;

import org.onehippo.cms.services.validation.api.ValidationContext;
import org.onehippo.cms.services.validation.api.Validator;
import org.onehippo.cms.services.validation.api.ValidatorConfig;
import org.onehippo.cms.services.validation.api.ValidatorInstance;
import org.onehippo.cms.services.validation.api.Violation;

class ValidatorInstanceImpl implements ValidatorInstance {

    private final Validator validator;
    private final ValidatorConfig config;
    private final ThreadLocal<Locale> locale;

    ValidatorInstanceImpl(final Validator validator, final ValidatorConfig config) {
        this.validator = validator;
        this.config = config;
        locale = new ThreadLocal<>();
    }

    @Override
    public ValidatorConfig getConfig() {
        return config;
    }

    @Override
    public Optional<Violation> validate(final ValidationContext context, final String value) {
        try {
            locale.set(context.getLocale());
            return validator.validate(context, value, this);
        } finally {
            locale.remove();
        }
    }

    @Override
    public Violation createViolation() {
        return new TranslatedViolation(config.getName(), locale.get());
    }

    @Override
    public Violation createViolation(final String subKey) {
        final String key = config.getName() + "#" + subKey;
        return new TranslatedViolation(key, locale.get());
    }
}

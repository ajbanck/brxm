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
package org.onehippo.cms.services.validation.legacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This utility is created to support the pre-13.3.0 configuration of document field validators.
 *
 * @deprecated This class has only a function for 13.3.0 versions and higher but will be released in a next major
 * version.
 */
@Deprecated
public class LegacyValidatorMapper {

    private final static Set<String> combination = new HashSet<>(Arrays.asList("non-empty", "required"));

    /**
     * Map a legacy validator to one that works correctly with the current implementation if necessary.
     */
    public static Set<String> legacyMapper(final Set<String> legacyValidators, final String fieldType) {
        if (legacyValidators == null) {
            return null;
        }
        if (legacyValidators.size() == 0) {
            return Collections.emptySet();
        }

        final LinkedHashSet<String> validators = new LinkedHashSet<>(legacyValidators);

        if (validators.containsAll(combination)) {
            validators.remove("non-empty");
        }

        if (validators.contains("resource-required")) {
            validators.remove("resource-required");
            validators.add("required");
        }

        if ("html".equalsIgnoreCase(fieldType) && validators.contains("non-empty") && !validators.contains("required")) {
            validators.remove("non-empty");
            validators.add("non-empty-html");
        }

        return validators;
    }

    public static List<String> legacyMapper(final List<String> legacyValidators, final String fieldType) {
        return new ArrayList<>(legacyMapper(new LinkedHashSet<>(legacyValidators), fieldType));
    }

    private LegacyValidatorMapper() {
    }
}

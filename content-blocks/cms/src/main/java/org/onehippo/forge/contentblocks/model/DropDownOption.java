/*
 * Copyright 2012-2020 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.contentblocks.model;

import java.io.Serializable;

/**
 * Represents a single option with a label and a value to be used in a dropdown.
 */
public class DropDownOption implements Serializable {

    private final String label;
    private final String value;

    /**
     * Constructor
     *
     * @param value the value of the dropdown option
     * @param label the label of the dropdown option
     */
    public DropDownOption(final String value, final String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * Get the label of the option
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the value of the option
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }
}

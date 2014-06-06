/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cms7.essentials.dashboard.utils;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * @version "$Id$"
 */
public final class PayloadUtils {

    private static Logger log = LoggerFactory.getLogger(PayloadUtils.class);

    public static String[] extractValueArray(final CharSequence value) {
        if (Strings.isEmpty(value)) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        final List<String> strings = extractValueList(value);
        return strings.toArray(new String[strings.size()]);
    }

    public static List<String> extractValueList(final CharSequence value) {
        if (Strings.isEmpty(value)) {
            return Collections.emptyList();
        }
        final Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
        final Iterable<String> iterable = splitter.split(value);
        return Lists.newArrayList(iterable);
    }

    private PayloadUtils() {
    }
}

/*
 *  Copyright 2020 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.hst.content.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PageModelAnyGetter {

    /**
     * Optional argument that defines whether this annotation is active or not. The only use for value 'false' if for
     * overriding purposes. Overriding may be necessary when used with "mix-in annotations" (aka "annotation
     * overrides"). For most cases, however, default value of "true" is just fine and should be omitted.
     *
     * @return True if annotation is enabled (normal case); false if it is to be ignored (only useful for mix-in
     * annotations to "mask" annotation
     * @since 2.9
     */
    boolean enabled() default true;

}

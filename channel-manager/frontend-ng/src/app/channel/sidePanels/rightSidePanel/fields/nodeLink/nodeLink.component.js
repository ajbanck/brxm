/*
 * Copyright 2018-2019 Hippo B.V. (http://www.onehippo.com)
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

import template from './nodeLink.html';
import controller from './nodeLink.controller';
import './nodeLink.scss';

const nodeLinkComponent = {
  controller,
  template,
  bindings: {
    ariaLabel: '@',
    config: '<',
    disabled: '<?',
    displayName: '=',
    hint: '<',
    index: '<',
    name: '@',
  },
  require: {
    ngModel: 'ngModel',
    mdInputContainer: '?^^mdInputContainer',
  },
};

export default nodeLinkComponent;
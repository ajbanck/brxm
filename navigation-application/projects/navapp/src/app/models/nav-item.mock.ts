/*
 * Copyright 2019-2020 BloomReach. All rights reserved. (https://www.bloomreach.com/)
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

import { NEVER, Observable } from 'rxjs';

import { NavItem } from './nav-item.model';

export class NavItemMock extends NavItem {
  constructor(initObject = {}, unsubscribe: Observable<void> = NEVER, activated = true) {
    const dto = {
      id: 'testNavItemId',
      displayName: 'testDisplayName',
      appIframeUrl: 'https://test.url',
      appPath: 'testPath',
      ...initObject,
    };

    super(dto, unsubscribe);

    if (activated) {
      this.activate();
    }
  }
}

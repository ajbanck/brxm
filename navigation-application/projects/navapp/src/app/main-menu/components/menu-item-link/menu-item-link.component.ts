/*
 * Copyright 2019 BloomReach. All rights reserved. (https://www.bloomreach.com/)
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

import { Component, HostBinding, Input } from '@angular/core';

import { DeepLinkingService } from '../../../routing/deep-linking.service';
import { MenuItemLink } from '../../models/menu-item-link.model';

@Component({
  selector: 'brna-menu-item-link',
  templateUrl: 'menu-item-link.component.html',
  styleUrls: ['menu-item-link.component.scss'],
})
export class MenuItemLinkComponent {
  @Input()
  config: MenuItemLink;

  @Input()
  @HostBinding('class.active')
  active = false;

  constructor(private deepLinkingService: DeepLinkingService) {}

  onClick(e: MouseEvent): void {
    e.preventDefault();

    this.deepLinkingService.navigateByAppUrl(this.config.navItem.appIframeUrl, this.config.navItem.appPath);
  }
}

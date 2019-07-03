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

import { animate, style, transition, trigger } from '@angular/animations';
import {
  Component,
  HostBinding,
  Input,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { Subject } from 'rxjs';

import { QaHelperService } from '../../../services/qa-helper.service';
import { MenuItemContainer } from '../../models/menu-item-container.model';
import { MenuItem } from '../../models/menu-item.model';
import { MenuStateService } from '../../services/menu-state.service';

@Component({
  selector: 'brna-menu-drawer',
  templateUrl: 'menu-drawer.component.html',
  styleUrls: ['menu-drawer.component.scss'],
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateX(-100%)' }),
        animate('300ms ease-in-out', style({ transform: 'translateX(0%)' })),
      ]),
      transition(':leave', [
        animate('300ms ease-in-out', style({ transform: 'translateX(-100%)' })),
      ]),
    ]),
    trigger('fadeIn', [
      transition('* => *', [
        style({ opacity: '0' }),
        animate('300ms ease-in-out', style({ opacity: '1' })),
      ]),
    ]),
  ],
})
export class MenuDrawerComponent implements OnChanges {
  configChange$ = new Subject();

  @HostBinding('@slideInOut')
  animate = true;

  @Input()
  config: MenuItemContainer;

  constructor(
    private menuStateService: MenuStateService,
    private qaHelperService: QaHelperService,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if ('config' in changes) {
      this.configChange$.next({});
    }
  }

  onClickedOutside(): void {
    this.menuStateService.closeDrawer();
  }

  isContainer(item: MenuItem): boolean {
    return item instanceof MenuItemContainer;
  }

  isActive(item: MenuItem): boolean {
    return this.menuStateService.isMenuItemActive(item);
  }

  getQaClass(item: MenuItem): string {
    return this.qaHelperService.getMenuItemClass(item);
  }
}

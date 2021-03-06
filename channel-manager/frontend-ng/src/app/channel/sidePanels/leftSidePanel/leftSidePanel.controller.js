/*
 * Copyright 2016-2020 Hippo B.V. (http://www.onehippo.com)
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

const LS_KEY_PANEL_WIDTH = 'channelManager.sidePanel.left.width';
const MIN_WIDTH = 290;

class LeftSidePanelCtrl {
  constructor(
    $element,
    $scope,
    localStorageService,
    CatalogService,
    LeftSidePanelService,
    SidePanelService,
    SiteMapService,
  ) {
    'ngInject';

    this.$element = $element;
    this.$scope = $scope;
    this.localStorageService = localStorageService;
    this.CatalogService = CatalogService;
    this.LeftSidePanelService = LeftSidePanelService;
    this.SidePanelService = SidePanelService;
    this.SiteMapService = SiteMapService;
  }

  $onInit() {
    this.sideNavElement = this.$element.find('.left-side-panel');
    this.width = Math.max(this._getStoredWidth(), MIN_WIDTH);
  }

  _getStoredWidth() {
    return parseInt(this.localStorageService.get(LS_KEY_PANEL_WIDTH), 10) || -1;
  }

  $postLink() {
    this.SidePanelService.initialize('left', this.$element, this.sideNavElement);
  }

  onResize(newWidth) {
    if (newWidth < MIN_WIDTH && this.width === MIN_WIDTH) {
      return;
    }

    this.width = Math.max(newWidth, MIN_WIDTH);
    this.localStorageService.set(LS_KEY_PANEL_WIDTH, this.width);
    this.$scope.$digest();
  }

  get selectedTab() {
    return this.LeftSidePanelService.selectedTab;
  }

  set selectedTab(selectedTab) {
    this.LeftSidePanelService.selectedTab = selectedTab;
  }

  get isOpen() {
    return this.LeftSidePanelService.isOpen;
  }

  set isOpen(isOpen) {
    this.LeftSidePanelService.isOpen = isOpen;
  }

  isLockedOpen() {
    return this.SidePanelService.isOpen('left');
  }

  showComponentsTab() {
    const catalog = this.getCatalog();
    return this.componentsVisible && catalog.length > 0;
  }

  getCatalog() {
    return this.CatalogService.getComponents();
  }

  getSiteMapItems() {
    return this.SiteMapService.get();
  }

  isSidePanelLifted() {
    return this.SidePanelService.isSidePanelLifted;
  }
}

export default LeftSidePanelCtrl;

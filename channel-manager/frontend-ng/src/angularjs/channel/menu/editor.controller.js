/*
 * Copyright 2016 Hippo B.V. (http://www.onehippo.com)
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

export class MenuEditorCtrl {
  constructor($q, $filter, $scope, $translate, SiteMenuService, FormStateService, HippoIframeService, DialogService,
              FeedbackService, ChannelService, PickerService) {
    'ngInject';

    this.$q = $q;
    this.$filter = $filter;
    this.$translate = $translate;
    this.SiteMenuService = SiteMenuService;
    this.FormStateService = FormStateService;
    this.HippoIframeService = HippoIframeService;
    this.DialogService = DialogService;
    this.FeedbackService = FeedbackService;
    this.ChannelService = ChannelService;
    this.PickerService = PickerService;

    this.isSaving = {};

    SiteMenuService.loadMenu(this.menuUuid)
      .then((menu) => {
        this.items = menu.items;

        // Currently, the SiteMenuService is loading and maintaining the menu structure.
        // Creation or deletion of a menu item trigger a full reload of the menu, and the
        // $watch below makes sure the MenuEditorCtrl becomes aware of these reloads.
        // TODO: this is ugly, inefficient and hard to maintain. We should improve this.
        $scope.$watch(
          () => menu.items,
          () => {
            this.items = menu.items;
          }
        );
      })
      .catch(() => this.onError({ key: 'ERROR_MENU_LOAD_FAILED' }));

    this.treeOptions = {
      // created an issue for the Tree component, to add a disabled state
      // link: https://github.com/JimLiu/angular-ui-tree/issues/63
      // for now, simply don't accept any moves when the form is invalid
      accept: () => FormStateService.isValid(),
      dropped: (event) => {
        const source = event.source;
        const sourceNodeScope = source.nodeScope;
        const sourceId = sourceNodeScope.$modelValue.id;
        const dest = event.dest;
        const destNodesScope = dest.nodesScope;
        const destId = destNodesScope.$nodeScope ? destNodesScope.$nodeScope.$modelValue.id : this.menuUuid;

        if (source.nodesScope !== destNodesScope || source.index !== dest.index) {
          SiteMenuService.moveMenuItem(this.menuUuid, sourceId, destId, dest.index)
            .then(() => { this.isMenuModified = true; })
            .catch(() => this.onError({ key: 'ERROR_MENU_MOVE_FAILED' }));
        }
      },
    };
  }

  _startEditingItem(item) {
    this.editingItem = item;
  }

  stopEditingItem() {
    this.editingItem = null;
  }

  toggleEditState(item) {
    if (!this.editingItem || this.editingItem.id !== item.id) {
      this.SiteMenuService.getEditableMenuItem(this.menuUuid, item.id)
        .then((editableItem) => this._startEditingItem(editableItem));
    } else {
      this.stopEditingItem();
    }
  }

  addItem() {
    this.isSaving.newItem = true;

    this.SiteMenuService.getMenu(this.menuUuid)
      .then((menu) => this._createBlankMenuItem(menu))
      .then((blankItem) => this.SiteMenuService.createEditableMenuItem(this.menuUuid, blankItem))
      .then((editableItem) => {
        this.isMenuModified = true;
        this.FormStateService.setValid(true);
        this.isSaving.newItem = false;
        this._startEditingItem(editableItem);
      })
      .catch((response) => {
        response = response || {};

        this.isSaving.newItem = false;
        this.onError({ key: 'ERROR_MENU_CREATE_FAILED', params: response.data });
      });
  }

  linkPick(targetEvent) {
    this.SiteMenuService.getMenu(this.menuUuid).then((menu) => {
      const pickerTypes = [
        {
          id: menu.siteContentIdentifier,
          name: 'Documents',
          type: 'documents',
        },
        {
          id: menu.siteMapIdentifier,
          name: 'Pages (Sitemap Items)',
          type: 'pages',
        },
      ];
      const pickerCfg = {
        locals: {
          pickerTypes,
          initialLink: this.editingItem.sitemapLink,
        },
        targetEvent,
      };

      this.PickerService.show(pickerCfg).then(({ pathInfo }) => {
        this.editingItem.sitemapLink = this.editingItem.link = pathInfo;
      });
    });
  }

  onBack() {
    if (this.isMenuModified) {
      this.HippoIframeService.reload();
      this.ChannelService.recordOwnChange();
    }
    this.onDone();
  }

  // TODO: Move this logic into the SiteMenuService. Don't make this controller worry about the prototypeItem.
  _createBlankMenuItem(menu) {
    const incFilter = this.$filter('incrementProperty');
    const result = {
      linkType: 'SITEMAPITEM',
      title: incFilter(menu.items, 'title', this.$translate.instant('SUBPAGE_MENU_EDITOR_NEW_ITEM_TITLE'), 'items'),
      link: '',
    };
    if (angular.isObject(menu.prototypeItem)) {
      result.localParameters = angular.copy(menu.prototypeItem.localParameters);
    }
    return result;
  }

  saveItem() {
    this.SiteMenuService.saveMenuItem(this.menuUuid, this.editingItem)
      .then(() => {
        this.isMenuModified = true;
        this.stopEditingItem();
      })
      .catch((response) => {
        response = response || {};

        this.FeedbackService.showErrorOnSubpage('ERROR_MENU_ITEM_SAVE_FAILED', response.data);
      });
  }

  _doDelete() {
    return this.SiteMenuService.deleteMenuItem(this.menuUuid, this.editingItem.id)
      .then(() => {
        this.isMenuModified = true;
        this.stopEditingItem();
      })
      .catch((response) => {
        response = response || {};

        this.FeedbackService.showErrorOnSubpage('ERROR_MENU_ITEM_DELETE_FAILED', response.data);
      });
  }

  _confirmDelete() {
    const confirm = this.DialogService.confirm()
      .textContent(this.$translate.instant('CONFIRM_DELETE_MENU_ITEM_MESSAGE', {
        menuItem: this.editingItem.title,
      }))
      .ok(this.$translate.instant('DELETE'))
      .cancel(this.$translate.instant('CANCEL'));

    return this.DialogService.show(confirm);
  }

  deleteItem() {
    this._confirmDelete().then(() => this._doDelete());
  }

  hasLocalParameters() {
    return Object.keys(this.editingItem.localParameters).length !== 0;
  }
}

/*
 * Copyright 2020 Hippo B.V. (http://www.onehippo.com)
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

class VersionsInfoWrapperCtrl {
  constructor(
    $uiRouterGlobals,
    $rootScope,
    PageStructureService,
    ProjectService,
  ) {
    'ngInject';

    this.$uiRouterGlobals = $uiRouterGlobals;
    this.$rootScope = $rootScope;
    this.PageStructureService = PageStructureService;
    this.ProjectService = ProjectService;
  }

  $onInit() {
    this.documentId = this.$uiRouterGlobals.params.documentId;
    this.branchId = this.ProjectService.getSelectedProjectId();
    this.unpublishedVariantId = this._getUnpublishedVariantId();

    this._onPageChange = this.$rootScope.$on('page:change', () => {
      this.unpublishedVariantId = this._getUnpublishedVariantId();
    });
    this._onPageCheckChanges = this.$rootScope.$on('page:check-changes', () => {
      this.unpublishedVariantId = this._getUnpublishedVariantId();
    });
  }

  $onDestroy() {
    this._onPageChange();
    this._onPageCheckChanges();
  }

  _getUnpublishedVariantId() {
    return this.PageStructureService
      .getPage()
      .getMeta()
      .getUnpublishedVariantId();
  }
}

export default VersionsInfoWrapperCtrl;
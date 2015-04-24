/*
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
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

(function () {
  'use strict';

  angular.module('hippo.channel.menu')

    .controller('hippo.channel.menu.EditMenuItemFormCtrl', [
      '$rootScope',
      '$scope',
      '$stateParams',
      '$filter',
      'hippo.channel.FormStateService',
      'hippo.channel.Container',
      'hippo.channel.menu.FocusService',
      'hippo.channel.menu.MenuService',
      function ($rootScope, $scope, $stateParams, $filter, FormStateService, ContainerService, FocusService, MenuService) {
        var EditMenuItemFormCtrl = this,
          translate = $filter('translate');

        EditMenuItemFormCtrl.focus = FocusService.focusElementWithId;

        $scope.$watch(function () {
          return $scope.form;
          if ($stateParams.selectedDocumentPath) {
            $scope.MenuItemCtrl.selectedMenuItem.link = $scope.MenuItemCtrl.selectedMenuItem.sitemapLink = $stateParams.selectedDocumentPath;
            $scope.EditMenuItemCtrl.linkToFocus = 'sitemapLink';
            $scope.EditMenuItemCtrl.saveSelectedMenuItem('link');
            $stateParams.selectedDocumentPath = null;
          }
        });

        // The following logic will check the client-side validation of the
        // edit menu item form. When a field is invalid, the FormStateService
        // will be updated, so the window can't be closed.
        $scope.$watch(function () {
          return $scope.form.title.$valid;
        }, function () {
          checkFormValidity();
        });

        $scope.$watch(function () {
          return $scope.MenuItemCtrl.selectedMenuItem.linkType;
        }, function () {
          checkFormValidity();
        });

        $scope.$watch(function () {
          return $scope.form.sitemapItem.$valid;
        }, function () {
          checkFormValidity();
        });

        $scope.$watch(function () {
          return $scope.form.url.$valid;
        }, function () {
          checkFormValidity();
        });

        function checkFormValidity () {
          var isValid = $scope.form.title.$valid &&
            $scope.form.destination.$valid;

          if ($scope.MenuItemCtrl.selectedMenuItem) {
            if ($scope.MenuItemCtrl.selectedMenuItem.linkType === 'EXTERNAL') {
              isValid = isValid && ($scope.form.url.$valid);
            } else if ($scope.MenuItemCtrl.selectedMenuItem.linkType === 'SITEMAPITEM') {
              isValid = isValid && ($scope.form.sitemapItem.$valid);
            }
          }

          FormStateService.setValid(isValid);
        }

        $scope.$watch(function () {
          return $scope.form.$dirty;
        }, function () {
          FormStateService.setDirty($scope.form.$dirty);
        });

        // The following logic will set the correct error messages when the
        // client-side validation status updates.
        $scope.$watch(function () {
          return $scope.form.title.$error.required;
        }, function () {
          $scope.EditMenuItemCtrl.fieldFeedbackMessage.title = translate('LABEL_REQUIRED');
        });

        $scope.$watch(function () {
          return $scope.form.title.$valid;
        }, function (value) {
          if (value) {
            $scope.EditMenuItemCtrl.fieldFeedbackMessage.title = '';
          }
        });

        $scope.$watch(function () {
          return $scope.form.url.$error.required;
        }, function () {
          $scope.EditMenuItemCtrl.fieldFeedbackMessage.url = translate('EXTERNAL_LINK_REQUIRED');
        });

        $scope.$watch(function () {
          return $scope.form.url.$valid;
        }, function (value) {
          if (value) {
            $scope.EditMenuItemCtrl.fieldFeedbackMessage.url = '';
          }
        });

        function saveFieldIfDirty (fieldName, propertyName) {
          if ($scope.form[fieldName].$dirty) {
            $scope.EditMenuItemCtrl.saveSelectedMenuItem(propertyName);
          }
        }

        $scope.$on('container:before-close', function () {
          if ($scope.form.$dirty) {
            saveFieldIfDirty('title', 'title');
            saveFieldIfDirty('sitemapItem', 'link');
            saveFieldIfDirty('url', 'link');
          }
        });

        $scope.$on('container:close', function (event) {
          // prevent close, process all menu changes first and then trigger the close ourselves
          event.preventDefault();
          MenuService.processAllChanges().then(function () {
            ContainerService.performClose();
          });
        });
      }
    ]);
}());

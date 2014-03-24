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
(function () {
    'use strict';

    angular.module('hippo.channel.page')

        .controller('hippo.channel.page.SettingsCtrl', [
            '$scope',
            'hippo.channel.FeedbackService',
            'hippo.channel.PageService',
            'hippo.channel.PrototypeService',
            'hippo.channel.ConfigService',
            'hippo.channel.Container',
            function ($scope, FeedbackService, PageService, PrototypeService, ConfigService, ContainerService) {
                $scope.page = {
                    id: null,
                    title: '',
                    url: '',
                    prototype: {
                        id: null
                    }
                };

                $scope.state = {
                    isEditable: false,
                    isLocked: false
                };

                $scope.lock = {
                    owner: null,
                    timestamp: null
                };

                $scope.template = {
                    isVisible: false
                };

                $scope.validation = {
                    illegalCharacters: '/ :'
                };

                $scope.host = '';

                function setErrorFeedback(errorResponse) {
                    $scope.errorFeedback = FeedbackService.getFeedback(errorResponse);
                }

                // fetch host
                PageService.getHost().then(function (response) {
                    $scope.host = response;
                }, setErrorFeedback);

                PrototypeService.getPrototypes()

                    //prototypes
                    .then(function (response) {
                        $scope.prototypes = response;

                        return PageService.getCurrentPage();
                    }, setErrorFeedback)

                    // page
                    .then(function (currentPage) {
                        $scope.page.id = currentPage.id;
                        $scope.page.title = currentPage.pageTitle;
                        $scope.page.url = currentPage.name;
                        $scope.page.hasContainerItem = currentPage.hasContainerItemInPageDefinition;

                        // only pages whose sitemap item is located in the HST workspace are editable,
                        // unless they are locked by someone else
                        $scope.state.isLocked = angular.isString(currentPage.lockedBy) && currentPage.lockedBy !== ConfigService.cmsUser;
                        $scope.state.isEditable = !$scope.state.isLocked && currentPage.workspaceConfiguration;

                        // lock information
                        $scope.lock.owner = currentPage.lockedBy;
                        $scope.lock.timestamp = currentPage.lockedOn;

                        // only pages whose sitemap item is located in the HST workspace are editable
                        $scope.isPageEditable = currentPage.workspaceConfiguration;
                    }, setErrorFeedback);

                $scope.submit = function () {
                    var pageModel = {
                        id: $scope.page.id,
                        pageTitle: $scope.page.title,
                        name: $scope.page.url,
                        componentConfigurationId: $scope.page.prototype.id
                    };

                    PageService.updatePage(pageModel).then(function (response) {
                        ContainerService.showPage(pageModel.name);
                    }, function (errorResponse) {
                        setErrorFeedback(errorResponse);
                    });
                };

                $scope.closeContainer = function() {
                    ContainerService.performClose();
                };
            }
        ]);
}());

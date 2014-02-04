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
    "use strict";

    angular.module('hippo.channelManager.menuManager')

        .controller('hippo.channelManager.menuManager.LoaderCtrl', [
            '$location',
            '$log',
            '$state',
            'hippo.channelManager.menuManager.ConfigService',
            'hippo.channelManager.menuManager.MenuService',
            function ($location, $log, $state, ConfigService, MenuService) {

                console.log('loader ctrl init');

                MenuService.getMenu(ConfigService.menuId).then(function (data) {
                    // go to default state
                    $state.go('menu-item.edit', {
                        menuItemId: data.children[0].id
                    });
                }, function (error) {
                    $log.error(error);
                });
            }
        ]);
}());
/*
 * Copyright © 2016-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* eslint-disable import/no-unresolved, import/default */

import pluginsTemplate from './plugins.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function PluginRoutes($stateProvider) {

    $stateProvider
        .state('home.plugins', {
            url: '/plugins',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['SYS_ADMIN', 'TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: pluginsTemplate,
                    controllerAs: 'vm',
                    controller: 'PluginController'
                }
            },
            data: {
                searchEnabled: true,
                pageTitle: 'plugin.plugins'
            },
            ncyBreadcrumb: {
                label: '{"icon": "extension", "label": "plugin.plugins", "link": "/static/svg/pluginslightgray.svg"}'
            }
        });
}

/*
 * Copyright © 2017-2018 Hashmap, Inc
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

import metadataTemplate from './metadata.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function MetadataRoutes($stateProvider, types) {
    $stateProvider
        .state('home.metadata', {
            url: '/metadata',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: metadataTemplate,
                    controller: 'MetadataController',
                    controllerAs: 'vm'
                }
            },
            data: {
                alertsType: 'tenant',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.alert,
                pageTitle: 'metadata.metadata'
            },
            ncyBreadcrumb: {
                label: '{"icon": "metadata", "label": "metadata.metadata", "link": "/static/svg/metadata-icon.svg"}'
            }
        })

}

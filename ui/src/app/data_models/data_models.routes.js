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

import dataModelsTemplate from './data_models.tpl.html';
import dataModelTemplate from './data_model.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function DataModelsRoutes($stateProvider, types) {
    $stateProvider
        .state('home.data_models', {
            url: '/data_models',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: dataModelsTemplate,
                    controller: 'DataModelsController',
                    controllerAs: 'vm'
                }
            },
            data: {
                alertsType: 'tenant',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.alert,
                pageTitle: 'data_model.data_models'
            },
            ncyBreadcrumb: {
                label: '{"icon": "data_models", "label": "data_model.data_models", "link": "/static/svg/data-models-icon.svg"}'
            }
        })
        .state('home.data_models.data_model', {
            url: '/:datamodelId?state',
            reloadOnSearch: false,
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: dataModelTemplate,
                    controller: 'DataModelController',
                    controllerAs: 'vm'
                }
            },
            data: {
                widgetEditMode: false,
                searchEnabled: false,
                pageTitle: 'data_model.data_models'
            },
            ncyBreadcrumb: {
                label: '{"icon": "data_models", "label": "{{ vm.data_model.title }}", "translate": "false", "link": "/static/svg/data-models-icon.svg"}'
            }
        })
}

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

import dashboardsTemplate from './dashboards.tpl.html';
import dashboardTemplate from './dashboard.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function DashboardRoutes($stateProvider) {
    $stateProvider
        .state('home.dashboards', {
            url: '/dashboards',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: dashboardsTemplate,
                    controller: 'DashboardsController',
                    controllerAs: 'vm'
                }
            },
            data: {
                dashboardsType: 'tenant',
                searchEnabled: true,
                pageTitle: 'dashboard.dashboards'
            },
            ncyBreadcrumb: {
                label: '{"icon": "dashboard", "label": "dashboard.dashboards", "link": "/static/svg/dashboardlightgray.svg"}'
            }
        })
        .state('home.customers.dashboards', {
            url: '/:customerId/dashboards',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: dashboardsTemplate,
                    controllerAs: 'vm',
                    controller: 'DashboardsController'
                }
            },
            data: {
                dashboardsType: 'customer',
                searchEnabled: true,
                pageTitle: 'customer.dashboards'
            },
            ncyBreadcrumb: {
                label: '{"icon": "dashboard", "label": "{{ vm.customerDashboardsTitle }}", "translate": "false", "link": "/static/svg/dashboardlightgray.svg"}'
            }
        })
        .state('home.dashboards.dashboard', {
            url: '/:dashboardId?state',
            reloadOnSearch: false,
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: dashboardTemplate,
                    controller: 'DashboardController',
                    controllerAs: 'vm'
                }
            },
            data: {
                widgetEditMode: false,
                searchEnabled: false,
                pageTitle: 'dashboard.dashboard'
            },
            ncyBreadcrumb: {
                label: '{"icon": "dashboard", "label": "{{ vm.dashboard.title }}", "translate": "false", "link": "/static/svg/dashboardlightgray.svg"}'
            }
        })
        .state('home.customers.dashboards.dashboard', {
            url: '/:dashboardId?state',
            reloadOnSearch: false,
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: dashboardTemplate,
                    controller: 'DashboardController',
                    controllerAs: 'vm'
                }
            },
            data: {
                searchEnabled: false,
                pageTitle: 'customer.dashboard'
            },
            ncyBreadcrumb: {
                label: '{"icon": "dashboard", "label": "customer.dashboard", "link": "/static/svg/dashboardlightgray.svg"}'
            }
        })
}

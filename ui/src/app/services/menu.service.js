/*
 * Copyright © 2016-2017 The Thingsboard Authors
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
import thingsboardApiUser from '../api/user.service';

export default angular.module('thingsboard.menu', [thingsboardApiUser])
    .factory('menu', Menu)
    .name;

/*@ngInject*/
function Menu(userService, $state, $rootScope) {

    var authority = '';
    var sections = [];
    var homeSections = [];

    if (userService.isUserLoaded() === true) {
        buildMenu();
    }

    var authenticatedHandle = $rootScope.$on('authenticated', function () {
        buildMenu();
    });

    var service = {
        authenticatedHandle: authenticatedHandle,
        getHomeSections: getHomeSections,
        getSections: getSections,
        sectionHeight: sectionHeight,
        sectionActive: sectionActive
    }

    return service;

    function getSections() {
        return sections;
    }

    function getHomeSections() {
        return homeSections;
    }

    function buildMenu() {
        var user = userService.getCurrentUser();
        if (user) {
            if (authority !== user.authority) {
                sections = [];
                authority = user.authority;
                if (authority === 'SYS_ADMIN') {
                    sections = [
                        {
                            name: 'home.home',
                            type: 'link',
                            state: 'home.links',
                            icon: 'home'
                        },
                        {
                            name: 'plugin.plugins',
                            type: 'link',
                            state: 'home.plugins',
                            icon: 'extension',
                            link: '/static/svg/pluginslightgray.svg'
                        },
                        {
                            name: 'rule.rules',
                            type: 'link',
                            state: 'home.rules',
                            icon: 'settings_ethernet',
                            link: '/static/svg/businessruleslightgray.svg'
                        },
                        {
                            name: 'tenant.tenants',
                            type: 'link',
                            state: 'home.tenants',
                            icon: 'supervisor_account'
                        },
                        {
                            name: 'widget.widget-library',
                            type: 'link',
                            state: 'home.widgets-bundles',
                            icon: 'now_widgets',
                            link: '/static/svg/widgetslightgray.svg'
                        },
                        {
                            name: 'admin.system-settings',
                            type: 'toggle',
                            state: 'home.settings',
                            height: '80px',
                            icon: 'settings',
                            pages: [
                                {
                                    name: 'admin.general',
                                    type: 'link',
                                    state: 'home.settings.general',
                                    icon: 'settings_applications'                                },
                                {
                                    name: 'admin.outgoing-mail',
                                    type: 'link',
                                    state: 'home.settings.outgoing-mail',
                                    icon: 'mail'                                }
                            ]
                        }];
                    homeSections =
                        [{
                            name: 'rule-plugin.management',
                            places: [
                                {
                                    name: 'plugin.plugins',
                                    icon: 'extension',
                                    state: 'home.plugins',
                                    link: 'static/svg/pluginslightgray.svg'
                                },
                                {
                                    name: 'rule.rules',
                                    icon: 'settings_ethernet',
                                    state: 'home.rules',
                                    link: '/static/svg/businessruleslightgray.svg'
                                }
                            ]
                        },
                        {
                            name: 'tenant.management',
                            places: [
                                {
                                    name: 'tenant.tenants',
                                    icon: 'supervisor_account',
                                    state: 'home.tenants'
                                }
                            ]
                        },
                            {
                                name: 'widget.management',
                                places: [
                                    {
                                        name: 'widget.widget-library',
                                        icon: 'now_widgets',
                                        state: 'home.widgets-bundles',
                                        link: '/static/svg/widgetslightgray.svg'
                                    }
                                ]
                            },
                            {
                                name: 'admin.system-settings',
                                places: [
                                    {
                                        name: 'admin.general',
                                        icon: 'settings_applications',
                                        state: 'home.settings.general'
                                    },
                                    {
                                        name: 'admin.outgoing-mail',
                                        icon: 'mail',
                                        state: 'home.settings.outgoing-mail',
                                    }
                                ]
                            }];
                } else if (authority === 'TENANT_ADMIN') {
                    sections = [
                        {
                            name: 'home.home',
                            type: 'link',
                            state: 'home.links',
                            icon: 'home'
                        },  
                        {
                            name: 'Applications',
                            type: 'link',
                            state: 'home.applications',
                            icon: 'dashboards',
                            link: '/static/svg/applicationslightgray.svg'
                        }, 
                        {
                            name: 'Tempusboard',
                            type: 'link',
                            state: 'home.tempusboard',
                            icon: 'dashboards',
                            link: '/static/svg/tempusboardlightgray.svg'
                        },
                        {
                            name: 'plugin.plugins',
                            type: 'link',
                            state: 'home.plugins',
                            icon: 'extension',
                            link: '/static/svg/pluginslightgray.svg'
                        },
                        {
                            name: 'rule.rules',
                            type: 'link',
                            state: 'home.rules',
                            icon: 'settings_ethernet',
                            link: '/static/svg/businessruleslightgray.svg'
                        },
                        {
                            name: 'customer.customers',
                            type: 'link',
                            state: 'home.customers',
                            icon: 'supervisor_account',
                            link: '/static/svg/businessunitslightgray.svg'
                        },
                        {
                            name: 'asset.assets',
                            type: 'link',
                            state: 'home.assets',
                            icon: 'domain',
                            link: '/static/svg/assetslightgray.svg'
                        },
                        {
                            name: 'device.devices',
                            type: 'link',
                            state: 'home.devices',
                            icon: 'devices_other',
                            link: '/static/svg/deviceslightgray.svg'
                        },
                        {
                            name: 'widget.widget-library',
                            type: 'link',
                            state: 'home.widgets-bundles',
                            icon: 'now_widgets',
                            link: '/static/svg/widgetslightgray.svg'
                        },
                        {
                            name: 'dashboard.dashboards',
                            type: 'link',
                            state: 'home.dashboards',
                            icon: 'dashboards',
                            link: '/static/svg/dashboardlightgray.svg'
                        },
                        {
                            name: 'computation.computations',
                            type: 'link',
                            state: 'home.computations',
                            icon: 'dashboards',
                            link: '/static/svg/computationslightgray.svg'
                        }];

                    homeSections =
                        [{
                            name: 'rule-plugin.management',
                            places: [
                                {
                                    name: 'plugin.plugins',
                                    icon: 'extension',
                                    state: 'home.plugins',
                                    link: '/static/svg/pluginslightgray.svg'

                                },
                                {
                                    name: 'rule.rules',
                                    icon: 'settings_ethernet',
                                    state: 'home.rules',
                                    link: '/static/svg/businessruleslightgray.svg'
                                }
                            ]
                        },
                        {
                            name: 'customer.management',
                            places: [
                                {
                                    name: 'customer.customers',
                                    icon: 'supervisor_account',
                                    state: 'home.customers',
                                    link: '/static/svg/businessunitslightgray.svg'
                                }
                            ]
                        },
                            {
                                name: 'asset.management',
                                places: [
                                    {
                                        name: 'asset.assets',
                                        icon: 'domain',
                                        state: 'home.assets',
                                        link: '/static/svg/assetslightgray.svg'
                                    }
                                ]
                            },
                            {
                                name: 'Tempusboard',
                                places: [
                                    {
                                        name: 'Tempusboard',
                                        icon: 'dashboard',
                                        state: 'home.tempusboard',
                                        link: '/static/svg/tempusboardlightgray.svg'
                                    }
                                ]
                            },
                            {
                                name: 'device.management',
                                places: [
                                    {
                                        name: 'device.devices',
                                        icon: 'devices_other',
                                        state: 'home.devices',
                                        link: '/static/svg/deviceslightgray.svg'
                                    }
                                ]
                            },
                            {
                                name: 'dashboard.management',
                                places: [
                                    {
                                        name: 'widget.widget-library',
                                        icon: 'now_widgets',
                                        state: 'home.widgets-bundles',
                                        link: '/static/svg/widgetslightgray.svg'
                                    },
                                    {
                                        name: 'dashboard.dashboards',
                                        icon: 'dashboard',
                                        state: 'home.dashboards',
                                        link: '/static/svg/dashboardlightgray.svg'
                                    }
                                ]
                            }];

                } else if (authority === 'CUSTOMER_USER') {
                    sections = [
                        {
                            name: 'home.home',
                            type: 'link',
                            state: 'home.links',
                            icon: 'home'
                        },
                        {
                            name: 'Tempusboard',
                            type: 'link',
                            icon: 'dashboard',
                            state: 'home.tempusboard',
                            link: '/static/svg/tempusboardlightgray.svg'
                        },
                        {
                            name: 'device.devices',
                            type: 'link',
                            state: 'home.devices',
                            icon: 'devices_other',
                            link: '/static/svg/deviceslightgray.svg'
                        },
                        {
                            name: 'dashboard.dashboards',
                            type: 'link',
                            state: 'home.dashboards',
                            icon: 'dashboard',                            
                            link: '/static/svg/dashboardlightgray.svg'
                        }];

                    homeSections =
                        [{
                            name: 'asset.view-assets',
                            places: [
                                {
                                    name: 'asset.assets',
                                    icon: 'domain',
                                    state: 'home.assets',
                                    link: '/static/svg/assetslightgray.svg'
                                }
                            ]
                        },
                        {
                            name: 'device.view-devices',
                            places: [
                                {
                                    name: 'device.devices',
                                    icon: 'devices_other',
                                    state: 'home.devices',                                        
                                    link: '/static/svg/deviceslightgray.svg'
                                }
                            ]
                        },
                            {
                                name: 'dashboard.view-dashboards',
                                places: [
                                    {
                                        name: 'dashboard.dashboards',
                                        icon: 'dashboard',
                                        state: 'home.dashboards',                            
                                        link: '/static/svg/dashboardlightgray.svg'
                                    }
                                ]
                            },
                            {
                                name: 'tempusboard.view-tempusboard',
                                places: [
                                    {
                                        name: 'Tempusboard',
                                        icon: 'dashboard',
                                        state: 'home.tempusboard',
                                        link: '/static/svg/tempusboardlightgray.svg'
                                    }
                                ]
                            }
                            ];
                }
            }
        }
    }

    function sectionHeight(section) {
        if ($state.includes(section.state)) {
            return section.height;
        } else {
            return '0px';
        }
    }

    function sectionActive(section) {
        return $state.includes(section.state);
    }

}

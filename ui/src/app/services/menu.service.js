/*
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
import tempusApiUser from '../api/user.service';
import tempusApiDatamodel from '../api/datamodel.service';
import tempusApiCustomer from '../api/customer.service';

export default angular.module('tempus.menu', [tempusApiUser,tempusApiDatamodel,tempusApiCustomer])
    .factory('menu', Menu)
    .name;

/*@ngInject*/
function Menu(userService, $state, $rootScope, $log,datamodelService,customerService) {

    var authority = '';
    var sections = [];
    var homeSections = [];
    var generatedSectionTree = {};


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
        sectionActive: sectionActive,
        getGeneratedSectionTree: getGeneratedSectionTree
    };

    return service;

    function getSections() {
        return sections;
    }

    function getGeneratedSectionTree() {
        return generatedSectionTree;
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
                            name: 'cluster.cluster-info',
                            type: 'link',
                            state: 'home.nodes',
                            icon: 'now_widgets'
                        },

                        {
                            name: 'admin.system-settings',
                            type: 'toggle',
                            state: 'home.settings',
                            height: '120px',
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
                                    icon: 'mail'                                },

                                {
                                    name: 'admin.ui-configuration',
                                    type: 'link',
                                    state: 'home.settings.ui-config',
                                    icon: 'settings_applications'                                }

                            ]
                        }];

                    generatedSectionTree = {};

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
                            },
                            {
                                name: 'admin.ui-configuration',
                                places: [
                                    {
                                        name: 'admin.look-feel',
                                        icon: 'settings_applications',
                                        state: 'home.settings.ui-config'
                                    }
                                ]
                            },
                            {
                                name: 'cluster.management',
                                places: [
                                    {
                                        name: 'cluster.cluster-info',
                                        icon: 'now_widgets',
                                        state: 'home.nodes',
                                    }
                                ]
                            }


                        ];
                } else if (authority === 'TENANT_ADMIN') {
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
                            name: 'data_model.data_models',
                            type: 'link',
                            state: 'home.data_models',
                            icon: 'data_models',
                            link: '/static/svg/data-models-icon.svg'
                        },
                        {
                            name: 'metadata.metadata',
                            type: 'link',
                            state: 'home.metadata',
                            icon: 'metadata',
                            link: '/static/svg/metadata-icon.svg'
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
                        },
                        {
                            name: 'audit-log.audit-logs',
                            type: 'link',
                            state: 'home.auditLogs',
                            icon: 'track_changes'
                        }];

                    generatedSectionTree = {};

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
                                name: 'audit-log.audit',
                                places: [
                                    {
                                        name: 'audit-log.audit-logs',
                                        icon: 'track_changes',
                                        state: 'home.auditLogs'
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
                            },
                            {
                                name: 'metadata.metadata',
                                places: [
                                    {
                                        name: 'metadata.metadata',
                                        icon: 'metadata',
                                        link: '/static/svg/metadata-icon.svg',
                                        state: 'home.metadata'
                                    }
                                ]
                            }
                        ];

                } else if (authority === 'CUSTOMER_USER') {
                    sections = [
                        {
                            name: 'home.home',
                            type: 'link',
                            state: 'home.links',
                            icon: 'home'
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

                    var dataModelsOfAssetType = [];

                    var customer = customerService.getCustomer(user.customerId, {ignoreLoading: true});

                    customer.then(function (data) {
                        var dataModelObjects = datamodelService.getDatamodelObjects(data.dataModelId.id);

                        dataModelObjects.then(function(modelObjects){
                            $log.log(modelObjects);
                            dataModelsOfAssetType = getDataModelObjectsOfTypeAsset(modelObjects);
                            generatedSectionTree.children = buildGeneratedSectionTree(dataModelsOfAssetType);

                        }, function (error){
                            $log.error(error);
                        })
                    }, function (error) {
                        $log.error(error);
                    });

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
                            }
                        ];
                }
            }
        }
    }


    function getDataModelObjectsOfTypeAsset(dataModelObjects){

        var dataModels = [];

        angular.forEach(dataModelObjects, function (dataModelObject) {

            if (dataModelObject.type === "Asset") {

                var sec = {
                    id : dataModelObject.id.id,
                    type: 'link',
                    name:dataModelObject.name,
                    state: 'home.assets',
                    icon: 'domain',
                    link: '/static/svg/assetslightgray.svg',
                    logoFile: dataModelObject.logoFile
                };

                if(dataModelObject.parentId != null)
                    sec['parentId'] =  dataModelObject.parentId.id;
                else
                    sec['parentId'] = null;

                dataModels.push(sec);
            }
        });

        return dataModels;
    }


    function buildSectionTreeList(id, parentId, children, list) {
        if (!id) id = 'id';
        if (!parentId) parentId = 'parentId';
        if (!children) children = 'children';
        var treeList = [];
        var lookup = {};
        list.forEach(function (obj) {
            lookup[obj.id] = obj;
            obj[children] = [];

        });


        list.forEach(function (obj) {
            if (obj[parentId] != null) {
                lookup[obj[parentId]][children].push(obj);
            } else {
                treeList.push(obj);
            }
        });
        return treeList;
    }


    function addToggleAndLevelToGeneratedSectionTree(parent,level) {

        var children = parent.children;

        if (children.length === 0){
            parent.type = 'link';
        }else {
            parent.type = 'toggle';
        }

        for (var i = 0, len = children.length; i < len; i++) {
            addToggleAndLevelToGeneratedSectionTree(children[i],level+1);
        }

        parent.level = level;
    }

    function buildGeneratedSectionTree(list, id, parentId, children) {
        var generatedSectionTreeList = buildSectionTreeList(id, parentId, children, list);

        var startingLevel = 1;
        addToggleAndLevelToGeneratedSectionTree(generatedSectionTreeList[0],startingLevel);

        return generatedSectionTreeList;
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

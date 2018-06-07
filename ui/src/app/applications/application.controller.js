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

import addApplicationTemplate from './add-application.tpl.html';
//import applicationCard from './application-card.tpl.html';
import addRuleTemplate from '../rule/add-rule.tpl.html';
import ruleCard from '../rule/rule-card.tpl.html';

import addDashboardTemplate from '../dashboard/add-dashboard.tpl.html';

import addComputationTemplate from '../computations/add-computation.tpl.html';
import computationCard from '../computations/computation-card.tpl.html';
import addComputationJobTemplate from '../computations/add-computation-job.tpl.html';
import computationJobCard from '../computations/computation-job-card.tpl.html';


/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export function ApplicationCardController(types) {

    var vm = this;

    vm.types = types;

}

/*@ngInject*/
export function ApplicationController($timeout, $log, $rootScope, userService, applicationService, customerService, $state, $stateParams, $document, $mdDialog, $q, types, ruleService, importExport, $filter, dashboardService, $translate, $window, computationService, computationJobService) {

    var customerId = $stateParams.customerId;

    var applicationActionsList = [];

    var applicationGroupActionsList = [];

    var vm = this;

    vm.types = types;
    vm.isShowSidenav = true;

    vm.applicationGridConfig = {
        deleteItemTitleFunc: deleteApplicationTitle,
        deleteItemContentFunc: deleteApplicationText,
        deleteItemsTitleFunc: deleteApplicationsTitle,
        deleteItemsActionTitleFunc: deleteApplicationsActionTitle,
        deleteItemsContentFunc: deleteApplicationsText,

        saveItemFunc: saveApplication,

        getItemTitleFunc: getApplicationTitle,

       // itemCardController: 'ApplicationCardController',
       // itemCardTemplateUrl: applicationCard,
        parentCtl: vm,

        actionsList: applicationActionsList,
        groupActionsList: applicationGroupActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addApplicationTemplate,

        addItemText: function() { return $translate.instant('application.add-application-text') },
        noItemsText: function() { return $translate.instant('application.no-applications-text') },
        itemDetailsText: function() { return $translate.instant('application.application-details') },
        isDetailsReadOnly: isCustomerUser,
        isSelectionEnabled: function () {
            return !isCustomerUser();
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.applicationGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.applicationGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.applicationsScope = $state.$current.data.applicationsType;

    vm.assignToCustomer = assignToCustomer;
    vm.makePublic = makePublic;
    vm.unassignFromCustomer = unassignFromCustomer;
    vm.manageCredentials = manageCredentials;
    vm.currentApp = currentApp;
    vm.appClicked = false;
    vm.appSliderOpen = true;

    vm.showAppMini = false;
    vm.showAppMain = false;
    vm.showAppRules = false;
    vm.showComputations = false;
    vm.showAppDetails = true;
    vm.showComputationJobs = false;

    vm.tabSelectedIndex = 0;

    vm.tabselected = function (selectedTab){
        if(selectedTab == 'Mini'){
            vm.showAppMini = true;
            vm.showAppMain = false;
            vm.showAppRules = false;
            vm.showAppDetails = false;
            vm.showComputations = false;
            vm.showComputationJobs = false;
            vm.tabSelectedIndex = 1;
        }
        else if(selectedTab == 'Rules'){
            vm.showAppMini = false;
            vm.showAppMain = false;
            vm.showAppRules = true;
            vm.showAppDetails = false;
            vm.showComputations = false;
            vm.showComputationJobs = false;
            vm.tabSelectedIndex = 2;

        }
        else if(selectedTab == 'Main'){
            vm.showAppMini = false;
            vm.showAppMain = true;
            vm.showAppRules = false;
            vm.showAppDetails = false;
            vm.showComputations = false;
            vm.showComputationJobs = false;
            vm.tabSelectedIndex = 3;

        }
        else if(selectedTab == 'Details'){
            vm.currentAppforDirective = vm.currentApplication;
            vm.showAppMini = false;
            vm.showAppMain = false;
            vm.showAppRules = false;
            vm.showComputations = false;
            vm.showComputationJobs = false;
            vm.showAppDetails = true; 
            vm.tabSelectedIndex = 0;
            $window.localStorage.removeItem('currentTab');
        }  
        else if(selectedTab == 'Computation'){
            vm.showAppMini = false;
            vm.showAppMain = false;
            vm.showAppRules = false;
            vm.showAppDetails = false;
            vm.tabSelectedIndex = 4;
            vm.showComputations = true;
            vm.showComputationJobs = false;
        }
    }

    vm.detailesClicked = function() {
        vm.appSliderOpen = false;
        $timeout( function(){
            vm.showAppMini = false;
            vm.showAppMain = false;
            vm.showAppRules = false;
            vm.showComputations = false;
            vm.showComputationJobs = false;
            vm.showAppDetails = true; 
            vm.grid.detailsConfig.isDetailsOpen = true;
            vm.appSliderOpen = true;
            vm.tabSelectedIndex = 0;
            $timeout( function(){
                vm.grid.openItem(null, vm.currentApplication);
            }, 100 ); 
        }, 100 ); 
    }

    function currentApp(item) {
       vm.currentApplication = item; 
    }    

    $window.onbeforeunload = function() {
      $window.localStorage.removeItem('currentApp');
      $window.localStorage.removeItem('currentTab');
    };

    function activateApplication(event, application) {
        applicationService.activateApplication(application.id.id).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

    function suspendApplication(event, application) {
        applicationService.suspendApplication(application.id.id).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

    initController();

    function initController() {
        var fetchApplicationsFunction = null;
        var deleteApplicationFunction = null;
        var refreshApplicationsParamsFunction = null;

        var user = userService.getCurrentUser();

        if (user.authority === 'CUSTOMER_USER') {
            vm.applicationsScope = 'customer_user';
            customerId = user.customerId;
        }
        if (customerId) {
            vm.customerApplicationsTitle = $translate.instant('customer.applications');
            customerService.getShortCustomerInfo(customerId).then(
                function success(info) {
                    if (info.isPublic) {
                        vm.customerApplicationsTitle = $translate.instant('customer.public-applications');
                    }
                }
            );
        }

        if (vm.applicationsScope === 'tenant') {
            fetchApplicationsFunction = function (pageLink, applicationType) {
                return applicationService.getTenantApplications(pageLink, true, null, applicationType);
            };
            deleteApplicationFunction = function (applicationId) {
                return applicationService.deleteApplication(applicationId);
            };
            refreshApplicationsParamsFunction = function() {
                return {"topIndex": vm.topIndex};
            };

            // applicationActionsList.push({
            //     onAction: function ($event, item) {
            //         makePublic($event, item);
            //     },
            //     name: function() { return $translate.instant('action.share') },
            //     details: function() { return $translate.instant('application.make-public') },
            //     icon: "share",
            //     isEnabled: function(application) {
            //         return application && (!application.customerId || application.customerId.id === types.id.nullUid);
            //     }
            // });

            // applicationActionsList.push(
            //     {
            //         onAction: function ($event, item) {
            //             assignToCustomer($event, [ item.id.id ]);
            //         },
            //         name: function() { return $translate.instant('action.assign') },
            //         details: function() { return $translate.instant('application.assign-to-customer') },
            //         icon: "assignment_ind",
            //         isEnabled: function(application) {
            //             return application && (!application.customerId || application.customerId.id === types.id.nullUid);
            //         }
            //     }
            // );

            // applicationActionsList.push(
            //     {
            //         onAction: function ($event, item) {
            //             unassignFromCustomer($event, item, false);
            //         },
            //         name: function() { return $translate.instant('action.unassign') },
            //         details: function() { return $translate.instant('application.unassign-from-customer') },
            //         icon: "assignment_return",
            //         isEnabled: function(application) {
            //             return application && application.customerId && application.customerId.id !== types.id.nullUid && !application.assignedCustomer.isPublic;
            //         }
            //     }
            // );

            // applicationActionsList.push({
            //     onAction: function ($event, item) {
            //         unassignFromCustomer($event, item, true);
            //     },
            //     name: function() { return $translate.instant('action.make-private') },
            //     details: function() { return $translate.instant('application.make-private') },
            //     icon: "reply",
            //     isEnabled: function(application) {
            //         return application && application.customerId && application.customerId.id !== types.id.nullUid && application.assignedCustomer.isPublic;
            //     }
            // });

            // applicationActionsList.push(
            //     {
            //         onAction: function ($event, item) {
            //             manageCredentials($event, item);
            //         },
            //         name: function() { return $translate.instant('application.credentials') },
            //         details: function() { return $translate.instant('application.manage-credentials') },
            //         icon: "security"
            //     }
            // );
            applicationActionsList.push(
                {
                    onAction: function ($event, item) {
                        activateApplication($event, item);
                    },
                    name: function() { return $translate.instant('action.activate') },
                    details: function() { return $translate.instant('application.activate') },
                    icon: "play_arrow",
                    isEnabled: function(application) {
                        return isApplicationEditable(application) && application && application.state === 'SUSPENDED';
                    }
                });
            applicationActionsList.push(
                {
                    onAction: function ($event, item) {
                        suspendApplication($event, item);
                    },
                    name: function() { return $translate.instant('action.suspend') },
                    details: function() { return $translate.instant('application.suspend') },
                    icon: "pause",
                    isEnabled: function(application) {
                        return isApplicationEditable(application) && application.state === 'ACTIVE';
                    }
            }),

            applicationActionsList.push(
                {
                    onAction: function ($event, item) {
                        vm.grid.deleteItem($event, item);
                    },
                    name: function() { return $translate.instant('action.delete') },
                    details: function() { return $translate.instant('application.delete') },
                    icon: "delete"
                }
            );

            applicationGroupActionsList.push(
                {
                    onAction: function ($event, items) {
                        assignApplicationsToCustomer($event, items);
                    },
                    name: function() { return $translate.instant('application.assign-applications') },
                    details: function(selectedCount) {
                        return $translate.instant('application.assign-applications-text', {count: selectedCount}, "messageformat");
                    },
                    icon: "assignment_ind"
                }
            );

            applicationGroupActionsList.push(
                {
                    onAction: function ($event) {
                        vm.grid.deleteItems($event);
                    },
                    name: function() { return $translate.instant('application.delete-applications') },
                    details: deleteApplicationsActionTitle,
                    icon: "delete"
                }
            );



        } else if (vm.applicationsScope === 'customer' || vm.applicationsScope === 'customer_user') {
            fetchApplicationsFunction = function (pageLink, applicationType) {
                return applicationService.getCustomerApplications(customerId, pageLink, true, null, applicationType);
            };
            deleteApplicationFunction = function (applicationId) {
                return applicationService.unassignApplicationFromCustomer(applicationId);
            };
            refreshApplicationsParamsFunction = function () {
                return {"customerId": customerId, "topIndex": vm.topIndex};
            };

            if (vm.applicationsScope === 'customer') {
                applicationActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, false);
                        },
                        name: function() { return $translate.instant('action.unassign') },
                        details: function() { return $translate.instant('application.unassign-from-customer') },
                        icon: "assignment_return",
                        isEnabled: function(application) {
                            return application && !application.assignedCustomer.isPublic;
                        }
                    }
                );
                applicationActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, true);
                        },
                        name: function() { return $translate.instant('action.make-private') },
                        details: function() { return $translate.instant('application.make-private') },
                        icon: "reply",
                        isEnabled: function(application) {
                            return application && application.assignedCustomer.isPublic;
                        }
                    }
                );

                applicationActionsList.push(
                    {
                        onAction: function ($event, item) {
                            manageCredentials($event, item);
                        },
                        name: function() { return $translate.instant('application.credentials') },
                        details: function() { return $translate.instant('application.manage-credentials') },
                        icon: "security"
                    }
                );

                applicationGroupActionsList.push(
                    {
                        onAction: function ($event, items) {
                            unassignApplicationsFromCustomer($event, items);
                        },
                        name: function() { return $translate.instant('application.unassign-applications') },
                        details: function(selectedCount) {
                            return $translate.instant('application.unassign-applications-action-title', {count: selectedCount}, "messageformat");
                        },
                        icon: "assignment_return"
                    }
                );

                vm.applicationGridConfig.addItemAction = {
                    onAction: function ($event) {
                        addApplicationsToCustomer($event);
                    },
                    name: function() { return $translate.instant('application.assign-applications') },
                    details: function() { return $translate.instant('application.assign-new-application') },
                    icon: "add"
                };


            } else if (vm.applicationsScope === 'customer_user') {
                applicationActionsList.push(
                    {
                        onAction: function ($event, item) {
                            manageCredentials($event, item);
                        },
                        name: function() { return $translate.instant('application.credentials') },
                        details: function() { return $translate.instant('application.view-credentials') },
                        icon: "security"
                    }
                );

                vm.applicationGridConfig.addItemAction = {};
            }
        }

        vm.applicationGridConfig.refreshParamsFunc = refreshApplicationsParamsFunction;
        vm.applicationGridConfig.fetchItemsFunc = fetchApplicationsFunction;
        vm.applicationGridConfig.deleteItemFunc = deleteApplicationFunction;
    }

    function deleteApplicationTitle(application) {
        return $translate.instant('application.delete-application-title', {applicationTitle: application.name});
    }

    function deleteApplicationText() {
        return $translate.instant('application.delete-application-text');
    }

    function deleteApplicationsTitle(selectedCount) {
        return $translate.instant('application.delete-applications-title', {count: selectedCount}, 'messageformat');
    }

    function deleteApplicationsActionTitle(selectedCount) {
        return $translate.instant('application.delete-applications-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteApplicationsText () {
        return $translate.instant('application.delete-applications-text');
    }

    function getApplicationTitle(application) {
        return application ? application.name : '';
    }
    function isApplicationEditable(application) {
        if (userService.getAuthority() === 'TENANT_ADMIN') {
            return application && application.tenantId.id != types.id.nullUid;
        } else {
            return userService.getAuthority() === 'SYS_ADMIN';
        }
    }

    function saveApplication(application) {
        var deferred = $q.defer();
        application.deviceTypes.deviceTypes.forEach(function(deviceType){
            deviceType.name = deviceType.name.toLowerCase();
        })
        applicationService.saveApplication(application).then(
            function success(savedApplication) {
                $rootScope.$broadcast('applicationSaved');
                var applications = [ savedApplication ];
                customerService.applyAssignedCustomersInfo(applications).then(
                    function success(items) {
                        if (items && items.length == 1) {
                            deferred.resolve(items[0]);
                        } else {
                            deferred.reject();
                        }
                    },
                    function fail() {
                        deferred.reject();
                    }
                );
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function isCustomerUser() {
        return vm.applicationsScope === 'customer_user';
    }

    function assignToCustomer($event, applicationIds) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        customerService.getCustomers({limit: pageSize, textSearch: ''}).then(
            function success(_customers) {
                var customers = {
                    pageSize: pageSize,
                    data: _customers.data,
                    nextPageLink: _customers.nextPageLink,
                    selection: null,
                    hasNext: _customers.hasNext,
                    pending: false
                };
                if (customers.hasNext) {
                    customers.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AssignApplicationToCustomerController',
                    controllerAs: 'vm',
                //    templateUrl: assignToCustomerTemplate,
                    locals: {applicationIds: applicationIds, customers: customers},
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: $event
                }).then(function () {
                    vm.grid.refreshList();
                }, function () {
                });
            },
            function fail() {
            });
    }

    function addApplicationsToCustomer($event) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        applicationService.getTenantApplications({limit: pageSize, textSearch: ''}, false).then(
            function success(_applications) {
                var applications = {
                    pageSize: pageSize,
                    data: _applications.data,
                    nextPageLink: _applications.nextPageLink,
                    selections: {},
                    selectedCount: 0,
                    hasNext: _applications.hasNext,
                    pending: false
                };
                if (applications.hasNext) {
                    applications.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AddApplicationsToCustomerController',
                    controllerAs: 'vm',
                //    templateUrl: addApplicationsToCustomerTemplate,
                    locals: {customerId: customerId, applications: applications},
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: $event
                }).then(function () {
                    vm.grid.refreshList();
                }, function () {
                });
            },
            function fail() {
            });
    }

    function assignApplicationsToCustomer($event, items) {
        var applicationIds = [];
        for (var id in items.selections) {
            applicationIds.push(id);
        }
        assignToCustomer($event, applicationIds);
    }

    function unassignFromCustomer($event, application, isPublic) {
        if ($event) {
            $event.stopPropagation();
        }
        var title;
        var content;
        var label;
        if (isPublic) {
            title = $translate.instant('application.make-private-application-title', {applicationName: application.name});
            content = $translate.instant('application.make-private-application-text');
            label = $translate.instant('application.make-private');
        } else {
            title = $translate.instant('application.unassign-application-title', {applicationName: application.name});
            content = $translate.instant('application.unassign-application-text');
            label = $translate.instant('application.unassign-application');
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .ariaLabel(label)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            applicationService.unassignApplicationFromCustomer(application.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }

    function unassignApplicationsFromCustomer($event, items) {
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('application.unassign-applications-title', {count: items.selectedCount}, 'messageformat'))
            .htmlContent($translate.instant('application.unassign-applications-text'))
            .ariaLabel($translate.instant('application.unassign-application'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            var tasks = [];
            for (var id in items.selections) {
                tasks.push(applicationService.unassignApplicationFromCustomer(id));
            }
            $q.all(tasks).then(function () {
                vm.grid.refreshList();
            });
        });
    }

    function makePublic($event, application) {
        if ($event) {
            $event.stopPropagation();
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('application.make-public-application-title', {applicationName: application.name}))
            .htmlContent($translate.instant('application.make-public-application-text'))
            .ariaLabel($translate.instant('application.make-public'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            applicationService.makeApplicationPublic(application.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }

    function manageCredentials($event, application) {
        if ($event) {
            $event.stopPropagation();
        }
        $mdDialog.show({
            controller: 'ManageApplicationCredentialsController',
            controllerAs: 'vm',
          //  templateUrl: applicationCredentialsTemplate,
            locals: {applicationId: application.id.id, isReadOnly: isCustomerUser()},
            parent: angular.element($document[0].body),
            fullscreen: true,
            targetEvent: $event
        }).then(function () {
        }, function () {
        });
    }

    //---------------------
    var ruleActionsList = [
        {
            onAction: function ($event, item) {
                exportRule($event, item);
            },
            name: function() { $translate.instant('action.export') },
            details: function() { return $translate.instant('rule.export') },
            icon: "file_download"
        },
        {
            onAction: function ($event, item) {
                activateRule($event, item);
            },
            name: function() { return $translate.instant('action.activate') },
            details: function() { return $translate.instant('rule.activate') },
            icon: "play_arrow",
            isEnabled: function(rule) {
                return isRuleEditable(rule) && rule && rule.state === 'SUSPENDED';
            }
        },
        {
            onAction: function ($event, item) {
                suspendRule($event, item);
            },
            name: function() { return $translate.instant('action.suspend') },
            details: function() { return $translate.instant('rule.suspend') },
            icon: "pause",
            isEnabled: function(rule) {
                return isRuleEditable(rule) && rule.state === 'ACTIVE';
            }
        },
        {
            onAction: function ($event, item) {
                vm.grid.deleteItem($event, item);
            },
            name: function() { return $translate.instant('action.delete') },
            details: function() { return $translate.instant('rule.delete') },
            icon: "delete",
            isEnabled: isRuleEditable
        }
    ];

    var ruleAddItemActionsList = [
        {
            onAction: function ($event) {
                vm.grid.addItem($event);
            },
            name: function() { return $translate.instant('action.create') },
            details: function() { return $translate.instant('rule.create-new-rule') },
            icon: "insert_drive_file"
        },
        {
            onAction: function ($event) {
                importExport.importRule($event).then(
                    function() {
                        vm.grid.refreshList();
                    }
                );
            },
            name: function() { return $translate.instant('action.import') },
            details: function() { return $translate.instant('rule.import') },
            icon: "file_upload"
        }
    ];


    vm.ruleGridConfig = {

        refreshParamsFunc: null,

        deleteItemTitleFunc: deleteRuleTitle,
        deleteItemContentFunc: deleteRuleText,
        deleteItemsTitleFunc: deleteRulesTitle,
        deleteItemsActionTitleFunc: deleteRulesActionTitle,
        deleteItemsContentFunc: deleteRulesText,

        fetchItemsFunc: fetchRules,
        saveItemFunc: saveRule,
        deleteItemFunc: deleteRule,

        getItemTitleFunc: getRuleTitle,
        itemCardTemplateUrl: ruleCard,
        parentCtl: vm,

        actionsList: ruleActionsList,
        addItemActions: ruleAddItemActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addRuleTemplate,

        addItemText: function() { return $translate.instant('rule.add-rule-text') },
        noItemsText: function() { return $translate.instant('rule.no-rules-text') },
        itemDetailsText: function() { return $translate.instant('rule.rule-details') },
        isSelectionEnabled: isRuleEditable,
        isDetailsReadOnly: function(rule) {
            return !isRuleEditable(rule);
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.ruleGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.ruleGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.isRuleEditable = isRuleEditable;

    vm.activateRule = activateRule;
    vm.suspendRule = suspendRule;
    vm.exportRule = exportRule;

    function deleteRuleTitle(rule) {
        return $translate.instant('rule.delete-rule-title', {ruleName: rule.name});
    }

    function deleteRuleText() {
        return $translate.instant('rule.delete-rule-text');
    }

    function deleteRulesTitle(selectedCount) {
        return $translate.instant('rule.delete-rules-title', {count: selectedCount}, 'messageformat');
    }

    function deleteRulesActionTitle(selectedCount) {
        return $translate.instant('rule.delete-rules-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteRulesText() {
        return $translate.instant('rule.delete-rules-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function fetchRules(pageLink) {
        return ruleService.getAllRules(pageLink);
    }

    function saveRule(rule) {
        return ruleService.saveRule(rule).then(
            function(savedRule) {
                var rules = {"applicationId": vm.currentApplication.id.id, "fields":[savedRule.id.id]};
                applicationService.assignRulesToApplication(rules).then(
                    function success(application) {
                        $window.localStorage.setItem('currentApp', angular.toJson(application));
                        $window.localStorage.setItem('currentTab', angular.toJson(vm.tabSelectedIndex));
                        vm.currentApplication = application;
                    },
                    function fail() {
                        $rootScope.$broadcast('Rule couldn\'t be assigned to the application');                    
                    }
                );
           }
        );
    }

    function deleteRule(ruleId) {
        return ruleService.deleteRule(ruleId).then(
            function() {
                var rules = {"applicationId": vm.currentApplication.id.id, "fields":[ruleId]};
                applicationService.unAssignRulesFromApplication(rules).then(
                    function success(application) {
                        $window.localStorage.setItem('currentApp', angular.toJson(application));
                        $window.localStorage.setItem('currentTab', angular.toJson(vm.tabSelectedIndex));
                        vm.currentApplication = application;
                    },
                    function fail() {
                        $rootScope.$broadcast('Rule couldn\'t be unassigned from the application');
                    }
                );
            }
        );
    }

    function getRuleTitle(rule) {
        return rule ? rule.name : '';
    }

    function isRuleEditable(rule) {
        if (userService.getAuthority() === 'TENANT_ADMIN') {
            return rule && rule.tenantId.id != types.id.nullUid;
        } else {
            return userService.getAuthority() === 'SYS_ADMIN';
        }
    }

    function exportRule($event, rule) {
        $event.stopPropagation();
        importExport.exportRule(rule.id.id);
    }

    function activateRule(event, rule) {
        ruleService.activateRule(rule.id.id).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

    function suspendRule(event, rule) {
        ruleService.suspendRule(rule.id.id).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

    //--------End of Rules

    var dashboardActionsList = [
        {
            onAction: function ($event, item) {
                vm.grid.openItem($event, item);
            },
            name: function() { return $translate.instant('dashboard.details') },
            details: function() { return $translate.instant('dashboard.dashboard-details') },
            icon: "edit"
        }
    ];

    var dashboardGroupActionsList = [];

    vm.dashboardGridConfig = {
        deleteItemTitleFunc: deleteDashboardTitle,
        deleteItemContentFunc: deleteDashboardText,
        deleteItemsTitleFunc: deleteDashboardsTitle,
        deleteItemsActionTitleFunc: deleteDashboardsActionTitle,
        deleteItemsContentFunc: deleteDashboardsText,

        loadItemDetailsFunc: loadDashboard,

        saveItemFunc: saveDashboard,

        clickItemFunc: openDashboard,

        getItemTitleFunc: getDashboardTitle,
     //   itemCardController: 'DashboardCardController',
     //   itemCardTemplateUrl: dashboardCard,
        parentCtl: vm,

        actionsList: dashboardActionsList,
        groupActionsList: dashboardGroupActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addDashboardTemplate,

        addItemText: function() { return $translate.instant('dashboard.add-dashboard-text') },
        noItemsText: function() { return $translate.instant('dashboard.no-dashboards-text') },
        itemDetailsText: function() { return $translate.instant('dashboard.dashboard-details') },
        isDetailsReadOnly: function () {
            return vm.dashboardsScope === 'customer_user';
        },
        isSelectionEnabled: function () {
            return !(vm.dashboardsScope === 'customer_user');
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.dashboardGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.dashboardGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.dashboardsScope = $state.$current.data.dashboardsType;

    //vm.assignToCustomer = assignToCustomer;
    //vm.makePublic = makePublic;
    //vm.unassignFromCustomer = unassignFromCustomer;
    vm.exportDashboard = exportDashboard;

    initDashController();

    function initDashController() {
        var fetchDashboardsFunction = null;
        var deleteDashboardFunction = null;
        var refreshDashboardsParamsFunction = null;

        if (customerId) {
            vm.customerDashboardsTitle = $translate.instant('customer.dashboards');
            customerService.getShortCustomerInfo(customerId).then(
                function success(info) {
                    if (info.isPublic) {
                        vm.customerDashboardsTitle = $translate.instant('customer.public-dashboards');
                    }
                }
            );
        }

        if (vm.dashboardsScope === 'tenant') {
            fetchDashboardsFunction = function (pageLink) {
                return dashboardService.getTenantDashboards(pageLink);
            };
            deleteDashboardFunction = function (dashboardId) {
                return dashboardService.deleteDashboard(dashboardId).then(
            function() {
                $window.localStorage.setItem('currentApp', angular.toJson(vm.currentApplication));
                $window.localStorage.setItem('currentTab', angular.toJson(vm.tabSelectedIndex));
           }
        );
            };
            refreshDashboardsParamsFunction = function () {
                return {"topIndex": vm.topIndex};
            };

            dashboardActionsList.push(
                {
                    onAction: function ($event, item) {
                        exportDashboard($event, item);
                    },
                    name: function() { $translate.instant('action.export') },
                    details: function() { return $translate.instant('dashboard.export') },
                    icon: "file_download"
                });

            // dashboardActionsList.push({
            //         onAction: function ($event, item) {
            //             makePublic($event, item);
            //         },
            //         name: function() { return $translate.instant('action.share') },
            //         details: function() { return $translate.instant('dashboard.make-public') },
            //         icon: "share",
            //         isEnabled: function(dashboard) {
            //             return dashboard && (!dashboard.customerId || dashboard.customerId.id === types.id.nullUid);
            //         }
            //     });

            // dashboardActionsList.push({
            //         onAction: function ($event, item) {
            //             assignToCustomer($event, [ item.id.id ]);
            //         },
            //         name: function() { return $translate.instant('action.assign') },
            //         details: function() { return $translate.instant('dashboard.assign-to-customer') },
            //         icon: "assignment_ind",
            //         isEnabled: function(dashboard) {
            //             return dashboard && (!dashboard.customerId || dashboard.customerId.id === types.id.nullUid);
            //         }
            //     });
            // dashboardActionsList.push({
            //         onAction: function ($event, item) {
            //             unassignFromCustomer($event, item, false);
            //         },
            //         name: function() { return $translate.instant('action.unassign') },
            //         details: function() { return $translate.instant('dashboard.unassign-from-customer') },
            //         icon: "assignment_return",
            //         isEnabled: function(dashboard) {
            //             return dashboard && dashboard.customerId && dashboard.customerId.id !== types.id.nullUid && !dashboard.assignedCustomer.isPublic;
            //         }
            //     });
            // dashboardActionsList.push({
            //         onAction: function ($event, item) {
            //             unassignFromCustomer($event, item, true);
            //         },
            //         name: function() { return $translate.instant('action.make-private') },
            //         details: function() { return $translate.instant('dashboard.make-private') },
            //         icon: "reply",
            //         isEnabled: function(dashboard) {
            //             return dashboard && dashboard.customerId && dashboard.customerId.id !== types.id.nullUid && dashboard.assignedCustomer.isPublic;
            //         }
            //     });

            dashboardActionsList.push(
                {
                    onAction: function ($event, item) {
                        vm.grid.deleteItem($event, item);
                    },
                    name: function() { return $translate.instant('action.delete') },
                    details: function() { return $translate.instant('dashboard.delete') },
                    icon: "delete"
                }
            );

            // dashboardGroupActionsList.push(
            //         {
            //             onAction: function ($event, items) {
            //                 assignDashboardsToCustomer($event, items);
            //             },
            //             name: function() { return $translate.instant('dashboard.assign-dashboards') },
            //             details: function(selectedCount) {
            //                 return $translate.instant('dashboard.assign-dashboards-text', {count: selectedCount}, "messageformat");
            //             },
            //             icon: "assignment_ind"
            //         }
            // );

            dashboardGroupActionsList.push(
                {
                    onAction: function ($event) {
                        vm.grid.deleteItems($event);
                    },
                    name: function() { return $translate.instant('dashboard.delete-dashboards') },
                    details: deleteDashboardsActionTitle,
                    icon: "delete"
                }
            );

            vm.dashboardGridConfig.addItemActions = [];
            vm.dashboardGridConfig.addItemActions.push({
                onAction: function ($event) {
                    vm.grid.addItem($event);
                },
                name: function() { return $translate.instant('action.create') },
                details: function() { return $translate.instant('dashboard.create-new-dashboard') },
                icon: "insert_drive_file"
            });
            vm.dashboardGridConfig.addItemActions.push({
                onAction: function ($event) {
                    importExport.importDashboard($event).then(
                        function() {
                            vm.grid.refreshList();
                        }
                    );
                },
                name: function() { return $translate.instant('action.import') },
                details: function() { return $translate.instant('dashboard.import') },
                icon: "file_upload"
            });
        } else if (vm.dashboardsScope === 'customer' || vm.dashboardsScope === 'customer_user') {
            fetchDashboardsFunction = function (pageLink) {
                return dashboardService.getCustomerDashboards(customerId, pageLink);
            };
            deleteDashboardFunction = function (dashboardId) {
                return dashboardService.unassignDashboardFromCustomer(dashboardId);
            };
            refreshDashboardsParamsFunction = function () {
                return {"customerId": customerId, "topIndex": vm.topIndex};
            };

            if (vm.dashboardsScope === 'customer') {
                dashboardActionsList.push(
                    {
                        onAction: function ($event, item) {
                            exportDashboard($event, item);
                        },
                        name: function() { $translate.instant('action.export') },
                        details: function() { return $translate.instant('dashboard.export') },
                        icon: "file_download"
                    }
                );

        //         dashboardActionsList.push(
        //             {
        //                 onAction: function ($event, item) {
        //                     unassignFromCustomer($event, item, false);
        //                 },
        //                 name: function() { return $translate.instant('action.unassign') },
        //                 details: function() { return $translate.instant('dashboard.unassign-from-customer') },
        //                 icon: "assignment_return",
        //                 isEnabled: function(dashboard) {
        //                     return dashboard && !dashboard.assignedCustomer.isPublic;
        //                 }
        //             }
        //         );
        //         dashboardActionsList.push(
        //             {
        //                 onAction: function ($event, item) {
        //                     unassignFromCustomer($event, item, true);
        //                 },
        //                 name: function() { return $translate.instant('action.make-private') },
        //                 details: function() { return $translate.instant('dashboard.make-private') },
        //                 icon: "reply",
        //                 isEnabled: function(dashboard) {
        //                     return dashboard && dashboard.assignedCustomer.isPublic;
        //                 }
        //             }
        //         );

        //         dashboardGroupActionsList.push(
        //             {
        //                 onAction: function ($event, items) {
        //                     unassignDashboardsFromCustomer($event, items);
        //                 },
        //                 name: function() { return $translate.instant('dashboard.unassign-dashboards') },
        //                 details: function(selectedCount) {
        //                     return $translate.instant('dashboard.unassign-dashboards-action-title', {count: selectedCount}, "messageformat");
        //                 },
        //                 icon: "assignment_return"
        //             }
        //         );


                // vm.dashboardGridConfig.addItemAction = {
                //     onAction: function ($event) {
                //         addDashboardsToCustomer($event);
                //     },
                //     name: function() { return $translate.instant('dashboard.assign-dashboards') },
                //     details: function() { return $translate.instant('dashboard.assign-new-dashboard') },
                //     icon: "add"
                // };
            } else if (vm.dashboardsScope === 'customer_user') {
                vm.dashboardGridConfig.addItemAction = {};
            }
        }

        vm.dashboardGridConfig.refreshParamsFunc = refreshDashboardsParamsFunction;
        vm.dashboardGridConfig.fetchItemsFunc = fetchDashboardsFunction;
        vm.dashboardGridConfig.deleteItemFunc = deleteDashboardFunction;

    }

    function deleteDashboardTitle (dashboard) {
        return $translate.instant('dashboard.delete-dashboard-title', {dashboardTitle: dashboard.title});
    }

    function deleteDashboardText () {
        return $translate.instant('dashboard.delete-dashboard-text');
    }

    function deleteDashboardsTitle (selectedCount) {
        return $translate.instant('dashboard.delete-dashboards-title', {count: selectedCount}, 'messageformat');
    }

    function deleteDashboardsActionTitle(selectedCount) {
        return $translate.instant('dashboard.delete-dashboards-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteDashboardsText () {
        return $translate.instant('dashboard.delete-dashboards-text');
    }

    // function gridInited(grid) {
    //     vm.grid = grid;
    // }

    function getDashboardTitle(dashboard) {
        return dashboard ? dashboard.title : '';
    }

    function loadDashboard(dashboard) {
        return dashboardService.getDashboard(dashboard.id.id);
    }

    function saveDashboard(dashboard) {
        var deferred = $q.defer();
        dashboardService.saveDashboard(dashboard).then(
            function success(savedDashboard) {
                var dashboards = [ savedDashboard ];
                if(vm.showAppMini){
                    applicationService.assignMiniDashboardToApplication(vm.currentApplication.id.id, savedDashboard.id.id).then(
                        function success(application) {
                            $window.localStorage.setItem('currentApp', angular.toJson(application));
                            $window.localStorage.setItem('currentTab', angular.toJson(vm.tabSelectedIndex));
                            vm.currentApplication = application;
                        },
                        function fail() {
                            deferred.reject();
                        }
                    );
                }
                else if(vm.showAppMain) {
                    applicationService.assignDashboardToApplication(vm.currentApplication.id.id, savedDashboard.id.id).then(
                        function success(application) {
                            $window.localStorage.setItem('currentApp', angular.toJson(application));
                            $window.localStorage.setItem('currentTab', angular.toJson(vm.tabSelectedIndex));
                            vm.currentApplication = application;
                        },
                        function fail() {
                            deferred.reject();
                        }
                    );
                }
               
                customerService.applyAssignedCustomersInfo(dashboards).then(
                    function success(items) {
                        if (items && items.length == 1) {
                            deferred.resolve(items[0]);
                        } else {
                            deferred.reject();
                        }
                    },
                    function fail() {
                        deferred.reject();
                    }
                );
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    // function assignToCustomer($event, dashboardIds) {
    //     if ($event) {
    //         $event.stopPropagation();
    //     }
    //     var pageSize = 10;
    //     customerService.getCustomers({limit: pageSize, textSearch: ''}).then(
    //         function success(_customers) {
    //             var customers = {
    //                 pageSize: pageSize,
    //                 data: _customers.data,
    //                 nextPageLink: _customers.nextPageLink,
    //                 selection: null,
    //                 hasNext: _customers.hasNext,
    //                 pending: false
    //             };
    //             if (customers.hasNext) {
    //                 customers.nextPageLink.limit = pageSize;
    //             }
    //             $mdDialog.show({
    //                 controller: 'AssignDashboardToCustomerController',
    //                 controllerAs: 'vm',
    //                 templateUrl: assignToCustomerTemplate,
    //                 locals: {dashboardIds: dashboardIds, customers: customers},
    //                 parent: angular.element($document[0].body),
    //                 fullscreen: true,
    //                 targetEvent: $event
    //             }).then(function () {
    //                 vm.grid.refreshList();
    //             }, function () {
    //             });
    //         },
    //         function fail() {
    //         });
    // }

    // function addDashboardsToCustomer($event) {
    //     if ($event) {
    //         $event.stopPropagation();
    //     }
    //     var pageSize = 10;
    //     dashboardService.getTenantDashboards({limit: pageSize, textSearch: ''}).then(
    //         function success(_dashboards) {
    //             var dashboards = {
    //                 pageSize: pageSize,
    //                 data: _dashboards.data,
    //                 nextPageLink: _dashboards.nextPageLink,
    //                 selections: {},
    //                 selectedCount: 0,
    //                 hasNext: _dashboards.hasNext,
    //                 pending: false
    //             };
    //             if (dashboards.hasNext) {
    //                 dashboards.nextPageLink.limit = pageSize;
    //             }
    //             $mdDialog.show({
    //                 controller: 'AddDashboardsToCustomerController',
    //                 controllerAs: 'vm',
    //                 templateUrl: addDashboardsToCustomerTemplate,
    //                 locals: {customerId: customerId, dashboards: dashboards},
    //                 parent: angular.element($document[0].body),
    //                 fullscreen: true,
    //                 targetEvent: $event
    //             }).then(function () {
    //                 vm.grid.refreshList();
    //             }, function () {
    //             });
    //         },
    //         function fail() {
    //         });
    // }

    // function assignDashboardsToCustomer($event, items) {
    //     var dashboardIds = [];
    //     for (var id in items.selections) {
    //         dashboardIds.push(id);
    //     }
    //     assignToCustomer($event, dashboardIds);
    // }

    // function unassignFromCustomer($event, dashboard, isPublic) {
    //     if ($event) {
    //         $event.stopPropagation();
    //     }
    //     var title;
    //     var content;
    //     var label;
    //     if (isPublic) {
    //         title = $translate.instant('dashboard.make-private-dashboard-title', {dashboardTitle: dashboard.title});
    //         content = $translate.instant('dashboard.make-private-dashboard-text');
    //         label = $translate.instant('dashboard.make-private-dashboard');
    //     } else {
    //         title = $translate.instant('dashboard.unassign-dashboard-title', {dashboardTitle: dashboard.title});
    //         content = $translate.instant('dashboard.unassign-dashboard-text');
    //         label = $translate.instant('dashboard.unassign-dashboard');
    //     }
    //     var confirm = $mdDialog.confirm()
    //         .targetEvent($event)
    //         .title(title)
    //         .htmlContent(content)
    //         .ariaLabel(label)
    //         .cancel($translate.instant('action.no'))
    //         .ok($translate.instant('action.yes'));
    //     $mdDialog.show(confirm).then(function () {
    //         dashboardService.unassignDashboardFromCustomer(dashboard.id.id).then(function success() {
    //             vm.grid.refreshList();
    //         });
    //     });
    // }

    // function makePublic($event, dashboard) {
    //     if ($event) {
    //         $event.stopPropagation();
    //     }
    //     dashboardService.makeDashboardPublic(dashboard.id.id).then(function success(dashboard) {
    //         $mdDialog.show({
    //             controller: 'MakeDashboardPublicDialogController',
    //             controllerAs: 'vm',
    //             templateUrl: makeDashboardPublicDialogTemplate,
    //             locals: {dashboard: dashboard},
    //             parent: angular.element($document[0].body),
    //             fullscreen: true,
    //             targetEvent: $event
    //         }).then(function () {
    //             vm.grid.refreshList();
    //         });
    //     });
    // }

    function exportDashboard($event, dashboard) {
        $event.stopPropagation();
        importExport.exportDashboard(dashboard.id.id);
    }

    // function unassignDashboardsFromCustomer($event, items) {
    //     var confirm = $mdDialog.confirm()
    //         .targetEvent($event)
    //         .title($translate.instant('dashboard.unassign-dashboards-title', {count: items.selectedCount}, 'messageformat'))
    //         .htmlContent($translate.instant('dashboard.unassign-dashboards-text'))
    //         .ariaLabel($translate.instant('dashboard.unassign-dashboards'))
    //         .cancel($translate.instant('action.no'))
    //         .ok($translate.instant('action.yes'));
    //     $mdDialog.show(confirm).then(function () {
    //         var tasks = [];
    //         for (var id in items.selections) {
    //             tasks.push(dashboardService.unassignDashboardFromCustomer(id));
    //         }
    //         $q.all(tasks).then(function () {
    //             vm.grid.refreshList();
    //         });
    //     });
    // }

    function openDashboard($event, dashboard) {
        if ($event) {
            $event.stopPropagation();
        }
        if (vm.dashboardsScope === 'customer') {
            $state.go('home.customers.dashboards.dashboard', {
                customerId: customerId,
                dashboardId: dashboard.id.id
            });
        } else {
            // $state.go('home.dashboards.dashboard', {dashboardId: dashboard.id.id});
            var url = $state.href('home.dashboards.dashboard', {dashboardId: dashboard.id.id});
            $window.open(url,'_blank');
        }
    }
// End of Dashboard
    
    var computationActionsList = [
        {
            onAction: function ($event, item) {
                vm.grid.deleteItem($event, item);
            },
            name: function() { return $translate.instant('action.delete') },
            details: function() { return $translate.instant('computation.delete') },
            icon: "delete",
        }
    ];

    var computationAddItemActionsList = [
        {
            onAction: function ($event) {
                $window.localStorage.setItem('currentApp', angular.toJson(vm.currentApplication));
                importExport.importComputation($event).then(
                    function() {
                        vm.grid.refreshList();
                    }
                );
            },
            name: function() { return $translate.instant('action.import') },
            details: function() { return $translate.instant('computation.import') },
            icon: "file_upload"
        }
    ];

    vm.showComputations = true;
    vm.showComputationJobs = false;

    vm.computationGridConfig = {

        refreshParamsFunc: null,

        fetchItemsFunc: fetchComputations,
        deleteItemFunc: deleteComputation,

        clickItemFunc: openComputation,

        getItemTitleFunc: getComputationTitle,
        itemCardTemplateUrl: computationCard,
        parentCtl: vm,

        actionsList: computationActionsList,
        addItemActions: computationAddItemActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addComputationTemplate,

        addItemText: function() { return $translate.instant('computation.add-computation-text') },
        noItemsText: function() { return $translate.instant('computation.no-computations-text') },

    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.computationGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.computationGridConfig.topIndex = $stateParams.topIndex;
    }

    // function gridInited(grid) {
    //     vm.grid = grid;
    // }

    function fetchComputations(pageLink) {

        return computationService.getAllComputations(pageLink);
    }

    function deleteComputation(computationId) {
        return computationService.deleteComputation(computationId).then(
            function() {
                vm.showComputations = false;
                vm.showComputationJobs = true;
                $window.localStorage.setItem('currentApp', angular.toJson(vm.currentApplication));
                $window.localStorage.setItem('currentTab', angular.toJson(vm.tabSelectedIndex));
           }
        );
    }

    function getComputationTitle(computation) {
        return computation ? computation.name : '';
    }
    
    function openComputation($event, computation) {
        if ($event) {
            $event.stopPropagation();
        }

        $state.transitionTo ('.', {computationId: computation.id.id}, { location: false, relative: $state.$current, reload: false, notify: false }) 

                vm.computation = computation;
                vm.showComputations = false;
                vm.showComputationJobs = true;
    }

    vm.backtoComputation = function (){
        vm.showComputations = true;
        vm.showComputationJobs = false;
    }

    if($stateParams.computationId != null){
        computationService.getComputation($stateParams.computationId).then(
            function success(computation) {
                vm.computation = computation;
                vm.showComputations = false;
                vm.showComputationJobs = true;
                openComputation(null, vm.computation);
            },
            function fail() {
            }
        );
       
    }

// Computation Job starts

    var computationJobActionsList = [
        {
            onAction: function ($event, item) {
                activateComputationJob($event, item);
            },
            name: function() { return $translate.instant('action.activate') },
            details: function() { return $translate.instant('computationJob.activate') },
            icon: "play_arrow",
            isEnabled: function(computationJob) {
                return isComputationJobEditable(computationJob) && computationJob && computationJob.state === 'SUSPENDED';
            }
        },
        {
            onAction: function ($event, item) {
                suspendComputationJob($event, item);
            },
            name: function() { return $translate.instant('action.suspend') },
            details: function() { return $translate.instant('computationJob.suspend') },
            icon: "pause",
            isEnabled: function(computationJob) {
                return isComputationJobEditable(computationJob) && computationJob && computationJob.state === 'ACTIVE';
            }
        },
        {
            onAction: function ($event, item) {
                vm.grid.deleteItem($event, item);
            },
            name: function() { return $translate.instant('action.delete') },
            details: function() { return $translate.instant('computationJob.delete') },
            icon: "delete",
            isEnabled: isComputationJobEditable
        }
    ];

    var computationJobAddItemActionsList = [
        {
            onAction: function ($event) {
                vm.grid.addItem($event);
            },
            name: function() { return $translate.instant('action.create') },
            details: function() { return $translate.instant('computationJob.create-new-computationJob') },
            icon: "insert_drive_file"
        }
    ];

    //var vm = this;
    

    //vm.types = types;

    //vm.helpLinkIdForComputationJob = helpLinkIdForComputationJob;


    vm.computationJobGridConfig = {

        refreshParamsFunc: null,

        deleteItemTitleFunc: deleteComputationJobTitle,
        deleteItemContentFunc: deleteComputationJobText,
        deleteItemsTitleFunc: deleteComputationJobsTitle,
        //deleteItemsActionTitleFunc: deleteComputationJobsActionTitle,
        deleteItemsContentFunc: deleteComputationJobsText,

        fetchItemsFunc: fetchComputationJobs,
        saveItemFunc: saveComputationJob,
        deleteItemFunc: deleteComputationJob,

        getItemTitleFunc: getComputationJobTitle,
        itemCardTemplateUrl: computationJobCard,
        parentCtl: vm,

        actionsList: computationJobActionsList,
        addItemActions: computationJobAddItemActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addComputationJobTemplate,

        addItemText: function() { return $translate.instant('computationJob.add-computationJob-text') },
        noItemsText: function() { return $translate.instant('computationJob.no-computationJobs-text') },
        itemDetailsText: function() { return $translate.instant('computationJob.computationJob-details') },
        isSelectionEnabled: isComputationJobEditable,
        isDetailsReadOnly: function(computationJob) {
            return !isComputationJobEditable(computationJob);
        }

    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.computationJobGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.computationJobGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.isComputationJobEditable = isComputationJobEditable;

    vm.activateComputationJob = activateComputationJob;
    vm.suspendComputationJob = suspendComputationJob;

    /*function helpLinkIdForComputationJob() {
        return helpLinks.getComputationJobLink(vm.grid.operatingItem());
    }*/

    function deleteComputationJobTitle(computationJob) {
        return $translate.instant('computationJob.delete-computationJob-title', {computationJobName: computationJob.name});
    }

    function deleteComputationJobText() {
        return $translate.instant('computationJob.delete-computationJob-text');
    }

    function deleteComputationJobsTitle(selectedCount) {
        return $translate.instant('computationJob.delete-computationJobs-title', {count: selectedCount}, 'messageformat');
    }

    /*function deleteComputationJobsActionTitle(selectedCount) {
        return $translate.instant('computationJob.delete-computationJobs-action-title', {count: selectedCount}, 'messageformat');
    }*/

    function deleteComputationJobsText() {
        return $translate.instant('computationJob.delete-computationJobs-text');
    }

    // function gridInited(grid) {
    //     vm.grid = grid;
    // }

    function fetchComputationJobs(pageLink) {
        if(vm.computation != null){
                    return computationJobService.getAllComputationJobs(pageLink, vm.computation.id.id);
        }
        else {
                computationService.getComputation($stateParams.computationId).then(
                function success(computation) {
                    //vm.computation = computation;
                    return computationJobService.getAllComputationJobs(pageLink, computation.id.id)
                },
                function fail() {
                }
            );
        }
    }

    function saveComputationJob(computationJob) {
        return computationJobService.saveComputationJob(computationJob, vm.computation.id.id).then(
            function(savedComputationJob) {
                var computationJob = {"applicationId": vm.currentApplication.id.id, "fields":[savedComputationJob.id.id]};
                applicationService.assignComputationJobToApplication(computationJob).then(
                    function success(application) {
                        $window.localStorage.setItem('currentApp', angular.toJson(application));
                        $window.localStorage.setItem('currentTab', angular.toJson(vm.tabSelectedIndex));
                        vm.currentApplication = application;
                    },
                    function fail() {
                        $rootScope.$broadcast('Computation couldn\'t be assigned to the application');                    
                    }
                );
           }
        );
    }

    function deleteComputationJob(computationJobId) {
        return computationJobService.deleteComputationJob(computationJobId).then(
            function() {
                vm.showComputations = false;
                vm.showComputationJobs = true;
                $window.localStorage.setItem('currentApp', angular.toJson(vm.currentApplication));
                $window.localStorage.setItem('currentTab', angular.toJson(vm.tabSelectedIndex));
           }
        );
    }

    function getComputationJobTitle(computationJob) {
        return computationJob ? computationJob.name : '';
    }

    function isComputationJobEditable(computationJob) {
        if (userService.getAuthority() === 'TENANT_ADMIN') {
            return computationJob && computationJob.tenantId.id != types.id.nullUid;
        } else {
            return userService.getAuthority() === 'SYS_ADMIN';
        }
    }

    function activateComputationJob(event,computationJob) {
        computationJobService.activateComputationJob(vm.computation.id.id, computationJob.id.id).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

    function suspendComputationJob(event, computationJob) {
        computationJobService.suspendComputationJob(vm.computation.id.id, computationJob.id.id).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

}


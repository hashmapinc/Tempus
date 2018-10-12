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
/* eslint-disable import/no-unresolved, import/default, no-unused-vars */
import './data_models.scss';
import addDataModel from './add-datamodel.tpl.html';

/*@ngInject*/
export function AddDataModelController($scope, $mdDialog, saveItemFunction, helpLinks, $state) {

    var vm = this;
    vm.helpLinks = helpLinks;
    vm.item = {};


    vm.add = add;
    vm.cancel = cancel;

    function cancel() {
        $mdDialog.cancel();
    }

    // add a new datamodel and redirect to the datamodel canvas
    function add() {
        saveItemFunction(vm.item).then(function success(item) {
            vm.item = item;
            $scope.theForm.$setPristine();
            $mdDialog.hide();
            $state.go('home.data_models.data_model', {
                'datamodelId': vm.item.id.id
            });
        });
    }
}


/*@ngInject*/
export function DataModelsController($scope, datamodelService, $q, toast, $timeout, $filter, $mdDialog, $document, $state, $translate) {
    var vm = this;

    vm.openDataModelDialog = openDataModelDialog;
    vm.deleteMultipleDataModelDialog = deleteMultipleDataModelDialog;
    vm.deleteDataModel = deleteDataModel;
    vm.selectedDataModel = [];
    vm.cancel = cancel;
    vm.saveDataModelFunc = saveDataModelFunc;
    vm.AddDataModelController = AddDataModelController;

    var fetchDataModelFunction = function(pageLink, deviceType) {
        return datamodelService.listDatamodels();
    };

    vm.listDataModel = fetchDataModelFunction;

    $scope.datamodel = {
        count: 0,
        data: []
    };

    $scope.query = {
        order: 'name',
        limit: 15,
        page: 1,
        search: null
    };

    function openDataModelDialog($event) {
        $mdDialog.show({
            controller: vm.AddDataModelController,
            controllerAs: 'vm',
            templateUrl: addDataModel,
            parent: angular.element($document[0].body),
            locals: {
                saveItemFunction: vm.saveDataModelFunc
            },
            fullscreen: true,
            targetEvent: $event
        }).then(function() {}, function() {});
    }

    function deleteMultipleDataModelDialog($event) {

        if ($event) {
            $event.stopPropagation();
        }

        vm.isDelete = [];
        if (vm.selectedDataModel && vm.selectedDataModel.length > 0) {
            var title = $translate.instant('dataModels.delete-datamodel-title', {
                count: vm.selectedDataModel.length
            }, 'messageformat');
            var content = $translate.instant('dataModels.delete-datamodel-text');
            var confirm = $mdDialog.confirm()
                .targetEvent($event)
                .title(title)
                .htmlContent(content)
                .ariaLabel(title)
                .cancel($translate.instant('action.no'))
                .ok($translate.instant('action.yes'));
            $mdDialog.show(confirm).then(function() {

                vm.selectedDataModel.forEach(id_to_delete => { // delete the object by ID
                    datamodelService.deleteDatamodel(id_to_delete).then(function success() {
                        vm.isDelete.push(id_to_delete);
                    });

                });
                $timeout(function() {
                    if (vm.selectedDataModel.length == vm.isDelete.length) {
                        loadDataModel();
                        toast.showSuccess($translate.instant('dataModels.delete-success'));
                    }
                }, 2000);

            });
        }

    }


    function deleteDataModel($event, id) {
        if ($event) {
            $event.stopPropagation();
        }
        var title = $translate.instant('dataModels.delete-datamodel-title', {
            count: 1
        }, 'messageformat');

        var content = $translate.instant('dataModels.delete-datamodel-text');
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function() {

            datamodelService.deleteDatamodel(id).then(function success() {

                toast.showSuccess($translate.instant('dataModels.delete-success'));
                loadDataModel();

            });

        });

    }

    function saveDataModelFunc(item) {
        var deferred = $q.defer();
        datamodelService.saveDatamodel(item).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function cancel() {
        $mdDialog.cancel();
    }

    loadDataModel();

    function loadDataModel() {
        var promise = vm.listDataModel();
        if (promise) {
            promise.then(function success(items) {
                var dataModelSortList = $filter('orderBy')(items, $scope.query.order);
                var startIndex = $scope.query.limit * ($scope.query.page - 1);
                if ($scope.query.search != null) {
                    dataModelSortList = $filter('filter')(items, function(data) {
                        if ($scope.query.search) {
                            return data.name.toLowerCase().indexOf($scope.query.search.toLowerCase()) > -1;
                        } else {
                            return true;
                        }
                    });
                    //$scope.query.page =1;
                    dataModelSortList = $filter('orderBy')(dataModelSortList, $scope.query.order);
                    if ($scope.query.search != '') {
                        startIndex = 0;
                    }
                }
                var dataModelPaginatedata = dataModelSortList.slice(startIndex, startIndex + $scope.query.limit);
                $scope.datamodel = {
                    count: items.length,
                    data: dataModelPaginatedata
                };
            }, )
        }
    }


    $scope.enterFilterMode = function() {
        $scope.query.search = '';
    }

    $scope.exitFilterMode = function() {
        $scope.query.search = null;
        loadDataModel();
    }

    $scope.resetFilter = function() {
        $scope.query = {
            order: 'name',
            limit: $scope.query.limit,
            page: 1,
            search: null
        };
        loadDataModel();
    }


    $scope.$watch("query.search", function(newVal, prevVal) {
        if (!angular.equals(newVal, prevVal) && $scope.query.search != null) {
            loadDataModel();
        }
    });

    $scope.onReorder = function() {
        loadDataModel();
    }

    $scope.onPaginate = function() {
        loadDataModel();
    }

    vm.openDatamodel = function(datamodel) {
        $state.go('home.data_models.data_model', {
            'datamodelId': datamodel.id.id
        });
    }
}
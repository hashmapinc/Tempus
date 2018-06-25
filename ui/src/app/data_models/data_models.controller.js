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
            $state.go('home.data_models.data_model', { 'datamodelId': vm.item.id.id });
        });
    }
}


/*@ngInject*/
export function DataModelsController($scope, $rootScope, $state, $stateParams, userService, datamodelService, deviceService, types, attributeService, $q, dashboardService, applicationService, entityService, tempusboardService, utils, $filter, dashboardUtils, $mdDialog, $document, $translate) {

    var vm = this;

    vm.openDataModelDialog = openDataModelDialog;
    vm.cancel = cancel;
    vm.saveDataModelFunc = saveDataModelFunc;
    vm.AddDataModelController = AddDataModelController;

    var fetchDataModelFunction = function (pageLink, deviceType) {
         return datamodelService.listDataModel();
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
            locals: {saveItemFunction: vm.saveDataModelFunc},
            fullscreen: true,
            targetEvent: $event
        }).then(function () {
        }, function () {
        });
    }


    function saveDataModelFunc(item) {

        var deferred = $q.defer();
        datamodelService.saveDataModel(item).then(function success(response) {
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
         if(promise) {

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
                     if ($scope.query.search != '') {startIndex =0;}
                 }
                 var dataModelPaginatedata = dataModelSortList.slice(startIndex, startIndex + $scope.query.limit);
                 $scope.datamodel = {
                     count: items.length,
                     data: dataModelPaginatedata
                 };

                 },
             )

         }

       }


   $scope.enterFilterMode = function() {

        $scope.query.search = '';
        //loadTableData();
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


}

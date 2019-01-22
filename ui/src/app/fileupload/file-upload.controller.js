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
/* eslint-disable import/no-unresolved, import/default */


/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function FileUploadController(types) {
    var vm = this;

    vm.types = types;

   }

//
//   function loadTableData() {
//           var promise = vm.deviceGridConfig.fetchItemsFunc({limit: $scope.query.limit, textSearch: ''}, false, pageNumber);
//           if(promise) {
//               promise.then(function success(items) {
//                   $scope.devices.data = [];
//                   var deviceSortList = $filter('orderBy')(items.data, $scope.query.order);
//                   if ($scope.query.search != null) {
//
//                       deviceSortList = $filter('filter')(items.data, function(data) {
//                           if ($scope.query.search) {
//                               return data.name.toLowerCase().indexOf($scope.query.search.toLowerCase()) > -1 || data.type.toLowerCase().indexOf($scope.query.search.toLowerCase()) > -1;
//                           } else {
//                               return true;
//                           }
//                       });
//                       deviceSortList = $filter('orderBy')(deviceSortList, $scope.query.order);
//                   }
//
//                   var devicePaginatedata = deviceSortList;
//                   $scope.devices = {
//                       count: items.totalElements,
//                       data: devicePaginatedata
//                   };
//                   },
//               )
//
//           }
//       }
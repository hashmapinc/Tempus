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
/*@ngInject*/
export default function AddAttributeDialogController($scope, toast, $mdDialog, $translate, $filter, types, importExport, attributeService, telementryData, depthData, entityType, entityId, attributeScope) {

    var vm = this;

    vm.attribute = {};

    vm.valueTypes = types.valueType;

    vm.valueType = types.valueType.string;

    vm.add = add;
    vm.downloadTelementry = downloadTelementry;
    vm.downloadDepth = downloadDepth;
    vm.cancel = cancel;
    vm.currentDate = new Date();
    vm.endDate = new Date();

    function cancel() {
        $mdDialog.cancel();
    }

    function downloadTelementry() {

     var startDate = Date.parse(vm.currentDate);
     var endDate =  Date.parse(vm.endDate);

      if(startDate > endDate) {

            toast.showError($translate.instant('attribute.date-validate'));
            return false;
       }
            var headers = {
                lastUpdateTs: 'Last Updated Date', // remove commas to avoid errors
                key: "Key",
                value: "Value"
            };

             var itemsFormatted = [];
             telementryData.forEach((item) => {
              if(item.lastUpdateTs >= startDate && item.lastUpdateTs <= endDate) {
                 itemsFormatted.push({
                     lastUpdateTs: $filter('date')(item.lastUpdateTs, "yyyy-MM-dd HH:mm:ss"),
                     key: item.key,
                     value: item.value
                     });
               }
             });

            importExport.exportAttribute(itemsFormatted, headers ,'telementry.csv');
            $mdDialog.hide();
    }

    function downloadDepth() {

     var start = vm.start;
     var end =  vm.end;
            var headers = {
                lastUpdateTs: 'Last Updated Date', // remove commas to avoid errors
                key: "Key",
                value: "Value"
            };

             var itemsFormatted = [];
             depthData.forEach((item) => {
              if(item.lastUpdateTs >= start && item.lastUpdateTs <= end) {
                 itemsFormatted.push({
                     lastUpdateTs:item.lastUpdateTs ,
                     key: item.key,
                     value: item.value
                     });
               }
             });
            importExport.exportAttribute(itemsFormatted, headers ,'depth.csv');
            $mdDialog.hide();
    }

    function add() {
        $scope.theForm.$setPristine();
        attributeService.saveEntityAttributes(entityType, entityId, attributeScope, [vm.attribute]).then(
            function success() {
                $mdDialog.hide();
            }
        );
    }

    $scope.$watch('vm.valueType', function() {
        if (vm.valueType === types.valueType.boolean) {
            vm.attribute.value = false;
        } else {
            vm.attribute.value = null;
        }
    });
}

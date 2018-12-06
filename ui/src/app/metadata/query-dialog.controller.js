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


/*@ngInject*/
export default function QueryDialogController($scope, $mdDialog,types, metadata, metadataService, $q, metadataQuery, isReadOnly, isAdd, selectedIndex) {


    var vm = this;
    vm.metadataQuery=metadataQuery;
    vm.isReadOnly = isReadOnly;
    vm.isAdd = isAdd;
    vm.close = close;
    vm.showConditionFlag = false;
    vm.templatesUrl = null
    vm.save = save;
    vm.showFlag = false;
    vm.displayAdd = true;
    vm.triggers=[{id:'CRON',name:'CRON'}]
    vm.regex="^\\s*($|#|\\w+\\s*=|(\\?|\\*|(?:[0-5]?\\d)(?:(?:-|\/|\\,)(?:[0-5]?\\d))?(?:,(?:[0-5]?\\d)(?:(?:-|\/|\\,)(?:[0-5]?\\d))?)*)\\s+(\\?|\\*|(?:[0-5]?\\d)(?:(?:-|\/|\\,)(?:[0-5]?\\d))?(?:,(?:[0-5]?\\d)(?:(?:-|\/|\\,)(?:[0-5]?\\d))?)*)\\s+(\\?|\\*|(?:[01]?\\d|2[0-3])(?:(?:-|\/|\\,)(?:[01]?\\d|2[0-3]))?(?:,(?:[01]?\\d|2[0-3])(?:(?:-|\/|\\,)(?:[01]?\\d|2[0-3]))?)*)\\s+(\\?|\\*|(?:0?[1-9]|[12]\\d|3[01])(?:(?:-|\/|\\,)(?:0?[1-9]|[12]\\d|3[01]))?(?:,(?:0?[1-9]|[12]\\d|3[01])(?:(?:-|\/|\\,)(?:0?[1-9]|[12]\\d|3[01]))?)*)\\s+(\\?|\\*|(?:[1-9]|1[012])(?:(?:-|\/|\\,)(?:[1-9]|1[012]))?(?:L|W)?(?:,(?:[1-9]|1[012])(?:(?:-|\/|\\,)(?:[1-9]|1[012]))?(?:L|W)?)*|\\?|\\*|(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(?:(?:-)(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?(?:,(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(?:(?:-)(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?)*)\\s+(\\?|\\*|(?:[0-6])(?:(?:-|\/|\\,|#)(?:[0-6]))?(?:L)?(?:,(?:[0-6])(?:(?:-|\/|\\,|#)(?:[0-6]))?(?:L)?)*|\\?|\\*|(?:MON|TUE|WED|THU|FRI|SAT|SUN)(?:(?:-)(?:MON|TUE|WED|THU|FRI|SAT|SUN))?(?:,(?:MON|TUE|WED|THU|FRI|SAT|SUN)(?:(?:-)(?:MON|TUE|WED|THU|FRI|SAT|SUN))?)*)(|\\s)+(\\?|\\*|(?:|\\d{4})(?:(?:-|\/|\\,)(?:|\\d{4}))?(?:,(?:|\\d{4})(?:(?:-|\/|\\,)(?:|\\d{4}))?)*))$";
    vm.selectedIndex = selectedIndex;
    initController();
    function initController(){
        if(vm.metadataQuery){
            angular.forEach(vm.triggers, function (list) {
                if (list.id === vm.metadataQuery.triggerType) {
                    $scope.trigger = list;
                }
            });
        }
    }
    function close () {
        $mdDialog.hide();
    }

    function save(parameter){
        var requestObject = {
           metadataConfigId:{
            id:metadata.id.id
           },
           queryStmt:null,
           triggerType:null,
           triggerSchedule:vm.metadataQuery.triggerSchedule,
           attribute:vm.metadataQuery.attribute
        }
        if(metadataQuery){
            requestObject.id;
             requestObject.id ={
                id:metadataQuery.id.id
             }
        }
        var deferred = $q.defer();
        if(parameter == 'generate'){
            requestObject.triggerType = vm.metadataQuery.triggerType.id
            requestObject.queryStmt = 'select '+vm.query.key+','+vm.query.value +' from '+ vm.query.tableName +' where '+vm.query.whereCondition+';';
        }else {
            requestObject.triggerType = $scope.trigger.id
            requestObject.queryStmt = vm.metadataQuery.queryStmt;
        }
        metadataService.saveMetadataQuery(requestObject).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        vm.close();
        return deferred.promise;
    }
}

/* eslint-enable angular/angularelement */

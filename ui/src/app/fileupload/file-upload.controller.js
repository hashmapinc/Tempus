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
export default function FileUploadController(toast, $scope, $injector,$document, fileUploadService, $q, $timeout, $translate, $mdDialog) {

    var vm = this;

    vm.files = [];
    var types;

    function loadTableData() {
            var promise = fileUploadService.getAllFile();

            if(promise) {
                promise.then(function success(items) {
                    vm.files = items;
                    },
                )
            }
     }

     loadTableData();

    function getTypes() {
            if (!types) {
                types = $injector.get("types");
            }
            return types;
        }

    vm.openFileDialog = function() {
           angular.element($document[0].getElementById('inputFile').click());
        };

    $scope.thisFileUpload = function(ele) {

           var fileToBeUploaded = ele.files;

           if(fileToBeUploaded.length > 0 ){
                if(fileToBeUploaded[0].size <= getTypes().fileUpload.maxSize){
                saveFile(fileToBeUploaded[0]);
                }
                else{
                toast.showError($translate.instant('file-upload.fileSizeError'));
                }
           }
    };

    function saveFile(file) {
        var deferred = $q.defer();
        fileUploadService.uploadFile(file).then(
            function success(savedFile) {
                vm.files.push(savedFile);
                loadTableData();
                return savedFile;
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    vm.downloadFile = function($event, file) {
        $event.stopPropagation();
        fileUploadService.exportFile(file.fileName);
    }



    vm.deleteFile = function($event,file) {
    var confirm = $mdDialog.confirm()
        .targetEvent($event)
        .title(deleteFileTitle(file))
        .htmlContent(deleteFileText())
        .ariaLabel($translate.instant('grid.delete-item'))
        .cancel($translate.instant('action.no'))
        .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
        fileUploadService.deleteFile(file.fileName).then(function success() {
            vm.resetFilter();

        });
    });
    }

    vm.resetFilter = function() {
        loadTableData();
    }

    function deleteFileTitle(file) {
        return $translate.instant('file-upload.delete-file-title', {fileName: file.fileName});
    }

    function deleteFileText() {
        return $translate.instant('file-upload.delete-file-text');
    }


   }

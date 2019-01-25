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
export default function FileUploadController(types, $scope, $log,$document, fileUploadService, $q, $timeout) {

    var vm = this;

    vm.types = types;

    $scope.files = [];
    $scope.thisFileUpload = function() {
           $log.log('samds');
           angular.element($document[0].getElementById('file').click())
           var x = angular.element($document[0].getElementById('file'));

           $timeout(function(){

           $log.log(x);

           var fileName ="";
           var fileSize ="";
           var fileType = "";
           var lastModified = "";

           fileName = x[0].files[0].name;
           fileSize = x[0].files[0].size;
           fileType = x[0].files[0].type;
           lastModified = x[0].files[0].lastModifiedDate.getTime();

          // $log.log(fileUploadService.getAllFile());
           $log.log(fileName);
           $log.log(fileSize);
           $log.log(fileType);

           var fileToAdd = {};

           fileToAdd = {fileName:fileName, lastModified: lastModified, fileSize:fileSize, fileType:fileType};


           $scope.files.push(fileToAdd);

           var file = saveFile(x[0].files[0]);
           $log.log(file);
           }, 2000);

        };

function saveFile(file) {
$log.log(file);
        var deferred = $q.defer();
        fileUploadService.uploadFile(file).then(
            function success(savedFile) {

                var files = [ savedFile ];
                return files;
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }
//    $scope.addFile = function($event) {
//
//
//     var x = angular.element("file-input");
//      var txt = "";
//      if ('files' in x) {
//        if (x.files.length == 0) {
//          txt = "Select one or more files.";
//        } else {
//          for (var i = 0; i < x.files.length; i++) {
//            txt += "<br><strong>" + (i+1) + ". file</strong><br>";
//            var file = x.files[i];
//            if ('name' in file) {
//              txt += "name: " + file.name + "<br>";
//            }
//            if ('size' in file) {
//              txt += "size: " + file.size + " bytes <br>";
//            }
//          }
//        }
//      }
//      else {
//        if (x.value == "") {
//          txt += "Select one or more files.";
//        } else {
//          txt += "The files property is not supported by your browser!";
//          txt  += "<br>The path of the selected file: " + x.value; // If the browser does not support the files property, it will return the path of the selected file instead.
//        }
//      }
//
//        $log.log($event);
//        $log.log(txt);
//        $log.log(file.size);
//       }
//
//       $scope.handleFiles =  function (file){
//           $log.log("in handle");
//           $log.log(file);
//      }

   }

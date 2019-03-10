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


export default angular.module('tempus.api.fileUpload', [])
	.factory('fileUploadService', FileUploadService)
	.name;

/*@ngInject*/
function FileUploadService(toast, $http, $q, $translate, $window, $document, $injector) {

	var service = {

		getAllFile: getAllFile,
		uploadFile: uploadFile,
		deleteFile: deleteFile,
		exportFile: exportFile,
		renameFile: renameFile,
		fileValidation: fileValidation,
		extensionValidation: extensionValidation,
		searchFile: searchFile,
		newNameValidation: newNameValidation

	}

	return service;

	function uploadFile(file, entityId, entityType){
		var deferred = $q.defer();
		var url = '/api/file?relatedEntityId=' + entityId + '&relatedEntityType=' + entityType;
		var fd = new FormData();
		fd.append("file", file);
		$http.post(url, fd,{
			transformRequest: angular.identity,
			headers: {
				'Content-Type': undefined
			}
		}).then(function success(response) {
			deferred.resolve(response.data);
		}, function fail(response) {
			deferred.reject(response.data);
		});
		return deferred.promise;
	}

	function exportFile(fileName, extension, entityId, entityType) {
		if (extension != getTypes().fileUpload.na && extension != "") {
			var name = fileName + "." + extension;
		} else {
			name = fileName;
		}

		getFile(name, entityId, entityType).then(
			function success(file) {
				exportToPc(prepareExport(file), name);
			},
			function fail(rejection) {
				var message = rejection;
				if (!message) {
					message = $translate.instant('error.unknown-error');
				}

			}
		);
	}

	function prepareExport(data) {
		var exportedData = angular.copy(data);
		if (angular.isDefined(exportedData.id)) {
			delete exportedData.id;
		}
		if (angular.isDefined(exportedData.createdTime)) {
			delete exportedData.createdTime;
		}
		if (angular.isDefined(exportedData.tenantId)) {
			delete exportedData.tenantId;
		}
		if (angular.isDefined(exportedData.customerId)) {
			delete exportedData.customerId;
		}
		return exportedData;
	}

	function exportToPc(data, filename) {
		if (!data) {
			return;
		}

		if (!filename) {
			filename = 'downloaded file';
		}

		if ($window.navigator && $window.navigator.msSaveOrOpenBlob) {
			$window.navigator.msSaveOrOpenBlob(data, filename);
		} else {

		var urlCreator = $window.URL || $window.webkitURL;

		var anchor = angular.element('<a/>');
		anchor.attr({
		href: urlCreator.createObjectURL(data),
		download: filename
		})[0].click();
		}
	}

	function getFile(fileName, entityId, entityType) {
		var deferred = $q.defer();
		var url = '/api/file/' + fileName + '?relatedEntityId=' + entityId + '&relatedEntityType=' + entityType;
		$http.get(url, { responseType: 'blob' }).then(function success(response) {
			deferred.resolve(response.data);
		}, function fail(response) {
			deferred.reject(response.data);
		});
		return deferred.promise;
	}


	function getAllFile(entityId, entityType) {
        if(angular.isDefined(entityId)) {
            var deferred = $q.defer();
            var url = '/api/file?relatedEntityId=' + entityId + '&relatedEntityType=' + entityType;
            $http.get(url).then(function success(response) {
                deferred.resolve(response.data);
            }, function fail() {
                deferred.reject();
            });
		return deferred.promise;
		}
	}

	function deleteFile(fileName, extension, entityId, entityType) {

		if (extension != getTypes().fileUpload.na && extension != "") {
			var url = '/api/file/' + fileName + "." + extension+'?relatedEntityId=' + entityId + '&relatedEntityType=' + entityType;
		} else {
			url = '/api/file/' + fileName+'?relatedEntityId=' + entityId + '&relatedEntityType=' + entityType;
		}

		var deferred = $q.defer();
		$http.delete(url).then(function success() {
			deferred.resolve();
			toast.showSuccess($translate.instant('file-upload.deleteSuccess'));
		}, function fail() {
			deferred.reject();
		});
		return deferred.promise;
	}


	function renameFile(oldFileName, newFileName, extension, entityId, entityType) {
		var deferred = $q.defer();
		if (extension != getTypes().fileUpload.na && extension != "") {
			var url = '/api/file/' + oldFileName + "." + extension;//+'?relatedEntityId=' + entityId + '&relatedEntityType=' + entityType;
			newFileName = newFileName + "." + extension;
		} else {
			url = '/api/file/' + oldFileName;//+'?relatedEntityId=' + entityId + '&relatedEntityType=' + entityType;
		}
		var map = createEntityInfoMap(entityId, entityType);
		map.newFileName = newFileName;
		$http.put(url, angular.toJson(map), {
			transformRequest: angular.identity,
			headers: {
				'Content-Type': 'application/json'
			}
		}).then(function success(response) {
			deferred.resolve(response.data);
		}, function fail(response) {
			deferred.reject(response.data);
		});
		return deferred.promise;
	}

	function searchFile(fileName, extension, entityId, entityType) {
		var deferred = $q.defer();

		if (extension != getTypes().fileUpload.na && extension != "") {
			var url = '/api/file?fileName=' + fileName + "." + extension+ '&relatedEntityId=' + entityId + '&relatedEntityType=' + entityType;
		} else {
			url = '/api/file?fileName=' + fileName+ '&relatedEntityId=' + entityId + '&relatedEntityType=' + entityType;
		}
		$http.get(url, {
			transformRequest: angular.identity,
			headers: {
				'Content-Type': undefined
			}
		}).then(function success(response) {
			deferred.resolve(response.data);
		}, function fail(response) {
			deferred.reject(response.data);
		});
		return deferred.promise;
	}

	function fileValidation(fileToBeUploaded) {

		if (extensionValidation(fileToBeUploaded[0].name) === true) {
			if (fileToBeUploaded[0].size <= getTypes().fileUpload.maxSize) {
				return true;
			} else {
				toast.showError($translate.instant('file-upload.fileSizeError'));
			}
		} else {
			toast.showError($translate.instant('file-upload.fileExtensionError'));
		}
	}

	function getTypes() {
		return $injector.get("types");
	}


	function newNameValidation(fileName) {
		var deferred = $q.defer();
		searchFile(fileName).then(
			function success(searchItems) {
				var existFile = 0;
				if (searchItems.length == 0) {

					existFile = 0;
				} else {

					for (var i = 0; i < searchItems.length; i++) {
						if (searchItems[i].fileName == fileName) {
							existFile++;
						}
					}
					if (existFile == 0) {
						return true;
					} else {
						toast.showError($translate.instant('file-upload.fileNameError'));
						return false;
					}
				}
			},
			function fail() {
				deferred.reject();
			}
		);
		return deferred.promise;
	}


	function extensionValidation(fileName) {
		var ext = fileName.split(".");
		var listOfExtension = getTypes().fileUpload.listOfExtension;
		var extension = ext[ext.length - 1];
		if (listOfExtension.indexOf(extension) > -1) {
			return false;
		} else {
			return true;
		}

	}

	function createEntityInfoMap(entityId, entityType) {
	var map = {
	"relatedEntityId" : entityId,
	"relatedEntityType": entityType
	}
	return map;
	}

}
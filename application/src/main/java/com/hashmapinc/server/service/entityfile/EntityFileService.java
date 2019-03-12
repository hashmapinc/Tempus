/**
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
package com.hashmapinc.server.service.entityfile;

import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.upload.FileMetaData;
import com.hashmapinc.server.common.data.upload.InputStreamWrapper;
import com.hashmapinc.server.common.data.upload.StorageTypes;
import com.hashmapinc.server.dao.filemetadata.FileMetaDataDao;
import com.hashmapinc.server.dao.tenant.TenantService;
import com.hashmapinc.server.service.CloudStorageServiceUtils;
import com.hashmapinc.server.service.computation.CloudStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
public class EntityFileService {

    @Autowired
    private CloudStorageService cloudStorageService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private FileMetaDataDao fileMetaDataDao;

    public FileMetaData uploadFile(MultipartFile file, TenantId tenantId, EntityId entityId) throws Exception {
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        String objectName = CloudStorageServiceUtils.createObjectName(file.getOriginalFilename(), entityId, StorageTypes.FILES);
        if(cloudStorageService.upload(bucketName, objectName, file.getInputStream(), file.getContentType())) {
            log.info("File uploaded to cloud storage ");
            FileMetaData fileMetaData = createFileMetaData(file, tenantId, entityId);
            return fileMetaDataDao.save(fileMetaData);
        }
        return null;
    }

    public List<FileMetaData> getFileList(TenantId tenantId, EntityId entityId, String fileName) {
        if(fileName != null) {
            return fileMetaDataDao.getFiles(tenantId, entityId, getFileNameWithOrWithoutExt(fileName),
                    getFileExtension(fileName));
        }
        return fileMetaDataDao.getFiles(tenantId, entityId);
    }

    public InputStreamWrapper downloadFile(String fileName, TenantId tenantId, EntityId entityId) throws Exception {
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        String objectUrl = CloudStorageServiceUtils.createObjectName(fileName, entityId, StorageTypes.FILES);
        return cloudStorageService.getFile(bucketName, objectUrl);
    }

    public void deleteFile(String fileName, TenantId tenantId, EntityId entityId) throws Exception {
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        String objectUrl = CloudStorageServiceUtils.createObjectName(fileName, entityId, StorageTypes.FILES);
        if (cloudStorageService.delete(bucketName, objectUrl))
            fileMetaDataDao.delete(tenantId, entityId, getFileNameWithOrWithoutExt(fileName),
                    getFileExtension(fileName));
    }

    public void renameFile(String oldFileName, String newFileName, TenantId tenantId, EntityId entityId) throws Exception{
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        String oldObjectUrl = CloudStorageServiceUtils.createObjectName(oldFileName, entityId, StorageTypes.FILES);
        String newObjectUrl = CloudStorageServiceUtils.createObjectName(newFileName, entityId, StorageTypes.FILES);
        if (!oldObjectUrl.contentEquals(newObjectUrl) && cloudStorageService.copyFile(bucketName, oldObjectUrl, newObjectUrl)) {
            cloudStorageService.delete(bucketName, oldObjectUrl);
            renameFileInDb(oldFileName, newFileName, tenantId, entityId);
        }
    }

    private void renameFileInDb(String oldFileName, String newFileName, TenantId tenantId, EntityId entityId) {
        String oldFileNameWithoutExt = getFileNameWithOrWithoutExt(oldFileName);
        String oldFileExt = getFileExtension(oldFileName);
        FileMetaData fileMetaData = fileMetaDataDao.getFiles(tenantId, entityId, oldFileNameWithoutExt,
                oldFileExt).get(0);
        fileMetaDataDao.delete(tenantId, entityId, oldFileNameWithoutExt, oldFileExt);
        String newFileWithoutExt = getFileNameWithOrWithoutExt(newFileName);
        String newFileExt = getFileExtension(newFileName);

        fileMetaData.setFileName(newFileWithoutExt);
        fileMetaData.setExtension(newFileExt);
        fileMetaData.setLastUpdated(System.currentTimeMillis());
        fileMetaDataDao.save(fileMetaData);
    }

    private FileMetaData createFileMetaData(MultipartFile file, TenantId tenantId, EntityId entityId) {
        final String originalFilename = file.getOriginalFilename();
        String fileName = getFileNameWithOrWithoutExt(originalFilename);
        String extension = getFileExtension(originalFilename);
        return new FileMetaData(tenantId, entityId, entityId.getEntityType(), fileName, extension,
                System.currentTimeMillis(),
                Precision.round((file.getSize() / 1024.0), 3));
    }

    private String getFileNameWithOrWithoutExt(String fileName) {
        String[] arrList = fileName.split("\\.");
        if (arrList.length >= 2) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    private String getFileExtension(String fileName) {
        String[] arrList = fileName.split("\\.");
        if (arrList.length >= 2) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "NA";
    }

}

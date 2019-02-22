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
package com.hashmapinc.server.service.upload;

import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.upload.FileMetaData;
import com.hashmapinc.server.common.data.upload.InputStreamWrapper;
import com.hashmapinc.server.common.data.upload.StorageTypes;
import com.hashmapinc.server.dao.tenant.TenantService;
import com.hashmapinc.server.service.CloudStorageServiceUtils;
import com.hashmapinc.server.service.computation.CloudStorageService;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UploadService {

    @Autowired
    private CloudStorageService cloudStorageService;

    @Autowired
    private TenantService tenantService;

    public FileMetaData uploadFile(MultipartFile file, TenantId tenantId) throws Exception {
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        String objectName = CloudStorageServiceUtils.createObjectName(file.getOriginalFilename(), StorageTypes.FILES);
        if(cloudStorageService.upload(bucketName, objectName, file.getInputStream(), file.getContentType())) {
            log.info("File uploaded to cloud storage ");
            List<Item> items = cloudStorageService.getAllFiles(bucketName, CloudStorageServiceUtils.createPrefix(file.getOriginalFilename(),
                    StorageTypes.FILES));
            return items.stream().map(this::addFileMetaData).collect(Collectors.toList()).get(0);
        }
        return null;
    }

    public List<FileMetaData> getFileList(TenantId tenantId, String fileName) throws Exception{
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        String fileSearchPrefix = StorageTypes.FILES;
        if (!StringUtils.isEmpty(fileName))
            fileSearchPrefix = CloudStorageServiceUtils.createPrefix(fileName, StorageTypes.FILES);
        List<Item> items = cloudStorageService.getAllFiles(bucketName, fileSearchPrefix);
        return items.stream().map(this::addFileMetaData).collect(Collectors.toList());
    }

    public InputStreamWrapper downloadFile(String fileName, TenantId tenantId) throws Exception {
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        String objectUrl = CloudStorageServiceUtils.createObjectName(fileName, StorageTypes.FILES);
        return cloudStorageService.getFile(bucketName, objectUrl);
    }

    public void deleteFile(String fileName, TenantId tenantId) throws Exception {
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        String objectUrl = CloudStorageServiceUtils.createObjectName(fileName, StorageTypes.FILES);
        cloudStorageService.delete(bucketName, objectUrl);
    }

    public void renameFile(String oldFileName, String newFileName, TenantId tenantId) throws Exception{
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        String oldObjectUrl = CloudStorageServiceUtils.createObjectName(oldFileName, StorageTypes.FILES);
        String newObjectUrl = CloudStorageServiceUtils.createObjectName(newFileName, StorageTypes.FILES);
        if (!oldObjectUrl.contentEquals(newObjectUrl) && cloudStorageService.copyFile(bucketName, oldObjectUrl, newObjectUrl)) {
            cloudStorageService.delete(bucketName, oldObjectUrl);
        }
    }

    private FileMetaData addFileMetaData(Item item) {
        String[] arrList = item.objectName().split("/");
        String fileName = arrList[arrList.length -1];
        String[] arrList2 = fileName.split("\\.");
        if (arrList2.length >= 2) {
            String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf("."));
            return new FileMetaData(fileNameWithoutExt, arrList2[arrList2.length - 1],
                    item.lastModified().toInstant().getEpochSecond() * 1000,
                    Precision.round(item.objectSize() / 1024.0, 4));
        }
        return new FileMetaData(fileName, "NA", item.lastModified().toInstant().getEpochSecond() * 1000,
                Precision.round(item.objectSize() / 1024.0, 4));
    }
}

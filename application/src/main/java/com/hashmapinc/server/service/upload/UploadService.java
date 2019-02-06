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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        String objectUrl = CloudStorageServiceUtils.createObjectUrl(file.getOriginalFilename(), StorageTypes.FILES);
        if(cloudStorageService.upload(bucketName, objectUrl, file.getInputStream(), file.getContentType())) {
            log.info("File uploaded to cloud storage ");
            List<Item> items = cloudStorageService.getAllFiles(bucketName, CloudStorageServiceUtils.createPrefix(file.getOriginalFilename(),
                    StorageTypes.FILES));
            return items.stream().map(this::addFileMetaData).collect(Collectors.toList()).get(0);
        }
        return null;
    }

    public List<FileMetaData> getFileList(TenantId tenantId) throws Exception{
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        List<Item> items = cloudStorageService.getAllFiles(bucketName, StorageTypes.FILES);
        return items.stream().map(this::addFileMetaData).collect(Collectors.toList());
    }

    public InputStreamWrapper downloadFile(String fileName, TenantId tenantId) throws Exception {
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        String s3ObjectUrl = CloudStorageServiceUtils.createObjectUrl(fileName, StorageTypes.FILES);
        return cloudStorageService.getFile(bucketName, s3ObjectUrl);
    }

    public void deleteFile(String fileName, TenantId tenantId) throws Exception {
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        String s3ObjectUrl = CloudStorageServiceUtils.createObjectUrl(fileName, StorageTypes.FILES);
        cloudStorageService.delete(bucketName, s3ObjectUrl);
    }

    private FileMetaData addFileMetaData(Item item) {
        String[] arrList = item.objectName().split("/");
        String fileName = arrList[arrList.length -1];
        String[] arrList2 = fileName.split("\\.");
        if (arrList2.length == 2)
            return new FileMetaData(fileName, arrList2[1], item.lastModified(), item.objectSize());
        return new FileMetaData(fileName, "NA", item.lastModified(), item.objectSize());
    }
}

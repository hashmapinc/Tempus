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
import com.hashmapinc.server.common.data.upload.FileObject;
import com.hashmapinc.server.dao.tenant.TenantService;
import com.hashmapinc.server.service.CloudStorageServiceUtils;
import com.hashmapinc.server.service.computation.CloudStorageService;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UploadService {
    @Autowired
    private CloudStorageService cloudStorageService;

    @Autowired
    private TenantService tenantService;

    public FileObject uploadFile(MultipartFile file, TenantId tenantId) throws Exception {
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantId, tenantService);
        String s3ObjectUrl = CloudStorageServiceUtils.createObjectUrl(file.getName(), "files");
        if(cloudStorageService.upload(bucketName, s3ObjectUrl, file.getBytes())) {
            log.info("File uploaded to cloud storage ");
            List<FileObject> fileObjects = new ArrayList<>();
            List<Item> items = cloudStorageService.getAllFiles(bucketName, s3ObjectUrl);
            items.forEach(item -> addFileObjects(fileObjects, item));
            return fileObjects.get(0);
        }
        return null;
    }

    public List<FileObject> getFileList(TenantId tenantId) throws Exception{
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantId, tenantService);
        List<FileObject> fileObjects = new ArrayList<>();
        List<Item> items = cloudStorageService.getAllFiles(bucketName, "files");
        items.forEach(item -> addFileObjects(fileObjects, item));
        return fileObjects;
    }

    public byte[] downloadFile(String fileName, TenantId tenantId) throws Exception {
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantId, tenantService);
        String s3ObjectUrl = CloudStorageServiceUtils.createObjectUrl(fileName, "files");
        return cloudStorageService.getFile(bucketName, s3ObjectUrl).getBytes();
    }

    public void deleteFile(String fileName, TenantId tenantId) throws Exception {
        String bucketName = CloudStorageServiceUtils.createBucketName(tenantId, tenantService);
        String s3ObjectUrl = CloudStorageServiceUtils.createObjectUrl(fileName, "files");
        cloudStorageService.delete(bucketName, s3ObjectUrl);
    }

    private boolean addFileObjects(List<FileObject> fileObjects, Item item) {
        String[] arrList = item.objectName().split("/");
        String[] arrList2 = arrList[arrList.length - 1].split(".");
        if (arrList2.length == 2)
            return fileObjects.add(new FileObject(arrList[arrList.length - 1], arrList2[1], item.lastModified(), item.objectSize()));
        return fileObjects.add(new FileObject(arrList[arrList.length - 1], "NA", item.lastModified(), item.objectSize()));
    }
}

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
package com.hashmapinc.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hashmapinc.server.common.data.upload.FileMetaData;
import com.hashmapinc.server.common.data.upload.InputStreamWrapper;
import com.hashmapinc.server.common.data.upload.StorageTypes;
import com.hashmapinc.server.service.computation.CloudStorageService;
import com.hashmapinc.server.service.upload.UploadService;
import io.minio.messages.Item;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseUploadControllerTest extends AbstractControllerTest {

    @Autowired
    private UploadService uploadService;

    @Autowired
    private CloudStorageService cloudStorageService;

    private MultipartFile multipartFile = null;

    private List<Item> items = new ArrayList<>();

    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();

        ClassLoader classLoader = this.getClass().getClassLoader();
        multipartFile = new MockMultipartFile("file", new FileInputStream(new File(classLoader.
                getResource("file.txt").getFile())));

        String objectName1 = StorageTypes.FILES + "/" + "temp.txt";
        String objectName2 = StorageTypes.FILES + "/" + "temp2";
        Item item = new Item(objectName1, false);
        item.putIfAbsent("LastModified", "2019-02-02T05:06:10.249Z");
        item.putIfAbsent("Size", 1);
        Item item2 = new Item(objectName2, false);
        item2.putIfAbsent("LastModified", "2019-02-02T05:06:10.249Z");
        item2.putIfAbsent("Size", 1);
        items.add(item);
        items.add(item2);
    }

    @Test
    public void uploadFile() throws Exception {
        when(cloudStorageService.upload(any(), any(), any(), any())).thenReturn(true);
        when(cloudStorageService.getAllFiles(any(), any())).thenReturn(items);
        uploadService.uploadFile(multipartFile, tenantId);
        FileMetaData retFileMetaData = doPostFile("/api/file", multipartFile, FileMetaData.class);
        Assert.assertNotNull(retFileMetaData);
        Assert.assertEquals("temp.txt", retFileMetaData.getFileName());
    }

    @Test
    public void uploadFileReturnsNotFoundException() throws Exception {
        when(cloudStorageService.upload(any(), any(), any(), any())).thenReturn(false);
        doPostFile("/api/file", multipartFile).andExpect(status().isNotFound());
    }

    @Test
    public void getAllFiles() throws Exception {
        List<FileMetaData> list = new ArrayList<>();
        when(cloudStorageService.getAllFiles(any(), any())).thenReturn(items);
        List<FileMetaData> retFileMetaDataList = doGetTyped("/api/file", new TypeReference<List<FileMetaData>>() {
        });
        Assert.assertEquals(2, retFileMetaDataList.size());
    }

    @Test
    public void downloadFile() throws Exception {
        InputStreamWrapper streamWrapper = new InputStreamWrapper(new ByteArrayInputStream("xtz".getBytes()), "txt");
        when(cloudStorageService.getFile(any(), any())).thenReturn(streamWrapper);
        ResultActions resultActions = doGet("/api/file/" +
                "image.jpg");
        Assert.assertEquals(200, resultActions.andReturn().getResponse().getStatus());
    }

    @Test
    public void downloadFileThrowsInternalServerError() throws Exception {
        InputStreamWrapper streamWrapper = null;
        when(cloudStorageService.getFile(any(), any())).thenReturn(streamWrapper);
        doGet("/api/file/image.jpg").andExpect(status().isInternalServerError());
    }
}

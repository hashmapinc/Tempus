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
import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.upload.FileMetaData;
import com.hashmapinc.server.common.data.upload.InputStreamWrapper;
import com.hashmapinc.server.service.computation.CloudStorageService;
import com.hashmapinc.server.service.entityfile.EntityFileService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class EntityFileControllerTest extends AbstractControllerTest {

    @Autowired
    private EntityFileService entityFileService;

    @Autowired
    private CloudStorageService cloudStorageService;

    private MultipartFile multipartFile = null;
    private MultipartFile multipartFile2 = null;
    private MultipartFile multipartFile3 = null;

    private Device device = null;

    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();

        ClassLoader classLoader = this.getClass().getClassLoader();
        multipartFile = new MockMultipartFile("file", "file.txt", "txt", new FileInputStream(new File(classLoader.
                getResource("file.txt").getFile())));

        multipartFile2 = new MockMultipartFile("file", "file2.txt", "txt", new FileInputStream(new File(classLoader.
                getResource("file2.txt").getFile())));

        multipartFile3 = new MockMultipartFile("file", "tempus-logo.jpg", "svg", new FileInputStream(new File(classLoader.
                getResource("tempus-logo.jpg").getFile())));

        createDevice();
    }

    private void createDevice() throws Exception {
        if(this.device == null) {
            Device device = new Device();
            device.setName("My device");
            device.setType("default");
            Device savedDevice = doPost("/api/device", device, Device.class);
            Assert.assertNotNull(savedDevice);
            this.device = savedDevice;
        }
    }

    @Test
    public void uploadFile() throws Exception {
        when(cloudStorageService.upload(any(), any(), any(), any())).thenReturn(true);
        String requestParams = "?relatedEntityId=" + device.getId() + "&relatedEntityType=DEVICE";
        FileMetaData retFileMetaData = doPostFile("/api/file" + requestParams, multipartFile, FileMetaData.class);
        Assert.assertNotNull(retFileMetaData);
        Assert.assertEquals("file", retFileMetaData.getFileName());

    }

    @Test
    public void uploadFileReturnsNotFoundException() throws Exception {
        when(cloudStorageService.upload(any(), any(), any(), any())).thenReturn(false);
        String requestParams = "?relatedEntityId=" + device.getId() + "&relatedEntityType=DEVICE";
        doPostFile("/api/file" + requestParams, multipartFile).andExpect(status().isNotFound());
    }

    @Test
    public void getAllFiles() throws Exception {
        when(cloudStorageService.upload(any(), any(), any(), any())).thenReturn(true);
        String requestParams = "?relatedEntityId=" + device.getId() + "&relatedEntityType=DEVICE";

        FileMetaData retFileMetaData = doPostFile("/api/file" + requestParams, multipartFile, FileMetaData.class);
        Assert.assertNotNull(retFileMetaData);

        FileMetaData retFileMetaData2 = doPostFile("/api/file" + requestParams, multipartFile2, FileMetaData.class);
        Assert.assertNotNull(retFileMetaData2);

        FileMetaData retFileMetaData3 = doPostFile("/api/file" + requestParams, multipartFile3, FileMetaData.class);
        Assert.assertNotNull(retFileMetaData3);

        List<FileMetaData> retFileMetaDataList = doGetTyped("/api/file" + requestParams, new TypeReference<>() {});
        Assert.assertEquals(3, retFileMetaDataList.size());
    }

    @Test
    public void downloadFile() throws Exception {
        InputStreamWrapper streamWrapper = new InputStreamWrapper(new ByteArrayInputStream("xtz".getBytes()), "txt");
        when(cloudStorageService.getFile(any(), any())).thenReturn(streamWrapper);

        String requestParams = "?relatedEntityId=" + device.getId() + "&relatedEntityType=DEVICE";

        ResultActions resultActions = doGet("/api/file/" +
                "file.txt" + requestParams);
        Assert.assertEquals(200, resultActions.andReturn().getResponse().getStatus());
    }

    @Test
    public void downloadFileThrowsInternalServerError() throws Exception {
        InputStreamWrapper streamWrapper = null;
        when(cloudStorageService.getFile(any(), any())).thenReturn(streamWrapper);
        doGet("/api/file/file.txt").andExpect(status().isInternalServerError());
    }

    @Test
    public void renameFile() throws Exception {
        when(cloudStorageService.upload(any(), any(), any(), any())).thenReturn(true);
        when(cloudStorageService.copyFile(any(), any(), any())).thenReturn(true);
        when(cloudStorageService.delete(any(), any())).thenReturn(true);

        String requestParams = "?relatedEntityId=" + device.getId() + "&relatedEntityType=DEVICE";
        FileMetaData retFileMetaData = doPostFile("/api/file" + requestParams, multipartFile, FileMetaData.class);
        Assert.assertNotNull(retFileMetaData);

        Map<String, String> map = new HashMap<>();
        map.put("relatedEntityId", device.getId().toString());
        map.put("relatedEntityType", "DEVICE");
        map.put("newFileName", "file3.txt");

        doPut("/api/file/file.txt", map).andExpect(status().isOk());
    }
}

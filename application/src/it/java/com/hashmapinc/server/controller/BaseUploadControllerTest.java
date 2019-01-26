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
import com.hashmapinc.server.service.upload.UploadService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseUploadControllerTest extends AbstractControllerTest {

    @MockBean
    protected UploadService uploadService;

    protected MultipartFile multipartFile = null;

    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();
    }

    @Test
    public void getAllFiles() throws Exception {
        List<FileMetaData> list = new ArrayList<>();
        list.add(new FileMetaData("temp", "txt", new Date(), 1));
        list.add(new FileMetaData("temp2", "txt", new Date(), 1));
        when(uploadService.getFileList(any())).thenReturn(list);
        List<FileMetaData> retFileMetaDataList = doGetTyped("/api/file", new TypeReference<List<FileMetaData>>() {
        });
        Assert.assertEquals(2, retFileMetaDataList.size());
    }

    @Test
    public void downloadFile() throws Exception {
        InputStreamWrapper streamWrapper = new InputStreamWrapper(new ByteArrayInputStream("xtz".getBytes()), "txt");
        when(uploadService.downloadFile(any(), any())).thenReturn(streamWrapper);
        ResultActions resultActions = doGet("/api/file/" +
                "image.jpg");
        Assert.assertEquals(200, resultActions.andReturn().getResponse().getStatus());
    }

    @Test
    public void downloadFileThrowsInternalServerError() throws Exception {
        InputStreamWrapper streamWrapper = null;
        when(uploadService.downloadFile(any(), any())).thenReturn(streamWrapper);
        doGet("/api/file/image.jpg").andExpect(status().isInternalServerError());
    }
}

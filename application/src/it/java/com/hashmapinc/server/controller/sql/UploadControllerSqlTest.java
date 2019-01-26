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
package com.hashmapinc.server.controller.sql;

import com.hashmapinc.server.common.data.upload.FileMetaData;
import com.hashmapinc.server.controller.BaseUploadControllerTest;
import com.hashmapinc.server.dao.service.DaoSqlTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.HttpServerErrorException;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DaoSqlTest
public class UploadControllerSqlTest extends BaseUploadControllerTest {

    @Test
    public void uploadFile() throws Exception {
        ClassLoader classLoader = new UploadControllerSqlTest().getClass().getClassLoader();
        multipartFile = new MockMultipartFile("file", new FileInputStream(new File(classLoader.
                getResource("file.txt").getFile())));
        FileMetaData fileMetaData = new FileMetaData("name", "type", new Date(), 1L);
        when(uploadService.uploadFile(any(), any())).thenReturn(fileMetaData);
        FileMetaData retFileMetaData = doPostFile("/api/file", multipartFile, FileMetaData.class);
        Assert.assertNotNull(retFileMetaData);
    }
}

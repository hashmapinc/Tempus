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

import com.hashmapinc.server.common.data.exception.TempusException;
import com.hashmapinc.server.common.data.upload.FileMetaData;
import com.hashmapinc.server.common.data.upload.InputStreamWrapper;
import com.hashmapinc.server.service.computation.CloudStorageService;
import com.hashmapinc.server.service.upload.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class UploadController extends BaseController {

    @Autowired
    private CloudStorageService cloudStorageService;

    @Autowired
    private UploadService uploadService;

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/file")
    @ResponseBody
    public FileMetaData upload(@RequestParam("file") MultipartFile file) throws TempusException {
        try {
            FileMetaData savedFileMetaData = checkNotNull(uploadService.uploadFile(file, getCurrentUser().getTenantId()));
            return savedFileMetaData;
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/file")
    @ResponseBody
    public List<FileMetaData> getFileList(@RequestParam(value = "fileName", required = false) String fileName) throws TempusException {
        try {
            return uploadService.getFileList(getCurrentUser().getTenantId(), fileName);
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/file/{name}")
    @ResponseBody
    public void downloadFile(@PathVariable("name") String name, HttpServletResponse response) throws TempusException {
        try {
            InputStreamWrapper inputStreamWrapper = uploadService.downloadFile(name, getCurrentUser().getTenantId());
            response.addHeader("Content-disposition", "attachment;filename=" + name);
            response.setContentType(inputStreamWrapper.getContentType());
            IOUtils.copy(inputStreamWrapper.getInputStream(), response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @DeleteMapping(value = "/file/{name}")
    @ResponseBody
    public void deleteFile(@PathVariable("name") String name) throws TempusException {
        try {
            uploadService.deleteFile(name, getCurrentUser().getTenantId());
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PutMapping(value = "/file/{oldName}")
    @ResponseBody
    public void changeFileName(@PathVariable("oldName") String oldName, @RequestBody String newFileName) throws TempusException {
        try {
            uploadService.renameFile(oldName, newFileName, getCurrentUser().getTenantId());
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
            throw handleException(e);
        }
    }
}

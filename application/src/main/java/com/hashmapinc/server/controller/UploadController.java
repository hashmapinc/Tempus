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
import com.hashmapinc.server.service.computation.S3BucketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class UploadController extends BaseController {

    @Autowired
    private S3BucketService s3BucketService;

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/file")
    @ResponseBody
    public void upload(@RequestParam("file") MultipartFile file) throws TempusException {
        try {
            if(!s3BucketService.uploadFile(file, getCurrentUser().getTenantId()))
                log.info("multipart file upload unsuccessful !!");
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/files")
    @ResponseBody
    public List<String> getFileList() {
        try {
            s3BucketService.getAllFilesForTenant(getCurrentUser().getTenantId());
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
        }
        return Collections.emptyList();
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @DeleteMapping(value = "/file")
    @ResponseBody
    public void deleteFile(@RequestParam String fileName, @RequestParam String fileType) {
        try {
            s3BucketService.deleteFile(getCurrentUser().getTenantId(), fileName, fileType);
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
        }
    }
}

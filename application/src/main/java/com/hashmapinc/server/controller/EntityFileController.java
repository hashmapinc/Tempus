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

import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.exception.TempusException;
import com.hashmapinc.server.common.data.id.AssetId;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.upload.FileMetaData;
import com.hashmapinc.server.common.data.upload.InputStreamWrapper;
import com.hashmapinc.server.service.computation.CloudStorageService;
import com.hashmapinc.server.service.entityfile.EntityFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class EntityFileController extends BaseController {

    @Autowired
    private CloudStorageService cloudStorageService;

    @Autowired
    private EntityFileService entityFileService;

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/file")
    @ResponseBody
    public FileMetaData uploadFile(@RequestParam("file") MultipartFile file,
                                   @RequestParam Map<String, String> relatedEntityInfo) throws TempusException {
        try {
            String strRelatedEntityId = relatedEntityInfo.get("relatedEntityId");
            String strRelatedEntityType = relatedEntityInfo.get("relatedEntityType");
            EntityId relatedEntityId = createRelatedEntityId(strRelatedEntityId, strRelatedEntityType);
            FileMetaData savedFileMetaData = checkNotNull(entityFileService.uploadFile(file, getCurrentUser().getTenantId(), relatedEntityId));
            return savedFileMetaData;
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/file")
    @ResponseBody
    public List<FileMetaData> getFileList(@RequestParam Map<String, String> relatedEntityInfo) throws TempusException {
        try {
            String strRelatedEntityId = relatedEntityInfo.get("relatedEntityId");
            String strRelatedEntityType = relatedEntityInfo.get("relatedEntityType");
            String fileName = null;
            if(relatedEntityInfo.containsKey("fileName"))
                fileName = relatedEntityInfo.get("fileName");
            EntityId relatedEntityId = createRelatedEntityId(strRelatedEntityId, strRelatedEntityType);
            List<FileMetaData> fileMetaDataList = entityFileService.getFileList(getCurrentUser().getTenantId(), relatedEntityId, fileName);
            return fileMetaDataList;
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/file/{name}")
    @ResponseBody
    public StreamingResponseBody downloadFile(@PathVariable("name") String name,
                                              @RequestParam Map<String, String> relatedEntityInfo,
                                              HttpServletResponse response) throws TempusException {
        try {
            String strRelatedEntityId = relatedEntityInfo.get("relatedEntityId");
            String strRelatedEntityType = relatedEntityInfo.get("relatedEntityType");
            EntityId relatedEntityId = createRelatedEntityId(strRelatedEntityId, strRelatedEntityType);
            InputStreamWrapper inputStreamWrapper = entityFileService.downloadFile(name, getCurrentUser().getTenantId(), relatedEntityId);
            response.addHeader("Content-disposition", "attachment;filename=" + name);
            response.setContentType(inputStreamWrapper.getContentType());
            return outputStream -> {
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = inputStreamWrapper.getInputStream().read(data, 0, data.length)) != -1) {
                    outputStream.write(data, 0, nRead);
                }
            };
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @DeleteMapping(value = "/file/{name}")
    @ResponseBody
    public void deleteFile(@PathVariable("name") String name,
                           @RequestParam Map<String, String> relatedEntityInfo) throws TempusException {
        try {
            String strRelatedEntityId = relatedEntityInfo.get("relatedEntityId");
            String strRelatedEntityType = relatedEntityInfo.get("relatedEntityType");
            EntityId relatedEntityId = createRelatedEntityId(strRelatedEntityId, strRelatedEntityType);
            entityFileService.deleteFile(name, getCurrentUser().getTenantId(), relatedEntityId);
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PutMapping(value = "/file/{oldName}")
    @ResponseBody
    public void renameFile(@PathVariable("oldName") String oldName,
                           @RequestBody Map<String, String> body) throws TempusException {
        try {
            String strRelatedEntityId = body.get("relatedEntityId");
            String strRelatedEntityType = body.get("relatedEntityType");
            String newFileName = body.get("newFileName");
            EntityId relatedEntityId = createRelatedEntityId(strRelatedEntityId, strRelatedEntityType);
            entityFileService.renameFile(oldName, newFileName, getCurrentUser().getTenantId(), relatedEntityId);
        } catch (Exception e) {
            log.info("Exception occurred {}", e);
            throw handleException(e);
        }
    }

    private EntityId createRelatedEntityId(String strRelatedEntity, String strEntityType) {
        EntityId entityId = null;
        switch (EntityType.valueOf(strEntityType)) {
            case DEVICE: entityId = new DeviceId(toUUID(strRelatedEntity));
            break;
            case ASSET: entityId = new AssetId(toUUID(strRelatedEntity));
            break;
        }
        return entityId;
    }
}

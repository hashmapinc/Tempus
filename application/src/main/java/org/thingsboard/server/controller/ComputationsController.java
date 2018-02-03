/**
 * Copyright © 2016-2017 The Thingsboard Authors
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
package org.thingsboard.server.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.exception.ThingsboardException;
import org.thingsboard.server.service.computation.ComputationDiscoveryService;

import java.io.File;

@RestController
@RequestMapping("/api/computations")
public class ComputationsController extends BaseController {

    @Value("${spark.jar_path}")
    private String uploadPath;

    @Autowired
    @Qualifier("uploadComputationDiscoveryService")
    private ComputationDiscoveryService computationDiscoveryService;

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FileInfo upload(@RequestParam("file") MultipartFile file) throws ThingsboardException {
        try {
            String path = uploadPath + File.separator + file.getOriginalFilename();
            File destinationFile = new File(path);
            file.transferTo(destinationFile);

            // Creating service call
            TenantId tenantId = getCurrentUser().getTenantId();
            computationDiscoveryService.onJarUpload(path, tenantId);
            return new FileInfo(file.getOriginalFilename(), path);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public void delete(@RequestParam("fileName") String fileName) throws ThingsboardException {
        String path = uploadPath + File.separator + fileName;
        computationDiscoveryService.deleteJarFile(path);
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/find/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void findAll() throws ThingsboardException {
        computationDiscoveryService.findAll();
    }

    /*@PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/computations", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Computations> getTenantComputations(
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            return checkNotNull(computationDiscoveryService.findTenantComputations(tenantId, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }*/

    @Data
    @AllArgsConstructor
    private static class FileInfo{
        private final String name;
        private final String path;
    }
}

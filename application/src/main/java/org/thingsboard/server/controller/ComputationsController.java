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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.computation.Computations;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.computations.ComputationJobService;
import org.thingsboard.server.dao.computations.ComputationsService;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.exception.ThingsboardErrorCode;
import org.thingsboard.server.exception.ThingsboardException;
import org.thingsboard.server.service.computation.ComputationDiscoveryService;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.thingsboard.server.dao.service.Validator.validateId;

@Slf4j
@RestController
@RequestMapping("/api")
public class ComputationsController extends BaseController {

    @Value("${spark.jar_path}")
    private String uploadPath;

    @Autowired
    private ComputationDiscoveryService computationDiscoveryService;

    @Autowired
    ComputationsService computationsService;

    @Autowired
    ComputationJobService computationJobService;

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/computations/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Computations upload(@RequestParam("file") MultipartFile file) throws ThingsboardException {
        try {
            String path = uploadPath + File.separator + file.getOriginalFilename();
            File destinationFile = new File(path);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, Paths.get(destinationFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
            }
            log.info(" uplaoding computations !!");
            TenantId tenantId = getCurrentUser().getTenantId();
            return computationDiscoveryService.onJarUpload(path, tenantId);
            //return new FileInfo(file.getOriginalFilename(), path);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/computations/{computationId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("computationId") String strComputationId) throws ThingsboardException {
        ComputationId computationId = new ComputationId(toUUID(strComputationId));
        Computations computation = checkComputation(computationsService.findById(computationId));
        List<ComputationJob> computationJobs = checkNotNull(computationJobService.findByComputationId(computationId));
        for (ComputationJob computationJob: computationJobs) {
            //computationJobService.deleteComputationJobById(computationJob.getId());
            actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(), computationJob.getId(), ComponentLifecycleEvent.DELETED);
        }
        computationsService.deleteById(computationId);
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
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
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/computations", method = RequestMethod.GET)
    @ResponseBody
    public List<Computations> getComputations() throws ThingsboardException {
        try {
                log.info(" Fetching computations.");
                TenantId tenantId = getCurrentUser().getTenantId();
                List<Computations> computations = checkNotNull(computationsService.findAllTenantComputationsByTenantId(tenantId));
                computations.stream()
                        .filter(computation -> computation.getTenantId().getId().equals(ModelConstants.NULL_UUID));
                log.info(" returning Computations {} ", computations);
                return computations;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/computations/{computationId}", method = RequestMethod.GET)
    @ResponseBody
    public Computations getComputation(@PathVariable("computationId") String strComputationId) throws ThingsboardException {

        try {
            log.info(" Fetching computations by id.");
            ComputationId computationId = new ComputationId(toUUID(strComputationId));
            //TenantId tenantId = getCurrentUser().getTenantId();
            Computations computations = checkNotNull(computationsService.findById(computationId));
            log.info(" returning Computations by id{} ", computations);
            return computations;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Data
    @AllArgsConstructor
    private static class FileInfo{
        private final String name;
        private final String path;
    }

    protected Computations checkComputation(Computations computation) throws ThingsboardException {
        checkNotNull(computation);
        SecurityUser authUser = getCurrentUser();
        TenantId tenantId = computation.getTenantId();
        validateId(tenantId, "Incorrect tenantId " + tenantId);
        if (authUser.getAuthority() != Authority.SYS_ADMIN) {
            if (authUser.getTenantId() == null ||
                    !tenantId.getId().equals(ModelConstants.NULL_UUID) && !authUser.getTenantId().equals(tenantId)) {
                throw new ThingsboardException("You don't have permission to perform this operation!",
                        ThingsboardErrorCode.PERMISSION_DENIED);

            } else if (tenantId.getId().equals(ModelConstants.NULL_UUID)) {
                computation.setJsonDescriptor(null);
            }
        }
        return computation;
    }
}

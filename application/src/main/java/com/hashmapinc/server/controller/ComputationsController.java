/**
 * Copyright © 2017-2018 Hashmap, Inc
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

import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.computations.ComputationJobService;
import com.hashmapinc.server.dao.computations.ComputationsService;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.exception.TempusErrorCode;
import com.hashmapinc.server.exception.TempusException;
import com.hashmapinc.server.service.computation.ComputationDiscoveryService;
import com.hashmapinc.server.service.security.model.SecurityUser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Slf4j
@RestController
@RequestMapping("/api")
public class ComputationsController extends BaseController {

    @Value("${spark.jar_path}")
    private String uploadPath;

    @Autowired
    private ComputationDiscoveryService computationDiscoveryService;

    @Autowired
    private ComputationsService computationsService;

    @Autowired
    private ComputationJobService computationJobService;

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/computations/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Computations upload(@RequestParam("file") MultipartFile file) throws TempusException {
        try {
            List<String> filesAtDestination = Files.list(Paths.get(this.uploadPath)).map(f -> f.getFileName().toString()).collect(Collectors.toList());
            if(filesAtDestination.contains(file.getOriginalFilename())) {
                throw new TempusException("Cant upload the same computation artifact again. Delete the existing computation first to upload it again" , TempusErrorCode.GENERAL);
            }
            String path = uploadPath + File.separator + file.getOriginalFilename();
            File destinationFile = new File(path);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, Paths.get(destinationFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
            }
            TenantId tenantId = getCurrentUser().getTenantId();
            return computationDiscoveryService.onJarUpload(path, tenantId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/computations/{computationId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("computationId") String strComputationId) throws TempusException, IOException {
        ComputationId computationId = new ComputationId(toUUID(strComputationId));
        Computations computation = checkComputation(computationsService.findById(computationId));
        List<ComputationJob> computationJobs = checkNotNull(computationJobService.findByComputationId(computation.getId()));
        for (ComputationJob computationJob: computationJobs) {
            computationJobService.deleteComputationJobById(computationJob.getId());
            actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(), computationJob.getId(), ComponentLifecycleEvent.DELETED);
        }
        computationsService.deleteById(computationId);
        Files.deleteIfExists(Paths.get(computation.getJarPath()));
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/computations", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Computations> getTenantComputations(
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws TempusException {
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
    public List<Computations> getComputations() throws TempusException {
        try {
                TenantId tenantId = getCurrentUser().getTenantId();
                List<Computations> computations = checkNotNull(computationsService.findAllTenantComputationsByTenantId(tenantId));
                computations.stream()
                        .filter(computation -> computation.getTenantId().getId().equals(ModelConstants.NULL_UUID));
                log.trace(" returning Computations {} ", computations);
                return computations;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/computations/{computationId}", method = RequestMethod.GET)
    @ResponseBody
    public Computations getComputation(@PathVariable("computationId") String strComputationId) throws TempusException {

        try {
            ComputationId computationId = new ComputationId(toUUID(strComputationId));
            Computations computation = checkNotNull(computationsService.findById(computationId));
            log.trace(" returning Computations by id {} ", computation);
            return computation;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    protected Computations checkComputation(Computations computation) throws TempusException {
        checkNotNull(computation);
        SecurityUser authUser = getCurrentUser();
        TenantId tenantId = computation.getTenantId();
        validateId(tenantId, "Incorrect tenantId " + tenantId);
        if (authUser.getAuthority() != Authority.SYS_ADMIN) {
            if (authUser.getTenantId() == null ||
                    !tenantId.getId().equals(ModelConstants.NULL_UUID) && !authUser.getTenantId().equals(tenantId)) {
                throw new TempusException("You don't have permission to perform this operation!",
                        TempusErrorCode.PERMISSION_DENIED);

            } else if (tenantId.getId().equals(ModelConstants.NULL_UUID)) {
                computation.setJsonDescriptor(null);
            }
        }
        return computation;
    }
}

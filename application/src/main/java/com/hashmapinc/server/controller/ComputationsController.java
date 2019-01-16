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

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.RegionType;
import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.computation.*;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.exception.TempusErrorCode;
import com.hashmapinc.server.exception.TempusException;
import com.hashmapinc.server.service.computation.*;
import com.hashmapinc.server.service.security.model.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hashmapinc.server.dao.service.Validator.validateId;
import static com.hashmapinc.server.exception.TempusErrorCode.ITEM_NOT_FOUND;

@Slf4j
@RestController
@RequestMapping("/api")
public class ComputationsController extends BaseController {

    public static final String COMPUTATION_ID = "computationId";

    @Value("${spark.jar_path}")
    private String uploadPath;

    @Value("${computations.lambda.zip_path}")
    private String uploadPathLambda;

    @Autowired
    private ComputationDiscoveryService computationDiscoveryService;

    @Autowired
    private S3BucketService s3BucketService;

    @Autowired
    private KubelessFunctionService kubelessFunctionService;

    @Autowired
    private AWSLambdaFunctionService awsLambdaFunctionService;

    @Autowired
    private AWSLambdaComputationsFunctionService awsLambdaComputationsFunctionService;

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/computations/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Computations upload(@RequestParam("file") MultipartFile file) throws TempusException {

        Computations computation = null;
        try(Stream<Path> filesStream = Files.list(Paths.get(this.uploadPath))) {
            List<String> filesAtDestination = filesStream.map(f -> f.getFileName().toString()).collect(Collectors.toList());
            if(filesAtDestination.contains(file.getOriginalFilename())) {
                throw new TempusException("Cant upload the same computation artifact again. Delete the existing computation first to upload it again" , TempusErrorCode.GENERAL);
            }
            String path = uploadPath + File.separator + file.getOriginalFilename();
            File destinationFile = new File(path);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, Paths.get(destinationFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
            }

            TenantId tenantId = getCurrentUser().getTenantId();
            computation = computationDiscoveryService.onJarUpload(path, tenantId);
            checkNotNull(computation);

            logEntityAction(computation.getId(), computation, getCurrentUser().getCustomerId(),
                    ActionType.ADDED,null);
            return computation;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.COMPUTATION), computation, null,
                    ActionType.ADDED, e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/computations", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Computations addComputations(@RequestBody Computations computation) throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            computation.setTenantId(tenantId);
            Optional<Computations> savedComputation = computationsService.findByTenantIdAndName(tenantId, computation.getName());
            if(!savedComputation.isPresent()) {
                ComputationId computationId = new ComputationId(UUIDs.timeBased());
                computation.setId(computationId);
                computation.getComputationMetadata().setId(computationId);
                if (s3BucketService.uploadKubelessFunction(computation, tenantId)) {
                    computationsService.save(computation);
                    actorService.onComputationStateChange(tenantId, computation.getId(), ComponentLifecycleEvent.CREATED);
                }
            }
            else
                throw new TempusException("Kubeless function upload unsuccessful ", TempusErrorCode.GENERAL);
        } catch (Exception e){
            logEntityAction(emptyId(EntityType.COMPUTATION), computation, null,
                    ActionType.ADDED, e);
            log.info("Exception is : " + e);
            throw handleException(e);
        }

        return computation;
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/computations/lambda", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Computations addComputations(@RequestParam("type") String type,
                                        @RequestParam("functionHandler") String functionHandler,
                                        @RequestParam("functionName") String functionName,
                                        @RequestParam("region") String region,
                                        @RequestParam("runtime") String runtime,
                                        @RequestParam("memorySize") int memorySize,
                                        @RequestParam("timeout") int timeout,
                                        @RequestPart("file") MultipartFile file) throws TempusException {

        try {
            /*log.error("Inside {}", computationStr);

            ObjectMapper mapper = new ObjectMapper();
            Computations computation = mapper.readValue(computationStr, Computations.class);
            log.error(computationStr);*/


            Computations computation = new Computations();
            computation.setName(functionName);
            computation.setTenantId(getCurrentUser().getTenantId());
            computation.setType(ComputationType.valueOf(type));

            if(ComputationType.valueOf(type).equals(ComputationType.LAMBDA)) {
                AWSLambdaComputationMetadata md = new AWSLambdaComputationMetadata();
                md.setFunctionName(functionName);
                md.setFunctionHandler(functionHandler);
                md.setRuntime(runtime);
                md.setTimeout(timeout);
                md.setRegion(RegionType.valueOf(region));
                computation.setComputationMetadata(md);

            }
            log.error("{}", computation);
            final String uploadedFilePath = getUploadedFilePath(file, uploadPathLambda);
            ((AWSLambdaComputationMetadata)computation.getComputationMetadata()).setFilePath(uploadedFilePath);
            return awsLambdaComputationsFunctionService.add(computation, getCurrentUser().getTenantId());
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.COMPUTATION), null, null,
                    ActionType.ADDED, e);
            log.info("Exception is : " + e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @DeleteMapping(value = "/computations/{computationId}")
    @ResponseBody
    public void delete(@PathVariable(COMPUTATION_ID) String strComputationId) throws TempusException, IOException {

        checkParameter(COMPUTATION_ID, strComputationId);
        try
        {
            ComputationId computationId = new ComputationId(toUUID(strComputationId));
            Computations computation = checkComputation(computationsService.findById(computationId));
            List<ComputationJob> computationJobs = checkNotNull(computationJobService.findByComputationId(computation.getId()));
            for (ComputationJob computationJob: computationJobs) {
                computationJobService.deleteComputationJobById(computationJob.getId());
                actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(), computationJob.getId(), ComponentLifecycleEvent.DELETED);
            }

            if (computation.getType() == ComputationType.SPARK) {
                Files.deleteIfExists(Paths.get(((SparkComputationMetadata) computation.getComputationMetadata()).getJarPath()));
                computationsService.deleteById(computation.getId());
            }
            else if (computation.getType() == ComputationType.KUBELESS) {
                actorService.onComputationStateChange(computation.getTenantId(), computation.getId(), ComponentLifecycleEvent.DELETED);
                s3BucketService.deleteKubelessFunction(computation);
            } else if(computation.getType() == ComputationType.LAMBDA) {
                awsLambdaComputationsFunctionService.delete(computation);
            }

            logEntityAction(computationId,computation,getCurrentUser().getCustomerId(),
                    ActionType.DELETED, null, strComputationId);
        }
        catch (Exception e) {
            logEntityAction(emptyId(EntityType.COMPUTATION),
                    null,
                    null,
                    ActionType.DELETED, e, strComputationId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/computations", params = {"limit"})
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
    @GetMapping(value = "/computations")
    @ResponseBody
    public List<Computations> getComputations() throws TempusException {
        try {
                TenantId tenantId = getCurrentUser().getTenantId();
                List<Computations> computations = checkNotNull(computationsService.findAllTenantComputationsByTenantId(tenantId));
                computations = computations.stream()
                        .filter(computation -> !computation.getTenantId().getId().equals(ModelConstants.NULL_UUID)).collect(Collectors.toList());
                Iterator itr = computations.iterator();
                while(itr.hasNext()){
                    Computations computation = (Computations) itr.next();
                    if(computation.getType() == ComputationType.KUBELESS &&
                            (!kubelessFunctionService.checkFunction(computation))) {
                        itr.remove();
                    }
                }
                log.info(" returning Computations {} ", computations);
                return computations;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @GetMapping(value = "/computations/{computationId}")
    @ResponseBody
    public Computations getComputation(@PathVariable(COMPUTATION_ID) String strComputationId) throws TempusException {

        try {
            ComputationId computationId = new ComputationId(toUUID(strComputationId));
            Computations computation = checkNotNull(computationsService.findById(computationId));
            if(computation.getType() == ComputationType.KUBELESS
                    && !kubelessFunctionService.checkFunction(computation)) {
                throw new TempusException("Kubeless fuction not present in kubernetes cluster ", ITEM_NOT_FOUND);
            }
            log.info(" returning Computations by id {} ", computation);
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
        if (authUser.getAuthority() != Authority.SYS_ADMIN &&
                authUser.getTenantId() == null ||
                !tenantId.getId().equals(ModelConstants.NULL_UUID) && !authUser.getTenantId().equals(tenantId)) {
                throw new TempusException("You don't have permission to perform this operation!",
                        TempusErrorCode.PERMISSION_DENIED);
        }
        return computation;
    }
}

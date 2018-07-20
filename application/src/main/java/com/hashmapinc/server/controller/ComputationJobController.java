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

import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import com.hashmapinc.server.exception.TempusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hashmapinc.server.exception.TempusErrorCode.ITEM_NOT_FOUND;

@Slf4j
@RestController
@RequestMapping("/api")
public class ComputationJobController extends BaseController{

    public static final String COMPUTATION_ID = "ComputationId ";
    public static final String NOT_FOUND_SUFFIX = " wasn't found! ";

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/computations/{computationid}/jobs")
    @ResponseBody
    public ComputationJob saveComputationJob(@PathVariable("computationid") String strComputationId,
                                             @RequestBody ComputationJob source) throws TempusException {
        ComputationJob computationJob = null;
        if(!validateComputationId(strComputationId)){
            throw new TempusException(COMPUTATION_ID + strComputationId + NOT_FOUND_SUFFIX, ITEM_NOT_FOUND);
        }
        try {
            boolean created = source.getId() == null;
            source.setTenantId(getCurrentUser().getTenantId());
            source.setComputationId(new ComputationId(toUUID(strComputationId)));
            computationJob = checkNotNull(computationJobService.saveComputationJob(source));
            actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(),
                    computationJob.getId(), created ? ComponentLifecycleEvent.CREATED : ComponentLifecycleEvent.UPDATED);

            logEntityAction(computationJob.getId(), computationJob,
                    getCurrentUser().getCustomerId(),
                    source.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

            return computationJob;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.COMPUTATION_JOB), computationJob,
                    null,
                    source.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @DeleteMapping(value = "/computations/jobs/{computationJobId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteComputationJob(@PathVariable("computationJobId") String strComputationJobId) throws TempusException {
        checkParameter("computationJobId", strComputationJobId);
        try {
            ComputationJobId computationJobId = new ComputationJobId(toUUID(strComputationJobId));
            ComputationJob computationJob = checkComputationJob(computationJobService.findComputationJobById(computationJobId));
            computationJobService.deleteComputationJobById(computationJobId);
            actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(), computationJob.getId(), ComponentLifecycleEvent.DELETED);

            logEntityAction(computationJob.getId(),computationJob,
                    getCurrentUser().getCustomerId(), ActionType.DELETED,null);

        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.COMPUTATION_JOB),null,
                    null, ActionType.DELETED, e, strComputationJobId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @GetMapping(value = "/computations/{computationId}/jobs/{computationJobId}")
    @ResponseStatus(value = HttpStatus.OK)
    public ComputationJob getComputationJob(@PathVariable("computationJobId") String strComputationJobId,
                                  @PathVariable("computationId") String strComputationId) throws TempusException {
        checkParameter("computationJobId", strComputationJobId);
        if(!validateComputationId(strComputationId)){
            throw new TempusException(COMPUTATION_ID + strComputationId + NOT_FOUND_SUFFIX,ITEM_NOT_FOUND);
        }
        try {
            ComputationJobId computationJobId = new ComputationJobId(toUUID(strComputationJobId));
            return checkComputationJob(computationJobService.findComputationJobById(computationJobId));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @PostMapping(value = "/computations/{computationId}/jobs/{computationJodId}/activate")
    @ResponseStatus(value = HttpStatus.OK)
    public void activateCompuationJobById(@PathVariable("computationJodId") String strComputationJobId,
                                   @PathVariable("computationId") String strComputationId) throws TempusException {
        checkParameter("strComputationJobId", strComputationJobId);
        checkParameter("strComputationId", strComputationId);
        if(!validateComputationId(strComputationId)){
            throw new TempusException(COMPUTATION_ID + strComputationId + NOT_FOUND_SUFFIX,ITEM_NOT_FOUND);
        }
        try {
            ComputationJobId computationJobId = new ComputationJobId(toUUID(strComputationJobId));
            ComputationId computationId = new ComputationId(toUUID(strComputationId));
            ComputationJob computationJob = checkComputationJob(computationJobService.findComputationJobById(computationJobId));
            computationJobService.activateComputationJobById(computationJobId);
            actorService.onComputationJobStateChange(computationJob.getTenantId(), computationId, computationJob.getId(), ComponentLifecycleEvent.ACTIVATED);

            logEntityAction(computationJob.getId(), computationJob,
                    getCurrentUser().getCustomerId(), ActionType.ACTIVATED, null);

        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.COMPUTATION_JOB),null,
                    null, ActionType.ACTIVATED, e, strComputationId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @PostMapping(value = "/computations/{computationId}/jobs/{computationJodId}/suspend")
    @ResponseStatus(value = HttpStatus.OK)
    public void suspendComputationJobById(@PathVariable("computationJodId") String strComputationJobId,
                                  @PathVariable("computationId") String strComputationId) throws TempusException {
        checkParameter("strComputationJobId", strComputationJobId);
        checkParameter("strComputationId", strComputationId);
        if(!validateComputationId(strComputationId)){
            throw new TempusException(COMPUTATION_ID + strComputationId + NOT_FOUND_SUFFIX,ITEM_NOT_FOUND);
        }
        try {
            ComputationJobId computationJobId = new ComputationJobId(toUUID(strComputationJobId));
            ComputationId computationId = new ComputationId(toUUID(strComputationId));
            ComputationJob computationJob = checkComputationJob(computationJobService.findComputationJobById(computationJobId));
            computationJobService.suspendComputationJobById(computationJobId);
            actorService.onComputationJobStateChange(computationJob.getTenantId(), computationId, computationJob.getId(), ComponentLifecycleEvent.SUSPENDED);

            logEntityAction(computationJob.getId(), computationJob,
                    getCurrentUser().getCustomerId(), ActionType.SUSPENDED, null);

        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.COMPUTATION_JOB),null,
                    null, ActionType.SUSPENDED, e, strComputationJobId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @GetMapping(value = "/computations/{computationId}/jobs")
    @ResponseStatus(value = HttpStatus.OK)
    public List<ComputationJob> getComputationJobs(@PathVariable("computationId") String strComputationId) throws TempusException {
        checkParameter("computationId", strComputationId);
        if(!validateComputationId(strComputationId)){
            throw new TempusException(COMPUTATION_ID + strComputationId + NOT_FOUND_SUFFIX,ITEM_NOT_FOUND);
        }
        try {
            ComputationId computationId = new ComputationId(toUUID(strComputationId));
            return computationJobService.findByComputationId(computationId);
            } catch (Exception e) {
            throw handleException(e);
        }
    }

    private boolean validateComputationId(String computationId){
        return isComputationExists(computationId);
    }

    private boolean isComputationExists(String computationId) {
        return computationsService.findById(new ComputationId(toUUID(computationId))) != null;
    }
}

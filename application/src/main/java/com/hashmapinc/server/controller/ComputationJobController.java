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
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hashmapinc.server.dao.computations.ComputationJobService;
import com.hashmapinc.server.dao.computations.ComputationsService;
import com.hashmapinc.server.exception.TempusException;

import java.util.List;

import static com.hashmapinc.server.exception.TempusErrorCode.ITEM_NOT_FOUND;

@Slf4j
@RestController
@RequestMapping("/api")
public class ComputationJobController extends BaseController{

    @Autowired
    ComputationJobService computationJobService;

    @Autowired
    ComputationsService computationsService;

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/computations/{computationid}/jobs", method = RequestMethod.POST)
    @ResponseBody
    public ComputationJob saveComputationJob(@PathVariable("computationid") String strComputationId,
                                             @RequestBody ComputationJob source) throws TempusException {
        if(!validateComputationId(strComputationId)){
            throw new TempusException("ComputationId " + strComputationId + " wasn't found! ",ITEM_NOT_FOUND);
        }
        try {
            boolean created = source.getId() == null;
            source.setTenantId(getCurrentUser().getTenantId());
            source.setComputationId(new ComputationId(toUUID(strComputationId)));
            ComputationJob computationJob = checkNotNull(computationJobService.saveComputationJob(source));
            actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(),
                    computationJob.getId(), created ? ComponentLifecycleEvent.CREATED : ComponentLifecycleEvent.UPDATED);
            return computationJob;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/computations/jobs/{computationJobId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteComputationJob(@PathVariable("computationJobId") String strComputationJobId) throws TempusException {
        checkParameter("computationJobId", strComputationJobId);
        try {
            ComputationJobId computationJobId = new ComputationJobId(toUUID(strComputationJobId));
            ComputationJob computationJob = checkComputationJob(computationJobService.findComputationJobById(computationJobId));
            computationJobService.deleteComputationJobById(computationJobId);
            actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(), computationJob.getId(), ComponentLifecycleEvent.DELETED);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/computations/{computationId}/jobs/{computationJobId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public ComputationJob getComputationJob(@PathVariable("computationJobId") String strComputationJobId,
                                  @PathVariable("computationId") String strComputationId) throws TempusException {
        checkParameter("computationJobId", strComputationJobId);
        if(!validateComputationId(strComputationId)){
            throw new TempusException("ComputationId " + strComputationId + " wasn't found!",ITEM_NOT_FOUND);
        }
        try {
            ComputationJobId computationJobId = new ComputationJobId(toUUID(strComputationJobId));
            return checkComputationJob(computationJobService.findComputationJobById(computationJobId));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/computations/{computationId}/jobs/{computationJodId}/activate", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void activateCompuationJobById(@PathVariable("computationJodId") String strComputationJobId,
                                   @PathVariable("computationId") String strComputationId) throws TempusException {
        checkParameter("strComputationJobId", strComputationJobId);
        checkParameter("strComputationId", strComputationId);
        if(!validateComputationId(strComputationId)){
            throw new TempusException("ComputationId " + strComputationId + " wasn't found!",ITEM_NOT_FOUND);
        }
        try {
            ComputationJobId computationJobId = new ComputationJobId(toUUID(strComputationJobId));
            ComputationId computationId = new ComputationId(toUUID(strComputationId));
            ComputationJob computationJob = checkComputationJob(computationJobService.findComputationJobById(computationJobId));
            computationJobService.activateComputationJobById(computationJobId);
            actorService.onComputationJobStateChange(computationJob.getTenantId(), computationId, computationJob.getId(), ComponentLifecycleEvent.ACTIVATED);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/computations/{computationId}/jobs/{computationJodId}/suspend", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void suspendComputationJobById(@PathVariable("computationJodId") String strComputationJobId,
                                  @PathVariable("computationId") String strComputationId) throws TempusException {
        checkParameter("strComputationJobId", strComputationJobId);
        checkParameter("strComputationId", strComputationId);
        if(!validateComputationId(strComputationId)){
            throw new TempusException("ComputationId " + strComputationId + " wasn't found!",ITEM_NOT_FOUND);
        }
        try {
            ComputationJobId computationJobId = new ComputationJobId(toUUID(strComputationJobId));
            ComputationId computationId = new ComputationId(toUUID(strComputationId));
            ComputationJob computationJob = checkComputationJob(computationJobService.findComputationJobById(computationJobId));
            computationJobService.suspendComputationJobById(computationJobId);
            actorService.onComputationJobStateChange(computationJob.getTenantId(), computationId, computationJob.getId(), ComponentLifecycleEvent.SUSPENDED);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/computations/{computationId}/jobs", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public List<ComputationJob> getComputationJobs(@PathVariable("computationId") String strComputationId) throws TempusException {
        checkParameter("computationId", strComputationId);
        if(!validateComputationId(strComputationId)){
            throw new TempusException("ComputationId " + strComputationId + " wasn't found!",ITEM_NOT_FOUND);
        }
        try {
            ComputationId computationId = new ComputationId(toUUID(strComputationId));
            List<ComputationJob> computationJobs = computationJobService.findByComputationId(computationId);
            return computationJobs;
            } catch (Exception e) {
            throw handleException(e);
        }
    }

    private boolean validateComputationId(String computationId){
        if(computationsService.findById(new ComputationId(toUUID(computationId))) == null){
            return false;
        }
        return true;
    }
}

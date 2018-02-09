package org.thingsboard.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleState;
import org.thingsboard.server.common.data.plugin.PluginMetaData;
import org.thingsboard.server.dao.computations.ComputationJobService;
import org.thingsboard.server.exception.ThingsboardException;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
public class ComputationJobController extends BaseController{

    @Autowired
    ComputationJobService computationJobService;

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/computations/{computationId}/jobs", method = RequestMethod.POST)
    @ResponseBody
    public ComputationJob saveComputationJob(@RequestBody ComputationJob source,
                                             @PathVariable("computationId") String strComputationId) throws ThingsboardException {
        checkParameter("computationId", strComputationId);
        log.error("HMDC strComputationId " + strComputationId);
        try {
            boolean created = source.getId() == null;
            source.setTenantId(getCurrentUser().getTenantId());
            source.setComputationId(new ComputationId(toUUID(strComputationId.trim())));
            ComputationJob computationJob = checkNotNull(computationJobService.saveComputationJob(source));
            actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(),
                    computationJob.getId(), created ? ComponentLifecycleEvent.CREATED : ComponentLifecycleEvent.UPDATED);
            return computationJob;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/computationJob/{computationJobId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteComputationJob(@PathVariable("computationJobId") String strComputationJobId) throws ThingsboardException {
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
    @RequestMapping(value = "/computations/{computationId}/jobs/{computaionJodId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public ComputationJob getComputationJob(@PathVariable("computationJobId") String strComputationJobId,
                                  @PathVariable("computationId") String strComputationId) throws ThingsboardException {
        checkParameter("computationJobId", strComputationJobId);
        try {
            ComputationJobId computationJobId = new ComputationJobId(toUUID(strComputationJobId));
            ComputationJob computationJob = checkComputationJob(computationJobService.findComputationJobById(computationJobId));
            return computationJob;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/computations/{computationId}/jobs", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public List<ComputationJob> getComputationJobs(@PathVariable("computationId") String strComputationId) throws ThingsboardException {
        checkParameter("computationId", strComputationId);
        try {
            ComputationId computationId = new ComputationId(toUUID(strComputationId));
            List<ComputationJob> computationJobs = computationJobService.findByComputationId(computationId);
            return computationJobs;
            } catch (Exception e) {
            throw handleException(e);
        }
    }
}

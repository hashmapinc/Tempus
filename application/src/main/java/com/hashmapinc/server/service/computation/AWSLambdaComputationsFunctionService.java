package com.hashmapinc.server.service.computation;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.actors.service.ActorService;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.ComputationType;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import com.hashmapinc.server.dao.computations.ComputationJobService;
import com.hashmapinc.server.dao.computations.ComputationsService;
import com.hashmapinc.server.exception.TempusErrorCode;
import com.hashmapinc.server.exception.TempusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AWSLambdaComputationsFunctionService implements ComputationsFunctionService {

    @Value("${computations.lambda.zip_path}")
    private String uploadPath;

    @Autowired
    private ComputationsService computationsService;

    @Autowired
    private ActorService actorService;

    @Autowired
    private ComputationJobService computationJobService;

    @Override
    public Computations add(Computations computation, TenantId tenantId) throws Exception {
        try {
            computation.setTenantId(tenantId);
            Optional<Computations> savedComputation = computationsService.findByTenantIdAndName(tenantId, computation.getName());
            if(savedComputation.isEmpty()) {
                ComputationId computationId = new ComputationId(UUIDs.timeBased());
                computation.setId(computationId);
                computation.getComputationMetadata().setId(computationId);
                computationsService.save(computation);
                actorService.onComputationStateChange(tenantId, computation.getId(), ComponentLifecycleEvent.CREATED);
            }
            else
                throw new TempusException("Lambda function upload unsuccessful ", TempusErrorCode.GENERAL);
        } catch (Exception e){
            throw e;
        }

        return computation;
    }

    @Override
    public Computations delete(Computations computation) throws Exception {
        try
        {
            List<ComputationJob> computationJobs = computationJobService.findByComputationId(computation.getId());
            if(computationJobs != null){
                for (ComputationJob computationJob: computationJobs) {
                    computationJobService.deleteComputationJobById(computationJob.getId());
                    actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(), computationJob.getId(), ComponentLifecycleEvent.DELETED);
                }
            }
            actorService.onComputationStateChange(computation.getTenantId(), computation.getId(), ComponentLifecycleEvent.DELETED);
            computationsService.deleteById(computation.getId());
        }
        catch (Exception e) {
            throw e;
        }
        return computation;
    }

    private boolean isZip(Path path) throws IOException {
        File file = path.toFile();
        return file.getCanonicalPath().endsWith(".zip") && file.canRead();
    }
}

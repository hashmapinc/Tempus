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
package com.hashmapinc.server.service.computation;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.msg.computation.ComputationRequestCompiled;
import com.hashmapinc.server.service.computation.classloader.RuntimeJavaCompiler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hashmapinc.server.dao.computations.ComputationsService;
import com.hashmapinc.server.service.computation.annotation.AnnotationsProcessor;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service("uploadComputationDiscoveryService")
@Slf4j
public class UploadComputationDiscoveryService implements ComputationDiscoveryService{

    @Autowired
    private ComputationsService computationsService;

    private RuntimeJavaCompiler compiler;

    private boolean isJar(Path jarPath) throws IOException {
        File file = jarPath.toFile();
        return file.getCanonicalPath().endsWith(".jar") && file.canRead();
    }

    public Computations onFileCreate(File file, TenantId tenantId) {
        log.debug("File {} is created", file.getAbsolutePath());
        return processComponent(file, tenantId);
    }

    private Computations processComponent(File file, TenantId tenantId) {
        Computations savedComputations = null;
        Path j = file.toPath();
        try{
            if(isJar(j)){
                AnnotationsProcessor processor = new AnnotationsProcessor(j, compiler);
                List<ComputationRequestCompiled> c = processor.processAnnotations();
                if(c != null && !c.isEmpty()) {
                    for (ComputationRequestCompiled computationRequestCompiled : c) {
                        Computations computations = new Computations();
                        computations.setTenantId(tenantId);
                        computations.setJarPath(j.toString());
                        computations.setMainClass(computationRequestCompiled.getMainClazz());
                        computations.setJsonDescriptor(computationRequestCompiled.getConfigurationDescriptor());
                        String args = Arrays.toString(computationRequestCompiled.getArgs());
                        computations.setArgsformat(args);
                        computations.setArgsType(computationRequestCompiled.getArgsType());
                        computations.setJarName(j.getFileName().toString());
                        computations.setName(computationRequestCompiled.getName());
                        Optional<Computations> persisted = computationsService.findByTenantIdAndName(tenantId, computations.getName());
                        if (!persisted.isPresent()) {
                            ComputationId computationId = new ComputationId(UUIDs.timeBased());
                            computations.setId(computationId);
                            savedComputations = computationsService.save(computations);
                        } else {
                            computations.setId(persisted.get().getId());
                            savedComputations = computationsService.save(computations);
                        }
                    }
                }
            }

        } catch (IOException e) {
            log.error("Error while accessing jar to scan dynamic components", e);
        }
        return savedComputations;
    }

    @Override
    public TextPageData<Computations> findTenantComputations(TenantId tenantId, TextPageLink pageLink) {
        return computationsService.findTenantComputations(tenantId, pageLink);
    }

    @Override
    public Computations onJarUpload(String path, TenantId tenantId) {
        return onFileCreate(new File(path), tenantId);
    }

    @PreDestroy
    public void destroy(){
        log.debug("Cleaning up resources from Computation discovery service");
        try {
            if(compiler != null){
                compiler.destroy();
            }
        } catch (Exception e) {
            log.error("Error while cleaning up resources for Directory Polling service");
        }
    }
}

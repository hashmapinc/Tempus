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
package org.thingsboard.server.service.computation;

import com.datastax.driver.core.utils.UUIDs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.computation.Computations;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.msg.computation.ComputationRequestCompiled;
import org.thingsboard.server.dao.computations.ComputationsService;
import org.thingsboard.server.dao.plugin.PluginService;
import org.thingsboard.server.service.component.ComponentDiscoveryService;
import org.thingsboard.server.service.computation.annotation.AnnotationsProcessor;
import org.thingsboard.server.service.computation.classloader.RuntimeJavaCompiler;
import org.thingsboard.server.utils.MiscUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service("uploadComputationDiscoveryService")
@Slf4j
public class UploadComputationDiscoveryService implements ComputationDiscoveryService{

    private static final Executor executor = Executors.newSingleThreadExecutor();
    private final String PLUGIN_CLAZZ = "org.thingsboard.server.extensions.spark.computation.plugin.SparkComputationPlugin";

    @Value("${spark.jar_path}")
    private String libraryPath;

    @Value("${spark.polling_interval}")
    private Long pollingInterval;

    @Autowired
    private ComponentDiscoveryService componentDiscoveryService;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ComputationsService computationsService;

    private RuntimeJavaCompiler compiler;
    private FileAlterationMonitor monitor;
    private Map<String, Set<String>> processedJarsSources = new HashMap<>();

    @PostConstruct
    public void init() {
        log.warn("Initializing bean Directory Computation discovery.");
        Assert.hasLength(libraryPath, MiscUtils.missingProperty("spark.jar_path"));
        Assert.notNull(pollingInterval, MiscUtils.missingProperty("spark.polling_interval"));
        this.compiler = new RuntimeJavaCompiler();
        discoverDynamicComponents();
    }

    public void discoverDynamicComponents(){
        final FileSystem fs = FileSystems.getDefault();
        try {
            List<Path> jars = Files.walk(fs.getPath(libraryPath)).collect(Collectors.toList());
            for(Path j: jars){
                if(isJar(j)) {
                    AnnotationsProcessor processor = new AnnotationsProcessor(j, compiler);
                    List<ComputationRequestCompiled> c = processor.processAnnotations();
                    if(c != null && !c.isEmpty()) {
                        for (ComputationRequestCompiled computationRequestCompiled : c) {
                            Computations computations = new Computations();
                            computations.setJarPath(j.toString());
                            computations.setMainClass(computationRequestCompiled.getMainClazz());
                            computations.setJsonDescriptor(computationRequestCompiled.getConfigurationDescriptor());
                            String args = Arrays.toString(computationRequestCompiled.getArgs());
                            computations.setArgsformat(args);
                            computations.setJarName(j.getFileName().toString());
                            computations.setName(computationRequestCompiled.getName());
                            Computations persistedComputations = computationsService.findByName(computations.getName());
                            if (persistedComputations == null) {
                                ComputationId computationId = new ComputationId(UUIDs.timeBased());
                                computations.setId(computationId);
                                computationsService.save(computations);
                            } else {
                                computations.setId(persistedComputations.getId());
                                computationsService.save(computations);
                            }
                        }
                    }
                }
            }
            //componentDiscoveryService.updateActionsForPlugin(compiledActions, PLUGIN_CLAZZ);
        } catch (IOException e) {
            log.error("Error while reading jars from directory.", e);
        }
    }

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
                        Computations persistedComputations = computationsService.findByName(computations.getName());
                        if (persistedComputations == null) {
                            ComputationId computationId = new ComputationId(UUIDs.timeBased());
                            computations.setId(computationId);
                            savedComputations = computationsService.save(computations);
                        } else {
                            computations.setId(persistedComputations.getId());
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

    public void onFileDelete(File file){
        computationsService.deleteByJarName(file.getName());
    }

    @Override
    public List<Computations> findAll() {
        return computationsService.findAll();
    }

    @Override
    public void deleteJarFile(String path){
        File file = new File(path);
        if(file.delete()){
            this.onFileDelete(file);
        }
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
            if (monitor != null) {
                monitor.stop();
            }
            if(compiler != null){
                compiler.destroy();
            }
        } catch (Exception e) {
            log.error("Error while cleaning up resources for Directory Polling service");
        }
    }
}

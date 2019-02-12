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
package com.hashmapinc.server.service.computation;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.computation.ComputationType;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.SparkComputationMetadata;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.msg.computation.ComputationRequestCompiled;
import com.hashmapinc.server.dao.computations.ComputationsService;
import com.hashmapinc.server.dao.tenant.TenantService;
import com.hashmapinc.server.exception.TempusApplicationException;
import com.hashmapinc.server.service.CloudStorageServiceUtils;
import com.hashmapinc.server.service.computation.annotation.AnnotationsProcessor;
import com.hashmapinc.server.service.computation.classloader.RuntimeJavaCompiler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service("uploadComputationDiscoveryService")
@Slf4j
public class
UploadComputationDiscoveryService implements ComputationDiscoveryService{

    @Autowired
    private ComputationsService computationsService;

    @Value("${aws.spark_jars_bucket}")
    private String sparkJarsBucket;

    @Autowired
    private MinioService minioService;

    @Autowired
    private TenantService tenantService;

    private RuntimeJavaCompiler compiler;

    @Override
    public TextPageData<Computations> findTenantComputations(TenantId tenantId, TextPageLink pageLink) {
        return computationsService.findTenantComputations(tenantId, pageLink);
    }

    @Override
    public void deleteComputationAndJar(Computations computation) throws TempusApplicationException {
        deleteJar(computation);
        computationsService.deleteById(computation.getId());
    }

    @Override
    public Computations onJarUpload(String path, TenantId tenantId) throws TempusApplicationException {
        return onFileCreate(new File(path), tenantId);
    }

    @PreDestroy
    public void destroy() {
        log.debug("Cleaning up resources from Computation discovery service");
        try {
            if (compiler != null) {
                compiler.destroy();
            }
        } catch (Exception e) {
            log.error("Error while cleaning up resources for Directory Polling service");
        }
    }

    private boolean isJar(Path jarPath) throws IOException {
        File file = jarPath.toFile();
        return file.getCanonicalPath().endsWith(".jar") && file.canRead();
    }

    private Computations onFileCreate(File file, TenantId tenantId) throws TempusApplicationException {
        log.debug("File {} is created", file.getAbsolutePath());
        ImmutablePair<String, String> storedFileDetails = useCloudStorage() ? uploadToCloud(file, tenantId) : new ImmutablePair<>(file.getName(), file.toPath().toString());
        Computations computations = processComponent(file, storedFileDetails, tenantId);
        if (useCloudStorage())
            file.delete();

        return computations;
    }

    private ImmutablePair uploadToCloud(File file, TenantId tenantId) throws TempusApplicationException {
        try {
            String objectName = getObjectNameForTenant(file.getName(), tenantId);
            minioService.upload(sparkJarsBucket, objectName, new FileInputStream(file), "application/zip");
            String objectUrl = minioService.getObjectUrl(sparkJarsBucket, objectName);
            return new ImmutablePair<>(objectName, objectUrl);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | XmlPullParserException e) {
            log.error("Error while uploading file [{}] to cloud. Exception is: {}", file.getName(), e.getMessage());
            throw new TempusApplicationException(e);
        }
    }

    private void deleteJar(Computations computation) throws TempusApplicationException {
        SparkComputationMetadata computationMetadata = (SparkComputationMetadata) computation.getComputationMetadata();
        try {
            if (useCloudStorage()) {
                minioService.delete(sparkJarsBucket, computationMetadata.getJarName());
            } else {
                Files.deleteIfExists(Paths.get(computationMetadata.getJarPath()));
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | XmlPullParserException e) {
            log.error("Error while deleting file [{}]. Exception is: {}", computationMetadata.getJarPath(), e.getMessage());
            throw new TempusApplicationException(e);
        }
    }

    private String getObjectNameForTenant(String fileName, TenantId tenantId) {
        String folder = CloudStorageServiceUtils.createBucketName(tenantService.findTenantById(tenantId));
        return CloudStorageServiceUtils.createObjectName(fileName, folder);
    }

    private boolean useCloudStorage() {
        return !sparkJarsBucket.isEmpty();
    }

    private Computations processComponent(File file, ImmutablePair<String, String> storedFileDetails, TenantId tenantId) {
        Computations savedComputations = null;
        Path j = file.toPath();
        try{
            if(isJar(j)){
                AnnotationsProcessor processor = new AnnotationsProcessor(j);
                List<ComputationRequestCompiled> c = processor.processAnnotations();
                if(c != null && !c.isEmpty()) {
                    for (ComputationRequestCompiled computationRequestCompiled : c) {
                        Computations computations = new Computations();
                        computations.setName(computationRequestCompiled.getName());
                        String args = Arrays.toString(computationRequestCompiled.getArgs());
                        computations.setTenantId(tenantId);
                        SparkComputationMetadata sparkComputationMetadata = new SparkComputationMetadata();

                        sparkComputationMetadata.setMainClass(computationRequestCompiled.getMainClazz());
                        sparkComputationMetadata.setArgsformat(args);
                        sparkComputationMetadata.setJarPath(storedFileDetails.getRight());
                        sparkComputationMetadata.setArgsType(computationRequestCompiled.getArgsType());
                        sparkComputationMetadata.setJarName(storedFileDetails.getLeft());
                        sparkComputationMetadata.setJsonDescriptor(computationRequestCompiled.getConfigurationDescriptor());

                        computations.setComputationMetadata(sparkComputationMetadata);
                        computations.setType(ComputationType.SPARK);

                        Optional<Computations> persisted = computationsService.findByTenantIdAndName(tenantId, computations.getName());
                        if (!persisted.isPresent()) {
                            ComputationId computationId = new ComputationId(UUIDs.timeBased());
                            sparkComputationMetadata.setId(computationId);
                            computations.setId(computationId);
                            savedComputations = computationsService.save(computations);
                        } else {
                            computations.setId(persisted.get().getId());
                            sparkComputationMetadata.setId(persisted.get().getId());
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
}

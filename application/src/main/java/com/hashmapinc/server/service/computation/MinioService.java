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

import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.KubelessComputationMetadata;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.tenant.TenantService;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class MinioService implements S3BucketService {

    public static final String MINIO_EXECPTION = "Minio Execption {} ";
    public static final int BUFFER_SIZE = 16384;
    public static final String SHA_256 = "SHA-256";
    private static volatile  MinioClient minioClient;

    private static final String FUNCTION_URL_FORMAT = "%s/%s.%s";
    private static final String AMAZON_S3_URL = "https://s3.amazonaws.com";
    private Base64.Encoder encoder = Base64.getEncoder();

    @Value("${kubeless.minio_access_key}")
    private String minioAccesskey;

    @Value("${kubeless.minio_secret_key}")
    private String minioSecretKey;

    @Autowired
    private TenantService tenantService;

    public boolean uploadKubelessFunction(Computations computation, TenantId tenantId) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException{
        boolean status = false;
        try {
            KubelessComputationMetadata md = (KubelessComputationMetadata) computation.getComputationMetadata();

            if (!checksum(md))
                return false;

            MinioClient client = getMinioClientInstance();

            Tenant tenant = tenantService.findTenantById(tenantId);
            String bucketName = getBucketName(tenant, tenantId.toString());

            boolean isPresent = client.bucketExists(bucketName);
            if(!isPresent) {
                client.makeBucket(bucketName);
            }
            byte[] functionContent = md.getFunctionContent().getBytes();
            ByteArrayInputStream byteArrayInputStream =  new ByteArrayInputStream(functionContent);
            String functionObjectUrl = createFunctionObjectUrl(computation);

            client.putObject(bucketName, functionObjectUrl, byteArrayInputStream, md.getFunctionContentType());
            return true;
        }
        catch(MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }

        return status;
    }

    private String getBucketName(Tenant tenant, String tenantId) {
        return tenant.getName().replaceAll(" ", "-").toLowerCase().concat(tenantId);
    }

    public List<String> getAllKubelessFunctionsForTenant(TenantId tenantId) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            Tenant tenant = tenantService.findTenantById(tenantId);
            String bucketName = getBucketName(tenant, tenantId.toString());
            boolean isPresent = client.bucketExists(bucketName);
            List<String> functionNames = new ArrayList<>();
            if(isPresent){
                Iterable<Result<Item>> functionObjects = client.listObjects(bucketName);
                for (Result<Item> result : functionObjects) {
                    Item item = result.get();
                    functionNames.add(item.name);
                }
            }
            return functionNames;
        }
        catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return Collections.emptyList();
    }

    public String getFunctionObjByTenantAndUrl(TenantId tenantId, Computations computation) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            String functionObjectUrl = createFunctionObjectUrl(computation);
            MinioClient client = getMinioClientInstance();
            Tenant tenant = tenantService.findTenantById(tenantId);
            String bucketName = getBucketName(tenant, tenantId.toString());
            client.statObject(bucketName, functionObjectUrl);

            InputStream stream = client.getObject(bucketName, functionObjectUrl);

            byte[] buf = new byte[BUFFER_SIZE];
            int bytesRead;
            StringBuilder functionContent = new StringBuilder();
            while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
                functionContent.append(new String(buf, 0, bytesRead, StandardCharsets.UTF_8));
            }
            stream.close();
            return functionContent.toString();
        } catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return null;
    }

    @Override
    public boolean deleteKubelessFunction(Computations computation) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            String functionObjectUrl = createFunctionObjectUrl(computation);
            MinioClient client = getMinioClientInstance();
            Tenant tenant = tenantService.findTenantById(computation.getTenantId());
            String bucketName = getBucketName(tenant, computation.getTenantId().toString());
            client.removeObject(bucketName, functionObjectUrl);
            return true;
        } catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return false;
    }

    private boolean checksum(KubelessComputationMetadata md) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(md.getFunctionContent().getBytes());
            String encodedChecksum = encoder.encodeToString(hash);
            if(md.getChecksum().contentEquals(encodedChecksum)) {
                log.info("Check is same.");
                return true;
            }

        } catch(NoSuchAlgorithmException e) {
            log.info("Execption occured : {}", e);
        }
        return false;
    }

    private String createFunctionObjectUrl(Computations computation) {
        KubelessComputationMetadata md = (KubelessComputationMetadata) computation.getComputationMetadata();
        String functionName = md.getFunction();
        String type = md.getFunctionContentType();
        String functionObjUrl;
        String folderName = computation.getName().replace(" ","-").toLowerCase();
        if(type.contains("zip"))
            functionObjUrl = String.format(FUNCTION_URL_FORMAT, folderName, functionName, "zip");
        else
            functionObjUrl = String.format(FUNCTION_URL_FORMAT, folderName, functionName, type);
        return functionObjUrl;
    }

    private MinioClient getMinioClientInstance() throws InvalidEndpointException, InvalidPortException
    {
        if (minioClient == null) {
            synchronized (MinioClient.class) {
                if (minioClient==null) {
                    minioClient = new MinioClient(AMAZON_S3_URL, minioAccesskey, minioSecretKey);
                    log.info("Minio connection successfull!");
                }
            }
        }
        return minioClient;
    }
    
}

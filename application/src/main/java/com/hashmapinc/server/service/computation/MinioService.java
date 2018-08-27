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

import com.hashmapinc.server.common.data.computation.ComputationRequest;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.KubelessComputationMetadata;
import com.hashmapinc.server.common.data.id.TenantId;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class MinioService implements ComputationFunctionService{

    public static final String MINIO_EXECPTION = "Minio Execption ";
    private volatile static MinioClient minioClient;

    private static final String FUNCTION_URL = "%s/%s.%s";
    private Base64.Decoder decoder = Base64.getDecoder();
    private Base64.Encoder encoder = Base64.getEncoder();

    @Value("${kubeless.minio_url}")
    private static String minioUrl;

    @Value("${kubeless.minio_url}")
    private String minioAccesskey;

    @Value("${kubeless.minio_url}")
    private String minioSecretKey;

    public boolean uploadKubelessFunction(ComputationRequest computationRequest, TenantId tenantId) throws Exception{
        boolean status = false;
        try {
            Computations computation = computationRequest.getComputation();
            MinioClient client = getMinioClientInstance();

            boolean isExist = client.bucketExists(tenantId.toString());
            if(!isExist) {
                client.makeBucket(tenantId.toString());
            }
            byte[] functionContent = decoder.decode(computationRequest.getFunctionContent());
            ByteArrayInputStream byteArrayInputStream =  new ByteArrayInputStream(functionContent);
            KubelessComputationMetadata md = (KubelessComputationMetadata) computation.getComputationMetadata();
            String objUrl = createObjectUrl(md);

            client.putObject(tenantId.toString(), objUrl, byteArrayInputStream, md.getFunctionContentType());

        }
        catch(MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }

        return status;
    }

    private MinioClient getMinioClientInstance() throws InvalidEndpointException, InvalidPortException
    {
        if (minioClient == null) {
            synchronized (MinioClient.class) {
                if (minioClient==null)
                    minioClient = new MinioClient(minioUrl, minioAccesskey, minioSecretKey);
            }
        }
        return minioClient;
    }

    public List<String> getAllKubelessFunctionsForTenant(TenantId tenantId) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            boolean isExist = client.bucketExists(tenantId.toString());
            List<String> functionNames = new ArrayList<>();
            if(isExist){
                Iterable<Result<Item>> functionObjects = client.listObjects(tenantId.toString());
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

    public String getFunctionByTenantAndName(TenantId tenantId, String name) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            client.statObject(tenantId.toString(), name);

            InputStream stream = client.getObject(tenantId.toString(), name);

            byte[] buf = new byte[16384];
            int bytesRead;
            StringBuilder stringBuilder = new StringBuilder();
            while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
                stringBuilder.append(new String(buf, 0, bytesRead, StandardCharsets.UTF_8));
            }
            stream.close();
            byte[] encodedFunc = encoder.encode(stringBuilder.toString().getBytes());
            return new String(encodedFunc);
        } catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return null;
    }

    private String createObjectUrl(KubelessComputationMetadata md) {
        String functionName = md.getFunction();
        String type = md.getFunctionContentType();
        return String.format(FUNCTION_URL, functionName, functionName, type);
    }
}

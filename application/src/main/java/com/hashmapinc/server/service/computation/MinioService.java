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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class MinioService implements CloudStorageService {

    public static final String MINIO_EXECPTION = "Minio Execption {} ";
    public static final int BUFFER_SIZE = 16384;
    public static final String SHA_256 = "SHA-256";
    private static volatile  MinioClient minioClient;

    private static final String FILE_URL_FORMAT = "%s/%s.%s";
    private static final String AMAZON_S3_URL = "https://s3.amazonaws.com";
    private Base64.Encoder encoder = Base64.getEncoder();

    @Value("${aws.access_key}")
    private String awsAccesskey;

    @Value("${aws.secret_key}")
    private String awsSecretKey;

    @Autowired
    private TenantService tenantService;

    @Override
    public boolean upload(String bucketName, String objectUrl, byte[] content) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            if(!client.bucketExists(bucketName)) {
                client.makeBucket(bucketName);
            }

            ByteArrayInputStream byteArrayInputStream =  new ByteArrayInputStream(content);
            client.putObject(bucketName, objectUrl, byteArrayInputStream, "");
        } catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return false;
    }

    @Override
    public boolean delete(String bucketName, String objectUrl) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            if (client.bucketExists(bucketName))
                client.removeObject(bucketName, objectUrl);
            return true;
        } catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return false;
    }

    @Override
    public List<Item> getAllFiles(String bucketName, String type) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            List<Item> items = new ArrayList<>();
            if(client.bucketExists(bucketName)){
                Iterable<Result<Item>> resourceObjects = client.listObjects(bucketName, type, true);
                for (Result<Item> result : resourceObjects) {
                     items.add(result.get());
                }
            }
            return items;
        }
        catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getFile(String bucketName, String objectUrl) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            client.statObject(bucketName, objectUrl);

            InputStream stream = client.getObject(bucketName, objectUrl);

            byte[] buf = new byte[BUFFER_SIZE];
            int bytesRead;
            StringBuilder fileContent = new StringBuilder();
            while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
                fileContent.append(new String(buf, 0, bytesRead, StandardCharsets.UTF_8));
            }
            stream.close();
            return fileContent.toString();
        } catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return null;
    }

    private MinioClient getMinioClientInstance() throws InvalidEndpointException, InvalidPortException
    {
        if (minioClient == null) {
            synchronized (MinioClient.class) {
                if (minioClient==null) {
                    minioClient = new MinioClient(AMAZON_S3_URL, awsAccesskey, awsSecretKey);
                    log.info("Minio connection successful!");
                }
            }
        }
        return minioClient;
    }
    
}

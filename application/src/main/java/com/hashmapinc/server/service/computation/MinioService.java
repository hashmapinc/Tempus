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

import com.hashmapinc.server.common.data.upload.InputStreamWrapper;
import com.hashmapinc.server.dao.tenant.TenantService;
import io.minio.MinioClient;
import io.minio.ObjectStat;
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

import java.io.IOException;
import java.io.InputStream;
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
    private static volatile  MinioClient minioClient;

    private static final String AMAZON_S3_URL = "https://s3.amazonaws.com";
    private Base64.Encoder encoder = Base64.getEncoder();

    @Value("${aws.access_key}")
    private String awsAccesskey;

    @Value("${aws.secret_key}")
    private String awsSecretKey;

    @Autowired
    private TenantService tenantService;

    @Override
    public boolean upload(String bucketName, String objectName, InputStream inputStream, String contentType) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            if(!client.bucketExists(bucketName)) {
                client.makeBucket(bucketName);
            }
            client.putObject(bucketName, objectName, inputStream, contentType);
            return true;
        } catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return false;
    }

    @Override
    public boolean delete(String bucketName, String objectName) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            if (client.bucketExists(bucketName))
                client.removeObject(bucketName, objectName);
            return true;
        } catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return false;
    }

    @Override
    public List<Item> getAllFiles(String bucketName, String prefix) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            List<Item> items = new ArrayList<>();
            if(client.bucketExists(bucketName)){
                Iterable<Result<Item>> resourceObjects = client.listObjects(bucketName, prefix);
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
    public InputStreamWrapper getFile(String bucketName, String objectName) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            ObjectStat stat = client.statObject(bucketName, objectName);
            return new InputStreamWrapper(client.getObject(bucketName, objectName), stat.contentType());
        } catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return null;
    }

    @Override
    public String getObjectUrl(String bucketName, String objectName) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            return client.getObjectUrl(bucketName, objectName);
        } catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return null;
    }

    @Override
    public boolean copyFile(String bucketName, String srcObjectUrl, String destObjectUrl) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient client = getMinioClientInstance();
            if (client.bucketExists(bucketName)) {
                client.copyObject(bucketName, srcObjectUrl, bucketName, destObjectUrl);
                return true;
            }
        } catch (MinioException e) {
            log.info(MINIO_EXECPTION, e);
        }
        return false;
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

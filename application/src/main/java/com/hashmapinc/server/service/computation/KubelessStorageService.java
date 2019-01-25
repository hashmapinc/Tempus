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
import com.hashmapinc.server.common.data.upload.InputStreamWrapper;
import com.hashmapinc.server.common.data.upload.StorageTypes;
import com.hashmapinc.server.dao.tenant.TenantService;
import com.hashmapinc.server.service.CloudStorageServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@Slf4j
public class KubelessStorageService {

    private Base64.Encoder encoder = Base64.getEncoder();

    static final String SHA_256 = "SHA-256";

    public static final int BUFFER_SIZE = 16384;

    @Autowired
    MinioService minioService;

    @Autowired
    TenantService tenantService;


    public boolean uploadFunction(Computations computation) throws Exception {
        KubelessComputationMetadata md = (KubelessComputationMetadata) computation.getComputationMetadata();
        if (!checksum(md))
            return false;
        Tenant tenant = tenantService.findTenantById(computation.getTenantId());
        String bucketName = CloudStorageServiceUtils.createBucketName(tenant);
        String objectUrl = CloudStorageServiceUtils.createObjectUrl(computation.getName(), StorageTypes.FUNCTIONS);
        ByteArrayInputStream byteArrayInputStream =  new ByteArrayInputStream(md.getFunctionContent().getBytes());
        return minioService.upload(bucketName, objectUrl, byteArrayInputStream, md.getFunctionContentType());
    }

    public String getFunction(Computations computation) throws Exception{
        Tenant tenant = tenantService.findTenantById(computation.getTenantId());
        String bucketName = CloudStorageServiceUtils.createBucketName(tenant);
        String objectUrl = CloudStorageServiceUtils.createObjectUrl(computation.getName(), StorageTypes.FUNCTIONS);

        InputStreamWrapper inputStreamWrapper = minioService.getFile(bucketName, objectUrl);
        InputStream stream = inputStreamWrapper.getInputStream();
        byte[] buf = new byte[BUFFER_SIZE];
        int bytesRead;
        StringBuilder fileContent = new StringBuilder();
        while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
            fileContent.append(new String(buf, 0, bytesRead, StandardCharsets.UTF_8));
        }
        stream.close();
        return fileContent.toString();
    }

    public boolean deleteFunction(Computations computation) throws Exception{
        Tenant tenant = tenantService.findTenantById(computation.getTenantId());
        String bucketName = CloudStorageServiceUtils.createBucketName(tenant);
        String objectUrl = CloudStorageServiceUtils.createObjectUrl(computation.getName(), StorageTypes.FUNCTIONS);
        return minioService.delete(bucketName, objectUrl);
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
            log.info("Exception occurred : {}", e);
        }
        return false;
    }
}

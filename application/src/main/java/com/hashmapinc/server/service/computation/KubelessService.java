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

import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.KubelessComputationMetadata;
import com.hashmapinc.server.dao.tenant.TenantService;
import com.hashmapinc.server.service.CloudStorageServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@Slf4j
public class KubelessService {

    private Base64.Encoder encoder = Base64.getEncoder();
    public static final String SHA_256 = "SHA-256";

    @Autowired
    MinioService minioService;

    @Autowired
    TenantService tenantService;

    public boolean uploadKubelessFunction(Computations computation) throws Exception {
        KubelessComputationMetadata md = (KubelessComputationMetadata) computation.getComputationMetadata();
        if (!checksum(md))
            return false;
        String bucketName = CloudStorageServiceUtils.createBucketName(computation.getTenantId(), tenantService);
        String objectUrl = CloudStorageServiceUtils.createObjectUrl(computation.getName(), "function");
        return minioService.upload(bucketName, objectUrl, md.getFunctionContent().getBytes());
    }

    public String getFunctionObjByTenantAndUrl(Computations computation) throws Exception{
        String bucketName = CloudStorageServiceUtils.createBucketName(computation.getTenantId(), tenantService);
        String objectUrl = CloudStorageServiceUtils.createObjectUrl(computation.getName(), "function");
        return minioService.getFile(bucketName, objectUrl);
    }

    public boolean deleteKubelessFunction(Computations computation) throws Exception{
        String bucketName = CloudStorageServiceUtils.createBucketName(computation.getTenantId(), tenantService);
        String objectUrl = CloudStorageServiceUtils.createObjectUrl(computation.getName(), "function");
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

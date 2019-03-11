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
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.computation.ComputationType;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.KubelessComputationMetadata;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.upload.InputStreamWrapper;
import com.hashmapinc.server.dao.computations.ComputationsService;
import com.hashmapinc.server.dao.tenant.TenantService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.annotation.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("kubeless-storage-test")
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = KubelessStorageServiceUnitTest.class, loader = SpringBootContextLoader.class)
@ComponentScan({"com.hashmapinc.server.service.computation","com.hashmapinc.server.service.minio"})
public class KubelessStorageServiceUnitTest {
    @Autowired
    TenantService tenantService;

    @Autowired
    CloudStorageService cloudStorageService;

    @Autowired
    KubelessStorageService kubelessStorageService;

    private Tenant tenant = new Tenant();

    private Computations computation = null;

    private Base64.Encoder encoder = Base64.getEncoder();

    static final String SHA_256 = "SHA-256";

    @Before
    public  void before() {
        MockitoAnnotations.initMocks(this);
        tenant.setTitle("Demo Tenant");
        tenant.setId(new TenantId(UUIDs.timeBased()));
        computation = createComputation();
    }

    @Test
    public void uploadFunction() throws Exception {
        when(tenantService.findTenantById(any())).thenReturn(tenant);
        when(cloudStorageService.upload(any(), any(), any(), any())).thenReturn(true);
        Assert.assertEquals( true, kubelessStorageService.uploadFunction(computation));
    }

    @Test
    public void getFunction() throws Exception {
        KubelessComputationMetadata md = (KubelessComputationMetadata) computation.getComputationMetadata();
        InputStream inputStream = new ByteArrayInputStream(md.getFunctionContent().getBytes());
        InputStreamWrapper inputStreamWrapper = new InputStreamWrapper(inputStream, "content-type");
        when(tenantService.findTenantById(any())).thenReturn(tenant);
        when(cloudStorageService.getFile(any(), any())).thenReturn(inputStreamWrapper);
        String functionContent = kubelessStorageService.getFunction(computation);
        Assert.assertEquals(md.getFunctionContent(), functionContent);
    }

    @Test
    public void deleteFunction() throws Exception {
        when(tenantService.findTenantById(any())).thenReturn(tenant);
        when(cloudStorageService.delete(any(), any())).thenReturn(true);
        Assert.assertEquals( true, kubelessStorageService.deleteFunction(computation));
    }

    private Computations createComputation() {
        Computations computation = new Computations();
        computation.setName("kubeless-comp");
        computation.setTenantId(tenant.getId());
        computation.setType(ComputationType.KUBELESS);
        computation.setId(new ComputationId(UUIDs.timeBased()));
        computation.setComputationMetadata(createKubelessMetadata());
        return computation;
    }

    private KubelessComputationMetadata createKubelessMetadata() {
        KubelessComputationMetadata md = new KubelessComputationMetadata();
        md.setFunction("func1");
        md.setRuntime("java1.8");
        md.setFunctionContent("Content");
        md.setFunctionContentType("txt");
        md.setHandler("handler");
        md.setNamespace("default");
        md.setTimeout("180");
        md.setChecksum(getChecksum("Content"));
        return md;
    }

    private String getChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(content.getBytes());
            return encoder.encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception : " + e);
        }
        return "";
    }
}

@Profile("kubeless-storage-test")
@Configuration
class KubelessStorageTestConfiguration {

    @Bean
    @Primary
    public TenantService tenantService() {
        return Mockito.mock(TenantService.class);
    }

    @Bean
    @Primary
    public CloudStorageService cloudStorageService() {
        return Mockito.mock(CloudStorageService.class);
    }

    @Bean
    @Primary
    public ComputationsService computationsService() {
        return Mockito.mock(ComputationsService.class);
    }

}

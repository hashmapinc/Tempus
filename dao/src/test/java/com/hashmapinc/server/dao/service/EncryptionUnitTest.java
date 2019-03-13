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
package com.hashmapinc.server.dao.service;


import com.hashmapinc.server.dao.encryption.EncryptionServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;


@RunWith(MockitoJUnitRunner.class)
public class EncryptionUnitTest{

    private static final String aesKey = "ABCRFGTHDKHLMNTF";

    @InjectMocks
    private EncryptionServiceImpl encryptionService;

    @Before
    public void setup(){
        ReflectionTestUtils.setField(encryptionService, "key", aesKey);
    }


    @Test()
    public void testEncrypt(){
        String plainText = "B4azA3QJl1E64D/4g/GIKA==";
        String encryptText = encryptionService.encrypt(plainText);
        Assert.assertNotEquals(plainText,encryptText);
    }

    @Test()
    public void testEncryptWithEmptyPlainText(){
        String plainText = "";
        String encryptText = encryptionService.encrypt(plainText);
        Assert.assertEquals(plainText,encryptText);
    }

    @Test()
    public void testDecrypt() throws Exception{
        String plainText = "B4azA3QJl1E64D/4g/GIKA==";
        String encryptText = encryptionService.encrypt(plainText);
        String decryptedText = encryptionService.decrypt(encryptText);
        Assert.assertEquals(plainText,decryptedText);
    }


    @Test()
    public void testDecryptWithEmptyEncryptedText(){
        String plainText = "";
        String encryptText = encryptionService.encrypt(plainText);
        String decryptedText = encryptionService.decrypt(encryptText);
        Assert.assertEquals(plainText,decryptedText);
    }
}


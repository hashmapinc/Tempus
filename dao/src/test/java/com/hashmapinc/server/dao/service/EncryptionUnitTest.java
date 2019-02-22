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

import org.junit.Assert;
import org.junit.Test;

public class EncryptionUnitTest{

    private static final String aesKey = "ABCRFGTHDKHLMNTF";

    @Test()
    public void testEncrypt(){
        String plainText = "B4azA3QJl1E64D/4g/GIKA==";
        String encryptText = Encryption.encrypt(plainText,aesKey);
        Assert.assertNotEquals(plainText,encryptText);
    }

    @Test()
    public void testDecrypt() throws Exception{
        String plainText = "B4azA3QJl1E64D/4g/GIKA==";
        String encryptText = Encryption.encrypt(plainText,aesKey);
        String decryptedText = Encryption.decrypt(encryptText,aesKey);
        Assert.assertEquals(plainText,decryptedText);
    }
}

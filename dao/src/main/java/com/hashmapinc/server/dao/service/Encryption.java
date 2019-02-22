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


import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
public class Encryption {

    public static final String UTF_8 = "UTF8";
    private static final String ALGORITHM_STR = "AES/ECB/PKCS5Padding";



    public static String encrypt(String plainText , String key) {
        try {
            if(plainText == null)
                return null;
            Cipher cipher = Cipher.getInstance(ALGORITHM_STR);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] cipherText = cipher.doFinal(plainText.getBytes());
            String encryptedString = new String(Base64.getEncoder().encode(cipherText), UTF_8);
            return encryptedString;
        } catch (Exception e) {
            log.trace(e.getMessage());
        }
        return null;
    }

    public static String decrypt(String encryptedText , String key) {
        try {
            if(encryptedText == null)
                return null;
            Cipher cipher = Cipher.getInstance(ALGORITHM_STR);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] cipherText = Base64.getDecoder().decode(encryptedText);
            String decryptedString = new String(cipher.doFinal(cipherText), UTF_8);
            return decryptedString;
        } catch (Exception e) {
            log.trace(e.getMessage());
        }
        return null;
    }
}

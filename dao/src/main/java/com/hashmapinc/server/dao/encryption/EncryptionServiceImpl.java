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
package com.hashmapinc.server.dao.encryption;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static com.hashmapinc.server.dao.model.ModelConstants.ALGORITHM_STR;
import static com.hashmapinc.server.dao.model.ModelConstants.UTF_8;

@Slf4j
@Service
public class EncryptionServiceImpl implements EncryptionService{

    @Value("${encryption.aes_key}")
    private String key;

    public String encrypt(String plainText) {
        try {
            if(StringUtils.isEmpty(plainText))
                return plainText;
            Cipher cipher = Cipher.getInstance(ALGORITHM_STR);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] cipherText = cipher.doFinal(plainText.getBytes());
            return new String(Base64.getEncoder().encode(cipherText), UTF_8);
        } catch (Exception e) {
            log.trace(e.getMessage());
        }
        return null;
    }

    public String decrypt(String encryptedText) {
        try {
            if(StringUtils.isEmpty(encryptedText))
                return encryptedText;
            Cipher cipher = Cipher.getInstance(ALGORITHM_STR);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] cipherText = Base64.getDecoder().decode(encryptedText);
            return new String(cipher.doFinal(cipherText), UTF_8);
        } catch (Exception e) {
            log.trace(e.getMessage());
        }
        return null;
    }
}

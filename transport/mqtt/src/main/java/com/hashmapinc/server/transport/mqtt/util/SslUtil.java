/**
 * Copyright © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server.transport.mqtt.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;
import com.hashmapinc.server.dao.EncryptionUtil;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;


@Slf4j
public class SslUtil {

    private SslUtil() {
    }

    public static String getX509CertificateString(X509Certificate cert)
            throws CertificateEncodingException, IOException {
        Base64Utils.encodeToString(cert.getEncoded());
        return EncryptionUtil.trimNewLines(Base64Utils.encodeToString(cert.getEncoded()));
    }

    public static String getX509CertificateString(javax.security.cert.X509Certificate cert)
            throws javax.security.cert.CertificateEncodingException, IOException {
        Base64Utils.encodeToString(cert.getEncoded());
        return EncryptionUtil.trimNewLines(Base64Utils.encodeToString(cert.getEncoded()));
    }
}

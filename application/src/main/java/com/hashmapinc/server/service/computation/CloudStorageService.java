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
import io.minio.messages.Item;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface CloudStorageService {
    boolean upload(String bucketName, String objectName, InputStream inputStream, String contentType) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException;

    boolean delete(String bucketName, String objectName) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException;

    List<Item> getAllFiles(String bucketName, String prefix) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException;

    InputStreamWrapper getFile(String bucketName, String objectName) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException;

    String getObjectUrl(String bucketName, String objectName) throws XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException, IOException;
}

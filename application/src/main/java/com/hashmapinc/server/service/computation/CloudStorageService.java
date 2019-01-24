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

import io.minio.messages.Item;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface CloudStorageService {
    boolean upload(String bucketName, String objectUrl, byte[] content) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException;
    boolean delete(String bucketName, String objectUrl) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException;
    List<Item> getAllFiles(String bucketName, String type) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException;
    String getFile(String bucketName, String objectUrl) throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException;
}

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
package com.hashmapinc.kubeless;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InputParserUtility {

    private List<String> keysToValidate = Arrays.asList("id", "ts", "ds");

    public InputParserUtility() {
    }


    public boolean validateJson(String json) throws IOException {
        return validateJson(json, Collections.EMPTY_LIST);
    }

    public boolean validateJson(String json, List<String> additionalKeys) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.readTree(json);
        List<String> fieldNames = new ArrayList<>();
        jsonNode.fieldNames().forEachRemaining(fieldNames::add);

        AtomicBoolean validationStatus = new AtomicBoolean(true);

        Stream.concat(keysToValidate.stream(), additionalKeys.stream())
                .collect(Collectors.toList())
                .forEach(key -> {
                    if (key.contentEquals("ts") || key.contentEquals("ds")) {
                        boolean timestampPresent = fieldNames.contains("ts");
                        boolean depthstampPresent = fieldNames.contains("ds");
                        if (timestampPresent && depthstampPresent) {
                            validationStatus.set(false);
                        }
                        if (!timestampPresent && !depthstampPresent) {
                            validationStatus.set(false);
                        }
                    } else if (!fieldNames.contains(key)) {
                        validationStatus.set(false);
                    }
                });
        return validationStatus.get();
    }
}

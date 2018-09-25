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

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
package com.hashmapinc.server.common.data.kubernetes;


import lombok.*;

import java.util.List;
import java.util.Map;

@Data
public class ReplicaSetConfig {

    private String replicaName;
    private String podTemplateName;
    private Map<String, String> replicaLabels;
    private Map<String, String> selectorLabels;
    private Map<String, String> podTemplateLabels;
    private int replica;
    private String containerName;
    private String image;
    private Map<Integer, String> ports;
    private Map<String, String> envVariables;
    private List<String> commands;
}

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
package com.hashmapinc.server.utils;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class KubelessConnectionCache {
    private static Map<String, Object> instances = new HashMap();

    private KubelessConnectionCache() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(String name, boolean clusterModeEnabled, String kublessConfigPath, Object... initargs) {
        Class<T> c;
        try {
            c = (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException e1) {
            log.info("ClassNotFoundException: " + name);
            return null;
        }

        if (!instances.containsKey(name)) {
            T inst;
            try {
                Class<?>[] parameterTypes = new Class<?>[initargs.length];
                for (int i = 0; i < initargs.length; i++) {
                    parameterTypes[i] = initargs[i].getClass();
                }
                if (clusterModeEnabled) {
                    ApiClient client = Config.fromCluster();
                    Configuration.setDefaultApiClient(client);
                } else {
                    //ApiClient client = Config.fromConfig("/home/himanshu/.kube/config"); // kublessConfigPath
                    ApiClient client = Config.fromToken("https://961027CB7DFC6FE7E8A4938AFC302DA5.sk1.us-east-1.eks.amazonaws.com",
                            "eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJla3MtYWRtaW4tdG9rZW4tczQ5NGYiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZWtzLWFkbWluIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiNmM0ODMxMDUtYmFiNS0xMWU4LWI5YmUtMGU3NDlhNzRjYTBjIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50Omt1YmUtc3lzdGVtOmVrcy1hZG1pbiJ9.JqPW7c1aSosdLHkbO2pmqmQzQ4KMj84AXmKoBTZxH3IpS7zeU-J6CJDkwA-kwpFX9pNw_uyraVaSfL_F8gWVJx3I8WB9q6d3aSgqe223Ai2H0v32FhCP5_rIGJ6uY0SMo9kEODuUhLPVymRgDiuroP0bxWfgNJvyQ2l7y7fZrq06DMjoAoFtt20ihN26bvMRf31BgWh-ptJcazirdVznw_h_qNrKQyoBAr48azjII6mVKOHU8bSllHHvBaGH0ZVMCd3fMbAi0_ihbVCX51Au1izdjKcFErlLk2Ol_pitg5jJ4FjQhquoZfrrBmXYW8Wzb20LVuxU9K-c5HbXgpKM3Q",
                    false);
                    Configuration.setDefaultApiClient(client);
                }
                inst = c.getConstructor(parameterTypes).newInstance(initargs);
            } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException
                    | InstantiationException | IllegalAccessException | IOException e) {
                log.info("Can't get or call constructor");
                return null;
            }
            instances.put(name, inst);
        }

        return c.cast(instances.get(name));
    }
}

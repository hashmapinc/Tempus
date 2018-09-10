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
                    ApiClient client = Config.fromConfig("/home/himanshu/.kube/aws.secure.kubeconfig"); // kublessConfigPath
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

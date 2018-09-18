package com.hashmapinc.server.common.data.kubernetes;


import lombok.*;

import java.util.List;
import java.util.Map;

@Data
public class ReplicaSetConfig {

    private String namespace;
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

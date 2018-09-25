package com.hashmapinc.server.common.data.kubernetes;

import lombok.Data;

@Data
public class ReplicaSetStatus {

    Integer replica;
    Integer ready;
    Integer inProgress;
    Integer crashed;
}

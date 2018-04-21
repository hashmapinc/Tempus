package com.hashmapinc.server.actors.application;

import com.hashmapinc.server.common.data.id.ComputationJobId;
import lombok.Getter;

public class ComputationJobDeleteMessage {
    @Getter
    private final ComputationJobId computationJobId;

    public ComputationJobDeleteMessage(ComputationJobId computationJobId) {
        this.computationJobId = computationJobId;
    }
}

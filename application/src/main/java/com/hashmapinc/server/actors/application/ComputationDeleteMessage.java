package com.hashmapinc.server.actors.application;

import lombok.Getter;
import com.hashmapinc.server.common.data.id.ComputationId;

public class ComputationDeleteMessage {
    @Getter
    private final ComputationId computationId;

    public ComputationDeleteMessage(ComputationId computationId) {
        this.computationId = computationId;
    }
}

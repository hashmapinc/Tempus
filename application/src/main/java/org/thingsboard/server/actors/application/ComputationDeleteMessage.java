package org.thingsboard.server.actors.application;

import lombok.Getter;
import org.thingsboard.server.common.data.id.ComputationId;

public class ComputationDeleteMessage {
    @Getter
    private final ComputationId computationId;

    public ComputationDeleteMessage(ComputationId computationId) {
        this.computationId = computationId;
    }
}

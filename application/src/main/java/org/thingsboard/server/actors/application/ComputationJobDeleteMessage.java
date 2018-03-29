package org.thingsboard.server.actors.application;

import lombok.Getter;
import org.thingsboard.server.common.data.id.ComputationJobId;

public class ComputationJobDeleteMessage {
    @Getter
    private final ComputationJobId computationJobId;

    public ComputationJobDeleteMessage(ComputationJobId computationJobId) {
        this.computationJobId = computationJobId;
    }
}

package org.thingsboard.server.actors.computation;

import org.thingsboard.server.actors.shared.ActorTerminationMsg;
import org.thingsboard.server.common.data.id.ComputationJobId;

public class ComputationJobTerminationMsg extends ActorTerminationMsg<ComputationJobId> {

    public ComputationJobTerminationMsg(ComputationJobId id) {
        super(id);
    }
}

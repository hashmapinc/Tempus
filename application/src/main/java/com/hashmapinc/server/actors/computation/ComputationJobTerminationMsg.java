package com.hashmapinc.server.actors.computation;

import com.hashmapinc.server.actors.shared.ActorTerminationMsg;
import com.hashmapinc.server.common.data.id.ComputationJobId;

public class ComputationJobTerminationMsg extends ActorTerminationMsg<ComputationJobId> {

    public ComputationJobTerminationMsg(ComputationJobId id) {
        super(id);
    }
}

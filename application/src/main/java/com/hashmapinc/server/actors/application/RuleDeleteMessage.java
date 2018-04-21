package com.hashmapinc.server.actors.application;

import lombok.Getter;
import com.hashmapinc.server.common.data.id.RuleId;

public class RuleDeleteMessage {
    @Getter
    private final RuleId ruleId;

    public RuleDeleteMessage(RuleId ruleId) {
        this.ruleId = ruleId;
    }


}

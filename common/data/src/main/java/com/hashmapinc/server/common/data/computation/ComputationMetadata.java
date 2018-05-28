package com.hashmapinc.server.common.data.computation;

import com.hashmapinc.server.common.data.BaseData;
import com.hashmapinc.server.common.data.id.ComputationId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ComputationMetadata extends BaseData<ComputationId> {

    public ComputationMetadata() {
        super();
    }

    public ComputationMetadata(ComputationId id) {
        super(id);
    }

    public ComputationMetadata(ComputationMetadata md){
        super(md);
    }
}

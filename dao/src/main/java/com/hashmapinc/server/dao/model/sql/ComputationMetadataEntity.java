package com.hashmapinc.server.dao.model.sql;

import com.hashmapinc.server.common.data.computation.ComputationMetadata;
import com.hashmapinc.server.dao.model.BaseSqlEntity;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ComputationMetadataEntity<T extends ComputationMetadata> extends BaseSqlEntity<T>{

    @Id
    @OneToOne(mappedBy="computation")
    private ComputationsEntity computation;


}

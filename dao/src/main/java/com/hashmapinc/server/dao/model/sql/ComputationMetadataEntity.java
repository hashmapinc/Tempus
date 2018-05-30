/**
 * Copyright © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hashmapinc.server.dao.model.sql;

import com.hashmapinc.server.common.data.computation.ComputationMetadata;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ComputationMetadataEntity<T extends ComputationMetadata> extends BaseSqlEntity<T> {

    //@Transient
    //private static final long serialVersionUID = -4873737406462009030L;

    /*@Id
    @Column(name = "computation_id")
    private String computation_id;
    @Id
    @Column(name="computation_id")
    @GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign",
            parameters= @Parameter(name="property", value="computation"))
    private String computationId;

    @OneToOne
    @PrimaryKeyJoinColumn
    private ComputationsEntity computation;

    public String getComputationId() {
        return computationId;
    }

    public void setComputationId(String computationId) {
        this.computationId = computationId;
    }

    public ComputationsEntity getComputation() {
        return computation;
    }

    public void setComputation(ComputationsEntity computation) {
        this.computation = computation;
    }*/

}

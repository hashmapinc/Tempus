package com.hashmapinc.server.dao.model.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeDefinitionCompositeKey implements Serializable {
    @Transient
    private static final long serialVersionUID = -4089175869616037516L;

    private String name;
    private String dataModelObjectId;
}

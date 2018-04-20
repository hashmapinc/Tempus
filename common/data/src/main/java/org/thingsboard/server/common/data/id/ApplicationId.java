/**
 * Copyright © 2016-2017 Hashmap, Inc
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
package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

public class ApplicationId extends UUIDBased implements EntityId{

    private static final long serialVersionUID = 1L;

    @JsonCreator
    public ApplicationId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static ApplicationId fromString(String applicationId) {
        return new ApplicationId(UUID.fromString(applicationId));
    }


    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.APPLICATION;
    }
}

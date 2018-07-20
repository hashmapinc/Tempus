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
package com.hashmapinc.kubeless.models.triggers;

import com.google.gson.annotations.SerializedName;
import com.hashmapinc.kubeless.models.V1beta1AbstractType;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class V1beta1HttpTrigger extends V1beta1AbstractType<V1beta1HttpTriggerSpec> {

    @SerializedName("kind")
    protected String kind = "HTTPTrigger";

    public V1beta1HttpTrigger kind(String kind) {
        this.kind = kind;
        return this;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}

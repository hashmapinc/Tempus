/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.kubeless.models;

import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.models.V1ObjectMeta;

import java.util.Objects;

public abstract class V1beta1AbstractType<T extends Spec> {

    @SerializedName("apiVersion")
    private String apiVersion = "kubeless.io/v1beta1";

    @SerializedName("metadata")
    private V1ObjectMeta metadata = null;

    @SerializedName("spec")
    private T spec = null;

    public V1beta1AbstractType<T> apiVersion(String apiVersion){
        this.apiVersion = apiVersion;
        return this;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public abstract String getKind();

    public V1beta1AbstractType<T> metadata(V1ObjectMeta metadata) {
        this.metadata = metadata;
        return this;
    }

    public V1ObjectMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(V1ObjectMeta metadata) {
        this.metadata = metadata;
    }

    public V1beta1AbstractType<T> spec(T spec) {
        this.spec = spec;
        return this;
    }

    public T getSpec() {
        return spec;
    }

    public void setSpec(T spec) {
        this.spec = spec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1beta1AbstractType)) return false;
        V1beta1AbstractType<?> that = (V1beta1AbstractType<?>) o;
        return Objects.equals(getApiVersion(), that.getApiVersion()) &&
                Objects.equals(getKind(), that.getKind()) &&
                Objects.equals(getMetadata(), that.getMetadata()) &&
                Objects.equals(getSpec(), that.getSpec());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApiVersion(), getKind(), getMetadata(), getSpec());
    }

    @Override
    public String toString() {
        return "V1beta1AbstractType{" +
                "apiVersion='" + apiVersion + '\'' +
                ", kind='" + getKind() + '\'' +
                ", metadata=" + metadata +
                ", spec=" + spec +
                '}';
    }
}

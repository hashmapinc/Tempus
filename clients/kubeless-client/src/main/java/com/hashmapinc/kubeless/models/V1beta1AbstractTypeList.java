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
package com.hashmapinc.kubeless.models;

import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.models.V1ListMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class V1beta1AbstractTypeList<T extends V1beta1AbstractType> {

    @SerializedName("apiVersion")
    private String apiVersion = "kubeless.io/v1beta1";

    @SerializedName("items")
    private List<T> items = new ArrayList<>();

    @SerializedName("metadata")
    private V1ListMeta metadata = null;

    public V1beta1AbstractTypeList<T> apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public V1beta1AbstractTypeList<T> items(List<T> items) {
        this.items = items;
        return this;
    }

    public V1beta1AbstractTypeList<T> addItemsItem(T itemsItem) {
        this.items.add(itemsItem);
        return this;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public abstract String getKind();

    public V1beta1AbstractTypeList<T> metadata(V1ListMeta metadata) {
        this.metadata = metadata;
        return this;
    }

    public V1ListMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(V1ListMeta metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1beta1AbstractTypeList)) return false;
        V1beta1AbstractTypeList<?> that = (V1beta1AbstractTypeList<?>) o;
        return Objects.equals(getApiVersion(), that.getApiVersion()) &&
                Objects.equals(getItems(), that.getItems()) &&
                Objects.equals(getKind(), that.getKind()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApiVersion(), getItems(), getKind(), getMetadata());
    }

    @Override
    public String toString() {
        return "V1beta1AbstractTypeList{" +
                "apiVersion='" + apiVersion + '\'' +
                ", items=" + items +
                ", kind='" + getKind() + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}

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
import com.hashmapinc.kubeless.models.Spec;
import io.kubernetes.client.models.V1LabelSelector;

import java.util.Objects;

public class V1beta1KafkaTriggerSpec implements Spec{

    @SerializedName("topic")
    private String topic;

    @SerializedName("functionSelector")
    private V1LabelSelector labelSelector;

    public V1beta1KafkaTriggerSpec topic(String topic){
        this.topic = topic;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public V1beta1KafkaTriggerSpec labelSelector(V1LabelSelector labelSelector){
        this.labelSelector = labelSelector;
        return this;
    }

    public V1LabelSelector getLabelSelector() {
        return labelSelector;
    }

    public void setLabelSelector(V1LabelSelector labelSelector) {
        this.labelSelector = labelSelector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1beta1KafkaTriggerSpec)) return false;
        V1beta1KafkaTriggerSpec that = (V1beta1KafkaTriggerSpec) o;
        return Objects.equals(getTopic(), that.getTopic()) &&
                Objects.equals(getLabelSelector(), that.getLabelSelector());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTopic(), getLabelSelector());
    }

    @Override
    public String toString() {
        return "V1beta1KafkaTriggerSpec{" +
                "topic='" + topic + '\'' +
                ", labelSelector=" + labelSelector +
                '}';
    }
}

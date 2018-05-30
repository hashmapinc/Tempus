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
package com.hashmapinc.models;

import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.models.AppsV1beta1Deployment;
import io.kubernetes.client.models.V1ServiceSpec;
import io.kubernetes.client.models.V2beta1HorizontalPodAutoscaler;

import java.util.Objects;

public class V1beta1FunctionSpec implements Spec{

    @SerializedName("handler")
    private String handler;

    @SerializedName("function")
    private String function;

    @SerializedName("function-content-type")
    private String functionContentType;

    @SerializedName("checksum")
    private String checksum;

    @SerializedName("runtime")
    private String runtime;

    @SerializedName("timeout")
    private String timeout;

    @SerializedName("deps")
    private String dependencies;

    @SerializedName("deployment")
    private AppsV1beta1Deployment deployment;

    @SerializedName("service")
    private V1ServiceSpec serviceSpec;

    @SerializedName("horizontalPodAutoscaler")
    private V2beta1HorizontalPodAutoscaler horizontalPodAutoscaler;

    public V1beta1FunctionSpec handler(String handler){
        this.handler = handler;
        return this;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public V1beta1FunctionSpec function(String function){
        this.function = function;
        return this;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public V1beta1FunctionSpec functionContentType(String functionContentType){
        this.functionContentType = functionContentType;
        return this;
    }

    public String getFunctionContentType() {
        return functionContentType;
    }

    public void setFunctionContentType(String functionContentType) {
        this.functionContentType = functionContentType;
    }

    public V1beta1FunctionSpec checksum(String checksum){
        this.checksum = checksum;
        return this;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public V1beta1FunctionSpec runtime(String runtime){
        this.runtime = runtime;
        return this;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public V1beta1FunctionSpec timeout(String timeout){
        this.timeout = timeout;
        return this;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public V1beta1FunctionSpec dependencies(String dependencies){
        this.dependencies = dependencies;
        return this;
    }

    public String getDependencies() {
        return dependencies;
    }

    public void setDependencies(String dependencies) {
        this.dependencies = dependencies;
    }

    public V1beta1FunctionSpec deployment(AppsV1beta1Deployment deployment){
        this.deployment = deployment;
        return this;
    }

    public AppsV1beta1Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(AppsV1beta1Deployment deployment) {
        this.deployment = deployment;
    }

    public V1beta1FunctionSpec serviceSpec(V1ServiceSpec serviceSpec){
        this.serviceSpec = serviceSpec;
        return this;
    }

    public V1ServiceSpec getServiceSpec() {
        return serviceSpec;
    }

    public void setServiceSpec(V1ServiceSpec serviceSpec) {
        this.serviceSpec = serviceSpec;
    }

    public V1beta1FunctionSpec horizontalPodAutoscaler(V2beta1HorizontalPodAutoscaler horizontalPodAutoscaler){
        this.horizontalPodAutoscaler = horizontalPodAutoscaler;
        return this;
    }

    public V2beta1HorizontalPodAutoscaler getHorizontalPodAutoscaler() {
        return horizontalPodAutoscaler;
    }

    public void setHorizontalPodAutoscaler(V2beta1HorizontalPodAutoscaler horizontalPodAutoscaler) {
        this.horizontalPodAutoscaler = horizontalPodAutoscaler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1beta1FunctionSpec)) return false;
        V1beta1FunctionSpec that = (V1beta1FunctionSpec) o;
        return Objects.equals(getHandler(), that.getHandler()) &&
                Objects.equals(getFunction(), that.getFunction()) &&
                Objects.equals(getFunctionContentType(), that.getFunctionContentType()) &&
                Objects.equals(getChecksum(), that.getChecksum()) &&
                Objects.equals(getRuntime(), that.getRuntime()) &&
                Objects.equals(getTimeout(), that.getTimeout()) &&
                Objects.equals(getDependencies(), that.getDependencies()) &&
                Objects.equals(getDeployment(), that.getDeployment()) &&
                Objects.equals(getServiceSpec(), that.getServiceSpec()) &&
                Objects.equals(getHorizontalPodAutoscaler(), that.getHorizontalPodAutoscaler());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHandler(), getFunction(), getFunctionContentType(), getChecksum(), getRuntime(), getTimeout(), getDependencies(), getDeployment(), getServiceSpec(), getHorizontalPodAutoscaler());
    }

    @Override
    public String toString() {
        return "V1beta1FunctionSpec{" +
                "handler='" + handler + '\'' +
                ", function='" + function + '\'' +
                ", functionContentType='" + functionContentType + '\'' +
                ", checksum='" + checksum + '\'' +
                ", runtime='" + runtime + '\'' +
                ", timeout='" + timeout + '\'' +
                ", dependencies='" + dependencies + '\'' +
                ", deployment=" + deployment +
                ", serviceSpec=" + serviceSpec +
                ", horizontalPodAutoscaler=" + horizontalPodAutoscaler +
                '}';
    }
}

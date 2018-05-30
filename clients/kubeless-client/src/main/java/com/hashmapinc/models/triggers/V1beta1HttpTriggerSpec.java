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
package com.hashmapinc.models.triggers;

import com.google.gson.annotations.SerializedName;
import com.hashmapinc.models.Spec;

import java.util.Objects;

public class V1beta1HttpTriggerSpec implements Spec{

    @SerializedName("function-name")
    private String functionName;

    @SerializedName("host-name")
    private String hostName;

    @SerializedName("tls")
    private boolean tls = false;

    @SerializedName("tls-secret")
    private String tlsSecret;

    @SerializedName("path")
    private String path;

    @SerializedName("basic-auth-secret")
    private String basicAuthSecret;

    @SerializedName("gateway")
    private String gateway;

    public V1beta1HttpTriggerSpec functionName(String functionName){
        this.functionName = functionName;
        return this;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public V1beta1HttpTriggerSpec hostName(String hostName){
        this.hostName = hostName;
        return this;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public V1beta1HttpTriggerSpec tls(boolean tls){
        this.tls = tls;
        return this;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public V1beta1HttpTriggerSpec tlsSecret(String tlsSecret){
        this.tlsSecret = tlsSecret;
        return this;
    }

    public String getTlsSecret() {
        return tlsSecret;
    }

    public void setTlsSecret(String tlsSecret) {
        this.tlsSecret = tlsSecret;
    }

    public V1beta1HttpTriggerSpec path(String path){
        this.path = path;
        return this;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public V1beta1HttpTriggerSpec basicAuthSecret(String basicAuthSecret){
        this.basicAuthSecret = basicAuthSecret;
        return this;
    }

    public String getBasicAuthSecret() {
        return basicAuthSecret;
    }

    public void setBasicAuthSecret(String basicAuthSecret) {
        this.basicAuthSecret = basicAuthSecret;
    }

    public V1beta1HttpTriggerSpec gateway(String gateway){
        this.gateway = gateway;
        return this;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1beta1HttpTriggerSpec)) return false;
        V1beta1HttpTriggerSpec that = (V1beta1HttpTriggerSpec) o;
        return isTls() == that.isTls() &&
                Objects.equals(getFunctionName(), that.getFunctionName()) &&
                Objects.equals(getHostName(), that.getHostName()) &&
                Objects.equals(getTlsSecret(), that.getTlsSecret()) &&
                Objects.equals(getPath(), that.getPath()) &&
                Objects.equals(getBasicAuthSecret(), that.getBasicAuthSecret()) &&
                Objects.equals(getGateway(), that.getGateway());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFunctionName(), getHostName(), isTls(), getTlsSecret(), getPath(), getBasicAuthSecret(), getGateway());
    }

    @Override
    public String toString() {
        return "V1beta1HttpTriggerSpec{" +
                "functionName='" + functionName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", tls=" + tls +
                ", tlsSecret='" + tlsSecret + '\'' +
                ", path='" + path + '\'' +
                ", basicAuthSecret='" + basicAuthSecret + '\'' +
                ", gateway='" + gateway + '\'' +
                '}';
    }
}

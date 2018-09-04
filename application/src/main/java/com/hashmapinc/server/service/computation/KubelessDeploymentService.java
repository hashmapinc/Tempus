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
package com.hashmapinc.server.service.computation;

import com.hashmapinc.kubeless.apis.KubelessV1beta1FunctionApi;
import com.hashmapinc.kubeless.models.V1beta1AbstractType;
import com.hashmapinc.kubeless.models.V1beta1Function;
import com.hashmapinc.kubeless.models.V1beta1FunctionSpec;
import com.hashmapinc.server.common.data.computation.ComputationMetadata;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.KubelessComputationMetadata;
import com.hashmapinc.server.common.msg.exception.TempusRuntimeException;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
public class KubelessDeploymentService implements ComputationFunctionDeploymentService{

    public static final String DEFAULT_NAMESPACE = "default";
    public static final String TXT = "txt";
    public static final String TXT_ZIP = "txt+zip";
    public static final String BASE64 = "base64";
    public static final String BASE64_ZIP = "base64+zip";
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int CREATED = 201;
    public static final int OK = 200;

    @Value("${kubeless.cluster_mode_enabled}")
    private boolean clusterModeEnabled;

    @Value("${kubeless.kube_config_path}")
    private String kublessConfigPath;


    private static volatile  KubelessV1beta1FunctionApi kubelessV1beta1FunctionApi;

    private Base64.Decoder decoder = Base64.getDecoder();

    @Override
    public void deployKubelessFunction(Computations computation) {
        try {
            KubelessV1beta1FunctionApi functionApi = getKubelessV1beta1FunctionApi(DEFAULT_NAMESPACE);
            KubelessComputationMetadata md = (KubelessComputationMetadata) computation.getComputationMetadata();
            int resposeCode = INTERNAL_SERVER_ERROR;
            switch (md.getFunctionContentType()) {
                case TXT_ZIP:
                case TXT:
                    String textContent = convertToTxt(md.getFunctionContent());
                    md.setFunctionContent(textContent);
                    V1beta1AbstractType<V1beta1FunctionSpec> v1beta1Function = createV1beta1Function(md);
                    Call call = functionApi.createFunctionCall((V1beta1Function)v1beta1Function);
                    Response response = call.execute();
                    resposeCode = response.code();
                    break;
                case BASE64_ZIP:
                case BASE64:
                    v1beta1Function = createV1beta1Function(md);
                    call = functionApi.createFunctionCall((V1beta1Function)v1beta1Function);
                    response = call.execute();
                    resposeCode = response.code();
                    break;
                    default:
                        break;
            }

            if ( resposeCode != CREATED)
                throw new TempusRuntimeException("Function was not deployed!!");
        }
        catch (ApiException e){
            log.info("Kubeless api e.kubeconfigxception for deploy funtion : {}", e);
        } catch (IOException e) {
            log.info("");
        }
    }

    @Override
    public boolean checkKubelessFunction(Computations computation) {
        try {
            KubelessComputationMetadata md = (KubelessComputationMetadata)(computation.getComputationMetadata());
            KubelessV1beta1FunctionApi functionApi = getKubelessV1beta1FunctionApi(DEFAULT_NAMESPACE);

            Call call = functionApi.getFunctionCall(md.getFunction());
            Response response = call.execute();
            if (response.code() != OK)
                return false;
        } catch (ApiException e) {
            log.error("Kubeless api exception for fetch function : " + e);
        } catch (IOException e) {
            log.error("Exception occured in call execution : " + e);
        } catch (Exception e) {
            log.error("Exception occured : " + e);
        }

        return true;
    }

    @Override
    public void deleteKubelessFunction(Computations computation) {
        try {
            KubelessComputationMetadata md = (KubelessComputationMetadata)(computation.getComputationMetadata());
            KubelessV1beta1FunctionApi functionApi = getKubelessV1beta1FunctionApi(DEFAULT_NAMESPACE);

            Call call = functionApi.deleteFunctionCall(md.getFunction());
            Response response = call.execute();
            if (response.code() != OK)
                throw new TempusRuntimeException("Problem occured in deleting kubeless funtion from kubernetes.");
        } catch (ApiException e) {
            log.error("Kubeless api exception for fetch function : " + e);
        } catch (IOException e) {
            log.error("Exception occured in call execution : " + e);
        } catch (Exception e) {
            log.error("Exception occured : " + e);
        }
    }

    private KubelessV1beta1FunctionApi getKubelessV1beta1FunctionApi(String namespace) {
        try {
            if (kubelessV1beta1FunctionApi == null) {
                synchronized (KubelessV1beta1FunctionApi.class) {
                    if (kubelessV1beta1FunctionApi == null) {
                        if (clusterModeEnabled) {
                            ApiClient client = Config.fromCluster();
                            Configuration.setDefaultApiClient(client);
                            return new KubelessV1beta1FunctionApi(namespace);
                        } else {
                            ApiClient client = Config.fromConfig("/home/himanshu/.kube/aws.secure.kubeconfig");
                            Configuration.setDefaultApiClient(client);
                            return new KubelessV1beta1FunctionApi(namespace);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.info("Execption occured in getting Kubernetes Api client : ", e);
        }
        return kubelessV1beta1FunctionApi;
    }

    private V1beta1Function createV1beta1Function(ComputationMetadata computationMetadata) {
        try {
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(((KubelessComputationMetadata)computationMetadata).getFunction());
            v1ObjectMeta.setNamespace(DEFAULT_NAMESPACE);
            V1beta1FunctionSpec v1beta1FunctionSpec = createV1beta1FunctionSpec(computationMetadata);
            V1beta1Function v1beta1Function = new V1beta1Function();
            v1beta1Function.setMetadata(v1ObjectMeta);
            v1beta1Function.setSpec(v1beta1FunctionSpec);
            return v1beta1Function;
        }
        catch (Exception e) {
            log.info("Exception occured : ", e);
        }
        return null;
    }

    private V1beta1FunctionSpec createV1beta1FunctionSpec(ComputationMetadata computationMetadata) {
        KubelessComputationMetadata md = (KubelessComputationMetadata)computationMetadata;
        V1beta1FunctionSpec v1beta1FunctionSpec = new V1beta1FunctionSpec();
        v1beta1FunctionSpec.setFunction(md.getFunctionContent());
        if(md.getDependencies() != null)
            v1beta1FunctionSpec.dependencies(md.getDependencies());
        v1beta1FunctionSpec.setHandler(md.getHandler());
        v1beta1FunctionSpec.setRuntime(md.getRuntime());
        v1beta1FunctionSpec.functionContentType(md.getFunctionContentType());
        v1beta1FunctionSpec.setChecksum(md.getChecksum());
        v1beta1FunctionSpec.setTimeout(md.getTimeout());
        return v1beta1FunctionSpec;
    }

    private String convertToTxt(String functionContent){
        byte[] decodedContent = decoder.decode(functionContent);
        return new String(decodedContent);
    }

}

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
import com.hashmapinc.kubeless.models.V1beta1Function;
import com.hashmapinc.kubeless.models.V1beta1FunctionSpec;
import com.hashmapinc.server.common.data.computation.ComputationRequest;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.KubelessComputationMetadata;
import com.hashmapinc.server.exception.TempusException;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;
import io.kubernetes.client.ApiException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static com.hashmapinc.server.exception.TempusErrorCode.ITEM_NOT_FOUND;

@Slf4j
public class KubelessDeploymentService implements ComputationFunctionDeploymentService{

    public static final String DEFAULT_NAMESPACE = "default";
    public static final String ZIP_CONST = "+zip";
    public static final String TXT = "txt";
    public static final String TXT_ZIP = "txt+zip";
    public static final String BASE64 = "base64";
    public static final String BASE64_ZIP = "base64+zip";

    private static volatile  KubelessV1beta1FunctionApi kubelessV1beta1FunctionApi;

    private Base64.Decoder decoder = Base64.getDecoder();
    private Base64.Encoder encoder = Base64.getEncoder();

    @Override
    public void deployKubelessFunction(ComputationRequest computationRequest) {
        try {
            Computations computation = computationRequest.getComputation();
            KubelessV1beta1FunctionApi functionApi = getKubelessV1beta1FunctionApi(DEFAULT_NAMESPACE);
            KubelessComputationMetadata md = (KubelessComputationMetadata) computation.getComputationMetadata();
            switch (md.getFunctionContentType()) {
                case TXT_ZIP:
                case TXT:
                    String textContent = convertToTxt(computationRequest.getFunctionContent());
                    V1beta1Function v1beta1Function = createV1beta1Function(computationRequest, textContent);
                    functionApi.createFunctionCall(v1beta1Function);
                    break;
                case BASE64_ZIP:
                case BASE64:
                    v1beta1Function = createV1beta1Function(computationRequest, computationRequest.getFunctionContent());
                    functionApi.createFunctionCall(v1beta1Function);
                    break;
                    default:
                        break;
            }
        }
        catch (ApiException e){
            log.info("Kubeless api exception for deploy funtion : ", e);
        }
    }

    @Override
    public ComputationRequest fetchKubelessFunction(Computations computation) {
        ComputationRequest cr = null;
        try {
            KubelessComputationMetadata md = (KubelessComputationMetadata)computation.getComputationMetadata();
            KubelessV1beta1FunctionApi functionApi = getKubelessV1beta1FunctionApi(DEFAULT_NAMESPACE);
            Call call = functionApi.getFunctionCall(md.getFunction());
            Response response = call.execute();
            byte[] resBytes = response.body().bytes();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(resBytes);
            String hashSha1 = new String(hash);
            if (!hashSha1.contentEquals(md.getChecksum()))
                throw new TempusException("Checksum of functions did not match ", ITEM_NOT_FOUND);
            // Convert function to base64 and put in request
            cr = new ComputationRequest();
            cr.setComputation(computation);
            cr.setFunctionContent(convertToBase64(resBytes));

        } catch (ApiException e) {
            log.info("Kubeless api exception for fetch function : ", e);
        } catch (IOException e) {
            log.info("Exception occured in call execution : ", e);
        } catch (Exception e) {
            log.info("Exception occured : ", e);
        }

        return cr;
    }

    private KubelessV1beta1FunctionApi getKubelessV1beta1FunctionApi(String namespace) {
        if (kubelessV1beta1FunctionApi == null) {
            synchronized (KubelessV1beta1FunctionApi.class) {
                if(kubelessV1beta1FunctionApi == null)
                return new KubelessV1beta1FunctionApi(namespace);
            }
        }
        return kubelessV1beta1FunctionApi;
    }

    private V1beta1Function createV1beta1Function(ComputationRequest computationRequest, String textContent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(textContent.getBytes(StandardCharsets.UTF_8));
            V1beta1FunctionSpec v1beta1FunctionSpec = createV1beta1FunctionSpec(textContent, computationRequest, hash);
            V1beta1Function v1beta1Function = new V1beta1Function();
            v1beta1Function.setSpec(v1beta1FunctionSpec);
            return v1beta1Function;
        }
        catch (NoSuchAlgorithmException e) {
            log.info("Sha1 algorithm not present ");
        }
        return null;
    }

    private V1beta1FunctionSpec createV1beta1FunctionSpec(String textContent, ComputationRequest computationRequest, byte[] hash) {
        Computations computation = computationRequest.getComputation();
        KubelessComputationMetadata md = (KubelessComputationMetadata)computation.getComputationMetadata();
        V1beta1FunctionSpec v1beta1FunctionSpec = new V1beta1FunctionSpec();
        v1beta1FunctionSpec.setChecksum(new String(hash));
        md.setChecksum(new String(hash));
        v1beta1FunctionSpec.setFunction(textContent);
        v1beta1FunctionSpec.dependencies(md.getDependencies());
        v1beta1FunctionSpec.setHandler(md.getHandler());
        v1beta1FunctionSpec.setRuntime(md.getRuntime().name());
//        if(computationRequest.isZip())
//            v1beta1FunctionSpec.functionContentType(md.getFunctionContentType() + ZIP_CONST);
//        else
        v1beta1FunctionSpec.functionContentType(md.getFunctionContentType());
        v1beta1FunctionSpec.setTimeout("180");
        return v1beta1FunctionSpec;
    }

    private String convertToTxt(String functionContent){
        byte[] decodedContent = decoder.decode(functionContent);
        return new String(decodedContent);
    }

    private String convertToBase64(byte[] fuctionBytes) {
        return encoder.encodeToString(fuctionBytes);
    }
}

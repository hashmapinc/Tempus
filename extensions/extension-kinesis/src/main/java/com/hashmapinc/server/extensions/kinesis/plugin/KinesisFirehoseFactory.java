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
package com.hashmapinc.server.extensions.kinesis.plugin;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsync;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsyncClientBuilder;

/**
 * @author Mitesh Rathore
 */
public class KinesisFirehoseFactory {

    private KinesisFirehoseFactory() {}

    public static final KinesisFirehoseFactory INSTANCE = new KinesisFirehoseFactory();



    public AmazonKinesisFirehoseAsync create(KinesisPluginConfiguration configuration) {
        if (basicCredentialsDefined(configuration)) {
            return initFirehoseBasedOnBasicCredentials(configuration);
        } else if (profileCredentialsDefined(configuration)) {
            return initFirehoseBasedOnProfileCredentials(configuration);
        } else {
            throw new IllegalStateException("Plugin configuration incomplete. Basic AWS Credentials or Profile required");
        }
    }

    private boolean isNotEmpty(String text) {
        return !(text == null || "".equals(text));
    }

    private boolean basicCredentialsDefined(KinesisPluginConfiguration configuration) {
        return isNotEmpty(configuration.getAccessKeyId()) &&
                isNotEmpty(configuration.getSecretAccessKey()) &&
                isNotEmpty(configuration.getRegion());
    }

    private boolean profileCredentialsDefined(KinesisPluginConfiguration configuration) {
        return isNotEmpty(configuration.getProfile());
    }

    private AmazonKinesisFirehoseAsync initFirehoseBasedOnProfileCredentials(KinesisPluginConfiguration configuration) {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(configuration.getProfile());
        try {
            credentialsProvider.getCredentials();
            return AmazonKinesisFirehoseAsyncClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .build();
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.", e);
        }
    }


    private AmazonKinesisFirehoseAsync initFirehoseBasedOnBasicCredentials(KinesisPluginConfiguration configuration) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(configuration.getAccessKeyId(), configuration.getSecretAccessKey());
        AWSStaticCredentialsProvider awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
        return  AmazonKinesisFirehoseAsyncClientBuilder.standard()
                .withCredentials(awsStaticCredentialsProvider)
                .withRegion(configuration.getRegion())
                .build();
    }

}

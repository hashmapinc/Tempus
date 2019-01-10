package com.hashmapinc.server.service.computation;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.Computations;
import org.springframework.beans.factory.annotation.Value;

public class AWSLambdaFunctionService implements ServerlessFunctionService {

    @Value("${aws.access_key}")
    private String awsAccesskey;

    @Value("${aws.secret_key}")
    private String awsSecretKey;

    BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccesskey, awsSecretKey);

    @Override
    public boolean deployFunction(Computations computations) {
        AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
        return false;
    }

    @Override
    public boolean checkFunction(Computations computation) {
        return false;
    }

    @Override
    public boolean deleteFunction(Computations computation) {
        return false;
    }

    @Override
    public boolean createTrigger(ComputationJob computationJob) {
        return false;
    }

    @Override
    public boolean checkTrigger(ComputationJob computationJob) {
        return false;
    }

    @Override
    public boolean deleteTrigger(ComputationJob computationJob) {
        return false;
    }
}

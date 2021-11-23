package com.ariielm.integrationtestssqs;

import cloud.localstack.awssdkv1.TestUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AWSTestConfiguration {

    @Bean
    public AmazonSQSAsync amazonSQS(){
        return TestUtils.getClientSQSAsync();
    }

    @Bean
    public AmazonS3 amazonS3(){
        return TestUtils.getClientS3();
    }

    @Bean
    public AmazonSNS amazonSNS(){
        return TestUtils.getClientSNS();
    }

}
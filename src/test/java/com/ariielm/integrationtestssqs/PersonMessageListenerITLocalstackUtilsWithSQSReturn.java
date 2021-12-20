package com.ariielm.integrationtestssqs;

import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Map;

import static cloud.localstack.ServiceName.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@LocalstackDockerProperties(services = { S3, SQS, SNS }, imageTag = "0.12.19.1")
@Import(AWSLocalstackUtilsTestConfiguration.class)
@ExtendWith(LocalstackDockerExtension.class)
@SpringBootTest
class PersonMessageListenerITLocalstackUtilsWithSQSReturn {

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Value("${sns.personTopic.arn}")
    private String personTopicARN;

    @Value("${sns.personTopicResult.url}")
    private String personTopicResultURL;


    static {
        TestUtils.getClientS3().createBucket("person-bucket");
        TestUtils.getClientSQS().createQueue("person-queue");
    }

    @DynamicPropertySource
    static void overrideConfiguration(DynamicPropertyRegistry registry) {
        registry.add("sns.personTopic.arn", () -> TestUtils.getClientSNS().createTopic("person-topic").getTopicArn());
        registry.add("sns.personTopicResult.url", () -> TestUtils.getClientSQS().createQueue("person-queue-result").getQueueUrl());
    }

    @Test
    public void testLocalS3API() {

        var queueArn = TestUtils.getClientSQS().getQueueAttributes(personTopicResultURL, List.of("QueueArn")).getAttributes().get("QueueArn");

        TestUtils.getClientSNS().subscribe(personTopicARN, "sqs", queueArn);

        queueMessagingTemplate.send("person-queue", new GenericMessage<>("{\n" +
                "           \"id\": \"42\",\n" +
                "           \"name\": \"Ariel Molina\",\n" +
                "           \"createdAt\": \"2021-11-11 12:00:00\",\n" +
                "           \"active\": true\n" +
                "        }", Map.of("contentType", "application/json")));

        given()
                .ignoreException(AmazonS3Exception.class)
                .await()
                .atMost(5, SECONDS)
                .untilAsserted(() -> assertNotNull(TestUtils.getClientS3().getObject("person-bucket", "42")));

        given()
                .ignoreException(AmazonSQSException.class)
                .await()
                .atMost(5, SECONDS)
                .untilAsserted(() -> assertThat(TestUtils.getClientSQS().receiveMessage(TestUtils.getClientSQS().getQueueUrl("person-queue-result").getQueueUrl()).getMessages().size(), greaterThan(0)));

    }



}

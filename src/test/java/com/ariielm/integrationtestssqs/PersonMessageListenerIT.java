package com.ariielm.integrationtestssqs;

import java.io.IOException;
import java.util.Map;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*;

@Testcontainers
@SpringBootTest
class PersonMessageListenerIT {

    private static final String QUEUE_NAME = "person-test-queue";
    private static final String BUCKET_NAME = "person-test-bucket";

    @Container
    static LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.13.0"))
                    .withServices(S3, SQS);

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
        localStack.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET_NAME);
    }

    @DynamicPropertySource
    static void overrideConfiguration(DynamicPropertyRegistry registry) {
        registry.add("sqs.personQueue", () -> QUEUE_NAME);
        registry.add("s3.personBucket", () -> BUCKET_NAME);
        registry.add("cloud.aws.sqs.endpoint", () -> localStack.getEndpointOverride(SQS));
        registry.add("cloud.aws.s3.endpoint", () -> localStack.getEndpointOverride(S3));
        registry.add("cloud.aws.credentials.access-key", localStack::getAccessKey);
        registry.add("cloud.aws.credentials.secret-key", localStack::getSecretKey);
    }

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Test
    void messageShouldBeUploadedToBucketOnceConsumedFromQueue() {
        queueMessagingTemplate.send(QUEUE_NAME, new GenericMessage<>("{\n" +
                "           \"id\": \"42\",\n" +
                "           \"name\": \"Ariel Molina\",\n" +
                "           \"createdAt\": \"2021-11-11 12:00:00\",\n" +
                "           \"active\": true\n" +
                "        }", Map.of("contentType", "application/json")));

        given()
                .ignoreException(AmazonS3Exception.class)
                .await()
                .atMost(5, SECONDS)
                .untilAsserted(() -> assertNotNull(amazonS3.getObject(BUCKET_NAME, "42")));

    }
}

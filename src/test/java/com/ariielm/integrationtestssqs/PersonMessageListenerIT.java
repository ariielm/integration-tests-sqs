package com.ariielm.integrationtestssqs;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cloud.localstack.awssdkv1.TestUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*;

@Testcontainers
@SpringBootTest
class PersonMessageListenerIT {

    private static final String QUEUE_NAME = "person-queue";
    private static final String BUCKET_NAME = "person-bucket";
    private static final String TOPIC_NAME = "person-topic";
    private static final String RESULTING_QUEUE_NAME = "resulting-queue";

    @Container
    static LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.13.0"))
                    .withServices(S3, SQS, SNS);

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
        localStack.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET_NAME);
    }

    @DynamicPropertySource
    static void overrideConfiguration(DynamicPropertyRegistry registry) {
        registry.add("sqs.personQueue", () -> QUEUE_NAME);
        registry.add("s3.personBucket", () -> BUCKET_NAME);
        registry.add("sns.personTopic.arn", PersonMessageListenerIT::createTopic);
        registry.add("sns.personTopicResult.url", PersonMessageListenerIT::createResultingQueue);
        registry.add("cloud.aws.sqs.endpoint", () -> localStack.getEndpointOverride(SQS));
        registry.add("cloud.aws.s3.endpoint", () -> localStack.getEndpointOverride(S3));
        registry.add("cloud.aws.credentials.access-key", localStack::getAccessKey);
        registry.add("cloud.aws.credentials.secret-key", localStack::getSecretKey);

    }

    private static String createTopic() {
        try {
            return localStack.execInContainer("awslocal", "sns", "create-topic", "--name", TOPIC_NAME, "--output", "text").getStdout();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static String createResultingQueue() {
        try {
            return localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", RESULTING_QUEUE_NAME, "--output", "text").getStdout();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Value("${sns.personTopic.arn}")
    private String personTopicArn;

    @Value("${sns.personTopicResult.url}")
    private String resultingQueueURL;

    @Test
    void messageShouldBeUploadedToBucketOnceConsumedFromQueue() {
        var queueArn = TestUtils.getClientSQS().getQueueAttributes(resultingQueueURL, List.of("QueueArn")).getAttributes().get("QueueArn");

        TestUtils.getClientSNS().subscribe(personTopicArn, "sqs", queueArn);

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

        given()
                .ignoreException(AmazonSQSException.class)
                .await()
                .atMost(5, SECONDS)
                .untilAsserted(() -> assertThat(TestUtils.getClientSQS().receiveMessage(resultingQueueURL).getMessages().size(), greaterThan(0)));

    }
}

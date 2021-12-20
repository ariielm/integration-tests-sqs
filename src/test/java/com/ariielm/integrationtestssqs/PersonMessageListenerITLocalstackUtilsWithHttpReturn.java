package com.ariielm.integrationtestssqs;

//import cloud.localstack.awssdkv1.TestUtils;
//import cloud.localstack.docker.LocalstackDockerExtension;
//import cloud.localstack.docker.annotation.LocalstackDockerProperties;
//import com.amazonaws.services.s3.model.AmazonS3Exception;
//import com.amazonaws.services.sns.model.AmazonSNSException;
//import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
//import org.junit.Ignore;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.web.server.LocalServerPort;
//import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
//import org.springframework.context.annotation.Import;
//import org.springframework.messaging.support.GenericMessage;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//
//import java.util.Map;
//
//import static cloud.localstack.ServiceName.*;
//import static com.github.tomakehurst.wiremock.client.WireMock.*;
//import static java.util.concurrent.TimeUnit.SECONDS;
//import static org.awaitility.Awaitility.given;
//import static org.junit.jupiter.api.Assertions.assertNotNull;

//@SpringBootTest
//@ExtendWith(LocalstackDockerExtension.class)
//@LocalstackDockerProperties(services = { S3, SQS, SNS }, imageTag = "0.12.19.1")
//@Import(AWSTestConfiguration.class)
//@AutoConfigureWireMock(port = 8099)
class PersonMessageListenerITLocalstackUtilsWithHttpReturn {

//    @Autowired
//    private QueueMessagingTemplate queueMessagingTemplate;
//
//    @Value("${sns.personTopic.arn}")
//    private String personTopicARN;
//
//    @BeforeAll
//    public static void init() {
//        TestUtils.getClientS3().createBucket("person-bucket");
//        TestUtils.getClientSQS().createQueue("person-queue");
//    }
//
//    @DynamicPropertySource
//    static void overrideConfiguration(DynamicPropertyRegistry registry) {
//        registry.add("sns.personTopic.arn", () -> TestUtils.getClientSNS().createTopic("person-topic").getTopicArn());
//    }
//
//    @Test
//    @Ignore
//    public void testLocalS3API() {
//        given()
//                .ignoreException(AmazonSNSException.class)
//                .await()
//                .atMost(10, SECONDS)
//                .untilAsserted(() -> verify(postRequestedFor(urlEqualTo("/sns-return"))));
//
//        TestUtils.getClientSNS().subscribe(personTopicARN, "http", "http://localhost:8099/sns-return");
//
//        queueMessagingTemplate.send("person-queue", new GenericMessage<>("{\n" +
//                "           \"id\": \"42\",\n" +
//                "           \"name\": \"Ariel Molina\",\n" +
//                "           \"createdAt\": \"2021-11-11 12:00:00\",\n" +
//                "           \"active\": true\n" +
//                "        }", Map.of("contentType", "application/json")));
//
//        given()
//                .ignoreException(AmazonS3Exception.class)
//                .await()
//                .atMost(5, SECONDS)
//                .untilAsserted(() -> assertNotNull(TestUtils.getClientS3().getObject("person-bucket", "42")));
//
//
//
//    }



}

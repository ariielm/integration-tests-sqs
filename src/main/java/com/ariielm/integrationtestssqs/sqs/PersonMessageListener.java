package com.ariielm.integrationtestssqs.sqs;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PersonMessageListener {

    private final AmazonS3 amazonS3;
    private final ObjectMapper objectMapper;
    private final String personBucket;

    public PersonMessageListener(@Value("${s3.personBucket}") String personBucket,
                                 AmazonS3 amazonS3,
                                 ObjectMapper objectMapper) {
        this.amazonS3 = amazonS3;
        this.objectMapper = objectMapper;
        this.personBucket = personBucket;
    }

    @SqsListener(value = "${sqs.personQueue}")
    public void processMessage(@Payload PersonEvent personEvent) throws JsonProcessingException {
        System.out.println("Message received: " + personEvent);
        amazonS3.putObject(personBucket, personEvent.getId(), objectMapper.writeValueAsString(personEvent));
        System.out.println("Uploaded person " + personEvent.getId() + " succcesfully");
    }
}
package com.ariielm.integrationtestssqs.sqs;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.core.region.RegionProvider;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

@Configuration
public class MessageConfig {

    //TODO ver como resolver o override desse bean pra ele nem tentar executar, senão ele já dá exception na hora de iniciar esse bean, então não é nem possível realizar o override
//    @Bean("amazonSQS")
//    public AmazonSQSAsync amazonSQSAsync(){
//        return AmazonSQSAsyncClientBuilder.standard()
//                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
//                .build();
//    }

    //TODO pra funcionar com o Testcontainer eu precisaria dar um override nesse Bean com um Configuration de Teste, passando o Localstack como Bean, por exemlo
    /*
    S3Client
                .builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                    localstack.getAccessKey(), localstack.getSecretKey()
                )))
                .region(Region.of(localstack.getRegion()))
                .build();
     */
    @Bean
    AmazonSQSAsync amazonSQS(AWSCredentialsProvider awsCredentialsProvider, RegionProvider regionProvider,
                             ClientConfiguration clientConfiguration) {
        return AmazonSQSAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider)
                .withClientConfiguration(clientConfiguration).withRegion(regionProvider.getRegion().getName()).build();
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSQS) {
        return new QueueMessagingTemplate(amazonSQS);
    }

    @Bean
    public MappingJackson2MessageConverter mappingJackson2MessageConverter(@Autowired ObjectMapper objectMapper) {
        MappingJackson2MessageConverter jackson2MessageConverter = new MappingJackson2MessageConverter();
        jackson2MessageConverter.setObjectMapper(objectMapper);
        return jackson2MessageConverter;
    }
}
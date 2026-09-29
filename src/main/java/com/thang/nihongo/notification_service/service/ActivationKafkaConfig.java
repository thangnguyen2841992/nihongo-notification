package com.thang.nihongo.notification_service.service;

import com.thang.nihongo.notification_service.model.MessageResponseUser;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import java.util.HashMap;

@Configuration
public class ActivationKafkaConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MessageResponseUser> activationKafkaFactory(KafkaProperties properties) {
        var config = new HashMap<>(properties.buildConsumerProperties());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        // Ignore producer Java type headers: the consumer owns the activation contract.
        var valueDeserializer = new ErrorHandlingDeserializer<>(new JsonDeserializer<>(MessageResponseUser.class, false));
        var factory = new ConcurrentKafkaListenerContainerFactory<String, MessageResponseUser>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), valueDeserializer));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        var handler = new DefaultErrorHandler(new FixedBackOff(5000, FixedBackOff.UNLIMITED_ATTEMPTS));
        // Preserve failed records, including malformed contracts, for operator recovery.
        handler.setClassifications(java.util.Map.of(), true);
        factory.setCommonErrorHandler(handler);
        return factory;
    }
}

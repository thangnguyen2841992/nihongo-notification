package com.thang.nihongo.notification_service.wallet;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.backoff.FixedBackOff;
import java.util.*;
@Configuration @EnableScheduling
public class WalletMailKafkaConfig {
    @Bean public KafkaTemplate<String, String> walletMailDltTemplate(KafkaProperties properties) {
        Map<String, Object> config = new HashMap<>(properties.buildProducerProperties());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));
    }
    @Bean public ConcurrentKafkaListenerContainerFactory<String, String> walletMailKafkaFactory(KafkaProperties properties,
            @Qualifier("walletMailDltTemplate") KafkaTemplate<String, String> template,
            @Value("${wallet.events.topic:wallet.events.v1}") String topic) {
        Map<String, Object> config = new HashMap<>(properties.buildConsumerProperties());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(config));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        var recoverer = new DeadLetterPublishingRecoverer(template, (record, error) -> new TopicPartition(topic + ".email.DLT", record.partition()));
        recoverer.setFailIfSendResultIsError(true);
        var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000, 4));
        // Infrastructure outages keep the event on its source partition; malformed contracts go to DLT.
        handler.setBackOffFunction((record, error) -> {
            Throwable cause = error;
            while (cause.getCause() != null) cause = cause.getCause();
            return cause instanceof IllegalArgumentException || cause instanceof com.fasterxml.jackson.core.JsonProcessingException
                ? new FixedBackOff(2000, 4) : new FixedBackOff(5000, FixedBackOff.UNLIMITED_ATTEMPTS);
        });
        factory.setCommonErrorHandler(handler);
        return factory;
    }
    @Bean public NewTopic walletMailDlt(@Value("${wallet.events.topic:wallet.events.v1}") String topic,
        @Value("${wallet.events.partitions:3}") int partitions, @Value("${wallet.events.replicas:1}") int replicas) {
        return TopicBuilder.name(topic + ".email.DLT").partitions(partitions).replicas(replicas).build();
    }
}

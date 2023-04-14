package com.teliacompany.webflux.request.kafka;

import com.teliacompany.webflux.request.log.RequestLogger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import(KafkaProducerConfig.class)
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true")
public class KafkaAutoConfiguration {

    @Bean
    public KafkaMessageSender kafkaMessageSender(RequestLogger requestLogger, KafkaProducerConfig producerConfig) {
        return new KafkaMessageSender(requestLogger, producerConfig);
    }
}

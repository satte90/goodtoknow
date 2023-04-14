package com.teliacompany.tiberius.base.test.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

public class ComponentTestKafkaConsumer extends KafkaConsumer<String, String> {

    public ComponentTestKafkaConsumer(EmbeddedKafkaBroker broker) {
        super(getConfigs(broker));
    }

    private static Map<String, Object> getConfigs(EmbeddedKafkaBroker broker) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testConsumer", "false", broker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return consumerProps;
    }
}

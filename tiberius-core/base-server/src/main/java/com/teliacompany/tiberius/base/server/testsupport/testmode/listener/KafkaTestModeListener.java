package com.teliacompany.tiberius.base.server.testsupport.testmode.listener;

import com.teliacompany.tiberius.base.server.api.TestModeData;
import com.teliacompany.webflux.request.kafka.KafkaMessageSender;
import com.teliacompany.webflux.request.kafka.KafkaProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.kafka.sender.KafkaSender;

@Service
@Profile({"componenttest", "local"})
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true")
public class KafkaTestModeListener implements TiberiusTestModeEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaTestModeListener.class);
    private final KafkaMessageSender kafkaMessageSender;
    private final KafkaProducerConfig testModeConfig;
    private KafkaProducerConfig originalConfig;
    private boolean testModeEnabled = false;

    public KafkaTestModeListener(KafkaMessageSender kafkaMessageSender) {
        LOG.info("Created KafkaTestModeListener");
        this.kafkaMessageSender = kafkaMessageSender;
        testModeConfig = new KafkaProducerConfig("127.0.0.1:9092", "PLAINTEXT", "GSSAPI", "https")
                .setCredentials("test", "test");
    }

    @Override
    public void enableTestMode(TestModeData testModeData) {
        if(!testModeEnabled) {
            testModeEnabled = true;
            LOG.info("Setting kafka sender to broker on server 127.0.0.1:9092");
            this.originalConfig = kafkaMessageSender.getActiveProducerConfig();
            kafkaMessageSender.createSender(testModeConfig);
        }
    }

    @Override
    public void disableTestMode() {
        if(testModeEnabled) {
            testModeEnabled = false;
            LOG.info("Resetting kafka sender using original config: {}", originalConfig);
            kafkaMessageSender.createSender(originalConfig);
        }
    }
}

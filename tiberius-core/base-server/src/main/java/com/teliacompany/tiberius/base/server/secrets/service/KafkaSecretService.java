package com.teliacompany.tiberius.base.server.secrets.service;

import com.teliacompany.webflux.request.kafka.KafkaMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true")
public class KafkaSecretService implements SecretService {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaSecretService.class);

    public static final String FAME_USERNAME_SECRET_NAME = "fame_username";
    public static final String FAME_PASSWORD_SECRET_NAME = "fame_password";

    private final KafkaMessageSender kafkaMessageSender;

    public KafkaSecretService(KafkaMessageSender kafkaMessageSender) {
        this.kafkaMessageSender = kafkaMessageSender;
    }

    @Override
    public List<String> requestSecretNames() {
        return List.of(FAME_USERNAME_SECRET_NAME, FAME_PASSWORD_SECRET_NAME);
    }

    @Override
    public void onSecretsReceived(Map<String, String> secrets) {
        String fameUsername = secrets.get(FAME_USERNAME_SECRET_NAME);
        String famePassword = secrets.get(FAME_PASSWORD_SECRET_NAME);
        if(fameUsername != null && famePassword != null) {
            LOG.info("Received kafka secrets");
            kafkaMessageSender.createSender(fameUsername, famePassword);
        }
    }
}

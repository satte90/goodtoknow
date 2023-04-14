package com.teliacompany.webflux.request.kafka;

import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class KafkaProducerConfig extends HashMap<String, Object> {
    private static final String JAAS_TEMPLATE = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";

    public static final String KEY_SERIALIZER = "key.serializer";
    public static final String VALUE_SERIALIZER = "value.serializer";
    public static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
    public static final String SECURITY_PROTOCOL = "security.protocol";
    public static final String SASL_MECHANISM = "sasl.mechanism";
    public static final String SSL_ENDPOINT_IDENTIFICATION_ALGORITHM = "ssl.endpoint.identification.algorithm";
    public static final String SASL_JAAS_CONFIG = "sasl.jaas.config";


    public KafkaProducerConfig(@Value("${kafka.bootstrap.servers:#{null}}") String bootstrapServers,
                               @Value("${kafka.security.protocol:SASL_SSL}") String securityProtocol,
                               @Value("${kafka.sasl.mechanism:SCRAM-SHA-512}") String saslMechanism,
                               @Value("${kafka.ssl.endpoint.identification.algorithm:}") String sslEndpointIdentificationAlgorithm) {

        if(bootstrapServers == null) {
            throw new InternalServerErrorException("Bootstrap servers property not set");
        }

        //These are hard coded and cannot be changed, as the solution is now we don't want any automatic (de)serialization by the kafka framework
        this.put(KEY_SERIALIZER, "org.apache.kafka.common.serialization.StringSerializer");
        this.put(VALUE_SERIALIZER, "org.apache.kafka.common.serialization.StringSerializer");
        this.put(SASL_JAAS_CONFIG, ""); // Set using setCredentials

        this.setBootstrapServers(bootstrapServers);
        this.setSecurityProtocol(securityProtocol);
        this.setSaslMechanism(saslMechanism);
        this.setSslEndpointIdentificationAlgorithm(sslEndpointIdentificationAlgorithm);
    }

    static KafkaProducerConfig copyOf(KafkaProducerConfig old) {
        KafkaProducerConfig copy = new KafkaProducerConfig(old.getBootstrapServers(), old.getSecurityProtocol(), old.getSaslMechanism(), old.getSslEndpointIdentificationAlgorithm());
        copy.put(SASL_JAAS_CONFIG, old.get(SASL_JAAS_CONFIG));
        return copy;
    }

    public KafkaProducerConfig setCredentials(String username, String password) {
        String jaasCfg = String.format(JAAS_TEMPLATE, username, password);
        this.put(SASL_JAAS_CONFIG, jaasCfg);
        return this;
    }

    public KafkaProducerConfig setBootstrapServers(String bootstrapServers) {
        this.put(BOOTSTRAP_SERVERS, bootstrapServers);
        return this;
    }

    public KafkaProducerConfig setSecurityProtocol(String securityProtocol) {
        this.put(SECURITY_PROTOCOL, securityProtocol);
        return this;
    }

    public KafkaProducerConfig setSaslMechanism(String saslMechanism) {
        this.put(SASL_MECHANISM, saslMechanism);
        return this;
    }

    public KafkaProducerConfig setSslEndpointIdentificationAlgorithm(String sslEndpointIdentificationAlgorithm) {
        this.put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM, sslEndpointIdentificationAlgorithm);
        return this;
    }

    public String getBootstrapServers() {
        return (String) get(BOOTSTRAP_SERVERS);
    }

    public String getSecurityProtocol() {
        return (String) get(SECURITY_PROTOCOL);
    }

    public String getSaslMechanism() {
        return (String) get(SASL_MECHANISM);
    }

    public String getSslEndpointIdentificationAlgorithm() {
        return (String) get(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM);
    }
}

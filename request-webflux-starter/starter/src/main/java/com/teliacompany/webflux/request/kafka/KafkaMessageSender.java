package com.teliacompany.webflux.request.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teliacompany.webflux.error.api.ErrorCause;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.webflux.jackson.TeliaObjectMapper;
import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.context.RequestContext;
import com.teliacompany.webflux.request.context.RequestContextBuilder;
import com.teliacompany.webflux.request.log.RequestLogger;
import com.teliacompany.webflux.request.log.RequestLoggingOptions;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

public class KafkaMessageSender {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaMessageSender.class);

    private final RequestLogger requestLogger;
    private final KafkaProducerConfig producerConfig;

    private KafkaProducerConfig activeProducerConfig;
    private KafkaSender<String, String> sender;

    public KafkaMessageSender(RequestLogger requestLogger, KafkaProducerConfig producerConfig) {
        this.requestLogger = requestLogger;

        // Save the producer config in two variables, one to keep the original configuration, and one for the currently active configuration which may change during
        // runtime with for example new credentials. Initially both will be the same.
        this.producerConfig = producerConfig;
        this.activeProducerConfig = producerConfig;
    }

    /**
     * Create a new sender for username and password. Use a copy of the default producerConfig and set required properties and create the sender
     * Sets the active sender to the created sender
     *
     * @param username         - username
     * @param password         - password
     */
    public void createSender(String username, String password) {
        //Do not change servers (null value ignored)
        KafkaProducerConfig config = KafkaProducerConfig.copyOf(producerConfig)
                .setCredentials(username, password);
        createSender(config);
    }

    /**
     * Create a new sender using KafkaProducerConfig;
     */
    public void createSender(KafkaProducerConfig config) {
        activeProducerConfig = config;
        sender = KafkaSender.create(SenderOptions.create(activeProducerConfig));
    }

    /**
     * Returns the active KafkaSender
     */
    public KafkaProducerConfig getActiveProducerConfig() {
        return activeProducerConfig;
    }

    /**
     * Post a message using the sender. Throws exception if no sender has been created.
     * Before use, create a sender using createSender() method or set an active sender using setActiveSender()
     */
    public <T> Mono<Void> postKafkaMessage(T event, String TOPIC_NAME) {
        if(sender == null) {
            throw new InternalServerErrorException("Sender has not been created");
        }

        Mono<SenderRecord<String, String, Integer>> recordPublisher = RequestProcessor.getTransactionContext()
                .map(tctx -> {

                    RequestContext requestContext = new RequestContextBuilder()
                            .withServiceName("Kafka")
                            .withUri("KafkaHost", TOPIC_NAME)
                            .withTransactionContext(tctx)
                            .withTcad(tctx.getTcad())
                            .withTid(tctx.getTid())
                            .withTscId(tctx.getTscid())
                            .buildRequestContext();

                    try {
                        String json = TeliaObjectMapper.get().writeValueAsString(event);
                        requestLogger.logRequest(requestContext, json, RequestLoggingOptions.defaults());
                        return SenderRecord.create(new ProducerRecord<>(TOPIC_NAME, "1", json), 1);
                    } catch(JsonProcessingException e) {
                        throw new InternalServerErrorException(ErrorCause.from("Internal", e), "Could not serializer kafka message to JSON");
                    }
                });

        return sender.createOutbound()
                .send(recordPublisher)
                .then()
                .doOnError(e -> {
                    throw new InternalServerErrorException(ErrorCause.from("Internal", e), e.getMessage());
                });
    }
}

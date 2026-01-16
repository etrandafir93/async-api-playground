package com.etrandafir.asyncapi.inventoryservice;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.contract.stubrunner.StubTrigger;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.etrandafir.asyncapi.contracts.events.OrderCreated;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.confluent.kafka.serializers.KafkaAvroSerializer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = { CollaborationTest.TestConfig.class, InventoryServiceApp.class, ContainersConfig.class })
@AutoConfigureStubRunner(ids = "com.etrandafir.asyncapi:order-service:1.0-SNAPSHOT:stubs", stubsMode = StubRunnerProperties.StubsMode.LOCAL)
@ExtendWith(OutputCaptureExtension.class)
class CollaborationTest {

    @Autowired
    StubTrigger trigger;

    @MockitoBean
    StockUpdatePublisher stockUpdatePublisher;

    @Test
    void shouldDecreaseStockWhenOrderingItems() {
        trigger.trigger("order-2-items");

        await().untilAsserted(() ->
            verify(stockUpdatePublisher, times(2))
                .publishStockUpdated(any()));
    }

    @Test
    void shouldPublishStockUnavailableWhenOrderingTooManyItems() {
        trigger.trigger("order-2k-items");

        await().untilAsserted(() ->
            verify(stockUpdatePublisher, times(2))
                .publishStockUnavailable(any()));
    }

    @Configuration
    static class TestConfig {

        @Bean
        MessageVerifierSender<Message<?>> standaloneMessageVerifier(
                KafkaProperties properties,
                KafkaConnectionDetails connectionDetails) {
            return new MessageVerifierSender<>() {

                @Override
                public void send(Message<?> message, String destination, YamlContract contract) {
                }

                @Override
                public <T> void send(T payload, Map<String, Object> headers, String destination, @Nullable YamlContract contract) {
                    Map<String, Object> newHeaders = headers != null ? new HashMap<>(headers) : new HashMap<>();
                    newHeaders.put(KafkaHeaders.TOPIC, destination);

                    try {
                        OrderCreated order = new JsonMapper().readValue(payload.toString(), OrderCreated.class);
                        Message<?> message = MessageBuilder.createMessage(order, new MessageHeaders(newHeaders));
                        kafkaTemplate().send(message);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }

                private KafkaTemplate<?, ?> kafkaTemplate() {
                    Map<String, Object> map = new HashMap<>();
                    map.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, connectionDetails.getProducerBootstrapServers());
                    map.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                    map.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
                    map.put(SPECIFIC_AVRO_READER_CONFIG, "true");
                    map.put(SCHEMA_REGISTRY_URL_CONFIG, "mock://test-registry");

                    DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(map);
                    return new KafkaTemplate<>(factory);
                }
            };
        }
    }

}

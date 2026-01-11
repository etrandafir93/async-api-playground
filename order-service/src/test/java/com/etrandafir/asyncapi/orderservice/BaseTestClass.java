package com.etrandafir.asyncapi.orderservice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import com.etrandafir.asyncapi.orderservice.OrderController.CreateOrderRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = { OrderServiceApp.class, BaseTestClass.TestConfig.class })
@AutoConfigureMessageVerifier
@ActiveProfiles("contracts")
public abstract class BaseTestClass {

    @Autowired
    OrderController orderController;

    @Container
    @ServiceConnection
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka"));

    public void orderCreated() {
        Map<String, Integer> items = Map.of(
            "sku-100", 10,
            "sku-200", 20
        );
        orderController.createOrder(new CreateOrderRequest(items));
    }

    public void orderCreatedWithTooManyItems() {
        Map<String, Integer> items = Map.of(
            "sku-100", 1000,
            "sku-200", 2000
        );
        orderController.createOrder(new CreateOrderRequest(items));
    }


    @Configuration
    static class TestConfig {

        @Bean
        KafkaMessageVerifier kafkaTemplateMessageVerifier() {
            return new KafkaMessageVerifier();
        }

        @Bean
        @Primary
        ContractVerifierObjectMapper contractVerifierObjectMapper() {
            var om = new ObjectMapper();
            om.addMixIn(SpecificRecordBase.class, AvroIgnoreMixin.class);
            return new ContractVerifierObjectMapper(om);
        }

        @JsonIgnoreProperties({ "schema", "specificData", "classSchema", "conversion" })
        interface AvroIgnoreMixin {
        }

        @Bean
        KafkaListenerContainerFactory<?> testContainerFactory(KafkaProperties kp) {
            var props = kp.buildConsumerProperties();
            props.put("bootstrap.servers", kafka.getBootstrapServers());
            var cf = new DefaultKafkaConsumerFactory<>(props);
            var lcf = new ConcurrentKafkaListenerContainerFactory();
            lcf.setConsumerFactory(cf);
            return lcf;
        }

    }

    static class KafkaMessageVerifier implements MessageVerifierReceiver<Message<?>> {

        private static final Log LOG = LogFactory.getLog(KafkaMessageVerifier.class);

        Map<String, BlockingQueue<Message<?>>> broker = new ConcurrentHashMap<>();

        @Override
        public Message receive(String destination, long timeout, TimeUnit timeUnit, @Nullable YamlContract contract) {
            broker.putIfAbsent(destination, new ArrayBlockingQueue<>(1));
            BlockingQueue<Message<?>> messageQueue = broker.get(destination);
            Message<?> message;
            try {
                message = messageQueue.poll(timeout, timeUnit);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (message != null) {
                LOG.info("Removed a message from a topic [" + destination + "]");
                LOG.info(message.getPayload()
                    .toString());
            }
            return message;
        }

        @KafkaListener(id = "testListener", topics = { "order-created" }, containerFactory = "testContainerFactory")
        public void listen(@Payload ConsumerRecord payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
            LOG.info("Got a message from a topic [" + topic + "]");
            Map<String, Object> headers = new HashMap<>();
            new DefaultKafkaHeaderMapper().toHeaders(payload.headers(), headers);
            broker.putIfAbsent(topic, new ArrayBlockingQueue<>(1));
            BlockingQueue<Message<?>> messageQueue = broker.get(topic);
            messageQueue.add(MessageBuilder.createMessage(payload.value(), new MessageHeaders(headers)));
        }

        @Override
        public Message receive(String destination, YamlContract contract) {
            return receive(destination, 15, TimeUnit.SECONDS, contract);
        }

    }
}


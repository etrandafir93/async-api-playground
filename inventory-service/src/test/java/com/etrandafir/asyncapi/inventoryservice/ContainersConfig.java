package com.etrandafir.asyncapi.inventoryservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class ContainersConfig {

    @Bean
    @ServiceConnection
    ConfluentKafkaContainer kafka() {
        return new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"));
    }

}
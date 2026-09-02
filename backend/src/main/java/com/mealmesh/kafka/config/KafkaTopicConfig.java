package com.mealmesh.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topic.orders:mealmesh.orders}")
    private String ordersTopic;

    @Value("${spring.kafka.topic.payments:mealmesh.payments}")
    private String paymentsTopic;

    @Value("${spring.kafka.topic.restaurant:mealmesh.restaurant}")
    private String restaurantTopic;

    @Value("${spring.kafka.topic.delivery:mealmesh.delivery}")
    private String deliveryTopic;

    @Value("${spring.kafka.topic.notifications:mealmesh.notifications}")
    private String notificationsTopic;

    @Value("${spring.kafka.topic.order-status:mealmesh.order-status}")
    private String orderStatusTopic;

    @Value("${spring.kafka.topic.dlq:mealmesh.dlq}")
    private String dlqTopic;

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(ordersTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentsTopic() {
        return TopicBuilder.name(paymentsTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic restaurantTopic() {
        return TopicBuilder.name(restaurantTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deliveryTopic() {
        return TopicBuilder.name(deliveryTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(notificationsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderStatusTopic() {
        return TopicBuilder.name(orderStatusTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name(dlqTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public org.springframework.kafka.core.KafkaAdmin kafkaAdmin() {
        java.util.Map<String, Object> configs = new java.util.HashMap<>();
        configs.put(org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, 
                System.getenv().getOrDefault("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
        org.springframework.kafka.core.KafkaAdmin admin = new org.springframework.kafka.core.KafkaAdmin(configs);
        admin.setFatalIfBrokerNotAvailable(false);
        return admin;
    }
}
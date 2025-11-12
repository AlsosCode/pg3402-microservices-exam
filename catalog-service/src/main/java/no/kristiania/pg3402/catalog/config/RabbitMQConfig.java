package no.kristiania.pg3402.catalog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Catalog Service
 * This service consumes events published by Collection Service
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "card.events.exchange";
    public static final String QUEUE_NAME = "card.events.queue";
    public static final String ROUTING_KEY = "card.events";

    /**
     * Define the exchange (should match Collection Service)
     */
    @Bean
    public TopicExchange cardEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    /**
     * Define the queue (should match Collection Service)
     */
    @Bean
    public Queue cardEventsQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    /**
     * Bind queue to exchange with routing key
     */
    @Bean
    public Binding binding(Queue cardEventsQueue, TopicExchange cardEventsExchange) {
        return BindingBuilder
                .bind(cardEventsQueue)
                .to(cardEventsExchange)
                .with(ROUTING_KEY);
    }

    /**
     * JSON message converter for deserializing events
     * Configured with JavaTimeModule to handle Instant and other Java 8 date/time types
     */
    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

package com.sparta.logistics.slack_service.infrastructure.config;

import com.sparta.logistics.slack_service.infrastructure.dto.DeliveryInfoDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class ConsumerApplicationKafkaConfig {
    @Bean
    public ConsumerFactory<String, DeliveryInfoDto> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        configProps.put(JsonDeserializer.TYPE_MAPPINGS,
                "com.sparta.logistics.delivery_service.application.dto.DeliveryInfoDto:com.sparta.logistics.slack_service.infrastructure.dto.DeliveryInfoDto");

        configProps.put(JsonDeserializer.TRUSTED_PACKAGES,
                "com.sparta.logistics.delivery_service.application.dto,com.sparta.logistics.slack_service.infrastructure.dto");

        JsonDeserializer<DeliveryInfoDto> valueDeserializer = new JsonDeserializer<>(DeliveryInfoDto.class, false);
        valueDeserializer.addTrustedPackages(
                "com.sparta.logistics.delivery_service.application.dto",
                "com.sparta.logistics.slack_service.infrastructure.dto"
        );
        valueDeserializer.setUseTypeMapperForKey(false);

        return new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeliveryInfoDto>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, DeliveryInfoDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

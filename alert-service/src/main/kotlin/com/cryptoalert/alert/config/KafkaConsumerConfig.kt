package com.cryptoalert.alert.config

import com.cryptoalert.shared.event.PriceChangedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@EnableKafka
@Configuration
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "kafka")
class KafkaConsumerConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,
) {

    @Bean
    fun priceEventListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, PriceChangedEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            JsonDeserializer.TRUSTED_PACKAGES to "com.cryptoalert.shared.event",
            JsonDeserializer.VALUE_DEFAULT_TYPE to PriceChangedEvent::class.java.name,
            JsonDeserializer.USE_TYPE_INFO_HEADERS to "false",
        )
        val consumerFactory = DefaultKafkaConsumerFactory<String, PriceChangedEvent>(props)
        return ConcurrentKafkaListenerContainerFactory<String, PriceChangedEvent>().apply {
            this.consumerFactory = consumerFactory
        }
    }
}

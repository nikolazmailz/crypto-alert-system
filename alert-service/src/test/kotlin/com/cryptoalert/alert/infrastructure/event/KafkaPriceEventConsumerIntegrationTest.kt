package com.cryptoalert.alert.infrastructure.event

import com.cryptoalert.alert.AlertsBaseIntegrationTest
import com.cryptoalert.alert.domain.AlertCondition
import com.cryptoalert.shared.event.PriceChangedEvent
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.TestPropertySource
import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

/**
 * Интеграционный тест для [KafkaPriceEventConsumer].
 *
 * Поднимает встроенный Kafka-брокер через [@EmbeddedKafka], переключает контекст
 * в режим `kafka` через [@TestPropertySource] и проверяет полный путь сообщения:
 *   KafkaTemplate → топик `price-events` → KafkaPriceEventConsumer → AlertService → БД.
 *
 * Сценарии идентичны [InternalPriceEventListenerIntegrationTest]: одна и та же
 * бизнес-логика должна работать независимо от транспорта.
 */
@EmbeddedKafka(
    partitions = 1,
    topics = [KafkaPriceEventConsumer.TOPIC],
    // Перезаписывает spring.kafka.bootstrap-servers адресом встроенного брокера.
    // KafkaConsumerConfig и TestKafkaProducerConfig подхватят его через @Value.
    bootstrapServersProperty = "spring.kafka.bootstrap-servers",
)
@TestPropertySource(properties = ["app.events.type=kafka"])
class KafkaPriceEventConsumerIntegrationTest(
    private val kafkaTemplate: KafkaTemplate<String, PriceChangedEvent>,
) : AlertsBaseIntegrationTest() {

    /**
     * Продюсер только для тестов — имитирует отправку из market-data-service.
     * Автоматически подхватывается [@SpringBootTest] как вложенная конфигурация.
     */
    @TestConfiguration
    class TestKafkaProducerConfig(
        @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    ) {
        @Bean
        fun kafkaTemplate(): KafkaTemplate<String, PriceChangedEvent> {
            val props = mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            )
            return KafkaTemplate(DefaultKafkaProducerFactory(props))
        }
    }

    init {
        beforeTest { runBlocking { deleteAllAlerts() } }

        context("KafkaPriceEventConsumer") {

            should("пометить GREATER_THAN алерт как отработавший, когда цена превысила порог") {
                // Arrange
                val alertId = UUID.randomUUID()
                insertAlert(alertId, UUID.randomUUID(), "BTCUSDT", BigDecimal("60000.00"), AlertCondition.GREATER_THAN)

                // Act — отправляем событие в Kafka напрямую, минуя market-data-service
                kafkaTemplate.send(KafkaPriceEventConsumer.TOPIC, "BTCUSDT",
                    PriceChangedEvent("BTCUSDT", BigDecimal("65000.00"))).await()

                // Assert — ждём асинхронного прохождения сообщения через consumer
                eventually(10.seconds) {
                    fetchIsActive(alertId) shouldBe false
                }
            }

            should("не трогать GREATER_THAN алерт, если цена ниже порога") {
                val alertId = UUID.randomUUID()
                insertAlert(alertId, UUID.randomUUID(), "BTCUSDT", BigDecimal("70000.00"), AlertCondition.GREATER_THAN)

                kafkaTemplate.send(KafkaPriceEventConsumer.TOPIC, "BTCUSDT",
                    PriceChangedEvent("BTCUSDT", BigDecimal("65000.00"))).await()

                eventually(10.seconds) {
                    fetchIsActive(alertId) shouldBe true
                }
            }

            should("пометить LESS_THAN алерт как отработавший, когда цена упала ниже порога") {
                val alertId = UUID.randomUUID()
                insertAlert(alertId, UUID.randomUUID(), "ETHUSDT", BigDecimal("3000.00"), AlertCondition.LESS_THAN)

                kafkaTemplate.send(KafkaPriceEventConsumer.TOPIC, "ETHUSDT",
                    PriceChangedEvent("ETHUSDT", BigDecimal("2800.00"))).await()

                eventually(10.seconds) {
                    fetchIsActive(alertId) shouldBe false
                }
            }

            should("не обрабатывать алерты другого символа") {
                // Arrange — алерт на BTCUSDT, событие придёт для ETHUSDT
                val alertId = UUID.randomUUID()
                insertAlert(alertId, UUID.randomUUID(), "BTCUSDT", BigDecimal("60000.00"), AlertCondition.GREATER_THAN)

                kafkaTemplate.send(KafkaPriceEventConsumer.TOPIC, "ETHUSDT",
                    PriceChangedEvent("ETHUSDT", BigDecimal("65000.00"))).await()

                eventually(10.seconds) {
                    fetchIsActive(alertId) shouldBe true
                }
            }

            should("обработать несколько алертов одного символа за одно событие") {
                val alertId1 = UUID.randomUUID()
                val alertId2 = UUID.randomUUID()
                insertAlert(alertId1, UUID.randomUUID(), "BTCUSDT", BigDecimal("50000.00"), AlertCondition.GREATER_THAN)
                insertAlert(alertId2, UUID.randomUUID(), "BTCUSDT", BigDecimal("55000.00"), AlertCondition.GREATER_THAN)

                kafkaTemplate.send(KafkaPriceEventConsumer.TOPIC, "BTCUSDT",
                    PriceChangedEvent("BTCUSDT", BigDecimal("65000.00"))).await()

                eventually(10.seconds) {
                    fetchIsActive(alertId1) shouldBe false
                    fetchIsActive(alertId2) shouldBe false
                }
            }

            should("не обрабатывать алерт с is_active = false повторно") {
                // Arrange — алерт уже отработал (is_active = false)
                val alertId = UUID.randomUUID()
                insertAlert(alertId, UUID.randomUUID(), "BTCUSDT", BigDecimal("60000.00"), AlertCondition.GREATER_THAN)
                // Помечаем как уже отработавший
                databaseClient.sql("UPDATE alerts SET is_active = false WHERE id = :id")
                    .bind("id", alertId)
                    .fetch().rowsUpdated().block()

                kafkaTemplate.send(KafkaPriceEventConsumer.TOPIC, "BTCUSDT",
                    PriceChangedEvent("BTCUSDT", BigDecimal("65000.00"))).await()

                // Алерт должен оставаться is_active = false (повторный UPDATE не страшен,
                // но репозиторий его попросту не найдёт среди активных)
                eventually(10.seconds) {
                    fetchIsActive(alertId) shouldBe false
                }
            }
        }
    }
}

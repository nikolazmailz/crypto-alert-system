package com.cryptoalert.alert.config

import com.cryptoalert.alert.application.AlertService
import com.cryptoalert.shared.event.PriceChangedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.mono
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import org.springframework.messaging.Message
import reactor.core.publisher.Mono

@Configuration
class PriceEventsConsumerConfig(
    private val alertService: AlertService,
) {
    private val log = KotlinLogging.logger {}

    @Bean
    fun priceEventsConsumer(): (Flux<Message<PriceChangedEvent>>) -> Unit =
        { events ->
            events
                .doOnNext { msg ->
                    log.info {
                        "Order received: ${msg.payload.symbol} " +
                            "partition=${msg.headers["kafka_receivedPartitionId"]} " +
                            "offset=${msg.headers["kafka_offset"]}"
                    }
                }
                .flatMap { msg ->
                    handlePriceEvents(msg.payload) }
                .onErrorContinue { ex, value ->
                    // onErrorContinue — поток не умирает при ошибке одного сообщения
                    log.error { "Order processing failed for $value: ${ex.message}" }
                }
        }

    private fun handlePriceEvents(event: PriceChangedEvent): Mono<Void> =
        mono {
            alertService.processPriceChange(event.symbol, event.price)
        }.then()
}

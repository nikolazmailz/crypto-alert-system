package com.cryptoalert.portfolio.config

import com.cryptoalert.shared.cached.CaffeineStampedeCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.time.Duration

@Configuration
class CacheConfig {

    @Bean
    fun priceCache() = CaffeineStampedeCache<String, BigDecimal>(
        name = "prices",
        ttl = Duration.ofMinutes(10),
        maxSize = 50_000,
    )

}

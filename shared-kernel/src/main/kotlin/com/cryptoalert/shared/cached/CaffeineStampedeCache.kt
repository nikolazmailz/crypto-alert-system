package com.cryptoalert.shared.cached

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.future.await
import java.time.Duration
import java.util.concurrent.CompletableFuture

class CaffeineStampedeCache<K : Any, V : Any>(
    val name: String,
    ttl: Duration,
    maxSize: Long = 10_000,
) {
    // AsyncCache — ключевое отличие от обычного Cache:
    // хранит CompletableFuture<V> вместо V,
    // что позволяет дедуплицировать параллельные запросы
    private val cache: AsyncCache<K, V> = Caffeine.newBuilder()
        .maximumSize(maxSize)
        .expireAfterWrite(ttl)
        .recordStats()
        .buildAsync()

    /**
     * Получить значение из кэша.
     * Если значения нет — вызывает loader ровно 1 раз,
     * даже если 1000 coroutines запросили одновременно.
     */
    suspend fun get(key: K, loader: suspend () -> V): V =
        cache.get(key) { _, executor ->
            // Оборачиваем suspend-loader в CompletableFuture
            // через executor от Caffeine — он управляет потоками
            CoroutineScope(executor.asCoroutineDispatcher())
                .async { loader() }
                .asCompletableFuture()
        }.await() // suspend — не блокирует поток пока ждём результат

    fun getIfPresent(key: K): V? =
        cache.getIfPresent(key)?.getNow(null)

    fun put(key: K, value: V) {
        cache.put(key, CompletableFuture.completedFuture(value))
    }

    fun invalidate(key: K) =
        cache.synchronous().invalidate(key)

    fun invalidateAll() =
        cache.synchronous().invalidateAll()

    fun printStats() {
        val s = cache.synchronous().stats()
        println(
            "[$name] " +
                "hitRate=${"%.0f".format(s.hitRate() * HIT_RATE)}% " +
                "hits=${s.hitCount()} " +
                "misses=${s.missCount()}"
        )
    }

    companion object {
        private const val HIT_RATE = 100
    }
}

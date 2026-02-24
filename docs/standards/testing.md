# Стратегия тестирования (Testing Strategy)

**Status:** Accepted
**Last Updated:** 202X-XX-XX

Этот документ описывает стандарты, инструменты и подходы к тестированию микросервисов в проекте.

## 1. Пирамида тестирования

Мы придерживаемся классической пирамиды, но с упором на "тяжелые" интеграционные тесты, так как основная логика системы завязана на БД и внешние API.

1.  **Unit тесты (70%):** Изолированное тестирование доменной логики, мапперов и утилит. Весь I/O замокан.
2.  **Интеграционные тесты (25%):** Тестирование связки "Код + БД" или "Код + Внешний API". Поднимаем Spring Context.
3.  **E2E / Нагрузочное тестирование (5%):** Тестирование всей цепочки. Запускается отдельно.

## 2. Инструментарий

* **Фреймворк:** [Kotest](https://kotest.io/)
* **Стиль написания:** `ShouldSpec`
* **Мокирование:** `MockK` (нативная поддержка Kotlin и корутин)
* **База данных:** `Testcontainers` (PostgreSQL) — **Запрещено использовать H2 или другие in-memory БД.** Мы тестируем на том же движке, что и в проде.
* **Внешние API:** `WireMock` — для эмуляции бирж (Binance, CoinGecko).
* **Web API:** `WebTestClient` — для тестирования реактивных контроллеров.

---

## 3. Правила Unit-тестирования

### 3.1. Стиль Kotest (ShouldSpec)
Мы используем `ShouldSpec`, так как он отлично читается и ложится на BDD-парадигму.

```kotlin
// ✅ GOOD: Структура ShouldSpec
class CryptoServiceTest : ShouldSpec({
    val repository = mockk<CryptoRepository>()
    val service = CryptoService(repository)

    context("get current price") {
        should("return expected price when crypto exists") {
            // Arrange
            coEvery { repository.findBySymbol("BTC") } returns Crypto("BTC", 50000.0)

            // Act
            val result = service.getPrice("BTC")

            // Assert
            result shouldBe 50000.0
        }

        should("throw exception when crypto not found") {
            coEvery { repository.findBySymbol("UNKNOWN") } returns null

            shouldThrow<NotFoundException> {
                service.getPrice("UNKNOWN")
            }
        }
    }
})

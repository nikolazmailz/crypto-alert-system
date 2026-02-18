# Стандарты кодирования (Kotlin & Spring Boot)

**Status:** Draft\
**Last Updated:** 2024-02-18

Этот документ описывает принятые инженерные практики и стиль кода для проекта Crypto Alert System.\
Мы придерживаемся принципа: **Code is read much more often than it is written.**

---

## 1. Автоматизация и Линтеры
Перед пушем ветки обязательно выполнение локальных проверок:
```bash
./gradlew ktlintCheck  # Проверка форматирования (отступы, скобки)
./gradlew detekt       # Проверка на code-smells и сложность
```
Правила форматирования: Заданы в .editorconfig\
Правила статического анализа: Заданы в config/detekt/detekt.yml.

## 2. Kotlin Style Guide
### 2.1. Immutability (Неизменяемость)
По умолчанию все переменные и классы должны быть неизменяемыми.\
✅ Использовать val.\
❌ Использовать var только если это абсолютно необходимо (и обосновано).\
✅ Использовать List, Map (read-only интерфейсы).\
❌ Избегать MutableList в публичных API классов.\

### 2.2. Null Safety
Мы боремся с NullPointerException.\
❌ Строго запрещено использование оператора !! (double-bang).\
✅ Используйте безопасный вызов ?., элвис-оператор ?: или requireNotNull()
```kotlin
// Bad
val price = ticker!!.price
// Good
val price = ticker?.price ?: throw IllegalStateException("Price required")
```
### 2.3. Выражения (Expressions)
```kotlin
// Bad
fun getStatus(code: Int): String {
    if (code == 200) {
        return "OK"
    } else {
        return "ERROR"
    }
}

// Good
fun getStatus(code: Int) = if (code == 200) "OK" else "ERROR"
```

## 3. Spring & Architecture Standards
### 3.1. Dependency Injection
✅ Только Constructor Injection. Это делает классы тестируемыми и гарантирует их инициализацию. \
❌ Field Injection (@Autowired lateinit var) запрещен.
```kotlin
// Bad
@Service
class CryptoService {
    @Autowired lateinit var repo: CryptoRepository
}

// Good
@Service
class CryptoService(
    private val repo: CryptoRepository
)
```

### 3.2. Data Classes & DTO // todo
?? Для JPA/R2DBC сущностей используем обычные class (из-за особенностей проксирования Spring/Hibernate, хотя в R2DBC это мягче, сохраняем стандарт). \
(что дает data class ??) Для передачи данных (API, Service layer) используем data class.

### 3.3. Controller & Service
Контроллеры должны быть тонкими. Никакой бизнес-логики, только валидация запроса и вызов сервиса. \
Вся бизнес-логика в @Service или Domain Model.

## 4. Coroutines & Async // todo
### 4.1. Блокирующие вызовы
В проекте используется реактивный стек (WebFlux + Coroutines). \
❌ Строжайше запрещены блокирующие вызовы (Thread.sleep, CountDownLatch, тяжелые вычисления) в потоках Dispatchers.Main или дефолтных потоках Netty. \
✅ Для блокирующих операций (если неизбежно) используйте withContext(Dispatchers.IO).

### 4.2. GlobalScope // todo пример (best practice)
❌ Запрещено использовать GlobalScope. Это приводит к утечкам памяти и потерянным задачам. \
✅ Используйте coroutineScope, viewModelScope (если применимо) или инжектируйте CoroutineScope.

## 5. Naming Conventions
Классы: PascalCase (e.g., MarketDataService). \
Методы/Переменные: camelCase (e.g., fetchPrice). \
Константы: SCREAMING_SNAKE_CASE (e.g., MAX_RETRY_COUNT). \
Тесты: В тестах разрешено использовать backticks для читаемости. // todo  backticks ??
```kotlin
@Test
fun `should return error when price is negative`() { ... }
```

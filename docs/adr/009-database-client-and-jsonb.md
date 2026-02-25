# [ADR-009] Использование DatabaseClient и jsonb_build_object для работы с БД

**Status:** Accepted
**Date:** 2026-02-25

## Context

В проекте используется реактивный стек (R2DBC) и PostgreSQL. Стандартные подходы с использованием
`CoroutineCrudRepository` (Spring Data R2DBC) скрывают SQL, усложняют выполнение комплексных запросов с `JOIN` и имеют
ограничения при маппинге сложных доменных сущностей. Нам требуется максимальный контроль над запросами и высокая
производительность при маппинге данных из БД в объекты Kotlin.

## Decision

Мы отказываемся от использования Spring Data R2DBC репозиториев (`CoroutineCrudRepository` и т.д.) в пользу работы с
базой данных напрямую через `org.springframework.r2dbc.core.DatabaseClient`.

Приняты следующие стандарты написания запросов:

**1. Чтение данных (SELECT):**
Вся выборка данных должна формироваться на стороне PostgreSQL в формате JSON с помощью агрегатных функций (например,
`jsonb_build_object`, `jsonb_agg`). Полученный JSON-строка десериализуется в Kotlin data-классы с использованием Jackson
`ObjectMapper`.

*Пример:*

```kotlin
suspend fun findCryptoPrice(): CryptoPrice? {
  return client.sql(
    """
        select jsonb_build_object(
            'symbol', cp.symbol,
            'price', cp.price,
            'updatedAt', cp.updated_at
        ) as data
        from crypto_prices cp
        where cp.symbol = :symbol
    """.trimIndent()
  )
    .bind("symbol", symbol.uppercase())
    .fetch()
    .one()
    .map { row ->
      // R2DBC драйвер PostgreSQL возвращает JSON как io.r2dbc.postgresql.codec.Json
      val jsonString = (row["data"] as Json).asString()
      objectMapper.readValue(jsonString, CryptoPrice::class.java)
    }
    .awaitSingleOrNull()
}
```

**2. Запись данных (INSERT / UPDATE):**
Используется нативный SQL с ручным биндингом параметров.
Для обработки null-значений будет использоваться расширение (DatabaseClientExtensions) для DatabaseClient.

*Пример:*

```kotlin
return databaseClient.sql(
  """
    insert into crypto_prices (symbol, price, updated_at)
    values (:symbol, :price, :updatedAt)
    on conflict (symbol) do update
    set price = excluded.price, updated_at = excluded.updated_at
  """.trimIndent()
)
  // Использование extension-функции для безопасного биндинга null
  .bind("symbol", price.symbol) // import org.springframework.r2dbc.core.bind из класса DatabaseClientExtensions
  .bind("price", price.price)
  .bind("updatedAt", price.updatedAt)
  .fetch()
  .rowsUpdated()
  .awaitSingle()
```

## Consequences

**Положительные:**
* **Производительность:** PostgreSQL собирает JSON быстрее, чем R2DBC-драйвер мапит сотни строк с JOIN-ами в памяти JVM.
* **Предсказуемость:** Разработчик пишет чистый SQL, нет магии ORM, отсутствуют проблемы N+1.
* **Гибкость:** Легко мапить любые иерархии объектов (вложенные классы, списки) через встроенные возможности Jackson.

**Отрицательные:**
* **Бойлерплейт:** Приходится писать SQL для базовых операций CRUD и вручную мапить параметры в bind.
* **Vendor Lock-in:** Жесткая привязка к PostgreSQL и его функциям работы с JSONB (не является проблемой, так как миграция на другую СУБД не планируется).
* **Сложность рефакторинга:** При переименовании полей в классе нужно не забыть переименовать ключи в jsonb_build_object внутри SQL-строки (IDE не всегда может отследить эту связь).

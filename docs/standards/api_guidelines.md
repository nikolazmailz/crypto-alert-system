# API Design Guidelines

**Status:** Accepted
**Last Updated:** 2024-02-18

## 1. Общие принципы

### 1.1. API First
Мы начинаем разработку с контракта (OpenAPI / Swagger YAML).
Код генерируется (или пишется) на основе спецификации, а не наоборот.

### 1.2 alternative: Мы используем подход **Code-First с автоматической генерацией OpenAPI (Swagger)**.
* Контроллеры и DTO должны быть размечены аннотациями `@Tag`, `@Operation`, `@Schema`.
* Swagger UI доступен на каждом сервисе по пути `/swagger-ui.html`.
* Запрещено вносить изменения в API без обновления описания в Swagger.


## 2. RESTful Maturity
Мы используем REST Level 2 (Resource URIs + HTTP Verbs). HATEOAS не используем (избыточная сложность).

### 2.1 Формат URL и Ресурсы
* **Множественное число:** Используем существительные во множественном числе.
  * ✅ `/api/v1/alerts`
  * ❌ `/api/v1/alert`
* **Стиль написания:** Строго `kebab-case` для путей.
  * ✅ `/api/v1/market-data`
  * ❌ `/api/v1/marketData` или `/api/v1/market_data`
* **Вложенность (Иерархия):** Используется для явного указания связи "родитель-ребенок".
  * ✅ `/api/v1/users/{userId}/alerts` (Алерты конкретного пользователя)
  * ✅ `/api/v1/users/{id}/wallets` (кошельки конкретного юзера)
* **Глаголы запрещены:** Метод действия задается HTTP-глаголом. Исключения делаются только для RPC-подобных бизнес-операций.
  * ❌ `/api/v1/getAlerts`
  * ❌ `/api/v1/alerts/create`

### 2.2. HTTP Методы и Идемпотентность

| Метод | Описание | Идемпотентность | Успешный статус | Пример использования |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | Получение ресурса или списка. Запрещено менять состояние БД. | ✅ Да | `200 OK` | `GET /api/v1/alerts/123` |
| **POST** | Создание нового ресурса. | ❌ Нет | `201 Created` (+ заголовок `Location`) | `POST /api/v1/alerts` |
| **PUT** | Полная замена ресурса. Клиент передает объект целиком. | ✅ Да | `200 OK` | `PUT /api/v1/alerts/123` |
| **PATCH**| Частичное обновление (например, смена статуса). | ❌/✅ Зависит | `200 OK` | `PATCH /api/v1/alerts/123/status` |
| **DELETE**| Удаление ресурса (физическое или soft-delete). | ✅ Да | `204 No Content` | `DELETE /api/v1/alerts/123` |


## 3 Request & Response Body

### 3.1. Форматирование JSON
Все ключи в JSON передаются строго в `camelCase`.
```json
{
  "portfolioId": "123",
  "createdAt": "2024-02-18T10:00:00Z"
}
```

### 3.2. Dates
Все даты строго в ISO-8601 UTC.
* ✅ 2024-02-18T14:30:00Z
* ❌ 18.02.2024, Timestamp (long)

### 3.3. Error Handling (RFC 7807)
Мы используем стандарт Problem Details for HTTP APIs. \
При любой ошибке возвращается JSON:
```json
{
  "type": "[https://crypto-alert-system.com/errors/validation-failed](https://crypto-alert-system.com/errors/validation-failed)",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Target price must be greater than zero",
  "instance": "/api/v1/alerts",
  "invalidParams": [
    {
      "field": "targetPrice",
      "reason": "Must be positive"
    }
  ]
}
```


## 4. Versioning
Версионирование через URL.
* /api/v1/...
* При ломающих изменениях выпускаем /api/v2/....
* Все публичные API должны версионироваться через URL.
* Базовый путь: `/api/v{major}/...` (например, `/api/v1/alerts`).
* Минорные версии не отражаются в URL (клиент должен быть обратно совместим с добавлением новых полей в JSON).


## 5. Documentation (Swagger)
Каждый контроллер обязан иметь аннотации Swagger (Springdoc OpenAPI).
* @Operation(summary = "...")
* @ApiResponse(responseCode = "404", description = "...")

```kotlin
@Tag(name = "Alerts", description = "Управление пользовательскими алертами")
@RestController
@RequestMapping("/api/v1/alerts")
class AlertController {

    @Operation(summary = "Создать новый алерт на цену")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Алерт успешно создан"),
        ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    ])
    @PostMapping
    suspend fun createAlert(@Valid @RequestBody request: CreateAlertRequest): ResponseEntity<AlertResponse> {
        // ... implementation
    }
}
```


## 6. Пагинация и Фильтрация
* Метод GET для списков должен возвращать постраничный результат.
* Используем query-параметры page (с 0) и size.
* Пример: GET /api/v1/alerts?page=0&size=20&status=ACTIVE

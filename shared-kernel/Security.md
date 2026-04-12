### Добавить JWT в любой новый сервис — 3 действия

```kotlin

// 1. build.gradle.kts
implementation(libs.spring.boot.starter.security)
implementation(libs.jjwt.api)
runtimeOnly(libs.jjwt.impl)
runtimeOnly(libs.jjwt.jackson)

// 2. SecurityConfig.kt — только маршруты, бины уже есть
@Configuration @EnableWebFluxSecurity
class SecurityConfig(
    private val authenticationManager: JwtReactiveAuthenticationManager,
    private val securityContextRepository: JwtSecurityContextRepository,
) {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http
        .authenticationManager(authenticationManager)
        .securityContextRepository(securityContextRepository)
        .authorizeExchange { it.anyExchange().authenticated() }
        .build()
}
```

```yaml
# 3. application.yml — тот же секрет!
jwt:
  secret: "${JWT_SECRET:crypto-alert-system-dev-secret-key-min-32-chars}"
```

# Как устроен Security в проекте

> Вся система безопасности работает по принципу **«каждый запрос несёт токен, токен несёт идентификатор пользователя»**. Никаких сессий, никакого состояния на сервере.

---

## Слой 1 — Auto-Configuration в `shared-kernel`

Когда Spring Boot стартует любой сервис, он сканирует файл `META-INF/spring/AutoConfiguration.imports`. Там зарегистрирован `JwtSecurityAutoConfiguration`.

Он активируется только при **двух условиях одновременно**:

- в `classpath` есть `spring-security` (то есть сервис явно добавил его в зависимости)
- в `application.yml` есть `jwt.secret`

Если оба условия выполнены — auto-config создаёт три бина:

| Бин | Роль |
|-----|------|
| `JwtSecurityService` | Работа с JWT-токенами |
| `JwtReactiveAuthenticationManager` | Валидация токенов |
| `JwtSecurityContextRepository` | Контекст безопасности на входе |

Если нет — они не создаются вообще, и сервис работает без security.

---

## Слой 2 — `JwtSecurityContextRepository` (фильтр на входе)

Это **первое, что видит каждый входящий HTTP-запрос**.

- Смотрит на заголовок `Authorization: Bearer <token>`
- Если заголовок **есть** — создаёт «заготовку» аутентификации (не аутентифицированную ещё, просто несущую сырой токен) и передаёт её дальше
- Если заголовка **нет** — запрос идёт без аутентификации (и `SecurityWebFilterChain` потом решает, пропустить ли его)

---

## Слой 3 — `JwtReactiveAuthenticationManager` (валидация)

Получает ту «заготовку» с токеном.

- ✅ **Токен валиден** — проверяет подпись и срок действия, извлекает `userId`, создаёт полноценный объект `Authentication` с этим `userId` внутри
- ❌ **Токен невалиден** — бросает исключение, запрос получает `401`

---

## Слой 4 — `ReactiveSecurityContextHolder`

В реактивном стеке нет `ThreadLocal` (потоков не хватит на всех). Вместо этого `SecurityContext` «едет» вместе с запросом в **Reactor Context** — это специальный контейнер данных внутри реактивной цепочки.

Любой `suspend fun` в рамках обработки этого запроса может достать его оттуда:

```kotlin
ReactiveSecurityContextHolder.getContext()
```

---

## Слой 5 — `SecurityWebFilterChain` (правила маршрутов)

Каждый сервис определяет свои правила доступа:

| Сервис | Путь | Доступ |
|--------|------|--------|
| `account-service` | `/auth/**` | `permitAll` — регистрация и вход |
| `account-service` | Всё остальное | Только с токеном |
| `alert-service` | Всё | Только с токеном |

---

## Слой 6 — `currentUserId()`

Удобный хелпер в `shared-kernel`. Достаёт `SecurityContext`, берёт из него `Authentication`, читает `.name` и парсит как `UUID`.

```kotlin
suspend fun currentUserId(): UUID {
    return ReactiveSecurityContextHolder.getContext()
        .awaitSingle()
        .authentication
        .name
        .let { UUID.fromString(it) }
}
```

Любой `suspend`-метод в любом сервисе может вызвать его и получить ID текущего пользователя.

---

## Поток запроса целиком

```
HTTP-запрос
    │
    ▼
[Слой 2] JwtSecurityContextRepository
    │  читает Authorization header → «заготовка» с токеном
    ▼
[Слой 3] JwtReactiveAuthenticationManager
    │  валидирует токен → Authentication(userId)   ── невалиден → 401
    ▼
[Слой 4] ReactiveSecurityContextHolder
    │  кладёт Authentication в Reactor Context
    ▼
[Слой 5] SecurityWebFilterChain
    │  проверяет правила маршрутов
    ▼
Бизнес-логика
    │  вызывает currentUserId() при необходимости
    ▼
[Слой 6] currentUserId()
    └─ достаёт userId из контекста → UUID
```

---

## Интерфейс `Authentication` в `Spring Security`
наследует от Java-интерфейса `Principal`, у которого есть единственный метод: getName(): String. \
Это буквально "имя" аутентифицированной сущности — строка-идентификатор. \
Когда `JwtReactiveAuthenticationManager` создаёт объект аутентификации, он делает это так:
```kotlin
UsernamePasswordAuthenticationToken(userId.toString(), token, emptyList())
```
Первый аргумент конструктора — это `principal` (кто аутентифицирован). \
Метод `getName()` у `UsernamePasswordAuthenticationToken` возвращает именно его строковое представление, то есть то, что передано первым аргументом.
Получается цепочка: мы положили `userId.toString()` как `principal → authentication.name` \
возвращает `getName()` → это и есть наш `userId.toString() → UUID.fromString()` восстанавливает `UUID`. \
Это стандартный контракт `Spring Security`: name — это "кто этот пользователь", \
и мы решили, что "кто" — это `UUID` из нашей базы данных. \
В классическом приложении там был бы username или email, но `UUID` удобнее для внутренних сервисных вызовов.

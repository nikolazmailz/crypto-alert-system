# Журнал архитектурных решений (Architecture Decision Log)

В этом разделе документируются ключевые архитектурные решения, принятые в рамках разработки проекта. Мы используем формат [Architecture Decision Records (ADR)](https://adr.github.io/).

## Глоссарий статусов

* **🟡 Proposed** (Предложено) — решение вынесено на обсуждение, но еще не принято.
* **🟢 Accepted** (Принято) — решение утверждено и применяется в проекте.
* **🟠 Deprecated** (Устарело) — решение более не актуально, новые компоненты не должны его использовать, но старый код еще существует.
* **🔵 Superseded** (Заменено) — решение было заменено другим ADR (обычно указывается ссылка на новый ADR).
* **🔴 Rejected** (Отклонено) — решение обсуждалось, но было отклонено.

## Оглавление

| ID                                            | Дата       | Название                                                         | Статус |
|:----------------------------------------------|:-----------|:-----------------------------------------------------------------| :--- |
| [001](001-adoption-of-reactive-stack.md)      | 2026-02-24 | Использование реактивного стека                                  | 🟢 Accepted |
| [002](002-architecture-style-modulith.md)     | 2026-02-24 | Использование архитектуры Modulith                               | 🟢 Accepted |
| [003](003-architecture-style-microservice.md) | 2026-02-24 | Использование архитектуры Microservices Architecture             | 🔴 Rejected |
| [004](004-clean-architecture.md)              | 2026-02-24 | Используем принципы **Clean Architecture (Robert Martin)**       | 🟢 Accepted |
| [005](005-use-api-first-approach.md)          | 2026-02-24 | Использование подхода API-First                                  | 🟢 Accepted |
| [006](006-use-code-first-approach.md)         | 2026-02-24 | Использование подхода Code-First для документирования API        | 🔴 Rejected |
| [007](007-use-liquibase-approach.md)          | 2026-02-25 | Использование Liquibase для миграций данных                      | 🟢 Accepted |
| [008](008-use-flyway-approach.md)             | 2026-02-25 | Использование Flyway для миграций данных                         | 🔴 Rejected |
| [009](009-database-client-and-jsonb.md)       | 2026-02-25 | Использование DatabaseClient для работы с БД                     | 🟢 Accepted |
| [010](010-circuitbreaker.md)                  | 2026-03-26 | Использование библиотеки Resilience4j и паттерна Circuit Breaker | 🟢 Accepted |


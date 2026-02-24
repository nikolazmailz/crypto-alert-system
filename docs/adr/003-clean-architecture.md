# [ADR-003] Clean Architecture (Hexagonal)

**Status:** Accepted
**Date:** 2024-02-18

## Context
В стандартных Spring-проектах часто смешивается бизнес-логика и инфраструктура (JPA сущности протекают в контроллеры, логика размазана по сервисам). Это усложняет тестирование и замену компонентов (например, переход с REST на gRPC или с Postgres на Mongo).

## Decision
Мы используем принципы **Clean Architecture (Robert Martin)**.

Основные правила:
1.  **Dependency Rule:** Зависимости направлены строго внутрь. Домен не знает ничего о БД и Вебе.
2.  **Layers:**
  * **Domain (Core):** Сущности, поддерживают принцип единой ответственности(сущность изменяем сама себя), интерфейсы репозиториев, в редком случае компоненты, низкоуровневый вариант использования (не бизнес-правила). Чистый Kotlin.
  * **Application (Use Cases):** Бизнес-правила, сценарии использования (Services/Interactors). Оркестрируют потоки данных.
  * **Infrastructure (R2DBC, Kafka, WebClients)**
  * **Controllers (Web Controllers)**

## Implementation Details
* Мы НЕ используем JPA/R2DBC сущности в бизнес-логике. Обязателен маппинг: `DTO -> Domain Model -> Entity`.
* Интерфейсы репозиториев (`Ports`) объявляются в слое Domain/Application, реализация — в Infrastructure.

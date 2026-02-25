# [ADR-003] Microservices Architecture

**Status:** Accepted
**Date:** 2024-02-18

## Context
Мы создаем High-Load систему мониторинга криптовалют.
Требования:
* Независимое масштабирование (агрегатор котировок требует много CPU/Network, рассыльщик уведомлений — I/O).
* Отказоустойчивость (падение одного модуля не должно валить всю систему).
* Возможность использования разных технологий.

## Decision
Мы выбираем **Microservices Architecture**.
Система разбивается на автономные сервисы:
1.  `market-data-service` (Ingestion).
2.  `portfolio-manager-service` (Core Domain).
3.  `notification-dispatcher` (Egress).
4.  `shared-kernel` Общий код (DTO, Exceptions, Utils).

Коммуникация:
* Синхронная: REST (WebFlux) для запросов "здесь и сейчас".
* Асинхронная: Message Broker (Kafka/RabbitMQ) для событий (Event-Driven).

## Consequences
**Плюсы:**
* Изоляция сбоев.
* Независимый деплой.
* Четкие границы контекстов (Bounded Contexts).

**Минусы:**
* Сложность инфраструктуры (Docker, K8s, Service Discovery).
* Распределенные транзакции (Saga pattern).
* Сложность отладки (Distributed Tracing).

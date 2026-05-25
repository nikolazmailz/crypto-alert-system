# 📐 Architecture Decision Records

В этой папке хранятся записи о принятых архитектурных и процессных решениях команды.

## Что такое ADR

ADR (Architecture Decision Record) — короткий документ, фиксирующий:
- **контекст** принятия решения,
- **само решение**,
- **рассмотренные альтернативы**,
- **последствия**.

Подробнее: [Documenting Architecture Decisions — Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

## Индекс

| № | Название | Статус | Дата |
|---|---|---|---|
| [0001](./0001-git-flow-strategy.md) | Git Flow Strategy | ✅ Accepted | 2026-05-25 |

## Как добавить новый ADR

1. Скопируй [`template.md`](./template.md) и переименуй в `NNNN-kebab-case-title.md`, где `NNNN` — следующий порядковый номер (с ведущими нулями).
2. Заполни разделы документа.
3. Поставь статус `Proposed`.
4. Открой PR с тегом `adr` и тегни stakeholder-ов.
5. После обсуждения и принятия — обнови статус на `Accepted` и добавь запись в этот индекс.

## Статусы

| Статус | Значение |
|---|---|
| `Proposed` | Предложено, обсуждается |
| `Accepted` | Принято и действует |
| `Deprecated` | Устарело, не действует, но не заменено |
| `Superseded by ADR-XXXX` | Заменено новым ADR |

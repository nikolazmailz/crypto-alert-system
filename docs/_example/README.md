# 📘 Engineering Guidelines

> Единый свод инженерных стандартов и практик команды.
> Документ описывает принятые подходы к разработке, ветвлению, ревью и релизам.

---

## 📑 Содержание

- [Обзор](#обзор)
- [Git Flow](#-git-flow)
- [Naming Conventions для веток](#-naming-conventions-для-веток)
- [Commit Messages](#-commit-messages)
- [Pull Request / Merge Request](#-pull-request--merge-request)
- [Code Review](#-code-review)
- [Версионирование (SemVer)](#-версионирование-semver)
- [Architecture Decision Records (ADR)](#-architecture-decision-records-adr)
- [Полезные ссылки](#-полезные-ссылки)

---

## Обзор

Этот документ — точка входа для всех инженеров команды. Он описывает **что** мы делаем и **почему**, со ссылками на внешние стандарты и внутренние ADR с обоснованиями принятых решений.

| Раздел | Стандарт | ADR |
|---|---|---|
| Стратегия ветвления | [Git Flow by Vincent Driessen](https://nvie.com/posts/a-successful-git-branching-model/) | [ADR-0001](./docs/adr/0001-git-flow-strategy.md) |
| Naming веток | внутренний | [ADR-0001](./docs/adr/0001-git-flow-strategy.md) |
| Формат коммитов | [Conventional Commits 1.0.0](https://www.conventionalcommits.org/ru/v1.0.0/) | [ADR-0001](./docs/adr/0001-git-flow-strategy.md) |
| Версионирование | [Semantic Versioning 2.0.0](https://semver.org/lang/ru/) | [ADR-0001](./docs/adr/0001-git-flow-strategy.md) |
| Code Review | внутренний | [ADR-0001](./docs/adr/0001-git-flow-strategy.md) |

---

## 🌿 Git Flow

Мы используем **классическую модель Git Flow** от Vincent Driessen с долгоживущими ветками `main` и `develop` и набором вспомогательных веток.

> 📄 Полное обоснование выбора и альтернативы — в [ADR-0001: Git Flow Strategy](./docs/adr/0001-git-flow-strategy.md)

### Структура веток

```
main         ──●─────────────●─────────●──────────►   (production, тегированные релизы)
                 \           /         /
release/*         \         ●─●       /                (стабилизация перед релизом)
                   \       /   \     /
develop      ──●────●─●───●─────●───●──────────────►   (интеграционная ветка)
                \  / \   /
feature/*        ●●   ●─●                              (новый функционал)

hotfix/*  ────────────────────●─●                      (срочные правки прода)
                              │ │
                              ▼ ▼
                          main + develop
```

### Типы веток

| Ветка | Назначение | Откуда | Куда мержится |
|---|---|---|---|
| `main` | Production-код, только релизы | — | — |
| `develop` | Интеграция фич для следующего релиза | `main` (один раз) | — |
| `feature/*` | Разработка новой функциональности | `develop` | `develop` |
| `release/*` | Подготовка релиза, баг-фиксы и метаданные | `develop` | `main` + `develop` |
| `hotfix/*` | Срочные правки production | `main` | `main` + `develop` |
| `bugfix/*` | Исправление багов в `develop` | `develop` | `develop` |

### Основные команды

<details>
<summary><b>🆕 Создание feature-ветки</b></summary>

```bash
git checkout develop
git pull origin develop
git checkout -b feature/JIRA-123-add-user-auth

# ... работа ...

git push -u origin feature/JIRA-123-add-user-auth
# Открыть Merge Request в develop
```
</details>

<details>
<summary><b>🚀 Подготовка релиза</b></summary>

```bash
git checkout develop
git pull origin develop
git checkout -b release/1.4.0

# Только bugfix-ы и обновление version, CHANGELOG.md
# НЕЛЬЗЯ добавлять новые фичи

git push -u origin release/1.4.0
# Открыть MR в main
```

После мержа в `main`:

```bash
git checkout main
git pull origin main
git tag -a v1.4.0 -m "Release 1.4.0"
git push origin v1.4.0

# Обратный мерж в develop
git checkout develop
git merge --no-ff main
git push origin develop
```
</details>

<details>
<summary><b>🚑 Hotfix production</b></summary>

```bash
git checkout main
git pull origin main
git checkout -b hotfix/1.4.1-critical-auth-fix

# ... фикс ...

git push -u origin hotfix/1.4.1-critical-auth-fix
# MR в main → после мержа создать тег v1.4.1
# Затем обратный мерж в develop
```
</details>

<details>
<summary><b>🔄 Синхронизация feature с develop</b></summary>

```bash
git checkout feature/JIRA-123-add-user-auth
git fetch origin
git rebase origin/develop      # предпочтительно для коротких веток
# или
git merge origin/develop       # для долгих веток с уже выложенными коммитами
```
</details>

---

## 🏷 Naming Conventions для веток

Формат: `<type>/<TICKET-ID>-<kebab-case-description>`

### Допустимые префиксы

| Префикс | Использование | Пример |
|---|---|---|
| `feature/` | Новая функциональность | `feature/JIRA-123-user-profile-page` |
| `bugfix/` | Исправление бага в `develop` | `bugfix/JIRA-456-fix-login-redirect` |
| `hotfix/` | Срочная правка прода | `hotfix/1.4.1-payment-gateway-timeout` |
| `release/` | Подготовка релиза | `release/1.4.0` |
| `chore/` | Технические задачи без бизнес-фич | `chore/upgrade-node-20` |
| `docs/` | Изменения только в документации | `docs/update-readme` |
| `experiment/` | Эксперименты, R&D | `experiment/try-new-cache-layer` |

### Правила

✅ **Хорошо**
- `feature/JIRA-892-add-2fa-support`
- `bugfix/JIRA-1024-fix-date-parsing-utc`
- `hotfix/2.1.3-fix-memory-leak-worker`

❌ **Плохо**
- `my-branch` — нет типа и описания
- `feature/new_stuff` — snake_case вместо kebab-case
- `Feature/JIRA-123-Add-Auth` — PascalCase, заглавные буквы
- `feature/very-long-and-detailed-description-of-what-this-branch-actually-does` — слишком длинно (max ~50 символов)

### Технические требования

- Только `[a-z0-9-]` и слеш-разделитель
- Длина описания: 3–50 символов
- Тикет-ID обязателен, кроме `release/`, `chore/`, `docs/`, `experiment/`

---

## ✍️ Commit Messages

Используем [**Conventional Commits 1.0.0**](https://www.conventionalcommits.org/ru/v1.0.0/).

> 📄 Обоснование выбора и альтернативы — в [ADR-0001](./docs/adr/0001-git-flow-strategy.md)

### Структура

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Типы коммитов

| Тип | Назначение | Влияние на версию |
|---|---|---|
| `feat` | Новая функциональность | MINOR |
| `fix` | Исправление бага | PATCH |
| `docs` | Только документация | — |
| `style` | Форматирование, без изменения логики | — |
| `refactor` | Рефакторинг без новой функциональности | — |
| `perf` | Улучшение производительности | PATCH |
| `test` | Добавление/правка тестов | — |
| `build` | Сборка, зависимости | — |
| `ci` | CI/CD конфигурация | — |
| `chore` | Прочее обслуживание | — |
| `revert` | Откат предыдущего коммита | — |

### Breaking Changes

Помечаются через `!` после типа **или** через `BREAKING CHANGE:` в футере. Влияют на MAJOR версию.

### Примеры

✅ **Хорошо**

```
feat(auth): add OAuth2 Google provider

Implements Google sign-in flow using authorization code grant.
Closes JIRA-892.
```

```
fix(api): handle null in user response

Returns empty object instead of crashing when backend
returns null for missing user profile.

Refs JIRA-1024
```

```
feat(api)!: change user.id from int to UUID

BREAKING CHANGE: User.id type changed from integer to UUID string.
All clients must update their type definitions.
```

❌ **Плохо**

```
fixed bug             ← нет типа, нет описания
update                ← бессмысленно
WIP                   ← не оставляем WIP в истории
Fix JIRA-123          ← нет описания, что именно исправлено
```

### Правила subject

- Императив, настоящее время: `add` не `added` / `adds`
- Без точки в конце
- ≤ 72 символа
- Первая буква строчная (после типа)

---

## 🔀 Pull Request / Merge Request

### Template

Шаблон PR находится в [`.github/PULL_REQUEST_TEMPLATE.md`](./.github/PULL_REQUEST_TEMPLATE.md) и автоматически подставляется при создании PR.

### Требования к PR

| Требование | Описание |
|---|---|
| 📝 Описание | Что и зачем изменено |
| 🎫 Ссылка на тикет | JIRA / Linear / GitHub Issue |
| ✅ Чек-лист | Тесты, документация, миграции |
| 🟢 CI | Все проверки пройдены |
| 👀 Approvals | Минимум 1 approve (2 для критичных частей) |
| 📦 Размер | До 400 строк изменений (рекомендация) |
| 🎯 Один scope | Один PR = одна логическая задача |

### Merge стратегия

- **`feature/*` → `develop`** — `Squash and merge` (одна задача = один коммит)
- **`release/*` → `main`** — `Merge commit` (сохранить историю релиза)
- **`hotfix/*` → `main`** — `Merge commit`
- **`main` → `develop`** (back-merge) — `Merge commit`

---

## 👀 Code Review

### Принципы

1. **Ревью — это диалог, а не экзамен.** Цель — улучшить код вместе, а не показать ошибку.
2. **Ревью обязателен.** Никакой merge в `develop`/`main` без approve.
3. **SLA на ревью — 1 рабочий день.** Если занят — сообщи и делегируй.
4. **Маленькие PR — быстрые ревью.** PR > 400 строк → запросить декомпозицию.

### Что проверяем

#### Обязательно
- [ ] Код решает заявленную задачу
- [ ] Нет очевидных багов и edge cases без обработки
- [ ] Покрытие тестами для новой логики
- [ ] Нет утечек секретов, токенов, паролей
- [ ] SQL-запросы безопасны (нет инъекций)
- [ ] Производительность критичных мест не деградирует

#### Желательно
- [ ] Читаемость и нейминг
- [ ] Соответствие архитектурным решениям (ADR)
- [ ] Документация обновлена
- [ ] Нет дублирования (DRY)

### Уровни замечаний

| Префикс | Значение | Блокирует merge? |
|---|---|---|
| `[blocker]` | Критичная проблема | ✅ Да |
| `[issue]` | Нужно поправить | ✅ Да |
| `[suggestion]` | Предложение | ❌ Нет |
| `[nit]` | Мелочь, на усмотрение | ❌ Нет |
| `[question]` | Вопрос на понимание | ❌ Нет |
| `[praise]` | Хорошее решение 👍 | ❌ Нет |

### Пример комментария

> `[issue]` Здесь не обработан случай, когда `user` приходит как `null` — упадёт `TypeError`. Предлагаю добавить early return или использовать optional chaining.

---

## 🏷 Версионирование (SemVer)

Используем [**Semantic Versioning 2.0.0**](https://semver.org/lang/ru/).

Формат: `MAJOR.MINOR.PATCH`

| Компонент | Когда увеличиваем |
|---|---|
| **MAJOR** | Breaking changes — несовместимые изменения API |
| **MINOR** | Новая функциональность с обратной совместимостью |
| **PATCH** | Исправления багов с обратной совместимостью |

### Pre-release версии

```
1.5.0-alpha.1     ← ранние тесты
1.5.0-beta.2      ← фича-фриз, бета-тестинг
1.5.0-rc.1        ← release candidate
1.5.0             ← релиз
```

### Связь с Conventional Commits

| Коммиты в релизе | Версия |
|---|---|
| Только `fix:` / `perf:` | PATCH (1.4.0 → 1.4.1) |
| Есть `feat:` | MINOR (1.4.1 → 1.5.0) |
| Есть `!` или `BREAKING CHANGE:` | MAJOR (1.5.0 → 2.0.0) |

### Теги в Git

```bash
git tag -a v1.5.0 -m "Release 1.5.0"
git push origin v1.5.0
```

Префикс `v` обязателен.

---

## 📐 Architecture Decision Records (ADR)

Архитектурные решения фиксируются в формате ADR в папке [`docs/adr/`](./docs/adr/).

### Когда писать ADR

ADR пишется, когда принимается решение, которое:
- Затрагивает архитектуру или процессы команды
- Имеет несколько альтернатив, требующих обсуждения
- Будет сложно отменить позднее
- Должно быть объяснено новым участникам команды

### Как создать новый ADR

1. Скопируй [`docs/adr/template.md`](./docs/adr/template.md)
2. Назови файл `NNNN-kebab-case-title.md`, где `NNNN` — следующий порядковый номер
3. Заполни разделы
4. Открой PR с тегом `adr` для обсуждения
5. После принятия — обнови статус на `Accepted`

### Текущие ADR

| № | Название | Статус |
|---|---|---|
| [0001](./docs/adr/0001-git-flow-strategy.md) | Git Flow Strategy | ✅ Accepted |

---

## 🔗 Полезные ссылки

### Внешние стандарты

- [Git Flow — Vincent Driessen](https://nvie.com/posts/a-successful-git-branching-model/)
- [Conventional Commits 1.0.0](https://www.conventionalcommits.org/ru/v1.0.0/)
- [Semantic Versioning 2.0.0](https://semver.org/lang/ru/)
- [Keep a Changelog](https://keepachangelog.com/ru/1.1.0/)
- [Atlassian: Comparing Workflows](https://www.atlassian.com/git/tutorials/comparing-workflows)

### Инструменты

- [commitlint](https://commitlint.js.org/) — линтер сообщений коммитов
- [Husky](https://typicode.github.io/husky/) — git hooks
- [semantic-release](https://semantic-release.gitbook.io/) — автоматическая публикация релизов
- [release-please](https://github.com/googleapis/release-please) — генератор CHANGELOG из коммитов

### Чтиво про code review

- [Google Engineering Practices: Code Review](https://google.github.io/eng-practices/review/)
- [Conventional Comments](https://conventionalcomments.org/)

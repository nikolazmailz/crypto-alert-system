# ADR-0001: Git Flow Strategy

| Поле | Значение |
|---|---|
| **Статус** | ✅ Accepted |
| **Дата** | 2026-05-25 |
| **Авторы** | @engineering-team |
| **Stakeholders** | @tech-lead, @architect, @qa-lead |

---

## 📌 Контекст

Команда запускает новый проект и нуждается в едином и формализованном подходе к работе с системой контроля версий. Без согласованных правил мы рискуем получить:

- хаотичную историю коммитов, в которой сложно ориентироваться;
- конфликты при параллельной разработке нескольких фич;
- проблемы с откатами и hotfix-ами на production;
- невозможность автоматизировать релизы и changelog;
- разные ожидания у новых членов команды.

Этот ADR объединяет в себе **все решения по Git Flow процессам**, чтобы не плодить мелкие ADR по каждому аспекту. По мере накопления опыта отдельные части могут быть вынесены в собственные ADR (со ссылкой на эту запись через статус `Superseded by`).

В рамках этого решения мы фиксируем:

1. [Стратегию ветвления](#1-стратегия-ветвления)
2. [Naming convention для веток](#2-naming-convention-для-веток)
3. [Формат commit messages](#3-формат-commit-messages)
4. [Стратегию версионирования](#4-стратегия-версионирования)
5. [Стратегию merge для разных типов веток](#5-стратегия-merge)
6. [Code Review процесс](#6-code-review-процесс)

---

## 🎯 Решение

### 1. Стратегия ветвления

**Используем классический Git Flow от Vincent Driessen.**

| Ветка | Тип | Назначение |
|---|---|---|
| `main` | долгоживущая | Production-код, только релизы с тегами |
| `develop` | долгоживущая | Интеграционная ветка для следующего релиза |
| `feature/*` | временная | Разработка новой функциональности |
| `release/*` | временная | Подготовка и стабилизация релиза |
| `hotfix/*` | временная | Срочные правки production |
| `bugfix/*` | временная | Исправления багов до релиза |

**Ключевые правила:**

- В `main` коммитят **только через релизы или hotfix**, прямые коммиты запрещены.
- В `develop` коммитят **только через MR** из feature/bugfix-веток.
- `release/*` создаётся из `develop`, после стабилизации мержится в `main` **и** обратно в `develop`.
- `hotfix/*` создаётся из `main`, после фикса мержится в `main` **и** в `develop` (или активный `release/*`).

### 2. Naming convention для веток

Формат: `<type>/<TICKET-ID>-<kebab-case-description>`

| Префикс | Источник | Целевая ветка | Тикет обязателен |
|---|---|---|---|
| `feature/` | `develop` | `develop` | ✅ |
| `bugfix/` | `develop` | `develop` | ✅ |
| `hotfix/` | `main` | `main` + `develop` | ❌ (версия вместо тикета) |
| `release/` | `develop` | `main` + `develop` | ❌ (версия вместо тикета) |
| `chore/` | `develop` | `develop` | ❌ |
| `docs/` | `develop` | `develop` | ❌ |
| `experiment/` | `develop` | удаляется или `develop` | ❌ |

**Технические ограничения:**
- символы: `[a-z0-9-]` и `/` как разделитель
- длина описания: 3–50 символов
- запрещено: пробелы, подчёркивания, заглавные буквы, кириллица

### 3. Формат commit messages

**Используем [Conventional Commits 1.0.0](https://www.conventionalcommits.org/ru/v1.0.0/).**

Структура:
```
<type>(<scope>): <subject>

<body>

<footer>
```

Разрешённые типы: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`.

Breaking changes: `!` после типа или `BREAKING CHANGE:` в футере.

**Линтер коммитов:**
- [commitlint](https://commitlint.js.org/) с конфигом `@commitlint/config-conventional`
- запуск через [Husky](https://typicode.github.io/husky/) на `commit-msg` хуке

### 4. Стратегия версионирования

**Используем [Semantic Versioning 2.0.0](https://semver.org/lang/ru/).**

`MAJOR.MINOR.PATCH` с правилами:

- `MAJOR` — breaking changes;
- `MINOR` — новая функциональность (обратно совместимая);
- `PATCH` — багфиксы (обратно совместимые).

Pre-release: `1.5.0-alpha.1`, `1.5.0-beta.2`, `1.5.0-rc.1`.

Теги в git создаются с префиксом `v`: `v1.5.0`.

CHANGELOG ведём по [Keep a Changelog](https://keepachangelog.com/ru/1.1.0/), генерируется автоматически из Conventional Commits.

### 5. Стратегия merge

| Откуда → Куда | Стратегия | Обоснование |
|---|---|---|
| `feature/*` → `develop` | **Squash and merge** | Одна задача = один коммит в истории `develop` |
| `bugfix/*` → `develop` | **Squash and merge** | Аналогично |
| `release/*` → `main` | **Merge commit** (`--no-ff`) | Сохраняем явный коммит релиза |
| `hotfix/*` → `main` | **Merge commit** (`--no-ff`) | Видна точка hotfix-а в истории |
| `main` → `develop` (back-merge) | **Merge commit** (`--no-ff`) | Возврат изменений в develop |
| Синхронизация feature с develop | **Rebase** | Линейная история коротких веток |

Rebase разрешён **только** на ветках, которые ещё не были опубликованы в общую работу или ревью.

### 6. Code Review процесс

**Минимальные требования к PR:**

- 1+ approve для `feature`/`bugfix` в `develop`
- 2+ approve для `release`/`hotfix` в `main`
- зелёный CI: lint, unit-tests, build
- ссылка на тикет в описании
- размер: рекомендация ≤ 400 строк изменений

**SLA на ревью:** 1 рабочий день. При большей нагрузке — делегирование коллеге.

**Комментарии оформляем по [Conventional Comments](https://conventionalcomments.org/):** `[blocker]`, `[issue]`, `[suggestion]`, `[nit]`, `[question]`, `[praise]`.

---

## 🔄 Альтернативы

### Альтернатива 1: GitHub Flow

**Описание:** Только `main` + feature-ветки. Релиз = деплой текущего `main`.

**Плюсы:**
- Максимально простой процесс
- Подходит для continuous deployment
- Меньше overhead на синхронизацию веток

**Минусы:**
- Нет явной фазы стабилизации релиза
- Сложнее поддерживать несколько версий продукта
- Hotfix и текущая разработка живут в одной ветке

**Почему отвергли:** Команда работает с релизами по расписанию, нужна фаза стабилизации, и продукт может иметь несколько активных версий у клиентов.

### Альтернатива 2: Trunk-Based Development

**Описание:** Всё мержится в `main` максимально быстро, без долгоживущих веток. Feature flags для незаконченной функциональности.

**Плюсы:**
- Минимизирует merge-конфликты
- Высокая скорость интеграции
- Поощряет маленькие изменения

**Минусы:**
- Требует зрелой культуры feature flags
- Требует развитой CI/CD инфраструктуры
- Сложнее для команды без опыта

**Почему отвергли:** Команда только начинает проект, нет готовой инфраструктуры feature-flag-ов и continuous deployment. Можем мигрировать в будущем — это будет отдельный ADR.

### Альтернатива 3: GitLab Flow

**Описание:** Git Flow + environment-ветки (`production`, `staging`, `pre-production`).

**Плюсы:**
- Чёткое соответствие веток окружениям
- Хорошо подходит для multi-environment deployment

**Минусы:**
- Добавляет сложности по сравнению с классическим Git Flow
- Дублирование процесса релиза через несколько веток

**Почему отвергли:** На старте проекта избыточно. Если потребуется multi-env workflow — добавим отдельным ADR.

### Альтернативы формату коммитов

| Формат | Почему отвергли |
|---|---|
| **Gitmoji** (`✨ Add new feature`) | Эмодзи плохо читаются в терминалах, проблемы с поиском, нет связи с SemVer |
| **JIRA-формат** (`JIRA-123: ...`) | Не структурирован, не интегрируется с автогенерацией CHANGELOG и SemVer |
| **Свободный формат** | Отсутствие стандарта = хаос в истории и невозможность автоматизации |

**Conventional Commits** выбраны как индустриальный стандарт, поддерживаемый большинством инструментов (semantic-release, commitizen, release-please).

---

## ✅ Последствия

### Положительные

- **Единый стандарт** для всей команды — снижает порог входа новых разработчиков.
- **Автоматизация релизов** становится возможной (semantic-release, release-please).
- **Чистый CHANGELOG** генерируется автоматически из истории коммитов.
- **Параллельная разработка** нескольких фич без блокировки релиза.
- **Возможность hotfix-а** прода без затрагивания текущей разработки.
- **Прослеживаемость:** ветка → тикет → коммиты → релиз → тег.

### Отрицательные / компромиссы

- **Сложнее, чем GitHub Flow** — больше типов веток, нужно учиться.
- **Overhead на back-merge** из `main` в `develop` после релизов и hotfix-ов.
- **Строгость Conventional Commits** требует дисциплины и линтера на pre-commit.
- **Долгоживущие ветки `release/*`** могут расходиться с `develop` при долгой стабилизации.

### Нейтральные

- Требуется настроить:
  - branch protection rules для `main` и `develop`;
  - commitlint + Husky;
  - PR template;
  - CI-проверки на формат веток и коммитов.

---

## 📋 План внедрения

- [ ] Настроить protected branches: `main`, `develop`
- [ ] Создать `develop` от `main`
- [ ] Добавить `.github/PULL_REQUEST_TEMPLATE.md`
- [ ] Установить и настроить `commitlint` + `husky`
- [ ] Настроить CI-чек на формат имени ветки
- [ ] Настроить автогенерацию CHANGELOG (release-please или semantic-release)
- [ ] Провести kickoff-встречу с командой
- [ ] Закрепить ссылку на этот README в onboarding-документации

---

## 🔗 Ссылки

### Стандарты

- [A successful Git branching model — Vincent Driessen](https://nvie.com/posts/a-successful-git-branching-model/)
- [Conventional Commits 1.0.0](https://www.conventionalcommits.org/ru/v1.0.0/)
- [Semantic Versioning 2.0.0](https://semver.org/lang/ru/)
- [Keep a Changelog 1.1.0](https://keepachangelog.com/ru/1.1.0/)
- [Conventional Comments](https://conventionalcomments.org/)

### Сравнения и обзоры

- [Atlassian: Comparing Workflows](https://www.atlassian.com/git/tutorials/comparing-workflows)
- [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow)
- [GitLab Flow](https://docs.gitlab.com/ee/topics/gitlab_flow.html)
- [Trunk-Based Development](https://trunkbaseddevelopment.com/)

### Инструменты

- [commitlint](https://commitlint.js.org/)
- [Husky](https://typicode.github.io/husky/)
- [semantic-release](https://semantic-release.gitbook.io/)
- [release-please](https://github.com/googleapis/release-please)
- [git-flow CLI](https://github.com/petervanderdoes/gitflow-avh)

---

## 📝 История изменений

| Дата | Автор | Изменение |
|---|---|---|
| 2026-05-25 | @engineering-team | Создание и принятие ADR |

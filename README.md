# Crypto Alert System 🚀

High-load система мониторинга криптовалют на стеке Kotlin + Spring Boot 3 + Coroutines.
Проект служит эталонной реализацией (Reference Implementation) микросервисной архитектуры.

## 📚 Документация
Вся инженерная документация находится в папке `/docs`:
* [Архитектурные решения (ADR)](docs/adr/index.md)
* [Стандарты кодирования](docs/standards/coding_style.md)
* [Стратегия тестирования](docs/standards/testing.md)

## 🛠 Технологический стек
* **Language:** Kotlin 1.9+, Java 21
* **Framework:** Spring Boot 3.4.x (WebFlux)
* **Async:** Coroutines
* **DB:** PostgreSQL + R2DBC (Reactive)
* **Testing:** Kotest, WireMock, Testcontainers
* **Build:** Gradle (Kotlin DSL) + Version Catalog

## 🚀 Быстрый старт
```bash
# Поднять инфраструктуру
docker-compose up -d

# Запустить тесты
./gradlew test
```

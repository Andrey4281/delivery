# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## О проекте

Микросервис «Delivery» из курса «Domain Driven Design и Clean Architecture на Java» (microarch.ru). Отвечает за учёт курьеров, диспетчеризацию и доставку заказов. Проект развивается по модулям курса: работа идёт в ветках `module-N`, зависимости в `pom.xml` помечены комментариями вида `<!-- 6 Module -->`.

## Команды

```bash
# Сборка + генерация кода (OpenAPI, gRPC, Protobuf)
mvn clean compile

# Запуск приложения (нужны PostgreSQL и Kafka)
mvn spring-boot:run

# Тесты
mvn test
mvn test -Dtest=OrderTest                # один класс
mvn test -Dtest=OrderTest#methodName     # один метод

# Стиль кода
mvn formatter:format                     # переформатировать код
mvn checkstyle:check                     # проверить стиль
```

`formatter:format` привязан к фазе `process-sources` и выполняется при каждой сборке — код переформатируется автоматически. Checkstyle (`config/checkstyle/checkstyle.xml`, Google-style, строка ≤ 180) запускается на `verify` с `failOnViolation=false` и сборку не ломает.

## Генерация кода из контрактов

Не писать вручную — генерируется при `mvn compile`:

- `contracts/openapi.yml` → интерфейсы контроллеров в `microarch.delivery.adapters.in.http.api` и модели в `microarch.delivery.adapters.in.http.model` (`target/generated-sources/openapi`, `interfaceOnly=true`). Контроллеры реализуют эти интерфейсы, модели используются в сигнатурах.
- `src/main/proto/geo.proto` → gRPC-клиент внешнего Geo-сервиса (пакет `clients.geo`)
- `src/main/proto/basket_events.proto`, `order_events.proto` → классы интеграционных событий Kafka (пакеты `queues.*`)

## Архитектура

Hexagonal (ports & adapters) + DDD. Раскладка пакетов `microarch.delivery.*` задекларирована в `DeliveryApplication.java` (`@EntityScan` / `@EnableJpaRepositories`):

- `core.domain.model` — агрегаты, сущности, value objects (JPA-сущности сканируются отсюда)
- `adapters.in.http` — REST-контроллеры, реализуют сгенерированные из OpenAPI интерфейсы
- `adapters.out.postgres` — JPA-репозитории, включая `outbox`

Таблицы БД: `orders`, `couriers`, `assignments`, `outbox`. Схема управляется Hibernate (`ddl-auto: update`).

### Строительные блоки DDD (`libs.ddd`)

- `BaseEntity<TId>` — базовая JPA-сущность, equals/hashCode по id (transient-объекты не равны никому)
- `Aggregate<TId>` — базовый агрегат, накапливает доменные события (`raiseDomainEvent` / `getDomainEvents` / `clearDomainEvents`)
- `ValueObject<T>` — базовый VO, равенство и сравнение через `equalityComponents()`
- `DomainEvent` — наследник Spring `ApplicationEvent`; публикуется через `DomainEventPublisher` (реализация `DefaultDomainEventPublisher` обёртывает `ApplicationEventPublisher`)

Outbox-паттерн: доменные события агрегатов после коммита попадают в таблицу `outbox` и уходят в Kafka (топик `order.events`). Входящие события — из `basket.events`.

### Обработка ошибок (`libs.errs`)

Домен не бросает исключений — возвращает `Result<T, Error>` / `UnitResult<Error>`:

- `Error.of(code, message)` — ошибка с кодом; типовые фабрики в `GeneralErrors`
- `Guard.*` — валидация, возвращает `Error` или `null` (null = ошибки нет), склейка через `Guard.combine`
- `DomainInvariantException` — fail-fast, только там, где ошибка невозможна по контракту (`Result.getValueOrThrow()`, `Error.throwIf()`)

## Инфраструктура

- Java 25, Spring Boot 3.5.5, Lombok + MapStruct (прописаны как annotation processors)
- PostgreSQL, Kafka, gRPC-клиент Geo-сервиса (конфигурация в `ApplicationProperties`, prefix `app`)
- Все параметры — через env-переменные в `application.yml`; HTTP-порт по умолчанию 8082
- Интеграционные тесты — Testcontainers (PostgreSQL), требуется Docker

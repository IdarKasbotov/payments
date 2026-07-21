# Code Review — payments service

> Ревью существующего функционала с позиции senior Java (хайлоад/микросервисы).
> Дата: 2026-06-28. Код на момент ревью не менялся — это разбор.

## 🔴 Баги (ломают функциональность)

### 1. ✅ ИСПРАВЛЕНО — `@NotBlank` на не-строковых полях — эндпоинт `getPaymentsByTypeAndDate` падал с 500
> Fixed: `@NotBlank` → `@NotNull` в `GetPaymentsByTypeAndDateRequest`. Покрыто слайс-тестом
> `PaymentsControllerTest.getPaymentsByTypeAndDate_validRequest_returns200WithPayments`.

`GetPaymentsByTypeAndDateRequest.java:21,25` — `@NotBlank` стоял на `PaymentType type` и
`LocalDate date`. `@NotBlank` валидирует только `CharSequence`. Hibernate Validator на этапе
валидации кинет `UnexpectedTypeException: No validator could be found for constraint 'NotBlank'
validating type 'PaymentType'`. То есть **каждый** вызов вернёт 500. Нужен `@NotNull`.
Самый дорогой баг — основная REST-функция из ТЗ нерабочая.

### 2. Деривация баланса без транзакции — окно пустой таблицы
`PaymentsServiceImpl.java:68-73` — `countBalancesByTypeAndDate()`: `deleteAll()` затем `save()`,
без `@Transactional`.
- Если `save` упадёт (или инстанс умрёт между шагами) — таблица `balance` остаётся **пустой** до
  следующего запуска, все запросы баланса вернут `null`. На хайлоаде это видимый инцидент.
- Даже в норме между delete и insert читатели видят пустой/частичный баланс (нет изоляции).

Минимум — `@Transactional`. Лучше — пересборка в staging-таблицу + атомарный свап, или upsert.

### 3. Тип SQL `Types.DOUBLE` для денег (`BigDecimal`)
`PaymentRepositoryImpl.java:47,58` — для `sum` указан `Types.DOUBLE`. Деньги + DOUBLE — ред-флаг.
PG-драйвер с `BigDecimal`-значением, скорее всего, отработает, но type hint неверный и может дать
потерю точности. Должно быть `Types.DECIMAL`/`NUMERIC`.

## 🟠 Хайлоад / микросервисы (ключевое для позиции)

### 4. `@Scheduled` без распределённой блокировки
`CountBalancesByTypeAndDateTask.java:16` — при деплое в >1 реплику джобу запустят **все инстансы
одновременно**. С учётом delete-all+insert без транзакции (п.2) — гонка, дубликаты, потерянные
данные. Нужен ShedLock (`@SchedulerLock`) или иной distributed lock.

### 5. Rabbit-консьюмер: нет DLQ, ретраев, идемпотентности
`PaymentConsumer.java:19-23` + `RabbitConfig.java` — очередь durable, но:
- Нет dead-letter exchange. Если `savePayment` бросит исключение — сообщение бесконечно
  реквеуится (poison message).
- At-least-once + нет ключа идемпотентности → дубль сообщения = дубль платежа. Для денег критично.
- Нет настройки `concurrency`/prefetch.

Минимум: DLX + ограниченные ретраи + дедупликация по бизнес-ключу (или `messageId`).

### 6. ✅ ИСПРАВЛЕНО — Нет индексов, нет PK на `balance`
> Fixed: в первой Liquibase-миграции `balance` получил PK `(date, type)`, а `payment` — индекс
> `payment__type_date__idx (type, date)`. Проверено на живом Postgres.

`schema.sql` — `balance` без ограничений и индексов.
- `payment WHERE type=? AND date=?` — full scan, нужен индекс `(type, date)`.
- `balance WHERE date=? AND type=?` — full scan, нужен PK/уникальный `(date, type)`. Уникальность
  заодно защитит от дублей из п.4/5.

### 7. Полная пересборка `balance` каждый запуск
`PaymentRepositoryImpl.paymentsGroupedByTypeAndDate()` — пересчёт всей таблицы и full rewrite.
Для теста ок, в проде на больших объёмах — O(всех платежей) каждый час. Стоит хотя бы упомянуть
инкрементальный/upsert-подход по затронутым `(date, type)`.

### 8. `DECIMAL(10,2)` для агрегата `balance`
`schema.sql:16` — баланс это `SUM` многих платежей, а лимит `(10,2)` ≈ 99 млн. Агрегат
переполнится легче отдельного платежа. Нужна бóльшая разрядность.

## 🟡 Дизайн / читаемость

### 9. `BalanceEntity.getType()` подменяет геттер `@Data`
`BalanceEntity.java:15-17` — поле `PaymentType type`, но геттер вручную возвращает `type.name()`
(String), чтобы `SqlParameterSourceUtils.createBatch` положил VARCHAR. Скрытый хак: тип поля и
тип геттера расходятся. Лучше конвертировать явно в SQL-параметрах.

### 10. `PaymentEntity` как переносчик агрегата
`PaymentsServiceImpl.java:75-76` — грипованный запрос (`date, type, SUM`) маппится в `PaymentEntity`
с `id=null` и держится только на том, что PG называет колонку `SUM(p.sum)` именем `sum`. Хрупко
(любой alias сломает) и грязно. Правильно — маппить сразу в `BalanceEntity`.

### 11. `updatePaymentSum` молча игнорирует несуществующий id
`PaymentRepositoryImpl.java:42-50` — `update` вернёт 0 строк, а API отдаст 200 OK. Клиент не
отличит успех от no-op. Проверять `affectedRows` → 404. Плюс `void`-POST на обновление —
семантически `PUT/PATCH`.

### 12. Эндпоинт-fetch через POST
`PaymentsController.java:40` — `getPaymentsByTypeAndDate` это чтение, но `POST` с телом; рядом
`getBalanceByTypeAndDate` — `GET`. Непоследовательно. Чтение лучше `GET` (проще кэшировать — п.3 ТЗ).

### 13. Хрупкая десериализация request-DTO
`GetPaymentsByTypeAndDateRequest`/`UpdatePaymentRequest` — только `@AllArgsConstructor`, без
no-args/`@JsonCreator`. Держится на `-parameters` (Boot 3.2+) + parameter-names модуле. Лучше явно
`@NoArgsConstructor` или `@JsonCreator`.

## 🟢 Инфра / эксплуатация

### 14. `logging.level.root: DEBUG` + логирование сумм
`application.yml:3` + логи печатают полные платежи. (а) PII — зарплаты в логах; (б) на хайлоаде
DEBUG-root убивает производительность. INFO + не логировать суммы.

### 15. ✅ ИСПРАВЛЕНО — `schema.sql` с `DROP SCHEMA ... CASCADE` и `init.mode: always`
> Fixed: `schema.sql` + `spring.sql.init` убраны, схема переведена на **Liquibase** (changelog в
> репозитории, по образцу gls-notify). DDL катится всегда, сид-данные вынесены в changeSet с
> `context=local` (`LIQUIBASE_CONTEXTS=local` по умолчанию, в проде — `prod`, сид пропускается).
> Оба сценария проверены на живом Postgres.

Сносит данные на каждом старте, мешает DDL с сидовыми данными. Для локалки норм, для «сервиса»
нужен Flyway/Liquibase + отдельные сиды.

### 16. Креды БД/Rabbit в `application.yml`
Захардкожены. Вынести в env/секреты. (Для теста — ок.)

## ⭐️ Главный козырь — тесты

Автор приходит из QA-автоматизации, а тестов ровно один — `contextLoads`
(`PaymentsApplicationTests.java`). Это естественное преимущество, которое сейчас не показано:
- юнит-тесты `PaymentsServiceImpl` (моки репозиториев);
- слайсы: `@WebMvcTest` на контроллер (поймал бы баг №1 с валидацией);
- интеграционные с **Testcontainers** (Postgres + RabbitMQ): полный путь «сообщение → БД →
  пересчёт баланса → запрос баланса».

Тесты заодно задокументируют, что баги №1 и №2 реальны.

## Приоритет работ

1. ✅ Баг `@NotBlank` → `@NotNull` (п.1) — сделано.
2. ✅ Юнит-тесты сервиса + `@WebMvcTest` слайс контроллера (Kotlin/mockito-kotlin) — сделано.
   Testcontainers-интеграция — отложено.
3. ✅ Liquibase вместо `schema.sql` (п.15) + PK/индексы (п.6) — сделано.
4. `@Transactional` на пересчёт баланса (п.2).
5. DLQ + идемпотентность консьюмера (п.5), ShedLock на джобу (п.4).
6. `Types.DECIMAL` (п.3), разрядность `balance` (п.8).

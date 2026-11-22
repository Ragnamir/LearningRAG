# LearningRAG

RAG-сервис на Java с фокусом на кибербезопасность. Корпус состоит только из синтетических документов
вымышленной компании; в них могут встречаться правдоподобные, но недействительные секреты.

План этапа M0 и принятые решения: [docs/m0-plan.md](docs/m0-plan.md).

## Пайплайн

```text
POST /api/v1/chat
  -> AuthN
  -> InputGuard
  -> EmbeddingService
  -> Retrieval + tenant/ACL
  -> PromptBuilder
  -> EgressGuard
  -> DeepSeek
  -> OutputGuard
  -> ответ + аудит
```

## Требования

- JDK 21
- Maven 3.9+
- PostgreSQL с расширением pgvector

## Конфигурация

Все параметры читаются из переменных среды. Секретов в репозитории нет.

| Переменная | Назначение |
| --- | --- |
| `RAG_DB_URL` | JDBC URL PostgreSQL |
| `RAG_DB_USERNAME` | пользователь БД |
| `RAG_DB_PASSWORD` | пароль БД |
| `RAG_API_KEY` | локальный API-ключ AuthN |
| `RAG_TENANT_ID` | tenant по умолчанию |
| `DEEPSEEK_API_KEY` | ключ DeepSeek |
| `DEEPSEEK_BASE_URL` | базовый URL DeepSeek |
| `DEEPSEEK_MODEL` | модель, по умолчанию `deepseek-chat` |
| `DEEPSEEK_DAILY_TOKEN_BUDGET` | дневной лимит токенов |

Полный список со значениями по умолчанию — в `src/main/resources/application.properties`.

## Запуск

```bash
export RAG_DB_USERNAME=learningrag
export RAG_DB_PASSWORD=...
export DEEPSEEK_API_KEY=...
mvn spring-boot:run
```

Проверка:

```bash
curl -s http://localhost:8080/api/v1/health
```

## Сборка и тесты

```bash
mvn verify
```



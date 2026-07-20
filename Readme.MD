## Описание
Сервис для создания коротких ссылок с поддержкой TTL.
## Запуск:

    git clone https://github.com/winroot333/UrlShortener.git
    cd urlshortener
    git checkout dev

    ./gradlew bootRun

## API
1. Создание короткой ссылки
**POST** `/links`

```bash
curl -X POST http://localhost:8080/links \
  -H "Content-Type: application/json" \
  -d '{
    "URL": "https://example.com/very/long/url",
    "code": "custom123",
    "expirationDate": "2026-12-31T23:59:59"
  }'
```

**Ответ:**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalUrl": "https://example.com/very/long/url",
  "shortCode": "custom123",
  "expiresAt": "2026-12-31T23:59:59"
}
```
### 2. Получение списка ссылок
**GET** `/links?page=0&size=20`

```bash
curl "http://localhost:8080/links?page=0&size=20"
```

**Ответ:**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "originalUrl": "https://example.com/very/long/url",
    "shortCode": "custom123",
    "expiresAt": "2026-12-31T23:59:59"
  }
]
```

### 3. Редирект по короткой ссылке
**GET** `/{shortCode}`

```bash
curl -v http://localhost:8080/custom123
```

## Тесты

```bash
./gradlew test
```
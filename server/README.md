# lottery-server

Spring Boot backend (MySQL persistence).

## Run

1. Prepare the database and create the schema: `lottery`
2. Update database connection settings in `src/main/resources/application.yml` as needed
3. Enter the `server` directory
4. Run:

```
mvn spring-boot:run
```

Default port: `8080`

## APIs

- `GET /api/prizes`
- `POST /api/draw` { "name": "Zhang San" }
- `GET /api/records`

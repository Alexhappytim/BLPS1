# BLPS Backend

## Run with Docker Compose

Prerequisites:
- Docker
- Docker Compose plugin

Start services:

```bash
docker compose up --build -d
```

Stop services:

```bash
docker compose down
```

Stop and remove Postgres volume:

```bash
docker compose down -v
```

Backend URL: `http://localhost:8080`
Postgres URL: `jdbc:postgresql://localhost:5432/blps`

## Postman import

Files:
- `postman/blps-api.postman_collection.json`
- `postman/blps-local.postman_environment.json`

Import both files into Postman (or Insomnia/Bruno with JSON import support), select environment `BLPS Local`, and run requests in order:
1. `1. List Blocks`
2. `2. List Tasks`
3. `3. Resolve Coefficient`
4. `4. Submit Auto Task`
5. `5. Submit Mentor Task`
6. `6. Review Mentor Submission`
7. `7. User Points`
8. `8. Open Block`

Notes:
- Login is passed as a request field, according to current auth design.
- `mentorSubmissionId` is automatically saved from request `5` test script and reused in request `6`.
- If your DB was previously populated and IDs differ, update `autoTaskId`, `mentorTaskId`, and `blockToOpenId` in environment variables.

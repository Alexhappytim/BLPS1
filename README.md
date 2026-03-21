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
1. `1. List Courses (pick active course)`
2. `2. List Blocks (for active course)`
3. `3. List Tasks (for active course)`
4. `4. Resolve Coefficient`
5. `5. Submit Auto Task`
6. `6. Submit Mentor Task`
7. `7. Review Mentor Submission`
8. `8. User Points`
9. `9. Open Block`

Notes:
- Login is passed as a request field, according to current auth design.
- `courseId` is automatically selected from `GET /api/courses` using `courseCode` (default `FRONTEND`).
- `autoTaskId`, `mentorTaskId`, and `blockToOpenId` are extracted automatically from list requests.
- `mentorSubmissionId` is automatically saved from request `6` and reused in request `7`.

## Test data generation

Application seeds test data on startup in idempotent mode (no duplicates on restarts):
- courses: `FRONTEND`, `BACKEND`
- blocks: `FE-BLK-*`, `BE-BLK-*`
- tasks: `FE-T-*`, `BE-T-*`

Seed logic is implemented in `src/main/java/com/blps/app/domain/service/DemoDataInitializer.java` and uses `code` uniqueness to upsert demo entities.

## Certificate email (SMTP / Gmail)

When a user completes all tasks in a selected course (all tasks have `APPROVED` status), the app sends a certificate email.

Rules:
- completion is calculated per course, not globally;
- email is sent once per user-course pair;
- user login is used as recipient email.

### Gmail setup

1. Enable 2-Step Verification in your Google account.
2. Generate an App Password in Google Security settings.
3. Set environment variables for backend:

```bash
MAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_account@gmail.com
MAIL_PASSWORD=your_16_char_app_password
MAIL_FROM=your_account@gmail.com
```

If you run via Docker Compose, these variables are already wired in `compose.yaml` and can be provided via shell environment or `.env` file in project root.

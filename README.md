# MoneyManager — Backend

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate_JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Google OAuth2](https://img.shields.io/badge/Google_OAuth2-4285F4?style=for-the-badge&logo=google&logoColor=white)
![Brevo SMTP](https://img.shields.io/badge/Brevo_SMTP-0B996E?style=for-the-badge&logo=brevo&logoColor=white)
![Cloudinary](https://img.shields.io/badge/Cloudinary-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white)
![Apache POI](https://img.shields.io/badge/Apache_POI-D22128?style=for-the-badge&logo=apache&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

REST API for a personal finance tracker. Users register (or sign in with Google), organise their money into categories, record incomes and expenses, and get a dashboard summary, filtered transaction history, Excel exports, and daily email reminders.

Built with Spring Boot, PostgreSQL, and stateless JWT authentication.

**Frontend repository:** [MoneyManager Frontend](https://github.com/Soumadeep1221/MoneyManager-Frontend.git) (React + Tailwind CSS)

---

## Flows

Short video walkthroughs of the three flows that matter most.

### 1. Full Architecture
How the React frontend, Spring Boot API, PostgreSQL, Brevo SMTP, Google Identity, and Cloudinary fit together, and what travels along each connection.

https://github.com/user-attachments/assets/bdec4f1d-d329-400c-b12b-d4336d4ba024

### 2. Email/Password Login with JWT
Registration with an emailed activation link, then login: Spring Security's `AuthenticationManager` checks the BCrypt-hashed password, the backend issues a signed JWT, and `JwtRequestFilter` validates it on every later request.

https://github.com/user-attachments/assets/0056b983-0348-4932-b9af-0cd35e7426ca

### 3. Google Authentication
The frontend gets a Google ID token, the backend verifies it, finds or creates the profile, and issues the same JWT as a password login, so everything after sign-in works identically.

https://github.com/user-attachments/assets/d99a3097-7a39-43f4-b1a4-ccf7099caefb

---

## Features

- **Authentication**: email + password with an activation email, or Google Sign-In
- **Profile** with an updatable picture (hosted on Cloudinary, only the URL is stored here)
- **Categories** (income or expense type): create, list, filter by type, update, delete
- **Incomes and expenses**: create, list the current month, delete
- **Dashboard**: total income, total expense, latest five of each, merged recent-transactions feed
- **Transaction filter**: by type, date range, name keyword, sort field, and sort order
- **Excel export** of the current month, as a download or as an email attachment
- **Scheduled emails**: a daily reminder and a daily expense summary
- **Per-user data isolation**: every query is scoped to the authenticated user

---

## Project Structure

```
src/main/java/com/soumadeep/MoneyManager/
├── Config/        SecurityConfig
├── Controller/    ProfileController, CategoryController, ExpenseController,
│                  IncomeController, DashboardController, FilterController,
│                  ExcelController, EmailController
├── DTO/           ProfileDTO, AuthDTO, CategoryDTO, ExpenseDTO, IncomeDTO,
│                  RecentTransactionDTO, FilterDTO
├── Entity/        ProfileEntity, CategoryEntity, ExpenseEntity, IncomeEntity
├── Repository/    ProfileRepository, CategoryRepository,
│                  ExpenseRepository, IncomeRepository
├── Security/      JwtRequestFilter
├── Service/       ProfileService, AppUserDetailsService, EmailService,
│                  CategoryService, ExpenseService, IncomeService,
│                  DashboardService, ExcelService, NotificationService
└── Util/          JwtUtil, GoogleAuthUtil
```

---

## Design Notes

- **Layered architecture**: Controller → Service → Repository. Controllers handle HTTP only, services hold the logic, repositories use Spring Data derived queries plus a few JPQL aggregates.
- **Entity / DTO separation**: entities map to tables, DTOs define what crosses the API. `ProfileDTO.password` is write-only: accepted on registration, never populated in a response.
- **The current user always comes from the token.** Services call `getCurrentProfile()`, which reads the authenticated email from `SecurityContextHolder`. No endpoint accepts a user or profile ID from the client.
- **Ownership checks on every ID-based operation**, so changing an ID in a URL or body cannot reach another user's data.
- **Stateless security**: no server-side sessions, CSRF disabled (no cookie auth), and a custom `JwtRequestFilter` runs before Spring's username/password filter.

---

## Data Model

| Table | Key fields | Relations |
|---|---|---|
| `tbl_profiles` | `id`, `fullName`, `email` (unique), `password` (BCrypt hash, empty for Google-created accounts), `profileImageUrl`, `isActive`, `activationToken`, `createdAt`, `updatedAt` | none |
| `tbl_categories` | `id`, `name`, `type`, `icon`, `createdAt`, `updatedAt` | many-to-one → profile |
| `tbl_expenses` | `id`, `name`, `icon`, `date`, `amount` (`BigDecimal`), `createdAt`, `updatedAt` | many-to-one → category, many-to-one → profile |
| `tbl_incomes` | same shape as expenses | many-to-one → category, many-to-one → profile |

`isActive` defaults to `false`, `date` defaults to today when not supplied, timestamps are filled automatically, and relationships are `LAZY`.

---

## API Reference

Base path: `/api/v1.0`. Authenticated endpoints expect `Authorization: Bearer <token>` (HS256, 30-minute expiry).

### Public

| Method | Endpoint | Description |
|---|---|---|
| POST | `/register` | Create an account and send the activation email |
| GET | `/activate?token=` | Activate an account (`ACTIVATED`, `ALREADY_ACTIVE`, or `NOT_FOUND`) |
| POST | `/login` | Email + password login, returns `{ token, user }` |
| POST | `/auth/google` | Google login or registration, body `{ "token": "<google id token>" }` |

`/status` and `/health` are also whitelisted in `SecurityConfig`.

### Authenticated

| Method | Endpoint | Description |
|---|---|---|
| PUT | `/profile/image` | Update profile picture URL, body `{ "profileImageUrl": "..." }` |
| POST | `/categories` | Create a category (name unique per user) |
| GET | `/categories` | List the user's categories |
| GET | `/categories/{type}` | List the user's categories of one type |
| PUT | `/categories/{categoryId}` | Update a category |
| DELETE | `/categories/{categoryId}` | Delete a category |
| POST | `/incomes` | Add an income |
| GET | `/incomes` | Current month's incomes |
| DELETE | `/incomes/{incomeId}` | Delete an income |
| POST | `/expenses` | Add an expense |
| GET | `/expenses` | Current month's expenses |
| DELETE | `/expenses/{expenseId}` | Delete an expense |
| GET | `/dashboard` | `totalIncome`, `totalExpense`, `latestIncome`, `latestExpense`, `recentTransactions` |
| POST | `/filter` | Filter incomes or expenses (body below) |
| GET | `/excel/download/income` | Download current month's incomes as `.xlsx` |
| GET | `/excel/download/expense` | Download current month's expenses as `.xlsx` |
| GET | `/email/income-excel` | Email the incomes report to the logged-in user |
| GET | `/email/expense-excel` | Email the expenses report to the logged-in user |

**Filter request body** (all fields optional except `type`):

```json
{
  "type": "expense",
  "startDate": "2026-10-01",
  "endDate": "2026-10-31",
  "keyword": "rent",
  "sortField": "date",
  "sortOrder": "desc"
}
```

`type` is `income` or `expense`. `sortField` is an entity field name such as `date`, `amount`, or `name`. `sortOrder` is `asc` or `desc`. Sorting by name is alphabetical and, in Postgres's default collation, case-sensitive.

---

## Scheduled Jobs

Both run in IST and are enabled with `@EnableScheduling`.

| Time | Job |
|---|---|
| 22:00 | Reminder email to every registered user to log the day's income and expenses |
| 23:00 | Summary email of the day's expenses, sent only to users who logged at least one |

---

## Configuration

All secrets and environment-specific values are read from environment variables. `src/main/resources/application.properties` contains no credentials:

```properties
#PostgreSQL Configuration
spring.datasource.url=${DATASOURCE_URL}
spring.datasource.username=${DATASOURCE_USER}
spring.datasource.password=${DATASOURCE_PASSWORD}

server.servlet.context-path=/api/v1.0

#JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

#Email Configuration
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=587
spring.mail.username=${BREVO_USERNAME}
spring.mail.password=${BREVO_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.protocol=smtp
spring.mail.properties.mail.smtp.from=${BREVO_FROM_EMAIL}

#Secret Key
jwt.secret=${SECRET_KEY}

#Frontend URL
money.manager.frontend.url=${FRONTEND_URL}

#Google Client ID
google.client.id=${GOOGLE_CLIENT_ID}
```

| Environment variable | Purpose |
|---|---|
| `DATASOURCE_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/Money-Manager` |
| `DATASOURCE_USER`, `DATASOURCE_PASSWORD` | PostgreSQL credentials |
| `SECRET_KEY` | Base64-encoded JWT signing secret, at least 256 bits |
| `BREVO_USERNAME`, `BREVO_PASSWORD` | Brevo SMTP credentials |
| `BREVO_FROM_EMAIL` | Verified sender address |
| `FRONTEND_URL` | Frontend base URL, used in emailed activation links |
| `GOOGLE_CLIENT_ID` | OAuth Web client ID from Google Cloud Console |

A ready-to-copy template is in [`.env.example`](.env.example). Generate a JWT secret with `openssl rand -base64 32`.

---

## Getting Started

**Prerequisites:** JDK 25, Maven, PostgreSQL.

1. Create the database (the quotes are needed because of the hyphen):
   ```sql
   CREATE DATABASE "Money-Manager";
   ```
2. Copy the template and fill in your values (`.env` is git-ignored, never commit it):
   ```bash
   cp .env.example .env
   ```
3. Make the variables available to the app. Spring Boot does not read `.env` files by itself, so load them into your shell first, or paste them into your IDE's run-configuration environment variables.

   macOS / Linux:
   ```bash
   set -a; source .env; set +a
   ```
   Windows (PowerShell):
   ```powershell
   Get-Content .env | ForEach-Object { if ($_ -match '^\s*([^#=]+)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process') } }
   ```
4. In Google Cloud Console, create a Web OAuth client and add the frontend origin (for example `http://localhost:3000`) under Authorized JavaScript origins.
5. Run the backend:
   ```bash
   mvn spring-boot:run
   ```
   The API is served at `http://localhost:8080/api/v1.0`.
6. Set up and run the frontend from its [repository](YOUR_FRONTEND_REPO_LINK).

**Deployment notes**
- Set the same environment variables on your host, with `FRONTEND_URL` pointing at the deployed frontend so activation links are correct.
- Add the deployed frontend origin to the Google OAuth client's authorized origins.
- Set the server timezone to `Asia/Kolkata` (for example `TZ=Asia/Kolkata`). `LocalDate.now()` and the timestamp annotations use the JVM's default zone, which is often UTC on cloud hosts and shifts dates by a day for about 5.5 hours each night.

---

## Known Limitations

- Access tokens cannot be revoked or refreshed; a token stays valid until its 30-minute expiry.
- No rate limiting or lockout on `/login`.
- No bean validation on request DTOs and no global exception handler, so most service errors surface as generic server errors.
- `ddl-auto=update` is used instead of versioned migrations.
- Replacing a profile picture leaves the previous image in Cloudinary, because the backend stores only the URL.
- The email-report endpoints are `GET` requests that trigger a side effect, and they are not rate limited.
- CORS should be restricted to the real frontend origin instead of a wildcard before any real deployment.

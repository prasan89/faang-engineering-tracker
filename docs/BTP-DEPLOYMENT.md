# SAP BTP Trial Deployment

This project is prepared for SAP BTP Cloud Foundry deployment as two applications:

- `faang-engineering-tracker-api` — Spring Boot REST API
- `faang-engineering-tracker-web` — Next.js application

SAP BTP Trial provides a Cloud Foundry org and `dev` space for deploying applications. The trial is time-limited and is intended for non-commercial testing.

## 1. Prepare the BTP trial

In SAP BTP Cockpit:

1. Open your **trial** subaccount.
2. Open **Cloud Foundry Environment**.
3. Use the `dev` space.
4. Copy the Cloud Foundry API endpoint from the subaccount overview.

## 2. Login with Cloud Foundry CLI

Install the Cloud Foundry CLI, then:

```bash
cf login -a <YOUR_CF_API_ENDPOINT>
```

Select the trial org and `dev` space when prompted.

## 3. Deploy the Spring Boot API

From the repository root:

```bash
cd backend
mvn clean package -DskipTests
cf push -f manifest.yml
```

The manifest expects:

```text
target/tracker-api-0.1.0.jar
```

After deployment:

```bash
cf apps
```

Copy the route assigned to `faang-engineering-tracker-api`.

## 4. Deploy the Next.js frontend

Return to the repository root and edit `manifest-frontend.yml`.

Replace:

```text
https://REPLACE_WITH_BACKEND_ROUTE
```

with the API route from step 3, for example:

```text
https://faang-engineering-tracker-api-<random>.<region>.cfapps.<domain>
```

Then deploy:

```bash
npm install
npm run build
cf push -f manifest-frontend.yml
```

Run:

```bash
cf apps
```

Open the route assigned to `faang-engineering-tracker-web`.

## 5. Verify

Check the following pages:

- `/` — career dashboard
- `/dsa` — DSA Command Center

The browser only talks to the Next.js application. Next.js server routes proxy `/api/tasks` and `/api/dsa` to the Spring Boot API using `BACKEND_URL`.

## Important Phase 1 limitation

The Spring Boot API currently uses file-backed H2. Cloud Foundry application files are not the right place for durable production data. This is acceptable for the first trial deployment, but **do not treat the deployed H2 file as durable storage**.

### Phase 2

Move persistence to SAP HANA Cloud and bind the database service to the Spring Boot application. Then the tracker becomes a proper cloud-persistent SAP BTP application.

## Useful commands

```bash
cf apps
cf logs faang-engineering-tracker-api --recent
cf logs faang-engineering-tracker-web --recent
cf env faang-engineering-tracker-web
cf env faang-engineering-tracker-api
```

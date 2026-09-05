# H2 Database

A lightweight H2 database is included for local persistence while the tracker is being developed.

## Tables

- `task_completion` — stores task completion state.
- `study_session` — tracks study time by track and date.
- `application` — tracks FAANG job applications.
- `recognition_activity` — tracks GitHub, writing, open-source, networking, and referral activity.

## Local setup

The schema is in `db/schema.sql` and can be loaded by H2 in embedded or file mode.

Example JDBC URL:

`jdbc:h2:file:./data/faang-tracker`

This is intentionally simple. We can add Spring Boot/JDBC integration, migrations, and a proper persistence API next without changing the core tracker model.

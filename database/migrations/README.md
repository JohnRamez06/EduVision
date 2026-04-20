# EduVision MySQL/XAMPP setup

## 1) Start MySQL in XAMPP
- Open XAMPP Control Panel.
- Start **MySQL**.
- Default local values are typically:
  - Host: `localhost`
  - Port: `3306`
  - User: `root`
  - Password: *(empty)*

## 2) Import schema and seed data
Run scripts in this exact order:

1. `/home/runner/work/EduVision/EduVision/database/schema/01_create_tables.sql`
2. `/home/runner/work/EduVision/EduVision/database/schema/02_seed_data.sql`
3. `/home/runner/work/EduVision/EduVision/database/schema/03_stored_procedures.sql`

You can execute via phpMyAdmin SQL tab, or CLI:

```bash
mysql -u root -h localhost -P 3306 < /home/runner/work/EduVision/EduVision/database/schema/01_create_tables.sql
mysql -u root -h localhost -P 3306 < /home/runner/work/EduVision/EduVision/database/schema/02_seed_data.sql
mysql -u root -h localhost -P 3306 < /home/runner/work/EduVision/EduVision/database/schema/03_stored_procedures.sql
```

## 3) Backend datasource configuration
Backend now supports env-var based configuration with XAMPP-friendly defaults:

- `DB_URL` (default: `jdbc:mysql://localhost:3306/eduvision?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`)
- `DB_USERNAME` (default: `root`)
- `DB_PASSWORD` (default: empty)

Then run backend from `/home/runner/work/EduVision/EduVision/backend`.

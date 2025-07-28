# my-calendar-backend

Create roles

```sql
INSERT INTO roles (name, created_date, created_by, modified_date, modified_by, is_active)
VALUES ('ADMIN', now(), 'anonymous', now(), 'anonymous', true),
       ('USER', now(), 'anonymous', now(), 'anonymous', true);
```

Create admin

```sql
INSERT INTO users (name, username, email, password, is_active)
VALUES ('ADMIN', 'ADMIN', 'admin@gmail.com', '12345', true);

INSERT INTO user_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'ADMIN'),
        1);
```
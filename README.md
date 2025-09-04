# my-calendar-backend

Create roles

```sql
INSERT INTO roles (name, created_date, created_by, modified_date, modified_by, is_active)
VALUES ('ADMIN', now(), 'anonymous', now(), 'anonymous', true),
       ('USER', now(), 'anonymous', now(), 'anonymous', true);
```

Create Role in Group

```sql
INSERT INTO role_groups (name)
VALUES ('ADMIN'),
       ('MEMBER');
```
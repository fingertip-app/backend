# Auth and Access Control

## Authentication

- Supabase access token is sent as `Authorization: Bearer {access_token}`.
- The backend verifies the JWT with `SUPABASE_JWT_SECRET`.
- On first valid request, the backend creates a local `users` row with:
  - `provider = supabase`
  - `provider_id = JWT sub`
  - `email = JWT email`
  - `role = USER`

## Error Split

- Missing authentication: `401 UNAUTHORIZED`
- Invalid or expired JWT: `401 TOKEN_INVALID`
- Authenticated but insufficient role: `403 FORBIDDEN`

## Roles

- `USER`: normal customer.
- `ARTISAN`: approved artisan. Assigned when admin approves an artisan application.
- `ADMIN`: administrator. Seed or update this role directly in DB for now.

## MVP API Matrix

| API | USER | ARTISAN | ADMIN | Notes |
| --- | --- | --- | --- | --- |
| `POST /auth/login` | O | O | O | Confirms Supabase JWT and returns local profile |
| `GET /users/me` | O | O | O | Profile |
| `PATCH /users/me` | O | O | O | Profile update |
| `POST /artisans/apply` | O | O | O | Creates `certification_status = PENDING` |
| `GET /artisans/me` | O | O | O | Own application/status |
| `GET /experiences` | O | O | O | Authenticated list |
| `GET /experiences/{id}` | O | O | O | Detail includes schedule capacity |
| `POST /admin/artisans/{id}/approve` | X | X | O | Sets artisan approved and user role artisan |
| `POST /admin/artisans/{id}/reject` | X | X | O | Sets artisan rejected |

## Schedule Capacity Rule

Capacity is held per `experience_schedules.id`, not per experience.

```
remaining_slots = experience_schedules.available_slots
    - sum(reservations.number_of_participants)
```

Only these reservation statuses hold capacity:

- `PENDING`
- `APPROVED`
- `PAID`
- `CONFIRMED`

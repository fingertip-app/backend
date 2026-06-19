ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS reserved_date_time TIMESTAMP,
    ADD COLUMN IF NOT EXISTS payment_key VARCHAR(255),
    ADD COLUMN IF NOT EXISTS payment_order_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(255),
    ADD COLUMN IF NOT EXISTS cancellation_reason VARCHAR(255),
    ADD COLUMN IF NOT EXISTS is_notification_sent BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE reservations
SET reserved_date_time = s.scheduled_at
FROM experience_schedules s
WHERE reservations.schedule_id = s.id
  AND reservations.reserved_date_time IS NULL;

UPDATE reservations
SET status = UPPER(status)
WHERE status <> UPPER(status);

CREATE UNIQUE INDEX IF NOT EXISTS ux_reservations_payment_order_id
    ON reservations(payment_order_id)
    WHERE payment_order_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_reservations_schedule_status
    ON reservations(schedule_id, status);

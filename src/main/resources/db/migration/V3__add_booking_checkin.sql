ALTER TABLE booking_schema.bookings
    ADD COLUMN IF NOT EXISTS checked_in_at TIMESTAMP NULL;

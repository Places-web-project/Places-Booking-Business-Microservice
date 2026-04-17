CREATE TABLE IF NOT EXISTS booking_schema.loyalty_reward (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    points_cost INT NOT NULL CHECK (points_cost > 0),
    category VARCHAR(40) NOT NULL,
    stock_remaining INT NOT NULL CHECK (stock_remaining >= 0),
    icon_key VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS booking_schema.loyalty_transaction (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount INT NOT NULL,
    transaction_type VARCHAR(40) NOT NULL,
    booking_id BIGINT REFERENCES booking_schema.bookings (id) ON DELETE SET NULL,
    reward_id BIGINT REFERENCES booking_schema.loyalty_reward (id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_loyalty_transaction_desk_booking
    ON booking_schema.loyalty_transaction (booking_id)
    WHERE transaction_type = 'DESK_CHECKIN';

INSERT INTO booking_schema.loyalty_reward (name, description, points_cost, category, stock_remaining, icon_key, active)
VALUES
    ('Premium Goodies Bag', 'Company branded hoodie, water bottle, and notebook set', 500, 'goodies', 15, 'CardGiftcard', TRUE),
    ('Starter Goodies Pack', 'T-shirt, pen set, and stickers', 200, 'goodies', 30, 'CardGiftcard', TRUE),
    ('Tech Workshop', 'Full-day workshop on latest technologies', 800, 'training', 5, 'School', TRUE),
    ('Online Course Credit', '$100 credit for Udemy, Coursera, or similar platforms', 600, 'training', 20, 'School', TRUE),
    ('Gym Membership (1 Month)', 'Access to World Class gym facilities', 400, 'wellness', 10, 'FitnessCenter', TRUE),
    ('Swimming Pool Pass', '10 entries to premium swimming pool', 300, 'wellness', 12, 'Pool', TRUE),
    ('Go-Karting Experience', '2-hour karting session for up to 4 people', 700, 'wellness', 8, 'DirectionsCar', TRUE),
    ('Coffee Shop Voucher', '$50 voucher for Starbucks or local coffee shops', 250, 'food', 25, 'LocalCafe', TRUE),
    ('Restaurant Voucher', '$100 voucher for selected restaurants', 500, 'food', 15, 'Restaurant', TRUE);

INSERT INTO booking_schema.loyalty_transaction (user_id, amount, transaction_type, booking_id, reward_id, created_at)
SELECT b.user_id,
       10,
       'DESK_CHECKIN',
       b.id,
       NULL,
       COALESCE(b.checked_in_at, b.ends_at)
FROM booking_schema.bookings b
         INNER JOIN booking_schema.rooms r ON r.id = b.room_id
WHERE b.status = 'APPROVED'
  AND b.checked_in_at IS NOT NULL
  AND UPPER(TRIM(COALESCE(r.room_type, ''))) = 'DESK'
  AND NOT EXISTS (SELECT 1
                  FROM booking_schema.loyalty_transaction t
                  WHERE t.booking_id = b.id
                    AND t.transaction_type = 'DESK_CHECKIN');

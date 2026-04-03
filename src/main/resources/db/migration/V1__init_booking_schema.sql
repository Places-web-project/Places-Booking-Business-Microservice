CREATE SCHEMA IF NOT EXISTS booking_schema;

CREATE TABLE IF NOT EXISTS booking_schema.rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    capacity INT NOT NULL,
    room_type VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS booking_schema.room_metadata (
    id BIGSERIAL PRIMARY KEY,
    floor_name VARCHAR(120) NOT NULL,
    has_projector BOOLEAN NOT NULL,
    room_id BIGINT NOT NULL UNIQUE REFERENCES booking_schema.rooms(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking_schema.desks (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    desk_type VARCHAR(50) NOT NULL,
    room_id BIGINT NOT NULL REFERENCES booking_schema.rooms(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking_schema.teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS booking_schema.team_members (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    team_id BIGINT NOT NULL REFERENCES booking_schema.teams(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking_schema.bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    room_id BIGINT NOT NULL REFERENCES booking_schema.rooms(id) ON DELETE CASCADE,
    team_id BIGINT REFERENCES booking_schema.teams(id) ON DELETE SET NULL
);

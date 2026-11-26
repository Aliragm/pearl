-- Migration V2: Add external catalog metadata columns to games
ALTER TABLE games
    ADD COLUMN external_id INTEGER UNIQUE,
    ADD COLUMN cover TEXT NOT NULL DEFAULT '',
    ADD COLUMN summary TEXT NOT NULL DEFAULT '';

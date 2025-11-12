-- Create card_sets table
CREATE TABLE card_sets (
    id BIGSERIAL PRIMARY KEY,
    set_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    game_type VARCHAR(50) NOT NULL,
    release_date DATE,
    total_cards INTEGER,
    logo_url VARCHAR(500)
);

-- Create cards table
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    card_set_id BIGINT NOT NULL,
    card_number VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    rarity VARCHAR(50),
    variant VARCHAR(100),
    image_url VARCHAR(500),
    description TEXT,
    type VARCHAR(100),
    artist VARCHAR(255),
    CONSTRAINT fk_card_set FOREIGN KEY (card_set_id) REFERENCES card_sets(id) ON DELETE CASCADE,
    CONSTRAINT uk_set_card_number UNIQUE (card_set_id, card_number)
);

-- Create indexes for common queries
CREATE INDEX idx_card_sets_game_type ON card_sets(game_type);
CREATE INDEX idx_card_sets_name ON card_sets(name);
CREATE INDEX idx_cards_card_set ON cards(card_set_id);
CREATE INDEX idx_cards_name ON cards(name);
CREATE INDEX idx_cards_rarity ON cards(rarity);

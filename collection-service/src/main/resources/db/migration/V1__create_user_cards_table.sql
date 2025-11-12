-- Create user_cards table for storing user card collections
CREATE TABLE user_cards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    condition VARCHAR(20) NOT NULL DEFAULT 'NEAR_MINT',
    is_reverse_holo BOOLEAN DEFAULT FALSE,
    notes TEXT,
    acquired_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_card UNIQUE (user_id, card_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_user_cards_user_id ON user_cards(user_id);
CREATE INDEX idx_user_cards_card_id ON user_cards(card_id);
CREATE INDEX idx_user_cards_condition ON user_cards(condition);
CREATE INDEX idx_user_cards_reverse_holo ON user_cards(is_reverse_holo);

-- Add comments
COMMENT ON TABLE user_cards IS 'Stores user card collections';
COMMENT ON COLUMN user_cards.user_id IS 'Reference to user owning the card';
COMMENT ON COLUMN user_cards.card_id IS 'Reference to card in catalog service';
COMMENT ON COLUMN user_cards.quantity IS 'Number of copies owned';
COMMENT ON COLUMN user_cards.condition IS 'Card condition: MINT, NEAR_MINT, EXCELLENT, GOOD, LIGHT_PLAYED, PLAYED, POOR';
COMMENT ON COLUMN user_cards.is_reverse_holo IS 'Whether this is a reverse holo variant';
COMMENT ON COLUMN user_cards.notes IS 'User notes about this card';
COMMENT ON COLUMN user_cards.acquired_date IS 'When the card was acquired';

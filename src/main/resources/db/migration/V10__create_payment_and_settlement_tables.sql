-- Payment 테이블 생성
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_key VARCHAR(200),
    order_id VARCHAR(200),
    paid_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    refunded_at TIMESTAMP,
    cancel_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_reservation_id ON payments(reservation_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_payment_key ON payments(payment_key);

-- Settlement 테이블 생성
CREATE TABLE settlements (
    id BIGSERIAL PRIMARY KEY,
    artisan_id BIGINT NOT NULL,
    settlement_start_date DATE NOT NULL,
    settlement_end_date DATE NOT NULL,
    total_revenue DECIMAL(10, 2) NOT NULL,
    platform_fee_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.10,
    platform_fee DECIMAL(10, 2) NOT NULL,
    settlement_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    completed_at TIMESTAMP,
    bank_account TEXT,
    transfer_reference VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_settlement_artisan FOREIGN KEY (artisan_id) REFERENCES artisans(id) ON DELETE CASCADE
);

CREATE INDEX idx_settlement_artisan_id ON settlements(artisan_id);
CREATE INDEX idx_settlement_status ON settlements(status);
CREATE INDEX idx_settlement_dates ON settlements(settlement_start_date, settlement_end_date);

-- SettlementItem 테이블 생성
CREATE TABLE settlement_items (
    id BIGSERIAL PRIMARY KEY,
    settlement_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    fee_amount DECIMAL(10, 2) NOT NULL,
    settlement_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_settlement_item_settlement FOREIGN KEY (settlement_id) REFERENCES settlements(id) ON DELETE CASCADE,
    CONSTRAINT fk_settlement_item_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);

CREATE INDEX idx_settlement_item_settlement_id ON settlement_items(settlement_id);
CREATE INDEX idx_settlement_item_reservation_id ON settlement_items(reservation_id);

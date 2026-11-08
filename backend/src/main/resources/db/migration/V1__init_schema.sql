-- ChitCircle V1 Schema Migration
-- Matches C4 Architecture Section 5

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS chit_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    chit_value NUMERIC(15,2) NOT NULL,
    monthly_contribution NUMERIC(15,2) NOT NULL,
    total_members INTEGER NOT NULL,
    duration_months INTEGER NOT NULL,
    start_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS chit_members (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES chit_groups(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    member_number INTEGER NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    prize_received BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_group_user UNIQUE (group_id, user_id),
    CONSTRAINT uq_group_member_number UNIQUE (group_id, member_number)
);

CREATE TABLE IF NOT EXISTS auctions (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES chit_groups(id) ON DELETE CASCADE,
    month_number INTEGER NOT NULL,
    auction_date TIMESTAMP NOT NULL,
    total_pool NUMERIC(15,2) NOT NULL,
    winning_discount NUMERIC(15,2),
    prize_amount NUMERIC(15,2),
    dividend_per_member NUMERIC(15,2),
    winner_member_id UUID REFERENCES chit_members(id),
    status VARCHAR(50) NOT NULL,
    CONSTRAINT uq_group_month UNIQUE (group_id, month_number)
);

CREATE TABLE IF NOT EXISTS bids (
    id UUID PRIMARY KEY,
    auction_id UUID NOT NULL REFERENCES auctions(id) ON DELETE CASCADE,
    member_id UUID NOT NULL REFERENCES chit_members(id) ON DELETE CASCADE,
    bid_amount NUMERIC(15,2) NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES chit_groups(id) ON DELETE CASCADE,
    member_id UUID NOT NULL REFERENCES chit_members(id) ON DELETE CASCADE,
    auction_month INTEGER NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    reference_number VARCHAR(100) UNIQUE,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS ledger_transactions (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES chit_groups(id),
    member_id UUID REFERENCES chit_members(id),
    auction_id UUID REFERENCES auctions(id),
    transaction_type VARCHAR(50) NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    reference_id UUID,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS prize_payouts (
    id UUID PRIMARY KEY,
    auction_id UUID NOT NULL REFERENCES auctions(id) UNIQUE,
    winner_member_id UUID NOT NULL REFERENCES chit_members(id),
    payout_amount NUMERIC(15,2) NOT NULL,
    payout_status VARCHAR(50) NOT NULL,
    paid_at TIMESTAMP,
    payment_reference VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS dividend_distributions (
    id UUID PRIMARY KEY,
    auction_id UUID NOT NULL REFERENCES auctions(id) ON DELETE CASCADE,
    member_id UUID NOT NULL REFERENCES chit_members(id) ON DELETE CASCADE,
    dividend_amount NUMERIC(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT uq_auction_member UNIQUE (auction_id, member_id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_members_group ON chit_members(group_id);
CREATE INDEX IF NOT EXISTS idx_auctions_group ON auctions(group_id);
CREATE INDEX IF NOT EXISTS idx_bids_auction ON bids(auction_id);
CREATE INDEX IF NOT EXISTS idx_ledger_group ON ledger_transactions(group_id);
CREATE INDEX IF NOT EXISTS idx_ledger_member ON ledger_transactions(member_id);
CREATE INDEX IF NOT EXISTS idx_payments_group_month ON payments(group_id, auction_month);


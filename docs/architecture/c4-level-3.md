# C4 Level 3 — Backend Component Architecture

The Component diagram breaks down the Spring Boot Modular Monolith into discrete business domain modules.

```mermaid
flowchart TB
    CLIENT["React Client (Vite)"]

    subgraph API["Security & API Layer"]
        FILTER["JwtAuthenticationFilter"]
        CONTROLLER["REST Controllers"]
        DTO["Request / Response DTOs"]
    end

    subgraph APPLICATION["Application Services"]
        AUTH["AuthService"]
        GROUP["GroupApplicationService"]
        MEMBER["MemberApplicationService"]
        AUCTION["AuctionApplicationService"]
        BID["BidApplicationService"]
        PAYMENT["PaymentApplicationService"]
        LEDGER["LedgerApplicationService"]
        DIVIDEND["DividendCalculatorService"]
        SETTLEMENT["AuctionSettlementService"]
        REPORT["ReportApplicationService"]
    end

    subgraph DOMAIN["Domain Entities & Business Invariants"]
        E_GROUP["ChitGroup"]
        E_MEMBER["ChitMember"]
        E_AUCTION["Auction"]
        E_BID["Bid"]
        E_PAYMENT["Payment"]
        E_LEDGER["LedgerTransaction (Append-Only)"]
    end

    subgraph INFRA["Data Repositories"]
        REPO_GROUP["ChitGroupRepository"]
        REPO_MEMBER["ChitMemberRepository"]
        REPO_AUCTION["AuctionRepository (Pessimistic Lock)"]
        REPO_BID["BidRepository"]
        REPO_PAYMENT["PaymentRepository"]
        REPO_LEDGER["LedgerTransactionRepository"]
        DB[("PostgreSQL 16")]
    end

    CLIENT --> FILTER
    FILTER --> CONTROLLER
    CONTROLLER --> DTO
    DTO --> APPLICATION

    AUCTION --> BID
    AUCTION --> DIVIDEND
    AUCTION --> SETTLEMENT
    SETTLEMENT --> LEDGER
    PAYMENT --> LEDGER

    APPLICATION --> DOMAIN
    APPLICATION --> INFRA
    INFRA --> DB
```

## Module Responsibilities

1. **Identity & Security**: User authentication, JWT issuance, BCrypt password hashing, and role verification (`ROLE_SUPER_ADMIN`, `ROLE_GROUP_ADMIN`, `ROLE_GROUP_MEMBER`).
2. **Chit Group Management**: Group lifecycle (`DRAFT`, `ACTIVE`, `COMPLETED`), pool configuration, and 20-member constraints.
3. **Auction Management & Bidding**: Enforces minimum (5%) and statutory maximum (40%) discount rates, prevents duplicate bids, enforces the rule that previous winners cannot bid, and breaks ties via earliest timestamp.
4. **Dividend Calculator**: Strict `BigDecimal` arithmetic dividing winning discounts equally across members and calculating winner net payout.
5. **Financial Ledger**: Invariant append-only audit trail logging every credit, debit, dividend distribution, and prize payout.


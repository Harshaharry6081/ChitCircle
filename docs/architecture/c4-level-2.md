# C4 Level 2 — Container Architecture

The Container diagram depicts the high-level technical architecture of ChitCircle, consisting of the React SPA client, Spring Boot Modular Monolith, PostgreSQL persistence layer, and cloud hosting infrastructure.

```mermaid
flowchart TB
    subgraph Users
        Admin["Administrator"]
        Member["Member"]
    end

    subgraph GCP["Google Cloud Platform / Local Docker"]
        LB["HTTPS Reverse Proxy / Load Balancer"]

        FE["React + TypeScript
        Web Application (Port 5173)"]

        BE["Spring Boot Modular Monolith
        Java 21 (Port 8080)"]

        DB[("PostgreSQL
        Database (Port 5432)")]

        STORAGE["Object Storage
        Reports and Statements"]

        SCHEDULER["Background Scheduler
        Spring Scheduling Engine"]
    end

    Admin --> LB
    Member --> LB

    LB --> FE
    FE -->|"REST APIs / HTTPS / Bearer JWT"| BE

    BE --> DB
    BE --> STORAGE
    BE --> SCHEDULER
    SCHEDULER --> DB
```

## Container Responsibilities

| Container | Technology | Responsibilities |
|---|---|---|
| Frontend | React 18/19, TypeScript, TailwindCSS, Vite | Single-page UI, responsive dashboard, real-time bidding console, member dues tracker, and ledger visualizer. |
| Backend | Java 21, Spring Boot 3.4, Spring Security, Hibernate | Modular monolith enforcing business logic, auction state machines, atomic settlement transactions, and immutable ledger logging. |
| Database | PostgreSQL 16 / H2 (dev/test) | Relational persistence, unique constraints, foreign keys, and indexes for audit logs, payments, and auctions. |
| Background Worker | Spring `@EnableScheduling` | Automatic status checks, payment reminder dispatch, and statement export compilation. |


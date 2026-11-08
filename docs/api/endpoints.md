# ChitCircle — Complete REST API Reference

Base URL: `/api/v1`

## 1. Authentication APIs
- `POST /api/v1/auth/register` — Register a new user account with role selection.
- `POST /api/v1/auth/login` — Authenticate and receive a Bearer JWT token.
- `GET /api/v1/auth/me` — Retrieve the currently authenticated user's profile and roles.

## 2. Group APIs
- `POST /api/v1/groups` — Create a new chit group with tenure, contribution, and member capacity.
- `GET /api/v1/groups` — List chit groups accessible to the authenticated user.
- `GET /api/v1/groups/{groupId}` — Get detailed specifications for a chit group.
- `POST /api/v1/groups/{groupId}/activate` — Transition group from `DRAFT` to `ACTIVE`.
- `POST /api/v1/groups/{groupId}/close` — Mark group as `COMPLETED`.

## 3. Member APIs
- `POST /api/v1/groups/{groupId}/members` — Enroll a member into a chit group.
- `GET /api/v1/groups/{groupId}/members` — List all 20 enrolled members with status and prize eligibility.
- `GET /api/v1/groups/{groupId}/members/{memberId}` — Get single member profile.
- `PATCH /api/v1/groups/{groupId}/members/{memberId}/status` — Update member status (`ACTIVE`, `SUSPENDED`, `EXITED`).

## 4. Auction & Bidding APIs
- `POST /api/v1/groups/{groupId}/auctions` — Schedule an auction for a specified month.
- `GET /api/v1/groups/{groupId}/auctions` — List all monthly auctions for a group.
- `GET /api/v1/auctions/{auctionId}` — Get auction details.
- `POST /api/v1/auctions/{auctionId}/open` — Open the live bidding window (`OPEN`).
- `POST /api/v1/auctions/{auctionId}/bids` — Submit a discount bid (`PlaceBidRequest`).
- `GET /api/v1/auctions/{auctionId}/bids` — Retrieve ranked bid leaderboard.
- `POST /api/v1/auctions/{auctionId}/close` — Close the bidding window (`CLOSED`).
- `POST /api/v1/auctions/{auctionId}/declare-winner` — Rank bids, declare winner, compute dividends.
- `GET /api/v1/auctions/{auctionId}/result` — Get comprehensive auction and dividend breakdown.
- `POST /api/v1/auctions/{auctionId}/settle` — Execute atomic financial settlement and ledger creation.

## 5. Payment & Dues APIs
- `POST /api/v1/payments` — Record a member monthly contribution payment.
- `GET /api/v1/payments` — List payments (filterable by `groupId` and `month`).
- `POST /api/v1/payments/{paymentId}/confirm` — Reconcile and confirm payment into ledger.
- `POST /api/v1/payments/{paymentId}/reverse` — Reverse a payment and log compensating ledger entry.
- `GET /api/v1/groups/{groupId}/dues?month={month}` — View net member dues (Contribution - Dividend offset).

## 6. Financial Ledger APIs
- `GET /api/v1/groups/{groupId}/ledger` — View complete group immutable double-entry ledger.
- `GET /api/v1/members/{memberId}/ledger` — View member personal ledger and net position.

## 7. Reporting APIs
- `GET /api/v1/reports/group-summary?groupId={groupId}` — Summary balance sheet for the chit circle.
- `GET /api/v1/reports/member-statement?memberId={memberId}` — Financial statement for a member.


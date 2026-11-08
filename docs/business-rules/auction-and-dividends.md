# ChitCircle — Auction Rules & Mathematical Formulation

## 1. Mathematical Formulation

Let:
- $P$ = Monthly chit pool ($P = \text{monthly contribution} \times N$)
- $D$ = Winning discount offered by highest bidder
- $C$ = Organizer commission or statutory deductions ($C = 0$ for MVP)
- $N$ = Total group members ($N = 20$)

### Equations:

$$\text{Dividend Pool} = D - C$$

$$\text{Dividend Per Member} = \frac{D - C}{N}$$

$$\text{Winner Prize Payout} = P - D$$

### Example Walkthrough (Section 1.2):
- $P = ₹2,00,000$ (20 members $\times$ ₹10,000)
- Winning bid discount $D = ₹40,000$
- Commission $C = ₹0$
- $N = 20$

**Calculations:**
$$\text{Dividend Pool} = ₹40,000 - ₹0 = ₹40,000$$
$$\text{Dividend Per Member} = \frac{₹40,000}{20} = ₹2,000$$
$$\text{Winner Prize Amount} = ₹2,00,000 - ₹40,000 = ₹1,60,000$$

Every active member's next monthly contribution is reduced by ₹2,000, so net payable is ₹8,000.

---

## 2. Business Invariants & Rules

1. **Eligibility**: Only enrolled, active group members can bid.
2. **Prior Winner Disqualification**: Once a member has won the prize (`prizeReceived == true`), they cannot win or submit bids in subsequent auctions of the cycle.
3. **Discount Bounds**:
   - Minimum Discount: 5% of monthly pool ($₹10,000$)
   - Maximum Discount: Statutory cap of 40% of monthly pool ($₹80,000$)
4. **Duplicate Bids**: A member cannot place duplicate active bids for the same auction.
5. **State Progression**:
   - Bidding is accepted ONLY when auction status is `OPEN`.
   - Bidding is closed by transitioning to `CLOSED`.
   - Winner is declared by ranking bids: Highest discount wins.
   - Tie-breaker: If two bids have identical highest discounts, the earliest submitted bid (`submitted_at` ASC) wins.
6. **Settlement Atomicity**:
   - Settle executes under `@Transactional` with a pessimistic row lock (`PESSIMISTIC_WRITE`).
   - Automatically generates Prize Payout record.
   - Automatically distributes Dividend records to all 20 active members.
   - Appends CREDIT and DEBIT entries to the immutable ledger.
   - Sets winner `prizeReceived = true`.
   - Transitions auction to `SETTLED`.


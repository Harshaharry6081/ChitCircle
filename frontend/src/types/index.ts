export type RoleName = 'SUPER_ADMIN' | 'GROUP_ADMIN' | 'GROUP_MEMBER';

export interface User {
  id: string;
  fullName: string;
  email: string;
  phoneNumber?: string;
  status: string;
  roles: string[];
  createdAt: string;
}

export interface ChitGroup {
  id: string;
  name: string;
  chitValue: number;
  monthlyContribution: number;
  totalMembers: number;
  durationMonths: number;
  startDate: string;
  status: 'DRAFT' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
  createdById?: string;
  createdByName?: string;
  currentMemberCount: number;
  createdAt: string;
}

export interface ChitMember {
  id: string;
  groupId: string;
  groupName: string;
  userId: string;
  fullName: string;
  email: string;
  phoneNumber?: string;
  memberNumber: number;
  status: 'ACTIVE' | 'SUSPENDED' | 'EXITED';
  prizeReceived: boolean;
  joinedAt: string;
}

export type AuctionStatus =
  | 'CREATED'
  | 'OPEN'
  | 'CLOSED'
  | 'WINNER_DECLARED'
  | 'SETTLEMENT_PENDING'
  | 'SETTLED'
  | 'CANCELLED';

export interface Auction {
  id: string;
  groupId: string;
  groupName: string;
  monthNumber: number;
  auctionDate: string;
  totalPool: number;
  winningDiscount?: number;
  prizeAmount?: number;
  dividendPerMember?: number;
  winnerMemberId?: string;
  winnerMemberName?: string;
  winnerMemberNumber?: number;
  status: AuctionStatus;
}

export interface Bid {
  id: string;
  auctionId: string;
  memberId: string;
  memberName: string;
  memberNumber: number;
  bidAmount: number;
  status: 'SUBMITTED' | 'VALID' | 'WINNING' | 'OUTBID' | 'REJECTED';
  submittedAt: string;
  isHighest?: boolean;
}

export interface AuctionResult {
  auctionId: string;
  groupId: string;
  groupName: string;
  monthNumber: number;
  auctionDate: string;
  totalPool: number;
  winningDiscount: number;
  prizeAmount: number;
  dividendPool: number;
  dividendPerMember: number;
  winnerMemberId: string;
  winnerMemberName: string;
  winnerMemberNumber: number;
  status: AuctionStatus;
  bids: Bid[];
}

export interface Payment {
  id: string;
  groupId: string;
  groupName: string;
  memberId: string;
  memberName: string;
  memberNumber: number;
  auctionMonth: number;
  amount: number;
  paymentDate: string;
  paymentMethod: string;
  referenceNumber: string;
  status: 'PENDING' | 'CONFIRMED' | 'REVERSED';
}

export interface DueSummary {
  memberId: string;
  memberName: string;
  memberNumber: number;
  auctionMonth: number;
  monthlyContribution: number;
  dividendCredit: number;
  netPayable: number;
  amountPaid: number;
  balanceDue: number;
  isPaid: boolean;
}

export interface LedgerTransaction {
  id: string;
  groupId: string;
  groupName: string;
  memberId?: string;
  memberName?: string;
  memberNumber?: number;
  auctionId?: string;
  transactionType: 'CONTRIBUTION' | 'PRIZE_PAYOUT' | 'DIVIDEND' | 'COMMISSION' | 'REVERSAL';
  amount: number;
  direction: 'CREDIT' | 'DEBIT';
  referenceId?: string;
  createdAt: string;
}

export interface LedgerBalance {
  groupId: string;
  memberId?: string;
  totalCredits: number;
  totalDebits: number;
  netBalance: number;
  transactions: LedgerTransaction[];
}

export interface GroupSummaryReport {
  groupId: string;
  groupName: string;
  status: string;
  chitValue: number;
  monthlyContribution: number;
  totalMembers: number;
  enrolledMembers: number;
  totalAuctionsConducted: number;
  totalAuctionsRemaining: number;
  totalPoolCollected: number;
  totalPrizeDisbursed: number;
  totalDividendsDistributed: number;
}


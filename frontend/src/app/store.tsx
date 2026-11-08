import React, { createContext, useContext, useState, useEffect } from 'react';
import { ChitGroup, ChitMember, Auction, Bid, LedgerTransaction, Payment, DueSummary, User, RoleName } from '../types';
import { api } from '../services/apiClient';

interface AppContextType {
  currentUser: User;
  currentRole: RoleName;
  switchRole: (role: RoleName) => void;
  activeGroup: ChitGroup | null;
  setActiveGroup: (group: ChitGroup | null) => void;
  groups: ChitGroup[];
  members: ChitMember[];
  auctions: Auction[];
  currentAuction: Auction | null;
  bids: Bid[];
  ledger: LedgerTransaction[];
  payments: Payment[];
  dues: DueSummary[];
  refreshData: () => Promise<void>;
  // Actions
  placeBid: (bidAmount: number) => Promise<void>;
  openAuction: (auctionId: string) => Promise<void>;
  closeAuction: (auctionId: string) => Promise<void>;
  declareWinner: (auctionId: string) => Promise<void>;
  settleAuction: (auctionId: string) => Promise<void>;
  recordPayment: (memberId: string, amount: number, method: string) => Promise<void>;
  createGroup: (data: any) => Promise<void>;
  addMember: (data: any) => Promise<void>;
}

// Initial Demo Seed Data matching Section 1.2 "Business Example"
const SEED_GROUP_ID = '33333333-3333-3333-3333-333333333301';
const SEED_AUCTION_ID = '44444444-4444-4444-4444-444444444401';

const ADMIN_USER: User = {
  id: '22222222-2222-2222-2222-222222222201',
  fullName: 'Rajesh Sharma (Admin)',
  email: 'admin@chitcircle.com',
  status: 'ACTIVE',
  roles: ['ROLE_GROUP_ADMIN'],
  createdAt: '2026-01-01T00:00:00Z'
};

const MEMBER_USER: User = {
  id: '22222222-2222-2222-2222-222222222202',
  fullName: 'Amit Patel (Member #1)',
  email: 'member@chitcircle.com',
  status: 'ACTIVE',
  roles: ['ROLE_GROUP_MEMBER'],
  createdAt: '2026-01-01T00:00:00Z'
};

const INITIAL_GROUPS: ChitGroup[] = [
  {
    id: SEED_GROUP_ID,
    name: 'ChitCircle Friends & Family 2026',
    chitValue: 4000000,
    monthlyContribution: 10000,
    totalMembers: 20,
    durationMonths: 20,
    startDate: '2026-01-01',
    status: 'ACTIVE',
    createdById: ADMIN_USER.id,
    createdByName: ADMIN_USER.fullName,
    currentMemberCount: 20,
    createdAt: '2026-01-01T00:00:00Z'
  }
];

// Generate 20 members
const INITIAL_MEMBERS: ChitMember[] = Array.from({ length: 20 }, (_, i) => {
  const num = i + 1;
  const names = [
    'Amit Patel', 'Bhavna Shah', 'Chirag Desai', 'Divya Joshi', 'Eshwar Rao',
    'Farhan Akhtar', 'Gaurav Mehta', 'Harini Iyer', 'Ishaan Verma', 'Jayesh Nair',
    'Kavita Reddy', 'Laxman Sen', 'Madhav Kulkarni', 'Neha Gupta', 'Omkar Patil',
    'Pooja Sharma', 'Qadir Sheikh', 'Rohit Saxena', 'Sunita Roy', 'Tarun Bhatia'
  ];
  return {
    id: `member-uuid-${num}`,
    groupId: SEED_GROUP_ID,
    groupName: 'ChitCircle Friends & Family 2026',
    userId: num === 1 ? MEMBER_USER.id : `user-uuid-${num}`,
    fullName: names[i] || `Member ${num}`,
    email: `member${num}@chitcircle.com`,
    phoneNumber: `+9198765000${num < 10 ? '0' + num : num}`,
    memberNumber: num,
    status: 'ACTIVE',
    prizeReceived: false,
    joinedAt: '2026-01-01T00:00:00Z'
  };
});

const INITIAL_AUCTIONS: Auction[] = [
  {
    id: SEED_AUCTION_ID,
    groupId: SEED_GROUP_ID,
    groupName: 'ChitCircle Friends & Family 2026',
    monthNumber: 1,
    auctionDate: '2026-02-15T18:00:00Z',
    totalPool: 200000,
    status: 'OPEN',
    winningDiscount: 40000,
    prizeAmount: 160000,
    dividendPerMember: 2000
  }
];

const INITIAL_BIDS: Bid[] = [
  {
    id: 'bid-1',
    auctionId: SEED_AUCTION_ID,
    memberId: 'member-uuid-1',
    memberName: 'Amit Patel',
    memberNumber: 1,
    bidAmount: 40000,
    status: 'VALID',
    submittedAt: '2026-02-15T18:10:00Z',
    isHighest: true
  },
  {
    id: 'bid-2',
    auctionId: SEED_AUCTION_ID,
    memberId: 'member-uuid-3',
    memberName: 'Chirag Desai',
    memberNumber: 3,
    bidAmount: 35000,
    status: 'VALID',
    submittedAt: '2026-02-15T18:05:00Z',
    isHighest: false
  },
  {
    id: 'bid-3',
    auctionId: SEED_AUCTION_ID,
    memberId: 'member-uuid-7',
    memberName: 'Gaurav Mehta',
    memberNumber: 7,
    bidAmount: 28000,
    status: 'VALID',
    submittedAt: '2026-02-15T18:02:00Z',
    isHighest: false
  }
];

const INITIAL_LEDGER: LedgerTransaction[] = [
  {
    id: 'tx-1',
    groupId: SEED_GROUP_ID,
    groupName: 'ChitCircle Friends & Family 2026',
    memberId: 'member-uuid-1',
    memberName: 'Amit Patel',
    memberNumber: 1,
    transactionType: 'CONTRIBUTION',
    amount: 10000,
    direction: 'CREDIT',
    createdAt: '2026-02-01T10:00:00Z'
  },
  {
    id: 'tx-2',
    groupId: SEED_GROUP_ID,
    groupName: 'ChitCircle Friends & Family 2026',
    memberId: 'member-uuid-2',
    memberName: 'Bhavna Shah',
    memberNumber: 2,
    transactionType: 'CONTRIBUTION',
    amount: 10000,
    direction: 'CREDIT',
    createdAt: '2026-02-01T10:15:00Z'
  }
];

const AppContext = createContext<AppContextType | null>(null);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<User>(ADMIN_USER);
  const [currentRole, setCurrentRole] = useState<RoleName>('GROUP_ADMIN');
  const [activeGroup, setActiveGroup] = useState<ChitGroup | null>(INITIAL_GROUPS[0]);
  const [groups, setGroups] = useState<ChitGroup[]>(INITIAL_GROUPS);
  const [members, setMembers] = useState<ChitMember[]>(INITIAL_MEMBERS);
  const [auctions, setAuctions] = useState<Auction[]>(INITIAL_AUCTIONS);
  const [currentAuction, setCurrentAuction] = useState<Auction | null>(INITIAL_AUCTIONS[0]);
  const [bids, setBids] = useState<Bid[]>(INITIAL_BIDS);
  const [ledger, setLedger] = useState<LedgerTransaction[]>(INITIAL_LEDGER);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [dues, setDues] = useState<DueSummary[]>([]);

  const switchRole = (role: RoleName) => {
    setCurrentRole(role);
    if (role === 'GROUP_ADMIN') {
      setCurrentUser(ADMIN_USER);
    } else {
      setCurrentUser(MEMBER_USER);
    }
  };

  const refreshData = async () => {
    try {
      if (activeGroup) {
        const fetchedAuctions = await api.getAuctions(activeGroup.id);
        if (fetchedAuctions && fetchedAuctions.length > 0) {
          setAuctions(fetchedAuctions);
          const openA = fetchedAuctions.find(a => a.status === 'OPEN' || a.status === 'WINNER_DECLARED') || fetchedAuctions[0];
          setCurrentAuction(openA);
          if (openA) {
            const fetchedBids = await api.getBids(openA.id);
            setBids(fetchedBids);
          }
        }
      }
    } catch (e) {
      // In offline/mock mode, retain local state
    }
  };

  const placeBid = async (bidAmount: number) => {
    if (!currentAuction) return;
    try {
      const newBid = await api.placeBid(currentAuction.id, 'member-uuid-1', bidAmount);
      setBids(prev => [newBid, ...prev]);
    } catch {
      // Mock Fallback
      const newBid: Bid = {
        id: `bid-${Date.now()}`,
        auctionId: currentAuction.id,
        memberId: currentRole === 'GROUP_ADMIN' ? 'member-uuid-2' : 'member-uuid-1',
        memberName: currentRole === 'GROUP_ADMIN' ? 'Bhavna Shah' : 'Amit Patel',
        memberNumber: currentRole === 'GROUP_ADMIN' ? 2 : 1,
        bidAmount,
        status: 'VALID',
        submittedAt: new Date().toISOString(),
        isHighest: true
      };

      setBids(prev => {
        const updated = prev.map(b => ({ ...b, isHighest: false }));
        return [newBid, ...updated].sort((a, b) => b.bidAmount - a.bidAmount);
      });
    }
  };

  const openAuction = async (auctionId: string) => {
    try {
      await api.openAuction(auctionId);
    } catch {}
    setAuctions(prev => prev.map(a => a.id === auctionId ? { ...a, status: 'OPEN' } : a));
    setCurrentAuction(prev => prev && prev.id === auctionId ? { ...prev, status: 'OPEN' } : prev);
  };

  const closeAuction = async (auctionId: string) => {
    try {
      await api.closeAuction(auctionId);
    } catch {}
    setAuctions(prev => prev.map(a => a.id === auctionId ? { ...a, status: 'CLOSED' } : a));
    setCurrentAuction(prev => prev && prev.id === auctionId ? { ...prev, status: 'CLOSED' } : prev);
  };

  const declareWinner = async (auctionId: string) => {
    const highestBid = bids.slice().sort((a, b) => b.bidAmount - a.bidAmount)[0];
    if (!highestBid) return;

    const discount = highestBid.bidAmount;
    const pool = currentAuction?.totalPool || 200000;
    const memberCount = activeGroup?.totalMembers || 20;
    const dividendPerMember = discount / memberCount;
    const prizeAmount = pool - discount;

    try {
      await api.declareWinner(auctionId);
    } catch {}

    setAuctions(prev => prev.map(a => a.id === auctionId ? {
      ...a,
      status: 'WINNER_DECLARED',
      winningDiscount: discount,
      prizeAmount,
      dividendPerMember,
      winnerMemberId: highestBid.memberId,
      winnerMemberName: highestBid.memberName,
      winnerMemberNumber: highestBid.memberNumber
    } : a));

    setCurrentAuction(prev => prev ? {
      ...prev,
      status: 'WINNER_DECLARED',
      winningDiscount: discount,
      prizeAmount,
      dividendPerMember,
      winnerMemberId: highestBid.memberId,
      winnerMemberName: highestBid.memberName,
      winnerMemberNumber: highestBid.memberNumber
    } : null);
  };

  const settleAuction = async (auctionId: string) => {
    if (!currentAuction || !currentAuction.winnerMemberId) return;

    try {
      await api.settleAuction(auctionId);
    } catch {}

    // Mark auction settled
    setAuctions(prev => prev.map(a => a.id === auctionId ? { ...a, status: 'SETTLED' } : a));
    setCurrentAuction(prev => prev ? { ...prev, status: 'SETTLED' } : null);

    // Update winner prize received
    setMembers(prev => prev.map(m => m.id === currentAuction.winnerMemberId ? { ...m, prizeReceived: true } : m));

    // Append to immutable ledger
    const now = new Date().toISOString();
    const newTxList: LedgerTransaction[] = [
      {
        id: `tx-payout-${Date.now()}`,
        groupId: SEED_GROUP_ID,
        groupName: activeGroup?.name || 'Chit Group',
        memberId: currentAuction.winnerMemberId,
        memberName: currentAuction.winnerMemberName,
        memberNumber: currentAuction.winnerMemberNumber,
        transactionType: 'PRIZE_PAYOUT',
        amount: currentAuction.prizeAmount || 160000,
        direction: 'DEBIT',
        createdAt: now
      },
      ...members.map(m => ({
        id: `tx-div-${m.id}-${Date.now()}`,
        groupId: SEED_GROUP_ID,
        groupName: activeGroup?.name || 'Chit Group',
        memberId: m.id,
        memberName: m.fullName,
        memberNumber: m.memberNumber,
        transactionType: 'DIVIDEND' as const,
        amount: currentAuction.dividendPerMember || 2000,
        direction: 'CREDIT' as const,
        createdAt: now
      }))
    ];

    setLedger(prev => [...newTxList, ...prev]);
  };

  const recordPayment = async (memberId: string, amount: number, method: string) => {
    const member = members.find(m => m.id === memberId);
    const newPayment: Payment = {
      id: `pay-${Date.now()}`,
      groupId: SEED_GROUP_ID,
      groupName: activeGroup?.name || 'Chit Group',
      memberId,
      memberName: member?.fullName || 'Member',
      memberNumber: member?.memberNumber || 1,
      auctionMonth: 1,
      amount,
      paymentDate: new Date().toISOString(),
      paymentMethod: method,
      referenceNumber: `UPI-${Date.now().toString().slice(-6)}`,
      status: 'CONFIRMED'
    };

    setPayments(prev => [newPayment, ...prev]);
    setLedger(prev => [
      {
        id: `tx-pay-${Date.now()}`,
        groupId: SEED_GROUP_ID,
        groupName: activeGroup?.name || 'Chit Group',
        memberId,
        memberName: member?.fullName || 'Member',
        memberNumber: member?.memberNumber || 1,
        transactionType: 'CONTRIBUTION',
        amount,
        direction: 'CREDIT',
        createdAt: new Date().toISOString()
      },
      ...prev
    ]);
  };

  const createGroup = async (data: any) => {
    const newGroup: ChitGroup = {
      id: `group-${Date.now()}`,
      name: data.name,
      chitValue: data.monthlyContribution * data.totalMembers * data.durationMonths,
      monthlyContribution: data.monthlyContribution,
      totalMembers: data.totalMembers,
      durationMonths: data.durationMonths,
      startDate: data.startDate,
      status: 'ACTIVE',
      createdById: currentUser.id,
      createdByName: currentUser.fullName,
      currentMemberCount: 1,
      createdAt: new Date().toISOString()
    };
    setGroups(prev => [newGroup, ...prev]);
    setActiveGroup(newGroup);
  };

  const addMember = async (data: any) => {
    if (!activeGroup) return;
    const newMember: ChitMember = {
      id: `member-${Date.now()}`,
      groupId: activeGroup.id,
      groupName: activeGroup.name,
      userId: `user-${Date.now()}`,
      fullName: data.fullName,
      email: data.email,
      phoneNumber: data.phoneNumber,
      memberNumber: members.length + 1,
      status: 'ACTIVE',
      prizeReceived: false,
      joinedAt: new Date().toISOString()
    };
    setMembers(prev => [...prev, newMember]);
  };

  return (
    <AppContext.Provider
      value={{
        currentUser,
        currentRole,
        switchRole,
        activeGroup,
        setActiveGroup,
        groups,
        members,
        auctions,
        currentAuction,
        bids,
        ledger,
        payments,
        dues,
        refreshData,
        placeBid,
        openAuction,
        closeAuction,
        declareWinner,
        settleAuction,
        recordPayment,
        createGroup,
        addMember
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error('useApp must be used within an AppProvider');
  return context;
};


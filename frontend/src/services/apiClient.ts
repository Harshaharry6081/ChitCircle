import {
  ChitGroup,
  ChitMember,
  Auction,
  Bid,
  AuctionResult,
  Payment,
  DueSummary,
  LedgerBalance,
  GroupSummaryReport,
  User
} from '../types';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

class ApiClient {
  private token: string | null = null;

  constructor() {
    this.token = localStorage.getItem('chitcircle_token');
  }

  setToken(token: string | null) {
    this.token = token;
    if (token) {
      localStorage.setItem('chitcircle_token', token);
    } else {
      localStorage.removeItem('chitcircle_token');
    }
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>)
    };

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Request failed with status ${response.status}`);
      }

      const resJson = await response.json();
      return resJson.data !== undefined ? resJson.data : resJson;
    } catch (err: any) {
      // If backend is not running, throw error or let UI handle mock fallback
      console.warn(`API call to ${endpoint} failed:`, err.message);
      throw err;
    }
  }

  // Auth
  async login(email: string, password: string) {
    return this.request<{ accessToken: string; user: User }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });
  }

  async getMe() {
    return this.request<User>('/auth/me');
  }

  // Groups
  async getGroups(): Promise<ChitGroup[]> {
    return this.request<ChitGroup[]>('/groups');
  }

  async getGroup(id: string): Promise<ChitGroup> {
    return this.request<ChitGroup>(`/groups/${id}`);
  }

  async createGroup(data: any): Promise<ChitGroup> {
    return this.request<ChitGroup>('/groups', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  async activateGroup(id: string): Promise<ChitGroup> {
    return this.request<ChitGroup>(`/groups/${id}/activate`, { method: 'POST' });
  }

  // Members
  async getMembers(groupId: string): Promise<ChitMember[]> {
    return this.request<ChitMember[]>(`/groups/${groupId}/members`);
  }

  async addMember(groupId: string, data: any): Promise<ChitMember> {
    return this.request<ChitMember>(`/groups/${groupId}/members`, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  // Auctions
  async getAuctions(groupId: string): Promise<Auction[]> {
    return this.request<Auction[]>(`/groups/${groupId}/auctions`);
  }

  async getAuction(auctionId: string): Promise<Auction> {
    return this.request<Auction>(`/auctions/${auctionId}`);
  }

  async createAuction(groupId: string, monthNumber: number, auctionDate: string): Promise<Auction> {
    return this.request<Auction>(`/groups/${groupId}/auctions`, {
      method: 'POST',
      body: JSON.stringify({ monthNumber, auctionDate })
    });
  }

  async openAuction(auctionId: string): Promise<Auction> {
    return this.request<Auction>(`/auctions/${auctionId}/open`, { method: 'POST' });
  }

  async closeAuction(auctionId: string): Promise<Auction> {
    return this.request<Auction>(`/auctions/${auctionId}/close`, { method: 'POST' });
  }

  async declareWinner(auctionId: string): Promise<AuctionResult> {
    return this.request<AuctionResult>(`/auctions/${auctionId}/declare-winner`, { method: 'POST' });
  }

  async settleAuction(auctionId: string): Promise<Auction> {
    return this.request<Auction>(`/auctions/${auctionId}/settle`, { method: 'POST' });
  }

  // Bids
  async getBids(auctionId: string): Promise<Bid[]> {
    return this.request<Bid[]>(`/auctions/${auctionId}/bids`);
  }

  async placeBid(auctionId: string, memberId: string, bidAmount: number): Promise<Bid> {
    return this.request<Bid>(`/auctions/${auctionId}/bids`, {
      method: 'POST',
      body: JSON.stringify({ memberId, bidAmount })
    });
  }

  // Payments
  async getPayments(groupId?: string, month?: number): Promise<Payment[]> {
    const params = new URLSearchParams();
    if (groupId) params.append('groupId', groupId);
    if (month) params.append('month', month.toString());
    return this.request<Payment[]>(`/payments?${params.toString()}`);
  }

  async recordPayment(data: any): Promise<Payment> {
    return this.request<Payment>('/payments', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  async getDues(groupId: string, month: number): Promise<DueSummary[]> {
    return this.request<DueSummary[]>(`/groups/${groupId}/dues?month=${month}`);
  }

  // Ledger
  async getGroupLedger(groupId: string): Promise<LedgerBalance> {
    return this.request<LedgerBalance>(`/groups/${groupId}/ledger`);
  }

  async getMemberLedger(memberId: string): Promise<LedgerBalance> {
    return this.request<LedgerBalance>(`/members/${memberId}/ledger`);
  }

  // Reports
  async getGroupSummary(groupId: string): Promise<GroupSummaryReport> {
    return this.request<GroupSummaryReport>(`/reports/group-summary?groupId=${groupId}`);
  }
}

export const api = new ApiClient();


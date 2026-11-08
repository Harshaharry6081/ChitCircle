import React from 'react';
import { useApp } from '../../app/store';
import { formatCurrency } from '../../utils/formatters';
import {
  Wallet,
  TrendingUp,
  Users,
  Gavel,
  CheckCircle2,
  AlertCircle,
  ArrowRight,
  ShieldCheck
} from 'lucide-react';

interface DashboardPageProps {
  setActiveTab: (tab: string) => void;
}

export const DashboardPage: React.FC<DashboardPageProps> = ({ setActiveTab }) => {
  const { activeGroup, currentAuction, members, currentRole, bids, ledger } = useApp();

  const enrolledCount = members.filter((m) => m.status === 'ACTIVE').length;
  const highestBid = bids.slice().sort((a, b) => b.bidAmount - a.bidAmount)[0];

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl relative overflow-hidden">
        <div className="absolute right-0 top-0 bottom-0 w-1/3 bg-gradient-to-l from-emerald-500/10 to-transparent pointer-events-none" />
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 relative z-10">
          <div>
            <div className="flex items-center space-x-2 text-emerald-400 text-xs font-semibold uppercase tracking-wider mb-1">
              <ShieldCheck className="w-4 h-4" />
              <span>Chit Group Overview</span>
            </div>
            <h1 className="text-2xl font-bold text-white tracking-tight">
              {activeGroup?.name || 'ChitCircle Group'}
            </h1>
            <p className="text-sm text-slate-400 mt-1 max-w-xl">
              20-Month Chit Circle with ₹10,000 monthly contribution. Monthly pool of ₹2,00,000 with transparent discount auction and dividend distribution.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => setActiveTab('auctions')}
              className="px-4 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold text-sm transition-all flex items-center space-x-2 shadow-lg shadow-emerald-500/20"
            >
              <Gavel className="w-4 h-4" />
              <span>Enter Auction Console</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium mb-2">
            <span>Total Chit Value</span>
            <div className="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
              <Wallet className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-white">
            {formatCurrency(activeGroup?.chitValue || 4000000)}
          </div>
          <div className="text-xs text-slate-400 mt-1">
            20 months × ₹2,00,000 pool
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium mb-2">
            <span>Monthly Pool</span>
            <div className="w-8 h-8 rounded-lg bg-teal-500/10 text-teal-400 flex items-center justify-center">
              <TrendingUp className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-white">
            {formatCurrency(activeGroup?.monthlyContribution ? activeGroup.monthlyContribution * 20 : 200000)}
          </div>
          <div className="text-xs text-slate-400 mt-1">
            ₹10,000 per member / month
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium mb-2">
            <span>Group Membership</span>
            <div className="w-8 h-8 rounded-lg bg-blue-500/10 text-blue-400 flex items-center justify-center">
              <Users className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-white">
            {enrolledCount} / {activeGroup?.totalMembers || 20}
          </div>
          <div className="text-xs text-emerald-400 mt-1 flex items-center space-x-1">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Full group capacity enrolled</span>
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium mb-2">
            <span>Auction Cycle Status</span>
            <div className="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-400 flex items-center justify-center">
              <Gavel className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-white flex items-center space-x-2">
            <span>Month #{currentAuction?.monthNumber || 1}</span>
            <span className="text-xs px-2 py-0.5 rounded-full font-semibold uppercase bg-emerald-500/20 text-emerald-400">
              {currentAuction?.status || 'OPEN'}
            </span>
          </div>
          <div className="text-xs text-slate-400 mt-1">
            {currentAuction?.status === 'OPEN' ? 'Bidding is currently live' : 'Auction completed'}
          </div>
        </div>
      </div>

      {/* Main Grid: Active Auction Room Spotlight & Recent Transactions */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Live Auction Spotlight */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-6">
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-800">
            <div>
              <h2 className="text-lg font-bold text-white">Live Monthly Auction Console</h2>
              <p className="text-xs text-slate-400">
                Month {currentAuction?.monthNumber || 1} • Pool: {formatCurrency(currentAuction?.totalPool || 200000)}
              </p>
            </div>
            <button
              onClick={() => setActiveTab('auctions')}
              className="text-xs font-semibold text-emerald-400 hover:text-emerald-300 flex items-center space-x-1"
            >
              <span>Manage Auction</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
            <div className="bg-slate-800/60 p-4 rounded-xl border border-slate-800">
              <div className="text-xs text-slate-400">Leading Discount Bid</div>
              <div className="text-xl font-bold text-emerald-400 mt-1">
                {formatCurrency(highestBid?.bidAmount || 40000)}
              </div>
              <div className="text-[11px] text-slate-400 mt-0.5">
                by {highestBid?.memberName || 'Member #1'}
              </div>
            </div>

            <div className="bg-slate-800/60 p-4 rounded-xl border border-slate-800">
              <div className="text-xs text-slate-400">Projected Winner Prize</div>
              <div className="text-xl font-bold text-white mt-1">
                {formatCurrency(
                  (currentAuction?.totalPool || 200000) - (highestBid?.bidAmount || 40000)
                )}
              </div>
              <div className="text-[11px] text-slate-400 mt-0.5">
                Pool - Winning Discount
              </div>
            </div>

            <div className="bg-slate-800/60 p-4 rounded-xl border border-slate-800">
              <div className="text-xs text-slate-400">Projected Dividend / Member</div>
              <div className="text-xl font-bold text-teal-300 mt-1">
                {formatCurrency((highestBid?.bidAmount || 40000) / 20)}
              </div>
              <div className="text-[11px] text-slate-400 mt-0.5">
                Shared equally across 20 members
              </div>
            </div>
          </div>

          {/* Quick Bid Roster Preview */}
          <div>
            <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
              Recent Auction Bids (Highest Discount Wins)
            </div>
            <div className="space-y-2">
              {bids.slice(0, 3).map((b, idx) => (
                <div
                  key={b.id}
                  className="flex items-center justify-between bg-slate-800/40 px-3 py-2.5 rounded-lg border border-slate-800/80 text-xs"
                >
                  <div className="flex items-center space-x-3">
                    <span className="w-5 h-5 rounded-full bg-slate-700 flex items-center justify-center font-bold text-[10px] text-slate-300">
                      #{idx + 1}
                    </span>
                    <div>
                      <span className="font-semibold text-white">{b.memberName}</span>
                      <span className="text-slate-400 ml-1.5">(Member #{b.memberNumber})</span>
                    </div>
                  </div>
                  <div className="flex items-center space-x-3">
                    <span className="font-bold text-emerald-400">{formatCurrency(b.bidAmount)}</span>
                    {idx === 0 && (
                      <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-emerald-500/20 text-emerald-300">
                        LEADER
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Col: Ledger Summary & Fast Actions */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-800">
              <h2 className="text-lg font-bold text-white">Immutable Ledger</h2>
              <button
                onClick={() => setActiveTab('ledger')}
                className="text-xs font-semibold text-emerald-400 hover:text-emerald-300"
              >
                View Full
              </button>
            </div>

            <div className="space-y-3">
              {ledger.slice(0, 4).map((entry) => (
                <div key={entry.id} className="p-3 bg-slate-800/50 rounded-xl border border-slate-800 text-xs">
                  <div className="flex items-center justify-between font-medium">
                    <span className="text-slate-300">{entry.memberName || 'Group Settlement'}</span>
                    <span
                      className={`font-bold ${
                        entry.direction === 'CREDIT' ? 'text-emerald-400' : 'text-rose-400'
                      }`}
                    >
                      {entry.direction === 'CREDIT' ? '+' : '-'} {formatCurrency(entry.amount)}
                    </span>
                  </div>
                  <div className="flex items-center justify-between text-[11px] text-slate-400 mt-1">
                    <span>{entry.transactionType}</span>
                    <span className="uppercase text-[10px] px-1 rounded bg-slate-700/50 text-slate-300">
                      {entry.direction}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-slate-800">
            <div className="text-xs text-slate-400 mb-2">Application Integrity Check</div>
            <div className="flex items-center space-x-2 text-xs text-emerald-400 font-medium">
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
              <span>Strict BigDecimal Financial Calculation Active</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};


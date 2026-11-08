import React, { useState } from 'react';
import { useApp } from '../../app/store';
import { formatCurrency } from '../../utils/formatters';
import {
  FileText,
  Download,
  Printer,
  PieChart,
  UserCheck,
  ShieldCheck,
  TrendingUp,
  Wallet
} from 'lucide-react';

export const ReportsPage: React.FC = () => {
  const { activeGroup, members, currentAuction, ledger } = useApp();
  const [selectedMemberId, setSelectedMemberId] = useState<string>(members[0]?.id || '');

  const member = members.find((m) => m.id === selectedMemberId) || members[0];
  const memberTxs = ledger.filter((tx) => tx.memberId === member?.id);

  const totalContributions = memberTxs
    .filter((tx) => tx.transactionType === 'CONTRIBUTION')
    .reduce((sum, tx) => sum + tx.amount, 10000);

  const totalDividends = memberTxs
    .filter((tx) => tx.transactionType === 'DIVIDEND')
    .reduce((sum, tx) => sum + tx.amount, 2000);

  const prizeWon = member?.prizeReceived ? (currentAuction?.prizeAmount || 160000) : 0;
  const netPosition = prizeWon + totalDividends - totalContributions;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Financial Reports & Statements</h1>
          <p className="text-xs text-slate-400 mt-1">
            Group balance summaries, member ledger statements, and financial performance reports.
          </p>
        </div>

        <div className="flex items-center space-x-2">
          <button
            onClick={() => window.print()}
            className="px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-white font-medium text-xs rounded-xl border border-slate-700 flex items-center space-x-1.5 transition-all"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>Print Report</span>
          </button>
        </div>
      </div>

      {/* Group Summary Statement */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
        <div className="flex items-center justify-between pb-4 border-b border-slate-800 mb-4">
          <div>
            <div className="text-xs font-semibold uppercase text-emerald-400 flex items-center space-x-1.5">
              <PieChart className="w-3.5 h-3.5" />
              <span>Group Performance Statement</span>
            </div>
            <h2 className="text-lg font-bold text-white mt-0.5">{activeGroup?.name}</h2>
          </div>
          <span className="px-3 py-1 bg-emerald-500/15 text-emerald-300 border border-emerald-500/30 text-xs rounded-full font-bold">
            CYCLE IN PROGRESS
          </span>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div className="p-3 bg-slate-800/50 rounded-xl border border-slate-800">
            <div className="text-xs text-slate-400">Total Chit Pool</div>
            <div className="text-base font-bold text-white mt-1">
              {formatCurrency(activeGroup?.chitValue || 4000000)}
            </div>
            <div className="text-[10px] text-slate-400 mt-0.5">20 months × ₹2,00,000</div>
          </div>

          <div className="p-3 bg-slate-800/50 rounded-xl border border-slate-800">
            <div className="text-xs text-slate-400">Auctions Settled</div>
            <div className="text-base font-bold text-emerald-400 mt-1">
              1 of 20 Months
            </div>
            <div className="text-[10px] text-slate-400 mt-0.5">19 months remaining</div>
          </div>

          <div className="p-3 bg-slate-800/50 rounded-xl border border-slate-800">
            <div className="text-xs text-slate-400">Total Prize Disbursed</div>
            <div className="text-base font-bold text-teal-300 mt-1">
              {formatCurrency(160000)}
            </div>
            <div className="text-[10px] text-slate-400 mt-0.5">Month 1 prize settlement</div>
          </div>

          <div className="p-3 bg-slate-800/50 rounded-xl border border-slate-800">
            <div className="text-xs text-slate-400">Dividends Distributed</div>
            <div className="text-base font-bold text-emerald-400 mt-1">
              {formatCurrency(40000)}
            </div>
            <div className="text-[10px] text-slate-400 mt-0.5">₹2,000 × 20 members</div>
          </div>
        </div>
      </div>

      {/* Member-Wise Individual Statement */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-800 mb-6 gap-3">
          <div>
            <h3 className="text-base font-bold text-white flex items-center space-x-2">
              <UserCheck className="w-4 h-4 text-emerald-400" />
              <span>Member Individual Financial Statement</span>
            </h3>
            <p className="text-xs text-slate-400 mt-0.5">
              Net financial position and full audit trail for the selected member.
            </p>
          </div>

          <div className="flex items-center space-x-2">
            <span className="text-xs text-slate-400">Member:</span>
            <select
              value={selectedMemberId}
              onChange={(e) => setSelectedMemberId(e.target.value)}
              className="bg-slate-800 border border-slate-700 text-xs text-white rounded-xl px-3 py-1.5 focus:outline-none"
            >
              {members.map((m) => (
                <option key={m.id} value={m.id}>
                  #{m.memberNumber} - {m.fullName}
                </option>
              ))}
            </select>
          </div>
        </div>

        {member && (
          <div className="space-y-6">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div className="p-4 bg-slate-800/60 rounded-xl border border-slate-800">
                <div className="text-xs text-slate-400">Total Contributions Paid</div>
                <div className="text-lg font-bold text-white mt-1">
                  {formatCurrency(totalContributions)}
                </div>
              </div>

              <div className="p-4 bg-slate-800/60 rounded-xl border border-slate-800">
                <div className="text-xs text-slate-400">Dividends Earned</div>
                <div className="text-lg font-bold text-emerald-400 mt-1">
                  + {formatCurrency(totalDividends)}
                </div>
              </div>

              <div className="p-4 bg-slate-800/60 rounded-xl border border-slate-800">
                <div className="text-xs text-slate-400">Prize Amount Won</div>
                <div className="text-lg font-bold text-white mt-1">
                  {formatCurrency(prizeWon)}
                </div>
              </div>

              <div className="p-4 bg-slate-800/60 rounded-xl border border-slate-800">
                <div className="text-xs text-slate-400">Net Financial Position</div>
                <div
                  className={`text-lg font-bold mt-1 ${
                    netPosition >= 0 ? 'text-emerald-400' : 'text-slate-300'
                  }`}
                >
                  {formatCurrency(netPosition)}
                </div>
              </div>
            </div>

            <div className="p-4 bg-slate-800/40 rounded-xl border border-slate-800 text-xs text-slate-400 flex items-center justify-between">
              <div>
                <span className="font-semibold text-white">Status: </span>
                {member.prizeReceived ? 'Prize Winner (Paid)' : 'Active Contributor'}
              </div>
              <div className="text-emerald-400 font-semibold">
                Member #{member.memberNumber} of 20
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};


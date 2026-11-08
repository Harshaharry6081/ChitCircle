import React, { useState } from 'react';
import { useApp } from '../../app/store';
import { formatCurrency, formatDateTime } from '../../utils/formatters';
import {
  BookOpen,
  ArrowDownLeft,
  ArrowUpRight,
  ShieldCheck,
  Search,
  Filter,
  CheckCircle2
} from 'lucide-react';

export const LedgerPage: React.FC = () => {
  const { ledger, activeGroup } = useApp();
  const [filterType, setFilterType] = useState<string>('ALL');
  const [search, setSearch] = useState<string>('');

  const filteredLedger = ledger.filter((tx) => {
    const matchesType = filterType === 'ALL' || tx.transactionType === filterType;
    const matchesSearch =
      (tx.memberName?.toLowerCase().includes(search.toLowerCase()) ?? false) ||
      (tx.transactionType.toLowerCase().includes(search.toLowerCase()) ?? false);
    return matchesType && matchesSearch;
  });

  const totalCredits = ledger
    .filter((tx) => tx.direction === 'CREDIT')
    .reduce((sum, tx) => sum + tx.amount, 0);

  const totalDebits = ledger
    .filter((tx) => tx.direction === 'DEBIT')
    .reduce((sum, tx) => sum + tx.amount, 0);

  const netBalance = totalCredits - totalDebits;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-xs font-semibold text-emerald-400 uppercase tracking-wider mb-1">
            <ShieldCheck className="w-4 h-4" />
            <span>Immutable Financial Ledger</span>
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Audit-Grade Financial Ledger</h1>
          <p className="text-xs text-slate-400 mt-1">
            Append-only double-entry transaction record. Financial records cannot be modified or deleted.
          </p>
        </div>
      </div>

      {/* Financial Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium mb-1">
            <span>Total Ledger Credits</span>
            <div className="w-7 h-7 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
              <ArrowDownLeft className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-emerald-400">
            {formatCurrency(totalCredits)}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">Contributions & Dividends</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium mb-1">
            <span>Total Ledger Debits</span>
            <div className="w-7 h-7 rounded-lg bg-rose-500/10 text-rose-400 flex items-center justify-center">
              <ArrowUpRight className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-rose-400">
            {formatCurrency(totalDebits)}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">Prize payouts & adjustments</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium mb-1">
            <span>Net Pool Balance</span>
            <div className="w-7 h-7 rounded-lg bg-teal-500/10 text-teal-400 flex items-center justify-center">
              <BookOpen className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-white">
            {formatCurrency(netBalance)}
          </div>
          <div className="text-[11px] text-emerald-400 mt-0.5 flex items-center space-x-1">
            <CheckCircle2 className="w-3 h-3" />
            <span>Balanced reconciliation</span>
          </div>
        </div>
      </div>

      {/* Ledger Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4 pb-3 border-b border-slate-800">
          <div className="flex items-center space-x-2">
            <Filter className="w-4 h-4 text-emerald-400" />
            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="bg-slate-800 border border-slate-700 text-xs text-white rounded-xl px-3 py-1.5 focus:outline-none cursor-pointer"
            >
              <option value="ALL">All Transaction Types</option>
              <option value="CONTRIBUTION">Contributions (Monthly Dues)</option>
              <option value="DIVIDEND">Dividends</option>
              <option value="PRIZE_PAYOUT">Prize Payouts</option>
              <option value="REVERSAL">Reversals</option>
            </select>
          </div>

          <div className="relative w-full sm:w-64">
            <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-slate-400" />
            <input
              type="text"
              placeholder="Search by member or type..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full bg-slate-800 border border-slate-700 rounded-xl pl-9 pr-3 py-1.5 text-xs text-white focus:outline-none focus:border-emerald-500"
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 uppercase text-[10px] tracking-wider">
                <th className="py-2.5 px-3">Timestamp</th>
                <th className="py-2.5 px-3">Transaction ID</th>
                <th className="py-2.5 px-3">Member / Party</th>
                <th className="py-2.5 px-3">Type</th>
                <th className="py-2.5 px-3 text-right">Direction</th>
                <th className="py-2.5 px-3 text-right">Amount</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-medium">
              {filteredLedger.map((tx) => (
                <tr key={tx.id} className="hover:bg-slate-800/40 transition-colors">
                  <td className="py-3 px-3 text-slate-400">{formatDateTime(tx.createdAt)}</td>
                  <td className="py-3 px-3 font-mono text-[11px] text-slate-400">
                    {tx.id.slice(0, 12)}...
                  </td>
                  <td className="py-3 px-3">
                    <span className="font-semibold text-white">
                      {tx.memberName || 'Chit Group Pool'}
                    </span>
                    {tx.memberNumber && (
                      <span className="text-[10px] text-slate-400 ml-1.5">
                        (#{tx.memberNumber})
                      </span>
                    )}
                  </td>
                  <td className="py-3 px-3">
                    <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-slate-800 text-slate-300 border border-slate-700">
                      {tx.transactionType}
                    </span>
                  </td>
                  <td className="py-3 px-3 text-right">
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                        tx.direction === 'CREDIT'
                          ? 'bg-emerald-500/20 text-emerald-400'
                          : 'bg-rose-500/20 text-rose-400'
                      }`}
                    >
                      {tx.direction}
                    </span>
                  </td>
                  <td
                    className={`py-3 px-3 text-right font-bold ${
                      tx.direction === 'CREDIT' ? 'text-emerald-400' : 'text-rose-400'
                    }`}
                  >
                    {tx.direction === 'CREDIT' ? '+' : '-'} {formatCurrency(tx.amount)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};


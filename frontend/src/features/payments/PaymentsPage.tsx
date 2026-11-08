import React, { useState } from 'react';
import { useApp } from '../../app/store';
import { formatCurrency, formatDateTime } from '../../utils/formatters';
import {
  CreditCard,
  Plus,
  CheckCircle2,
  Clock,
  ArrowDownLeft,
  Search,
  DollarSign
} from 'lucide-react';

export const PaymentsPage: React.FC = () => {
  const { members, payments, recordPayment, currentAuction, currentRole } = useApp();
  const [showRecordModal, setShowRecordModal] = useState(false);
  const [selectedMemberId, setSelectedMemberId] = useState(members[0]?.id || '');
  const [amountInput, setAmountInput] = useState('8000');
  const [methodInput, setMethodInput] = useState('UPI');

  const dividendCredit = currentAuction?.dividendPerMember || 2000;
  const standardContribution = 10000;
  const netDuePerMember = Math.max(0, standardContribution - dividendCredit);

  const handleRecord = async (e: React.FormEvent) => {
    e.preventDefault();
    await recordPayment(selectedMemberId, parseFloat(amountInput), methodInput);
    setShowRecordModal(false);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Payments & Monthly Dues</h1>
          <p className="text-xs text-slate-400 mt-1">
            Track member monthly contributions, dividend credit offsets, and payment reconciliations.
          </p>
        </div>

        {currentRole === 'GROUP_ADMIN' && (
          <button
            onClick={() => setShowRecordModal(true)}
            className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-xs rounded-xl flex items-center space-x-1.5 transition-all shadow-lg shadow-emerald-500/20"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Record Payment</span>
          </button>
        )}
      </div>

      {/* Dues Calculation Summary Card */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <div className="text-xs text-slate-400">Monthly Contribution</div>
          <div className="text-xl font-bold text-white mt-1">
            {formatCurrency(standardContribution)}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">Fixed per agreement</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <div className="text-xs text-slate-400">Monthly Dividend Credit Offset</div>
          <div className="text-xl font-bold text-emerald-400 mt-1">
            - {formatCurrency(dividendCredit)}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">Auction discount distribution</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <div className="text-xs text-slate-400">Net Due Payable by Member</div>
          <div className="text-xl font-bold text-teal-300 mt-1">
            {formatCurrency(netDuePerMember)}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">Contribution - Dividend Credit</div>
        </div>
      </div>

      {/* Member Monthly Dues Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
        <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-800">
          <div>
            <h2 className="text-base font-bold text-white">Member Dues Roster (Month 1)</h2>
            <p className="text-xs text-slate-400">
              Each member's net payable amount is reduced by their dividend share!
            </p>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 uppercase text-[10px] tracking-wider">
                <th className="py-2.5 px-3">#</th>
                <th className="py-2.5 px-3">Member</th>
                <th className="py-2.5 px-3 text-right">Contribution</th>
                <th className="py-2.5 px-3 text-right">Dividend Credit</th>
                <th className="py-2.5 px-3 text-right">Net Payable</th>
                <th className="py-2.5 px-3 text-right">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-medium">
              {members.map((m) => {
                const paid = payments.some((p) => p.memberId === m.id);
                return (
                  <tr key={m.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-3 px-3">
                      <span className="w-6 h-6 rounded-full bg-slate-800 text-slate-300 font-bold text-[11px] inline-flex items-center justify-center">
                        {m.memberNumber}
                      </span>
                    </td>
                    <td className="py-3 px-3">
                      <div className="font-semibold text-white">{m.fullName}</div>
                      <div className="text-[10px] text-slate-400">{m.email}</div>
                    </td>
                    <td className="py-3 px-3 text-right text-slate-200">
                      {formatCurrency(standardContribution)}
                    </td>
                    <td className="py-3 px-3 text-right text-emerald-400 font-semibold">
                      - {formatCurrency(dividendCredit)}
                    </td>
                    <td className="py-3 px-3 text-right font-bold text-white">
                      {formatCurrency(netDuePerMember)}
                    </td>
                    <td className="py-3 px-3 text-right">
                      {paid ? (
                        <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 inline-flex items-center space-x-1">
                          <CheckCircle2 className="w-3 h-3" />
                          <span>PAID</span>
                        </span>
                      ) : (
                        <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30 inline-flex items-center space-x-1">
                          <Clock className="w-3 h-3" />
                          <span>PENDING</span>
                        </span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Record Payment Modal */}
      {showRecordModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <h3 className="text-lg font-bold text-white mb-1">Record Member Payment</h3>
            <p className="text-xs text-slate-400 mb-4">
              Enter payment collection details to reconcile dues and append to ledger.
            </p>

            <form onSubmit={handleRecord} className="space-y-4 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">Select Member</label>
                <select
                  value={selectedMemberId}
                  onChange={(e) => setSelectedMemberId(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-emerald-500"
                >
                  {members.map((m) => (
                    <option key={m.id} value={m.id}>
                      #{m.memberNumber} - {m.fullName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Payment Amount (₹)</label>
                <input
                  type="number"
                  required
                  value={amountInput}
                  onChange={(e) => setAmountInput(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white font-bold text-base focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Payment Method</label>
                <select
                  value={methodInput}
                  onChange={(e) => setMethodInput(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-emerald-500"
                >
                  <option value="UPI">UPI (Google Pay / PhonePe / Paytm)</option>
                  <option value="BANK_TRANSFER">Bank IMPS / NEFT Transfer</option>
                  <option value="CASH">Cash Collection</option>
                  <option value="CHEQUE">Cheque</option>
                </select>
              </div>

              <div className="pt-2 flex justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setShowRecordModal(false)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold rounded-xl shadow-lg"
                >
                  Confirm & Save
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};


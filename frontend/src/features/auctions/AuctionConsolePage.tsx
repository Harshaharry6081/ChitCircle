import React, { useState } from 'react';
import { useApp } from '../../app/store';
import { formatCurrency, formatDateTime } from '../../utils/formatters';
import {
  Gavel,
  Trophy,
  Calculator,
  Play,
  Square,
  Award,
  CheckCircle,
  AlertTriangle,
  Info,
  DollarSign
} from 'lucide-react';

export const AuctionConsolePage: React.FC = () => {
  const {
    currentAuction,
    activeGroup,
    bids,
    members,
    currentRole,
    currentUser,
    placeBid,
    openAuction,
    closeAuction,
    declareWinner,
    settleAuction
  } = useApp();

  const [bidAmountInput, setBidAmountInput] = useState<string>('42000');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!currentAuction || !activeGroup) {
    return (
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-400">
        <Gavel className="w-12 h-12 mx-auto text-slate-600 mb-3" />
        <h3 className="text-lg font-semibold text-white">No Active Auction Found</h3>
        <p className="text-sm mt-1">Please select an active chit group or schedule an auction.</p>
      </div>
    );
  }

  const pool = currentAuction.totalPool || 200000;
  const minDiscount = pool * 0.05; // 5% = 10,000
  const maxDiscount = pool * 0.40; // 40% = 80,000

  const sortedBids = bids.slice().sort((a, b) => b.bidAmount - a.bidAmount);
  const leadingBid = sortedBids[0];

  const currentMember = members.find((m) => m.userId === currentUser.id) || members[0];
  const hasWonPrize = currentMember?.prizeReceived;

  const handlePlaceBid = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    const amount = parseFloat(bidAmountInput);

    if (isNaN(amount)) {
      setErrorMsg('Please enter a valid numeric discount amount.');
      return;
    }
    if (amount < minDiscount) {
      setErrorMsg(`Discount must be at least ₹${minDiscount.toLocaleString('en-IN')} (5% minimum).`);
      return;
    }
    if (amount > maxDiscount) {
      setErrorMsg(`Discount cannot exceed statutory maximum ₹${maxDiscount.toLocaleString('en-IN')} (40%).`);
      return;
    }
    if (hasWonPrize) {
      setErrorMsg('You have already won the prize in this chit cycle and cannot bid again.');
      return;
    }

    try {
      setIsSubmitting(true);
      await placeBid(amount);
      setBidAmountInput('');
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to submit bid');
    } finally {
      setIsSubmitting(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'OPEN':
        return <span className="px-3 py-1 bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 rounded-full text-xs font-bold animate-pulse">● LIVE BIDDING OPEN</span>;
      case 'CLOSED':
        return <span className="px-3 py-1 bg-amber-500/20 text-amber-400 border border-amber-500/40 rounded-full text-xs font-bold">BIDDING CLOSED</span>;
      case 'WINNER_DECLARED':
        return <span className="px-3 py-1 bg-teal-500/20 text-teal-300 border border-teal-500/40 rounded-full text-xs font-bold">WINNER DECLARED</span>;
      case 'SETTLED':
        return <span className="px-3 py-1 bg-blue-500/20 text-blue-400 border border-blue-500/40 rounded-full text-xs font-bold">SETTLED & DISBURSED</span>;
      default:
        return <span className="px-3 py-1 bg-slate-800 text-slate-300 rounded-full text-xs font-bold">{status}</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Auction Header Card */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-3 mb-1">
              <h1 className="text-2xl font-bold text-white tracking-tight">
                Month #{currentAuction.monthNumber} Monthly Auction
              </h1>
              {getStatusBadge(currentAuction.status)}
            </div>
            <p className="text-sm text-slate-400">
              Group: <span className="text-white font-medium">{activeGroup.name}</span> • Total Pool: <span className="text-emerald-400 font-semibold">{formatCurrency(pool)}</span>
            </p>
          </div>

          {/* Admin Controls */}
          {currentRole === 'GROUP_ADMIN' && (
            <div className="flex flex-wrap items-center gap-2">
              {currentAuction.status === 'CREATED' && (
                <button
                  onClick={() => openAuction(currentAuction.id)}
                  className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold text-xs rounded-xl flex items-center space-x-1.5 shadow"
                >
                  <Play className="w-3.5 h-3.5 fill-current" />
                  <span>Start Live Auction</span>
                </button>
              )}

              {currentAuction.status === 'OPEN' && (
                <button
                  onClick={() => closeAuction(currentAuction.id)}
                  className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold text-xs rounded-xl flex items-center space-x-1.5 shadow"
                >
                  <Square className="w-3.5 h-3.5 fill-current" />
                  <span>Close Bidding</span>
                </button>
              )}

              {(currentAuction.status === 'CLOSED' || currentAuction.status === 'OPEN') && (
                <button
                  onClick={() => declareWinner(currentAuction.id)}
                  className="px-4 py-2 bg-teal-500 hover:bg-teal-400 text-slate-950 font-semibold text-xs rounded-xl flex items-center space-x-1.5 shadow"
                >
                  <Award className="w-3.5 h-3.5" />
                  <span>Declare Winner</span>
                </button>
              )}

              {currentAuction.status === 'WINNER_DECLARED' && (
                <button
                  onClick={() => settleAuction(currentAuction.id)}
                  className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold text-xs rounded-xl flex items-center space-x-1.5 shadow-lg shadow-emerald-500/25 animate-bounce"
                >
                  <CheckCircle className="w-3.5 h-3.5" />
                  <span>Settle & Commit to Ledger</span>
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Main Grid: Bidding Console & Live Leaderboard */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column (5 Cols): Live Bidding Panel & Math Formula */}
        <div className="lg:col-span-5 space-y-6">
          {/* Bid Entry Form */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-base font-bold text-white flex items-center space-x-2">
                <Gavel className="w-4 h-4 text-emerald-400" />
                <span>Place Discount Bid</span>
              </h2>
              <span className="text-xs text-slate-400">
                Member #{currentMember.memberNumber}: {currentMember.fullName}
              </span>
            </div>

            {hasWonPrize ? (
              <div className="p-4 bg-amber-500/10 border border-amber-500/30 rounded-xl text-xs text-amber-300 flex items-start space-x-2.5">
                <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5" />
                <div>
                  <div className="font-semibold">Prize Already Received</div>
                  <div className="mt-0.5 text-amber-400/90">
                    Per chit rules (Section 8.2), members who have already won the prize in this cycle cannot bid again. You will continue to earn dividends on monthly auctions!
                  </div>
                </div>
              </div>
            ) : currentAuction.status !== 'OPEN' ? (
              <div className="p-4 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-slate-300 flex items-start space-x-2.5">
                <Info className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
                <div>
                  <div className="font-semibold">Auction Not Open</div>
                  <div className="mt-0.5 text-slate-400">
                    Bidding is closed for this auction. Current status: <span className="text-white font-medium">{currentAuction.status}</span>.
                  </div>
                </div>
              </div>
            ) : (
              <form onSubmit={handlePlaceBid} className="space-y-4">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1.5">
                    Offered Winning Discount (₹)
                  </label>
                  <div className="relative">
                    <span className="absolute left-3.5 top-2.5 text-slate-400 font-bold">₹</span>
                    <input
                      type="number"
                      step="500"
                      min={minDiscount}
                      max={maxDiscount}
                      value={bidAmountInput}
                      onChange={(e) => setBidAmountInput(e.target.value)}
                      placeholder="e.g. 42000"
                      className="w-full bg-slate-800 border border-slate-700 rounded-xl pl-8 pr-4 py-2 text-white font-bold text-lg focus:outline-none focus:border-emerald-500"
                    />
                  </div>
                  <div className="flex items-center justify-between text-[11px] text-slate-400 mt-1.5">
                    <span>Min: {formatCurrency(minDiscount)} (5%)</span>
                    <span>Max: {formatCurrency(maxDiscount)} (40%)</span>
                  </div>
                </div>

                {/* Quick Selection Pills */}
                <div className="flex items-center gap-1.5">
                  <span className="text-[11px] text-slate-400">Quick:</span>
                  {[30000, 38000, 42000, 45000].map((amt) => (
                    <button
                      key={amt}
                      type="button"
                      onClick={() => setBidAmountInput(amt.toString())}
                      className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs rounded-lg border border-slate-700 font-medium"
                    >
                      ₹{(amt / 1000).toFixed(0)}k
                    </button>
                  ))}
                </div>

                {/* Live Math Projection */}
                {parseFloat(bidAmountInput) > 0 && (
                  <div className="bg-slate-800/60 p-3.5 rounded-xl border border-slate-700/60 space-y-1.5 text-xs">
                    <div className="flex justify-between text-slate-400">
                      <span>Prize Amount Won:</span>
                      <span className="font-bold text-white">
                        {formatCurrency(pool - (parseFloat(bidAmountInput) || 0))}
                      </span>
                    </div>
                    <div className="flex justify-between text-slate-400">
                      <span>Dividend Per Member:</span>
                      <span className="font-bold text-emerald-400">
                        {formatCurrency((parseFloat(bidAmountInput) || 0) / 20)}
                      </span>
                    </div>
                  </div>
                )}

                {errorMsg && (
                  <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl text-xs text-rose-300 font-medium">
                    {errorMsg}
                  </div>
                )}

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full py-2.5 bg-gradient-to-r from-emerald-500 to-teal-400 hover:from-emerald-400 hover:to-teal-300 text-slate-950 font-bold text-sm rounded-xl transition-all shadow-lg shadow-emerald-500/20"
                >
                  {isSubmitting ? 'Submitting Bid...' : 'Submit Discount Bid'}
                </button>
              </form>
            )}
          </div>

          {/* Section 8.3 Exact Formula Card */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 text-xs">
            <div className="flex items-center space-x-2 font-bold text-white mb-2">
              <Calculator className="w-4 h-4 text-emerald-400" />
              <span>ChitCircle Mathematical Formulation (Section 8.3)</span>
            </div>
            <div className="bg-slate-950/60 p-3 rounded-lg font-mono text-[11px] text-emerald-300 space-y-1 border border-slate-800">
              <div>Dividend Pool = Winning Discount (D) - Commission (C)</div>
              <div>Dividend / Member = Dividend Pool / 20 Members</div>
              <div>Winner Prize = Monthly Pool (P) - Winning Discount (D)</div>
            </div>
            <div className="mt-2 text-slate-400 text-[11px]">
              Engineered with Java 21 <code className="text-white">BigDecimal</code> arithmetic and half-up precision.
            </div>
          </div>
        </div>

        {/* Right Column (7 Cols): Live Bids Leaderboard & Financial Breakdown */}
        <div className="lg:col-span-7 space-y-6">
          {/* Winner Announcement Banner (if declared/settled) */}
          {(currentAuction.status === 'WINNER_DECLARED' || currentAuction.status === 'SETTLED') && (
            <div className="bg-gradient-to-r from-teal-950/60 to-emerald-950/60 border border-emerald-500/40 rounded-2xl p-5 shadow-xl flex items-center justify-between">
              <div className="flex items-center space-x-4">
                <div className="w-12 h-12 rounded-2xl bg-emerald-500 text-slate-950 flex items-center justify-center font-bold shadow-lg">
                  <Trophy className="w-6 h-6" />
                </div>
                <div>
                  <div className="text-xs uppercase font-extrabold tracking-wider text-emerald-400">
                    {currentAuction.status === 'SETTLED' ? 'Auction Settled' : 'Winner Declared'}
                  </div>
                  <div className="text-lg font-bold text-white">
                    {currentAuction.winnerMemberName} (Member #{currentAuction.winnerMemberNumber})
                  </div>
                  <div className="text-xs text-slate-300 mt-0.5">
                    Winning Discount: <span className="font-bold text-emerald-300">{formatCurrency(currentAuction.winningDiscount)}</span> • Payout: <span className="font-bold text-white">{formatCurrency(currentAuction.prizeAmount)}</span>
                  </div>
                </div>
              </div>

              <div className="text-right">
                <div className="text-xs text-slate-400">Dividend / Member</div>
                <div className="text-xl font-extrabold text-emerald-400">
                  {formatCurrency(currentAuction.dividendPerMember)}
                </div>
              </div>
            </div>
          )}

          {/* Bids Leaderboard */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-800">
              <div>
                <h2 className="text-base font-bold text-white">Bidding Room Leaderboard</h2>
                <p className="text-xs text-slate-400">
                  Ranked by discount offered (Highest Discount Wins, tie-breaker: Earliest Bid)
                </p>
              </div>
              <span className="text-xs bg-slate-800 text-slate-300 px-2.5 py-1 rounded-full font-semibold">
                {bids.length} Bids Submitted
              </span>
            </div>

            {sortedBids.length === 0 ? (
              <div className="text-center py-10 text-slate-400 text-sm">
                No bids submitted yet for this auction.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead>
                    <tr className="border-b border-slate-800 text-slate-400 uppercase text-[10px] tracking-wider">
                      <th className="py-2.5 px-3">Rank</th>
                      <th className="py-2.5 px-3">Member</th>
                      <th className="py-2.5 px-3 text-right">Discount Bid</th>
                      <th className="py-2.5 px-3 text-right">Prize Payout</th>
                      <th className="py-2.5 px-3 text-right">Dividend Share</th>
                      <th className="py-2.5 px-3 text-right">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60 font-medium">
                    {sortedBids.map((b, idx) => {
                      const isLeader = idx === 0;
                      return (
                        <tr
                          key={b.id}
                          className={`transition-colors ${
                            isLeader ? 'bg-emerald-500/10 hover:bg-emerald-500/15' : 'hover:bg-slate-800/40'
                          }`}
                        >
                          <td className="py-3 px-3">
                            <span
                              className={`w-6 h-6 rounded-full inline-flex items-center justify-center font-bold text-[11px] ${
                                isLeader
                                  ? 'bg-emerald-500 text-slate-950'
                                  : 'bg-slate-800 text-slate-400'
                              }`}
                            >
                              {idx + 1}
                            </span>
                          </td>
                          <td className="py-3 px-3">
                            <div className="font-semibold text-white">{b.memberName}</div>
                            <div className="text-[10px] text-slate-400">Member #{b.memberNumber}</div>
                          </td>
                          <td className="py-3 px-3 text-right font-bold text-emerald-400">
                            {formatCurrency(b.bidAmount)}
                          </td>
                          <td className="py-3 px-3 text-right text-slate-200">
                            {formatCurrency(pool - b.bidAmount)}
                          </td>
                          <td className="py-3 px-3 text-right text-teal-300 font-semibold">
                            {formatCurrency(b.bidAmount / 20)}
                          </td>
                          <td className="py-3 px-3 text-right">
                            {isLeader ? (
                              <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                                WINNING
                              </span>
                            ) : (
                              <span className="px-2 py-0.5 rounded text-[10px] text-slate-400 bg-slate-800">
                                OUTBID
                              </span>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};


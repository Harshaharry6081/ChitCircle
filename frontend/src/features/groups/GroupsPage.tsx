import React, { useState } from 'react';
import { useApp } from '../../app/store';
import { formatCurrency, formatDate } from '../../utils/formatters';
import {
  Users,
  Plus,
  ShieldCheck,
  CheckCircle2,
  Calendar,
  Layers,
  Search,
  UserPlus
} from 'lucide-react';

export const GroupsPage: React.FC = () => {
  const { activeGroup, groups, members, createGroup, addMember, currentRole } = useApp();
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showAddMemberModal, setShowAddMemberModal] = useState(false);

  // Create group form state
  const [groupName, setGroupName] = useState('');
  const [contribution, setContribution] = useState('10000');
  const [membersCount, setMembersCount] = useState('20');
  const [duration, setDuration] = useState('20');
  const [startDate, setStartDate] = useState('2026-03-01');

  // Add member form state
  const [newMemberName, setNewMemberName] = useState('');
  const [newMemberEmail, setNewMemberEmail] = useState('');
  const [newMemberPhone, setNewMemberPhone] = useState('');

  const filteredMembers = members.filter(
    (m) =>
      m.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      m.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      m.memberNumber.toString().includes(searchTerm)
  );

  const handleCreateGroup = async (e: React.FormEvent) => {
    e.preventDefault();
    await createGroup({
      name: groupName,
      monthlyContribution: parseFloat(contribution),
      totalMembers: parseInt(membersCount),
      durationMonths: parseInt(duration),
      startDate
    });
    setShowCreateModal(false);
    setGroupName('');
  };

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    await addMember({
      fullName: newMemberName,
      email: newMemberEmail,
      phoneNumber: newMemberPhone
    });
    setShowAddMemberModal(false);
    setNewMemberName('');
    setNewMemberEmail('');
    setNewMemberPhone('');
  };

  return (
    <div className="space-y-6">
      {/* Header and Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Chit Groups & Member Roster</h1>
          <p className="text-xs text-slate-400 mt-1">
            Manage your chit groups, track 20-member capacity, and monitor prize receipt eligibility.
          </p>
        </div>

        {currentRole === 'GROUP_ADMIN' && (
          <div className="flex items-center space-x-3">
            <button
              onClick={() => setShowAddMemberModal(true)}
              className="px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-white font-medium text-xs rounded-xl border border-slate-700 flex items-center space-x-1.5 transition-all"
            >
              <UserPlus className="w-3.5 h-3.5" />
              <span>Enroll Member</span>
            </button>
            <button
              onClick={() => setShowCreateModal(true)}
              className="px-3.5 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-xs rounded-xl flex items-center space-x-1.5 transition-all shadow-lg shadow-emerald-500/20"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>New Chit Group</span>
            </button>
          </div>
        )}
      </div>

      {/* Active Group Highlight Card */}
      {activeGroup && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
          <div className="flex flex-col md:flex-row md:items-center justify-between pb-4 border-b border-slate-800 gap-4">
            <div>
              <div className="text-xs font-semibold uppercase text-emerald-400 flex items-center space-x-1.5">
                <ShieldCheck className="w-3.5 h-3.5" />
                <span>Active Chit Circle</span>
              </div>
              <h2 className="text-xl font-bold text-white mt-0.5">{activeGroup.name}</h2>
              <div className="text-xs text-slate-400 mt-1 flex items-center space-x-4">
                <span>Created by: {activeGroup.createdByName || 'Admin'}</span>
                <span>•</span>
                <span>Start Date: {formatDate(activeGroup.startDate)}</span>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <span className="px-3 py-1 bg-emerald-500/15 text-emerald-300 border border-emerald-500/30 text-xs rounded-full font-bold">
                {activeGroup.status}
              </span>
            </div>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-4">
            <div className="p-3 bg-slate-800/50 rounded-xl border border-slate-800">
              <div className="text-xs text-slate-400">Total Group Value</div>
              <div className="text-base font-bold text-white mt-0.5">
                {formatCurrency(activeGroup.chitValue)}
              </div>
            </div>

            <div className="p-3 bg-slate-800/50 rounded-xl border border-slate-800">
              <div className="text-xs text-slate-400">Monthly Contribution</div>
              <div className="text-base font-bold text-emerald-400 mt-0.5">
                {formatCurrency(activeGroup.monthlyContribution)}
              </div>
            </div>

            <div className="p-3 bg-slate-800/50 rounded-xl border border-slate-800">
              <div className="text-xs text-slate-400">Monthly Pool (P)</div>
              <div className="text-base font-bold text-teal-300 mt-0.5">
                {formatCurrency(activeGroup.monthlyContribution * activeGroup.totalMembers)}
              </div>
            </div>

            <div className="p-3 bg-slate-800/50 rounded-xl border border-slate-800">
              <div className="text-xs text-slate-400">Duration & Members</div>
              <div className="text-base font-bold text-white mt-0.5">
                {activeGroup.durationMonths} Mos / {activeGroup.totalMembers} Members
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Member Roster Card */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4 pb-3 border-b border-slate-800">
          <div>
            <h3 className="text-base font-bold text-white flex items-center space-x-2">
              <Users className="w-4 h-4 text-emerald-400" />
              <span>Enrolled Members ({members.length} / {activeGroup?.totalMembers || 20})</span>
            </h3>
            <p className="text-xs text-slate-400 mt-0.5">
              Unique constraint: 1 member number per slot (1 to 20).
            </p>
          </div>

          <div className="relative w-full sm:w-64">
            <Search className="w-3.5 h-3.5 absolute left-3 top-3 text-slate-400" />
            <input
              type="text"
              placeholder="Search member by name..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full bg-slate-800 border border-slate-700 rounded-xl pl-9 pr-3 py-1.5 text-xs text-white focus:outline-none focus:border-emerald-500"
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 uppercase text-[10px] tracking-wider">
                <th className="py-2.5 px-3">#</th>
                <th className="py-2.5 px-3">Member Name</th>
                <th className="py-2.5 px-3">Email</th>
                <th className="py-2.5 px-3">Phone</th>
                <th className="py-2.5 px-3">Status</th>
                <th className="py-2.5 px-3 text-right">Prize Eligibility</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-medium">
              {filteredMembers.map((m) => (
                <tr key={m.id} className="hover:bg-slate-800/40 transition-colors">
                  <td className="py-3 px-3">
                    <span className="w-6 h-6 rounded-full bg-slate-800 text-slate-300 font-bold text-[11px] inline-flex items-center justify-center">
                      {m.memberNumber}
                    </span>
                  </td>
                  <td className="py-3 px-3">
                    <span className="font-semibold text-white">{m.fullName}</span>
                  </td>
                  <td className="py-3 px-3 text-slate-400">{m.email}</td>
                  <td className="py-3 px-3 text-slate-400">{m.phoneNumber || '—'}</td>
                  <td className="py-3 px-3">
                    <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-500/20 text-emerald-300">
                      {m.status}
                    </span>
                  </td>
                  <td className="py-3 px-3 text-right">
                    {m.prizeReceived ? (
                      <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30">
                        PRIZE WON & PAID
                      </span>
                    ) : (
                      <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                        ELIGIBLE TO BID
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create Group Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <h3 className="text-lg font-bold text-white mb-1">Create New Chit Group</h3>
            <p className="text-xs text-slate-400 mb-4">
              Configure group tenure, monthly contribution and target member roster.
            </p>

            <form onSubmit={handleCreateGroup} className="space-y-4 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">Group Name</label>
                <input
                  type="text"
                  required
                  value={groupName}
                  onChange={(e) => setGroupName(e.target.value)}
                  placeholder="e.g. ChitCircle Family 2026"
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Monthly Contribution (₹)</label>
                  <input
                    type="number"
                    required
                    value={contribution}
                    onChange={(e) => setContribution(e.target.value)}
                    className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-emerald-500 font-bold"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Total Members</label>
                  <input
                    type="number"
                    required
                    value={membersCount}
                    onChange={(e) => setMembersCount(e.target.value)}
                    className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-emerald-500 font-bold"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Duration (Months)</label>
                  <input
                    type="number"
                    required
                    value={duration}
                    onChange={(e) => setDuration(e.target.value)}
                    className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Start Date</label>
                  <input
                    type="date"
                    required
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-emerald-500"
                  />
                </div>
              </div>

              <div className="pt-2 flex justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold rounded-xl shadow-lg"
                >
                  Create Group
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Member Modal */}
      {showAddMemberModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <h3 className="text-lg font-bold text-white mb-1">Enroll Group Member</h3>
            <p className="text-xs text-slate-400 mb-4">
              Add a trusted member to slot #{members.length + 1}.
            </p>

            <form onSubmit={handleAddMember} className="space-y-4 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">Full Name</label>
                <input
                  type="text"
                  required
                  value={newMemberName}
                  onChange={(e) => setNewMemberName(e.target.value)}
                  placeholder="e.g. Ramesh Chandra"
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Email Address</label>
                <input
                  type="email"
                  required
                  value={newMemberEmail}
                  onChange={(e) => setNewMemberEmail(e.target.value)}
                  placeholder="ramesh@example.com"
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Phone Number</label>
                <input
                  type="text"
                  value={newMemberPhone}
                  onChange={(e) => setNewMemberPhone(e.target.value)}
                  placeholder="+919876543210"
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="pt-2 flex justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setShowAddMemberModal(false)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold rounded-xl shadow-lg"
                >
                  Save Member
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};


import React from 'react';
import { useApp } from '../app/store';
import { Users, Shield, UserCircle, Bell, ChevronDown } from 'lucide-react';

interface NavbarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
}

export const Navbar: React.FC<NavbarProps> = ({ activeTab, setActiveTab }) => {
  const { currentUser, currentRole, switchRole, activeGroup, groups, setActiveGroup } = useApp();

  return (
    <header className="bg-slate-900 border-b border-slate-800 text-white sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand */}
        <div className="flex items-center space-x-3 cursor-pointer" onClick={() => setActiveTab('dashboard')}>
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center font-bold text-slate-950 text-xl shadow-lg shadow-emerald-500/20">
            CC
          </div>
          <div>
            <div className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-white to-slate-300 bg-clip-text text-transparent">
              ChitCircle
            </div>
            <div className="text-[10px] text-emerald-400 uppercase tracking-widest font-semibold">
              Chit Group Platform
            </div>
          </div>
        </div>

        {/* Group Selector */}
        <div className="hidden md:flex items-center space-x-2 bg-slate-800/80 px-3 py-1.5 rounded-lg border border-slate-700">
          <Users className="w-4 h-4 text-emerald-400" />
          <span className="text-xs text-slate-400">Group:</span>
          <select
            className="bg-transparent text-xs font-medium text-white focus:outline-none cursor-pointer"
            value={activeGroup?.id || ''}
            onChange={(e) => {
              const selected = groups.find((g) => g.id === e.target.value);
              if (selected) setActiveGroup(selected);
            }}
          >
            {groups.map((g) => (
              <option key={g.id} value={g.id} className="bg-slate-800 text-white">
                {g.name}
              </option>
            ))}
          </select>
        </div>

        {/* Role Switcher & User Profile */}
        <div className="flex items-center space-x-4">
          {/* Quick Role Switcher */}
          <div className="flex items-center bg-slate-800 p-1 rounded-lg border border-slate-700 text-xs">
            <button
              onClick={() => switchRole('GROUP_ADMIN')}
              className={`px-3 py-1 rounded-md transition-all font-medium flex items-center space-x-1.5 ${
                currentRole === 'GROUP_ADMIN'
                  ? 'bg-emerald-500 text-slate-950 shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Shield className="w-3.5 h-3.5" />
              <span>Admin</span>
            </button>
            <button
              onClick={() => switchRole('GROUP_MEMBER')}
              className={`px-3 py-1 rounded-md transition-all font-medium flex items-center space-x-1.5 ${
                currentRole === 'GROUP_MEMBER'
                  ? 'bg-teal-500 text-slate-950 shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <UserCircle className="w-3.5 h-3.5" />
              <span>Member</span>
            </button>
          </div>

          {/* Current User Pill */}
          <div className="flex items-center space-x-2.5 pl-2 border-l border-slate-800">
            <div className="w-8 h-8 rounded-full bg-slate-800 border border-emerald-500/30 flex items-center justify-center text-xs font-semibold text-emerald-400">
              {currentUser.fullName.charAt(0)}
            </div>
            <div className="hidden sm:block text-left">
              <div className="text-xs font-semibold text-white leading-tight">{currentUser.fullName}</div>
              <div className="text-[10px] text-slate-400">{currentUser.email}</div>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};


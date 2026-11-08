import React from 'react';
import { useApp } from '../app/store';
import {
  LayoutDashboard,
  Users,
  Gavel,
  CreditCard,
  BookOpen,
  FileText
} from 'lucide-react';

interface SidebarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ activeTab, setActiveTab }) => {
  const { currentRole } = useApp();

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'groups', label: 'Groups & Members', icon: Users },
    { id: 'auctions', label: 'Auction & Bidding', icon: Gavel },
    { id: 'payments', label: 'Payments & Dues', icon: CreditCard },
    { id: 'ledger', label: 'Financial Ledger', icon: BookOpen },
    { id: 'reports', label: 'Reports & Summary', icon: FileText }
  ];

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col justify-between p-4 min-h-[calc(100vh-4rem)]">
      <div className="space-y-6">
        <div className="px-3">
          <div className="text-[11px] font-bold uppercase tracking-wider text-slate-500">
            {currentRole === 'GROUP_ADMIN' ? 'Administrator Workspace' : 'Member Portal'}
          </div>
        </div>

        <nav className="space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? 'text-emerald-400' : 'text-slate-400'}`} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>
      </div>

      {/* Role Context Card */}
      <div className="bg-slate-800/50 rounded-xl p-3 border border-slate-800 text-xs">
        <div className="flex items-center justify-between text-slate-400 mb-1">
          <span>Active Role:</span>
          <span className="font-semibold text-emerald-400">
            {currentRole === 'GROUP_ADMIN' ? 'Group Admin' : 'Chit Member'}
          </span>
        </div>
        <p className="text-[11px] text-slate-400 leading-relaxed">
          {currentRole === 'GROUP_ADMIN'
            ? 'Full administrative control: create auctions, record payments, declare winners & settle.'
            : 'Enrolled member view: place discount bids, track dividends, and inspect personal ledger.'}
        </p>
      </div>
    </aside>
  );
};


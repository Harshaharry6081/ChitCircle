import React, { useState } from 'react';
import { AppProvider } from './store';
import { Navbar } from '../layouts/Navbar';
import { Sidebar } from '../layouts/Sidebar';
import { DashboardPage } from '../features/dashboard/DashboardPage';
import { GroupsPage } from '../features/groups/GroupsPage';
import { AuctionConsolePage } from '../features/auctions/AuctionConsolePage';
import { PaymentsPage } from '../features/payments/PaymentsPage';
import { LedgerPage } from '../features/ledger/LedgerPage';
import { ReportsPage } from '../features/reports/ReportsPage';

const AppContent: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('dashboard');

  const renderActiveView = () => {
    switch (activeTab) {
      case 'dashboard':
        return <DashboardPage setActiveTab={setActiveTab} />;
      case 'groups':
        return <GroupsPage />;
      case 'auctions':
        return <AuctionConsolePage />;
      case 'payments':
        return <PaymentsPage />;
      case 'ledger':
        return <LedgerPage />;
      case 'reports':
        return <ReportsPage />;
      default:
        return <DashboardPage setActiveTab={setActiveTab} />;
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-emerald-500 selection:text-slate-950">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="flex-1 flex flex-col md:flex-row max-w-7xl w-full mx-auto">
        <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />
        <main className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto">
          {renderActiveView()}
        </main>
      </div>
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
};

export default App;


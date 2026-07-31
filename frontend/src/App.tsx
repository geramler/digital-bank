import { Routes, Route } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import Dashboard from './pages/Dashboard'
import CustomerManagement from './pages/CustomerManagement'
import AccountManagement from './pages/AccountManagement'
import TransactionManagement from './pages/TransactionManagement'
import Navigation from './components/Navigation'

function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation />
      <main className="container mx-auto px-4 py-8">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/customers" element={<CustomerManagement />} />
          <Route path="/accounts" element={<AccountManagement />} />
          <Route path="/transactions" element={<TransactionManagement />} />
        </Routes>
      </main>
      <Toaster position="top-right" />
    </div>
  )
}

export default App
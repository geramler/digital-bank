import { useState, useEffect } from 'react'
import { Building, Users, CreditCard, TrendingUp } from 'lucide-react'
import { apiService } from '../services/api'

const Dashboard = () => {
  const [serviceStatus, setServiceStatus] = useState({
    customer: false,
    account: false,
    transaction: false,
    auth: false
  })

  useEffect(() => {
    const checkServices = async () => {
      const customerStatus = await apiService.checkCustomerServiceHealth()
      const accountStatus = await apiService.checkAccountServiceHealth()
      const transactionStatus = await apiService.checkTransactionServiceHealth()
      const authStatus = await apiService.checkAuthServiceHealth()

      setServiceStatus({
        customer: customerStatus,
        account: accountStatus,
        transaction: transactionStatus,
        auth: authStatus
      })
    }

    checkServices()
  }, [])

  const statusCards = [
    {
      title: 'Customer Service',
      status: serviceStatus.customer,
      icon: Users,
      description: 'Manages customer registration and profiles'
    },
    {
      title: 'Account Service',
      status: serviceStatus.account,
      icon: CreditCard,
      description: 'Handles account creation and balance management'
    },
    {
      title: 'Transaction Service',
      status: serviceStatus.transaction,
      icon: TrendingUp,
      description: 'Processes deposits and withdrawals'
    },
    {
      title: 'Auth Service',
      status: serviceStatus.auth,
      icon: Building,
      description: 'Manages authentication and authorization'
    }
  ]

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Digital Bank Dashboard</h1>
        <p className="text-gray-600 mt-2">Monitor your banking services and operations</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statusCards.map((card) => {
          const Icon = card.icon
          return (
            <div key={card.title} className="card">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className={`p-2 rounded-lg ${
                    card.status ? 'bg-success-100 text-success-600' : 'bg-danger-100 text-danger-600'
                  }`}>
                    <Icon className="h-6 w-6" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">{card.title}</h3>
                    <p className="text-sm text-gray-600">{card.description}</p>
                  </div>
                </div>
                <div className={`px-2 py-1 rounded-full text-xs font-medium ${
                  card.status ? 'bg-success-100 text-success-800' : 'bg-danger-100 text-danger-800'
                }`}>
                  {card.status ? 'Online' : 'Offline'}
                </div>
              </div>
            </div>
          )
        })}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="card">
          <h2 className="card-header">Quick Actions</h2>
          <div className="space-y-4">
            <button className="w-full btn-primary">
              Create New Customer
            </button>
            <button className="w-full btn-secondary">
              Open New Account
            </button>
            <button className="w-full btn-secondary">
              Process Transaction
            </button>
          </div>
        </div>

        <div className="card">
          <h2 className="card-header">System Information</h2>
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-gray-600">Environment:</span>
              <span className="font-medium">{import.meta.env.VITE_ENV}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">API Base URL:</span>
              <span className="font-medium text-sm">{import.meta.env.VITE_API_BASE_URL}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Services Online:</span>
              <span className="font-medium">
                {Object.values(serviceStatus).filter(Boolean).length} / 4
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Dashboard
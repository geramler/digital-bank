import { Link, useLocation } from 'react-router-dom'
import { Building, Users, CreditCard, TrendingUp } from 'lucide-react'

const Navigation = () => {
  const location = useLocation()

  const navItems = [
    { path: '/', label: 'Dashboard', icon: Building },
    { path: '/customers', label: 'Customers', icon: Users },
    { path: '/accounts', label: 'Accounts', icon: CreditCard },
    { path: '/transactions', label: 'Transactions', icon: TrendingUp },
  ]

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center space-x-8">
            <div className="flex items-center space-x-2">
              <Building className="h-8 w-8 text-primary-600" />
              <span className="text-xl font-bold text-gray-900">Digital Bank</span>
            </div>
            <div className="hidden md:flex space-x-1">
              {navItems.map((item) => {
                const Icon = item.icon
                const isActive = location.pathname === item.path
                return (
                  <Link
                    key={item.path}
                    to={item.path}
                    className={`flex items-center px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                      isActive
                        ? 'bg-primary-100 text-primary-700'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                    }`}
                  >
                    <Icon className="h-4 w-4 mr-2" />
                    {item.label}
                  </Link>
                )
              })}
            </div>
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="text-sm text-gray-500">
              Environment: {import.meta.env.VITE_ENV}
            </div>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Navigation
import { 
  CreateCustomerRequest, 
  Customer, 
  CreateAccountRequest, 
  Account, 
  CreateTransactionRequest, 
  Transaction 
} from '../types/api';

class ApiService {
  private baseURL = '';

  constructor() {
    this.baseURL = import.meta.env.VITE_API_BASE_URL || '';
  }

  private async request(url: string, options: RequestInit = {}): Promise<any> {
    const fullUrl = `${this.baseURL}${url}`;
    const response = await fetch(fullUrl, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
  }

  private async post(url: string, data: any): Promise<any> {
    return this.request(url, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  private async get(url: string): Promise<any> {
    return this.request(url, { method: 'GET' });
  }

  // Customer Service Methods
  async createCustomer(request: CreateCustomerRequest): Promise<Customer> {
    return this.post('/api/customers', request);
  }

  // Account Service Methods
  async createAccount(request: CreateAccountRequest): Promise<Account> {
    return this.post('/api/accounts', request);
  }

  async getAccountBalance(accountId: number): Promise<{ accountId: number; balance: string }> {
    return this.get(`/api/accounts/${accountId}/balance`);
  }

  // Transaction Service Methods
  async createTransaction(request: CreateTransactionRequest): Promise<Transaction> {
    return this.post('/api/transactions', request);
  }

  async getTransaction(transactionId: number): Promise<Transaction> {
    return this.get(`/api/transactions/${transactionId}`);
  }

  // Health check methods
  async checkCustomerServiceHealth(): Promise<boolean> {
    try {
      const response = await fetch('/api/customers/health');
      return response.status === 200;
    } catch {
      return false;
    }
  }

  async checkAccountServiceHealth(): Promise<boolean> {
    try {
      const response = await fetch('/api/accounts/health');
      return response.status === 200;
    } catch {
      return false;
    }
  }

  async checkTransactionServiceHealth(): Promise<boolean> {
    try {
      const response = await fetch('/api/transactions/health');
      return response.status === 200;
    } catch {
      return false;
    }
  }

  async checkAuthServiceHealth(): Promise<boolean> {
    try {
      const response = await fetch('/api/auth/health');
      return response.status === 200;
    } catch {
      return false;
    }
  }
}

export const apiService = new ApiService();
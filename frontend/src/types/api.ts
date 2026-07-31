// Customer types
export interface CreateCustomerRequest {
  name: string;
  email: string;
}

export interface Customer {
  id: number;
  name: string;
  email: string;
  createdAt: string;
}

// Account types
export interface CreateAccountRequest {
  customerId: number;
  accountType?: string;
  initialBalance?: number;
  customerEmail?: string;
}

export interface Account {
  id: number;
  customerId: number;
  accountType: string;
  balance: string;
  createdAt: string;
}

// Transaction types
export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL';

export interface CreateTransactionRequest {
  accountId: number;
  type: TransactionType;
  amount: number;
  accountOwnerEmail?: string;
}

export interface Transaction {
  id: number;
  accountId: number;
  type: TransactionType;
  amount: string;
  status: string;
  createdAt: string;
}

// API Response types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface ApiError {
  message: string;
  status: number;
  details?: any;
}
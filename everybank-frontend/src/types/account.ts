export interface Account {
    accountId: number;
    accountName: string;
    balance: number;
    bank: string;
    productName: string;
    accountType: 'CHECK' | 'DEPOSIT' | 'SAVING';
    startDate: string;
    endDate?: string;
    interestRate?: number;
    status: 'active' | 'matured' | 'closed';
}

export interface TransactionHistory {
    id: number;
    date: string;
    description: string;
    amount: number;
    balance: number;
    type: 'deposit' | 'withdrawal' | 'transfer';
}

export interface AccountItemProps {
    account: Account;
    sectionType: 'CHECK' | 'DEPOSIT' | 'SAVING';
}

export interface AccountSectionProps {
    title: string;
    accounts: Account[];
    sectionType: 'CHECK' | 'DEPOSIT' | 'SAVING';
}

export interface TransferModalProps {
    isOpen: boolean;
    onClose: () => void;
    account: {
        accountNumber: string;
        accountName: string;
        balance: number;
    };
}


export interface AccountDetail {
    id: number;
    accountName: string;
    bank: string;
    accountType: string;
    interestRate: number;
    monthlyPayment: number;
    paymentCount: number;
    startDate: string;
    endDate: string;
    currentBalance: number;
    expectedInterest: number;
    expectedMaturityAmount: number;
}

export interface Transaction {
    id: number;
    date: string;
    description: string;
    amount: number;
    balance: number;
    paymentNumber: number;
}
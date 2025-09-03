export interface Account {
    accountId: number;
    accountName: string;
    balance: number;
    bank: string;
    productName: string;
    accountType: 'CHECK' | 'DEPOSIT' | 'SAVING';
    startDate: string;
    endDate?: string;
    status: 'active' | 'matured' | 'closed';
    monthlyPayment?: number | null; // 적금의 월납입액
    currentPaymentCount?: number | null; // 현재 납입 회차
    totalPaymentCount?: number | null; // 총 납입 회차
    nextPaymentDate?: string | null; // 다음 납입일
    totalDepositAmount?: number | null; // 예금의 총 예치금액
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

export interface AccountTransactionModalProps {
    isOpen: boolean;
    onClose: () => void;
    accountId: number;
    accountName: string;
    bank: string;
    currentBalance: number;
    isMatured: boolean;
    monthlyPayment?: number | null; // 적금의 월납입액
    transactionType: 'deposit' | 'withdraw' | 'payment' | 'refund';
    onSuccess: () => void;
}
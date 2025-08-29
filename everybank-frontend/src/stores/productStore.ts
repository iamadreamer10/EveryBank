import { create } from 'zustand';

interface SelectedOption {
    optionId: number;
    interestRateTypeName: string;
    interestRate: number;
    saveTerm: number;
}

interface ProductState {
    productCode: string | null;
    productName: string;
    bankName: string;
    productType: 'deposits' | 'savings' | null;
    amount: number;
    selectedOption: SelectedOption | null;

    // 상태 세팅 함수
    setProductInfo: (info: {
        productCode: string;
        productName: string;
        bankName: string;
        productType: 'deposits' | 'savings';
    }) => void;

    setAmount: (amount: number) => void;

    setSelectedOption: (option: SelectedOption) => void;

    reset: () => void;
}

export const useProductStore = create<ProductState>((set) => ({
    productCode: null,
    productName: '',
    bankName: '',
    productType: null,
    amount: 0,
    selectedOption: null,

    setProductInfo: (info) =>
        set((state) => ({
            ...state,
            productCode: info.productCode,
            productName: info.productName,
            bankName: info.bankName,
            productType: info.productType,
        })),

    setAmount: (amount) =>
        set((state) => ({
            ...state,
            amount,
        })),

    setSelectedOption: (option) =>
        set((state) => ({
            ...state,
            selectedOption: option,
        })),

    reset: () =>
        set(() => ({
            productCode: null,
            productName: '',
            bankName: '',
            productType: null,
            amount: 0,
            selectedOption: null,
        })),
}));

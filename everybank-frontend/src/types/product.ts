import type {ContractOption} from "./contract.ts";

export interface Product {
    productCode: string;
    bank: string;
    name: string;
    member: string;
    maxLimit: string | number;
    rate: number;
}

export interface ProductDetail {
    productCode: string;
    bank: string;
    name: string;
    member: string;
    maxLimit: string | number;
    period: number[];
    option: ProductOption[] | SavingOption[]
}


export interface ProductOption {
    id: number;
    saveTerm: number;
    interestRateType: string;
    interestRateTypeName: string;
    interestRate: number;
    interestRate2: number;
}

export interface SavingOption extends ProductOption {
    reverseType: string;
    reverseTypeName: string;
}

export interface Contract {
    contractId: number;
    userId: number;
    nickname: string;
    productCode: string;
    productName: string;
    companyName: string;
    totalAmount: number;
    contractDate: string;  // YYYY-MM-DD
    maturityDate: string;  // YYYY-MM-DD
    option: ProductOption | SavingOption;
}
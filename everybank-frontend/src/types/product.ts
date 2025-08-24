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
    period: string;
    option: ProductOption[] | SavingOption[]
}

export interface ProductOption {
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
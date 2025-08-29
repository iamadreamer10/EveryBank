export interface ContractOption {
    id: number;
    interestRateType: string;
    interestRateTypeName: string;
    saveTerm: number;
    interestRate: number;
    interestRate2: number;
}


export interface ContractDetail {
    nickname: string;
    productName: string;
    bankName: string;
    productType: string;
    interestType: string;
    interestRate: number;
    contractDate: string;
    maturityDate: string;
    saveTerm: number;
    amount: number;
}
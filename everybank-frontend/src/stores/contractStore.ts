import { create } from "zustand";

interface ContractState {
    checkMaturity: (endDate: string | null | undefined) => boolean;
}

export const useContractStore = create<ContractState>(() => ({
    checkMaturity: (endDate) => {
        if (!endDate) return false;
        try {
            const today = new Date();
            const end = new Date(endDate);
            return end <= today;
        } catch {
            return false;
        }
    },
}));
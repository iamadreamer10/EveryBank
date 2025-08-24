interface BankFilterProps {
    banks: string[];
    selectedBank: string;
    onBankChange: (bank: string) => void;
}

export default function BankFilter({banks, selectedBank, onBankChange}: BankFilterProps) {
    return (
        <div className="mb-6">
            <div className="flex flex-wrap gap-2">
                {banks.map((bank) => (
                    <button
                        key={bank}
                        className={`px-4 py-2 rounded-full text-sm font-medium border ${
                            selectedBank === bank
                                ? 'bg-bank-primary text-white border-bank-primary'
                                : 'bg-white text-gray-700 border-gray-300 hover:bg-bank-light'
                        }`}
                        onClick={() => onBankChange(bank)}
                    >
                        {bank}
                    </button>
                ))}
            </div>
        </div>
    );
}

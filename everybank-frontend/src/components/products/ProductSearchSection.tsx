interface ProductSearchSectionProps {
    selectedAmount: string;
    selectedPeriod: string;
    onAmountChange: (amount: string) => void;
    onPeriodChange: (period: string) => void;
    onBankCompare: (bank: string) => void;
}

export default function ProductSearchSection({
                                                 selectedAmount,
                                                 selectedPeriod,
                                                 onAmountChange,
                                                 onPeriodChange,
                                                 onBankCompare
                                             }: ProductSearchSectionProps) {
    return (
        <div className="bg-white rounded-lg shadow-sm border p-6 mb-8">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-end">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">월납입액:</label>
                    <input
                        type="text"
                        value={selectedAmount + ' (원)'}
                        onChange={(e) => onAmountChange(e.target.value.replace(/[^0-9]/g, ''))}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">개월수:</label>
                    <input
                        type="text"
                        value={selectedPeriod}
                        onChange={(e) => onPeriodChange(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                    />
                </div>
                <div className="flex gap-3">
                    <button
                        className="px-6 py-2 bg-bank-primary text-white rounded-md hover:bg-bank-dark"
                        onClick={() => onBankCompare('우리은행')}
                    >
                        우리은행
                    </button>
                    <button
                        className="px-6 py-2 bg-bank-accent text-white rounded-md hover:bg-bank-primary"
                        onClick={() => onBankCompare('신한은행')}
                    >
                        신한은행
                    </button>
                    <button
                        className="px-6 py-2 bg-bank-accent text-white rounded-md hover:bg-bank-primary"
                        onClick={() => onBankCompare('상세비교')}
                    >
                        상세비교
                    </button>
                </div>
            </div>
        </div>
    );
}

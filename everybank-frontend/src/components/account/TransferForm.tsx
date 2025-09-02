import {useState} from "react";

interface TransferFormProps {
    fromAccount: {
        accountNumber: string;
        accountName: string;
        balance: number;
    } | null;
    onSubmit: (data: TransferData) => void;
    onCancel: () => void;
}

interface TransferData {
    toAccount: string;
    amount: number;
    memo: string;
}

export function TransferForm({ fromAccount, onSubmit, onCancel }: TransferFormProps) {
    const [formData, setFormData] = useState({
        toAccount: '',
        amount: '',
        memo: ''
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        if (!formData.toAccount || !formData.amount) {
            alert('계좌번호와 이체금액을 입력해주세요.');
            return;
        }

        if (Number(formData.amount) > (fromAccount?.balance || 0)) {
            alert('잔액이 부족합니다.');
            return;
        }

        onSubmit({
            toAccount: formData.toAccount,
            amount: Number(formData.amount),
            memo: formData.memo
        });
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    return (
        <div>
            {/* 출금 계좌 정보 */}
            <div className="mb-6 p-4 bg-bank-light rounded-md">
                <h4 className="font-medium mb-2">출금계좌</h4>
                <p className="text-sm text-gray-600">{fromAccount?.accountNumber}</p>
                <p className="font-medium">{fromAccount?.accountName}</p>
                <p className="text-sm">
                    잔액: <span className="font-medium">{fromAccount?.balance.toLocaleString()}원</span>
                </p>
            </div>

            <form onSubmit={handleSubmit}>
                <div className="space-y-4">
                    {/* 받는 계좌 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            받는 계좌번호
                        </label>
                        <input
                            type="text"
                            name="toAccount"
                            value={formData.toAccount}
                            onChange={handleInputChange}
                            placeholder="000-000-000000"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                            required
                        />
                    </div>

                    {/* 이체 금액 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            이체금액
                        </label>
                        <input
                            type="number"
                            name="amount"
                            value={formData.amount}
                            onChange={handleInputChange}
                            placeholder="이체할 금액을 입력하세요"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                            required
                        />
                    </div>

                    {/* 메모 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            메모 (선택사항)
                        </label>
                        <input
                            type="text"
                            name="memo"
                            value={formData.memo}
                            onChange={handleInputChange}
                            placeholder="메모를 입력하세요"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                        />
                    </div>
                </div>

                <div className="flex gap-3 mt-6">
                    <button
                        type="button"
                        onClick={onCancel}
                        className="flex-1 px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                    >
                        취소
                    </button>
                    <button
                        type="submit"
                        className="flex-1 px-4 py-2 bg-bank-primary text-white rounded-md hover:bg-bank-dark"
                    >
                        이체하기
                    </button>
                </div>
            </form>
        </div>
    );
};
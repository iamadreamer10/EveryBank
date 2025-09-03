import {useState, useEffect} from 'react';
import {useNavigate} from "react-router-dom";
import type {AccountTransactionModalProps} from "../../types/account.ts";


// 통합 API 호출 함수
const executeTransaction = async (
    transactionType: 'deposit' | 'withdraw' | 'payment',
    accountId: number,
    amount: number
) => {
    const endpoints = {
        deposit: 'my_account/deposit',
        withdraw: 'my_account/withdraw',
        payment: 'my_account/payment',
    };

    // API별 body 구조
    const getRequestBody = () => {
        switch (transactionType) {
            case 'deposit':
            case 'withdraw':
                return {amount};
            case 'payment':
                return {toAccountId: accountId, amount};
            default:
                return {amount};
        }
    };

    const requestbody = getRequestBody();

    const response = await fetch(`http://localhost:8080/${endpoints[transactionType]}`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestbody)
    });

    if (!response.ok) throw new Error(`${transactionType} 실패`);
    return response.json();
};

// 입출금계좌 잔액 확인 함수
const fetchCheckingAccountBalance = async (): Promise<number> => {
    const response = await fetch('http://localhost:8080/my_account/check_balance', {
        headers: {
            'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) throw new Error('입출금계좌 조회 실패');
    const data = await response.json();
    return data.result.currentBalance;
};

export default function AccountTransactionModal({
                                                    isOpen,
                                                    onClose,
                                                    accountId,
                                                    accountName,
                                                    bank,
                                                    currentBalance,
                                                    isMatured,
                                                    monthlyPayment,
                                                    transactionType,
                                                    onSuccess
                                                }: AccountTransactionModalProps) {
    const [amount, setAmount] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isRefund, setIsRefund] = useState(false);
    const [checkingBalance, setCheckingBalance] = useState<number | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        if (isOpen && transactionType === 'refund') {
            setAmount(currentBalance.toString());
            setIsRefund(true);
        } else if (isOpen && transactionType === 'payment' && monthlyPayment) {
            // 적금 납입 시 월납입액으로 고정
            setAmount(monthlyPayment.toString());


            // 입출금계좌 잔액 조회
            fetchCheckingAccountBalance()
                .then(setCheckingBalance)
                .catch(console.error);
        } else if (isOpen) {
            setAmount('');
            setCheckingBalance(null);
        }
    }, [isOpen, transactionType, currentBalance, monthlyPayment]);

    // 거래 타입별 설정
    const configs = {
        deposit: {title: '입금', color: 'bg-bank-success hover:bg-green-600', needsBalanceCheck: false},
        withdraw: {title: '출금', color: 'bg-gray-600 hover:bg-gray-700', needsBalanceCheck: true},
        payment: {title: '납입', color: 'bg-bank-success hover:bg-green-600', needsBalanceCheck: false},
        refund: {title: '환급', color: 'bg-red-500 hover:bg-red-600', needsBalanceCheck: true}
    };

    const config = configs[transactionType];

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const amountValue = Number(amount);

        if (!amount || amountValue <= 0) {
            alert('올바른 금액을 입력해주세요.');
            return;
        }

        if (config.needsBalanceCheck && amountValue > currentBalance) {
            alert('잔액이 부족합니다.');
            return;
        }

        // 적금 납입 시 입출금계좌 잔액 확인
        if (transactionType === 'payment') {
            if (checkingBalance === null) {
                alert('입출금계좌 정보를 확인할 수 없습니다.');
                return;
            }
            if (checkingBalance < amountValue) {
                alert(`입출금계좌 잔액이 부족합니다. (보유: ${checkingBalance.toLocaleString()}원)`);
                return;
            }
        }

        if (transactionType === 'refund') {
            const actionText = isMatured ? '환급' : '중도해지';
            if (!window.confirm(`정말 ${amountValue.toLocaleString()}원을 ${actionText}하시겠습니까?`)) {
                return;
            }
        }

        setIsSubmitting(true);
        try {
            if (transactionType === 'refund') {
                navigate(`/contract/maturity/${accountId}`, {state: {accountId, isMatured}});
                return;
            }

            await executeTransaction(transactionType, accountId, amountValue);
            alert(`${config.title}이 완료되었습니다.`);
            setAmount('');
            onClose();
            onSuccess();
        } catch (error) {
            alert(`${config.title} 중 오류가 발생했습니다.`);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
             onClick={(e) => e.target === e.currentTarget && onClose()}>
            <div className="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
                <div className="flex items-center justify-between p-6 border-b">
                    <h3 className="text-lg font-semibold">{config.title}</h3>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600">×</button>
                </div>

                <div className="p-6">
                    <div className="mb-6 p-4 bg-bank-light rounded-md">
                        <h4 className="font-medium mb-2">{bank} | {accountName}</h4>
                        <p className="text-sm">현재 잔액: <span
                            className="font-medium text-bank-primary">{currentBalance.toLocaleString()}원</span></p>

                        {/* 적금 납입 시 입출금계좌 잔액 표시 */}
                        {transactionType === 'payment' && checkingBalance !== null && (
                            <p className="text-sm text-blue-600 mt-1">
                                입출금계좌 잔액: <span className="font-medium">{checkingBalance.toLocaleString()}원</span>
                            </p>
                        )}

                        {/* 적금 납입 시 월납입액 안내 */}
                        {transactionType === 'payment' && monthlyPayment && (
                            <p className="text-sm text-purple-600 mt-2">
                                월납입액: <span className="font-medium">{monthlyPayment.toLocaleString()}원</span>
                                <span className="text-xs text-gray-500 block mt-1">
                                    (계약 시 정해진 고정 금액입니다)
                                </span>
                            </p>
                        )}
                    </div>

                    <form onSubmit={handleSubmit}>
                        <input
                            type="number"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            placeholder={`${config.title}할 금액을 입력하세요`}
                            className="w-full px-3 py-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                            disabled={isSubmitting || isRefund || (transactionType === 'payment')}
                            readOnly={isRefund || (transactionType === 'payment')}
                            required
                        />

                        {/* 적금 납입 시 추가 안내 메시지 */}
                        {transactionType === 'payment' && (
                            <div className="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded">
                                <p className="text-sm text-yellow-800">
                                    💡 적금 납입액은 계약 시 정해진 금액으로 고정됩니다.
                                    입출금계좌에서 자동으로 차감됩니다.
                                </p>
                            </div>
                        )}

                        <div className="flex gap-3">
                            <button
                                type="button"
                                onClick={onClose}
                                className="flex-1 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
                                disabled={isSubmitting}
                            >
                                취소
                            </button>
                            <button
                                type="submit"
                                className={`flex-1 px-4 py-2 text-white rounded-md ${config.color} disabled:opacity-50`}
                                disabled={isSubmitting || (transactionType === 'payment' && (checkingBalance === null || checkingBalance < Number(amount)))}
                            >
                                {isSubmitting ? '처리중...' : `${config.title}하기`}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
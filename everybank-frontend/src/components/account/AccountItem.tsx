import { useState } from 'react';
import { Link } from 'react-router-dom';
import type { Account } from "../../types/account.ts";
import AccountTransactionModal from './AccountTransactionModal';
import {useContractStore} from "../../stores/contractStore.ts";

interface AccountItemProps {
    account: Account;
    sectionType: 'CHECK' | 'DEPOSIT' | 'SAVING';
    onAccountUpdated?: () => void;
}

export default function AccountItem({ account, sectionType, onAccountUpdated }: AccountItemProps) {
    const [modalState, setModalState] = useState<{
        isOpen: boolean;
        transactionType: 'deposit' | 'withdraw' | 'payment' | 'refund' | null;
    }>({ isOpen: false, transactionType: null });

    const formatBalance = (balance: number | undefined | null): string => {
        return (balance ?? 0).toLocaleString();
    };

    const formatDate = (dateString: string | undefined | null): string => {
        if (!dateString) return '정보 없음';
        try {
            return new Date(dateString).toLocaleDateString('ko-KR');
        } catch {
            return '정보 없음';
        }
    };

    const { checkMaturity } = useContractStore();
    const matured = checkMaturity(account.endDate);

    const openModal = (transactionType: 'deposit' | 'withdraw' | 'payment' | 'refund') => {
        setModalState({ isOpen: true, transactionType });
    };

    const closeModal = () => {
        setModalState({ isOpen: false, transactionType: null });
    };

    // 계좌 타입별 색상과 버튼
    const typeConfig = {
        CHECK: { color: 'bg-blue-100 text-blue-800', label: '입출금', buttons: ['deposit', 'withdraw'] },
        DEPOSIT: { color: 'bg-green-100 text-green-800', label: '예금', buttons: ['refund'] },
        SAVING: { color: 'bg-purple-100 text-purple-800', label: '적금', buttons: ['payment', 'refund'] }
    };

    const config = typeConfig[sectionType];

    return (
        <>
            <div className="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow">
                <div className="flex items-start justify-between">
                    <div className="flex-1">
                        <div className="flex items-center gap-3 mb-4">
                            <div className={`px-3 py-1 rounded-full text-xs font-medium ${config.color}`}>
                                {config.label}
                            </div>
                            <div className="text-sm text-gray-500">
                                {account.bank} | {account.productName}
                            </div>
                        </div>

                        <div className="mb-4">
                            <div className="text-sm text-gray-600 mb-1">현재 잔액</div>
                            <div className="text-2xl font-bold text-bank-primary">
                                {formatBalance(account.balance)}원
                            </div>
                            {/* 만기 상태 표시 */}
                            {account.endDate && (sectionType === 'DEPOSIT' || sectionType === 'SAVING') && (
                                <div className={`mt-2 px-2 py-1 rounded text-xs font-medium inline-block ${
                                    matured
                                        ? 'bg-green-100 text-green-800'
                                        : 'bg-yellow-100 text-yellow-800'
                                }`}>
                                    {matured ? '만기도달' : '만기전'}
                                </div>
                            )}
                        </div>

                        <div className="grid grid-cols-2 gap-4 text-sm">
                            <div>
                                <span className="text-gray-600">시작일:</span>
                                <span className="ml-2 font-medium">{formatDate(account.startDate)}</span>
                            </div>
                            {account.endDate && (
                                <div>
                                    <span className="text-gray-600">만기일:</span>
                                    <span className="ml-2 font-medium">{formatDate(account.endDate)}</span>
                                </div>
                            )}
                        </div>

                        {/* 적금인 경우 월납입액 표시 */}
                        {sectionType === 'SAVING' && account.monthlyPayment && (
                            <div className="mt-3 text-sm">
                                <span className="text-gray-600">월납입액:</span>
                                <span className="ml-2 font-medium text-purple-600">
                                    {account.monthlyPayment.toLocaleString()}원
                                </span>
                            </div>
                        )}
                    </div>

                    <div className="flex flex-col gap-2 ml-4">
                        <Link
                            to={`/my_account/${account.accountId}`}
                            className="px-4 py-2 text-sm bg-bank-primary text-white rounded-md hover:bg-bank-dark transition-colors text-center"
                        >
                            상세보기
                        </Link>

                        {config.buttons.map((buttonType) => (
                            <button
                                key={buttonType}
                                onClick={() => openModal(buttonType as any)}
                                className={`px-4 py-2 text-sm rounded-md transition-colors ${
                                    buttonType === 'deposit' || buttonType === 'payment'
                                        ? 'bg-bank-success text-white hover:bg-green-600'
                                        : buttonType === 'withdraw'
                                            ? 'border border-gray-300 text-gray-700 hover:bg-gray-50'
                                            : 'bg-red-500 text-white hover:bg-red-600'
                                }`}
                            >
                                {buttonType === 'deposit' ? '입금' :
                                    buttonType === 'withdraw' ? '출금' :
                                        buttonType === 'payment' ?
                                            `납입${account.monthlyPayment ? ` (${account.monthlyPayment.toLocaleString()}원)` : ''}` :
                                            matured ? '환급' : '중도해지' }
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            {modalState.transactionType && (
                <AccountTransactionModal
                    isOpen={modalState.isOpen}
                    onClose={closeModal}
                    accountId={account.accountId}
                    accountName={account.productName}
                    bank={account.bank}
                    currentBalance={account.balance}
                    transactionType={modalState.transactionType}
                    endDate={account.endDate}
                    isMatured={matured}
                    monthlyPayment={account.monthlyPayment} // 추가
                    onSuccess={() => {
                        onAccountUpdated?.();
                    }}
                />
            )}
        </>
    );
}
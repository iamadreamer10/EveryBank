import type {TransferModalProps} from "../../types/account.ts";
import {useState} from "react";
import Modal from "../common/Modal.tsx";

export default function TransferModal({ isOpen, onClose, account }: TransferModalProps) {
    const [transferData, setTransferData] = useState({
        toAccount: '',
        amount: '',
        memo: ''
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        console.log('이체 데이터:', transferData);
        onClose();
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setTransferData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="계좌이체">
            <div>
                {/* 출금 계좌 정보 */}
                <div className="mb-6 p-4 bg-bank-light rounded-md">
                    <h4 className="font-medium mb-2">출금계좌</h4>
                    <p className="text-sm text-gray-600">{account.accountNumber}</p>
                    <p className="font-medium">{account.accountName}</p>
                    <p className="text-sm">
                        잔액: <span className="font-medium">{account.balance.toLocaleString()}원</span>
                    </p>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                받는 계좌번호
                            </label>
                            <input
                                type="text"
                                name="toAccount"
                                value={transferData.toAccount}
                                onChange={handleInputChange}
                                placeholder="000-000-000000"
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                이체금액
                            </label>
                            <input
                                type="number"
                                name="amount"
                                value={transferData.amount}
                                onChange={handleInputChange}
                                placeholder="이체할 금액을 입력하세요"
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                메모
                            </label>
                            <input
                                type="text"
                                name="memo"
                                value={transferData.memo}
                                onChange={handleInputChange}
                                placeholder="메모를 입력하세요"
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                            />
                        </div>
                    </div>

                    <div className="flex gap-3 mt-6">
                        <button
                            type="button"
                            onClick={onClose}
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
        </Modal>
    );
};

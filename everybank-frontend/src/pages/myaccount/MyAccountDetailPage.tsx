import {Link, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import type {AccountDetail, Transaction} from "../../types/account.ts";

export default function MyAccountDetailPage() {

    const { accountId } = useParams<{ accountId: string }>();
    const [accountDetail, setAccountDetail] = useState<AccountDetail | null>(null);
    const [transactions, setTransactions] = useState<Transaction[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (accountId) {
            fetchAccountDetail(accountId);
        }
    }, [accountId]);

    const fetchAccountDetail = async (id: string) => {
        try {
            const token = sessionStorage.getItem('accessToken');

            // 계좌 상세 정보 조회
            const response = await fetch(`http://localhost:8080/my_account/${id}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) throw new Error('계좌 상세 조회 실패');

            // 임시 mock 데이터
            const mockDetail: AccountDetail = {
                id: Number(id),
                accountName: '영플러스적금',
                bank: '아이엠뱅크',
                accountType: 'SAVING',
                interestRate: 3.25,
                monthlyPayment: 500000,
                paymentCount: 8,
                startDate: '2025.01.11',
                endDate: '2027.01.11',
                currentBalance: 4000000,
                expectedInterest: 552618,
                expectedMaturityAmount: 12552618
            };

            const mockTransactions: Transaction[] = [
                {
                    id: 1,
                    date: '2025.08.14-18:24:33',
                    description: '8회차 | 25년 08월분',
                    amount: 500000,
                    balance: 4000000,
                    paymentNumber: 8
                },
                {
                    id: 2,
                    date: '2025.07.11-10:55:11',
                    description: '7회차 | 25년 07월분',
                    amount: 500000,
                    balance: 3500000,
                    paymentNumber: 7
                }
            ];

            setAccountDetail(mockDetail);
            setTransactions(mockTransactions);

        } catch (error) {
            console.error('계좌 상세 조회 실패:', error);
            setError('계좌 정보를 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleTerminate = () => {
        if (window.confirm('정말 해지하시겠습니까?')) {
            // 해지 API 호출
            console.log('계좌 해지');
        }
    };

    const handleDeposit = () => {
        // 입금 모달 또는 페이지로 이동
        console.log('입금하기');
    };

    if (loading) {
        return <div className="max-w-7xl mx-auto px-4 py-12">로딩 중...</div>;
    }

    if (error || !accountDetail) {
        return <div className="max-w-7xl mx-auto px-4 py-12 text-red-500">에러: {error}</div>;
    }

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            {/* 뒤로가기 */}
            <div className="mb-6">
                <Link
                    to="/my_account"
                    className="text-bank-primary hover:text-bank-dark font-medium"
                >
                    ← 내계좌로 돌아가기
                </Link>
            </div>

            <h1 className="text-3xl font-bold text-gray-900 mb-8">상품안내</h1>

            {/* 계좌 상세 정보 카드 */}
            <div className="bg-white rounded-lg border border-gray-200 p-8 mb-8">
                <div className="flex justify-between items-start mb-6">
                    <h2 className="text-2xl font-bold text-bank-primary">{accountDetail.accountName}</h2>
                    <h3 className="text-xl font-semibold text-gray-700">{accountDetail.bank}</h3>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
                    <div className="space-y-3">
                        <div className="flex justify-between">
                            <span className="text-gray-600">가입조건</span>
                            <span className="font-medium">단리 / {accountDetail.interestRate}%</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-600">월납입액</span>
                            <span className="font-medium">{accountDetail.monthlyPayment.toLocaleString()}원</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-600">진행회차</span>
                            <span className="font-medium">{accountDetail.paymentCount}회</span>
                        </div>
                    </div>

                    <div className="space-y-3">
                        <div className="flex justify-between">
                            <span className="text-gray-600">가입일</span>
                            <span className="font-medium">{accountDetail.startDate}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-600">만기일</span>
                            <span className="font-medium">{accountDetail.endDate}</span>
                        </div>
                    </div>
                </div>

                <div className="text-right">
                    <p className="text-3xl font-bold text-bank-primary">
                        {accountDetail.currentBalance.toLocaleString()}원
                    </p>
                </div>
            </div>

            {/* 예상 수익 정보 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
                <div className="bg-white rounded-lg border border-gray-200 p-6">
                    <h3 className="text-lg font-semibold mb-4">예상수익</h3>
                    <p className="text-2xl font-bold text-bank-primary">
                        {accountDetail.expectedInterest.toLocaleString()}원
                    </p>
                </div>

                <div className="bg-white rounded-lg border border-gray-200 p-6">
                    <h3 className="text-lg font-semibold mb-4">만기예상액</h3>
                    <p className="text-2xl font-bold text-bank-primary">
                        {accountDetail.expectedMaturityAmount.toLocaleString()}원
                    </p>
                </div>
            </div>

            {/* 액션 버튼 */}
            <div className="flex justify-center gap-4 mb-8">
                <button
                    onClick={handleTerminate}
                    className="px-8 py-3 bg-red-500 text-white rounded-md hover:bg-red-600 font-medium"
                >
                    해지하기
                </button>
                <button
                    onClick={handleDeposit}
                    className="px-8 py-3 bg-bank-success text-white rounded-md hover:bg-bank-dark font-medium"
                >
                    입금하기
                </button>
            </div>

            {/* 거래내역 */}
            <div className="bg-white rounded-lg border border-gray-200">
                <div className="p-6 border-b">
                    <h3 className="text-xl font-semibold">거래내역</h3>
                </div>

                <div className="divide-y divide-gray-200">
                    {transactions.map((transaction) => (
                        <div key={transaction.id} className="p-6">
                            <div className="flex justify-between items-center">
                                <div>
                                    <p className="text-sm text-bank-primary font-medium mb-1">
                                        {transaction.date}
                                    </p>
                                    <p className="font-medium text-gray-900">
                                        {transaction.description}
                                    </p>
                                </div>
                                <div className="text-right">
                                    <p className="text-xl font-bold text-bank-primary mb-1">
                                        +{transaction.amount.toLocaleString()}원
                                    </p>
                                    <p className="text-sm text-gray-600">
                                        잔액 {transaction.balance.toLocaleString()}원
                                    </p>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                {transactions.length === 0 && (
                    <div className="p-8 text-center text-gray-500">
                        거래내역이 없습니다.
                    </div>
                )}

                {transactions.length > 0 && (
                    <div className="p-4 text-center border-t">
                        <button className="text-bank-primary hover:text-bank-dark font-medium">
                            더보기
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};
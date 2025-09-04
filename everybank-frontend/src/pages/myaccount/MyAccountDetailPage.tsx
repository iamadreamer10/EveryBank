import { Link, useParams } from "react-router-dom";
import { useEffect, useState } from "react";

// API 응답 타입 정의
interface ContractInfo {
    contractId: number | null;
    productCode: string;
    productName: string;
    bank: string;
    contractType: string;
    interestRate: number;
    interestRateType: string;
    monthlyPayment: number | null;
    totalAmount: number | null;
    term: number | null;
    startDate: string;
    endDate: string;
    contractStatus: string;
}

interface AccountInfo {
    accountId: number;
    accountNumber: string;
    currentBalance: number;
    paymentCount: number | null;
    lastTransactionDate: string;
}

interface ExpectedAmounts {
    totalPayment: number;
    expectedInterest: number;
    maturityAmount: number;
}

interface Transaction {
    id: number;
    date: string;
    description: string;
    amount: number;
    balance: number;
}

interface Pagination {
    currentPage: number;
    totalPages: number;
    totalCount: number;
    hasNext: boolean;
}

interface ContractDetailResponse {
    status: number;
    message: string;
    result: {
        contractInfo: ContractInfo;
        accountInfo: AccountInfo;
        expectedAmounts: ExpectedAmounts;
        transactions: Transaction[];
        pagination: Pagination;
    };
}

export default function MyAccountDetailPage() {
    const { accountId } = useParams<{ accountId: string }>();
    const [contractData, setContractData] = useState<ContractDetailResponse['result'] | null>(null);
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
            const response = await fetch(`http://localhost:8080/contract/${id}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) throw new Error('계좌 상세 조회 실패');

            const data: ContractDetailResponse = await response.json();
            console.log("응답 데이터:", data);

            setContractData(data.result);
            setError(null);

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

    // 날짜 포맷팅
    const formatDate = (dateString: string): string => {
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('ko-KR');
        } catch (error) {
            return dateString;
        }
    };

    // 계약 상태 한글 변환
    const getContractStatusText = (status: string): string => {
        switch (status) {
            case 'ACTIVE':
                return '정상';
            case 'CLOSED':
                return '해지';
            case 'MATURED':
                return '만기';
            default:
                return status;
        }
    };

    // 계약 타입별 표시명
    const getContractTypeText = (type: string): string => {
        switch (type) {
            case 'CHECKING':
                return '입출금계좌';
            case 'DEPOSIT':
                return '정기예금';
            case 'SAVING':
                return '적금';
            default:
                return type;
        }
    };

    if (loading) {
        return (
            <div className="max-w-7xl mx-auto px-4 py-12">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-bank-primary mx-auto mb-4"></div>
                    <p className="text-gray-600">계좌 정보를 불러오고 있습니다...</p>
                </div>
            </div>
        );
    }

    if (error || !contractData) {
        return (
            <div className="max-w-7xl mx-auto px-4 py-12">
                <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
                    <div className="text-red-600 mb-2">⚠️ 오류가 발생했습니다</div>
                    <p className="text-red-800">{error}</p>
                    <button
                        onClick={() => window.location.reload()}
                        className="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition-colors"
                    >
                        다시 시도
                    </button>
                </div>
            </div>
        );
    }

    const { contractInfo, accountInfo, expectedAmounts, transactions } = contractData;

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

            <h1 className="text-3xl font-bold text-gray-900 mb-8">계좌 상세정보</h1>

            {/* 계좌 상세 정보 카드 */}
            <div className="bg-white rounded-lg border border-gray-200 p-8 mb-8">
                <div className="flex justify-between items-start mb-6">
                    <div>
                        <h2 className="text-2xl font-bold text-bank-primary mb-2">
                            {contractInfo.productName}
                        </h2>
                        <div className="flex items-center gap-3">
                            <span className="text-lg font-semibold text-gray-700">
                                {contractInfo.bank}
                            </span>
                            <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm">
                                {getContractTypeText(contractInfo.contractType)}
                            </span>
                            <span className={`px-3 py-1 rounded-full text-sm ${
                                contractInfo.contractStatus === 'ACTIVE'
                                    ? 'bg-green-100 text-green-800'
                                    : 'bg-gray-100 text-gray-800'
                            }`}>
                                {getContractStatusText(contractInfo.contractStatus)}
                            </span>
                        </div>
                    </div>
                    <div className="text-right">
                        <p className="text-sm text-gray-600 mb-1">계좌번호</p>
                        <p className="text-lg font-mono font-bold text-gray-900">
                            {accountInfo.accountNumber}
                        </p>
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
                    <div className="space-y-3">
                        {contractInfo.interestRate > 0 && (
                            <div className="flex justify-between">
                                <span className="text-gray-600">금리</span>
                                <span className="font-medium">
                                    {contractInfo.interestRateType} / {contractInfo.interestRate}%
                                </span>
                            </div>
                        )}

                        {contractInfo.monthlyPayment && (
                            <div className="flex justify-between">
                                <span className="text-gray-600">월납입액</span>
                                <span className="font-medium">
                                    {contractInfo.monthlyPayment.toLocaleString()}원
                                </span>
                            </div>
                        )}

                        {contractInfo.totalAmount !== null && (
                            <div className="flex justify-between">
                                <span className="text-gray-600">가입금액</span>
                                <span className="font-medium">
                                    {contractInfo.totalAmount.toLocaleString()}원
                                </span>
                            </div>
                        )}

                        {accountInfo.paymentCount !== null && (
                            <div className="flex justify-between">
                                <span className="text-gray-600">진행회차</span>
                                <span className="font-medium">{accountInfo.paymentCount}회</span>
                            </div>
                        )}

                        {contractInfo.term && (
                            <div className="flex justify-between">
                                <span className="text-gray-600">계약기간</span>
                                <span className="font-medium">{contractInfo.term}개월</span>
                            </div>
                        )}
                    </div>

                    <div className="space-y-3">
                        <div className="flex justify-between">
                            <span className="text-gray-600">가입일</span>
                            <span className="font-medium">{formatDate(contractInfo.startDate)}</span>
                        </div>

                        {contractInfo.contractType !== 'CHECKING' && (
                            <div className="flex justify-between">
                                <span className="text-gray-600">만기일</span>
                                <span className="font-medium">{formatDate(contractInfo.endDate)}</span>
                            </div>
                        )}

                        <div className="flex justify-between">
                            <span className="text-gray-600">최종거래일</span>
                            <span className="font-medium">
                                {formatDate(accountInfo.lastTransactionDate)}
                            </span>
                        </div>
                    </div>
                </div>

                <div className="text-center border-t pt-6">
                    <p className="text-sm text-gray-600 mb-2">현재 잔액</p>
                    <p className="text-4xl font-bold text-bank-primary">
                        {accountInfo.currentBalance.toLocaleString()}원
                    </p>
                </div>
            </div>

            {/* 예상 수익 정보 (예금/적금인 경우에만) */}
            {contractInfo.contractType !== 'CHECKING' && expectedAmounts.maturityAmount > 0 && (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    <div className="bg-white rounded-lg border border-gray-200 p-6 text-center">
                        <h3 className="text-sm text-gray-600 mb-2">총 납입예정액</h3>
                        <p className="text-xl font-bold text-gray-900">
                            {expectedAmounts.totalPayment.toLocaleString()}원
                        </p>
                    </div>

                    <div className="bg-white rounded-lg border border-gray-200 p-6 text-center">
                        <h3 className="text-sm text-gray-600 mb-2">예상 수익</h3>
                        <p className="text-xl font-bold text-bank-success">
                            {expectedAmounts.expectedInterest.toLocaleString()}원
                        </p>
                    </div>

                    <div className="bg-white rounded-lg border border-gray-200 p-6 text-center">
                        <h3 className="text-sm text-gray-600 mb-2">만기 예상액</h3>
                        <p className="text-xl font-bold text-bank-primary">
                            {expectedAmounts.maturityAmount.toLocaleString()}원
                        </p>
                    </div>
                </div>
            )}

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
                                        {formatDate(transaction.date)}
                                    </p>
                                    <p className="font-medium text-gray-900">
                                        {transaction.description}
                                    </p>
                                </div>
                                <div className="text-right">
                                    <p className={`text-xl font-bold mb-1 ${
                                        transaction.amount > 0 ? 'text-bank-success' : 'text-red-500'
                                    }`}>
                                        {transaction.amount > 0 ? '+' : ''}{transaction.amount.toLocaleString()}원
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

                {transactions.length > 0 && contractData.pagination.hasNext && (
                    <div className="p-4 text-center border-t">
                        <button
                            className="text-bank-primary hover:text-bank-dark font-medium"
                            onClick={() => {
                                // TODO: 더 많은 거래내역 로드
                                console.log('더 많은 거래내역 로드');
                            }}
                        >
                            더보기 ({contractData.pagination.totalCount - transactions.length}개 더 있음)
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}
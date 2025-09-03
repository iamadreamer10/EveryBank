import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';

interface RefundData {
    // 계좌 기본 정보
    accountId: number;
    accountType: string;   // enum → string
    productName: string;
    companyName: string;

    // 계약 정보
    contractDate: string;
    maturityDate: string;
    saveTerm: number;
    interestRate: number;
    interestRate2: number;
    interestRateTypeName: string;

    // 적금 전용 정보
    monthlyPayment: number | null;
    totalPaymentCount: number | null;
    currentPaymentCount: number | null;

    // 만기정산 계산 결과
    totalPrincipal: number;
    totalInterest: number;
    totalPayout: number;

    // 현재 상태
    currentCheckAmount: number;
    isMatured: boolean;
}

// 환급 정보 조회 API
const fetchRefundData = async (accountId: string): Promise<RefundData> => {
    const response = await fetch(`http://localhost:8080/contract/maturity/${accountId}`, {
        headers: {
            'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) throw new Error('환급 정보 조회 실패');

    const data = await response.json();
    return data.result as RefundData;
};

// 환급 실행 API
const executeRefund = async (accountId: number, amount: number) => {
    const response = await fetch(`http://localhost:8080/my_account/refund`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            fromAccountId: accountId,
            amount: amount
        })
    });

    if (!response.ok) throw new Error('환급 실행 실패');
    return response.json();
};

export default function RefundConfirmationPage() {
    const { accountId } = useParams<{ accountId: string }>();
    const navigate = useNavigate();
    const [refundData, setRefundData] = useState<RefundData | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);

    useEffect(() => {
        if (accountId) {
            loadRefundData(accountId);
        }
    }, [accountId]);

    const loadRefundData = async (id: string) => {
        try {
            setLoading(true);
            const data = await fetchRefundData(id);
            console.log(data);
            setRefundData(data);
            setError(null);
        } catch (error) {
            console.error('환급 정보 조회 실패:', error);
            setError('환급 정보를 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleRefund = async () => {
        if (!refundData) return;

        const actionText = refundData.isMatured ? '만기환급' : '중도해지';

        if (!window.confirm(`정말 ${actionText}하시겠습니까?\n환급액: ${refundData.totalPayout.toLocaleString()}원`)) {
            return;
        }

        setIsProcessing(true);

        try {
            await executeRefund(refundData.accountId, refundData.totalPayout);
            alert('환급이 완료되었습니다.');
            navigate('/my_account');
        } catch (error) {
            console.error('환급 실행 실패:', error);
            alert('환급 처리 중 오류가 발생했습니다.');
        } finally {
            setIsProcessing(false);
        }
    };

    const formatDate = (dateString: string): string => {
        try {
            return new Date(dateString).toLocaleDateString('ko-KR').replace(/\./g, '. ').replace(/\s+$/, '');
        } catch {
            return dateString;
        }
    };

    if (loading) {
        return (
            <div className="max-w-4xl mx-auto px-4 py-12">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-bank-primary mx-auto mb-4"></div>
                    <p className="text-gray-600">환급 정보를 계산하고 있습니다...</p>
                </div>
            </div>
        );
    }

    if (error || !refundData) {
        return (
            <div className="max-w-4xl mx-auto px-4 py-12">
                <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
                    <div className="text-red-600 mb-2">⚠️ 오류가 발생했습니다</div>
                    <p className="text-red-800">{error}</p>
                    <Link
                        to="/my_account"
                        className="mt-4 inline-block px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition-colors"
                    >
                        내 계좌로 돌아가기
                    </Link>
                </div>
            </div>
        );
    }

    const isMatured = refundData.isMatured;

    return (
        <div className="max-w-4xl mx-auto px-4 py-8">
            {/* 뒤로가기 */}
            <div className="mb-6">
                <Link
                    to="/my_account"
                    className="text-bank-primary hover:text-bank-dark font-medium"
                >
                    ← 내계좌로 돌아가기
                </Link>
            </div>

            <div className="text-center mb-8">
                <h1 className="text-2xl font-bold text-gray-900 mb-2">
                    {isMatured ? '만기정산' : '중도해지'}
                </h1>
            </div>

            {/* 상품 정보 카드 */}
            <div className="bg-white rounded-lg border-2 border-bank-primary p-6 mb-8">
                <div className="flex justify-between items-start mb-6">
                    <h2 className="text-xl font-bold text-bank-primary">{refundData.productName}</h2>
                    <h3 className="text-lg font-semibold text-bank-primary">{refundData.companyName}</h3>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm mb-6">
                    <div className="space-y-2">
                        <div className="flex justify-between">
                            <span className="text-gray-600">금리유형</span>
                            <span className="font-medium">{refundData.interestRateTypeName}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-600">기본금리</span>
                            <span className="font-medium">{refundData.interestRate}%</span>
                        </div>
                        {refundData.monthlyPayment && (
                            <div className="flex justify-between">
                                <span className="text-gray-600">월납입액</span>
                                <span className="font-medium">{refundData.monthlyPayment.toLocaleString()}원</span>
                            </div>
                        )}
                        {refundData.currentPaymentCount && (
                            <div className="flex justify-between">
                                <span className="text-gray-600">진행회차</span>
                                <span className="font-medium">{refundData.currentPaymentCount}회</span>
                            </div>
                        )}
                    </div>

                    <div className="space-y-2">
                        <div className="flex justify-between">
                            <span className="text-gray-600">가입일</span>
                            <span className="font-medium">{formatDate(refundData.contractDate)}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-600">만기일</span>
                            <span className="font-medium">{formatDate(refundData.maturityDate)}</span>
                        </div>
                    </div>
                </div>

                <div className="text-right">
                    <p className="text-2xl font-bold text-bank-primary">
                        {refundData.currentCheckAmount.toLocaleString()}원
                    </p>
                </div>
            </div>

            {/* 축하 메시지 (만기인 경우만) */}
            {isMatured && (
                <div className="text-center mb-8">
                    <div className="text-4xl mb-2">🎉</div>
                    <h2 className="text-xl font-bold text-bank-primary mb-2">
                        {refundData.saveTerm}개월 동안 정말 수고하셨습니다!
                    </h2>
                    <p className="text-gray-600">꾸준한 노력이 만들어낸 소중한 결실이네요.</p>
                </div>
            )}

            {/* 환급 계산 정보 */}
            <div className="space-y-4 mb-8">
                <div className="flex justify-between items-center py-3">
                    <span className="text-bank-primary font-medium">원금:</span>
                    <span className="font-bold">{refundData.totalPrincipal.toLocaleString()}원</span>
                </div>

                <div className="flex justify-between items-center py-3">
                    <span className="text-bank-primary font-medium">이자:</span>
                    <span className="font-bold">{refundData.totalInterest.toLocaleString()}원</span>
                </div>

                <hr className="border-gray-300" />

                <div className="flex justify-between items-center py-4">
                    <span className="text-xl font-bold text-bank-primary">지급 금액:</span>
                    <span className="text-2xl font-bold text-bank-primary">
                        {refundData.totalPayout.toLocaleString()}원
                    </span>
                </div>
            </div>

            {/* 액션 버튼 */}
            <div className="flex justify-center gap-4">
                <Link
                    to="/my_account"
                    className="px-8 py-3 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 font-medium"
                >
                    취소
                </Link>
                <button
                    onClick={handleRefund}
                    disabled={isProcessing}
                    className="px-8 py-3 bg-bank-success text-white rounded-md hover:bg-bank-dark font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    {isProcessing ? '처리중...' : (isMatured ? '만기환급' : '중도해지')}
                </button>
            </div>

            {/* 주의사항 */}
            {!isMatured && (
                <div className="mt-8 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
                    <p className="text-sm text-yellow-800">
                        ⚠️ 해당 상품을 다시 가입하고 싶으시다면?
                    </p>
                    <p className="text-xs text-yellow-700 mt-1">
                        중도해지 후에는 동일 조건으로 재가입이 불가능할 수 있습니다.
                    </p>
                </div>
            )}
        </div>
    );
}

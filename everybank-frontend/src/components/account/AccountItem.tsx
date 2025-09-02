import type { Account } from "../../types/account.ts";

interface AccountItemProps {
    account: Account;
    sectionType: 'CHECK' | 'DEPOSIT' | 'SAVING';
}

export default function AccountItem({ account, sectionType }: AccountItemProps) {
    // 안전한 숫자 포맷팅 함수
    const formatBalance = (balance: number | undefined | null): string => {
        if (balance === undefined || balance === null) {
            return '0';
        }
        return balance.toLocaleString();
    };

    // 날짜 포맷팅 함수
    const formatDate = (dateString: string | undefined | null): string => {
        if (!dateString) {
            return '정보 없음';
        }
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('ko-KR');
        } catch (error) {
            console.error('날짜 파싱 오류:', error);
            return '정보 없음';
        }
    };

    // 계좌 상태 한글 변환
    const getAccountStatusText = (status: string | undefined): string => {
        switch (status) {
            case 'ACTIVE':
                return '정상';
            case 'INACTIVE':
                return '정지';
            case 'CLOSED':
                return '해지';
            default:
                return '알 수 없음';
        }
    };

    // 계좌 타입별 색상
    const getTypeColor = (type: string) => {
        switch (sectionType) {
            case 'CHECK':
                return 'bg-blue-100 text-blue-800';
            case 'DEPOSIT':
                return 'bg-green-100 text-green-800';
            case 'SAVING':
                return 'bg-purple-100 text-purple-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    };

    console.log('🏦 AccountItem 렌더링:', account); // 디버깅용

    return (
        <div className="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow">
            <div className="flex items-start justify-between">
                <div className="flex-1">
                    {/* 계좌 기본 정보 */}
                    <div className="flex items-center gap-3 mb-4">
                        <div className={`px-3 py-1 rounded-full text-xs font-medium ${getTypeColor(account.accountType)}`}>
                            {account.accountType || '타입 없음'}
                        </div>
                        <div className="text-sm text-gray-500">
                            {account.bank + ' | ' + account.productName  || '정보 없음'}
                        </div>
                    </div>

                    {/* 잔액 정보 */}
                    <div className="mb-4">
                        <div className="text-sm text-gray-600 mb-1">현재 잔액</div>
                        <div className="text-2xl font-bold text-bank-primary">
                            {formatBalance(account.balance)}원
                        </div>
                    </div>

                    {/* 계좌 상세 정보 */}
                    <div className="grid grid-cols-2 gap-4 text-sm">
                        <div>
                            <span className="text-gray-600">시작일:</span>
                            <span className="ml-2 font-medium">
                                {formatDate(account.startDate)}
                            </span>
                        </div>

                        {account.endDate && (
                            <div>
                                <span className="text-gray-600">만기일:</span>
                                <span className="ml-2 font-medium">
                                    {formatDate(account.endDate)}
                                </span>
                            </div>
                        )}
                    </div>
                </div>

                {/* 액션 버튼들 */}
                <div className="flex flex-col gap-2 ml-4">
                    <button className="px-4 py-2 text-sm bg-bank-primary text-white rounded-md hover:bg-bank-dark transition-colors">
                        상세보기
                    </button>
                    <button className="px-4 py-2 text-sm border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors">
                        거래내역
                    </button>
                </div>
            </div>
        </div>
    );
}
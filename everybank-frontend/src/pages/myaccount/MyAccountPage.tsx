import { useState, useEffect } from 'react';
import AccountSection from "../../components/account/AccountSection.tsx";
import type { Account } from "../../types/account.ts";

// API 응답 타입 정의
interface ApiResponse {
    result: {
        count: number;
        accountList: Account[];
    };
    status: number;
    message: string;
}

async function fetchAccounts(): Promise<ApiResponse> {
    const response = await fetch(`http://localhost:8080/my_account`, {
        headers: {
            'Authorization': `Bearer ${sessionStorage.getItem("accessToken")}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: 계좌 조회 실패`);
    }

    return response.json();
}

export default function MyAccountPage() {
    const [accounts, setAccounts] = useState<Account[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const loadAccounts = async () => {
            try {
                setLoading(true);
                setError(null);

                const data = await fetchAccounts();
                setAccounts(data.result.accountList);
            } catch (err) {
                const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다';
                setError(errorMessage);
                console.error('계좌 조회 오류:', err);
            } finally {
                setLoading(false);
            }
        };

        loadAccounts();
    }, []);

    // 로딩 상태
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

    // 에러 상태
    if (error) {
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

    // 계좌 타입별 분류
    const checkingAccounts = accounts.filter(acc => acc.accountType === 'CHECK');
    const depositAccounts = accounts.filter(acc => acc.accountType === 'DEPOSIT');
    const savingsAccounts = accounts.filter(acc => acc.accountType === 'SAVING');

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            {/* 헤더 */}
            <div className="mb-8">
                <h1 className="text-3xl font-bold text-bank-primary mb-2">
                    반갑습니다! 돈쭐노우버바우미님
                </h1>
                <p className="text-gray-600">
                    총 {accounts.length}개의 계좌가 있습니다
                </p>
            </div>

            {/* 계좌가 없는 경우 */}
            {accounts.length === 0 ? (
                <div className="bg-gray-50 border border-gray-200 rounded-lg p-8 text-center">
                    <div className="text-gray-500 mb-4">📊</div>
                    <h3 className="text-lg font-medium text-gray-900 mb-2">
                        등록된 계좌가 없습니다
                    </h3>
                    <p className="text-gray-600 mb-4">
                        새로운 금융상품에 가입하여 계좌를 만들어보세요
                    </p>
                    <button className="px-4 py-2 bg-bank-primary text-white rounded hover:bg-bank-primary-dark transition-colors">
                        상품 둘러보기
                    </button>
                </div>
            ) : (
                <>
                    {/* 입출금계좌 섹션 */}
                    <AccountSection
                        title="입출금계좌"
                        accounts={checkingAccounts}
                        sectionType="CHECK"
                    />

                    {/* 예금 섹션 */}
                    <AccountSection
                        title="예금"
                        accounts={depositAccounts}
                        sectionType="DEPOSIT"
                    />

                    {/* 적금 섹션 */}
                    <AccountSection
                        title="적금"
                        accounts={savingsAccounts}
                        sectionType="SAVING"
                    />
                </>
            )}
        </div>
    )
}
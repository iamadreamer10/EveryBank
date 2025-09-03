import { useState, useEffect } from 'react';
import AccountSection from "../../components/account/AccountSection.tsx";
import AccountModal from "../../components/account/AccountModal.tsx";
import type { Account } from "../../types/account.ts";
import { useQueryClient } from '@tanstack/react-query';
import {useUserStore} from "../../stores/userStore.ts";
import {useContractStore} from "../../stores/contractStore.ts";

// API 응답 타입 정의
interface ApiResponse {
    result: {
        count: number;
        accountList: Account[];
    };
    status: number;
    message: string;
}

interface Company {
    companyCode: string;
    companyName: string;
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
    const [isCreateAccountModalOpen, setIsCreateAccountModalOpen] = useState(false);
    const queryClient = useQueryClient();
    const user = useUserStore.getState().user;

    const loadAccounts = async () => {
        try {
            setLoading(true);
            setError(null);

            const data = await fetchAccounts();
            console.log(data.result.accountList);
            setAccounts(data.result.accountList);
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다';
            setError(errorMessage);
            console.error('계좌 조회 오류:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadAccounts();
    }, []);

    // 계좌 생성 후 목록 새로고침
    const handleAccountCreated = () => {
        loadAccounts();
        queryClient.invalidateQueries({ queryKey: ['accounts'] });
    };

    // 계좌 등록 처리
    const handleAccountRegistered = async (company: Company) => {
        try {
            const requestBody = {
                companyCode: company.companyCode,
                bankName: company.companyName
            };

            console.log('📤 전송할 데이터:', requestBody);

            // API 호출
            const response = await fetch('http://localhost:8080/my_account/check/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${sessionStorage.getItem("accessToken")}`
                },
                body: JSON.stringify(requestBody)
            });

            if (response.ok) {
                const result = await response.json();
                console.log('계좌 등록 성공:', result);

                // 모달 닫기
                setIsCreateAccountModalOpen(false);

                // 성공 메시지
                alert('입출금계좌가 성공적으로 개설되었습니다!');

                // 계좌 목록 새로고침
                handleAccountCreated();

            } else {
                throw new Error('계좌 개설에 실패했습니다.');
            }
        } catch (error) {
            console.error('API 호출 에러:', error);
            alert('계좌 개설 중 오류가 발생했습니다.');
        }
    };

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
                    반갑습니다! {user?.nickname}
                </h1>
                <p className="text-gray-600">
                    총 {accounts.length}개의 계좌가 있습니다
                </p>
            </div>

            {/* 계좌가 없는 경우 */}
            {accounts.length === 0 ? (
                <div className="bg-gray-50 border border-gray-200 rounded-lg p-8 text-center">
                    <div className="text-gray-500 mb-4">🏦</div>
                    <h3 className="text-lg font-medium text-gray-900 mb-2">
                        등록된 계좌가 없습니다
                    </h3>
                    <p className="text-gray-600 mb-6">
                        먼저 입출금계좌를 개설하여 뱅킹 서비스를 시작해보세요
                    </p>
                    <div className="flex justify-center gap-4">
                        <button
                            onClick={() => setIsCreateAccountModalOpen(true)}
                            className="px-6 py-3 bg-bank-primary text-white rounded-md hover:bg-bank-dark transition-colors font-medium"
                        >
                            입출금계좌 개설하기
                        </button>
                        <a
                            href="/products"
                            className="px-6 py-3 border border-bank-primary text-bank-primary rounded-md hover:bg-bank-light transition-colors font-medium"
                        >
                            금융상품 둘러보기
                        </a>
                    </div>
                </div>
            ) : (
                <>
                    {/* 입출금계좌가 없는 경우에만 개설 안내 */}
                    {checkingAccounts.length === 0 && (
                        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-6">
                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="text-lg font-medium text-blue-900 mb-1">
                                        입출금계좌가 없습니다
                                    </h3>
                                    <p className="text-blue-700">
                                        다른 금융상품 이용을 위해 입출금계좌를 먼저 개설해주세요
                                    </p>
                                </div>
                                <button
                                    onClick={() => setIsCreateAccountModalOpen(true)}
                                    className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                                >
                                    계좌 개설
                                </button>
                            </div>
                        </div>
                    )}

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

            {/* 계좌 개설 모달 */}
            <AccountModal
                isOpen={isCreateAccountModalOpen}
                onClose={() => setIsCreateAccountModalOpen(false)}
                onSubmit={handleAccountRegistered}
                title="입출금계좌 개설"
            />
        </div>
    )
}
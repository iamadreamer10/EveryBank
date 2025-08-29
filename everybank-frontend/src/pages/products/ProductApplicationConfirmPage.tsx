import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { Contract } from "../../types/product.ts";
import { useUserStore } from "../../stores/userStore.ts";
import { useProductStore } from "../../stores/productStore.ts";

// 가입 API 타입
interface ContractRequest {
    productCode: string;
    amount: number;
    optionId: number;
}

// 예금 가입 API
async function createDepositContract(contractData: ContractRequest): Promise<Contract> {
    const response = await fetch(`http://localhost:8080/contract/deposit`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            productCode: contractData.productCode,
            totalAmount: contractData.amount,
            optionId: contractData.optionId
        })
    });
    if (!response.ok) throw new Error('예금 가입 실패');
    return response.json();
}

// 적금 가입 API
async function createSavingContract(contractData: ContractRequest): Promise<Contract> {
    const response = await fetch(`http://localhost:8080/contract/saving`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            productCode: contractData.productCode,
            monthlyAmount: contractData.amount,
            optionId: contractData.optionId
        })
    });
    if (!response.ok) throw new Error('적금 가입 실패');
    return response.json();
}

function calculateMaturityDate(startDate: string | Date, monthsToAdd: number): string {
    const d = startDate ? new Date(startDate) : new Date();
    if (isNaN(d.getTime())) {
        throw new Error("유효하지 않은 시작 날짜입니다.");
    }

    const day = d.getDate();
    d.setMonth(d.getMonth() + monthsToAdd);

    // 월말 보정
    if (d.getDate() < day) {
        d.setDate(0); // 이전 달 마지막 날로 보정
    }

    return d.toISOString().split('T')[0];
}

export default function ProductApplicationConfirmPage() {
    const navigate = useNavigate();
    const user = useUserStore(state => state.user);
    const productCode = useProductStore(state => state.productCode);
    const productName = useProductStore(state => state.productName);
    const bankName = useProductStore(state => state.bankName);
    const productType = useProductStore(state => state.productType);
    const amount = useProductStore(state => state.amount);
    const selectedOption = useProductStore(state => state.selectedOption);
    const contractDate = new Date().toISOString().split('T')[0];
    const maturityDate = calculateMaturityDate(contractDate, selectedOption?.saveTerm);


    const [isSubmitting, setIsSubmitting] = useState(false);

    if (!productCode || !selectedOption || !amount || !productType) {
        return (
            <div className="text-center py-12">
                가입 정보가 없습니다. 이전 페이지로 이동해주세요.
            </div>
        );
    }

    const handleFinalSubmit = async () => {
        try {
            setIsSubmitting(true);

            const contractData: ContractRequest = {
                productCode,
                amount,
                optionId: selectedOption.optionId
            };

            let result: Contract;
            if (productType === 'deposit') {
                result = await createDepositContract(contractData);
            } else {
                result = await createSavingContract(contractData);
            }

            console.log('가입 완료:', result);
            alert('가입이 완료되었습니다!');
            navigate('/');

        } catch (error) {
            console.error('가입 실패:', error);
            alert('가입 중 오류가 발생했습니다.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
            <h1 className="text-3xl font-bold text-gray-900 mb-8 text-center">상품가입정보 확인</h1>

            <div className="bg-white shadow-md rounded-lg p-6 max-w-4xl mx-auto mb-8">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* 좌측 컬럼 */}
                    <div className="space-y-2">
                        <div className="flex justify-between">
                            <span className="font-medium text-gray-700">닉네임</span>
                            <span>{user?.nickname || '-'}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="font-medium text-gray-700">상품명</span>
                            <span>{productName}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="font-medium text-gray-700">은행명</span>
                            <span>{bankName}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="font-medium text-gray-700">상품유형</span>
                            <span>{productType == 'deposit' ? "정기예금" : "적금"}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="font-medium text-gray-700">이자유형</span>
                            <span>{selectedOption.interestRateTypeName}</span>
                        </div>
                    </div>

                    {/* 우측 컬럼 */}
                    <div className="space-y-2">
                        <div className="flex justify-between">
                            <span className="font-medium text-gray-700">금리</span>
                            <span>{selectedOption.interestRate}%</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="font-medium text-gray-700">{productType == 'deposit' ? "가입금액" : "월납입액"}</span>
                            <span>{amount.toLocaleString()}원</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="font-medium text-gray-700">계약일</span>
                            <span>{contractDate}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="font-medium text-gray-700">만기일</span>
                            <span>{maturityDate}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="font-medium text-gray-700">기간</span>
                            <span>{selectedOption.saveTerm}개월</span>
                        </div>
                    </div>
                </div>
            </div>

            <div className="flex justify-center gap-4">
                <button
                    onClick={() => navigate(-1)}
                    className="px-8 py-3 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 font-medium"
                >
                    취소
                </button>
                <button
                    onClick={handleFinalSubmit}
                    disabled={isSubmitting}
                    className="px-8 py-3 bg-bank-success text-white rounded-md hover:bg-bank-dark font-medium disabled:bg-gray-400"
                >
                    {isSubmitting ? '처리중...' : '가입완료'}
                </button>
            </div>
        </div>
    );
}

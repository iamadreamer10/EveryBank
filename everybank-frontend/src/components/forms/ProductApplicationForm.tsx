import {useState, useEffect} from 'react';
import type {ProductDetail, ProductOption, SavingOption} from '../../../types/product';
import {useNavigate} from 'react-router-dom';
import {useProductStore} from "../../stores/productStore.ts";

export interface ProductApplicationFormProps {
    productDetail: ProductDetail;
    productType: string;
    onSubmit: (formData: ApplicationFormData) => void;
    onCancel: () => void;
}

export interface ApplicationFormData {
    productCode: string;
    amount: number;
    optionId: number;
}

// 입출금계좌 정보 타입
interface CheckingAccount {
    accountId: number;
    currentBalance: number;
    bankName: string;
    lastTransactionDate: string;
}

// 입출금계좌 조회 API
const fetchCheckingAccount = async (): Promise<CheckingAccount | null> => {
    try {
        const response = await fetch('http://localhost:8080/my_account/check_balance', {
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) return null;
        const data = await response.json();
        return data.result;
    } catch (error) {
        console.error('입출금계좌 조회 실패:', error);
        return null;
    }
};

export default function ProductApplicationForm({
                                                   productDetail,
                                                   productType,
                                                   onCancel
                                               }: ProductApplicationFormProps) {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        amount: '',
        selectedOptionId: 0
    });
    const [checkingAccount, setCheckingAccount] = useState<CheckingAccount | null>(null);
    const [balanceError, setBalanceError] = useState<string>('');
    const [loadingAccount, setLoadingAccount] = useState(false);

    const setProductInfo = useProductStore(state => state.setProductInfo);
    const setAmount = useProductStore(state => state.setAmount);
    const setSelectedOption = useProductStore(state => state.setSelectedOption);

    // 예금 상품인 경우에만 입출금계좌 조회
    useEffect(() => {
        if (productType === 'deposit') {
            setLoadingAccount(true);
            fetchCheckingAccount()
                .then((account) => {
                    setCheckingAccount(account);
                    if (!account) {
                        setBalanceError('입출금계좌 정보를 찾을 수 없습니다.');
                    }
                })
                .finally(() => setLoadingAccount(false));
        }
    }, [productType]);

    // 금액 입력 시 실시간 잔액 확인 (예금인 경우만)
    const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const amount = e.target.value;
        setFormData({...formData, amount});

        if (productType === 'deposit' && checkingAccount) {
            const amountValue = Number(amount);
            if (amountValue > checkingAccount.currentBalance) {
                setBalanceError(`잔액이 부족합니다. (보유: ${checkingAccount.currentBalance.toLocaleString()}원)`);
            } else {
                setBalanceError('');
            }
        }
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.selectedOptionId) {
            alert('옵션을 선택해주세요.');
            return;
        }

        // 예금 상품인 경우 잔액 확인
        if (productType === 'deposit') {
            if (!checkingAccount) {
                alert('입출금계좌가 없습니다. 먼저 입출금계좌를 개설해주세요.');
                return;
            }

            const amount = Number(formData.amount);
            if (checkingAccount.currentBalance < amount) {
                alert(`잔액이 부족합니다. (보유: ${checkingAccount.currentBalance.toLocaleString()}원)`);
                return;
            }
        }

        // 선택한 옵션 객체
        const selectedOptionObj = productDetail.option.find(
            opt => opt.id === formData.selectedOptionId
        );
        if (!selectedOptionObj) return;

        // Store에 저장
        setProductInfo({
            productCode: productDetail.productCode,
            productName: productDetail.name,
            bankName: productDetail.bank,
            productType: productType as 'deposit' | 'saving'
        });
        setAmount(Number(formData.amount));
        setSelectedOption({
            optionId: selectedOptionObj.id,
            interestRateTypeName: selectedOptionObj.interestRateTypeName,
            interestRate: selectedOptionObj.interestRate,
            saveTerm: selectedOptionObj.saveTerm
        });

        // Confirm 페이지 이동
        navigate('/products/application-confirm');
    };

    return (
        <div>
            <div className="mb-4 p-3 bg-bank-light rounded-md">
                <p className="text-sm text-gray-600">상품명: {productDetail.name}</p>
                <p className="text-sm text-gray-600">은행: {productDetail.bank}</p>
            </div>

            {/* 예금 상품인 경우 입출금계좌 정보 표시 */}
            {productType === 'deposit' && (
                <div className="mb-4">
                    {loadingAccount ? (
                        <div className="p-4 bg-gray-50 rounded-md text-center">
                            <div className="text-sm text-gray-600">입출금계좌 정보를 확인하고 있습니다...</div>
                        </div>
                    ) : checkingAccount ? (
                        <div className="p-4 bg-blue-50 border border-blue-200 rounded-md">
                            <h4 className="font-medium text-blue-900 mb-2">입출금계좌 정보</h4>
                            <div className="space-y-1 text-sm">
                                <div className="flex justify-between">
                                    <span className="text-blue-700">계좌번호</span>
                                    <span className="font-medium">{checkingAccount.accountId}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-blue-700">은행</span>
                                    <span className="font-medium">{checkingAccount.bankName}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-blue-700">보유 잔액</span>
                                    <span className="font-medium">{checkingAccount.currentBalance.toLocaleString()}원</span>
                                </div>
                                {formData.amount && (
                                    <div className="flex justify-between border-t pt-1 mt-2">
                                        <span className="text-blue-700">가입 후 잔액</span>
                                        <span className={`font-medium ${
                                            checkingAccount.currentBalance - Number(formData.amount) >= 0
                                                ? 'text-green-600'
                                                : 'text-red-600'
                                        }`}>
                                            {(checkingAccount.currentBalance - Number(formData.amount || 0)).toLocaleString()}원
                                        </span>
                                    </div>
                                )}
                            </div>
                        </div>
                    ) : (
                        <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-md">
                            <p className="text-yellow-800 mb-2">
                                ⚠️ 예금 가입을 위해서는 입출금계좌가 필요합니다.
                            </p>
                            <button
                                type="button"
                                onClick={() => navigate('/my_account')}
                                className="text-yellow-900 underline hover:text-yellow-700"
                            >
                                입출금계좌 개설하러 가기
                            </button>
                        </div>
                    )}
                </div>
            )}

            <form onSubmit={handleSubmit}>
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            {productType === 'deposit' ? '가입금액' : '월납입액'}
                        </label>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                value={formData.amount}
                                onChange={handleAmountChange}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                                placeholder={`${productType === 'deposit' ? '가입할' : '월납입할'} 금액을 입력하세요`}
                                required
                                disabled={!checkingAccount && productType === 'deposit'}
                            />
                            {/* 예금인 경우 전액 가입 버튼 */}
                            {productType === 'deposit' && checkingAccount && (
                                <button
                                    type="button"
                                    onClick={() => {
                                        setFormData({...formData, amount: checkingAccount.currentBalance.toString()});
                                        setBalanceError('');
                                    }}
                                    className="px-3 py-2 text-sm text-bank-primary hover:text-bank-dark border border-bank-primary rounded whitespace-nowrap"
                                >
                                    전액가입
                                </button>
                            )}
                        </div>
                        {balanceError && (
                            <div className="mt-2 text-sm text-red-600">
                                {balanceError}
                            </div>
                        )}
                    </div>

                    {/* 예금 상품 가입 시 안내 메시지 */}
                    {productType === 'deposit' && formData.amount && checkingAccount && (
                        <div className="p-3 bg-yellow-50 border border-yellow-200 rounded">
                            <p className="text-sm text-yellow-800">
                                ⚠️ 가입과 동시에 입출금계좌에서 {Number(formData.amount).toLocaleString()}원이 차감됩니다.
                                예금 가입 후에는 만기일까지 추가 입금이 불가능합니다.
                            </p>
                        </div>
                    )}

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            가입 옵션 선택
                        </label>
                        <div className="border border-gray-200 rounded-lg overflow-hidden">
                            <table className="min-w-full">
                                <thead className="bg-bank-light">
                                <tr>
                                    <th className="px-3 py-2 text-left text-xs font-medium">선택</th>
                                    <th className="px-3 py-2 text-left text-xs font-medium">기간</th>
                                    <th className="px-3 py-2 text-left text-xs font-medium">금리유형</th>
                                    <th className="px-3 py-2 text-left text-xs font-medium">기본금리</th>
                                    <th className="px-3 py-2 text-left text-xs font-medium">최고금리</th>
                                    {productType === 'saving' && (
                                        <th className="px-3 py-2 text-left text-xs font-medium">적립유형</th>
                                    )}
                                </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                {productDetail.option.map((option: SavingOption | ProductOption, index: number) => (
                                    <tr key={option.optionId || index} className="hover:bg-gray-50">
                                        <td className="px-3 py-2">
                                            <input
                                                type="radio"
                                                name="optionId"
                                                value={option.id}
                                                checked={formData.selectedOptionId === option.id}
                                                onChange={(e) => {
                                                    console.log("선택된 optionId:", e.target.value);
                                                    setFormData({
                                                        ...formData,
                                                        selectedOptionId: Number(e.target.value)
                                                    });
                                                }}
                                                className="accent-bank-dark focus:ring-bank-dark"
                                            />
                                        </td>
                                        <td className="px-3 py-2 text-sm">{option.saveTerm}개월</td>
                                        <td className="px-3 py-2 text-sm">{option.interestRateTypeName}</td>
                                        <td className="px-3 py-2 text-sm">{option.interestRate}%</td>
                                        <td className="px-3 py-2 text-sm">{option.interestRate2}%</td>
                                        {productType === 'saving' && (
                                            <td className="px-3 py-2 text-sm">
                                                {(option as SavingOption).reverseTypeName}
                                            </td>
                                        )}
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div className="flex gap-3 mt-6">
                    <button
                        type="button"
                        onClick={onCancel}
                        className="flex-1 px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                    >
                        취소
                    </button>
                    <button
                        type="submit"
                        className="flex-1 px-4 py-2 bg-bank-primary text-white rounded-md hover:bg-bank-dark disabled:opacity-50 disabled:cursor-not-allowed"
                        disabled={
                            (productType === 'deposit' && (!checkingAccount || balanceError !== '')) ||
                            !formData.amount ||
                            !formData.selectedOptionId
                        }
                    >
                        신청하기
                    </button>
                </div>
            </form>
        </div>
    );
};
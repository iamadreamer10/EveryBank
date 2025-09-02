import {useState} from 'react';
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


export default function ProductApplicationForm({
                                                   productDetail,
                                                   productType,
                                                   onCancel
                                               }: ProductApplicationFormProps) {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        amount: '',
        selectedOptionId: 0  // number로 초기화
    });
    const setProductInfo = useProductStore(state => state.setProductInfo);
    const setAmount = useProductStore(state => state.setAmount);
    const setSelectedOption = useProductStore(state => state.setSelectedOption);


    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.selectedOptionId) {
            alert('옵션을 선택해주세요.');
            return;
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
            productType: productType as 'deposits' | 'savings'
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

            <form onSubmit={handleSubmit}>
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            가입금액
                        </label>
                        <input
                            type="number"
                            value={formData.amount}
                            onChange={(e) => setFormData({...formData, amount: e.target.value})}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                            placeholder="가입할 금액을 입력하세요"
                            required
                        />
                    </div>

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
                                    {productType === 'savings' && (
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
                                                    console.log("선택된 optionId:", e.target.value);   // string
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
                                        {productType === 'savings' && (
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
                        className="flex-1 px-4 py-2 bg-bank-primary text-white rounded-md hover:bg-bank-dark"
                    >
                        신청하기
                    </button>
                </div>
            </form>
        </div>
    );
};
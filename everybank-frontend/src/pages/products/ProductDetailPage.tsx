import {Link, useParams, useNavigate} from "react-router-dom";
import type { ProductDetail, ProductOption, SavingOption} from "../../types/product.ts";
import {useEffect, useState} from "react";
import Modal from "../../components/common/Modal.tsx";
import ProductApplicationForm, {type ApplicationFormData} from "../../components/forms/ProductApplicationForm.tsx";


async function fetchProductDetail(productCode: string, productType: string): Promise<ProductDetail> {
    const response = await fetch(`http://localhost:8080/product/${productType}/${productCode}`);
    if (!response.ok) throw new Error("Failed to fetch deposits");
    return response.json();
}


export default function ProductDetailPage() {
    const {productCode, productType} = useParams<{productCode: string, productType: string}>();
    const [productDetail, setProductDetail] = useState<ProductDetail>();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [showModal, setShowModal] = useState(false);
    const navigate = useNavigate();


    useEffect(() => {
        if (!productCode || !productType) return;

        setLoading(true);
        fetchProductDetail(productCode, productType)
            .then((data) => {
                try {
                    const result = data.result;
                    // 타입 가드 추가
                    if (!result || !result.options) {
                        throw new Error('Invalid product data structure');
                    }

                    const productInfo = result.productInfo;
                    console.log(result);

                    // 옵션 타입 동적 처리
                    const productOption = productType === 'savings'
                        ? result.options as SavingOption[]
                        : result.options as ProductOption[];

                    // 최단/최장 개월수 계산
                    const periods = productOption.map(option => option.saveTerm || 0);
                    const minPeriod = Math.min(...periods);
                    const maxPeriod = Math.max(...periods);

                    const productDetail: ProductDetail = {
                        productCode: productInfo.productCode,
                        bank: productInfo.companyName,
                        name: productInfo.productName,
                        member: productInfo.joinMember,
                        maxLimit: productInfo.maxLimit === 0
                            ? "한도없음"
                            : (typeof productInfo.maxLimit === 'number' && productInfo.maxLimit > 0)
                                ? productInfo.maxLimit.toLocaleString() + "원"
                                : "정보없음",
                        period: [minPeriod, maxPeriod],
                        option: productOption
                    };

                    // console.log("product 상세: ", productDetail);

                    setProductDetail(productDetail);
                    setError(null);

                } catch (transformError) {
                    setError(`데이터 변환 오류: ${transformError.message}`);
                }
            })
            .catch((err) => {
                setError(`API 요청 실패: ${err.message}`);
                console.error('Product detail fetch error:', err);
            })
            .finally(() => setLoading(false));
    }, [productCode, productType]);  // 의존성 배열 수정

    const handleContractSubmit = (formData: ApplicationFormData) => {
        navigate('/products/application-confirm', {
            state: {
                formData,
                productDetail,
                productType
            }
        });
    };

    return(
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
            {loading && <p>로딩 중...</p>}
            {error && <p className="text-red-500">에러: {error}</p>}
            {!loading && !error && productDetail && (
                <div className="text-center mb-12">
                    <h1 className="text-3xl font-bold text-gray-900 mb-4">상품가입정보</h1>
                        <div className="bg-bank-primary text-white rounded-lg p-6">
                            <div className="flex items-center justify-between">
                                <h2 className="text-2xl font-bold mb-4">{productDetail.name}</h2>
                                <h2 className="text-2xl font-bold mb-4">{productDetail.bank}</h2>
                            </div>
                            <div className="text-left space-y-2">
                                <p><span className="font-semibold">상품종류:</span> {productType === 'deposits' ? '정기예금' : '적금'}</p>
                                <p><span className="font-semibold">가입대상:</span> {productDetail.member}</p>
                                <p><span className="font-semibold">가입기간:</span> {productDetail.period[0]}~{productDetail.period[1]}개월</p>
                                <p><span className="font-semibold">최대한도:</span> {productDetail.maxLimit}</p>
                            </div>
                        </div>

                    <div className="flex justify-center gap-4 m-8">
                        <Link
                            to="/products"
                            className="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                        >
                            취소
                        </Link>
                        <button
                            className="px-6 py-2 bg-bank-success text-white rounded-md hover:bg-bank-dark"
                            onClick={() => setShowModal(true)}
                        >
                            가입하기
                        </button>
                    </div>

                    <h3 className="text-xl font-bold text-gray-900 mb-6">가입옵션</h3>
                    <div className="bg-white rounded-lg shadow overflow-hidden">
                        <table className="min-w-full">
                            <thead className="bg-white divide-y divide-gray-200">
                            <tr>
                                <th className="px-6 py-4 bg-bank-dark text-white font-medium">저축금리유형</th>
                                {productDetail.option.map((opt, index) => (
                                    <th key={index} className="px-6 py-3 text-center text-sm font-medium">단리</th>
                                ))}
                            </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                            <tr>
                                <td className="px-6 py-4 bg-bank-dark text-white font-medium">저축기간(개월)</td>
                                {productDetail.option.map((opt, index) => (
                                    <td key={index} className="px-6 py-4 text-center">{opt.saveTerm}</td>
                                ))}
                            </tr>
                            <tr>
                                <td className="px-6 py-4 bg-bank-dark text-white font-medium">저축금리(%)</td>
                                {productDetail.option.map((opt, index) => (
                                    <td key={index} className="px-6 py-4 text-center">{opt.interestRate}</td>
                                ))}
                            </tr>
                            <tr>
                                <td className="px-6 py-4 bg-bank-dark text-white font-medium">최고우대금리(%)</td>
                                {productDetail.option.map((opt, index) => (
                                    <td key={index} className="px-6 py-4 text-center">{opt.interestRate2}</td>
                                ))}
                            </tr>
                            {productType === 'savings' && (
                                <>
                                    <tr>
                                        <td className="px-6 py-4 bg-bank-dark text-white font-medium">적립유형</td>
                                        {productDetail.option.map((opt, index) => (
                                            <td key={index} className="px-6 py-4 text-center">
                                                {(opt as SavingOption).reverseTypeName}
                                            </td>
                                        ))}
                                    </tr>
                                </>
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
            <Modal
                isOpen={showModal}
                onClose={() => setShowModal(false)}
                title="상품 가입 신청"
            >
                <ProductApplicationForm
                    productDetail={productDetail}
                    productType={productType || 'deposit'}
                    onSubmit={handleContractSubmit}
                    onCancel={() => setShowModal(false)}
                />
            </Modal>
        </div>
    )
}

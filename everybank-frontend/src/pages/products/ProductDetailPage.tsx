import { Link } from "react-router-dom";
import type {ProductDetail} from "../../types/product.ts";

// API 함수들
async function fetchProductDeatil(): Promise<ProductDetail> {
    const response = await fetch("http://localhost:8080/product/deposit");
    if (!response.ok) throw new Error("Failed to fetch deposits");
    return response.json();
}


export default function ProductDetailPage() {
    return(
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
            <div className="text-center mb-12">
                <h1 className="text-3xl font-bold text-gray-900 mb-4">상품가입정보</h1>
                <div className="bg-bank-light rounded-lg p-8 mb-8">
                    <div className="bg-bank-primary text-white rounded-lg p-6">
                        <h2 className="text-2xl font-bold mb-4">WON플러스예금</h2>
                        <div className="text-left space-y-2">
                            <p><span className="font-semibold">상품종류:</span> 정기예금</p>
                            <p><span className="font-semibold">가입대상:</span> 실명의 개인</p>
                            <p><span className="font-semibold">가입기간:</span> 1~36개월</p>
                            <p><span className="font-semibold">가입금액:</span> 1만원 이상</p>
                        </div>
                    </div>
                    <div className="text-right mt-4 text-lg font-semibold text-gray-700">
                        우리은행
                    </div>
                </div>

                <div className="flex justify-center gap-4 mb-8">
                    <Link
                        to="/products"
                        className="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                    >
                        취소
                    </Link>
                    <button className="px-6 py-2 bg-bank-success text-white rounded-md hover:bg-bank-dark">
                        가입완료
                    </button>
                </div>

                <h3 className="text-xl font-bold text-gray-900 mb-6">가입옵션</h3>
                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <table className="min-w-full">
                        <thead className="bg-bank-dark text-white">
                        <tr>
                            <th className="px-6 py-3 text-left text-sm font-medium">저축금리유형</th>
                            <th className="px-6 py-3 text-center text-sm font-medium">단리</th>
                            <th className="px-6 py-3 text-center text-sm font-medium">단리</th>
                            <th className="px-6 py-3 text-center text-sm font-medium">단리</th>
                            <th className="px-6 py-3 text-center text-sm font-medium">단리</th>
                            <th className="px-6 py-3 text-center text-sm font-medium">단리</th>
                            <th className="px-6 py-3 text-center text-sm font-medium">단리</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        <tr>
                            <td className="px-6 py-4 bg-bank-dark text-white font-medium">저축기간(개월)</td>
                            <td className="px-6 py-4 text-center">1</td>
                            <td className="px-6 py-4 text-center">3</td>
                            <td className="px-6 py-4 text-center">6</td>
                            <td className="px-6 py-4 text-center">12</td>
                            <td className="px-6 py-4 text-center">24</td>
                            <td className="px-6 py-4 text-center">36</td>
                        </tr>
                        <tr>
                            <td className="px-6 py-4 bg-bank-dark text-white font-medium">저축금리(%)</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                        </tr>
                        <tr>
                            <td className="px-6 py-4 bg-bank-dark text-white font-medium">최고우대금리(%)</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                            <td className="px-6 py-4 text-center">2.45</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    )
}

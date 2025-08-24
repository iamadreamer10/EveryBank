import { useState, useEffect } from 'react';
import ProductTabs from "../../components/products/ProductTabs.tsx";
import ProductGrid from "../../components/products/ProductGrid.tsx";
import BankFilter from "../../components/products/BankFilter.tsx";
import ProductSearchSection from "../../components/products/ProductSearchSection.tsx";
import type {Product} from "../../types/product.ts";

// API 함수들
async function fetchDeposits(): Promise<Product[]> {
    const response = await fetch("http://localhost:8080/product/deposit");
    if (!response.ok) throw new Error("Failed to fetch deposits");
    return response.json();
}

async function fetchSaving(): Promise<Product[]> {
    const response = await fetch("http://localhost:8080/product/savings");
    if (!response.ok) throw new Error("Failed to fetch saving");
    return response.json();
}

export default function ProductsPage() {
    const [selectedAmount, setSelectedAmount] = useState('300000');
    const [selectedPeriod, setSelectedPeriod] = useState('24');
    const [selectedTab, setSelectedTab] = useState('정기예금상품');
    const [selectedBank, setSelectedBank] = useState('전체');

    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let fetchFn: (() => Promise<Product[]>) | null = null;

        if (selectedTab === "정기예금상품") {
            fetchFn = fetchDeposits;
        } else if (selectedTab === "적금상품") {
            fetchFn = fetchSaving;
        }

        if (fetchFn) {
            setLoading(true);
            fetchFn()
                .then((data) => {
                    console.log(data.result);
                    const mappedProducts: Product[] = data.result.map((productApi: any) => ({
                        productCode: productApi.productCode,
                        bank: productApi.companyName,
                        name: productApi.productName,
                        member: productApi.joinMember,
                        maxLimit:
                            productApi.maxLimit === 0 ? "한도없음" : productApi.maxLimit,
                        rate: productApi.mainRate,
                    }));
                    setProducts(mappedProducts);
                    setError(null);
                })
                .catch((err) => setError(err.message))
                .finally(() => setLoading(false));
        }
    }, [selectedTab]);

    const bankTabs = ['전체', '우리은행', '신한은행', '국민은행', '하나은행', '농협은행', '카카오뱅크'];


    const handleBankCompare = (bank: string) => {
        console.log('은행 비교:', bank);
        if (bank !== '상세비교') {
            setSelectedBank(bank);
        }
    };

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <ProductSearchSection
                selectedAmount={selectedAmount}
                selectedPeriod={selectedPeriod}
                onAmountChange={setSelectedAmount}
                onPeriodChange={setSelectedPeriod}
                onBankCompare={handleBankCompare}
            />

            <ProductTabs selectedTab={selectedTab} onTabChange={setSelectedTab} />

            <BankFilter
                banks={bankTabs}
                selectedBank={selectedBank}
                onBankChange={setSelectedBank}
            />

            {loading && <p>로딩 중...</p>}
            {error && <p className="text-red-500">에러: {error}</p>}
            {!loading && !error && (
                <ProductGrid products={products} onCompare={(id) => console.log(id)} />
            )}
        </div>
    );
}


import ProductCard from "./ProductCard.tsx";
import type {Product} from "../../types/product.ts";


interface ProductGridProps {
    products: Product[];
    productType: 'deposits' | 'savings';  // 현재 탭 정보
    onCompare: (productId: number) => void;
}

export default function ProductGrid({products, productType, onCompare}: ProductGridProps) {
    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {products.map((product) => (
                <ProductCard
                    key={product.productCode}
                    product={product}
                    productType={productType}
                    onCompare={onCompare}
                />
            ))}
        </div>
    );
}

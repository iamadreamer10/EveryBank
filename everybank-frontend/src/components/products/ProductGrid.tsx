import ProductCard from "./ProductCard.tsx";
import type {Product} from "../../types/product.ts";


interface ProductGridProps {
    products: Product[];
    onCompare: (productId: number) => void;
}

export default function ProductGrid({products, onCompare}: ProductGridProps) {
    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {products.map((product) => (
                <ProductCard
                    key={product.productCode}
                    product={product}
                    onCompare={onCompare}
                />
            ))}
        </div>
    );
}

import { Link } from 'react-router-dom';
import type {Product} from "../../types/product.ts";

interface ProductCardProps {
    product: Product;
    onCompare: (productId: string) => void;
}

export default function ProductCard({product, onCompare}: ProductCardProps) {
    return (
        <div className="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow">
            <div className="flex justify-between items-start mb-4">
                <span className="text-sm font-bold text-gray-600">{product.bank}</span>
                <button
                    className="px-3 py-1 bg-bank-primary text-white text-xs rounded hover:bg-bank-dark"
                    onClick={() => onCompare(product.productCode)}
                >
                    비교하기
                </button>
            </div>

            <Link to={`/products/${product.productCode}`} className="block">
                <h3 className="font-semibold text-lg text-gray-900 mb-3 hover:text-bank-primary transition-colors">
                    {product.name}
                </h3>
            </Link>

            <div className="space-y-2 text-m text-gray-600 mb-4">
                <p>최대한도: {
                    typeof product.maxLimit === "number"
                    ? product.maxLimit.toLocaleString() + " 원"
                    : product.maxLimit}
                </p>
            </div>

            <div className="text-right">
                <span className="text-2xl font-bold text-bank-primary">{product.rate} %</span>
            </div>
        </div>
    );
}

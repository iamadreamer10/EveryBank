import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import Header from "./components/common/Header";
import Footer from "./components/common/Footer";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/auth/LoginPage";
import SignupPage from "./pages/auth/SignupPage";
import ProductsPage from "./pages/products/ProductsPage.tsx";
import ProductDetailPage from "./pages/products/ProductDetailPage.tsx";
import {AuthProvider} from "./context/AuthContext.tsx";

export default function App() {
    return (
        <AuthProvider>
            <Router>
                <div className="min-h-screen bg-gray-50 flex flex-col">
                    <Header/>
                    <main className="flex-1">
                        <Routes>
                            <Route path="/" element={<HomePage/>}/>
                            <Route path="/login" element={<LoginPage/>}/>
                            <Route path="/signup" element={<SignupPage/>}/>
                            <Route path="/products" element={<ProductsPage/>}/>
                            <Route path="/products/:productType/:productCode" element={<ProductDetailPage/>}/>
                        </Routes>
                    </main>
                    <Footer/>
                </div>
            </Router>

        </AuthProvider>
    );
}
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import logo from '../../assets/image/everybank_logo.png';

export default function Header() {
    const { isAuthenticated, logout } = useAuth();

    const handleLogout = () => {
        if(window.confirm('로그아웃 하시겠습니까?')){
            logout();
        }
    };

    return (
        <header className="bg-white border-b border-gray-200 shadow-sm">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">
                    {/* Logo */}
                    <div className="flex-shrink-0">
                        <Link to="/" className="text-2xl font-bold text-gray-900 hover:text-bank-primary transition-colors duration-200">
                            <img src={logo} alt="로고" className="w-auto h-10 object-contain"/>
                        </Link>
                    </div>

                    {/* Navigation */}
                    <nav className="hidden md:flex space-x-8">
                        <Link
                            to="/products"
                            className="text-bank-primary border-b-2 border-bank-primary px-3 py-2 text-sm font-medium"
                        >
                            금융상품
                        </Link>

                        {isAuthenticated && (
                            <Link
                                to="/my_account"
                                className="text-gray-700 hover:text-bank-primary px-3 py-2 text-sm font-medium transition-colors duration-200"
                            >
                                내계좌
                            </Link>
                        )}

                        {isAuthenticated ? (
                            <button
                                onClick={handleLogout}
                                className="text-gray-700 hover:text-bank-primary px-3 py-2 text-sm font-medium transition-colors duration-200"
                            >
                                로그아웃
                            </button>
                        ) : (
                            <>
                                <Link
                                    to="/login"
                                    className="text-gray-700 hover:text-bank-primary px-3 py-2 text-sm font-medium transition-colors duration-200"
                                >
                                    로그인
                                </Link>
                                <Link
                                    to="/signup"
                                    className="text-gray-700 hover:text-bank-primary px-3 py-2 text-sm font-medium transition-colors duration-200"
                                >
                                    회원가입
                                </Link>
                            </>
                        )}
                    </nav>

                    {/* Mobile menu button */}
                    <div className="md:hidden">
                        <button className="text-gray-700 hover:text-bank-primary">
                            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                            </svg>
                        </button>
                    </div>
                </div>
            </div>
        </header>
    );
}
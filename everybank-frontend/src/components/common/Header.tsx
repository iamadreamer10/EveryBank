import {Link} from 'react-router-dom'
import logo from '../../assets/image/everybank_logo.png'

export default function Header() {
    return (
        <header className="bg-white border-b border-gray-200 shadow-sm">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">
                    {/* Logo */}
                    <div className="flex-shrink-0">
                        <Link
                            to="/"
                            className="text-2xl font-bold text-gray-900 hover:text-green-600 transition-colors duration-200"
                        >
                            <img src={logo} alt="로고" className="w-auto h-10 object-contain"/>
                        </Link>
                    </div>

                    {/* Navigation */}
                    <nav className="hidden md:flex space-x-8">
                        <Link
                            to="/products"
                            className="text-green-600 border-b-2 border-green-600 px-3 py-2 text-sm font-medium"
                        >
                            금융상품
                        </Link>
                        <Link
                            to="/login"
                            className="text-gray-700 hover:text-green-600 px-3 py-2 text-sm font-medium transition-colors duration-200"
                        >
                            로그인
                        </Link>
                        <Link
                            to="/signup"
                            className="text-gray-700 hover:text-green-600 px-3 py-2 text-sm font-medium transition-colors duration-200"
                        >
                            회원가입
                        </Link>
                    </nav>

                    {/* Mobile menu button */}
                    <div className="md:hidden">
                        <button className="text-gray-700 hover:text-green-600">
                            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                      d="M4 6h16M4 12h16M4 18h16"/>
                            </svg>
                        </button>
                    </div>
                </div>
            </div>
        </header>
    )
}

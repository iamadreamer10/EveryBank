import React, { useEffect, useState } from 'react';

export default function BankingLandingPage(){
    const [isVisible, setIsVisible] = useState({});

    useEffect(() => {
        const observer = new IntersectionObserver(
            (entries) => {
                entries.forEach((entry) => {
                    setIsVisible(prev => ({
                        ...prev,
                        [entry.target.id]: entry.isIntersecting
                    }));
                });
            },
            { threshold: 0.1, rootMargin: '0px 0px -50px 0px' }
        );

        const elements = document.querySelectorAll('[data-animate]');
        elements.forEach(el => observer.observe(el));

        return () => observer.disconnect();
    }, []);

    const AnimatedSection = ({ children, id, className = "" }) => (
        <div
            id={id}
            data-animate
            className={`transition-all duration-1000 'opacity-100 translate-y-0' ${className}`}
        >
            {children}
        </div>
    );

    return (
        <div className="min-h-screen bg-white font-sans">
            {/* Hero Section */}
            <section className="relative min-h-screen flex items-center justify-center overflow-hidden">
                {/* Background with custom colors */}
                <div className="absolute inset-0 bg-gradient-to-br from-emerald-800 via-teal-600 to-emerald-500"></div>

                {/* Floating coins pattern */}
                <div className="absolute inset-0 opacity-20">
                    <div className="absolute top-20 left-20 w-12 h-12 bg-emerald-200 rounded-full animate-bounce"></div>
                    <div className="absolute top-40 right-32 w-8 h-8 bg-teal-200 rounded-full animate-pulse"></div>
                    <div className="absolute bottom-32 left-40 w-16 h-16 bg-emerald-300 rounded-full animate-bounce" style={{animationDelay: '1s'}}></div>
                    <div className="absolute bottom-20 right-20 w-10 h-10 bg-teal-300 rounded-full animate-pulse" style={{animationDelay: '0.5s'}}></div>
                </div>

                <div className="relative z-10 max-w-6xl mx-auto px-6 text-center text-white">
                    <h1 className="text-5xl md:text-7xl font-bold mb-8 leading-tight">
                        🏦 안전한 예적금으로<br />시작하는 미래 설계
                    </h1>
                    <p className="text-xl md:text-2xl mb-12 opacity-90 leading-relaxed">
                        국내 18개 은행의<br />
                        검증된 상품으로 목돈을 만들어보세요
                    </p>
                    <div className="flex flex-col sm:flex-row gap-6 justify-center">
                        <button className="bg-gradient-to-r from-teal-400 to-emerald-400 hover:from-teal-300 hover:to-emerald-300 text-white px-12 py-4 rounded-full text-xl font-bold shadow-2xl transform hover:-translate-y-1 transition-all duration-300">
                            상품 보러가기
                        </button>
                        <button className="border-2 border-emerald-200 hover:bg-emerald-200 hover:text-emerald-800 text-white px-12 py-4 rounded-full text-xl font-bold transition-all duration-300">
                            금리 비교하기
                        </button>
                    </div>
                </div>
            </section>

            {/* Features Section */}
            <AnimatedSection id="features" className="py-24 bg-bank-light">
                <div className="max-w-7xl mx-auto px-6">
                    <h2 className="text-4xl md:text-5xl font-bold text-center mb-16" style={{color: '#387F6C'}}>
                        왜 이 상품들을 선택해야 할까요?
                    </h2>
                    <div className="grid md:grid-cols-3 gap-12">
                        <div className="bg-white p-10 rounded-3xl shadow-2xl hover:shadow-3xl transform hover:-translate-y-3 transition-all duration-300 border-2" style={{borderColor: '#E0F2ED'}}>
                            <div className="text-6xl mb-6 text-center">🏛️</div>
                            <h3 className="text-2xl font-bold mb-4 text-center" style={{color: '#387F6C'}}>
                                신뢰할 수 있는 은행
                            </h3>
                            <p className="text-lg leading-relaxed" style={{color: '#5E7F76'}}>
                                KB국민은행과 한국스탠다드차타드은행은 국내 대표 금융기관으로, 예금보호법에 따라 최대 5천만원까지 안전하게 보장합니다.
                            </p>
                        </div>

                        <div className="bg-white p-10 rounded-3xl shadow-2xl hover:shadow-3xl transform hover:-translate-y-3 transition-all duration-300 border-2" style={{borderColor: '#E0F2ED'}}>
                            <div className="text-6xl mb-6 text-center">💰</div>
                            <h3 className="text-2xl font-bold mb-4 text-center" style={{color: '#387F6C'}}>
                                경쟁력 있는 금리
                            </h3>
                            <p className="text-lg leading-relaxed" style={{color: '#5E7F76'}}>
                                KB국민프리미엄적금 연 4%, e-그린세이브예금 연 2.85%의 우대금리로 안정적인 수익을 보장받으세요.
                            </p>
                        </div>

                        <div className="bg-white p-10 rounded-3xl shadow-2xl hover:shadow-3xl transform hover:-translate-y-3 transition-all duration-300 border-2" style={{borderColor: '#E0F2ED'}}>
                            <div className="text-6xl mb-6 text-center">📋</div>
                            <h3 className="text-2xl font-bold mb-4 text-center" style={{color: '#387F6C'}}>
                                유연한 가입 조건
                            </h3>
                            <p className="text-lg leading-relaxed" style={{color: '#5E7F76'}}>
                                KB적금은 한도 없음, SC예금은 최대 10억원까지 가입 가능하여 개인의 재정 상황에 맞게 선택할 수 있습니다.
                            </p>
                        </div>
                    </div>
                </div>
            </AnimatedSection>

            {/* Products Section */}
            <AnimatedSection id="products" className="py-24 bg-white">
                <div className="max-w-7xl mx-auto px-6">
                    <h2 className="text-4xl md:text-5xl font-bold text-center mb-16" style={{color: '#387F6C'}}>
                        추천 예적금 상품
                    </h2>
                    <div className="grid lg:grid-cols-2 gap-12">
                        <div className="relative overflow-hidden rounded-3xl p-12 text-white shadow-2xl transform hover:-translate-y-2 transition-all duration-300" style={{background: 'linear-gradient(135deg, #5ACCAE, #00C490)'}}>
                            {/* Shimmer effect */}
                            <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-radial from-emerald-200 to-transparent opacity-30 animate-pulse"></div>

                            <h3 className="text-3xl font-bold mb-6">🏦 KB국민프리미엄적금(정액)</h3>
                            <div className="text-6xl font-bold mb-8">연 4%</div>
                            <ul className="space-y-4 mb-8">
                                <li className="flex items-center text-lg">
                                    <span className="text-emerald-200 mr-4 text-xl">✓</span>
                                    최대 한도: 한도 없음
                                </li>
                                <li className="flex items-center text-lg">
                                    <span className="text-emerald-200 mr-4 text-xl">✓</span>
                                    KB국민은행 대표 적금상품
                                </li>
                                <li className="flex items-center text-lg">
                                    <span className="text-emerald-200 mr-4 text-xl">✓</span>
                                    안정적인 수익률 보장
                                </li>
                                <li className="flex items-center text-lg">
                                    <span className="text-emerald-200 mr-4 text-xl">✓</span>
                                    정액 적립방식으로 계획적 저축
                                </li>
                            </ul>
                            <button className="bg-gradient-to-r from-teal-400 to-emerald-400 hover:from-teal-300 hover:to-emerald-300 px-10 py-4 rounded-full text-lg font-bold shadow-xl transform hover:-translate-y-1 transition-all duration-300">
                                비교하기
                            </button>
                        </div>

                        <div className="relative overflow-hidden rounded-3xl p-12 text-white shadow-2xl transform hover:-translate-y-2 transition-all duration-300" style={{background: 'linear-gradient(135deg, #5ACCAE, #00C490)'}}>
                            {/* Shimmer effect */}
                            <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-radial from-emerald-200 to-transparent opacity-30 animate-pulse" style={{animationDelay: '1s'}}></div>

                            <h3 className="text-3xl font-bold mb-6">🌱 e-그린세이브예금</h3>
                            <div className="text-6xl font-bold mb-8">연 2.85%</div>
                            <ul className="space-y-4 mb-8">
                                <li className="flex items-center text-lg">
                                    <span className="text-emerald-200 mr-4 text-xl">✓</span>
                                    최대 한도: 1,000,000,000원
                                </li>
                                <li className="flex items-center text-lg">
                                    <span className="text-emerald-200 mr-4 text-xl">✓</span>
                                    한국스탠다드차타드은행
                                </li>
                                <li className="flex items-center text-lg">
                                    <span className="text-emerald-200 mr-4 text-xl">✓</span>
                                    친환경 컨셉의 예금상품
                                </li>
                                <li className="flex items-center text-lg">
                                    <span className="text-emerald-200 mr-4 text-xl">✓</span>
                                    대용량 예치 가능
                                </li>
                            </ul>
                            <button className="bg-gradient-to-r from-teal-400 to-emerald-400 hover:from-teal-300 hover:to-emerald-300 px-10 py-4 rounded-full text-lg font-bold shadow-xl transform hover:-translate-y-1 transition-all duration-300">
                                비교하기
                            </button>
                        </div>
                    </div>
                </div>
            </AnimatedSection>

            {/* Statistics Section */}
            <AnimatedSection id="stats" className="py-24 bg-gradient-to-br from-bank-dark to-bank-secondary">
                <div className="max-w-7xl mx-auto px-6">
                    <div className="grid grid-cols-2 lg:grid-cols-4 gap-12 text-center text-white">
                        <div>
                            <h3 className="text-5xl md:text-6xl font-bold mb-4" style={{color: '#70FFD9'}}>2개</h3>
                            <p className="text-xl opacity-90">대형 은행 제휴</p>
                        </div>
                        <div>
                            <h3 className="text-5xl md:text-6xl font-bold mb-4" style={{color: '#70FFD9'}}>4%</h3>
                            <p className="text-xl opacity-90">최대 연금리</p>
                        </div>
                        <div>
                            <h3 className="text-5xl md:text-6xl font-bold mb-4" style={{color: '#70FFD9'}}>5000만원</h3>
                            <p className="text-xl opacity-90">예금보호 한도</p>
                        </div>
                        <div>
                            <h3 className="text-5xl md:text-6xl font-bold mb-4" style={{color: '#70FFD9'}}>10억원</h3>
                            <p className="text-xl opacity-90">최대 예치한도</p>
                        </div>
                    </div>
                </div>
            </AnimatedSection>

            {/* Final CTA Section */}
            <AnimatedSection id="cta" className="py-24 bg-gradient-to-br from-bank-success to-bank-primary">
                <div className="max-w-6xl mx-auto px-6 text-center text-white">
                    <h2 className="text-4xl md:text-5xl font-bold mb-8">💡 나에게 맞는 상품을 찾아보세요!</h2>
                    <p className="text-xl mb-12 opacity-90 leading-relaxed">
                        국내 18개 은행의 우수한 예적금 상품을 비교해보고<br />
                        최적의 저축 계획을 세워보세요
                    </p>
                    <div className="flex flex-col sm:flex-row gap-6 justify-center">
                        <button className="border-2 border-emerald-200 hover:bg-emerald-200 hover:text-emerald-800 px-12 py-4 rounded-full text-xl font-bold transition-all duration-300">
                            상품 비교하기
                        </button>
                        <button className="bg-gradient-to-r from-teal-400 to-emerald-400 hover:from-teal-300 hover:to-emerald-300 px-12 py-4 rounded-full text-xl font-bold shadow-xl transform hover:-translate-y-1 transition-all duration-300">
                            지금 시뮬레이션
                        </button>
                    </div>
                </div>
            </AnimatedSection>
        </div>
    );
};

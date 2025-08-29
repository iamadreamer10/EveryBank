import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import type {LoginRequest} from "../../types/user.ts";
import {useAuth} from "../../context/AuthContext.tsx";
import {useUserStore} from "../../stores/userStore.ts";

async function loginApi(loginData: LoginRequest): Promise<any> {
    const response = await fetch('http://localhost:8080/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginData)
    });

    if (!response.ok) throw new Error('로그인 실패');
    return response.json();
}



export default function LoginPage() {
    const navigate = useNavigate();
    const {login} = useAuth();
    const [formData, setFormData] = useState({
        email: '',
        password: ''
    });
    const setUser = useUserStore((state) => state.setUser);



    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    // handleSubmit 함수 수정
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            const result = await loginApi(formData);
            let accessToken = result.result.access;
            let refreshToken =result.result.refresh;

            if (accessToken.startsWith('Bearer ')) {
                accessToken = accessToken.replace('Bearer ', '');
            }

            if (refreshToken.startsWith('Bearer ')) {
                refreshToken = refreshToken.replace('Bearer ', '');
            }


            login(accessToken);
            // Refresh Token은 httpOnly 쿠키가 가장 안전하지만,
            // 불가능하면 localStorage 사용
            if (refreshToken) {
                localStorage.setItem('refreshToken', refreshToken);

            }
            alert("로그인 성공")
            console.log('로그인 성공', result);

            setUser({
                id: result.result.id,
                email: result.result.email,
                nickname: result.result.nickname
            });
            // 메인 페이지로 리다이렉트
            navigate('/'); // useNavigate 훅 필요

        } catch (error) {
            console.error('로그인 실패:', error);
            alert('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
        }
    };

    return (
        <div className="max-w-md mx-auto px-4 py-12">
            <div className="bg-white rounded-lg shadow-lg p-8">
                <h1 className="text-2xl font-bold text-center text-gray-900 mb-8">로그인</h1>

                <form className="space-y-6" onSubmit={handleSubmit}>
                    {/* 이메일 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            이메일 주소
                        </label>
                        <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleInputChange}
                            placeholder="이메일 주소 입력"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary focus:border-transparent"
                            required
                        />
                    </div>

                    {/* 비밀번호 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            비밀번호
                        </label>
                        <input
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleInputChange}
                            placeholder="비밀번호 입력"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary focus:border-transparent"
                            required
                        />
                    </div>

                    {/* 로그인 버튼 */}
                    <button
                        type="submit"
                        className="w-full bg-bank-primary text-white py-3 px-4 rounded-md font-medium hover:bg-bank-dark focus:outline-none focus:ring-2 focus:ring-bank-primary focus:ring-offset-2 transition-colors duration-200"
                    >
                        로그인
                    </button>

                    {/* 링크들 */}
                    <div className="text-right text-sm space-x-2">
                        <Link
                            to="/password-reset"
                            className="text-gray-600 hover:text-bank-primary transition-colors"
                        >
                            비밀번호찾기
                        </Link>
                        <span className="text-gray-400">|</span>
                        <Link
                            to="/signup"
                            className="text-gray-600 hover:text-bank-primary transition-colors"
                        >
                            회원가입
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}
import {useState} from 'react';
import type {SignupRequest} from "../../types/user.ts";
import {useNavigate} from "react-router-dom";


// 이메일 중복 확인 API 함수
async function checkEmailDuplicate(email: string): Promise<boolean> {
    console.log(email);
    const response = await fetch(`http://localhost:8080/email_check/${email}`, {
        method: 'GET'
    });

    if (!response.ok) throw new Error('이메일 중복 확인 실패');
    const result = await response.json();
    return result.result.isAvailable; // API 응답 구조에 맞게 수정
}


async function signup(userData: SignupRequest): Promise<any> {
    const response = await fetch('http://localhost:8080/join', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData)
    });

    if (!response.ok) throw new Error('회원가입 실패');
    return response.json();
}


export default function SignupPage() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        nickname: '',
        birthdate: ''
    });

    const [emailCheckStatus, setEmailCheckStatus] = useState<'unchecked' | 'checking' | 'available' | 'duplicate'>('unchecked');

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));

        // 이메일 변경 시 중복 확인 상태 초기화
        if (name === 'email') {
            setEmailCheckStatus('unchecked');
        }
    };

// form onSubmit 핸들러 추가
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if(emailCheckStatus != 'available'){
            alert('이메일 인증이 완료되지 않았습니다.');
            return;
        }

        try {
            const result = await signup(formData);
            alert("회원가입 성공")
            console.log('회원가입 성공:', result);
            navigate('/login'); // useNavigate 훅 필요

            // 성공 메시지 표시하거나 로그인 페이지로 이동
        } catch (error) {
            console.error('회원가입 실패:', error);
            // 에러 메시지 표시
        }
    };

    // 이메일 중복 확인 핸들러
    const handleEmailCheck = async () => {
        if (!formData.email) {
            alert('이메일을 입력해주세요.');
            return;
        }

        try {
            setEmailCheckStatus('checking');
            const isAvailable = await checkEmailDuplicate(formData.email);
            console.log("사용 가능: " ,isAvailable);
            if (!isAvailable) {
                setEmailCheckStatus('duplicate');
                alert('이미 사용 중인 이메일입니다.');
            } else {
                setEmailCheckStatus('available');
                alert('사용 가능한 이메일입니다.');
            }
        } catch (error) {
            console.error('이메일 확인 실패:', error);
            setEmailCheckStatus('unchecked');
            alert('이메일 확인 중 오류가 발생했습니다.');
        }
    };

    return (
        <div className="max-w-md mx-auto px-4 py-12">
            <div className="bg-white rounded-lg shadow-lg p-8">
                <h1 className="text-2xl font-bold text-center text-gray-900 mb-8">회원가입</h1>

                <form className="space-y-6" onSubmit={handleSubmit}>
                    {/* 이메일 주소 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            이메일 주소
                        </label>
                        <div className="flex gap-2">
                            <input
                                type="email"
                                name="email"
                                value={formData.email}
                                onChange={handleInputChange}
                                placeholder="이메일 주소 입력"
                                className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary focus:border-transparent"
                            />
                            <button
                                type="button"
                                onClick={handleEmailCheck}
                                disabled={emailCheckStatus === 'checking'}
                                className={`px-4 py-2 text-white text-sm font-medium rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary ${
                                    emailCheckStatus === 'checking'
                                        ? 'bg-gray-400 cursor-not-allowed'
                                        : 'bg-bank-primary hover:bg-bank-dark'
                                }`}
                            >
                                {emailCheckStatus === 'checking' ? '확인중...' : '이메일 중복'}
                            </button>
                        </div>
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
                        />
                    </div>

                    {/* 닉네임 확인 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            닉네임
                        </label>
                        <div className="flex gap-2">
                            <input
                                type="text"
                                name="nickname"
                                value={formData.nickname}
                                onChange={handleInputChange}
                                placeholder="닉네임 입력"
                                className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary focus:border-transparent"
                            />
                        </div>
                    </div>

                    {/* 생년월일 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            생년월일
                        </label>
                        <input
                            type="date"
                            name="birthdate"
                            value={formData.birthdate}
                            onChange={handleInputChange}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary focus:border-transparent"
                            required
                        />
                    </div>

                    {/* 제출 버튼 */}
                    <button
                        type="submit"
                        className="w-full bg-bank-primary text-white py-3 px-4 rounded-md font-medium hover:bg-bank-dark focus:outline-none focus:ring-2 focus:ring-bank-primary focus:ring-offset-2 transition-colors duration-200"
                    >
                        회원가입
                    </button>
                </form>
            </div>
        </div>
    );
};
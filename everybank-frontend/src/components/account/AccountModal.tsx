import {useEffect, useState} from 'react';

interface AccountModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (company: Company) => Promise<void>; // Company 객체 전체를 전달
    title: string;
}

interface Company {
    companyCode: string;
    companyName: string;
}

// API 함수
const fetchCompanies = async (): Promise<Company[]> => {
    console.log('🔍 금융회사 목록 API 호출...');

    const response = await fetch('http://localhost:8080/company', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${sessionStorage.getItem("accessToken")}`,
        }
    });

    if (!response.ok) {
        throw new Error('금융회사 목록 조회 실패');
    }

    const result = await response.json();
    console.log('📦 API 응답:', result);

    // 다양한 응답 구조 처리
    let companiesData = [];

    if (result.data && Array.isArray(result.data)) {
        companiesData = result.data;
        console.log('✅ result.data에서 데이터 발견');
    } else if (result.result && Array.isArray(result.result)) {
        companiesData = result.result;
        console.log('✅ result.result에서 데이터 발견');
    } else if (Array.isArray(result)) {
        companiesData = result;
        console.log('✅ result 자체가 배열');
    } else {
        console.warn('⚠️ 알려진 구조에서 배열을 찾을 수 없음:', result);
        companiesData = [];
    }

    console.log('✅ 최종 반환할 데이터:', companiesData);
    return companiesData;
};

export default function AccountModal({ isOpen, onClose, onSubmit, title }: AccountModalProps) {
    const [selectedBank, setSelectedBank] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [companies, setCompanies] = useState<Company[]>([]);
    const [isLoadingCompanies, setIsLoadingCompanies] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // 금융회사 목록 가져오기
    const loadCompanies = async () => {
        setIsLoadingCompanies(true);
        setError(null);

        try {
            const data = await fetchCompanies();
            console.log('✅ 받은 데이터:', data);
            setCompanies(data);
        } catch (err) {
            console.error('❌ 금융회사 목록 로드 실패:', err);
            setError(err instanceof Error ? err.message : '알 수 없는 에러');
        } finally {
            setIsLoadingCompanies(false);
        }
    };

    useEffect(() => {
        if (isOpen) {
            loadCompanies();
        }
    }, [isOpen]);

    if (!isOpen) return null;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!selectedBank) {
            alert('금융회사를 선택해주세요.');
            return;
        }

        // 선택된 회사 정보 찾기
        const selectedCompany = companies.find(company => company.companyCode === selectedBank);
        if (!selectedCompany) {
            alert('선택된 금융회사 정보를 찾을 수 없습니다.');
            return;
        }

        setIsLoading(true);

        try {
            await onSubmit(selectedCompany); // 전체 Company 객체 전달
            setSelectedBank('');
        } catch (error) {
            console.error('등록 실패:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleBackdropClick = (e: React.MouseEvent) => {
        if (e.target === e.currentTarget) {
            onClose();
        }
    };

    return (
        <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={handleBackdropClick}
        >
            <div className="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
                {/* 헤더 */}
                <div className="flex items-center justify-between p-6 border-b">
                    <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {/* 폼 */}
                <form onSubmit={handleSubmit} className="p-6">
                    <div className="mb-6">
                        <div className="flex items-center justify-between mb-3">
                            <label className="block text-sm font-medium text-gray-700">
                                금융회사 선택
                            </label>
                            {error && (
                                <button
                                    type="button"
                                    onClick={loadCompanies}
                                    className="text-sm text-bank-primary hover:text-bank-dark"
                                >
                                    다시 시도
                                </button>
                            )}
                        </div>
                        <select
                            value={selectedBank}
                            onChange={(e) => setSelectedBank(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bank-primary"
                            disabled={isLoadingCompanies}
                        >
                            <option value="">
                                {isLoadingCompanies ? '로딩 중...' : error ? '로딩 실패' : '금융회사를 선택하세요'}
                            </option>
                            {companies.map((company) => (
                                <option key={company.companyCode} value={company.companyCode}>
                                    {company.companyName}
                                </option>
                            ))}
                        </select>
                        {error && (
                            <p className="mt-1 text-sm text-red-500">
                                {error}
                            </p>
                        )}
                    </div>

                    {/* 버튼 영역 */}
                    <div className="flex gap-3">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
                            disabled={isLoading}
                        >
                            취소
                        </button>
                        <button
                            type="submit"
                            className="flex-1 px-4 py-2 bg-bank-success text-white rounded-md hover:bg-bank-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                            disabled={isLoading || isLoadingCompanies || !selectedBank}
                        >
                            {isLoading ? '등록 중...' : '등록'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
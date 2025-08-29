import { createContext, useContext, useState, useEffect, ReactNode } from 'react';

interface AuthContextType {
    isAuthenticated: boolean;
    login: (token: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    useEffect(() => {
        // 초기 로드 시 토큰 확인
        const token = sessionStorage.getItem('accessToken');
        setIsAuthenticated(!!token);
    }, []);

    const login = (token: string) => {
        sessionStorage.setItem('accessToken', token);
        setIsAuthenticated(true);
    };

    const logout = async () => {
        try {
            if (!sessionStorage.getItem('accessToken')) {
                throw new Error('액세스 토큰이 없습니다.');
            }


            // 로그아웃 API 호출
            await fetch('http://localhost:8080/service-logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json'
                }
            });

            // console.log("로그아웃 성공: ", result)
        } catch (error) {
            console.error('로그아웃 API 실패:', error);
        } finally {
            // API 성공/실패와 관계없이 로컬 토큰 제거
            sessionStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            setIsAuthenticated(false);
        }
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
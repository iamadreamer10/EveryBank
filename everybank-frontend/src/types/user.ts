
export interface SignupRequest {
    email: string;
    password: string;
    nickname: string;
    birthdate: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}
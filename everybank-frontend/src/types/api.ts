export interface ApiResponse<T> {
    message: string;
    status: number;
    result: T;
}
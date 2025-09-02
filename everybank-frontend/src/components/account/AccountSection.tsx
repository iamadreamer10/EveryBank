import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import AccountItem from "./AccountItem.tsx";
import AccountModal from "./AccountModal.tsx";
import type {AccountSectionProps} from "../../types/account.ts";

interface Company {
    companyCode: string;
    companyName: string;
}

interface ExtendedAccountSectionProps extends AccountSectionProps {
    onAccountCreated?: () => void; // 계좌 생성 후 콜백
}

export default function AccountSection({title, accounts, sectionType, onAccountCreated}: ExtendedAccountSectionProps) {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const queryClient = useQueryClient();

    if (accounts.length === 0) return null;

    const handleOpenModal = () => {
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
    };

    const handleAccountRegistered = async (company: Company) => { // Company 객체 받기
        try {
            const requestBody = {
                companyCode: company.companyCode,
                bankName: company.companyName
            };

            console.log('📤 전송할 데이터:', requestBody);

            // API 호출
            const response = await fetch('http://localhost:8080/my_account/check/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${sessionStorage.getItem("accessToken")}`
                },
                body: JSON.stringify(requestBody)
            });

            if (response.ok) {
                const result = await response.json();
                console.log('계좌 등록 성공:', result);

                // 모달 닫기
                setIsModalOpen(false);

                // 성공 메시지
                alert('계좌가 성공적으로 등록되었습니다!');

                // 계좌 목록 새로고침
                if (onAccountCreated) {
                    onAccountCreated();
                }

                // React Query 캐시 무효화 (필요시)
                queryClient.invalidateQueries({ queryKey: ['accounts'] });

            } else {
                throw new Error('계좌 등록에 실패했습니다.');
            }
        } catch (error) {
            console.error('API 호출 에러:', error);
            alert('계좌 등록 중 오류가 발생했습니다.');
        }
    };

    return (
        <>
            <div className="mb-12">
                <div className="flex items-center mb-6">
                    <h2 className="text-xl font-semibold text-bank-primary mr-4">{title}</h2>
                    {sectionType === 'CHECK' && (
                        <button
                            onClick={handleOpenModal}
                            className="px-4 py-2 bg-bank-success text-white rounded-md hover:bg-bank-dark text-sm"
                        >
                            개설
                        </button>
                    )}
                </div>

                <div className="space-y-4">
                    {accounts.map((account) => (
                        <AccountItem
                            key={account.accountId}
                            account={account}
                            sectionType={sectionType}
                        />
                    ))}
                </div>
            </div>

            {/* 계좌 등록 모달 */}
            <AccountModal
                isOpen={isModalOpen}
                onClose={handleCloseModal}
                onSubmit={handleAccountRegistered}
                title="계좌 등록"
            />
        </>
    );
}
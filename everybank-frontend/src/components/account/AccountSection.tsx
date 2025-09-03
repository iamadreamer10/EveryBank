import AccountItem from "./AccountItem.tsx";
import type {AccountSectionProps} from "../../types/account.ts";



interface ExtendedAccountSectionProps extends AccountSectionProps {
    onAccountCreated?: () => void; // 계좌 생성 후 콜백
}

export default function AccountSection({title, accounts, sectionType}: ExtendedAccountSectionProps) {

    if (accounts.length === 0) return null;

    return (
        <>
            <div className="mb-12">
                <div className="flex items-center mb-6">
                    <h2 className="text-xl font-semibold text-bank-primary mr-4">{title}</h2>
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
        </>
    );
}
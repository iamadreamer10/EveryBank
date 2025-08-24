interface ProductTabsProps {
    selectedTab: string;
    onTabChange: (tab: string) => void;
}

export default function ProductTabs({ selectedTab, onTabChange }: ProductTabsProps) {
    const tabs = ['정기예금상품', '적금상품', '비교하기'];

    return (
        <div className="mb-6">
            <div className="border-b border-gray-200">
                <nav className="flex space-x-8">
                    {tabs.map((tab) => (
                        <button
                            key={tab}
                            className={`py-2 px-1 border-b-2 font-medium text-sm ${
                                selectedTab === tab
                                    ? 'border-bank-primary text-bank-primary'
                                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                            }`}
                            onClick={() => onTabChange(tab)}
                        >
                            {tab}
                        </button>
                    ))}
                </nav>
            </div>
        </div>
    );

}

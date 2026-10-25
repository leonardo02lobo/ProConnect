import { useEffect } from "react";
import { useStore } from "@nanostores/react";
import Search from "./Search";
import { $user, loadSession, logout } from "../Service/session";

function Header() {
    const user = useStore($user);

    useEffect(() => {
        loadSession();
    }, []);

    if (user) {
        return (
            <>
                <div className="flex flex-column justify-between items-center p-2">
                    <a href="/">
                        <img src="/assets/ProConnect.webp" alt="Proconnect" className="w-16 rounded-3xl" />
                    </a>
                    <Search />
                    <div className="flex flex-column justify-start items-center">
                        <details className="relative">
                            <summary
                                data-testid="header-user-menu"
                                className="flex items-center gap-3 cursor-pointer rounded-full px-3 py-1 hover:bg-gray-100"
                            >
                                <span className="text-sm font-medium text-gray-800" id="header-user-name">
                                    {user?.name || "User"}
                                </span>
                            </summary>

                            <div className="absolute right-0 mt-2 w-44 bg-white rounded-lg shadow-lg ring-1 ring-black ring-opacity-5 z-20">
                                <a
                                    href="/Profile"
                                    data-testid="header-profile-link"
                                    className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                                >
                                    Profile
                                </a>
                                <button
                                    id="logout-button"
                                    data-testid="header-logout"
                                    onClick={() => {
                                        logout();
                                    }}
                                    className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100"
                                >
                                    Log out
                                </button>
                            </div>
                        </details>
                    </div>
                </div>
            </>
        );
    } else {
        return (
            <>
                <div className="flex flex-column justify-between items-center p-2">
                    <a href="/">
                        <img src="/assets/ProConnect.webp" alt="Proconnect" className="w-16 rounded-3xl" />
                    </a>
                    <Search />
                    <div className="flex flex-column justify-start items-center">
                        <a
                            href="/login"
                            className="bg-blue-600 text-white px-6 py-3 rounded-xl hover:bg-blue-700 transition"
                        >
                            Log in
                        </a>
                    </div>
                </div>
            </>
        );
    }
}

export default Header;

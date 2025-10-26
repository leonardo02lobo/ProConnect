import { useState } from "react";
import Search from "./Search";
import { getUserFromCookie } from "../Service/GetCookie";

function Header() {
    
    const [user, setUser] = useState(getUserFromCookie);

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
                            <summary className="flex items-center gap-3 cursor-pointer rounded-full px-3 py-1 hover:bg-gray-100">
                                <span className="text-sm font-medium text-gray-800">{user?.name || "User"}</span>
                            </summary>

                            <div className="absolute right-0 mt-2 w-44 bg-white rounded-lg shadow-lg ring-1 ring-black ring-opacity-5 z-20">
                                <a
                                    href="/Profile"
                                    className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                                >
                                    Profile
                                </a>
                                <button
                                    onClick={() => {
                                        document.cookie = "user=; Max-Age=0; path=/";
                                        window.location.href = "/";
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
                        <img src="assets/ProConnect.webp" alt="Proconnect" className="w-16 rounded-3xl" />
                    </a>
                    <Search />
                    <div className="flex flex-column justify-start items-center">
                        <a href="/SignUp"
                            className="bg-blue-600 text-white px-6 py-3 rounded-xl hover:bg-blue-700 transition"
                        >
                            Sign Up
                        </a>
                    </div>
                </div>
            </>
        );
    }
}

export default Header;
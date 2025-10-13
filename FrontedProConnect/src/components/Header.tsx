import { useState } from "react";
import Search from "./Search";

function Header() {
    const getUserFromCookie = () => {
        const cookie = document.cookie.split("; ")

        const userCookie = cookie.find(row => row.startsWith("user="))
        if (userCookie) {
            try {
                const value = userCookie.split("=")[1]
                return JSON.parse(decodeURIComponent(value))
            } catch (err) {
                console.error("Error al parsear: " + err)
                return null
            }
        }
        return null
    }
    const [user, setUser] = useState(getUserFromCookie);

    if (user) {
        return (
            <>
                <div className="flex flex-column justify-between items-center p-2">
                    <a href="/">
                        <img src="assets/ProConnect.webp" alt="Proconnect" className="w-16 rounded-3xl" />
                    </a>
                    <Search />
                    <div className="flex flex-column justify-start items-center">
                        <h2>Sesion Active</h2>
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
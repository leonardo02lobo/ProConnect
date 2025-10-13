import { useState } from "react";

function Search() {
    const [query, setQuery] = useState("");

    const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter" && query.trim() !== "") {
            //REDIRECCION
            console.log(query)
        }
    };
    return (
        <>
            <input
                type="text"
                placeholder="Buscar..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={handleKeyPress}
                className="w-full/2 border border-gray-300 rounded-xl p-3 text-lg outline-none focus:border-blue-500"
            />
        </>
    );
}

export default Search;
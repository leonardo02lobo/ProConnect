import { useState } from "react";
import type React from "react";

interface SearchProps {
    initialQuery?: string;
}

/** Buscador de usuarios del header: Enter lleva a /search?q=... */
function Search({ initialQuery = "" }: SearchProps) {
    const [query, setQuery] = useState(initialQuery);

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter" && query.trim() !== "") {
            window.location.href = `/search?q=${encodeURIComponent(query.trim())}`;
        }
    };

    return (
        <input
            type="search"
            placeholder="Buscar..."
            aria-label="Buscar usuarios"
            data-testid="header-search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={handleKeyDown}
            className="w-full/2 border border-gray-300 rounded-xl p-3 text-lg outline-none focus:border-blue-500"
        />
    );
}

export default Search;

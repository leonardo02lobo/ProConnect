import { useState, useEffect } from "react";
import { appendUnique } from "../../Service/pagination";
import { avatarUrl, mediaUrl } from "../../Service/http";
import { loadSession } from "../../Service/session";
import { usersService } from "../../Service/users";
import type { UserProfileDTO } from "../../types/Dtos";
import Post from "../Post";

interface HeaderProfileProps {
    /** Perfil ya cargado por SSR (página /user/[userId]). Si falta, se carga el de la sesión. */
    initialUser?: UserProfileDTO | null;
}

function HeaderProfile({ initialUser }: HeaderProfileProps) {
    const [user, setUser] = useState<UserProfileDTO | null>(initialUser ?? null);
    const [loadingMore, setLoadingMore] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Página siguiente de posts del perfil por cursor.
    async function loadMore() {
        if (!user?.nextCursor || loadingMore) return;
        setLoadingMore(true);
        try {
            const page = await usersService.posts(user.id, user.nextCursor);
            setUser((current) =>
                current
                    ? { ...current, posts: appendUnique(current.posts, page.items), nextCursor: page.nextCursor }
                    : current,
            );
            setError(null);
        } catch {
            setError("No se pudieron cargar más publicaciones");
        } finally {
            setLoadingMore(false);
        }
    }

    useEffect(() => {
        if (initialUser) {
            return;
        }
        let mounted = true;
        loadSession()
            .then((me) => {
                if (!me) {
                    window.location.href = "/login";
                    return null;
                }
                return usersService.profile(me.id);
            })
            .catch(() => null)
            .then((profile) => {
                if (mounted) setUser(profile);
            });
        return () => {
            mounted = false;
        };
    }, [initialUser]);
    return (
        <div className="flex flex-col justify-center items-center bg-white">
            <header className="relative w-full">
                <a href={mediaUrl(user?.coverUrl)} target="_blank">
                    <img src={mediaUrl(user?.coverUrl)} alt="Portada" className="w-full h-48 object-cover" />
                </a>
                <a href={mediaUrl(user?.avatarUrl)} target="_blank">
                    <img
                        src={avatarUrl(user?.avatarUrl)}
                        alt="Foto de perfil"
                        className="w-24 h-24 rounded-full object-cover border-4 border-white absolute left-1/2 -translate-x-1/2 -translate-y-1/2 bottom-0 z-10"
                    />
                </a>
            </header>

            <div className="w-full max-w-4xl px-4 py-6">
                <div className="text-center mb-4">
                    <h1 className="text-2xl font-bold text-gray-900" data-testid="profile-name">
                        {user?.name}
                    </h1>
                    <p className="text-gray-600 mt-1">8 amigos • 8 amigos en común</p>
                </div>

                <div className="flex justify-center gap-2 mb-6">
                    <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded font-medium transition-colors">
                        Amigos
                    </button>
                    <button className="bg-gray-200 hover:bg-gray-300 text-gray-800 px-4 py-2 rounded font-medium transition-colors">
                        Mensaje
                    </button>
                </div>

                <nav className="border-t border-gray-200 pt-4">
                    <div className="flex justify-between items-center">
                        <div className="flex space-x-6">
                            <a href="#" className="text-blue-600 font-medium border-b-2 border-blue-600 pb-2">
                                Publicaciones
                            </a>
                            <a href="#" className="text-gray-600 hover:text-blue-600 font-medium pb-2">
                                Información
                            </a>
                            <a href="#" className="text-gray-600 hover:text-blue-600 font-medium pb-2">
                                Fotos
                            </a>
                            <a href="#" className="text-gray-600 hover:text-blue-600 font-medium pb-2">
                                Amigos
                            </a>
                        </div>

                        <div className="flex items-center space-x-4">
                            <a href="#" className="text-gray-600 hover:text-blue-600 text-sm">
                                ...
                            </a>
                            <a href="#" className="text-gray-600 hover:text-blue-600 text-sm">
                                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth={2}
                                        d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                                    />
                                </svg>
                            </a>
                        </div>
                    </div>
                </nav>
                <div>
                    {user?.posts.map((post) => (
                        <Post key={post.id} post={post} />
                    ))}
                    {error && (
                        <p role="alert" data-testid="profile-error" className="text-red-600 my-4 text-center">
                            {error}
                        </p>
                    )}
                    {user?.nextCursor && (
                        <div className="flex justify-center">
                            <button
                                type="button"
                                data-testid="profile-load-more"
                                onClick={() => void loadMore()}
                                disabled={loadingMore}
                                className="mb-8 px-6 py-2 rounded-full bg-white border border-gray-300 text-gray-700 hover:bg-gray-100 disabled:opacity-50"
                            >
                                {loadingMore ? "Cargando..." : "Cargar más"}
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
export default HeaderProfile;

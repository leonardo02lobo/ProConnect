import { useCallback, useEffect, useState } from "react";
import type { FeedPostDTO } from "../types/Dtos";
import { postsService } from "../Service/posts";
import { appendUnique } from "../Service/pagination";
import Post from "./Post";
import AddPost from "./profile/AddPost";

function Main() {
    const [posts, setPosts] = useState<FeedPostDTO[]>([]);
    const [nextCursor, setNextCursor] = useState<string | null>(null);
    const [loadingMore, setLoadingMore] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Primera página (al entrar y tras publicar).
    const loadFeed = useCallback(async () => {
        try {
            const page = await postsService.feed();
            setPosts(page.items);
            setNextCursor(page.nextCursor);
            setError(null);
        } catch {
            setError("No se pudo cargar el feed");
        }
    }, []);

    // Página siguiente por cursor: estable aunque se publiquen posts mientras tanto.
    async function loadMore() {
        if (!nextCursor || loadingMore) return;
        setLoadingMore(true);
        try {
            const page = await postsService.feed(nextCursor);
            setPosts((current) => appendUnique(current, page.items));
            setNextCursor(page.nextCursor);
            setError(null);
        } catch {
            setError("No se pudieron cargar más publicaciones");
        } finally {
            setLoadingMore(false);
        }
    }

    useEffect(() => {
        void loadFeed();
    }, [loadFeed]);

    return (
        <section className="flex flex-col justify-center items-center">
            <AddPost onCreated={loadFeed} />
            {error && (
                <p role="alert" data-testid="feed-error" className="text-red-600 my-4">
                    {error}
                </p>
            )}
            {posts.map((post) => (
                <Post key={post.id} post={post} />
            ))}
            {nextCursor && (
                <button
                    type="button"
                    data-testid="feed-load-more"
                    onClick={() => void loadMore()}
                    disabled={loadingMore}
                    className="mb-8 px-6 py-2 rounded-full bg-white border border-gray-300 text-gray-700 hover:bg-gray-100 disabled:opacity-50"
                >
                    {loadingMore ? "Cargando..." : "Cargar más"}
                </button>
            )}
        </section>
    );
}

export default Main;

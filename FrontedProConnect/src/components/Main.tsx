import { useCallback, useEffect, useState } from "react";
import type { FeedPostDTO } from "../types/Dtos";
import { postsService } from "../Service/posts";
import Post from "./Post";
import AddPost from "./profile/AddPost";

function Main() {
    const [posts, setPosts] = useState<FeedPostDTO[]>([]);
    const [error, setError] = useState<string | null>(null);

    const loadFeed = useCallback(async () => {
        try {
            setPosts(await postsService.feed());
            setError(null);
        } catch {
            setError("No se pudo cargar el feed");
        }
    }, []);

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
        </section>
    );
}

export default Main;

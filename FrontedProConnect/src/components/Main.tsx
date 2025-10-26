import { useEffect, useState } from "react";
import type { PostDTO } from "../types/Dtos";
import { PostsApi } from "../Service/PostApi";
import Post from "./Post";
import AddPost from "./profile/AddPost";

function RMain() {
    const [posts, setPosts] = useState<PostDTO[]>([])

    useEffect(() => {
        const fetchPosts = async () => {
            try {
                const response = await PostsApi.Posts();
                if (!response.ok) {
                    let errorBody: unknown;
                    try {
                        errorBody = await response.json();
                    } catch {
                        errorBody = await response.text();
                    }
                    throw new Error(`HTTP ${response.status}: ${JSON.stringify(errorBody)}`);
                }
                const posts: PostDTO[] = await response.json();
                setPosts(posts);
            } catch (error) {
                console.error("Error al obtener posts:", error);
                setPosts([]);
                window.location.href = "/SignUp"
            }
        };
        fetchPosts();
    }, [])

    return (
        <section className="flex flex-col justify-center items-center">
            <AddPost />
            {
                posts.map(post => (
                    <Post
                        key={post.id}
                        id={post.id}
                        title={post.title}
                        content={post.content}
                        imageUrl={post.imageUrl}
                        userId={post.user?.id ?? 0}
                        name={post.user?.name ?? ""}
                        photoUrl={post.user?.profilePhotosDto.photoUrl ?? ""}
                        totalLikes={post.totalLikes}
                        totalComentarios={post.totalComentarios}
                        userLiked={post.userLiked}
                        comment={post.comments}
                    />
                ))
            }
        </section>
    );
}

export default RMain
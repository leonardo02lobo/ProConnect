import type React from "react";
import Comment from "./Comment";
import type { CommentsDTO } from "../types/Dtos";
import { useState } from "react";
import { apis } from "../Service/LikeApi";
import { PostsApi } from "../Service/PostApi";
import { mediaUrl } from "../Service/http";

interface Post {
    id: number;
    title: string;
    content: string;
    imageUrl: string;
    userId: number;
    name: string;
    photoUrl?: string | null;
    totalLikes: number;
    totalComentarios: number;
    userLiked?: boolean;
    comment: CommentsDTO[];
}

const Post: React.FC<Post> = ({ id, title, content, imageUrl,userId, name, photoUrl, totalLikes, totalComentarios, userLiked, comment }) => {

    const [newComment,setNewComment] = useState<string>("")

    async function SendComment(){
        if (!newComment.trim()) {
            console.log("El comentario está vacío");
            return;
        }
        try {
            const response = await PostsApi.Comment(id, newComment)

            if (!response.ok) {
                const errorText = await response.text();
                console.error("Error del servidor:", errorText);
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            if(response.ok){
                window.location.reload()
            }
        } catch (error) {
            console.log((error as Error).message)
        }

        console.log("Enviando comentario:", newComment);
        console.log("Para el post ID:", id);
        setNewComment("")
    }

    const SystemLike = async () => {
        try {
            if(!userLiked){
                await apis().AddLike(id)
            }else{
                await apis().RemoveLike(id)
            }
        } catch (error) {
            console.log((error as Error).message)
        }
        window.location.reload()
    }

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setNewComment(e.target.value)
    }
    
    const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            SendComment();
        }
    } 

    return (
        <section data-testid="post-card" data-post-id={id} className="bg-white rounded-2xl shadow-md p-5 w-full max-w-lg mx-auto mb-6 transition hover:shadow-xl">
            <div className="flex items-center gap-3 mb-4">
                <img
                    src={mediaUrl(photoUrl) ?? "/assets/avatar-default.svg"}
                    alt={`Foto de ${name}`}
                    data-testid="post-author-photo"
                    className="w-12 h-12 rounded-full object-cover border"
                />
                <a href={`/user/${userId}`} data-testid="post-author"><p className="font-semibold text-lg">{name}</p></a>
            </div>
            <div className="mb-4">
                <p className="text-gray-800 mb-3 text-2xl" data-testid="post-title">
                    {title}
                </p>
                <p className="text-gray-800 mb-3">
                    {content}
                </p>
                {!imageUrl ?
                    <br />
                    :
                    <img
                        src={mediaUrl(imageUrl)}
                        alt={title}
                        data-testid="post-image"
                        className="rounded-xl max-h-80 w-full object-cover"
                    />
                }
            </div>
            <div className="flex justify-around py-2 border-t border-gray-200 text-gray-600">
                <button 
                    className="flex items-center gap-2 hover:text-red-500 transition cursor-pointer"
                    onClick={SystemLike}
                    data-testid="post-like-button"
                    aria-pressed={!!userLiked}
                    aria-label={userLiked ? "Quitar me gusta" : "Me gusta"}
                >
                    {userLiked ? '❤️' : '♡'}
                    <span data-testid="post-like-count">{totalLikes}</span>
                </button>
                <button className="flex items-center gap-2 hover:text-blue-500 transition">
                    💬 <span data-testid="post-comment-count">{totalComentarios}</span>
                </button>
                {/* <button className="flex items-center gap-2 hover:text-green-500 transition">
                        🔗 Compartir
                    </button> */}
            </div>
            <div className="mt-4 flex flex-col gap-2">
                {
                    comment.map(com => (
                        <Comment
                            key={com.id}
                            content={com.content}
                            date={com.date}
                            id={com.id}
                            postId={com.postId}
                            user={com.user}
                            userId={com.userId}
                        />
                    ))
                }
            </div>
            <div className="mt-4 flex items-center gap-2 border-t pt-3">
                <input
                    type="text"
                    placeholder="Escribe un comentario..."
                    aria-label="Escribe un comentario"
                    data-testid="post-comment-input"
                    value={newComment}
                    onChange={handleInputChange}
                    onKeyDown={handleKeyPress}
                    className="flex-1 p-2 border rounded-xl outline-none focus:ring-2 focus:ring-blue-400"
                />
                <button 
                    className="bg-blue-500 text-white px-4 py-2 rounded-xl hover:bg-blue-600 transition"
                    onClick={SendComment}
                    data-testid="post-comment-submit"
                    disabled={!newComment.trim()}
                >
                    Enviar
                </button>
            </div>
        </section>
    );
}
export default Post;
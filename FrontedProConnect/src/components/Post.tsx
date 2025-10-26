import type React from "react";
import Comment from "./Comment";
import type { CommentsDTO } from "../types/Dtos";
import { useState } from "react";
import { apis } from "../Service/LikeApi";
import { getUserFromCookie } from "../Service/GetCookie";

interface Post {
    id: number;
    title: string;
    content: string;
    imageUrl: string;
    userId: number;
    name: string;
    photoUrl: string;
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
        const data = await getUserFromCookie()

        try {
            const response = await fetch("http://localhost:8080/api/comment/create",{
                method: "POST",
                headers: {
                    "Content-Type":"application/json"
                },
                body: JSON.stringify({
                    postId: id,
                    userId: data.id,
                    content: newComment
                })
            })

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
        const data = await getUserFromCookie()
        if(!userLiked){
            apis().AddLike(data.id, id)
        }else{
            apis().RemoveLike(data.id,id)
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
        <section className="bg-white rounded-2xl shadow-md p-5 w-full max-w-lg mx-auto mb-6 transition hover:shadow-xl">
            <div className="flex items-center gap-3 mb-4">
                <img
                    src={photoUrl ? `http://localhost:8080/upload/uploads/${photoUrl}` : "https://cdn-icons-png.flaticon.com/512/1946/1946429.png"}
                    alt="User"
                    className="w-12 h-12 rounded-full object-cover border"
                />
                <a href={`/user/${userId}`}><p className="font-semibold text-lg">{name}</p></a>
            </div>
            <div className="mb-4">
                <p className="text-gray-800 mb-3 text-2xl">
                    {title}
                </p>
                <p className="text-gray-800 mb-3">
                    {content}
                </p>
                {!imageUrl ?
                    <br />
                    :
                    <img
                        src={`http://localhost:8080/upload/uploads/${imageUrl}`}
                        alt="Post"
                        className="rounded-xl max-h-80 w-full object-cover"
                    />
                }
            </div>
            <div className="flex justify-around py-2 border-t border-gray-200 text-gray-600">
                <button 
                    className="flex items-center gap-2 hover:text-red-500 transition cursor-pointer"
                    onClick={SystemLike}
                >
                    {userLiked ? '❤️' : '♡'}
                    <span>{totalLikes}</span>
                </button>
                <button className="flex items-center gap-2 hover:text-blue-500 transition">
                    💬 <span>{totalComentarios}</span>
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
                    value={newComment}
                    onChange={handleInputChange}
                    onKeyDown={handleKeyPress}
                    className="flex-1 p-2 border rounded-xl outline-none focus:ring-2 focus:ring-blue-400"
                />
                <button 
                    className="bg-blue-500 text-white px-4 py-2 rounded-xl hover:bg-blue-600 transition"
                    onClick={SendComment}
                    disabled={!newComment.trim()}
                >
                    Enviar
                </button>
            </div>
        </section>
    );
}
export default Post;
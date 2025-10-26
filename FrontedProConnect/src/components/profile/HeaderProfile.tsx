import { useState, useEffect } from "react";
import { getUserFromCookie } from "../../Service/GetCookie";
import type { UserProfileDTO } from "../../types/Dtos";
import Post from "../Post";

function HeaderProfile() {
    async function GetData(): Promise<UserProfileDTO | null> {
        const dataUser = getUserFromCookie()

        if (!dataUser) {
            console.log(dataUser)
            window.location.href = "/SignUp"
            return null;
        } else {
            const response = await fetch(`http://localhost:8080/api/user/${dataUser.id}/profiles-posts`, {
                headers: {
                    "Content-type": "application/json"
                },
            })

            if (!response.ok) {
                return null
            }

            const data = await response.json()
            return data ?? null
        }
    }

    const [user, setUser] = useState<UserProfileDTO | null>(null)

    useEffect(() => {
        let mounted = true
        GetData().then(result => {
            if (mounted) setUser(result)
        })
        return () => {
            mounted = false
        }
        
    }, [])
    return (
        <div className="flex flex-col justify-center items-center bg-white">
            <header className="relative w-full">
                <a href={`http://localhost:8080/upload/uploads${user?.wallPhotoUrl}`} target="_blank">
                    <img
                        src={`http://localhost:8080/upload/uploads${user?.wallPhotoUrl}`}
                        alt="Portada"
                        className="w-full h-48 object-cover"
                    />
                </a>
                <a href={`http://localhost:8080/upload/uploads${user?.photoUrl}`} target="_blank">
                    <img
                        src={`http://localhost:8080/upload/uploads${user?.photoUrl}`}
                        alt="Foto de perfil"
                        className="w-24 h-24 rounded-full object-cover border-4 border-white absolute left-1/2 -translate-x-1/2 -translate-y-1/2 bottom-0 z-10"
                    />
                </a>
            </header>

            <div className="w-full max-w-4xl px-4 py-6">
                <div className="text-center mb-4">
                    <h1 className="text-2xl font-bold text-gray-900">{user?.name}</h1>
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
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                </svg>
                            </a>
                        </div>
                    </div>
                </nav>
                <div>
                    {user?.posts.map((post,i) => (
                        <Post 
                            key={post.id}
                            id={post.id}
                            title={post.title} 
                            content={post.content} 
                            date={post.date} 
                            imageUrl={post.imageUrl} 
                            userId={user.id}
                            name={user.name} 
                            photoUrl={user.photoUrl}  
                            totalComentarios={post.totalComentarios}
                            totalLikes={post.totalLikes}
                            userLiked={post.userLiked}
                            comment={post.comments}
                        />
                    ))}
                </div>
            </div>
        </div>
    );
}
export default HeaderProfile;
import type React from "react";
import type { CommentsDTO } from "../types/Dtos";

const Comment: React.FC<CommentsDTO> = ({content,date,id,user}) => {
    return (
        <div data-testid="comment-item" className="flex gap-3 items-start bg-gray-50 p-3 rounded-xl hover:bg-gray-100 transition">
            <img
                src="https://cdn-icons-png.flaticon.com/512/219/219983.png"
                alt="Avatar"
                className="w-10 h-10 rounded-full object-cover"
            />
            <div className="flex flex-col">
                <a href={`/user/${user?.id ?? ""}`}><p className="font-semibold text-sm" data-testid="comment-author">{user?.name ?? "Usuario"}</p></a>
                <p className="text-gray-700 text-sm">
                    {content}
                </p>
            </div>
        </div>
    );
}
export default Comment;
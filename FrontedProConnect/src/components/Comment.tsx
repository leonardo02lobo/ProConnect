import type { CommentDTO } from "../types/Dtos";
import { avatarUrl } from "../Service/http";

interface CommentProps {
    comment: CommentDTO;
    /** true mientras el comentario optimista aún no fue confirmado por el servidor. */
    pending?: boolean;
}

function Comment({ comment, pending = false }: CommentProps) {
    const { content, user } = comment;
    return (
        <div
            data-testid="comment-item"
            aria-busy={pending}
            className={`flex gap-3 items-start bg-gray-50 p-3 rounded-xl hover:bg-gray-100 transition ${pending ? "opacity-60" : ""}`}
        >
            <img
                src={avatarUrl(user?.photoUrl)}
                alt={`Foto de ${user?.name ?? "usuario"}`}
                className="w-10 h-10 rounded-full object-cover"
            />
            <div className="flex flex-col">
                <a href={`/user/${user?.id ?? comment.userId}`}>
                    <p className="font-semibold text-sm" data-testid="comment-author">
                        {user?.name ?? "Usuario"}
                    </p>
                </a>
                <p className="text-gray-700 text-sm">{content}</p>
            </div>
        </div>
    );
}

export default Comment;

import { useState } from "react";
import type React from "react";
import Comment from "./Comment";
import type { CommentDTO, FeedPostDTO } from "../types/Dtos";
import { ApiError, avatarUrl, mediaUrl } from "../Service/http";
import { likesService } from "../Service/likes";
import { commentsService } from "../Service/comments";
import { $user } from "../Service/session";

interface PostProps {
    post: FeedPostDTO;
}

interface ShownComment {
    comment: CommentDTO;
    pending: boolean;
}

let tempIds = 0;

function Post({ post }: PostProps) {
    const { id, title, content, imageUrl, user } = post;

    const [liked, setLiked] = useState<boolean>(post.userLiked);
    const [likeCount, setLikeCount] = useState<number>(post.totalLikes);
    const [likeBusy, setLikeBusy] = useState(false);
    const [comments, setComments] = useState<ShownComment[]>(
        post.comments.map((comment) => ({ comment, pending: false })),
    );
    const [commentCount, setCommentCount] = useState<number>(post.totalComments);
    const [newComment, setNewComment] = useState<string>("");
    const [error, setError] = useState<string | null>(null);

    // Like optimista: se actualiza al instante y se revierte si el servidor falla.
    async function toggleLike() {
        if (likeBusy) return;
        const wasLiked = liked;
        setLikeBusy(true);
        setError(null);
        setLiked(!wasLiked);
        setLikeCount((n) => n + (wasLiked ? -1 : 1));
        try {
            if (wasLiked) {
                await likesService.unlike(id);
            } else {
                await likesService.like(id);
            }
        } catch (e) {
            // 409 (ya tenía like) / 404 al quitar (ya no lo tenía): el estado optimista ya coincide con el servidor.
            const alreadyInSync = e instanceof ApiError && ((!wasLiked && e.status === 409) || (wasLiked && e.status === 404));
            if (!alreadyInSync) {
                setLiked(wasLiked);
                setLikeCount((n) => n + (wasLiked ? 1 : -1));
                setError("No se pudo actualizar el me gusta");
            }
        } finally {
            setLikeBusy(false);
        }
    }

    // Comentario optimista: aparece de inmediato y se reemplaza por el del servidor (o se retira si falla).
    async function sendComment() {
        const text = newComment.trim();
        if (!text) return;
        const me = $user.get() ?? null;
        const tempId = --tempIds;
        const optimistic: CommentDTO = {
            id: tempId,
            postId: id,
            userId: me?.id ?? 0,
            content: text,
            date: new Date().toISOString(),
            user: me,
        };
        setError(null);
        setNewComment("");
        setComments((list) => [...list, { comment: optimistic, pending: true }]);
        setCommentCount((n) => n + 1);
        try {
            const saved = await commentsService.create(id, text);
            setComments((list) =>
                list.map((item) => (item.comment.id === tempId ? { comment: saved, pending: false } : item)),
            );
        } catch {
            setComments((list) => list.filter((item) => item.comment.id !== tempId));
            setCommentCount((n) => n - 1);
            setNewComment(text);
            setError("No se pudo enviar el comentario");
        }
    }

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            void sendComment();
        }
    };

    return (
        <section
            data-testid="post-card"
            data-post-id={id}
            className="bg-white rounded-2xl shadow-md p-5 w-full max-w-lg mx-auto mb-6 transition hover:shadow-xl"
        >
            <div className="flex items-center gap-3 mb-4">
                <img
                    src={avatarUrl(user?.photoUrl)}
                    alt={`Foto de ${user?.name ?? "usuario"}`}
                    data-testid="post-author-photo"
                    className="w-12 h-12 rounded-full object-cover border"
                />
                <a href={`/user/${user?.id ?? post.userId}`} data-testid="post-author">
                    <p className="font-semibold text-lg">{user?.name ?? ""}</p>
                </a>
            </div>
            <div className="mb-4">
                <p className="text-gray-800 mb-3 text-2xl" data-testid="post-title">
                    {title}
                </p>
                <p className="text-gray-800 mb-3">{content}</p>
                {imageUrl ? (
                    <img
                        src={mediaUrl(imageUrl)}
                        alt={title}
                        data-testid="post-image"
                        className="rounded-xl max-h-80 w-full object-cover"
                    />
                ) : (
                    <br />
                )}
            </div>
            <div className="flex justify-around py-2 border-t border-gray-200 text-gray-600">
                <button
                    className="flex items-center gap-2 hover:text-red-500 transition cursor-pointer"
                    onClick={() => void toggleLike()}
                    data-testid="post-like-button"
                    aria-pressed={liked}
                    aria-label={liked ? "Quitar me gusta" : "Me gusta"}
                >
                    {liked ? "❤️" : "♡"}
                    <span data-testid="post-like-count">{likeCount}</span>
                </button>
                <span className="flex items-center gap-2">
                    💬 <span data-testid="post-comment-count">{commentCount}</span>
                </span>
            </div>
            {error && (
                <p role="alert" data-testid="post-error" className="text-sm text-red-600 mt-2">
                    {error}
                </p>
            )}
            <div className="mt-4 flex flex-col gap-2">
                {comments.map(({ comment, pending }) => (
                    <Comment key={comment.id} comment={comment} pending={pending} />
                ))}
            </div>
            <div className="mt-4 flex items-center gap-2 border-t pt-3">
                <input
                    type="text"
                    placeholder="Escribe un comentario..."
                    aria-label="Escribe un comentario"
                    data-testid="post-comment-input"
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className="flex-1 p-2 border rounded-xl outline-none focus:ring-2 focus:ring-blue-400"
                />
                <button
                    className="bg-blue-500 text-white px-4 py-2 rounded-xl hover:bg-blue-600 transition"
                    onClick={() => void sendComment()}
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

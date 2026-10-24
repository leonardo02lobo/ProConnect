import { useRef, useState } from "react";
import { PostsApi } from "../../Service/PostApi";

function AddPost() {
    const [content, setContent] = useState<string>("");
    const [title, setTitle] = useState<string>("");
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [isUploading, setIsUploading] = useState<boolean>(false);
    const fileInputRef = useRef<HTMLInputElement | null>(null);

    const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        setContent(e.target.value);
    };

    const handleTitleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setTitle(e.target.value);
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const files = e.target.files;
        if (files && files.length > 0) {
            setImageFile(files[0]);
        } else {
            setImageFile(null);
        }
    };

    // Disabled unless: title present AND (content OR image)
    const isDisabled =
        title.trim() === "" || (content.trim() === "" && imageFile === null);

    async function SendPost() {
        if (isDisabled || isUploading) return;
        setIsUploading(true);
        try {
            let resultFilename: string | null = null;

            if (imageFile) {
                const response = await PostsApi.UploadImage(imageFile);

                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(`Upload failed: ${response.status} ${errorText}`);
                }

                const result = await response.json();
                resultFilename = result?.data?.filename ?? null;
            }

            const response2 = await PostsApi.Create(content, title, resultFilename ?? "");
            if (response2.ok) {
                window.location.reload();
            } else {
                console.error("Create post failed", response2);
            }
        } catch (err) {
            console.error(err);
        } finally {
            setIsUploading(false);
        }
    }

    return (
        <section className="w-full max-w-lg bg-white border border-gray-200 rounded-xl shadow-sm p-4">
            <div className="flex gap-4">
                <img
                    src="https://cdn-icons-png.flaticon.com/512/1946/1946429.png"
                    alt="User"
                    className="w-12 h-12 rounded-full object-cover border"
                />
                <div className="flex-1">
                    <div>
                        <input
                            type="text"
                            value={title}
                            onChange={handleTitleChange}
                            placeholder="Que has pensado en el titulo?"
                            className="w-full bg-gray-50 border border-transparent focus:border-blue-300 focus:ring-2 focus:ring-blue-200 rounded-2xl p-3 resize-none min-h-[28px] max-h-48 text-sm text-gray-800"
                        />
                        <textarea
                            name="post"
                            id="post"
                            value={content}
                            onChange={handleContentChange}
                            placeholder="¿Qué estás pensando?"
                            className="w-full bg-gray-50 border border-transparent focus:border-blue-300 focus:ring-2 focus:ring-blue-200 rounded-2xl p-3 resize-none min-h-[88px] max-h-48 text-sm text-gray-800"
                        />
                    </div>
                    <div className="mt-3 flex items-center justify-between gap-2">
                        <div className="flex items-center gap-3 text-sm text-gray-600">
                            <label
                                htmlFor="imageUpload"
                                className="flex items-center gap-2 cursor-pointer px-2 py-1 rounded-full hover:bg-gray-100"
                                title="Agregar imagen"
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-green-500" viewBox="0 0 20 20" fill="currentColor">
                                    <path d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V7.414A2 2 0 0017.414 6L13 1.586A2 2 0 0011.586 1H4z" />
                                </svg>
                                <span>Imagen</span>
                            </label>
                            <input
                                id="imageUpload"
                                type="file"
                                accept="image/*"
                                className="hidden"
                                ref={fileInputRef}
                                onChange={handleFileChange}
                            />
                            {imageFile && (
                                <p className="text-sm text-green-600">
                                    Imagen lista para publicar: {imageFile.name}
                                </p>
                            )}
                        </div>

                        <div className="flex items-center gap-2">
                            <button
                                type="button"
                                className="px-4 py-1 rounded-full bg-blue-600 text-sm text-white font-semibold hover:bg-blue-700 disabled:opacity-50"
                                disabled={isDisabled || isUploading}
                                onClick={SendPost}
                            >
                                {isUploading ? "Publicando..." : "Publicar"}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}

export default AddPost;
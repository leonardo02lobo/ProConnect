import { apiRequest } from "./http";

interface UploadResponse {
    data?: { filename?: string };
}

export const mediaService = {
    /** Sube una imagen al servicio de media (vía gateway) y devuelve el nombre de archivo guardado. */
    upload: async (file: File): Promise<string> => {
        const form = new FormData();
        form.append("miArchivo", file);
        const result = await apiRequest<UploadResponse>("/upload/subirImagen", "POST", form);
        const filename = result?.data?.filename;
        if (!filename) {
            throw new Error("La subida no devolvió el nombre del archivo");
        }
        return filename;
    },
};

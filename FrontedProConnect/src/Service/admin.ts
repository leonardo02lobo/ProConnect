// Administración de roles (solo ADMIN). El backend es la barrera real: estas llamadas responden 403 al resto.
import type { AdminUserDTO, AdminUserPageDTO, Role } from "../types/Dtos";
import { apiRequest } from "./http";

export const ROLES: readonly Role[] = ["USER", "COMPANY_ADMIN", "ADMIN"];

export const adminService = {
    listUsers: (query = "", page = 0, size = 20): Promise<AdminUserPageDTO> => {
        const params = new URLSearchParams({ page: String(page), size: String(size) });
        if (query.trim()) {
            params.set("q", query.trim());
        }
        return apiRequest<AdminUserPageDTO>(`/api/admin/users?${params.toString()}`);
    },

    changeRole: (userId: number, role: Role): Promise<AdminUserDTO> =>
        apiRequest<AdminUserDTO>(`/api/admin/users/${userId}/role`, "PUT", { role }),
};

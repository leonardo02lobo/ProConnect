import { useCallback, useEffect, useState } from "react";
import type React from "react";
import { ApiError } from "../../Service/http";
import { adminService, ROLES } from "../../Service/admin";
import type { AdminUserDTO, AdminUserPageDTO, Role } from "../../types/Dtos";

const PAGE_SIZE = 20;

const ROLE_LABELS: Record<Role, string> = {
    USER: "Usuario",
    COMPANY_ADMIN: "Administrador de empresa",
    ADMIN: "Administrador",
};

function errorMessage(error: unknown): string {
    if (error instanceof ApiError) {
        return error.problem?.errors?.[0]?.message ?? error.problem?.detail ?? `Error HTTP ${error.status}`;
    }
    return "No se pudo contactar con el servidor";
}

interface RowProps {
    user: AdminUserDTO;
    onSaved: (user: AdminUserDTO) => void;
    onError: (message: string) => void;
}

function AdminUserRow({ user, onSaved, onError }: RowProps) {
    const [draft, setDraft] = useState<Role>(user.role);
    const [saving, setSaving] = useState(false);

    const save = async () => {
        setSaving(true);
        try {
            const updated = await adminService.changeRole(user.id, draft);
            onSaved(updated);
        } catch (error) {
            setDraft(user.role);
            onError(errorMessage(error));
        } finally {
            setSaving(false);
        }
    };

    return (
        <tr data-testid="admin-user-row" data-user-id={user.id} className="border-t border-gray-200">
            <td className="p-2">
                <span className="font-semibold" data-testid="admin-user-name">
                    {user.name}
                </span>
                <span className="block text-sm text-gray-500">@{user.userName}</span>
            </td>
            <td className="p-2 text-sm text-gray-700" data-testid="admin-user-email">
                {user.email}
            </td>
            <td className="p-2" data-testid="admin-user-role">
                {user.role}
            </td>
            <td className="p-2">
                <div className="flex items-center gap-2">
                    <select
                        aria-label={`Rol de ${user.userName}`}
                        data-testid="admin-user-role-select"
                        value={draft}
                        disabled={saving}
                        onChange={(e) => setDraft(e.target.value as Role)}
                        className="border border-gray-300 rounded-lg p-1"
                    >
                        {ROLES.map((role) => (
                            <option key={role} value={role}>
                                {ROLE_LABELS[role]}
                            </option>
                        ))}
                    </select>
                    <button
                        type="button"
                        data-testid="admin-user-role-save"
                        disabled={saving || draft === user.role}
                        onClick={save}
                        className="bg-blue-600 text-white px-3 py-1 rounded-lg disabled:opacity-50"
                    >
                        Guardar
                    </button>
                </div>
            </td>
        </tr>
    );
}

/** Gestión de roles (/admin/users): buscador, tabla paginada y cambio de rol por usuario. */
function AdminUsers() {
    const [query, setQuery] = useState("");
    const [submittedQuery, setSubmittedQuery] = useState("");
    const [page, setPage] = useState(0);
    const [data, setData] = useState<AdminUserPageDTO | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [status, setStatus] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const load = useCallback(async (q: string, p: number) => {
        setLoading(true);
        setError(null);
        try {
            setData(await adminService.listUsers(q, p, PAGE_SIZE));
        } catch (e) {
            setData(null);
            setError(errorMessage(e));
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        load(submittedQuery, page);
    }, [load, submittedQuery, page]);

    const onSearch = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setStatus(null);
        setPage(0);
        setSubmittedQuery(query.trim());
    };

    const onSaved = (updated: AdminUserDTO) => {
        setError(null);
        setStatus(`Rol de @${updated.userName} actualizado a ${updated.role}`);
        setData((current) =>
            current
                ? { ...current, content: current.content.map((u) => (u.id === updated.id ? updated : u)) }
                : current,
        );
    };

    const onError = (message: string) => {
        setStatus(null);
        setError(message);
    };

    return (
        <section className="w-full max-w-4xl mx-auto my-6 flex flex-col gap-4" data-testid="admin-users">
            <h1 className="text-xl font-semibold">Administración de usuarios</h1>
            <form onSubmit={onSearch} className="flex gap-2" role="search">
                <input
                    type="search"
                    placeholder="Nombre, usuario o email"
                    aria-label="Buscar usuarios por nombre, usuario o email"
                    data-testid="admin-users-search"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    className="flex-1 border border-gray-300 rounded-xl p-2 outline-none focus:border-blue-500"
                />
                <button
                    type="submit"
                    data-testid="admin-users-search-submit"
                    className="bg-blue-600 text-white px-4 py-2 rounded-xl hover:bg-blue-700"
                >
                    Buscar
                </button>
            </form>

            {error && (
                <p role="alert" data-testid="admin-users-error" className="text-red-600">
                    {error}
                </p>
            )}
            {status && (
                <p role="status" data-testid="admin-users-status" className="text-green-700">
                    {status}
                </p>
            )}

            {data && data.content.length === 0 && <p data-testid="admin-users-empty">No hay usuarios</p>}
            {data && data.content.length > 0 && (
                <table className="w-full bg-white rounded-xl shadow text-left" data-testid="admin-users-table">
                    <thead>
                        <tr className="text-sm text-gray-500">
                            <th className="p-2">Usuario</th>
                            <th className="p-2">Email</th>
                            <th className="p-2">Rol</th>
                            <th className="p-2">Cambiar rol</th>
                        </tr>
                    </thead>
                    <tbody>
                        {data.content.map((user) => (
                            <AdminUserRow
                                key={`${user.id}-${user.role}`}
                                user={user}
                                onSaved={onSaved}
                                onError={onError}
                            />
                        ))}
                    </tbody>
                </table>
            )}

            {data && data.totalPages > 1 && (
                <nav className="flex items-center gap-3" aria-label="Paginación">
                    <button
                        type="button"
                        data-testid="admin-users-prev"
                        disabled={loading || page === 0}
                        onClick={() => setPage((p) => p - 1)}
                        className="px-3 py-1 rounded-lg border disabled:opacity-50"
                    >
                        Anterior
                    </button>
                    <span className="text-sm">
                        Página {data.page + 1} de {data.totalPages}
                    </span>
                    <button
                        type="button"
                        data-testid="admin-users-next"
                        disabled={loading || page + 1 >= data.totalPages}
                        onClick={() => setPage((p) => p + 1)}
                        className="px-3 py-1 rounded-lg border disabled:opacity-50"
                    >
                        Siguiente
                    </button>
                </nav>
            )}
        </section>
    );
}

export default AdminUsers;

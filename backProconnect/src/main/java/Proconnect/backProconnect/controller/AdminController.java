package Proconnect.backProconnect.controller;

import Proconnect.backProconnect.model.AdminUserDto;
import Proconnect.backProconnect.model.AdminUserPage;
import Proconnect.backProconnect.model.RoleChangeRequest;
import Proconnect.backProconnect.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administración (solo ADMIN). Doble barrera: {@code hasRole('ADMIN')} sobre {@code /api/admin/**}
 * en SecurityConfig y el check de rol en {@link RoleService}.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RoleService roleService;

    public AdminController(RoleService roleService) {
        this.roleService = roleService;
    }

    /** Usuarios con su rol; {@code q} filtra por nombre, userName o email; size máx. 100. */
    @GetMapping("/users")
    public AdminUserPage users(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + RoleService.DEFAULT_PAGE_SIZE)
                    int size) {
        return roleService.listUsers(query, page, size);
    }

    /** Asigna USER, COMPANY_ADMIN o ADMIN (400 otro valor, 409 auto-degradación/último ADMIN). */
    @PutMapping("/users/{id}/role")
    public AdminUserDto changeRole(
            @PathVariable Long id, @RequestBody @Valid RoleChangeRequest request) {
        return roleService.changeRole(id, request.role());
    }
}

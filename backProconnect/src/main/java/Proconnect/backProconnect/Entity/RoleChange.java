package Proconnect.backProconnect.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Auditoría de cambios de rol (add-role-management): quién cambió el rol de quién, de qué a qué y
 * cuándo. Solo se inserta; ninguna ruta de la API la modifica ni la borra. Tabla creada con
 * ddl-auto=update; al integrar con Flyway (restructure-models-performance) va en su migración.
 */
@Entity
@Table(
        name = "role_changes",
        indexes = @Index(name = "idx_role_changes_target", columnList = "target_user_id"))
public class RoleChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id", nullable = false, updatable = false)
    private Long actorId;

    @Column(name = "target_user_id", nullable = false, updatable = false)
    private Long targetUserId;

    @Column(name = "from_role", nullable = false, updatable = false, length = 32)
    private String fromRole;

    @Column(name = "to_role", nullable = false, updatable = false, length = 32)
    private String toRole;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    /** Requerido por JPA. */
    protected RoleChange() {}

    public RoleChange(
            Long actorId, Long targetUserId, String fromRole, String toRole, Instant changedAt) {
        this.actorId = actorId;
        this.targetUserId = targetUserId;
        this.fromRole = fromRole;
        this.toRole = toRole;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getActorId() {
        return actorId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public String getFromRole() {
        return fromRole;
    }

    public String getToRole() {
        return toRole;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}

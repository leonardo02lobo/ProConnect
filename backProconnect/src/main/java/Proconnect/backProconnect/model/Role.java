package Proconnect.backProconnect.model;

/** Roles válidos del sistema. El registro siempre asigna USER. */
public enum Role {
    USER,
    COMPANY_ADMIN,
    ADMIN;

    public static Role fromString(String value) {
        if (value == null) {
            return USER;
        }
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return USER;
        }
    }
}

package Proconnect.backProconnect.security;

/**
 * Política de fortaleza de password: longitud mínima y número mínimo de clases de caracteres
 * (minúsculas, mayúsculas, dígitos, símbolos). Nunca incluye el password en sus mensajes.
 */
public final class PasswordPolicy {

    private final int minLength;
    private final int minClasses;

    private PasswordPolicy(int minLength, int minClasses) {
        this.minLength = minLength;
        this.minClasses = minClasses;
    }

    /** Cuenta más privilegiada: ≥ 12 caracteres y 3 de las 4 clases. */
    public static PasswordPolicy bootstrap() {
        return new PasswordPolicy(12, 3);
    }

    public boolean accepts(String password) {
        return password != null
                && password.length() >= minLength
                && characterClasses(password) >= minClasses;
    }

    public String describe() {
        return "al menos "
                + minLength
                + " caracteres y "
                + minClasses
                + " de estas 4 clases: minúsculas, mayúsculas, dígitos y símbolos";
    }

    static int characterClasses(String password) {
        boolean lower = false;
        boolean upper = false;
        boolean digit = false;
        boolean symbol = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLowerCase(c)) {
                lower = true;
            } else if (Character.isUpperCase(c)) {
                upper = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            } else if (!Character.isWhitespace(c)) {
                symbol = true;
            }
        }
        return (lower ? 1 : 0) + (upper ? 1 : 0) + (digit ? 1 : 0) + (symbol ? 1 : 0);
    }
}

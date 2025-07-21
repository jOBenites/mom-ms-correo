package pe.gob.bcrp.util;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
public class EmailUtils {

    /**
     * Clase para encapsular el resultado de validación de un email
     */
    public static class EmailValidationResult {
        private final String email;
        private final boolean isValid;
        private final String message;

        public EmailValidationResult(String email, boolean isValid, String message) {
            this.email = email;
            this.isValid = isValid;
            this.message = message;
        }

        public String getEmail() {
            return email;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("Email: %s, Válido: %s, Mensaje: %s", email, isValid, message);
        }
    }

    /**
     * Parsea una cadena de direcciones de email separadas por comas o punto y coma
     * y retorna los resultados de validación para cada email
     * @param emails String con emails separados por , o ;
     * @return Lista de EmailValidationResult con el resultado de cada email
     */
    public static List<EmailValidationResult> parseAndValidateEmailAddresses(String emails) {
        List<EmailValidationResult> results = new ArrayList<>();

        if (emails == null || emails.trim().isEmpty()) {
            results.add(new EmailValidationResult("", false, "No se proporcionaron emails"));
            return results;
        }

        // Separar por coma o punto y coma
        String[] emailArray = emails.split("[,;]");

        for (String email : emailArray) {
            String trimmedEmail = email.trim();
            if (trimmedEmail.isEmpty()) {
                results.add(new EmailValidationResult(trimmedEmail, false, "Email vacío"));
                continue;
            }

            EmailValidationResult result = validateEmail(trimmedEmail);
            results.add(result);
        }

        return results;
    }

    /**
     * Versión original que mantiene el comportamiento de filtrado
     * @param emails String con emails separados por , o ;
     * @return Array de strings con emails válidos únicamente
     */
    public static String[] parseEmailAddresses(String emails) {
        if (emails == null || emails.trim().isEmpty()) {
            return new String[0];
        }

        return Arrays.stream(emails.split("[,;]"))
                .map(String::trim)
                .filter(email -> !email.isEmpty() && validateEmail(email).isValid())
                .toArray(String[]::new);
    }

    /**
     * Validación de email que retorna el resultado con mensaje
     * @param email Email a validar
     * @return EmailValidationResult con el resultado de la validación
     */
    public static EmailValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new EmailValidationResult(email, false, "Email vacío o nulo");
        }

        email = email.trim();

        // Verificar si el email contiene @
        if (!email.contains("@")) {
            String message = "Email sin símbolo '@'";
            log.warn("{}: {}", message, email);
            return new EmailValidationResult(email, false, message);
        }

        // Verificar si el email termina solo con @ (incompleto)
        if (email.endsWith("@")) {
            String message = "Email incompleto (termina en '@')";
            log.warn("{}: {}", message, email);
            return new EmailValidationResult(email, false, message);
        }

        // Verificar si el email empieza con @ (formato inválido)
        if (email.startsWith("@")) {
            String message = "Email con formato inválido (empieza con '@')";
            log.warn("{}: {}", message, email);
            return new EmailValidationResult(email, false, message);
        }

        // Contar cuántos @ tiene (debe ser exactamente 1)
        long atCount = email.chars().filter(ch -> ch == '@').count();
        if (atCount != 1) {
            String message = "Email con múltiples '@' o sin '@'";
            log.warn("{}: {}", message, email);
            return new EmailValidationResult(email, false, message);
        }

        // Dividir por @ para validar partes
        String[] parts = email.split("@");
        if (parts.length != 2) {
            String message = "Email con estructura inválida";
            log.warn("{}: {}", message, email);
            return new EmailValidationResult(email, false, message);
        }

        String localPart = parts[0];
        String domainPart = parts[1];

        // Validar parte local (antes del @)
        if (localPart.isEmpty()) {
            String message = "Email sin parte local (antes del '@')";
            log.warn("{}: {}", message, email);
            return new EmailValidationResult(email, false, message);
        }

        // Validar parte del dominio (después del @)
        if (domainPart.isEmpty()) {
            String message = "Email sin dominio (después del '@')";
            log.warn("{}: {}", message, email);
            return new EmailValidationResult(email, false, message);
        }

        // Verificar que el dominio tenga al menos un punto
        if (!domainPart.contains(".")) {
            String message = "Email con dominio sin punto";
            log.warn("{}: {}", message, email);
            return new EmailValidationResult(email, false, message);
        }

        // Verificar que no termine en punto
        if (domainPart.endsWith(".")) {
            String message = "Email con dominio que termina en punto";
            log.warn("{}: {}", message, email);
            return new EmailValidationResult(email, false, message);
        }

        // Expresión regular más completa para validación final
        String emailPattern = "^[a-zA-Z0-9]([a-zA-Z0-9._%-]*[a-zA-Z0-9])?@[a-zA-Z0-9]([a-zA-Z0-9.-]*[a-zA-Z0-9])?\\.[a-zA-Z]{2,}$";
        boolean isValidByRegex = email.matches(emailPattern);

        if (!isValidByRegex) {
            String message = "Email con formato inválido según validación regex";
            log.warn("{}: {}", message, email);
            return new EmailValidationResult(email, false, message);
        }

        return new EmailValidationResult(email, true, "Email válido");
    }
}
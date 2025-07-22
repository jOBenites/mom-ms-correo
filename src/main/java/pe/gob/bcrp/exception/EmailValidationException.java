package pe.gob.bcrp.exception;
import pe.gob.bcrp.util.EmailUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Excepción personalizada para errores de validación de emails
 */
public class EmailValidationException extends RuntimeException {

    private final List<EmailUtils.EmailValidationResult> validationResults;

    public EmailValidationException(String message, List<EmailUtils.EmailValidationResult> validationResults) {
        super(message);
        this.validationResults = validationResults;
    }

    public EmailValidationException(List<EmailUtils.EmailValidationResult> validationResults) {
        super(buildMessage(validationResults));
        this.validationResults = validationResults;
    }

    public List<EmailUtils.EmailValidationResult> getValidationResults() {
        return validationResults;
    }

    /**
     * Obtiene solo los emails inválidos
     */
    public List<EmailUtils.EmailValidationResult> getInvalidEmails() {
        return validationResults.stream()
                .filter(result -> !result.isValid())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los mensajes de error de los emails inválidos
     */
    public List<String> getErrorMessages() {
        return getInvalidEmails().stream()
                .map(EmailUtils.EmailValidationResult::getMessage)
                .collect(Collectors.toList());
    }

    /**
     * Construye un mensaje detallado con todos los errores
     */
    private static String buildMessage(List<EmailUtils.EmailValidationResult> validationResults) {
        List<EmailUtils.EmailValidationResult> invalidEmails = validationResults.stream()
                .filter(result -> !result.isValid())
                .collect(Collectors.toList());

        if (invalidEmails.isEmpty()) {
            return "Error de validación de emails";
        }

        if (invalidEmails.size() == 1) {
            EmailUtils.EmailValidationResult result = invalidEmails.get(0);
            return String.format("Email inválido '%s': %s", result.getEmail(), result.getMessage());
        }

        StringBuilder sb = new StringBuilder("Se encontraron emails inválidos: ");
        for (int i = 0; i < invalidEmails.size(); i++) {
            EmailUtils.EmailValidationResult result = invalidEmails.get(i);
            if (i > 0) sb.append(", ");
            sb.append(String.format("'%s' (%s)", result.getEmail(), result.getMessage()));
        }

        return sb.toString();
    }
}

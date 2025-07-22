package pe.gob.bcrp.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pe.gob.bcrp.exception.EmailValidationException;
import pe.gob.bcrp.model.dto.RequestSendEmail;
import pe.gob.bcrp.model.entity.Alerta;
import pe.gob.bcrp.model.entity.TipoEvento;
import pe.gob.bcrp.service.IEmailService;
import pe.gob.bcrp.traceability.service.ITraceabilityService;
import pe.gob.bcrp.util.EmailUtils;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final ITraceabilityService traceabilityService;

    @Value("${spring.mail.from}")
    private String emailFrom;

    /**
     * Envía correo de alerta usando plantilla HTML
     */
    public boolean enviarAlertaCorreo(RequestSendEmail requestSendEmail, Alerta alerta, String procesoId) throws UnsupportedEncodingException, EmailValidationException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            List<EmailUtils.EmailValidationResult> emailValidationResultList = EmailUtils.parseAndValidateEmailAddresses(requestSendEmail.getTo());

            // Verificar si hay emails inválidos
            List<EmailUtils.EmailValidationResult> invalidEmails = emailValidationResultList.stream()
                    .filter(result -> !result.isValid())
                    .toList();

            if (!invalidEmails.isEmpty()) {
                throw new EmailValidationException(emailValidationResultList);
            }

            // También puedes validar CC si es necesario
            if (requestSendEmail.getCc() != null && !requestSendEmail.getCc().isEmpty()) {
                List<EmailUtils.EmailValidationResult> ccValidationResultList = EmailUtils.parseAndValidateEmailAddresses(requestSendEmail.getCc());

                List<EmailUtils.EmailValidationResult> invalidCcEmails = ccValidationResultList.stream()
                        .filter(result -> !result.isValid())
                        .toList();

                if (!invalidCcEmails.isEmpty()) {
                    throw new EmailValidationException("Emails inválidos en campo CC", ccValidationResultList);
                }
            }

            // Configurar destinatarios y remitente
            if (requestSendEmail.getTo() != null && !requestSendEmail.getTo().isEmpty()) {
                String[] toEmails = EmailUtils.parseEmailAddresses(requestSendEmail.getTo());
                helper.setTo(toEmails);
            } else {
                traceabilityService.logSuccess(TipoEvento.PROCESO_WARN.name(), procesoId,
                        String.format("No se especificaron destinatarios TO para alerta ID: %d", alerta.getIdAlerta()));
                return false;
            }

            if (requestSendEmail.getCc() != null && !requestSendEmail.getCc().isEmpty()) {
                String[] ccEmails = EmailUtils.parseEmailAddresses(requestSendEmail.getCc());
                helper.setCc(ccEmails);
            }

            helper.setFrom(emailFrom, "Soporte");
            helper.setSubject(requestSendEmail.asunto + " - " + requestSendEmail.getTipoError());

            // Generar contenido HTML usando Thymeleaf
            Context context = crearContextoPlantilla(alerta);

            String contenidoHtml;
            if(alerta.getTipError().isEmpty()) {
                contenidoHtml = templateEngine.process("success-email", context);
            } else {
                contenidoHtml = templateEngine.process("alerta-email", context);
            }
            helper.setText(contenidoHtml, true);

            // Enviar correo
            javaMailSender.send(message);

            traceabilityService.logSuccess(TipoEvento.PROCESO_OK.name(), procesoId,
                    String.format("Correo enviado exitosamente para alerta ID: %d", alerta.getIdAlerta()));
            return true;

        } catch (MessagingException e) {
            traceabilityService.logSuccess(TipoEvento.PROCESO_ERROR.name(), procesoId,
                    String.format("Error al procesar alerta: %d", alerta.getIdAlerta()));
            return false;
        }
    }

    /**
     * Crea el contexto con variables para la plantilla
     */
    private Context crearContextoPlantilla(Alerta alerta) {
        Context context = new Context();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        context.setVariable("nombreArchivo", alerta.getNomArchivo());
        context.setVariable("tipoError", alerta.getTipError());
        context.setVariable("fecha", alerta.getFechaCreacion().format(formatter));
        context.setVariable("camaraAfectada", alerta.getCamAfectada());
        context.setVariable("trama", formatearXml(alerta.getTrama()));
        context.setVariable("alertaId", alerta.getIdAlerta());

        return context;
    }

    /**
     * Formatea el XML para mejor visualización
     */
    private String formatearXml(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            return "No disponible";
        }

        // Escapar caracteres HTML para mostrar correctamente en email
        return xml.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("&", "&amp;");
    }
}
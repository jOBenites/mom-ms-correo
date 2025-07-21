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
import pe.gob.bcrp.model.dto.RequestSendEmail;
import pe.gob.bcrp.model.entity.Alerta;
import pe.gob.bcrp.service.IEmailService;
import pe.gob.bcrp.util.EmailUtils;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from}")
    private String emailFrom;

    /**
     * Envía correo de alerta usando plantilla HTML
     */
    public boolean enviarAlertaCorreo(RequestSendEmail requestSendEmail, Alerta alerta) throws UnsupportedEncodingException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            List<EmailUtils.EmailValidationResult> emailValidationResultList = EmailUtils.parseAndValidateEmailAddresses(requestSendEmail.getTo());
            for (EmailUtils.EmailValidationResult emailValidationResult : emailValidationResultList) {
                if(!emailValidationResult.isValid()) {
                    throw new UnsupportedEncodingException("Email validation failed");
                }
            }

            // Configurar destinatarios y remitente
            if (requestSendEmail.getTo() != null && !requestSendEmail.getTo().isEmpty()) {
                String[] toEmails = EmailUtils.parseEmailAddresses(requestSendEmail.getTo());
                helper.setTo(toEmails);
            } else {
                log.warn("No se especificaron destinatarios TO para alerta ID: {}", alerta.getIdAlerta());
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
            String contenidoHtml = templateEngine.process("alerta-email", context);
            helper.setText(contenidoHtml, true);

            // Enviar correo
            javaMailSender.send(message);
            log.info("Correo enviado exitosamente para alerta ID: {}", alerta.getIdAlerta());
            return true;

        } catch (MessagingException e) {
            log.error("Error al enviar correo para alerta ID: {}", alerta.getIdAlerta(), e);
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
        context.setVariable("fecha", alerta.getFechaError().format(formatter));
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
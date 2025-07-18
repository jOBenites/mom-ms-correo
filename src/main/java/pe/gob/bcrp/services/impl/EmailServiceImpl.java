package pe.gob.bcrp.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pe.gob.bcrp.model.dtos.RequestSendEmail;
import pe.gob.bcrp.model.entity.Alerta;
import pe.gob.bcrp.services.IEmailService;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;

@Log4j2
@Service
@AllArgsConstructor
public class EmailServiceImpl implements IEmailService {
    private JavaMailSender javaMailSender;
    private TemplateEngine templateEngine;

    /**
     * Envía correo de alerta usando plantilla HTML
     */
    public boolean enviarAlertaCorreo(RequestSendEmail requestSendEmail, Alerta alerta) throws UnsupportedEncodingException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configurar destinatarios y remitente
            helper.setTo(requestSendEmail.getTo());
            helper.setFrom("prueba@gmail.com", "soporte");
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
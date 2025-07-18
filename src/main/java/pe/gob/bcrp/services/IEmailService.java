package pe.gob.bcrp.services;

import jakarta.mail.MessagingException;
import pe.gob.bcrp.model.dtos.RequestSendEmail;
import pe.gob.bcrp.model.entity.Alerta;

import java.io.UnsupportedEncodingException;

public interface IEmailService {
    public boolean enviarAlertaCorreo(RequestSendEmail requestSendEmail, Alerta alerta) throws UnsupportedEncodingException;
}

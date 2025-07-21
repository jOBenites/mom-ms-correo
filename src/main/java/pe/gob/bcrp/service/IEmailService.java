package pe.gob.bcrp.service;

import pe.gob.bcrp.model.dto.RequestSendEmail;
import pe.gob.bcrp.model.entity.Alerta;

import java.io.UnsupportedEncodingException;

public interface IEmailService {
    public boolean enviarAlertaCorreo(RequestSendEmail requestSendEmail, Alerta alerta) throws UnsupportedEncodingException;
}

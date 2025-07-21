package pe.gob.bcrp.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pe.gob.bcrp.model.dto.RequestSendEmail;
import pe.gob.bcrp.model.entity.Alerta;
import pe.gob.bcrp.model.entity.EstadoAlerta;
import pe.gob.bcrp.repository.AlertaRepository;
import pe.gob.bcrp.service.IEmailService;

import java.time.LocalDateTime;

@Log4j2
@Service
@AllArgsConstructor
@Transactional
public class AlertaService {

    private AlertaRepository alertaRepository;
    private IEmailService emailService;

    /**
     * Procesa una nueva alerta: la guarda en BD y envía el correo
     */
    public Alerta procesarAlerta(RequestSendEmail request) {
        try {
            // 1. Crear y guardar la alerta en BD
            Alerta alerta = crearAlerta(request);
            Alerta alertaGuardada = alertaRepository.save(alerta);

            log.info("Alerta guardada en BD con ID: {}", alertaGuardada.getIdAlerta());

            // 2. Enviar correo
            boolean correoEnviado = enviarCorreoAlerta(request, alertaGuardada);

            // 3. Actualizar estado según resultado del envío
            if (correoEnviado) {
                alertaGuardada.setFechaCreacion(LocalDateTime.now());
                alertaGuardada.setEstado(EstadoAlerta.ENVIADA);
                log.info("Correo enviado exitosamente para alerta ID: {}", alertaGuardada.getIdAlerta());
            } else {
                alertaGuardada.setEstado(EstadoAlerta.ERROR_ENVIO);
                log.error("Error al enviar correo para alerta ID: {}", alertaGuardada.getIdAlerta());
            }

            return alertaRepository.save(alertaGuardada);

        } catch (Exception e) {
            log.error("Error al procesar alerta: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar alerta", e);
        }
    }

    /**
     * Crea una entidad Alerta a partir del DTO
     */
    private Alerta crearAlerta(RequestSendEmail alertaDTO) {
        Alerta alerta = new Alerta();
        alerta.setNomArchivo(alertaDTO.getNombreArchivo());
        alerta.setTipError(alertaDTO.getTipoError());
        alerta.setFechaError(LocalDateTime.parse(alertaDTO.getFecha()));
        alerta.setCamAfectada(alertaDTO.getCamaraAfectada());
        alerta.setTrama(alertaDTO.getTrama());
        alerta.setEstado(EstadoAlerta.PENDIENTE);

        return alerta;
    }

    /**
     * Envía correo para una alerta específica
     */
    private boolean enviarCorreoAlerta(RequestSendEmail request, Alerta alerta) {
        try {
            return emailService.enviarAlertaCorreo(request, alerta);
        } catch (Exception e) {
            log.error("Error al enviar correo para alerta ID: {}", alerta.getIdAlerta(), e);
            return false;
        }
    }

}
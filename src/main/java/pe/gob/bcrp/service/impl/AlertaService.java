package pe.gob.bcrp.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pe.gob.bcrp.exception.EmailValidationException;
import pe.gob.bcrp.model.dto.RequestSendEmail;
import pe.gob.bcrp.model.entity.Alerta;
import pe.gob.bcrp.model.entity.EstadoAlerta;
import pe.gob.bcrp.model.entity.TipoEvento;
import pe.gob.bcrp.repository.AlertaRepository;
import pe.gob.bcrp.service.IEmailService;
import pe.gob.bcrp.traceability.service.ITraceabilityService;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

@Log4j2
@Service
@AllArgsConstructor
@Transactional
public class AlertaService {

    private AlertaRepository alertaRepository;
    private IEmailService emailService;
    private ITraceabilityService traceabilityService;

    /**
     * Procesa una nueva alerta: la guarda en BD y envía el correo
     */
    public Alerta procesarAlerta(RequestSendEmail request, String procesoId) throws EmailValidationException {
        try {
            // 1. Crear y guardar la alerta en BD
            Alerta alerta = crearAlerta(request);
            Alerta alertaGuardada = alertaRepository.save(alerta);

            traceabilityService.logSuccess(TipoEvento.PROCESO_OK.name(), procesoId,"Alerta guardada en BD con ID: {}" + alertaGuardada.getIdAlerta());

            // 2. Enviar correo
            boolean correoEnviado = enviarCorreoAlerta(request, alertaGuardada, procesoId);

            // 3. Actualizar estado según resultado del envío
            if (correoEnviado) {
                alertaGuardada.setFechaEnvio(LocalDateTime.now());
                alertaGuardada.setEstado(EstadoAlerta.ENVIADA);

                traceabilityService.logSuccess(TipoEvento.PROCESO_OK.name(), procesoId,"Correo enviado exitosamente para alerta ID: {}" + alertaGuardada.getIdAlerta());
            } else {
                alertaGuardada.setEstado(EstadoAlerta.ERROR_ENVIO);

                traceabilityService.logSuccess(TipoEvento.PROCESO_ERROR.name(), procesoId,"Error al enviar correo para alerta ID: {}" + alertaGuardada.getIdAlerta());
            }

            return alertaRepository.save(alertaGuardada);

        } catch (EmailValidationException e) {
            // Re-lanzar EmailValidationException para que sea manejada por GlobalExceptionHandler
            traceabilityService.logError(TipoEvento.PROCESO_ERROR.name(), procesoId,"Error de validación de emails: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            traceabilityService.logError(TipoEvento.PROCESO_ERROR.name(), procesoId,"Error al procesar alerta: " + e.getMessage());
            throw new RuntimeException("Error interno al procesar la alerta", e);
        }
    }

    /**
     * Crea una entidad Alerta a partir del DTO
     */
    private Alerta crearAlerta(RequestSendEmail alertaDTO) {
        Alerta alerta = new Alerta();
        alerta.setNomArchivo(alertaDTO.getNombreArchivo());
        alerta.setTipError(alertaDTO.getTipoError());
        alerta.setFechaCreacion(LocalDateTime.now());
        alerta.setCamAfectada(alertaDTO.getCamaraAfectada());
        alerta.setTrama(alertaDTO.getTrama());
        alerta.setEstado(EstadoAlerta.PENDIENTE);

        return alerta;
    }

    /**
     * Envía correo para una alerta específica
     */
    private boolean enviarCorreoAlerta(RequestSendEmail request, Alerta alerta, String procesoId) throws EmailValidationException {
        try {
            return emailService.enviarAlertaCorreo(request, alerta, procesoId);
        } catch (EmailValidationException e) {
            // Re-lanzar EmailValidationException para que sea manejada por el método padre
            log.error("Error de validación de emails para alerta ID: {}", alerta.getIdAlerta(), e);
            throw e;
        } catch (UnsupportedEncodingException e) {
            log.error("Error de codificación al enviar correo para alerta ID: {}", alerta.getIdAlerta(), e);
            traceabilityService.logError(TipoEvento.PROCESO_ERROR.name(), procesoId,"Error de codificación para alerta ID: " + alerta.getIdAlerta());
            return false;
        } catch (Exception e) {
            log.error("Error al enviar correo para alerta ID: {}", alerta.getIdAlerta(), e);
            traceabilityService.logError(TipoEvento.PROCESO_ERROR.name(), procesoId,"Error al enviar correo para alerta ID: " + alerta.getIdAlerta());
            return false;
        }
    }
}
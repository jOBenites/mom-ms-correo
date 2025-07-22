package pe.gob.bcrp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.gob.bcrp.exception.EmailValidationException;
import pe.gob.bcrp.model.dto.RequestSendEmail;
import pe.gob.bcrp.model.dto.ResponseDTO;
import pe.gob.bcrp.model.entity.Alerta;
import pe.gob.bcrp.model.entity.EstadoAlerta;
import pe.gob.bcrp.model.entity.TipoEvento;
import pe.gob.bcrp.service.impl.AlertaService;
import pe.gob.bcrp.traceability.service.ITraceabilityService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/correo")
@CrossOrigin(origins = "*")
public class EmailController {
    private final AlertaService alertaService;
    private final ITraceabilityService traceabilityService;

    public EmailController(AlertaService alertaService, ITraceabilityService traceabilityService) {
        this.alertaService = alertaService;
        this.traceabilityService = traceabilityService;
    }

    @GetMapping
    public String index() {
        return "Hello world!";
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> createEmail(@RequestBody @Validated RequestSendEmail request) {
        String processId = UUID.randomUUID().toString(); // Mover aquí para generar un nuevo ID por cada request

        traceabilityService.logSuccess(TipoEvento.PROCESO_INICIO.name(), processId,
                "Iniciando proceso con datos: " + request.toString());

        ResponseDTO responseDTO = ResponseDTO.builder().build();

        try {
            Alerta alerta = alertaService.procesarAlerta(request, processId);

            if(alerta.getEstado().name().equals(EstadoAlerta.ERROR_ENVIO.name())) {
                traceabilityService.logSuccess(TipoEvento.PROCESO_ERROR.name(), processId,
                        "Error al enviar correo");

                responseDTO.setMessage("Error al enviar correo");
                return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                traceabilityService.logSuccess(TipoEvento.PROCESO_COMPLETADO.name(), processId,
                        "Proceso completado exitosamente");

                responseDTO.setMessage("Se envió el mensaje correctamente");
                responseDTO.setData(alerta);
                return ResponseEntity.ok(responseDTO);
            }

        } catch (EmailValidationException e) {
            // No manejar EmailValidationException aquí, dejar que GlobalExceptionHandler se encargue
            traceabilityService.logError(TipoEvento.PROCESO_ERROR.name(), processId,
                    "Error de validación de emails: " + e.getMessage());
            throw e; // Re-lanzar para que GlobalExceptionHandler lo maneje

        } catch (Exception e) {
            traceabilityService.logError(TipoEvento.PROCESO_ERROR.name(), processId,
                    "Error en el proceso: " + e.getMessage());

            responseDTO.setMessage("Error interno del servidor: " + e.getMessage());
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
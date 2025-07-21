package pe.gob.bcrp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.gob.bcrp.model.dto.RequestSendEmail;
import pe.gob.bcrp.model.dto.ResponseDTO;
import pe.gob.bcrp.model.entity.Alerta;
import pe.gob.bcrp.service.impl.AlertaService;
import pe.gob.bcrp.traceability.service.ITraceabilityService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/correo")
@CrossOrigin(origins = "*")
public class EmailController {
    private AlertaService alertaService;
    private ITraceabilityService traceabilityService;

    String processId = UUID.randomUUID().toString();
    public EmailController(AlertaService alertaService, ITraceabilityService traceabilityService) {
        this.alertaService = alertaService;
        this.traceabilityService = traceabilityService;
    }

    @GetMapping
    public String index() {
        return "Hello world!";
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> createEmail(@RequestBody RequestSendEmail request) {
        traceabilityService.logSuccess("PROCESO_INICIO", processId,
                "Iniciando proceso con datos: " + request.toString());

        ResponseDTO responseDTO = ResponseDTO.builder().build();
        try {
            Alerta alerta = alertaService.procesarAlerta(request);

            if(alerta.getEstado().equals("ERROR_ENVIO")) {
                traceabilityService.logSuccess("PROCESO_ERROR", processId,
                        "Error al enviar correo");

                responseDTO.setMessage("Error al enviar correo");
            } else {
                traceabilityService.logSuccess("PROCESO_COMPLETADO", processId,
                        "Proceso completado exitosamente");

                responseDTO.setMessage("Se envío el mensaje correctamente");
                responseDTO.setData(alerta);
            }

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            traceabilityService.logError("PROCESO_ERROR", processId,
                    "Error en el proceso: " + e.getMessage());

            responseDTO.setMessage("Error al enviar el mensaje");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }
    }
}
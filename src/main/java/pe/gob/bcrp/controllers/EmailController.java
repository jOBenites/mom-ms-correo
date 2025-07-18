package pe.gob.bcrp.controllers;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.gob.bcrp.model.dtos.RequestSendEmail;
import pe.gob.bcrp.model.dtos.ResponseDTO;
import pe.gob.bcrp.model.entity.Alerta;
import pe.gob.bcrp.services.impl.AlertaService;

@Slf4j
@RestController
@RequestMapping("/correo")
//@Tag(name = "AvisoOperacion", description = "Aviso de Operaciones  - Listar aviso de operacion")
public class EmailController {
    private AlertaService alertaService;
    public EmailController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public String index() {
        return "Hello world!";
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> createEmail(@RequestBody RequestSendEmail request) {
        log.info("INI - getAllAvisosOperacion()");

        ResponseDTO responseDTO = ResponseDTO.builder().build();
        try {
            Alerta alerta = alertaService.procesarAlerta(request);

            responseDTO.setMessage("Se envío el mensaje correctamente");
            responseDTO.setData(alerta);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            responseDTO.setMessage("Error al enviar el mensaje");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }
    }
}
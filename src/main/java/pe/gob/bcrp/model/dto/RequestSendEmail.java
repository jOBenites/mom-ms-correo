package pe.gob.bcrp.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestSendEmail {
    @NotBlank(message = "Para(TO) es requerido")
    public String to;
    @NotBlank(message = "Copia(CC) es requerido")
    public String cc;
    @NotBlank(message = "Asunto es requerido")
    public String asunto;
    @NotBlank(message = "Nombre de archivo es requerido")
    public String nombreArchivo;
    public String tipoError;
    @NotBlank(message = "Camara afectada es requerida")
    public String camaraAfectada;
    @NotBlank(message = "La trama es requerida")
    public String trama;
}
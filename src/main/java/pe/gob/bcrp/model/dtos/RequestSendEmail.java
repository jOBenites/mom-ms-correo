package pe.gob.bcrp.model.dtos;

import lombok.Data;

@Data
public class RequestSendEmail {
    public String to;
    public String cc;
    public String asunto;
    public String nombreArchivo;
    public String tipoError;
    public String fecha;
    public String camaraAfectada;
    public String trama;
}
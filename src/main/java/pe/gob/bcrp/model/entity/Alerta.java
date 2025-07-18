package pe.gob.bcrp.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "MOM_MAE_ALERTA")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador de la cuenta")
    @Column(name = "ID_ALERTA")
    private Long idAlerta;

    @Column(name = "NOM_ARCHIVO")
    @Comment("Número de cuenta")
    private String nomArchivo;

    @Column(name = "TIP_ERROR")
    @Comment("Tipo de error")
    private String tipError;

    @Column(name = "FEC_ERROR")
    @Comment("Fecha de origen error")
    private LocalDateTime fechaError;

    @Column(name = "CAM_AFECTADA")
    @Comment("Camara afectada")
    private String camAfectada;

    @Column(name = "trama", columnDefinition = "TEXT")
    @Comment("Trama de error")
    private String trama;

    @Column(name = "FEC_REG")
    @Comment("Fecha de registro")
    private LocalDateTime fechaCreacion;

    @Column(name = "USU_REG",length = 50)
    @Comment("Usuario de registro")
    private String usuarioCreacion;

    @Column(name = "estado")
    @Comment("Estado del envío")
    @Enumerated(EnumType.STRING)
    private EstadoAlerta estado = EstadoAlerta.PENDIENTE;
}

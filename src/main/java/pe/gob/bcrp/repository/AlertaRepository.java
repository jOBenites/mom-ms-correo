package pe.gob.bcrp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.bcrp.model.entity.Alerta;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
}

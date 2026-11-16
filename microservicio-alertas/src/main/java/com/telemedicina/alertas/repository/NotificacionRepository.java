package com.telemedicina.alertas.repository;
import com.telemedicina.alertas.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    List<Notificacion> findByAlertaId(UUID alertaId);
}

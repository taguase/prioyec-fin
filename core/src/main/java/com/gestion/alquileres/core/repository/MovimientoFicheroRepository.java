package com.gestion.alquileres.core.repository;

import com.gestion.alquileres.core.model.EstadoMovimiento;
import com.gestion.alquileres.core.model.MovimientoFichero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoFicheroRepository extends JpaRepository<MovimientoFichero, Long> {

    List<MovimientoFichero> findByCargaIdAndEstadoOrderByFilaAsc(Long cargaId, EstadoMovimiento estado);

    List<MovimientoFichero> findByCargaIdOrderByFilaAsc(Long cargaId);
}

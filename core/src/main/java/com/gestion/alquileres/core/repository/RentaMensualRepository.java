package com.gestion.alquileres.core.repository;

import com.gestion.alquileres.core.model.RentaMensual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RentaMensualRepository extends JpaRepository<RentaMensual, Long> {

    Optional<RentaMensual> findByContratoIdAndAnioAndMes(Long contratoId, int anio, int mes);

    /** Informe mensual (funcionalidad 5). */
    @Query("""
           select r from RentaMensual r
             join fetch r.contrato c
             join fetch c.inquilino q
             join fetch c.inmueble m
            where r.anio = :anio and r.mes = :mes
            order by m.nombreCalle, m.portal, m.piso, m.letra
           """)
    List<RentaMensual> findDelPeriodo(@Param("anio") int anio, @Param("mes") int mes);

    /** Informe anual (funcionalidad 6). */
    @Query("""
           select r from RentaMensual r
             join fetch r.contrato c
             join fetch c.inquilino q
             join fetch c.inmueble m
            where r.anio = :anio
            order by m.nombreCalle, m.portal, m.piso, m.letra, r.mes
           """)
    List<RentaMensual> findDelAnio(@Param("anio") int anio);

    List<RentaMensual> findByContratoIdOrderByAnioDescMesDesc(Long contratoId);
}

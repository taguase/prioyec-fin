package com.gestion.alquileres.core.repository;

import com.gestion.alquileres.core.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    /** Inquilino vigente de una vivienda. */
    Optional<Contrato> findByInmuebleIdAndActualTrue(Long inmuebleId);

    /** Historico completo de la vivienda, el vigente primero. */
    @Query("""
           select c from Contrato c
             join fetch c.inquilino
             join fetch c.inmueble
            where c.inmueble.id = :inmuebleId
            order by c.actual desc, c.fechaInicio desc
           """)
    List<Contrato> findHistoricoDeInmueble(@Param("inmuebleId") Long inmuebleId);

    /** Inquilinos anteriores (no vigentes) de la vivienda. */
    @Query("""
           select c from Contrato c
             join fetch c.inquilino
             join fetch c.inmueble
            where c.inmueble.id = :inmuebleId and c.actual = false
            order by c.fechaInicio desc
           """)
    List<Contrato> findAnterioresDeInmueble(@Param("inmuebleId") Long inmuebleId);

    /**
     * Busqueda tipo LIKE por nombre, apellidos o DNI del inquilino.
     * El parametro debe llegar ya en mayusculas y envuelto en '%'.
     */
    @Query("""
           select c from Contrato c
             join fetch c.inquilino q
             join fetch c.inmueble m
            where upper(q.nombre) like :patron
               or upper(q.apellidos) like :patron
               or upper(concat(q.nombre, ' ', q.apellidos)) like :patron
               or upper(q.dni) like :patron
            order by c.actual desc, q.apellidos, q.nombre, c.fechaInicio desc
           """)
    List<Contrato> buscarPorNombreODni(@Param("patron") String patron);

    Optional<Contrato> findByInquilinoIdAndInmuebleIdAndFechaInicio(Long inquilinoId, Long inmuebleId, java.time.LocalDate fechaInicio);

    /** Todos los contratos con inquilino e inmueble cargados: base del casado del fichero mensual. */
    @Query("""
           select c from Contrato c
             join fetch c.inquilino
             join fetch c.inmueble
           """)
    List<Contrato> findAllConDetalle();

    /** Contratos vigentes ordenados por direccion: base de los informes. */
    @Query("""
           select c from Contrato c
             join fetch c.inquilino q
             join fetch c.inmueble m
            where c.actual = true
            order by m.nombreCalle, m.portal, m.piso, m.letra
           """)
    List<Contrato> findActualesOrdenadosPorDireccion();

    /** Contratos marcados como actual del inmueble, para desmarcarlos al dar de alta otro. */
    List<Contrato> findByInmuebleIdAndActualTrueAndIdNot(Long inmuebleId, Long id);
}

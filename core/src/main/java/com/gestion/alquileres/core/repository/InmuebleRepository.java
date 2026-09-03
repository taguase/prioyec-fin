package com.gestion.alquileres.core.repository;

import com.gestion.alquileres.core.dto.EdificioDTO;
import com.gestion.alquileres.core.model.Inmueble;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InmuebleRepository extends JpaRepository<Inmueble, Long> {

    Optional<Inmueble> findByNombreCalleAndPortalAndPisoAndLetraAndMunicipioAndCiudad(
            String nombreCalle, String portal, String piso, String letra, String municipio, String ciudad);

    /** Direcciones distintas a nivel de portal, para el primer combo de busqueda. */
    @Query("""
           select distinct new com.gestion.alquileres.core.dto.EdificioDTO(
                  i.nombreCalle, i.portal, i.municipio, i.ciudad)
             from Inmueble i
            order by i.nombreCalle, i.portal
           """)
    List<EdificioDTO> findEdificios();

    /** Viviendas (piso + letra) de un portal concreto, para el segundo combo. */
    @Query("""
           select i from Inmueble i
            where i.nombreCalle = :calle and i.portal = :portal
              and i.municipio = :municipio and i.ciudad = :ciudad
            order by i.piso, i.letra
           """)
    List<Inmueble> findViviendasDeEdificio(@Param("calle") String calle,
                                           @Param("portal") String portal,
                                           @Param("municipio") String municipio,
                                           @Param("ciudad") String ciudad);

    List<Inmueble> findAllByOrderByNombreCalleAscPortalAscPisoAscLetraAsc();
}

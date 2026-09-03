package com.gestion.alquileres.core.repository;

import com.gestion.alquileres.core.model.CargaFichero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CargaFicheroRepository extends JpaRepository<CargaFichero, Long> {

    List<CargaFichero> findTop50ByOrderByFechaCargaDesc();
}

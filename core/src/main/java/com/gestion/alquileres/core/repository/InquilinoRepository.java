package com.gestion.alquileres.core.repository;

import com.gestion.alquileres.core.model.Inquilino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquilinoRepository extends JpaRepository<Inquilino, Long> {

    Optional<Inquilino> findByDniIgnoreCase(String dni);
}

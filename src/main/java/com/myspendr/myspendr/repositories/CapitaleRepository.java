package com.myspendr.myspendr.repositories;

import com.myspendr.myspendr.model.Capitale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CapitaleRepository extends JpaRepository<Capitale, Long> {
    Optional<Capitale> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);


}


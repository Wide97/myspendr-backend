package com.myspendr.myspendr.repositories;

import com.myspendr.myspendr.model.Movimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentoRepository extends JpaRepository<Movimento, Long> {
    List<Movimento> findByCapitaleId(Long capitaleId);
}

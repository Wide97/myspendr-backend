package com.myspendr.myspendr.repositories;

import com.myspendr.myspendr.model.CategoriaMovimento;
import com.myspendr.myspendr.model.Movimento;
import com.myspendr.myspendr.model.TipoMovimento;
import com.myspendr.myspendr.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MovimentoRepository extends JpaRepository<Movimento, Long> {
    List<Movimento> findByCapitaleId(Long capitaleId);
    void deleteByCapitaleId(Long capitaleId);

    List<Movimento> findByUserAndCategoriaAndTipoAndDataBetween(
            User user,
            CategoriaMovimento categoria,
            TipoMovimento tipo,
            LocalDate inizio,
            LocalDate fine
    );

}

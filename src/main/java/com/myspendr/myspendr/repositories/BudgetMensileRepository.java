package com.myspendr.myspendr.repositories;

import com.myspendr.myspendr.model.BudgetMensile;
import com.myspendr.myspendr.model.CategoriaMovimento;
import com.myspendr.myspendr.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetMensileRepository extends JpaRepository<BudgetMensile, UUID> {
    Optional<BudgetMensile> findByUserAndCategoriaAndMeseAndAnno(User user, CategoriaMovimento categoria, int mese, int anno);
    List<BudgetMensile> findByUserAndMeseAndAnno(User user, int mese, int anno);
}

package com.myspendr.myspendr.services;

import com.myspendr.myspendr.dto.BudgetRequest;
import com.myspendr.myspendr.dto.BudgetResponse;
import com.myspendr.myspendr.exceptions.UserNotFoundException;
import com.myspendr.myspendr.model.*;
import com.myspendr.myspendr.repositories.BudgetMensileRepository;
import com.myspendr.myspendr.repositories.MovimentoRepository;
import com.myspendr.myspendr.repositories.TelegramUserRepository;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetMensileRepository budgetRepo;
    private final MovimentoRepository movimentoRepo;
    private final UserRepository userRepo;
    private final JwtUtils jwtUtils;
    private final TelegramUserRepository telegramUserRepository;
    private final TelegramNotificationService telegramNotificationService;


    private User getUserFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        String email = jwtUtils.getUsernameFromJwtToken(token);
        log.info("🔐 Estrazione utente da token per email: {}", email);
        return userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ Utente non trovato per email: {}", email);
                    return new UserNotFoundException("Utente non trovato");
                });
    }

    @Transactional
    public void setBudget(String authHeader, BudgetRequest req) {
        try {
            User user = getUserFromToken(authHeader);

            BudgetMensile budget = budgetRepo.findByUserAndCategoriaAndMeseAndAnno(
                    user, req.getCategoria(), req.getMese(), req.getAnno()
            ).orElse(BudgetMensile.builder()
                    .user(user)
                    .categoria(req.getCategoria())
                    .mese(req.getMese())
                    .anno(req.getAnno())
                    .build());

            budget.setLimite(req.getLimite());
            budgetRepo.save(budget);

            log.info("💾 Budget salvato per utente {}, categoria {}, mese {}/{}",
                    user.getEmail(), req.getCategoria(), req.getMese(), req.getAnno());

        } catch (Exception e) {
            log.error("❌ Errore nel salvataggio del budget", e);
            throw new RuntimeException("Errore nel salvataggio del budget", e);
        }
    }

    public BudgetResponse getBudget(String authHeader, CategoriaMovimento categoria, int mese, int anno) {
        try {
            User user = getUserFromToken(authHeader);
            log.info("📥 Recupero budget per utente {}, categoria {}, mese {}/{}", user.getEmail(), categoria, mese, anno);

            BudgetMensile budget = budgetRepo.findByUserAndCategoriaAndMeseAndAnno(user, categoria, mese, anno)
                    .orElse(null);

            BigDecimal limite = budget != null ? budget.getLimite() : BigDecimal.ZERO;
            BigDecimal speso = getSpesaTotale(user, categoria, mese, anno);
            BigDecimal residuo = limite.subtract(speso);
            boolean superato = speso.compareTo(limite) > 0;


            BudgetResponse response = BudgetResponse.builder()
                    .categoria(categoria)
                    .limite(limite)
                    .speso(speso)
                    .residuo(residuo)
                    .superato(superato)
                    .mese(mese)
                    .anno(anno)
                    .build();

            log.info("✅ Budget restituito: {}", response);
            return response;

        } catch (Exception e) {
            log.error("❌ Errore nel recupero del budget", e);
            throw new RuntimeException("Errore nel recupero del budget", e);
        }
    }


    private BigDecimal getSpesaTotale(User user, CategoriaMovimento categoria, int mese, int anno) {
        try {
            LocalDate inizio = LocalDate.of(anno, mese, 1);
            LocalDate fine = inizio.withDayOfMonth(inizio.lengthOfMonth());

            List<Movimento> spese = movimentoRepo.findByCapitale_UserAndCategoriaAndTipoAndDataBetween(
                    user, categoria, TipoMovimento.USCITA, inizio, fine
            );

            BigDecimal totale = spese.stream()
                    .map(Movimento::getImporto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("💸 Totale speso da {} per {}: {}", user.getEmail(), categoria, totale);
            return totale;
        } catch (Exception e) {
            log.error("❌ Errore nel calcolo delle spese", e);
            return BigDecimal.ZERO;
        }
    }

    public List<BudgetResponse> getAllBudgets(String authHeader, int mese, int anno) {
        try {
            User user = getUserFromToken(authHeader);
            log.info("📊 Recupero TUTTI i budget per utente {}, mese {}/{}", user.getEmail(), mese, anno);

            List<BudgetMensile> budgets = budgetRepo.findByUserAndMeseAndAnno(user, mese, anno);

            return budgets.stream()
                    .map(budget -> {
                        try {
                            BigDecimal speso = getSpesaTotale(user, budget.getCategoria(), mese, anno);
                            BigDecimal residuo = budget.getLimite().subtract(speso);
                            boolean superato = speso.compareTo(budget.getLimite()) > 0;

                            return BudgetResponse.builder()
                                    .categoria(budget.getCategoria())
                                    .limite(budget.getLimite())
                                    .speso(speso)
                                    .residuo(residuo)
                                    .superato(superato)
                                    .mese(mese)
                                    .anno(anno)
                                    .build();
                        } catch (Exception e) {
                            log.error("❌ Errore nella categoria " + budget.getCategoria(), e);
                            return null;
                        }
                    })
                    .filter(b -> b != null)
                    .toList();

        } catch (Exception e) {
            log.error("❌ Errore nel recupero di tutti i budget", e);
            throw new RuntimeException("Errore nel recupero di tutti i budget", e);
        }
    }

    public void controllaSuperamentoEBotta(User user, CategoriaMovimento categoria, LocalDate data) {
        try {
            int mese = data.getMonthValue();
            int anno = data.getYear();

            BudgetMensile budget = budgetRepo.findByUserAndCategoriaAndMeseAndAnno(user, categoria, mese, anno)
                    .orElse(null);

            if (budget != null) {
                BigDecimal limite = budget.getLimite();
                BigDecimal speso = getSpesaTotale(user, categoria, mese, anno);

                if (speso.compareTo(limite) > 0) {
                    telegramUserRepository.findByUser(user).ifPresent(telegramUser -> {
                        BigDecimal sforamento = speso.subtract(limite);
                        String messaggio = "⚠️ *Budget Superato!*\nHai speso *" + speso + "€* su un limite di *" + limite + "€* per la categoria *" + categoria + "*.\nSforamento: *" + sforamento + "€*";

                        telegramNotificationService.inviaAvvisoBudgetSuperato(user, categoria, mese, anno, speso, limite);


                    });
                }
            }

        } catch (Exception e) {
            log.error("❌ Errore nel controllo budget da app", e);
        }
    }


}

package com.myspendr.myspendr.services;

import com.myspendr.myspendr.dto.CapitaleRequest;
import com.myspendr.myspendr.dto.CapitaleResponse;
import com.myspendr.myspendr.dto.ReportCapitaleDTO;
import com.myspendr.myspendr.exceptions.CapitaleNotFoundException;
import com.myspendr.myspendr.exceptions.UserNotFoundException;
import com.myspendr.myspendr.model.Capitale;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.CapitaleRepository;
import com.myspendr.myspendr.repositories.MovimentoRepository;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CapitaleService {

    private final CapitaleRepository capitaleRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final MovimentoRepository movimentoRepository;

    public CapitaleService(CapitaleRepository capitaleRepository, UserRepository userRepository, JwtUtils jwtUtils, MovimentoRepository movimentoRepository) {
        this.capitaleRepository = capitaleRepository;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.movimentoRepository = movimentoRepository;
    }

    private User getUserFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        String email = jwtUtils.getUsernameFromJwtToken(token);
        log.info("Estrazione utente da token per email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Utente non trovato per email: {}", email);
                    return new UserNotFoundException("Utente non trovato");
                });
    }

    public CapitaleResponse createCapitale(String authHeader, CapitaleRequest req) {
        try {
            User user = getUserFromToken(authHeader);

            if (capitaleRepository.findByUserId(user.getId()).isPresent()) {
                throw new IllegalStateException("Il capitale è già stato creato per questo utente.");
            }

            Capitale capitale = new Capitale();
            capitale.setContoBancario(req.getContoBancario());
            capitale.setLiquidita(req.getLiquidita());
            capitale.setAltriFondi(req.getAltriFondi());
            capitale.setUser(user);
            capitale.setDataAggiornamento(LocalDate.now());

            Capitale saved = capitaleRepository.save(capitale);
            log.info("✅ Creato nuovo capitale per utente {}: {}", user.getEmail(), saved);
            return new CapitaleResponse(saved);
        } catch (Exception e) {
            log.error("❌ Errore durante la creazione del capitale", e);
            throw new RuntimeException("Errore nella creazione del capitale", e);
        }
    }


    public CapitaleResponse updateCapitale(String authHeader, CapitaleRequest req) {
        try {
            User user = getUserFromToken(authHeader);

            // Cerca il capitale, oppure ne crea uno nuovo se non esiste
            Capitale cap = capitaleRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        Capitale nuovo = new Capitale();
                        nuovo.setUser(user);
                        nuovo.setContoBancario(BigDecimal.ZERO);
                        nuovo.setLiquidita(BigDecimal.ZERO);
                        nuovo.setAltriFondi(BigDecimal.ZERO);
                        nuovo.setDataAggiornamento(LocalDate.now());
                        return nuovo;
                    });

            // Aggiorna i valori con quelli ricevuti
            cap.setContoBancario(req.getContoBancario());
            cap.setLiquidita(req.getLiquidita());
            cap.setAltriFondi(req.getAltriFondi());
            cap.setDataAggiornamento(LocalDate.now());

            // Salva (aggiorna o crea)
            Capitale updated = capitaleRepository.save(cap);

            log.info("🔄 Capitale creato/aggiornato per utente {}: {}", user.getEmail(), updated);
            return new CapitaleResponse(updated);

        } catch (Exception e) {
            log.error("❌ Errore nell'aggiornamento del capitale", e);
            throw new RuntimeException("Errore nell'aggiornamento", e);
        }
    }


    public CapitaleResponse getCapitale(String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            Optional<Capitale> optionalCap = capitaleRepository.findByUserId(user.getId());

            if (optionalCap.isPresent()) {
                Capitale cap = optionalCap.get();
                log.info("📥 Recuperato capitale per utente {}: {}", user.getEmail(), cap);
                return new CapitaleResponse(cap);
            } else {
                log.info("📥 Nessun capitale trovato per utente {}. Restituisco capitale vuoto.", user.getEmail());
                return CapitaleResponse.vuoto();
            }

        } catch (Exception e) {
            log.error("❌ Errore nel recupero del capitale", e);
            throw new RuntimeException("Errore nel recupero del capitale", e);
        }
    }



    @Transactional
    public void deleteCapitale(String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            log.info("🗑 Eliminazione capitale per utente {}", user.getEmail());
            capitaleRepository.deleteByUserId(user.getId());
        } catch (Exception e) {
            log.error("❌ Errore durante l'eliminazione del capitale", e);
            throw new RuntimeException("Errore nella cancellazione", e);
        }
    }

    @Transactional
    public CapitaleResponse resetCapitaleCompleto(String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            Capitale cap = capitaleRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new CapitaleNotFoundException("Capitale non trovato"));

            movimentoRepository.deleteByCapitaleId(cap.getId());

            cap.setContoBancario(BigDecimal.ZERO);
            cap.setLiquidita(BigDecimal.ZERO);
            cap.setAltriFondi(BigDecimal.ZERO);
            cap.setDataAggiornamento(LocalDate.now());

            Capitale reset = capitaleRepository.save(cap);
            log.info("🧨 Reset completo effettuato per capitale {}", reset.getId());
            return new CapitaleResponse(reset);
        } catch (Exception e) {
            log.error("❌ Errore nel reset completo del capitale", e);
            throw new RuntimeException("Errore nel reset completo", e);
        }
    }


    public CapitaleResponse resetCapitale(String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            Capitale cap = capitaleRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new CapitaleNotFoundException("Capitale non trovato"));

            cap.setContoBancario(BigDecimal.ZERO);
            cap.setLiquidita(BigDecimal.ZERO);
            cap.setAltriFondi(BigDecimal.ZERO);
            cap.setDataAggiornamento(LocalDate.now());

            Capitale reset = capitaleRepository.save(cap);
            log.info("🔁 Reset parziale effettuato per capitale {}", reset.getId());
            return new CapitaleResponse(reset);
        } catch (Exception e) {
            log.error("❌ Errore nel reset parziale del capitale", e);
            throw new RuntimeException("Errore nel reset", e);
        }
    }


    public List<ReportCapitaleDTO> getReportMensile(String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            log.info("📊 Generazione report mensile per utente {}", user.getEmail());

            List<Capitale> capitali = capitaleRepository.findAll().stream()
                    .filter(c -> c.getUser().getId().equals(user.getId()))
                    .toList();

            Map<String, List<Capitale>> raggruppatiPerMese = capitali.stream()
                    .collect(Collectors.groupingBy(
                            c -> c.getDataAggiornamento().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                    ));

            List<ReportCapitaleDTO> report = raggruppatiPerMese.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        String periodo = entry.getKey();
                        BigDecimal sommaTotale = entry.getValue().stream()
                                .map(Capitale::getTotale)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        return new ReportCapitaleDTO(periodo, sommaTotale, BigDecimal.ZERO);
                    })
                    .toList();

            log.info("✅ Report mensile generato con {} periodi", report.size());
            return report;
        } catch (Exception e) {
            log.error("❌ Errore durante la generazione del report mensile", e);
            throw new RuntimeException("Errore nel report mensile", e);
        }
    }


    public List<ReportCapitaleDTO> getReportAnnuale(String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            log.info("📆 Generazione report annuale per utente {}", user.getEmail());

            List<Capitale> capitali = capitaleRepository.findAll().stream()
                    .filter(c -> c.getUser().getId().equals(user.getId()))
                    .toList();

            Map<String, List<Capitale>> raggruppatiPerAnno = capitali.stream()
                    .collect(Collectors.groupingBy(
                            c -> String.valueOf(c.getDataAggiornamento().getYear())
                    ));

            List<ReportCapitaleDTO> report = raggruppatiPerAnno.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        String periodo = entry.getKey();
                        BigDecimal sommaTotale = entry.getValue().stream()
                                .map(Capitale::getTotale)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        return new ReportCapitaleDTO(periodo, sommaTotale, BigDecimal.ZERO);
                    })
                    .toList();

            log.info("✅ Report annuale generato con {} anni", report.size());
            return report;
        } catch (Exception e) {
            log.error("❌ Errore durante la generazione del report annuale", e);
            throw new RuntimeException("Errore nel report annuale", e);
        }
    }


}

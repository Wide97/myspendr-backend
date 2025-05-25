package com.myspendr.myspendr.services;

import com.myspendr.myspendr.dto.MovimentoRequest;
import com.myspendr.myspendr.dto.MovimentoResponse;
import com.myspendr.myspendr.exceptions.CapitaleNotFoundException;
import com.myspendr.myspendr.exceptions.UserNotFoundException;
import com.myspendr.myspendr.model.Capitale;
import com.myspendr.myspendr.model.Movimento;
import com.myspendr.myspendr.model.TipoMovimento;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.CapitaleRepository;
import com.myspendr.myspendr.repositories.MovimentoRepository;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MovimentoService {

    private final CapitaleRepository capitaleRepository;
    private final MovimentoRepository movimentoRepository;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public MovimentoService(CapitaleRepository capitaleRepository,
                            MovimentoRepository movimentoRepository,
                            JwtUtils jwtUtils,
                            UserRepository userRepository) {
        this.capitaleRepository = capitaleRepository;
        this.movimentoRepository = movimentoRepository;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    private Capitale getCapitaleFromToken(String authHeader) {
        String email = jwtUtils.getUsernameFromJwtToken(authHeader.replace("Bearer ", "").trim());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utente non trovato"));
        return capitaleRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CapitaleNotFoundException("Capitale non trovato"));
    }

    public MovimentoResponse creaMovimento(String authHeader, MovimentoRequest request) {
        try {
            String email = jwtUtils.getUsernameFromJwtToken(authHeader.replace("Bearer ", "").trim());
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Utente non trovato"));

            Capitale capitale = capitaleRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        Capitale nuovo = new Capitale();
                        nuovo.setUser(user);
                        nuovo.setContoBancario(BigDecimal.ZERO);
                        nuovo.setLiquidita(BigDecimal.ZERO);
                        nuovo.setAltriFondi(BigDecimal.ZERO);
                        nuovo.setDataAggiornamento(LocalDate.now());
                        return capitaleRepository.save(nuovo);
                    });

            log.info("▶️ Creazione movimento: categoria={}, tipo={}, fonte={}, importo={}, data={}, descrizione={}",
                    request.getCategoria(), request.getTipo(), request.getFonte(),
                    request.getImporto(), request.getData(), request.getDescrizione());

            BigDecimal importo = request.getImporto();
            String fonte = request.getFonte();
            log.info("🔐 Recupero capitale per utente {}", email);

            if (fonte == null) {
                throw new IllegalArgumentException("Fonte obbligatoria per il movimento");
            }

            Movimento movimento = Movimento.builder()
                    .importo(importo)
                    .tipo(request.getTipo())
                    .categoria(request.getCategoria())
                    .descrizione(request.getDescrizione())
                    .data(request.getData())
                    .fonte(fonte.toUpperCase())
                    .capitale(capitale)
                    .build();

            switch (request.getTipo()) {
                case ENTRATA -> {
                    switch (fonte.toUpperCase()) {
                        case "BANCA" -> capitale.setContoBancario(capitale.getContoBancario().add(importo));
                        case "CONTANTI" -> capitale.setLiquidita(capitale.getLiquidita().add(importo));
                        case "ALTRI" -> capitale.setAltriFondi(capitale.getAltriFondi().add(importo));
                        default -> throw new IllegalArgumentException("Fonte non valida: " + fonte);
                    }
                }
                case USCITA -> {
                    switch (fonte.toUpperCase()) {
                        case "BANCA" -> capitale.setContoBancario(capitale.getContoBancario().subtract(importo));
                        case "CONTANTI" -> capitale.setLiquidita(capitale.getLiquidita().subtract(importo));
                        case "ALTRI" -> capitale.setAltriFondi(capitale.getAltriFondi().subtract(importo));
                        default -> throw new IllegalArgumentException("Fonte non valida: " + fonte);
                    }
                }
                default -> throw new IllegalArgumentException("Tipo di movimento non valido: " + request.getTipo());
            }

            capitaleRepository.save(capitale);
            Movimento saved = movimentoRepository.save(movimento);
            log.info("✅ Movimento {} [{}] salvato per capitale {}", saved.getTipo(), saved.getFonte(), capitale.getId());

            return new MovimentoResponse(saved);

        } catch (IllegalArgumentException e) {
            log.error("❌ Errore di validazione nel movimento: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("💥 Errore imprevisto nella creazione del movimento", e);
            throw new RuntimeException("Errore nella creazione del movimento", e);
        }
    }


    public List<MovimentoResponse> getMovimentiByUser(String authHeader) {
        try {
            Capitale capitale = getCapitaleFromToken(authHeader);
            log.info("🔎 Recupero movimenti per capitale ID={}", capitale.getId());

            return movimentoRepository.findByCapitaleId(capitale.getId()).stream()
                    .map(MovimentoResponse::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Errore nel recupero dei movimenti", e);
            throw new RuntimeException("Errore nel recupero dei movimenti", e);
        }
    }

    public void eliminaMovimento(Long id) {
        try {
            Movimento movimento = movimentoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Movimento non trovato con ID: " + id));

            Capitale capitale = movimento.getCapitale();
            BigDecimal importo = movimento.getImporto();
            String fonte = movimento.getFonte().toUpperCase();

            log.info("🗑 Eliminazione movimento ID={} - Tipo: {}, Fonte: {}, Importo: {}", id, movimento.getTipo(), fonte, importo);

            switch (movimento.getTipo()) {
                case ENTRATA -> {
                    switch (fonte) {
                        case "BANCA" -> capitale.setContoBancario(capitale.getContoBancario().subtract(importo));
                        case "CONTANTI" -> capitale.setLiquidita(capitale.getLiquidita().subtract(importo));
                        case "ALTRI" -> capitale.setAltriFondi(capitale.getAltriFondi().subtract(importo));
                        default -> throw new IllegalArgumentException("Fonte non valida: " + fonte);
                    }
                }
                case USCITA -> {
                    switch (fonte) {
                        case "BANCA" -> capitale.setContoBancario(capitale.getContoBancario().add(importo));
                        case "CONTANTI" -> capitale.setLiquidita(capitale.getLiquidita().add(importo));
                        case "ALTRI" -> capitale.setAltriFondi(capitale.getAltriFondi().add(importo));
                        default -> throw new IllegalArgumentException("Fonte non valida: " + fonte);
                    }
                }
            }

            capitaleRepository.save(capitale);
            movimentoRepository.deleteById(id);
            log.info("✅ Movimento eliminato e capitale aggiornato (ID: {})", id);
        } catch (Exception e) {
            log.error("❌ Errore durante l'eliminazione del movimento ID={}", id, e);
            throw new RuntimeException("Errore durante l'eliminazione del movimento", e);
        }
    }


    public List<MovimentoResponse> getMovimentiByDateRange(String authHeader, LocalDate start, LocalDate end) {
        try {
            Capitale capitale = getCapitaleFromToken(authHeader);
            log.info("📅 Filtro movimenti tra {} e {} per capitale ID={}", start, end, capitale.getId());

            return movimentoRepository.findByCapitaleId(capitale.getId()).stream()
                    .filter(m -> !m.getData().isBefore(start) && !m.getData().isAfter(end))
                    .map(MovimentoResponse::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Errore nel filtro per intervallo date", e);
            throw new RuntimeException("Errore nel recupero dei movimenti per intervallo", e);
        }
    }


    public BigDecimal getTotaleEntrate(String authHeader) {
        try {
            Capitale capitale = getCapitaleFromToken(authHeader);

            BigDecimal totale = movimentoRepository.findByCapitaleId(capitale.getId()).stream()
                    .filter(m -> m.getTipo() == TipoMovimento.ENTRATA)
                    .map(Movimento::getImporto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("📊 Totale ENTRATE per capitale {}: {}€", capitale.getId(), totale);
            return totale;

        } catch (CapitaleNotFoundException e) {
            log.info("ℹ️ Capitale non presente: ritorno 0€ come totale entrate.");
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("❌ Errore nel calcolo totale entrate", e);
            throw new RuntimeException("Errore nel calcolo delle entrate", e);
        }
    }


    public BigDecimal getTotaleUscite(String authHeader) {
        try {
            Capitale capitale = getCapitaleFromToken(authHeader);
            BigDecimal totale = movimentoRepository.findByCapitaleId(capitale.getId()).stream()
                    .filter(m -> m.getTipo() == TipoMovimento.USCITA)
                    .map(Movimento::getImporto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("📉 Totale USCITE per capitale {}: {}€", capitale.getId(), totale);
            return totale;
        } catch (Exception e) {
            log.error("❌ Errore nel calcolo totale uscite", e);
            throw new RuntimeException("Errore nel calcolo delle uscite", e);
        }
    }

    public MovimentoResponse creaMovimentoDaTelegram(User user, MovimentoRequest request) {
        try {
            Capitale capitale = capitaleRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        Capitale nuovo = new Capitale();
                        nuovo.setUser(user);
                        nuovo.setContoBancario(BigDecimal.ZERO);
                        nuovo.setLiquidita(BigDecimal.ZERO);
                        nuovo.setAltriFondi(BigDecimal.ZERO);
                        nuovo.setDataAggiornamento(LocalDate.now());
                        return capitaleRepository.save(nuovo);
                    });

            BigDecimal importo = request.getImporto();
            String fonte = request.getFonte();

            Movimento movimento = Movimento.builder()
                    .importo(importo)
                    .tipo(request.getTipo())
                    .categoria(request.getCategoria())
                    .descrizione(request.getDescrizione())
                    .data(request.getData())
                    .fonte(fonte.toUpperCase())
                    .capitale(capitale)
                    .build();

            switch (request.getTipo()) {
                case ENTRATA -> {
                    switch (fonte.toUpperCase()) {
                        case "BANCA" -> capitale.setContoBancario(capitale.getContoBancario().add(importo));
                        case "CONTANTI" -> capitale.setLiquidita(capitale.getLiquidita().add(importo));
                        case "ALTRI" -> capitale.setAltriFondi(capitale.getAltriFondi().add(importo));
                        default -> throw new IllegalArgumentException("Fonte non valida: " + fonte);
                    }
                }
                case USCITA -> {
                    switch (fonte.toUpperCase()) {
                        case "BANCA" -> capitale.setContoBancario(capitale.getContoBancario().subtract(importo));
                        case "CONTANTI" -> capitale.setLiquidita(capitale.getLiquidita().subtract(importo));
                        case "ALTRI" -> capitale.setAltriFondi(capitale.getAltriFondi().subtract(importo));
                        default -> throw new IllegalArgumentException("Fonte non valida: " + fonte);
                    }
                }
                default -> throw new IllegalArgumentException("Tipo di movimento non valido: " + request.getTipo());
            }

            capitaleRepository.save(capitale);
            Movimento saved = movimentoRepository.save(movimento);

            log.info("✅ Movimento {} [{}] salvato per capitale {}", saved.getTipo(), saved.getFonte(), capitale.getId());

            return new MovimentoResponse(saved);
        } catch (Exception e) {
            log.error("💥 Errore nella creazione del movimento da Telegram", e);
            throw new RuntimeException("Errore nella creazione del movimento da Telegram", e);
        }
    }


}

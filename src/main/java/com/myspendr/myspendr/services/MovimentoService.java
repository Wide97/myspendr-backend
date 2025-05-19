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
        Capitale capitale = getCapitaleFromToken(authHeader);
        BigDecimal importo = request.getImporto();

        String fonte = request.getFonte();
        if (fonte == null) {
            throw new IllegalArgumentException("Fonte obbligatoria per il movimento");
        }

        Movimento movimento = Movimento.builder()
                .importo(importo)
                .tipo(request.getTipo())
                .categoria(request.getCategoria())
                .descrizione(request.getDescrizione())
                .data(request.getData())
                .fonte(fonte.toUpperCase()) // salva in maiuscolo per coerenza
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
        }

        capitaleRepository.save(capitale);
        Movimento saved = movimentoRepository.save(movimento);
        log.info("Movimento {} [{}] salvato per capitale {}", saved.getTipo(), saved.getFonte(), capitale.getId());

        return new MovimentoResponse(saved);
    }

    public List<MovimentoResponse> getMovimentiByUser(String authHeader) {
        Capitale capitale = getCapitaleFromToken(authHeader);
        return movimentoRepository.findByCapitaleId(capitale.getId())
                .stream().map(MovimentoResponse::new).collect(Collectors.toList());
    }

    public void eliminaMovimento(Long id) {
        Movimento movimento = movimentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimento non trovato con ID: " + id));

        Capitale capitale = movimento.getCapitale();
        BigDecimal importo = movimento.getImporto();

        switch (movimento.getTipo()) {
            case ENTRATA -> capitale.setLiquidita(capitale.getLiquidita().subtract(importo));
            case USCITA -> capitale.setLiquidita(capitale.getLiquidita().add(importo));
        }

        capitaleRepository.save(capitale);
        movimentoRepository.deleteById(id);
        log.info("Movimento eliminato e capitale ripristinato (ID: {})", id);
    }

    // 📅 Filtro per intervallo di date
    public List<MovimentoResponse> getMovimentiByDateRange(String authHeader, LocalDate start, LocalDate end) {
        Capitale capitale = getCapitaleFromToken(authHeader);
        return movimentoRepository.findByCapitaleId(capitale.getId()).stream()
                .filter(m -> !m.getData().isBefore(start) && !m.getData().isAfter(end))
                .map(MovimentoResponse::new)
                .collect(Collectors.toList());
    }

    // 📊 Totale entrate
    public BigDecimal getTotaleEntrate(String authHeader) {
        Capitale capitale = getCapitaleFromToken(authHeader);
        return movimentoRepository.findByCapitaleId(capitale.getId()).stream()
                .filter(m -> m.getTipo() == TipoMovimento.ENTRATA)
                .map(Movimento::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 📉 Totale uscite
    public BigDecimal getTotaleUscite(String authHeader) {
        Capitale capitale = getCapitaleFromToken(authHeader);
        return movimentoRepository.findByCapitaleId(capitale.getId()).stream()
                .filter(m -> m.getTipo() == TipoMovimento.USCITA)
                .map(Movimento::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

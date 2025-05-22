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
        log.info("Creato nuovo capitale per utente {}: {}", user.getEmail(), saved);
        return new CapitaleResponse(saved);
    }

    public CapitaleResponse updateCapitale(String authHeader, CapitaleRequest req) {
        User user = getUserFromToken(authHeader);
        Capitale cap = capitaleRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.warn("Capitale non trovato per utente {}", user.getEmail());
                    return new CapitaleNotFoundException("Capitale non trovato");
                });

        cap.setContoBancario(req.getContoBancario());
        cap.setLiquidita(req.getLiquidita());
        cap.setAltriFondi(req.getAltriFondi());
        cap.setDataAggiornamento(LocalDate.now());

        Capitale updated = capitaleRepository.save(cap);
        log.info("Capitale aggiornato per utente {}: {}", user.getEmail(), updated);
        return new CapitaleResponse(updated);
    }

    public CapitaleResponse getCapitale(String authHeader) {
        User user = getUserFromToken(authHeader);
        Capitale cap = capitaleRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.warn("Capitale non trovato per utente {}", user.getEmail());
                    return new CapitaleNotFoundException("Capitale non trovato");
                });
        log.info("Recuperato capitale per utente {}: {}", user.getEmail(), cap);
        return new CapitaleResponse(cap);
    }

    @Transactional
    public void deleteCapitale(String authHeader) {
        User user = getUserFromToken(authHeader);
        log.info("Eliminazione capitale per utente {}", user.getEmail());
        capitaleRepository.deleteByUserId(user.getId());
    }


    public CapitaleResponse resetCapitaleCompleto(String authHeader) {
        User user = getUserFromToken(authHeader);
        Capitale cap = capitaleRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CapitaleNotFoundException("Capitale non trovato"));

        movimentoRepository.deleteByCapitaleId(cap.getId());

        cap.setContoBancario(BigDecimal.ZERO);
        cap.setLiquidita(BigDecimal.ZERO);
        cap.setAltriFondi(BigDecimal.ZERO);
        cap.setDataAggiornamento(LocalDate.now());

        Capitale reset = capitaleRepository.save(cap);
        return new CapitaleResponse(reset);
    }

    public CapitaleResponse resetCapitale(String authHeader) {
        User user = getUserFromToken(authHeader);
        Capitale cap = capitaleRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CapitaleNotFoundException("Capitale non trovato"));
        cap.setContoBancario(BigDecimal.ZERO);
        cap.setLiquidita(BigDecimal.ZERO);
        cap.setAltriFondi(BigDecimal.ZERO);
        cap.setDataAggiornamento(LocalDate.now());
        Capitale reset = capitaleRepository.save(cap);
        return new CapitaleResponse(reset);
    }



    public List<ReportCapitaleDTO> getReportMensile(String authHeader) {
        User user = getUserFromToken(authHeader);

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
                    return new ReportCapitaleDTO(periodo, sommaTotale, BigDecimal.ZERO); // variazione = 0 per ora
                })
                .toList();

        return report;
    }


    public List<ReportCapitaleDTO> getReportAnnuale(String authHeader) {
        User user = getUserFromToken(authHeader);

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

        return report;
    }



}

package com.myspendr.myspendr.services;

import com.myspendr.myspendr.dto.CapitaleRequest;
import com.myspendr.myspendr.dto.CapitaleResponse;
import com.myspendr.myspendr.exceptions.CapitaleNotFoundException;
import com.myspendr.myspendr.exceptions.UserNotFoundException;
import com.myspendr.myspendr.model.Capitale;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.CapitaleRepository;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CapitaleService {

    private final CapitaleRepository capitaleRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public CapitaleService(CapitaleRepository capitaleRepository, UserRepository userRepository, JwtUtils jwtUtils) {
        this.capitaleRepository = capitaleRepository;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
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

        Capitale capitale = new Capitale();
        capitale.setContoBancario(req.getContoBancario());
        capitale.setLiquidita(req.getLiquidita());
        capitale.setAltriFondi(req.getAltriFondi());
        capitale.setUser(user);

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

    public void deleteCapitale(String authHeader) {
        User user = getUserFromToken(authHeader);
        log.info("Eliminazione capitale per utente {}", user.getEmail());
        capitaleRepository.deleteByUserId(user.getId());
    }
}

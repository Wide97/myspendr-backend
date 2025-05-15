package com.myspendr.myspendr.services;

import com.myspendr.myspendr.dto.CapitaleRequest;
import com.myspendr.myspendr.dto.CapitaleResponse;
import com.myspendr.myspendr.exceptions.UserNotFoundException;
import com.myspendr.myspendr.model.Capitale;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.CapitaleRepository;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import org.springframework.stereotype.Service;

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
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utente non trovato"));
    }

    public CapitaleResponse createCapitale(String authHeader, CapitaleRequest req) {
        User user = getUserFromToken(authHeader);

        Capitale capitale = new Capitale();
        capitale.setContoBancario(req.getContoBancario());
        capitale.setLiquidita(req.getLiquidita());
        capitale.setAltriFondi(req.getAltriFondi());
        capitale.setUser(user);

        Capitale saved = capitaleRepository.save(capitale);
        return new CapitaleResponse(saved);
    }

    public CapitaleResponse updateCapitale(String authHeader, CapitaleRequest req) {
        User user = getUserFromToken(authHeader);
        Capitale cap = capitaleRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Capitale non trovato"));

        cap.setContoBancario(req.getContoBancario());
        cap.setLiquidita(req.getLiquidita());
        cap.setAltriFondi(req.getAltriFondi());

        Capitale updated = capitaleRepository.save(cap);
        return new CapitaleResponse(updated);
    }

    public CapitaleResponse getCapitale(String authHeader) {
        User user = getUserFromToken(authHeader);
        Capitale cap = capitaleRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Capitale non trovato"));
        return new CapitaleResponse(cap);
    }

    public void deleteCapitale(String authHeader) {
        User user = getUserFromToken(authHeader);
        capitaleRepository.deleteByUserId(user.getId());
    }
}

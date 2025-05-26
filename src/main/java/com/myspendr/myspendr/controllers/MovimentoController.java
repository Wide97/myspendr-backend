package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.dto.MovimentoRequest;
import com.myspendr.myspendr.dto.MovimentoResponse;
import com.myspendr.myspendr.services.MovimentoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/movimenti")
public class MovimentoController {

    private final MovimentoService movimentoService;

    public MovimentoController(MovimentoService movimentoService) {
        this.movimentoService = movimentoService;
    }

    // ➕ Crea un nuovo movimento
    @PostMapping
    public ResponseEntity<?> creaMovimento(@RequestBody MovimentoRequest request,
                                           @RequestHeader("Authorization") String token) {
        try {
            MovimentoResponse response = movimentoService.creaMovimento(token, request);
            log.info("✅ Movimento creato con successo: {}", response);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.warn("⚠️ Movimento non valido: {}", ex.getMessage());
            return ResponseEntity.badRequest().body("Dati non validi: " + ex.getMessage());
        } catch (Exception e) {
            log.error("❌ Errore interno nella creazione del movimento", e);
            return ResponseEntity.internalServerError().body("Errore interno del server");
        }
    }

    // 📥 Ritorna tutti i movimenti dell'utente
    @GetMapping
    public ResponseEntity<?> getMovimenti(@RequestHeader("Authorization") String token) {
        try {
            List<MovimentoResponse> lista = movimentoService.getMovimentiByUser(token);
            log.info("📦 Recuperati {} movimenti", lista.size());
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            log.error("❌ Errore nel recupero dei movimenti", e);
            return ResponseEntity.internalServerError().body("Errore nel recupero dei movimenti");
        }
    }

    // 🗓️ Ritorna movimenti per intervallo di date
    @GetMapping("/range")
    public ResponseEntity<?> getMovimentiByRange(
            @RequestHeader("Authorization") String token,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            List<MovimentoResponse> lista = movimentoService.getMovimentiByDateRange(token, start, end);
            log.info("📆 Recuperati {} movimenti tra {} e {}", lista.size(), start, end);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            log.error("❌ Errore nel recupero movimenti per intervallo", e);
            return ResponseEntity.internalServerError().body("Errore nel filtro per data");
        }
    }

    // 📊 Totale ENTRATE
    @GetMapping("/totale/entrate")
    public ResponseEntity<?> getTotaleEntrate(@RequestHeader("Authorization") String token) {
        try {
            BigDecimal totale = movimentoService.getTotaleEntrate(token);
            log.info("💰 Totale entrate: {}€", totale);
            return ResponseEntity.ok(totale);
        } catch (Exception e) {
            log.error("❌ Errore nel calcolo totale entrate", e);
            return ResponseEntity.internalServerError().body("Errore nel calcolo delle entrate");
        }
    }

    // 📉 Totale USCITE
    @GetMapping("/totale/uscite")
    public ResponseEntity<?> getTotaleUscite(@RequestHeader("Authorization") String token) {
        try {
            BigDecimal totale = movimentoService.getTotaleUscite(token);
            log.info("💸 Totale uscite: {}€", totale);
            return ResponseEntity.ok(totale);
        } catch (Exception e) {
            log.error("❌ Errore nel calcolo totale uscite", e);
            return ResponseEntity.internalServerError().body("Errore nel calcolo delle uscite");
        }
    }

    // ❌ Elimina un movimento per ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminaMovimento(@PathVariable Long id) {
        try {
            movimentoService.eliminaMovimento(id);
            log.info("🗑 Movimento eliminato con ID {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("❌ Errore durante l'eliminazione del movimento con ID {}", id, e);
            return ResponseEntity.internalServerError().body("Errore durante l'eliminazione del movimento");
        }
    }

    // 📈 Totale ENTRATE dell'ultimo mese
    @GetMapping("/totale/entrate/ultimo-mese")
    public ResponseEntity<?> getTotaleEntrateUltimoMese(@RequestHeader("Authorization") String token) {
        try {
            BigDecimal totale = movimentoService.getTotaleEntrateUltimoMese(token);
            log.info("💰 Totale entrate ultimo mese: {}€", totale);
            return ResponseEntity.ok(totale);
        } catch (Exception e) {
            log.error("❌ Errore nel calcolo totale entrate ultimo mese", e);
            return ResponseEntity.internalServerError().body("Errore nel calcolo delle entrate dell'ultimo mese");
        }
    }

    // 📉 Totale USCITE dell'ultimo mese
    @GetMapping("/totale/uscite/ultimo-mese")
    public ResponseEntity<?> getTotaleUsciteUltimoMese(@RequestHeader("Authorization") String token) {
        try {
            BigDecimal totale = movimentoService.getTotaleUsciteUltimoMese(token);
            log.info("💸 Totale uscite ultimo mese: {}€", totale);
            return ResponseEntity.ok(totale);
        } catch (Exception e) {
            log.error("❌ Errore nel calcolo totale uscite ultimo mese", e);
            return ResponseEntity.internalServerError().body("Errore nel calcolo delle uscite dell'ultimo mese");
        }
    }

}

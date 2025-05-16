package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.dto.MovimentoRequest;
import com.myspendr.myspendr.dto.MovimentoResponse;
import com.myspendr.myspendr.services.MovimentoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/movimenti")
public class MovimentoController {

    private final MovimentoService movimentoService;

    public MovimentoController(MovimentoService movimentoService) {
        this.movimentoService = movimentoService;
    }

    // ➕ Crea un nuovo movimento
    @PostMapping
    public ResponseEntity<MovimentoResponse> creaMovimento(@RequestBody MovimentoRequest request,
                                                           @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(movimentoService.creaMovimento(token, request));
    }

    // 📥 Ritorna tutti i movimenti dell'utente
    @GetMapping
    public ResponseEntity<List<MovimentoResponse>> getMovimenti(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(movimentoService.getMovimentiByUser(token));
    }

    // 🗓️ Ritorna movimenti per intervallo di date
    @GetMapping("/range")
    public ResponseEntity<List<MovimentoResponse>> getMovimentiByRange(
            @RequestHeader("Authorization") String token,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(movimentoService.getMovimentiByDateRange(token, start, end));
    }

    // 📊 Totale ENTRATE
    @GetMapping("/totale/entrate")
    public ResponseEntity<BigDecimal> getTotaleEntrate(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(movimentoService.getTotaleEntrate(token));
    }

    // 📉 Totale USCITE
    @GetMapping("/totale/uscite")
    public ResponseEntity<BigDecimal> getTotaleUscite(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(movimentoService.getTotaleUscite(token));
    }

    // ❌ Elimina un movimento per ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminaMovimento(@PathVariable Long id) {
        movimentoService.eliminaMovimento(id);
        return ResponseEntity.noContent().build();
    }
}

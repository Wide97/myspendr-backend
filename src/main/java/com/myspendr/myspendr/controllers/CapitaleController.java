package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.dto.CapitaleRequest;
import com.myspendr.myspendr.dto.CapitaleResponse;
import com.myspendr.myspendr.dto.ReportCapitaleDTO;
import com.myspendr.myspendr.services.CapitaleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/capitale")
public class CapitaleController {

    private final CapitaleService capitaleService;

    public CapitaleController(CapitaleService capitaleService) {
        this.capitaleService = capitaleService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CapitaleRequest request,
                                    @RequestHeader("Authorization") String token) {
        try {
            CapitaleResponse response = capitaleService.createCapitale(token, request);
            log.info("✅ Capitale creato con successo");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Errore creazione capitale", e);
            return ResponseEntity.internalServerError().body("Errore nella creazione del capitale");
        }
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody CapitaleRequest request,
                                    @RequestHeader("Authorization") String token) {
        try {
            CapitaleResponse response = capitaleService.updateCapitale(token, request);
            log.info("✅ Capitale aggiornato");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Errore aggiornamento capitale", e);
            return ResponseEntity.internalServerError().body("Errore nell'aggiornamento del capitale");
        }
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(capitaleService.getCapitale(token));
        } catch (Exception e) {
            log.error("❌ Errore nel recupero del capitale", e);
            return ResponseEntity.internalServerError().body("Errore nel recupero del capitale");
        }
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String token) {
        try {
            capitaleService.deleteCapitale(token);
            log.info("🗑 Capitale eliminato");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("❌ Errore eliminazione capitale", e);
            return ResponseEntity.internalServerError().body("Errore nell'eliminazione del capitale");
        }
    }

    @PutMapping("/reset")
    public ResponseEntity<?> resetCapitale(@RequestHeader("Authorization") String token) {
        try {
            CapitaleResponse response = capitaleService.resetCapitale(token);
            log.info("🔁 Reset parziale del capitale eseguito");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Errore nel reset parziale", e);
            return ResponseEntity.internalServerError().body("Errore nel reset del capitale");
        }
    }

    @PutMapping("/reset-completo")
    public ResponseEntity<?> resetCapitaleCompleto(@RequestHeader("Authorization") String token) {
        try {
            CapitaleResponse response = capitaleService.resetCapitaleCompleto(token);
            log.info("💥 Reset completo del capitale eseguito");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Errore nel reset completo", e);
            return ResponseEntity.internalServerError().body("Errore nel reset completo del capitale");
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("🏓 Ping richiesto al backend");
        return ResponseEntity.ok("✅ MySpendr backend is alive!");
    }

    @GetMapping("/report/mensile")
    public ResponseEntity<?> getReportMensile(@RequestHeader("Authorization") String token) {
        try {
            List<ReportCapitaleDTO> report = capitaleService.getReportMensile(token);
            log.info("📊 Report mensile generato con {} elementi", report.size());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("❌ Errore generazione report mensile", e);
            return ResponseEntity.internalServerError().body("Errore nel report mensile");
        }
    }

    @GetMapping("/report/annuale")
    public ResponseEntity<?> getReportAnnuale(@RequestHeader("Authorization") String token) {
        try {
            List<ReportCapitaleDTO> report = capitaleService.getReportAnnuale(token);
            log.info("📅 Report annuale generato con {} elementi", report.size());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("❌ Errore generazione report annuale", e);
            return ResponseEntity.internalServerError().body("Errore nel report annuale");
        }
    }
}

package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.dto.CapitaleRequest;
import com.myspendr.myspendr.dto.CapitaleResponse;
import com.myspendr.myspendr.dto.ReportCapitaleDTO;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.services.CapitaleService;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/capitale")
public class CapitaleController {

    private final CapitaleService capitaleService;

    public CapitaleController(CapitaleService capitaleService) {
        this.capitaleService = capitaleService;
    }

    @PostMapping
    public ResponseEntity<CapitaleResponse> create(@RequestBody CapitaleRequest request,
                                                   @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(capitaleService.createCapitale(token, request));
    }

    @PutMapping
    public ResponseEntity<CapitaleResponse> update(@RequestBody CapitaleRequest request,
                                                   @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(capitaleService.updateCapitale(token, request));
    }

    @GetMapping
    public ResponseEntity<CapitaleResponse> get(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(capitaleService.getCapitale(token));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String token) {
        capitaleService.deleteCapitale(token);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reset")
    public ResponseEntity<CapitaleResponse> resetCapitale(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(capitaleService.resetCapitale(token));
    }

    @PutMapping("/reset-completo")
    public ResponseEntity<CapitaleResponse> resetCapitaleCompleto(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(capitaleService.resetCapitaleCompleto(token));
    }


    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("✅ MySpendr backend is alive!");
    }

    @GetMapping("/report/mensile")
    public ResponseEntity<List<ReportCapitaleDTO>> getReportMensile(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(capitaleService.getReportMensile(token));
    }

    @GetMapping("/report/annuale")
    public ResponseEntity<List<ReportCapitaleDTO>> getReportAnnuale(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(capitaleService.getReportAnnuale(token));
    }



}

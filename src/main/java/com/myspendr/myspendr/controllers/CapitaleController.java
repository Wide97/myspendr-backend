package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.dto.CapitaleRequest;
import com.myspendr.myspendr.dto.CapitaleResponse;
import com.myspendr.myspendr.services.CapitaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CapitaleResponse> reset(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(capitaleService.resetCapitale(token));
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("✅ MySpendr backend is alive!");
    }

}

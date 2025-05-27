package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.dto.BudgetRequest;
import com.myspendr.myspendr.dto.BudgetResponse;
import com.myspendr.myspendr.model.CategoriaMovimento;
import com.myspendr.myspendr.services.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/budget")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping("/set")
    public ResponseEntity<Void> setBudget(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody BudgetRequest req) {
        log.info("📤 Richiesta di impostazione budget: {}", req);
        budgetService.setBudget(authHeader, req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/info")
    public ResponseEntity<BudgetResponse> getBudget(@RequestHeader("Authorization") String authHeader,
                                                    @RequestParam CategoriaMovimento categoria,
                                                    @RequestParam int mese,
                                                    @RequestParam int anno) {
        log.info("📥 Richiesta info budget per categoria={}, mese={}, anno={}", categoria, mese, anno);
        BudgetResponse response = budgetService.getBudget(authHeader, categoria, mese, anno);
        return ResponseEntity.ok(response);
    }
}

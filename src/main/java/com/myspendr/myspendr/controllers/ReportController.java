package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.services.ReportExcelService;
import com.myspendr.myspendr.services.ReportPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {

    private final ReportPdfService reportPdfService;
    private final ReportExcelService reportExcelService;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generaPdf(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int mese,
            @RequestParam int anno) {

        byte[] pdf = reportPdfService.generaReportPdfMensile(authHeader, mese, anno);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("report_" + mese + "_" + anno + ".pdf")
                .build());

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> generaExcel(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int mese,
            @RequestParam int anno) {

        byte[] excel = reportExcelService.generaReportExcelMensile(authHeader, mese, anno);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("report_" + mese + "_" + anno + ".xlsx")
                .build());

        return new ResponseEntity<>(excel, headers, HttpStatus.OK);
    }
}

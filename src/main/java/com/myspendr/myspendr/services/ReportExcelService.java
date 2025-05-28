package com.myspendr.myspendr.services;

import com.myspendr.myspendr.model.Movimento;
import com.myspendr.myspendr.model.TipoMovimento;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.MovimentoRepository;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportExcelService {

    private final JwtUtils jwtUtils;
    private final MovimentoRepository movimentoRepository;
    private final UserRepository userRepository;

    public byte[] generaReportExcelMensile(String authHeader, int mese, int anno) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtils.getUsernameFromJwtToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        LocalDate inizio = LocalDate.of(anno, mese, 1);
        LocalDate fine = LocalDate.of(anno, mese, YearMonth.of(anno, mese).lengthOfMonth());

        List<Movimento> movimenti = movimentoRepository
                .findByCapitale_UserAndDataBetweenOrderByDataAsc(user, inizio, fine);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Spese " + mese + "-" + anno);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle entrataStyle = workbook.createCellStyle();
            Font greenFont = workbook.createFont();
            greenFont.setColor(IndexedColors.GREEN.getIndex());
            entrataStyle.setFont(greenFont);

            CellStyle uscitaStyle = workbook.createCellStyle();
            Font redFont = workbook.createFont();
            redFont.setColor(IndexedColors.RED.getIndex());
            uscitaStyle.setFont(redFont);

            Row headerRow = sheet.createRow(0);
            String[] colonne = {"Data", "Tipo", "Categoria", "Importo", "Descrizione"};
            for (int i = 0; i < colonne.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colonne[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            BigDecimal totaleEntrate = BigDecimal.ZERO;
            BigDecimal totaleUscite = BigDecimal.ZERO;

            for (Movimento m : movimenti) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(m.getData().toString());
                row.createCell(1).setCellValue(m.getTipo().name());
                row.createCell(2).setCellValue(m.getCategoria().name());

                Cell importoCell = row.createCell(3);
                importoCell.setCellValue(m.getImporto().doubleValue());
                importoCell.setCellStyle(m.getTipo() == TipoMovimento.ENTRATA ? entrataStyle : uscitaStyle);

                row.createCell(4).setCellValue(m.getDescrizione());

                if (m.getTipo() == TipoMovimento.ENTRATA)
                    totaleEntrate = totaleEntrate.add(m.getImporto());
                else
                    totaleUscite = totaleUscite.add(m.getImporto());
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            totalRow.createCell(2).setCellValue("Totale ENTRATE:");
            Cell entrateTot = totalRow.createCell(3);
            entrateTot.setCellValue(totaleEntrate.doubleValue());
            entrateTot.setCellStyle(entrataStyle);

            Row totalRow2 = sheet.createRow(rowIdx + 2);
            totalRow2.createCell(2).setCellValue("Totale USCITE:");
            Cell usciteTot = totalRow2.createCell(3);
            usciteTot.setCellValue(totaleUscite.doubleValue());
            usciteTot.setCellStyle(uscitaStyle);

            for (int i = 0; i < colonne.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Errore nella generazione del file Excel", e);
        }
    }
}

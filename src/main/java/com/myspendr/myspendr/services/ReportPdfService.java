package com.myspendr.myspendr.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.myspendr.myspendr.model.Movimento;
import com.myspendr.myspendr.model.TipoMovimento;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.MovimentoRepository;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReportPdfService {

    private final JwtUtils jwtUtils;
    private final MovimentoRepository movimentoRepository;
    private final UserRepository userRepository;

    public byte[] generaReportPdfMensile(String authHeader, int mese, int anno) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtils.getUsernameFromJwtToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        LocalDate inizio = LocalDate.of(anno, mese, 1);
        LocalDate fine = LocalDate.of(anno, mese, YearMonth.of(anno, mese).lengthOfMonth());

        List<Movimento> movimenti = movimentoRepository
                .findByCapitale_UserAndDataBetweenOrderByDataAsc(user, inizio, fine);

        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("📄 Report Mensile - " +
                    Month.of(mese).getDisplayName(TextStyle.FULL, Locale.ITALIAN) + " " + anno));
            document.add(new Paragraph("Utente: " + user.getNome()));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            Stream.of("Data", "Tipo", "Categoria", "Importo", "Descrizione").forEach(col -> {
                PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            });

            BigDecimal totaleEntrate = BigDecimal.ZERO;
            BigDecimal totaleUscite = BigDecimal.ZERO;

            for (Movimento m : movimenti) {
                BaseColor bgColor = m.getTipo() == TipoMovimento.ENTRATA
                        ? new BaseColor(204, 255, 204)
                        : new BaseColor(255, 204, 204);

                table.addCell(createColoredCell(m.getData().toString(), bgColor));
                table.addCell(createColoredCell(m.getTipo().name(), bgColor));
                table.addCell(createColoredCell(m.getCategoria().name(), bgColor));
                table.addCell(createColoredCell(m.getImporto().toString(), bgColor));
                table.addCell(createColoredCell(m.getDescrizione(), bgColor));

                if (m.getTipo() == TipoMovimento.ENTRATA)
                    totaleEntrate = totaleEntrate.add(m.getImporto());
                else
                    totaleUscite = totaleUscite.add(m.getImporto());
            }

            document.add(table);
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Totale ENTRATE: " + totaleEntrate + " €"));
            document.add(new Paragraph("Totale USCITE: " + totaleUscite + " €"));

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Errore nella generazione PDF", e);
        }
    }

    private PdfPCell createColoredCell(String text, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setBackgroundColor(bgColor);
        return cell;
    }
}

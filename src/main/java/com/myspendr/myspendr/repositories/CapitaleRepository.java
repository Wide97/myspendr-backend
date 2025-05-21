package com.myspendr.myspendr.repositories;

import com.myspendr.myspendr.dto.ReportCapitaleDTO;
import com.myspendr.myspendr.model.Capitale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CapitaleRepository extends JpaRepository<Capitale, Long> {
    Optional<Capitale> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);


    @Query("SELECT NEW com.myspendr.myspendr.dto.ReportCapitaleDTO(" +
            "FUNCTION('to_char', c.data, 'YYYY-MM'), " +
            "SUM(c.valore), " +
            "SUM(c.variazione)) " +
            "FROM Capitale c WHERE c.user.id = :userId " +
            "GROUP BY FUNCTION('to_char', c.data, 'YYYY-MM') " +
            "ORDER BY FUNCTION('to_char', c.data, 'YYYY-MM')")
    List<ReportCapitaleDTO> getReportMensileByUserId(@Param("userId") UUID userId);

    @Query("SELECT NEW com.myspendr.myspendr.dto.ReportCapitaleDTO(" +
            "FUNCTION('to_char', c.data, 'YYYY'), " +
            "SUM(c.valore), " +
            "SUM(c.variazione)) " +
            "FROM Capitale c WHERE c.user.id = :userId " +
            "GROUP BY FUNCTION('to_char', c.data, 'YYYY') " +
            "ORDER BY FUNCTION('to_char', c.data, 'YYYY')")
    List<ReportCapitaleDTO> getReportAnnualeByUserId(@Param("userId") UUID userId);




}


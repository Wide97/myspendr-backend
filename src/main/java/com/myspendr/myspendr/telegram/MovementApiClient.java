
package com.myspendr.myspendr.telegram;

import org.springframework.stereotype.Service;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@Service
public class MovementApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl = "https://api.myspendr.duckdns.org"; // cambia se necessario
    private final String token = "Bearer TUO_JWT"; // genera con /auth/login

    public MovementApiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public void inviaMovimento(BigDecimal importo, String categoria, String descrizione, String tipo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.replace("Bearer ", ""));

        Map<String, Object> body = new HashMap<>();
        body.put("importo", importo);
        body.put("categoria", categoria);
        body.put("descrizione", descrizione);
        body.put("tipo", tipo);
        body.put("data", LocalDate.now().toString());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(baseUrl + "/movimenti", entity, Void.class);
    }
}

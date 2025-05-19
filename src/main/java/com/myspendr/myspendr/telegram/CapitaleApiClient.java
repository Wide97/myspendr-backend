package com.myspendr.myspendr.telegram;

import com.myspendr.myspendr.dto.CapitaleResponse;
import org.springframework.stereotype.Service;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;


@Service
public class CapitaleApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl = "https://api.myspendr.duckdns.org";
    private final String token = "Bearer TUO_JWT";

    public CapitaleApiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public CapitaleResponse getCapitale() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.replace("Bearer ", ""));
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<CapitaleResponse> response = restTemplate.exchange(
                baseUrl + "/capitale",
                HttpMethod.GET,
                entity,
                CapitaleResponse.class
        );
        return response.getBody();
    }
}

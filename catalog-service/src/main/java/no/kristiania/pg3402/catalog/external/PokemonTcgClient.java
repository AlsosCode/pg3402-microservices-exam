package no.kristiania.pg3402.catalog.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Component
@Slf4j
@RequiredArgsConstructor
public class PokemonTcgClient {

    private final RestTemplate restTemplate;

    @Value("${pokemontcg.api.key}")
    private String apiKey;

    @Value("${pokemontcg.api.base-url:https://api.pokemontcg.io/v2}")
    private String baseUrl;

    public String getSets(int page, int pageSize) {
        String url = String.format("%s/sets?page=%d&pageSize=%d", baseUrl, page, pageSize);
        log.info("Fetching sets from Pokemon TCG API: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    public String getCards(String setId, int page, int pageSize) {
        String url = String.format("%s/cards?q=set.id:%s&page=%d&pageSize=%d", baseUrl, setId, page, pageSize);
        log.info("Fetching cards from Pokemon TCG API: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    public String getSet(String setId) {
        String url = String.format("%s/sets/%s", baseUrl, setId);
        log.info("Fetching set from Pokemon TCG API: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.info("Successfully fetched set {} from Pokemon TCG API", setId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch set {} from Pokemon TCG API: {}", setId, e.getMessage());
            throw new RuntimeException("Failed to fetch set from Pokemon TCG API: " + e.getMessage(), e);
        }
    }
}

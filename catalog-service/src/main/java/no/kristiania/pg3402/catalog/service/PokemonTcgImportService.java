package no.kristiania.pg3402.catalog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.pg3402.catalog.external.PokemonTcgClient;
import no.kristiania.pg3402.catalog.model.Card;
import no.kristiania.pg3402.catalog.model.CardSet;
import no.kristiania.pg3402.catalog.repository.CardRepository;
import no.kristiania.pg3402.catalog.repository.CardSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PokemonTcgImportService {

    private final PokemonTcgClient pokemonTcgClient;
    private final CardSetRepository cardSetRepository;
    private final CardRepository cardRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public String importSet(String setId) {
        try {
            log.info("Importing set: {}", setId);

            // Fetch set details
            String setJson = pokemonTcgClient.getSet(setId);
            JsonNode setData = objectMapper.readTree(setJson).get("data");

            // Create or update set
            CardSet cardSet = mapToCardSet(setData);
            CardSet savedSet = cardSetRepository.findBySetCode(cardSet.getSetCode())
                    .orElse(cardSet);

            if (savedSet.getId() == null) {
                savedSet = cardSetRepository.save(cardSet);
                log.info("Created new set: {}", savedSet.getName());
            }

            // Fetch and import cards
            int page = 1;
            int imported = 0;
            boolean hasMore = true;

            while (hasMore) {
                String cardsJson = pokemonTcgClient.getCards(setId, page, 250);
                JsonNode cardsData = objectMapper.readTree(cardsJson);
                JsonNode cards = cardsData.get("data");

                if (cards == null || cards.size() == 0) {
                    hasMore = false;
                } else {
                    for (JsonNode cardNode : cards) {
                        Card card = mapToCard(cardNode, savedSet);
                        // Check if card already exists
                        if (cardRepository.findByCardSetAndCardNumber(savedSet, card.getCardNumber()).isEmpty()) {
                            cardRepository.save(card);
                            imported++;
                        }
                    }
                    page++;

                    // Check if there are more pages
                    int totalCount = cardsData.get("totalCount").asInt();
                    int pageSize = cardsData.get("pageSize").asInt();
                    hasMore = (page * pageSize) < totalCount;
                }
            }

            log.info("Import completed. Imported {} cards for set {}", imported, savedSet.getName());
            return String.format("Successfully imported %d cards for set %s", imported, savedSet.getName());

        } catch (Exception e) {
            log.error("Error importing set {}: {}", setId, e.getMessage(), e);
            throw new RuntimeException("Failed to import set: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<String> importRecentSets(int count) {
        try {
            List<String> results = new ArrayList<>();
            String setsJson = pokemonTcgClient.getSets(1, count);
            JsonNode setsData = objectMapper.readTree(setsJson).get("data");

            for (JsonNode setNode : setsData) {
                String setId = setNode.get("id").asText();
                try {
                    String result = importSet(setId);
                    results.add(result);
                } catch (Exception e) {
                    log.error("Failed to import set {}: {}", setId, e.getMessage());
                    results.add("Failed to import set " + setId + ": " + e.getMessage());
                }
            }

            return results;
        } catch (Exception e) {
            log.error("Error importing recent sets: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to import recent sets: " + e.getMessage(), e);
        }
    }

    private CardSet mapToCardSet(JsonNode setNode) {
        CardSet cardSet = new CardSet();
        cardSet.setSetCode(setNode.get("id").asText());
        cardSet.setName(setNode.get("name").asText());
        cardSet.setGameType(CardSet.GameType.POKEMON);

        if (setNode.has("series")) {
            cardSet.setDescription(setNode.get("series").asText());
        }

        if (setNode.has("releaseDate")) {
            String releaseDateStr = setNode.get("releaseDate").asText();
            try {
                cardSet.setReleaseDate(LocalDate.parse(releaseDateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            } catch (Exception e) {
                log.warn("Could not parse release date: {}", releaseDateStr);
            }
        }

        if (setNode.has("total")) {
            cardSet.setTotalCards(setNode.get("total").asInt());
        }

        if (setNode.has("images") && setNode.get("images").has("logo")) {
            cardSet.setLogoUrl(setNode.get("images").get("logo").asText());
        }

        return cardSet;
    }

    private Card mapToCard(JsonNode cardNode, CardSet cardSet) {
        Card card = new Card();
        card.setCardSet(cardSet);
        card.setCardNumber(cardNode.get("number").asText());
        card.setName(cardNode.get("name").asText());

        if (cardNode.has("rarity")) {
            String rarityStr = cardNode.get("rarity").asText().toUpperCase()
                    .replace(" ", "_")
                    .replace("-", "_");
            try {
                card.setRarity(Card.Rarity.valueOf(rarityStr));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown rarity: {}. Setting to COMMON", rarityStr);
                card.setRarity(Card.Rarity.COMMON);
            }
        }

        if (cardNode.has("subtypes") && cardNode.get("subtypes").size() > 0) {
            card.setVariant(cardNode.get("subtypes").get(0).asText());
        }

        if (cardNode.has("images") && cardNode.get("images").has("small")) {
            card.setImageUrl(cardNode.get("images").get("small").asText());
        }

        if (cardNode.has("flavorText")) {
            card.setDescription(cardNode.get("flavorText").asText());
        }

        if (cardNode.has("types") && cardNode.get("types").size() > 0) {
            card.setType(cardNode.get("types").get(0).asText());
        }

        if (cardNode.has("artist")) {
            card.setArtist(cardNode.get("artist").asText());
        }

        return card;
    }
}

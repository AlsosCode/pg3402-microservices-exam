package no.kristiania.pg3402.catalog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.pg3402.catalog.dto.CardDTO;
import no.kristiania.pg3402.catalog.dto.CardSetDTO;
import no.kristiania.pg3402.catalog.model.Card;
import no.kristiania.pg3402.catalog.model.CardSet;
import no.kristiania.pg3402.catalog.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/sets")
    public ResponseEntity<List<CardSetDTO>> getAllSets(
            @RequestParam(required = false) String gameType) {
        log.info("GET /sets - gameType: {}", gameType);

        List<CardSet> sets;
        if (gameType != null) {
            CardSet.GameType type = CardSet.GameType.valueOf(gameType.toUpperCase());
            sets = catalogService.getSetsByGameType(type);
        } else {
            sets = catalogService.getAllSets();
        }

        return ResponseEntity.ok(sets.stream()
                .map(CardSetDTO::fromEntity)
                .collect(Collectors.toList()));
    }

    @GetMapping("/sets/{setCode}")
    public ResponseEntity<CardSet> getSetByCode(@PathVariable String setCode) {
        log.info("GET /sets/{}", setCode);
        return ResponseEntity.ok(catalogService.getSetByCode(setCode));
    }

    @PostMapping("/sets")
    public ResponseEntity<CardSet> createSet(@RequestBody CardSet cardSet) {
        log.info("POST /sets - Creating set: {}", cardSet.getSetCode());
        return ResponseEntity.ok(catalogService.createSet(cardSet));
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardDTO>> searchCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String setCode,
            @RequestParam(required = false) String rarity) {
        log.info("GET /cards - name: {}, setCode: {}, rarity: {}", name, setCode, rarity);

        Card.Rarity rarityEnum = rarity != null ? Card.Rarity.valueOf(rarity.toUpperCase()) : null;
        List<Card> cards = catalogService.searchCards(name, setCode, rarityEnum);

        return ResponseEntity.ok(cards.stream()
                .map(CardDTO::fromEntity)
                .collect(Collectors.toList()));
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<Card> getCardById(@PathVariable Long id) {
        log.info("GET /cards/{}", id);
        return ResponseEntity.ok(catalogService.getCardById(id));
    }

    @GetMapping("/sets/{setCode}/cards")
    public ResponseEntity<List<CardDTO>> getCardsBySet(@PathVariable String setCode) {
        log.info("GET /sets/{}/cards", setCode);
        List<Card> cards = catalogService.getCardsBySet(setCode);

        return ResponseEntity.ok(cards.stream()
                .map(CardDTO::fromEntity)
                .collect(Collectors.toList()));
    }

    @GetMapping("/sets/{setCode}/cards/{cardNumber}")
    public ResponseEntity<Card> getCardBySetAndNumber(
            @PathVariable String setCode,
            @PathVariable String cardNumber) {
        log.info("GET /sets/{}/cards/{}", setCode, cardNumber);
        return ResponseEntity.ok(catalogService.getCardBySetAndNumber(setCode, cardNumber));
    }

    @PostMapping("/cards")
    public ResponseEntity<Card> createCard(@RequestBody Card card) {
        log.info("POST /cards - Creating card: {}", card.getName());
        return ResponseEntity.ok(catalogService.createCard(card));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Catalog Service is healthy");
    }
}

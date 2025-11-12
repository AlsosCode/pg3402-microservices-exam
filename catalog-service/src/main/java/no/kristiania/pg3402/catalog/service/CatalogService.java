package no.kristiania.pg3402.catalog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.pg3402.catalog.model.Card;
import no.kristiania.pg3402.catalog.model.CardSet;
import no.kristiania.pg3402.catalog.repository.CardRepository;
import no.kristiania.pg3402.catalog.repository.CardSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final CardSetRepository cardSetRepository;
    private final CardRepository cardRepository;

    // CardSet operations
    @Transactional(readOnly = true)
    public List<CardSet> getAllSets() {
        log.debug("Fetching all card sets");
        return cardSetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CardSet getSetByCode(String setCode) {
        log.debug("Fetching card set by code: {}", setCode);
        return cardSetRepository.findBySetCode(setCode)
                .orElseThrow(() -> new RuntimeException("Set not found: " + setCode));
    }

    @Transactional(readOnly = true)
    public List<CardSet> getSetsByGameType(CardSet.GameType gameType) {
        log.debug("Fetching card sets by game type: {}", gameType);
        return cardSetRepository.findByGameType(gameType);
    }

    @Transactional
    public CardSet createSet(CardSet cardSet) {
        log.info("Creating new card set: {}", cardSet.getSetCode());
        return cardSetRepository.save(cardSet);
    }

    // Card operations
    @Transactional(readOnly = true)
    public List<Card> getCardsBySet(String setCode) {
        log.debug("Fetching cards for set: {}", setCode);
        CardSet cardSet = getSetByCode(setCode);
        return cardRepository.findByCardSet(cardSet);
    }

    @Transactional(readOnly = true)
    public Card getCardById(Long id) {
        log.debug("Fetching card by id: {}", id);
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found: " + id));
    }

    @Transactional(readOnly = true)
    public Card getCardBySetAndNumber(String setCode, String cardNumber) {
        log.debug("Fetching card by set {} and number {}", setCode, cardNumber);
        CardSet cardSet = getSetByCode(setCode);
        return cardRepository.findByCardSetAndCardNumber(cardSet, cardNumber)
                .orElseThrow(() -> new RuntimeException("Card not found: " + setCode + "/" + cardNumber));
    }

    @Transactional(readOnly = true)
    public List<Card> searchCards(String name, String setCode, Card.Rarity rarity) {
        log.debug("Searching cards with name: {}, setCode: {}, rarity: {}", name, setCode, rarity);
        return cardRepository.searchCards(name, setCode, rarity);
    }

    @Transactional
    public Card createCard(Card card) {
        log.info("Creating new card: {} in set {}", card.getName(), card.getCardSet().getSetCode());
        return cardRepository.save(card);
    }
}

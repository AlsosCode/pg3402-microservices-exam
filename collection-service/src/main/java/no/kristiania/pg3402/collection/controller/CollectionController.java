package no.kristiania.pg3402.collection.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.pg3402.collection.dto.UserCardRequest;
import no.kristiania.pg3402.collection.dto.UserCardResponse;
import no.kristiania.pg3402.collection.service.CollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
@Slf4j
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * Add a card to user's collection
     * POST /api/collections/users/{userId}/cards
     */
    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<UserCardResponse> addCard(
            @PathVariable Long userId,
            @Valid @RequestBody UserCardRequest request) {
        log.info("Request to add card {} to user {}'s collection", request.getCardId(), userId);
        UserCardResponse response = collectionService.addCardToCollection(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all cards in user's collection
     * GET /api/collections/users/{userId}/cards
     */
    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<List<UserCardResponse>> getUserCollection(@PathVariable Long userId) {
        log.info("Request to get collection for user {}", userId);
        List<UserCardResponse> collection = collectionService.getUserCollection(userId);
        return ResponseEntity.ok(collection);
    }

    /**
     * Get a specific card from user's collection
     * GET /api/collections/users/{userId}/cards/{cardId}
     */
    @GetMapping("/users/{userId}/cards/{cardId}")
    public ResponseEntity<UserCardResponse> getUserCard(
            @PathVariable Long userId,
            @PathVariable Long cardId) {
        log.info("Request to get card {} for user {}", cardId, userId);
        UserCardResponse response = collectionService.getUserCard(userId, cardId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a card in user's collection
     * PUT /api/collections/users/{userId}/cards/{cardId}
     */
    @PutMapping("/users/{userId}/cards/{cardId}")
    public ResponseEntity<UserCardResponse> updateUserCard(
            @PathVariable Long userId,
            @PathVariable Long cardId,
            @Valid @RequestBody UserCardRequest request) {
        log.info("Request to update card {} for user {}", cardId, userId);
        UserCardResponse response = collectionService.updateUserCard(userId, cardId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove a card from user's collection
     * DELETE /api/collections/users/{userId}/cards/{cardId}
     */
    @DeleteMapping("/users/{userId}/cards/{cardId}")
    public ResponseEntity<Void> removeCard(
            @PathVariable Long userId,
            @PathVariable Long cardId) {
        log.info("Request to remove card {} from user {}'s collection", cardId, userId);
        collectionService.removeCardFromCollection(userId, cardId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get collection statistics for a user
     * GET /api/collections/users/{userId}/stats
     */
    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<CollectionService.CollectionStats> getCollectionStats(@PathVariable Long userId) {
        log.info("Request to get collection stats for user {}", userId);
        CollectionService.CollectionStats stats = collectionService.getCollectionStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get list of card IDs owned by a user (for progress service)
     * GET /api/collections/users/{userId}/card-ids
     */
    @GetMapping("/users/{userId}/card-ids")
    public ResponseEntity<List<Long>> getUserCardIds(@PathVariable Long userId) {
        log.info("Request to get card IDs for user {}", userId);
        List<Long> cardIds = collectionService.getUserCardIds(userId);
        return ResponseEntity.ok(cardIds);
    }
}

package no.kristiania.pg3402.collection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.pg3402.collection.config.RabbitMQConfig;
import no.kristiania.pg3402.collection.dto.UserCardRequest;
import no.kristiania.pg3402.collection.dto.UserCardResponse;
import no.kristiania.pg3402.collection.events.CardAddedEvent;
import no.kristiania.pg3402.collection.events.CardRemovedEvent;
import no.kristiania.pg3402.collection.events.CardUpdatedEvent;
import no.kristiania.pg3402.collection.exception.CardAlreadyOwnedException;
import no.kristiania.pg3402.collection.exception.CardNotFoundException;
import no.kristiania.pg3402.collection.model.UserCard;
import no.kristiania.pg3402.collection.repository.UserCardRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionService {

    private final UserCardRepository userCardRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Add a card to user's collection
     */
    @Transactional
    public UserCardResponse addCardToCollection(Long userId, UserCardRequest request) {
        log.info("Adding card {} to user {}'s collection", request.getCardId(), userId);

        // Check if user already owns this card
        if (userCardRepository.existsByUserIdAndCardId(userId, request.getCardId())) {
            throw new CardAlreadyOwnedException(
                    String.format("User %d already owns card %d", userId, request.getCardId())
            );
        }

        UserCard userCard = UserCard.builder()
                .userId(userId)
                .cardId(request.getCardId())
                .quantity(request.getQuantity())
                .condition(request.getCondition())
                .isReverseHolo(request.getIsReverseHolo())
                .notes(request.getNotes())
                .acquiredDate(request.getAcquiredDate())
                .build();

        UserCard savedCard = userCardRepository.save(userCard);
        log.info("Successfully added card {} to user {}'s collection", request.getCardId(), userId);

        // Publish CardAddedEvent to RabbitMQ
        CardAddedEvent event = new CardAddedEvent(
                userId,
                request.getCardId(),
                request.getQuantity(),
                request.getCondition().name(),
                request.getIsReverseHolo()
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
        log.info("Published CardAddedEvent for card {} and user {}", request.getCardId(), userId);

        return UserCardResponse.fromEntity(savedCard);
    }

    /**
     * Get all cards in user's collection
     */
    @Transactional(readOnly = true)
    public List<UserCardResponse> getUserCollection(Long userId) {
        log.info("Fetching collection for user {}", userId);
        List<UserCard> userCards = userCardRepository.findByUserId(userId);
        return userCards.stream()
                .map(UserCardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific card from user's collection
     */
    @Transactional(readOnly = true)
    public UserCardResponse getUserCard(Long userId, Long cardId) {
        log.info("Fetching card {} for user {}", cardId, userId);
        UserCard userCard = userCardRepository.findByUserIdAndCardId(userId, cardId)
                .orElseThrow(() -> new CardNotFoundException(
                        String.format("Card %d not found in user %d's collection", cardId, userId)
                ));
        return UserCardResponse.fromEntity(userCard);
    }

    /**
     * Update a card in user's collection
     */
    @Transactional
    public UserCardResponse updateUserCard(Long userId, Long cardId, UserCardRequest request) {
        log.info("Updating card {} for user {}", cardId, userId);

        UserCard userCard = userCardRepository.findByUserIdAndCardId(userId, cardId)
                .orElseThrow(() -> new CardNotFoundException(
                        String.format("Card %d not found in user %d's collection", cardId, userId)
                ));

        userCard.setQuantity(request.getQuantity());
        userCard.setCondition(request.getCondition());
        userCard.setIsReverseHolo(request.getIsReverseHolo());
        userCard.setNotes(request.getNotes());
        if (request.getAcquiredDate() != null) {
            userCard.setAcquiredDate(request.getAcquiredDate());
        }

        UserCard updatedCard = userCardRepository.save(userCard);
        log.info("Successfully updated card {} for user {}", cardId, userId);

        // Publish CardUpdatedEvent to RabbitMQ
        CardUpdatedEvent event = new CardUpdatedEvent(
                userId,
                cardId,
                request.getQuantity(),
                request.getCondition().name()
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
        log.info("Published CardUpdatedEvent for card {} and user {}", cardId, userId);

        return UserCardResponse.fromEntity(updatedCard);
    }

    /**
     * Remove a card from user's collection
     */
    @Transactional
    public void removeCardFromCollection(Long userId, Long cardId) {
        log.info("Removing card {} from user {}'s collection", cardId, userId);

        if (!userCardRepository.existsByUserIdAndCardId(userId, cardId)) {
            throw new CardNotFoundException(
                    String.format("Card %d not found in user %d's collection", cardId, userId)
            );
        }

        userCardRepository.deleteByUserIdAndCardId(userId, cardId);
        log.info("Successfully removed card {} from user {}'s collection", cardId, userId);

        // Publish CardRemovedEvent to RabbitMQ
        CardRemovedEvent event = new CardRemovedEvent(userId, cardId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
        log.info("Published CardRemovedEvent for card {} and user {}", cardId, userId);
    }

    /**
     * Get collection statistics for a user
     */
    @Transactional(readOnly = true)
    public CollectionStats getCollectionStats(Long userId) {
        log.info("Fetching collection stats for user {}", userId);

        Long uniqueCards = userCardRepository.countUniqueCardsByUserId(userId);
        Long totalCards = userCardRepository.sumQuantityByUserId(userId);

        return CollectionStats.builder()
                .userId(userId)
                .uniqueCards(uniqueCards != null ? uniqueCards : 0L)
                .totalCards(totalCards != null ? totalCards : 0L)
                .build();
    }

    /**
     * Get list of card IDs owned by a user (for progress service)
     */
    @Transactional(readOnly = true)
    public List<Long> getUserCardIds(Long userId) {
        log.info("Fetching card IDs for user {}", userId);
        return userCardRepository.findCardIdsByUserId(userId);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CollectionStats {
        private Long userId;
        private Long uniqueCards;
        private Long totalCards;
    }
}

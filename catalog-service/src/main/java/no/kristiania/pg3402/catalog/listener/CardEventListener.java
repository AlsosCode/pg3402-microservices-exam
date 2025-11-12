package no.kristiania.pg3402.catalog.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.pg3402.catalog.events.CardAddedEvent;
import no.kristiania.pg3402.catalog.events.CardRemovedEvent;
import no.kristiania.pg3402.catalog.events.CardUpdatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens to card events published by Collection Service
 * This demonstrates asynchronous communication between microservices
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CardEventListener {

    /**
     * Handle CardAddedEvent
     * In a real system, this could:
     * - Update search index
     * - Update analytics
     * - Trigger notifications
     * - Update card popularity statistics
     */
    @RabbitListener(queues = "card.events.queue")
    public void handleCardAddedEvent(CardAddedEvent event) {
        log.info("=== ASYNC EVENT RECEIVED ===");
        log.info("Event: CardAdded");
        log.info("User {} added card {} to their collection", event.getUserId(), event.getCardId());
        log.info("Quantity: {}, Condition: {}, Reverse Holo: {}",
                event.getQuantity(),
                event.getCondition(),
                event.getIsReverseHolo());
        log.info("Timestamp: {}", event.getTimestamp());
        log.info("===========================");

        // TODO: In future, implement actual business logic here:
        // - Update card popularity counter
        // - Index for search service
        // - Send notification to friends who want this card
    }

    /**
     * Handle CardRemovedEvent
     */
    @RabbitListener(queues = "card.events.queue")
    public void handleCardRemovedEvent(CardRemovedEvent event) {
        log.info("=== ASYNC EVENT RECEIVED ===");
        log.info("Event: CardRemoved");
        log.info("User {} removed card {} from their collection", event.getUserId(), event.getCardId());
        log.info("Timestamp: {}", event.getTimestamp());
        log.info("===========================");

        // TODO: Update statistics, search index, etc.
    }

    /**
     * Handle CardUpdatedEvent
     */
    @RabbitListener(queues = "card.events.queue")
    public void handleCardUpdatedEvent(CardUpdatedEvent event) {
        log.info("=== ASYNC EVENT RECEIVED ===");
        log.info("Event: CardUpdated");
        log.info("User {} updated card {} in their collection", event.getUserId(), event.getCardId());
        log.info("New Quantity: {}, New Condition: {}",
                event.getNewQuantity(),
                event.getNewCondition());
        log.info("Timestamp: {}", event.getTimestamp());
        log.info("===========================");

        // TODO: Update search index, analytics, etc.
    }
}

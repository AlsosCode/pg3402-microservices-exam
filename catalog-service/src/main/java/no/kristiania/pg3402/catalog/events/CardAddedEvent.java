package no.kristiania.pg3402.catalog.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Event published when a card is added to a user's collection
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardAddedEvent implements Serializable {
    private Long userId;
    private Long cardId;
    private Integer quantity;
    private String condition;
    private Boolean isReverseHolo;
    private Instant timestamp;

    public CardAddedEvent(Long userId, Long cardId, Integer quantity, String condition, Boolean isReverseHolo) {
        this.userId = userId;
        this.cardId = cardId;
        this.quantity = quantity;
        this.condition = condition;
        this.isReverseHolo = isReverseHolo;
        this.timestamp = Instant.now();
    }
}

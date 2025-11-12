package no.kristiania.pg3402.collection.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Event published when a card is removed from a user's collection
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRemovedEvent implements Serializable {
    private Long userId;
    private Long cardId;
    private Instant timestamp;

    public CardRemovedEvent(Long userId, Long cardId) {
        this.userId = userId;
        this.cardId = cardId;
        this.timestamp = Instant.now();
    }
}

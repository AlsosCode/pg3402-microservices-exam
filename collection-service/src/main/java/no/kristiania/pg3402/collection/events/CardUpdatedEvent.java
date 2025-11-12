package no.kristiania.pg3402.collection.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Event published when a card in a user's collection is updated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardUpdatedEvent implements Serializable {
    private Long userId;
    private Long cardId;
    private Integer newQuantity;
    private String newCondition;
    private Instant timestamp;

    public CardUpdatedEvent(Long userId, Long cardId, Integer newQuantity, String newCondition) {
        this.userId = userId;
        this.cardId = cardId;
        this.newQuantity = newQuantity;
        this.newCondition = newCondition;
        this.timestamp = Instant.now();
    }
}

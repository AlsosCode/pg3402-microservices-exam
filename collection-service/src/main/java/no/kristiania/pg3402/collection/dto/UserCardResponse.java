package no.kristiania.pg3402.collection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.kristiania.pg3402.collection.model.UserCard;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCardResponse {

    private Long id;
    private Long userId;
    private Long cardId;
    private Integer quantity;
    private UserCard.CardCondition condition;
    private Boolean isReverseHolo;
    private String notes;
    private LocalDateTime acquiredDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserCardResponse fromEntity(UserCard userCard) {
        return UserCardResponse.builder()
                .id(userCard.getId())
                .userId(userCard.getUserId())
                .cardId(userCard.getCardId())
                .quantity(userCard.getQuantity())
                .condition(userCard.getCondition())
                .isReverseHolo(userCard.getIsReverseHolo())
                .notes(userCard.getNotes())
                .acquiredDate(userCard.getAcquiredDate())
                .createdAt(userCard.getCreatedAt())
                .updatedAt(userCard.getUpdatedAt())
                .build();
    }
}

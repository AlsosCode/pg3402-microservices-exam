package no.kristiania.pg3402.collection.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class UserCardRequest {

    @NotNull(message = "Card ID is required")
    private Long cardId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    private UserCard.CardCondition condition = UserCard.CardCondition.NEAR_MINT;

    private Boolean isReverseHolo = false;

    private String notes;

    private LocalDateTime acquiredDate;
}

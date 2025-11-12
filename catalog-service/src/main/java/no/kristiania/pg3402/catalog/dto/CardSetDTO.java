package no.kristiania.pg3402.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.kristiania.pg3402.catalog.model.CardSet;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardSetDTO {
    private Long id;
    private String setCode;
    private String name;
    private String description;
    private String gameType;
    private LocalDate releaseDate;
    private Integer totalCards;
    private String logoUrl;
    private Integer cardCount;

    public static CardSetDTO fromEntity(CardSet cardSet) {
        CardSetDTO dto = new CardSetDTO();
        dto.setId(cardSet.getId());
        dto.setSetCode(cardSet.getSetCode());
        dto.setName(cardSet.getName());
        dto.setDescription(cardSet.getDescription());
        dto.setGameType(cardSet.getGameType().name());
        dto.setReleaseDate(cardSet.getReleaseDate());
        dto.setTotalCards(cardSet.getTotalCards());
        dto.setLogoUrl(cardSet.getLogoUrl());
        dto.setCardCount(cardSet.getCards() != null ? cardSet.getCards().size() : 0);
        return dto;
    }
}

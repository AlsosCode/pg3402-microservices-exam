package no.kristiania.pg3402.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.kristiania.pg3402.catalog.model.Card;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    private Long id;
    private String setCode;
    private String setName;
    private String cardNumber;
    private String name;
    private String rarity;
    private String variant;
    private String imageUrl;
    private String description;
    private String type;
    private String artist;

    public static CardDTO fromEntity(Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setSetCode(card.getCardSet().getSetCode());
        dto.setSetName(card.getCardSet().getName());
        dto.setCardNumber(card.getCardNumber());
        dto.setName(card.getName());
        dto.setRarity(card.getRarity() != null ? card.getRarity().name() : null);
        dto.setVariant(card.getVariant());
        dto.setImageUrl(card.getImageUrl());
        dto.setDescription(card.getDescription());
        dto.setType(card.getType());
        dto.setArtist(card.getArtist());
        return dto;
    }
}

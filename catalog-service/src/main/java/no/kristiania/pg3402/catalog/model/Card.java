package no.kristiania.pg3402.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cards",
       uniqueConstraints = @UniqueConstraint(columnNames = {"card_set_id", "card_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_set_id", nullable = false)
    @JsonIgnoreProperties("cards")
    private CardSet cardSet;

    @Column(nullable = false)
    private String cardNumber;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    private String variant; // e.g., "Holo", "Reverse Holo", "Full Art"

    private String imageUrl;

    @Column(length = 2000)
    private String description;

    private String type; // e.g., "Fire", "Water" for Pokemon; "Character", "Event" for One Piece

    private String artist;

    public enum Rarity {
        COMMON,
        UNCOMMON,
        RARE,
        HOLO_RARE,
        ULTRA_RARE,
        SECRET_RARE,
        PROMO
    }
}

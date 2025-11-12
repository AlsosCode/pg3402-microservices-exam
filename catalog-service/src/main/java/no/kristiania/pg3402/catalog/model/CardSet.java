package no.kristiania.pg3402.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "card_sets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String setCode;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameType gameType;

    private LocalDate releaseDate;

    private Integer totalCards;

    private String logoUrl;

    @OneToMany(mappedBy = "cardSet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("cardSet")
    private List<Card> cards = new ArrayList<>();

    public enum GameType {
        POKEMON,
        ONE_PIECE
    }
}

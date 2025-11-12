package no.kristiania.pg3402.collection.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_cards",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "card_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardCondition condition = CardCondition.NEAR_MINT;

    @Column(name = "is_reverse_holo")
    private Boolean isReverseHolo = false;

    @Column(length = 1000)
    private String notes;

    @Column(name = "acquired_date")
    private LocalDateTime acquiredDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (acquiredDate == null) {
            acquiredDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CardCondition {
        MINT,           // Perfect condition
        NEAR_MINT,      // Slight wear
        EXCELLENT,      // Minor wear
        GOOD,           // Noticeable wear
        LIGHT_PLAYED,   // Visible wear
        PLAYED,         // Heavy wear
        POOR            // Damaged
    }
}

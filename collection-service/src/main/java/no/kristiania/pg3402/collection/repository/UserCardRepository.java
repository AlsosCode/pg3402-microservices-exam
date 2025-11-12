package no.kristiania.pg3402.collection.repository;

import no.kristiania.pg3402.collection.model.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCardRepository extends JpaRepository<UserCard, Long> {

    /**
     * Find all cards owned by a specific user
     */
    List<UserCard> findByUserId(Long userId);

    /**
     * Find a specific card for a user
     */
    Optional<UserCard> findByUserIdAndCardId(Long userId, Long cardId);

    /**
     * Check if a user owns a specific card
     */
    boolean existsByUserIdAndCardId(Long userId, Long cardId);

    /**
     * Get total number of unique cards owned by a user
     */
    @Query("SELECT COUNT(DISTINCT uc.cardId) FROM UserCard uc WHERE uc.userId = :userId")
    Long countUniqueCardsByUserId(@Param("userId") Long userId);

    /**
     * Get total quantity of all cards owned by a user
     */
    @Query("SELECT SUM(uc.quantity) FROM UserCard uc WHERE uc.userId = :userId")
    Long sumQuantityByUserId(@Param("userId") Long userId);

    /**
     * Find all reverse holo cards for a user
     */
    List<UserCard> findByUserIdAndIsReverseHoloTrue(Long userId);

    /**
     * Find cards by user and condition
     */
    List<UserCard> findByUserIdAndCondition(Long userId, UserCard.CardCondition condition);

    /**
     * Delete a specific card from user's collection
     */
    void deleteByUserIdAndCardId(Long userId, Long cardId);

    /**
     * Get list of card IDs owned by a user (for progress calculation)
     */
    @Query("SELECT uc.cardId FROM UserCard uc WHERE uc.userId = :userId")
    List<Long> findCardIdsByUserId(@Param("userId") Long userId);
}

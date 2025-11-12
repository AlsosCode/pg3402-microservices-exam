package no.kristiania.pg3402.catalog.repository;

import no.kristiania.pg3402.catalog.model.Card;
import no.kristiania.pg3402.catalog.model.CardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByCardSet(CardSet cardSet);

    Optional<Card> findByCardSetAndCardNumber(CardSet cardSet, String cardNumber);

    List<Card> findByNameContainingIgnoreCase(String name);

    List<Card> findByCardSetAndRarity(CardSet cardSet, Card.Rarity rarity);

    @Query("SELECT c FROM Card c WHERE " +
           "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:setCode IS NULL OR c.cardSet.setCode = :setCode) AND " +
           "(:rarity IS NULL OR c.rarity = :rarity)")
    List<Card> searchCards(@Param("name") String name,
                          @Param("setCode") String setCode,
                          @Param("rarity") Card.Rarity rarity);
}

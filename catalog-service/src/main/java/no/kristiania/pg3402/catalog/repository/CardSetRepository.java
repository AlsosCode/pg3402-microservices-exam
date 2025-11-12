package no.kristiania.pg3402.catalog.repository;

import no.kristiania.pg3402.catalog.model.CardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardSetRepository extends JpaRepository<CardSet, Long> {

    Optional<CardSet> findBySetCode(String setCode);

    List<CardSet> findByGameType(CardSet.GameType gameType);

    List<CardSet> findByNameContainingIgnoreCase(String name);
}

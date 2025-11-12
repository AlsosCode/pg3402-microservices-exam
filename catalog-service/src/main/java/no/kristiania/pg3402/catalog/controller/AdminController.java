package no.kristiania.pg3402.catalog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.pg3402.catalog.service.PokemonTcgImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final PokemonTcgImportService pokemonTcgImportService;

    @PostMapping("/import/set/{setId}")
    public ResponseEntity<Map<String, String>> importSet(@PathVariable String setId) {
        log.info("Admin: Importing set {}", setId);
        try {
            String result = pokemonTcgImportService.importSet(setId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", result
            ));
        } catch (Exception e) {
            log.error("Failed to import set: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/import/recent")
    public ResponseEntity<Map<String, Object>> importRecentSets(
            @RequestParam(defaultValue = "5") int count) {
        log.info("Admin: Importing {} recent sets", count);
        try {
            List<String> results = pokemonTcgImportService.importRecentSets(count);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "imported", count,
                    "results", results
            ));
        } catch (Exception e) {
            log.error("Failed to import recent sets: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}

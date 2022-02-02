package com.sorbonne.book_search_engine.controller;

import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Sylvain in 2022/02.
 */
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/api")
@Validated
@Slf4j
public class RankingBookController {
    private final Map<Integer, Double> closenessCentrality;
    private final List<Pair<Integer, String>> top100BooksPreview;

    /**
     * GET ranking of books id by closeness centrality
     * @return ResponseEntity<Set<Integer>>
     */
    @GetMapping("/ranking")
    public ResponseEntity<Set<Integer>> closeness(){
        log.info("GET /ranking");
        return ResponseEntity.ok(closenessCentrality.keySet());
    }

    /**
     * GET top 100 books preview Map(id, title)
     * @return ResponseEntity<List<Pair<Integer, String>>>
     */
    @GetMapping("/ranking/top100books")
    public ResponseEntity<List<Pair<Integer, String>>> getTop100BooksPreview(){
        log.info("GET /ranking/top100books");
        return ResponseEntity.ok(top100BooksPreview);
    }
}

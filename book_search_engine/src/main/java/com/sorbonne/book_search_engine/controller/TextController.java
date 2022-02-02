package com.sorbonne.book_search_engine.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Sylvain in 2022/02.
 */
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/api")
@Validated
@Slf4j
public class TextController {

    @GetMapping("/text/{id}")
    public ResponseEntity<String> bookText(@PathVariable(required = true) int id){
        log.info("GET /text/" + id);
        try {
            String content = new String(Files.readAllBytes(Paths.get("books/" + id + ".txt")));
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

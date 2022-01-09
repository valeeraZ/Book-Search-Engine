package com.sorbonne.book_search_engine.controller;

import com.sorbonne.book_search_engine.entity.Book;
import com.sorbonne.book_search_engine.entity.Result;
import com.sorbonne.book_search_engine.service.SearchBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by Sylvain in 2022/01.
 */
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/api")
@Validated
@Slf4j
public class SearchBookController {
    private final SearchBookService searchBookService;

    @GetMapping("/books")
    public ResponseEntity<Result> books(@RequestParam(required = false,defaultValue = "0") int page){
        log.info("GET /books?page=" + page);
        return ResponseEntity.ok(searchBookService.getBooksOnPage(page));
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Book> bookById(@PathVariable(required = true) int id){
        log.info("GET /books/" + id);
        Book book = searchBookService.getBookById(id);
        if (book != null)
            return ResponseEntity.ok(book);
        else
            return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/books", params = "search")
    public ResponseEntity<List<Book>> booksByWord(@NotBlank @NotNull @RequestParam(name = "search", required = true) String content){
        log.info("GET /books?search=" + content);
        String[] words = content.split("\\s+");
        List<List<Book>> results = new ArrayList<>();
        for (String word: words) {
            results.add(searchBookService.getBooksByWord(word));
        }
        Optional<List<Book>> result = results.parallelStream()
                .filter(bookList -> bookList != null && bookList.size() != 0)
                .reduce((a, b) -> {
                    a.retainAll(b);
                    return a;
                });
        return ResponseEntity.ok(result.orElse(new ArrayList<>()));
    }
}

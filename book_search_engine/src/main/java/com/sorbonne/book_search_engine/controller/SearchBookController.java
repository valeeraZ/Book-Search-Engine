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
import javax.validation.constraints.NotEmpty;
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

    /**
     * GET books page by page
     * @param page the page number, 0 by default
     * @return ResponseEntity<Result> containing books and some meta information about page
     */
    @GetMapping("/books")
    public ResponseEntity<Result> books(@RequestParam(required = false,defaultValue = "0") int page){
        log.info("GET /books?page=" + page);
        return ResponseEntity.ok(searchBookService.getBooksOnPage(page));
    }

    /**
     * GET book by id
     * @param id the book's id
     * @return ResponseEntity<Book> a book if id found, else return a 404 error
     */
    @GetMapping("/books/{id}")
    public ResponseEntity<Book> bookById(@PathVariable(required = true) int id){
        log.info("GET /books/" + id);
        Book book = searchBookService.getBookById(id);
        if (book != null)
            return ResponseEntity.ok(book);
        else
            return ResponseEntity.notFound().build();
    }

    /**
     * GET books by searching keyword
     * @param content the keyword string
     * @param closeness boolean, ordered by closeness centrality or not, by default is not (ordered by relevance score to keyword)
     * @return ResponseEntity<List<Book>>
     */
    @GetMapping(value = "/books", params = "search")
    public ResponseEntity<List<Book>> booksByWord(@NotBlank @NotNull @RequestParam(name = "search", required = true) String content,
                                                  @RequestParam(name = "closeness", required = false, defaultValue = "false") boolean closeness){
        log.info("GET /books?search=" + content + "&closeness=" + closeness);
        String[] words = content.split("\\s+");
        List<List<Book>> results = new ArrayList<>();

        for (String word: words) {
            results.add(searchBookService.getBooksByWord(word));
        }
        List<Book> result = retainIntersection(results);
        if (closeness)
            result = searchBookService.orderBooksByCloseness(result);
        return ResponseEntity.ok(result);
    }

    private List<Book> retainIntersection(List<List<Book>> results) {
        Optional<List<Book>> resultKeywords = results.parallelStream()
                .filter(bookList -> bookList != null && bookList.size() != 0)
                .reduce((a, b) -> {
                    a.retainAll(b);
                    return a;
                });

        return resultKeywords.orElse(new ArrayList<>());
    }

    /**
     * GET books by searching keyword in its titles
     * @param content the keyword string, ordered by relevance score to keyword
     * @return ResponseEntity<List<Book>>
     */
    @GetMapping(value = "/books", params = "searchByTitle")
    public ResponseEntity<List<Book>> booksByTitle(@NotBlank @NotNull @RequestParam(name = "searchByTitle", required = true) String content){
        log.info("GET /books?searchByTitle=" + content);
        String[] words = content.split("\\s+");
        List<List<Book>> results = new ArrayList<>();
        for (String word: words) {
            results.add(searchBookService.getBooksByTitle(word));
        }
        List<Book> result = retainIntersection(results);
        result = searchBookService.orderBooksByCloseness(result);
        return ResponseEntity.ok(result);
    }

    /**
     * GET books by searching keyword in its authors
     * @param content the keyword string, ordered by relevance score to keyword
     * @return ResponseEntity<List<Book>>
     */
    @GetMapping(value = "/books", params = "searchByAuthor")
    public ResponseEntity<List<Book>> booksByAuthor(@NotBlank @NotNull @RequestParam(name = "searchByAuthor", required = true) String content){
        log.info("GET /books?searchByAuthor=" + content);
        String[] words = content.split("\\s+");
        List<List<Book>> results = new ArrayList<>();
        for (String word: words) {
            results.add(searchBookService.getBooksByAuthor(word));
        }
        List<Book> result = retainIntersection(results);
        result = searchBookService.orderBooksByCloseness(result);
        return ResponseEntity.ok(result);
    }

    /**
     * GET books by matching regex
     * @param content the regex string
     * @param closeness boolean, ordered by closeness centrality or not, by default is not (ordered by relevance score to keyword)
     * @return ResponseEntity<List<Book>>
     */
    @GetMapping(value = "/books", params = "regex")
    public ResponseEntity<List<Book>> booksByRegEx(@NotBlank @NotNull @RequestParam(name = "regex", required = true) String content,
                                                   @RequestParam(name = "closeness", required = false, defaultValue = "false") boolean closeness){
        log.info("GET /books?regex=" + content + "&closeness=" + closeness);
        List<Book> results = new ArrayList<>();

        results.addAll(searchBookService.getBooksByRegexInTitle(content));
        results.addAll(searchBookService.getBooksByRegexInAuthor(content));
        results.addAll(searchBookService.getBooksByRegex(content));

        HashSet<Book> uniqueBooks;
        List<Book> uniqueResult;
        if (closeness){
            uniqueBooks = new HashSet<>(results);
            uniqueResult = new ArrayList<>(uniqueBooks);
            uniqueResult = searchBookService.orderBooksByCloseness(uniqueResult);
        }else {
            uniqueBooks = new HashSet<>();
            uniqueResult = new ArrayList<>();
            for (Book book: results){
                if (uniqueBooks.add(book))
                    uniqueResult.add(book);
            }
        }

        return ResponseEntity.ok(uniqueResult);
    }

    /**
     * GET some similar books to the books representing by its id
     * @param suggestions ids of books to search their similar books as suggestions
     * @return ResponseEntity<List<Book>> a list of similar books
     */
    @GetMapping(value = "/books", params = "suggestions")
    public ResponseEntity<List<Book>> booksByJaccardDistance(@NotEmpty @RequestParam(name = "suggestions") Integer[] suggestions){
        log.info("GET /books?ids=" + Arrays.toString(suggestions));
        List<Integer> bookIds = Arrays.asList(suggestions);
        return ResponseEntity.ok(searchBookService.getNeighborBooksByJaccard(bookIds));
    }
}

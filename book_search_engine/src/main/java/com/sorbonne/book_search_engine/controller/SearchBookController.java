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
     * GET books by searching keyword or regex in its content
     * @param content the keyword or regex string
     * @param closeness boolean, ordered by closeness centrality or not, by default is not (ordered by relevance score to keyword)
     * @return ResponseEntity<List<Book>>
     */
    @GetMapping(value = "/books", params = "search")
    public ResponseEntity<List<Book>> booksByWord(@NotBlank @NotNull @RequestParam(name = "search", required = true) String content,
                                                  @RequestParam(name = "closeness", required = false, defaultValue = "false") boolean closeness){
        log.info("GET /books?search=" + content + "&closeness=" + closeness);

        // search books by keyword, that's to say `content` is not regarded as an regex
        String[] words = content.split("\\s+");
        List<List<Book>> resultsKeyword = new ArrayList<>();

        for (String word: words) {
            resultsKeyword.add(searchBookService.getBooksByWord(word));
        }
        List<Book> resultKeyword = retainIntersection(resultsKeyword);

        // search books by regex, that's to say `content` is regarded as an regex
        List<Book> resultRegex = new ArrayList<>(searchBookService.getBooksByRegex(content));

        // remove duplicates of resultKeyword & resultRegex and make union
        List<Book> finalUniqueResult = unionAndRemoveDuplicates(resultKeyword, resultRegex);

        if (closeness)
            finalUniqueResult = searchBookService.orderBooksByCloseness(finalUniqueResult);
        return ResponseEntity.ok(finalUniqueResult);
    }

    /**
     * GET books by searching keyword or regex in its titles
     * @param content the keyword or regex string
     * @param closeness boolean, ordered by closeness centrality or not, by default is not (ordered by relevance score to keyword)
     * @return ResponseEntity<List<Book>>
     */
    @GetMapping(value = "/books", params = "searchByTitle")
    public ResponseEntity<List<Book>> booksByTitle(@NotBlank @NotNull @RequestParam(name = "searchByTitle", required = true) String content,
                                                   @RequestParam(name = "closeness", required = false, defaultValue = "false") boolean closeness){
        log.info("GET /books?searchByTitle=" + content + "&closeness=" + closeness);
        // search books by regex, that's to say `content` is regarded as an regex
        String[] words = content.split("\\s+");
        List<List<Book>> resultsKeyword = new ArrayList<>();
        for (String word: words) {
            resultsKeyword.add(searchBookService.getBooksByTitle(word));
        }
        List<Book> resultKeyword = retainIntersection(resultsKeyword);
        // search books by regex, that's to say `content` is regarded as an regex
        List<Book> resultRegex = new ArrayList<>(searchBookService.getBooksByRegexInTitle(content));
        // remove duplicates of resultKeyword & resultRegex and make union
        List<Book> finalUniqueResult = unionAndRemoveDuplicates(resultKeyword, resultRegex);

        if (closeness)
            finalUniqueResult = searchBookService.orderBooksByCloseness(finalUniqueResult);
        return ResponseEntity.ok(finalUniqueResult);
    }

    /**
     * GET books by searching keyword or regex in its authors' names
     * @param content the keyword string or regex
     * @param closeness boolean, ordered by closeness centrality or not, by default is not (ordered by relevance score to keyword)
     * @return ResponseEntity<List<Book>>
     */
    @GetMapping(value = "/books", params = "searchByAuthor")
    public ResponseEntity<List<Book>> booksByAuthor(@NotBlank @NotNull @RequestParam(name = "searchByAuthor", required = true) String content,
                                                    @RequestParam(name = "closeness", required = false, defaultValue = "false") boolean closeness){
        log.info("GET /books?searchByAuthor=" + content + "&closeness=" + closeness);
        // search books by regex, that's to say `content` is regarded as an regex
        String[] words = content.split("\\s+");
        List<List<Book>> resultsKeyword = new ArrayList<>();
        for (String word: words) {
            resultsKeyword.add(searchBookService.getBooksByAuthor(word));
        }
        List<Book> resultKeyword = retainIntersection(resultsKeyword);
        // search books by regex, that's to say `content` is regarded as an regex
        List<Book> resultRegex = new ArrayList<>(searchBookService.getBooksByRegexInAuthor(content));
        // remove duplicates of resultKeyword & resultRegex and make union
        List<Book> finalUniqueResult = unionAndRemoveDuplicates(resultKeyword, resultRegex);

        if (closeness)
            finalUniqueResult = searchBookService.orderBooksByCloseness(finalUniqueResult);
        return ResponseEntity.ok(finalUniqueResult);
    }

    /**
     * GET a similar book to the book representing by it's id
     * @param bookId id of book to search it's similar book as suggestion
     * @return ResponseEntity<Book> a similar book
     */
    @GetMapping(value = "/books", params = "suggestion")
    public ResponseEntity<Book> booksByJaccardDistance(@NotEmpty @RequestParam(name = "suggestion") Integer bookId){
        log.info("GET /books?suggestion=" + bookId);
        return ResponseEntity.ok(searchBookService.getNeighborBookByJaccard(bookId));
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

    @SafeVarargs
    private final List<Book> unionAndRemoveDuplicates(List<Book>... lists){
        HashSet<Book> uniqueBooks = new HashSet<>();
        List<Book> uniqueResult = new ArrayList<>();
        for (List<Book> list: lists) {
            for (Book book: list) {
                if (uniqueBooks.add(book))
                    uniqueResult.add(book);
            }
        }
        return uniqueResult;
    }
}

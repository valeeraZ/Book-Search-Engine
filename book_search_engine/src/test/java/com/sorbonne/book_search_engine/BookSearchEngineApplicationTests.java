package com.sorbonne.book_search_engine;

import com.sorbonne.book_search_engine.algorithms.keyword.Keyword;
import com.sorbonne.book_search_engine.algorithms.keyword.KeywordsExtractor;
import com.sorbonne.book_search_engine.algorithms.keyword.StemmerLanguage;
import com.sorbonne.book_search_engine.config.IndexTableConfig;
import com.sorbonne.book_search_engine.entity.Book;
import com.sorbonne.book_search_engine.service.SearchBookService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class BookSearchEngineApplicationTests {
    private final SearchBookService searchBookService;
    private final Map<Integer, Double> closenessCentrality;

    @Test
    void contextLoads() {
    }

    @Test
    void testKeywords() throws IOException {
        String filename = "books/10.txt";
        FileReader reader = new FileReader(filename);
        StemmerLanguage language = StemmerLanguage.ENGLISH;
        KeywordsExtractor extractor = new KeywordsExtractor(language);
        List<Keyword> keywords = extractor.extract(reader);
        System.out.println(keywords);
        assertTrue(keywords.size() > 0);
    }

    @Test
    void testSearchOneWord(){
        List<Book> books = searchBookService.getBooksByWord("winterbourne");
        System.out.println("books = " + books);
        assertTrue(books.size() > 0);
    }

    @Test
    void testSplitWords() throws IOException {
        HashSet<String> words = IndexTableConfig.splitWords("The King James Version of the Bible", "en");
        System.out.println("words = " + words);
    }

    @Test
    void testRegExSearchKeyword(){
        List<Book> books = searchBookService.getBooksByRegex("winterb");
        System.out.println("books = " + books);
    }

    @Test
    void testClosenessCentralityOrder(){
        List<Book> page1Books = searchBookService.orderBooksByCloseness(searchBookService.getBooksOnPage(1).getResult());
        System.out.println("order books on page1 by closeness centrality = " + page1Books);
    }

    @Test
    void testClosenessCentralityValue(){
        List<Map.Entry<Integer, Double>> res = closenessCentrality.entrySet().stream()
                .limit(200)
                .collect(Collectors.toList());
        System.out.println("res = " + res);
    }

}

package com.sorbonne.book_search_engine.config;

import com.sorbonne.book_search_engine.entity.Book;
import com.sorbonne.book_search_engine.entity.GutendexData;
import com.sorbonne.book_search_engine.service.FetchBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Sylvain in 2022/01.
 */
@Component
@Slf4j
@EnableAsync
public class InitLibraryConfig {
    @Autowired
    private FetchBookService fetchBookService;

    /**
     * loading books from Gutenberg project, or from file
     * @param restTemplate a modified RestTemplate
     * @param httpHeaders HttpHeaders witt accept JSON
     * @return ArrayList of Book
     */
    @Bean
    public Map<Integer, Book> library(RestTemplate restTemplate, HttpEntity<String> httpHeaders) throws IOException, ClassNotFoundException {
        Map<Integer, Book> library = new ConcurrentHashMap<>();

        // if the books.ser file already exists, load the information of books into a map
        if (new File("books.ser").exists()){
            log.info("Loading books from file to memory...");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("books.ser"));
            library = (Map<Integer, Book>) inputStream.readObject();
            inputStream.close();
            return library;
        }

        // else, download the 1664 books information into a .ser file and download the text of each book into /books/<id>.txt
        log.info("First time use, Downloading 1664 books ...");
        ResponseEntity<GutendexData> result = restTemplate.exchange("http://gutendex.com/books?mime_type=text", HttpMethod.GET, httpHeaders, GutendexData.class);
        ArrayList<Book> books;
        while (library.size() < 1664){
            books = Objects.requireNonNull(result.getBody()).getResults();
            List<Future<Map.Entry<Integer, Book>>> futures = new ArrayList<>();
            for (Book book: books){
                futures.add(fetchBookService.getBook(book));
            }
            Iterator<Future<Map.Entry<Integer, Book>>> iterator = futures.iterator();
            while (iterator.hasNext()){
                Future<Map.Entry<Integer, Book>> future = iterator.next();
                if (future.isDone()){
                    try {
                        iterator.remove();
                        Map.Entry<Integer, Book> entry = future.get();
                        if (entry != null)
                            library.put(entry.getKey(), entry.getValue());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                if (!iterator.hasNext()) {
                    iterator = futures.iterator();
                }
            }
            log.info("progress: " + library.size());
            String nextURL = result.getBody().getNext();
            result = restTemplate.exchange(nextURL, HttpMethod.GET, httpHeaders, GutendexData.class);
        }
        System.out.println();

        log.info("Saving " + library.size() + " books from memory to local file...");
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("books.ser"));
        outputStream.writeObject(library);
        outputStream.flush();
        outputStream.close();
        return library;
    }

    /**
     * separate the 1680 books library to many lists, each list contains 12 books
     * @return the PagedListHolder for request books from page 0 - 139 (1680/12 = 140)
     */
    @Bean
    public PagedListHolder<Book> pagedLibrary(Map<Integer, Book> library){
        List<Book> list = new ArrayList<>(library.values());
        PagedListHolder<Book> pagedLibrary = new PagedListHolder<>(list);
        pagedLibrary.setPageSize(12);
        return pagedLibrary;
    }
}

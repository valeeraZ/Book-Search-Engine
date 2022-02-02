package com.sorbonne.book_search_engine.config;

import com.sorbonne.book_search_engine.entity.Book;
import com.sorbonne.book_search_engine.entity.GutendexData;
import com.sorbonne.book_search_engine.service.FetchBookService;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

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
        ResponseEntity<GutendexData> result = restTemplate.exchange("http://gutendex.com/books?mime_type=text&languages=en", HttpMethod.GET, httpHeaders, GutendexData.class);
        ArrayList<Book> books;
        while (library.size() < 1664){
            books = Objects.requireNonNull(result.getBody()).getResults();
            books = books.stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
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
     * separate the books library to many lists (pageable), each list contains 20 books
     * @return the PagedListHolder of books ordered by closeness centrality, each page containing 20 books
     */
    @Bean
    public PagedListHolder<Book> pagedLibrary(Map<Integer, Book> library, Map<Integer, Double> closenessCentrality){
        List<Book> books = new ArrayList<>();
        List<Integer> orderedIds = new ArrayList<>(closenessCentrality.keySet());
        for (Integer id: orderedIds) {
            books.add(library.get(id));
        }
        PagedListHolder<Book> pagedLibrary = new PagedListHolder<>(books);
        pagedLibrary.setPageSize(20);
        return pagedLibrary;
    }

    @Bean
    public List<Pair<Integer, String>> top100BooksPreview(Map<Integer, Book> library, Map<Integer, Double> closenessCentrality){
        List<Pair<Integer, String>> result = new ArrayList<>();
        Set<Integer> ids = closenessCentrality.keySet();
        int i = 0;
        for (Integer id: ids){
            result.add(new Pair<>(id, library.get(id).getTitle()));
            i++;
            if (i == 100) break;
        }
        return result;
    }
}

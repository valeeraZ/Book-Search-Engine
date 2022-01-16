package com.sorbonne.book_search_engine.service;

import com.sorbonne.book_search_engine.entity.Book;
import com.sorbonne.book_search_engine.entity.Format;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Created by Sylvain in 2022/01.
 */
@Service
@Slf4j
public class FetchBookService {
    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate restTemplate;

    /**
     * set image's and text's URL for a book object
     * download the text to files in /books/id.txt
     * @param book a book object to be completed init
     * @return Future<Entry<Id of Book, Book>> a Future Task of work on the book object
     */
    @Async("asyncTaskExecutor")
    public Future<Map.Entry<Integer, Book>> getBook(Book book){
        Format format = book.getFormats();
        String textURL = getTextURL(format);
        if (textURL == null)
            return new AsyncResult<>(null);
        if (format.getImage() != null){
            book.setImage(format.getImage().replace("small", "medium"));
        }
        book.setText(textURL);
        try {
            String text = restTemplate.getForObject(textURL, String.class);
            if (text == null)
                return new AsyncResult<>(null);
            if (text.split("\\s+").length > 10000){
                PrintWriter pw = new PrintWriter(new FileOutputStream("books/" + book.getId() + ".txt"));
                pw.println(text);
                pw.flush();
                pw.close();
                Map.Entry<Integer, Book> b = new AbstractMap.SimpleEntry<>(book.getId(), book);
                return new AsyncResult<>(b);
            }else {
                return new AsyncResult<>(null);
            }
        }catch (HttpClientErrorException | FileNotFoundException ignored){
        }
        return new AsyncResult<>(null);
    }

    /**
     * get at least one valid URL to download the text of a book
     * @param format the URLs given by "gutendex" api
     * @return a URL to download txt
     */
    private String getTextURL(Format format){
        if (format.getText1() != null){
            if (format.getText1().endsWith(".txt") || format.getText1().endsWith(".txt.utf-8"))
                return format.getText1();
        }
        if (format.getText2() != null) {
            if (format.getText2().endsWith(".txt") || format.getText2().endsWith(".txt.utf-8"))
                return format.getText2();
        }
        if (format.getText3() != null) {
            if (format.getText3().endsWith(".txt") || format.getText3().endsWith(".txt.utf-8"))
                return format.getText3();
        }
        return null;
    }
}

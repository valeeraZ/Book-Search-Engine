package com.sorbonne.book_search_engine;

import com.sorbonne.book_search_engine.entity.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.Map;

@SpringBootApplication
@Slf4j
public class BookSearchEngineApplication {
    @Autowired
    private Map<Integer, Book> library;

    public static void main(String[] args) {
        SpringApplication.run(BookSearchEngineApplication.class, args);
    }

    @Bean
    public CommandLineRunner run() throws Exception {
        return args -> {
            log.info("Finish loading library from file/Internet with " + library.size() + " books.");
        };
    }

}

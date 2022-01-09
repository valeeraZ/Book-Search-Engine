package com.sorbonne.book_search_engine.algorithms.keyword.config;

import com.sorbonne.book_search_engine.algorithms.keyword.Keyword;
import com.sorbonne.book_search_engine.algorithms.keyword.KeywordsExtractor;
import com.sorbonne.book_search_engine.algorithms.keyword.StemmerLanguage;
import com.sorbonne.book_search_engine.entity.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sylvain in 2022/01.
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KeywordConfig {

    @Bean
    public KeywordDictionary keywordDictionary(Map<Integer, Book> library) throws IOException, ClassNotFoundException {

        if (new File("keywords.ser").exists()){
            log.info("Loading index table of keywords from file to memory...");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("keywords.ser"));
            KeywordDictionary dictionary = (KeywordDictionary) inputStream.readObject();
            inputStream.close();
            return dictionary;
        }

        log.info("Charging index tables of keywords...");
        // a map of <word, stem>
        HashMap<String, String> word2Keyword = new HashMap<>();
        // a map of <Stem, map <Id_book, Relevancy_Keyword_Book>>
        HashMap<String, HashMap<Integer, Double>> keywordInBooks = new HashMap<>();
        FileReader reader;
        KeywordsExtractor extractor;
        StemmerLanguage languageEn = StemmerLanguage.ENGLISH;
        StemmerLanguage languageFr = StemmerLanguage.FRENCH;
        for (Book book: library.values()){
            String bookText = "books/" + book.getId() + ".txt";
            try {
                reader = new FileReader(bookText);
                if (book.getLanguages().contains("en")) {
                    extractor = new KeywordsExtractor(languageEn);
                }
                else if (book.getLanguages().contains("fr")){
                    extractor = new KeywordsExtractor(languageFr);
                }else {
                    continue;
                }
                List<Keyword> keywords = extractor.extract(reader);
                for (Keyword keyword: keywords) {
                    for (String word: keyword.getWords()){
                        word2Keyword.put(word, keyword.getStem());
                    }
                    String stem = keyword.getStem();
                    if (keywordInBooks.containsKey(stem)){
                        HashMap<Integer, Double> value = keywordInBooks.get(stem);
                        value.put(book.getId(), keyword.getRelevance());
                        keywordInBooks.put(stem, value);
                    }else {
                        HashMap<Integer, Double> value = new HashMap<>();
                        value.put(book.getId(), keyword.getRelevance());
                        keywordInBooks.put(stem, value);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        KeywordDictionary dictionary = new KeywordDictionary(word2Keyword, keywordInBooks);
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("keywords.ser"));
        outputStream.writeObject(dictionary);
        outputStream.flush();
        outputStream.close();
        return dictionary;
    }
}

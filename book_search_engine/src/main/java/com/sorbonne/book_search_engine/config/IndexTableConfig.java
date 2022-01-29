package com.sorbonne.book_search_engine.config;

import com.sorbonne.book_search_engine.algorithms.keyword.Keyword;
import com.sorbonne.book_search_engine.algorithms.keyword.KeywordsExtractor;
import com.sorbonne.book_search_engine.algorithms.keyword.StemmerLanguage;
import com.sorbonne.book_search_engine.algorithms.keyword.config.KeywordDictionary;
import com.sorbonne.book_search_engine.entity.Book;
import com.sorbonne.book_search_engine.entity.Person;
import javafx.util.Pair;
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
public class IndexTableConfig {

    /**
     * loading a keyword dictionary from file or initializing a new instance by some calculation
     * @param library the (Integer, Book) library
     * @return a Keyword Dictionary
     */
    @Bean
    public KeywordDictionary keywordDictionary(Map<Integer, Book> library) throws IOException, ClassNotFoundException {

        if (new File("keywordsDictionary.ser").exists()){
            log.info("Loading dictionary of keywords from file to memory...");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("keywordsDictionary.ser"));
            KeywordDictionary dictionary = (KeywordDictionary) inputStream.readObject();
            inputStream.close();
            return dictionary;
        }

        log.info("Charging dictionary of keywords...");

        // a map of <word, stem>
        HashMap<String, String> word2Keyword = new HashMap<>();
        // a map of <Stem, map <Id_book, Relevancy_Keyword_Book>>
        HashMap<String, HashMap<Integer, Double>> keywordInBooks = new HashMap<>();
        // a map of <Id_book, map<Stem, Relevance>>
        HashMap<Integer, HashMap<String, Double>> keywordBookTable = new HashMap<>();

        FileReader reader;

        StemmerLanguage languageEn = StemmerLanguage.ENGLISH;
        StemmerLanguage languageFr = StemmerLanguage.FRENCH;
        KeywordsExtractor extractorEn = new KeywordsExtractor(languageEn);
        KeywordsExtractor extractorFr = new KeywordsExtractor(languageFr);
        List<Keyword> keywords;
        for (Book book: library.values()){
            int bookId = book.getId();
            String bookText = "books/" + bookId + ".txt";
            try {
                reader = new FileReader(bookText);
                if (book.getLanguages().contains("en")) {
                    keywords = extractorEn.extract(reader);
                }
                else if (book.getLanguages().contains("fr")){
                    keywords = extractorFr.extract(reader);
                }else {
                    continue;
                }
                for (Keyword keyword: keywords){
                    String stem = keyword.getStem();
                    Set<String> words = keyword.getWords();
                    double relevance = keyword.getRelevance();

                    // word2Keyword
                    for (String word: words){
                        word2Keyword.put(word, stem);
                    }

                    // keywordInBooks
                    if (keywordInBooks.containsKey(stem)){
                        HashMap<Integer, Double> keywordRelevance = keywordInBooks.get(stem);
                        keywordRelevance.put(bookId, relevance);
                        keywordInBooks.put(stem, keywordRelevance);
                    }else {
                        HashMap<Integer, Double> keywordRelevance = new HashMap<>();
                        keywordRelevance.put(bookId, relevance);
                        keywordInBooks.put(stem, keywordRelevance);
                    }

                    // keywordBookTable, in fact, a reverse version of keywordInBooks
                    if (keywordBookTable.containsKey(bookId)){
                        HashMap<String, Double> stemRelevanceMap = keywordBookTable.get(bookId);
                        stemRelevanceMap.put(stem, relevance);
                        keywordBookTable.put(bookId, stemRelevanceMap);
                    }else {
                        HashMap<String, Double> stemRelevanceMap = new HashMap<>();
                        stemRelevanceMap.put(stem, relevance);
                        keywordBookTable.put(bookId, stemRelevanceMap);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        KeywordDictionary dictionary = new KeywordDictionary(word2Keyword, keywordInBooks, keywordBookTable);
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("keywordsDictionary.ser"));
        outputStream.writeObject(dictionary);
        outputStream.flush();
        outputStream.close();
        return dictionary;
    }

    /**
     * loading a title dictionary from file or initializing a new instance by some calculation
     * @param library the (Integer, Book) library
     * @return HashMap<String, HashSet<Integer>>: key for keyword, value for ids of books containing the keyword
     */
    @Bean
    public HashMap<String, HashSet<Integer>> titleDictionary(Map<Integer, Book> library) throws IOException, ClassNotFoundException {
        if (new File("titles.ser").exists()){
            log.info("Loading index table of titles from file to memory...");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("titles.ser"));
            HashMap<String, HashSet<Integer>> titleDictionary = (HashMap<String, HashSet<Integer>>) inputStream.readObject();
            inputStream.close();
            return titleDictionary;
        }

        log.info("Charging index tables of titles...");
        HashMap<String, HashSet<Integer>> titleDictionary = new HashMap<>();
        for (Book book: library.values()){
            String title = book.getTitle();
            String languageCode;
            if(book.getLanguages().contains("en")){
                languageCode = "en";
            }else if (book.getLanguages().contains("fr")){
                languageCode = "fr";
            }else {
                continue;
            }
            HashSet<String> words = splitWords(title, languageCode);
            for (String word: words) {
                if (titleDictionary.containsKey(word)){
                    HashSet<Integer> ids = titleDictionary.get(word);
                    ids.add(book.getId());
                    titleDictionary.put(word, ids);
                }else {
                    HashSet<Integer> ids = new HashSet<>();
                    ids.add(book.getId());
                    titleDictionary.put(word, ids);
                }
            }
        }
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("titles.ser"));
        outputStream.writeObject(titleDictionary);
        outputStream.flush();
        outputStream.close();
        return titleDictionary;
    }

    /**
     * loading an author dictionary from file or initializing a new instance by some calculation
     * @param library the (Integer, Book) library
     * @return HashMap<String, HashSet<Integer>>: key for keyword, value for ids of books' authors containing the keyword
     */
    @Bean
    public HashMap<String, HashSet<Integer>> authorDictionary(Map<Integer, Book> library) throws IOException, ClassNotFoundException{
        if (new File("authors.ser").exists()){
            log.info("Loading index table of authors from file to memory...");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("authors.ser"));
            HashMap<String, HashSet<Integer>> titleDictionary = (HashMap<String, HashSet<Integer>>) inputStream.readObject();
            inputStream.close();
            return titleDictionary;
        }

        log.info("Charging index tables of authors...");
        HashMap<String, HashSet<Integer>> authorDictionary = new HashMap<>();
        for (Book book: library.values()){
            ArrayList<Person> authors = book.getAuthors();
            String languageCode;
            if(book.getLanguages().contains("en")){
                languageCode = "en";
            }else if (book.getLanguages().contains("fr")){
                languageCode = "fr";
            }else {
                continue;
            }
            HashSet<String> words = new HashSet<>();
            for (Person author: authors) {
                words.addAll(splitWords(author.getName(), languageCode));
            }
            for (String word: words) {
                if (authorDictionary.containsKey(word)){
                    HashSet<Integer> ids = authorDictionary.get(word);
                    ids.add(book.getId());
                    authorDictionary.put(word, ids);
                }else {
                    HashSet<Integer> ids = new HashSet<>();
                    ids.add(book.getId());
                    authorDictionary.put(word, ids);
                }
            }
        }
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("authors.ser"));
        outputStream.writeObject(authorDictionary);
        outputStream.flush();
        outputStream.close();
        return authorDictionary;
    }

    /**
     * Extract a list of words for a short phrase (a title, a name, etc)
     * @param text the contenu in String
     * @return list of all keywords
     * @throws IOException when error while reading from stream occurs
     */
    public static HashSet<String> splitWords(String text, String languageCode) throws IOException {
        Set<Character> alphabet = getAlphabet(languageCode);
        HashSet<String> words = new HashSet<>();
        StringBuilder currentWord = new StringBuilder();
        for (int i = 0; i < text.length(); i++) { //browsing the text, char by char
            char c = Character.toLowerCase(text.charAt(i));
            if (alphabet.contains(c)) { //if current char is in the alphabet (which is not a space, a point, etc)
                currentWord.append(c); //it is the next char of the current word
            } else {                   //else we have a word!
                String word = currentWord.toString();
                currentWord = new StringBuilder();
                if (!word.isEmpty() ) { //if the word is not empty
                    words.add(word);
                }
            }
        }
        String word = currentWord.toString();
        if (!word.isEmpty() ) { //if the word is not empty
            words.add(word);
        }
        return words;
    }

    public static Set<Character> getAlphabet(String languageCode){
        Set<Character> alphabet = new HashSet<>();
        String filename = "language/" + languageCode + "/alphabet.txt";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String line = reader.readLine();
            while (line != null) {
                alphabet.add(line.charAt(0));
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return alphabet;
    }
}

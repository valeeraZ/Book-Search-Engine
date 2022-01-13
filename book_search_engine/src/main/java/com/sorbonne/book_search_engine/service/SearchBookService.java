package com.sorbonne.book_search_engine.service;

import com.sorbonne.book_search_engine.algorithms.keyword.Keyword;
import com.sorbonne.book_search_engine.algorithms.keyword.config.KeywordDictionary;
import com.sorbonne.book_search_engine.algorithms.regex.DFA;
import com.sorbonne.book_search_engine.algorithms.regex.DFAState;
import com.sorbonne.book_search_engine.algorithms.regex.NFA;
import com.sorbonne.book_search_engine.algorithms.regex.RegExTree;
import com.sorbonne.book_search_engine.entity.Book;
import com.sorbonne.book_search_engine.entity.Result;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.sorbonne.book_search_engine.algorithms.regex.RegEx.parse;

/**
 * Created by Sylvain in 2022/01.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SearchBookService {
    private final Map<Integer, Book> library;
    private final PagedListHolder<Book> pagedLibrary;
    private final KeywordDictionary keywordDictionary;
    private final HashMap<String, HashSet<Integer>> titleDictionary;
    private final HashMap<String, HashSet<Integer>> authorDictionary;
    private final HashMap<Integer, HashMap<Integer, Double>> jaccardDistanceMap;
    private final Map<Integer, Double> closenessCentrality;

    public Result getBooksOnPage(int page){
        pagedLibrary.setPage(page);
        Result result = new Result();
        result.setTotalCount(pagedLibrary.getNrOfElements());
        result.setPageCount(pagedLibrary.getPageCount());
        result.setPerPage(pagedLibrary.getPageSize());
        result.setCurrentPage(page);
        result.setResult(pagedLibrary.getPageList());
        return result;
    }

    public Book getBookById(int id){
        try {
            return library.get(id);
        }catch (NullPointerException e){
            return null;
        }
    }

    public List<Book> getBooksByWord(String word){
        String stem = keywordDictionary.getWord2Keyword().get(word.toLowerCase(Locale.ROOT));
        if (stem == null)
            return new ArrayList<>();
        HashMap<Integer, Double> result = keywordDictionary.getKeywordInBooks().get(stem);
        // sort the result by relevancy
        result = result.entrySet().stream()
                .sorted((e1, e2) -> - (e1.getValue().compareTo(e2.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        List<Book> list = new ArrayList<>();
        for (Integer id: result.keySet()) {
            Book book = getBookById(id);
            list.add(book);
        }
        return list;

    }

    public List<Book> getBooksByTitle(String word){
        HashSet<Integer> result = titleDictionary.getOrDefault(word.toLowerCase(Locale.ROOT), new HashSet<>());
        if (result.isEmpty())
            return new ArrayList<>();
        List<Book> list = new ArrayList<>();
        for (Integer id: result) {
            Book book = getBookById(id);
            list.add(book);
        }
        return list;
    }

    public List<Book> getBooksByAuthor(String word){
        HashSet<Integer> result = authorDictionary.getOrDefault(word.toLowerCase(Locale.ROOT), new HashSet<>());
        if (result.isEmpty())
            return new ArrayList<>();
        List<Book> list = new ArrayList<>();
        for (Integer id: result) {
            Book book = getBookById(id);
            list.add(book);
        }
        return list;
    }


    public List<Book> getBooksByRegex(String regEx){
        HashMap<String, String> word2Keywords = keywordDictionary.getWord2Keyword();
        HashSet<String> candidats = new HashSet<>(word2Keywords.keySet());
        HashSet<String> words = getWordsByRegEx(candidats, regEx);
        List<List<Book>> listBooks = new ArrayList<>();
        for (String word: words){
            listBooks.add(getBooksByWord(word));
        }
        HashSet<Book> uniqueBooks = new HashSet<>();
        List<Book> result = new ArrayList<>();
        for (List<Book> books: listBooks) {
            for (Book book: books) {
                if (uniqueBooks.add(book))
                    result.add(book);
            }
        }
        return result;
    }

    public List<Book> getBooksByRegexInTitle(String regEx){
        HashSet<String> candidats = new HashSet<>(titleDictionary.keySet());
        HashSet<String> words = getWordsByRegEx(candidats, regEx);
        List<List<Book>> listBooks = new ArrayList<>();
        for (String word: words){
            listBooks.add(getBooksByTitle(word));
        }
        HashSet<Book> uniqueBooks = new HashSet<>();
        List<Book> result = new ArrayList<>();
        for (List<Book> books: listBooks) {
            for (Book book: books) {
                if (uniqueBooks.add(book))
                    result.add(book);
            }
        }
        return result;
    }

    public List<Book> getBooksByRegexInAuthor(String regEx){
        HashSet<String> candidats = new HashSet<>(authorDictionary.keySet());
        HashSet<String> words = getWordsByRegEx(candidats, regEx);
        List<List<Book>> listBooks = new ArrayList<>();
        for (String word: words){
            listBooks.add(getBooksByAuthor(word));
        }
        HashSet<Book> uniqueBooks = new HashSet<>();
        List<Book> result = new ArrayList<>();
        for (List<Book> books: listBooks) {
            for (Book book: books) {
                if (uniqueBooks.add(book))
                    result.add(book);
            }
        }
        return result;
    }

    public double jaccardDistance2Books(int id1, int id2){
        return jaccardDistanceMap.get(id1).get(id2);
    }

    public List<Book> orderBooksByCloseness(List<Book> books){
        List<Integer> orderedIds = new ArrayList<>(closenessCentrality.keySet());
        books.sort(Comparator.comparing(book -> orderedIds.indexOf(book.getId())));
        return books;
    }

    private HashSet<String> getWordsByRegEx(HashSet<String> words, String regEx){
        RegExTree ret;
        DFAState root;
        Set<DFAState> acceptings;
        if (regEx.length() < 1) {
            System.err.println("  >> ERROR: empty regEx.");
            return new HashSet<>();
        } else {
            try {
                ret = parse(regEx);
            } catch (Exception e) {
                log.info("Error parsing RegEx: " + regEx);
                e.printStackTrace();
                return new HashSet<>();
            }
        }

        NFA nfa = NFA.fromRegExTreeToNFA(ret);
        DFA dfa = DFA.fromNFAtoDFA(nfa);
        root = dfa.getRoot();
        acceptings = dfa.getAcceptings();

        HashSet<String> result = new HashSet<>();
        for (String word: words){
            if (search(root, root, acceptings, word, 0)){
                result.add(word);
            }
        }
        return result;
    }

    private boolean search(DFAState root, DFAState state, Set<DFAState> acceptings, String line, int position) {
        if (acceptings.contains(state))
            return true;

        if (position >= line.length())
            return false;

        int input = line.charAt(position);

        DFAState next = state.getTransition(input);

        if (next == null)
            return search(root, root, acceptings, line, position + 1);

        if (!search(root, next, acceptings, line, position + 1))
            return search(root, root, acceptings, line, position + 1);

        return true;
    }


}

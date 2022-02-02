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
    //private final HashMap<Integer, HashMap<Integer, Double>> jaccardDistanceMap;
    private final HashMap<Integer, Integer> jaccardMapNeighbor;
    private final Map<Integer, Double> closenessCentrality;

    /**
     * return books on a specific page of pagedLibrary
     * @param page the page number
     * @return a list of books with some meta information in a Result Object
     */
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

    /**
     * get a book by its id from library
     * @param id the id of book
     * @return a book object or null if the id not exists
     */
    public Book getBookById(int id){
        try {
            return library.get(id);
        }catch (NullPointerException e){
            return null;
        }
    }

    /**
     * search books containing words having the stem of keyword given in parameter
     * @param word the keyword to search in books
     * @return a list of books
     */
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
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());

    }

    /**
     * search books with titles containing words having the stem of keyword given in parameter
     * @param word the keyword to search in books' titles
     * @return a list of books
     */
    public List<Book> getBooksByTitle(String word){
        HashSet<Integer> result = titleDictionary.getOrDefault(word.toLowerCase(Locale.ROOT), new HashSet<>());
        if (result.isEmpty())
            return new ArrayList<>();
        List<Book> list = new ArrayList<>();
        for (Integer id: result) {
            Book book = getBookById(id);
            list.add(book);
        }
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * search books with authors containing words having the stem of keyword given in parameter
     * @param word the keyword to search in books' authors
     * @return a list of books
     */
    public List<Book> getBooksByAuthor(String word){
        HashSet<Integer> result = authorDictionary.getOrDefault(word.toLowerCase(Locale.ROOT), new HashSet<>());
        if (result.isEmpty())
            return new ArrayList<>();
        List<Book> list = new ArrayList<>();
        for (Integer id: result) {
            Book book = getBookById(id);
            list.add(book);
        }
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * search books containing text matching the regex given in parameter
     * @param regEx the regex to match in books' contenu
     * @return a list of books
     */
    public List<Book> getBooksByRegex(String regEx){
        HashMap<String, String> word2Keywords = keywordDictionary.getWord2Keyword();
        HashSet<String> candidats = new HashSet<>(word2Keywords.keySet());
        HashSet<String> words = getWordsByRegEx(candidats, regEx.toLowerCase(Locale.ROOT));
        List<List<Book>> listBooks = new ArrayList<>();
        for (String word: words){
            listBooks.add(getBooksByWord(word));
        }
        List<Book> result = unionAndRemoveDuplicates(listBooks);
        return result.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * search books with title containing text matching the regex given in parameter
     * @param regEx the regex to match in books' title
     * @return a list of books
     */
    public List<Book> getBooksByRegexInTitle(String regEx){
        HashSet<String> candidats = new HashSet<>(titleDictionary.keySet());
        HashSet<String> words = getWordsByRegEx(candidats, regEx.toLowerCase(Locale.ROOT));
        List<List<Book>> listBooks = new ArrayList<>();
        for (String word: words){
            listBooks.add(getBooksByTitle(word));
        }
        List<Book> result = unionAndRemoveDuplicates(listBooks);
        return result.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * search books with authors containing text matching the regex given in parameter
     * @param regEx the regex to match in books' authors
     * @return a list of books
     */
    public List<Book> getBooksByRegexInAuthor(String regEx){
        HashSet<String> candidats = new HashSet<>(authorDictionary.keySet());
        HashSet<String> words = getWordsByRegEx(candidats, regEx.toLowerCase(Locale.ROOT));
        List<List<Book>> listBooks = new ArrayList<>();
        for (String word: words){
            listBooks.add(getBooksByAuthor(word));
        }
        List<Book> result = unionAndRemoveDuplicates(listBooks);
        return result.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * order a list of books by closeness centrality in descended order
     * @param books the list of books to be ordered
     * @return a list of books
     */
    public List<Book> orderBooksByCloseness(List<Book> books){
        List<Integer> orderedIds = new ArrayList<>(closenessCentrality.keySet());
        books.sort(Comparator.comparing(book -> orderedIds.indexOf(book.getId())));
        return books;
    }

    /**
     * //get some similar books of books given in parameter
     * //@param ids the books' id to search for some other similar books
     * //@return a list books
     */
    /*
    public List<Book> getNeighborBooksByJaccard(List<Integer> ids){
        HashSet<Integer> neighborIds = new HashSet<>();
        for (Integer id: ids){
            HashMap<Integer, Double> distances = jaccardDistanceMap.get(id);
            distances.remove(id);
            Integer id2 = Collections.min(distances.entrySet(), Map.Entry.comparingByValue()).getKey();
            neighborIds.add(id2);
        }
        List<Book> result = new ArrayList<>();
        neighborIds.forEach(id -> result.add(getBookById(id)));
        return result.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }*/

    public Book getNeighborBookByJaccard(int id){
        int id2 = jaccardMapNeighbor.get(id);
        return getBookById(id2);
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

    private List<Book> unionAndRemoveDuplicates(List<List<Book>> lists){
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

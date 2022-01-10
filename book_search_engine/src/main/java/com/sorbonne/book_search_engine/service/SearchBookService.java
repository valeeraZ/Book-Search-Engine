package com.sorbonne.book_search_engine.service;

import com.sorbonne.book_search_engine.algorithms.keyword.Keyword;
import com.sorbonne.book_search_engine.algorithms.keyword.config.KeywordDictionary;
import com.sorbonne.book_search_engine.entity.Book;
import com.sorbonne.book_search_engine.entity.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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


}

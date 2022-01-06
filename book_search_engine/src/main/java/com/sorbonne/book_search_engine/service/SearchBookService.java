package com.sorbonne.book_search_engine.service;

import com.sorbonne.book_search_engine.entity.Book;
import com.sorbonne.book_search_engine.entity.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by Sylvain in 2022/01.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SearchBookService {
    private final Map<Integer, Book> library;
    private final PagedListHolder<Book> pagedLibrary;

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



}

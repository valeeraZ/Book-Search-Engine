package com.sorbonne.book_search_engine.entity;

import lombok.Data;
import java.util.List;

/**
 * Created by Sylvain in 2022/01.
 */
@Data
public class Result {
    private int totalCount;
    private int pageCount;
    private int currentPage;
    private int perPage;
    private List<Book> result;
}

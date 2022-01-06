package com.sorbonne.book_search_engine.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
/**
 * Created by Sylvain in 2021/12.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GutendexData {
    private int count;
    private String next;
    private String previous;
    private ArrayList<Book> results;
}

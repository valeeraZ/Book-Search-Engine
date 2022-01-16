package com.sorbonne.book_search_engine.algorithms.keyword.config;

import com.sorbonne.book_search_engine.algorithms.keyword.Keyword;
import javafx.util.Pair;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Sylvain in 2022/01.
 */
@Data
public class KeywordDictionary implements Serializable {
    private final HashMap<String, String> word2Keyword;
    private final HashMap<String, HashMap<Integer, Double>> keywordInBooks;
    private final HashMap<Integer, HashMap<String, Double>> keywordBookTable;
}

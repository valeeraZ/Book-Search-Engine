package com.sorbonne.book_search_engine.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Sylvain in 2021/12.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book implements Serializable {
    private int id;
    private String title;
    private ArrayList<Person> authors;
    private ArrayList<Person> translators;
    private ArrayList<String> subjects;
    private ArrayList<String> bookshelves;
    private ArrayList<String> languages;
    private Format formats;
    // URL of txt
    private String text;
    // URL of image
    private String image;
}

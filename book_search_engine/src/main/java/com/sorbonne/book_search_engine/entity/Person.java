package com.sorbonne.book_search_engine.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Sylvain in 2021/12.
 */
@Data
public class Person implements Serializable {
    private String name;
    private int birth_year;
    private int death_year;
}

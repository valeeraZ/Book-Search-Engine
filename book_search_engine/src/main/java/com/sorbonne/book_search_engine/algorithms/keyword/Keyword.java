package com.sorbonne.book_search_engine.algorithms.keyword;

import com.google.common.base.Objects;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Sylvain in 2022/01.
 */
@Data
public class Keyword implements Comparable<Keyword>, Serializable {
    private String stem;

    private Double relevance;

    private Set<String> words;

    @Override
    public int compareTo(Keyword o) {
        return -(relevance.compareTo(o.getRelevance()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Keyword keyword = (Keyword) o;
        return Objects.equal(stem, keyword.stem);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stem);
    }

    @Override
    public String toString() {
        return "Stem: '" + stem + '\'' +
                ", Relevance: " + relevance +
                ", Words: " + words.toString() +
                "\n";
    }
}

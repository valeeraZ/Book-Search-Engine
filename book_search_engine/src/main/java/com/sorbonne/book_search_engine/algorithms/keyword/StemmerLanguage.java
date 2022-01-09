package com.sorbonne.book_search_engine.algorithms.keyword;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.frenchStemmer;

import java.util.function.Supplier;

/**
 * Created by Sylvain in 2022/01.
 */
public enum StemmerLanguage {
    FRENCH("fr", frenchStemmer::new),
    ENGLISH("en", englishStemmer::new);

    private final String code;
    private final Supplier<SnowballStemmer> stemmerSupplier;

    StemmerLanguage(String code, Supplier<SnowballStemmer> stemmerSupplier) {
        this.code = code;
        this.stemmerSupplier = stemmerSupplier;
    }

    public String getCode() {
        return code;
    }

    public SnowballStemmer getStemmer() {
        return stemmerSupplier.get();
    }
}

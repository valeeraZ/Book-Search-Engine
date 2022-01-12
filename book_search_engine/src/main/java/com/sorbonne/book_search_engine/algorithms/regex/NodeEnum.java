package com.sorbonne.book_search_engine.algorithms.regex;

import java.util.ArrayList;
import java.util.Arrays;

public class NodeEnum {
    // Operands
    static final Integer CONCAT = 0xC04CA7;
    static final Integer ETOILE = 0xE7011E;
    static final Integer ALTERN = 0xA17E54;
    static final Integer PROTECTION = 0xBADDAD;
    static final Integer PARENTHESEOUVRANT = 0x16641664;
    static final Integer PARENTHESEFERMANT = 0x51515151;
    static final Integer DOT = 0xD07;

    // Status
    static final Integer END = 0xC04CA0;
    static final Integer ING = 0xC04CA1;
    static final Integer START = 0xC04CA2;

    // Special Symbols
    static final Integer EPSILON = 0xE70113;

    public static boolean isOperand(Integer x){
        return new ArrayList<>(Arrays.asList(
                0xC04CA7,
                0xE7011E,
                0xA17E54,
                0xBADDAD,
                0x16641664,
                0x51515151,
                0xD07
        )).contains(x);
    }
}

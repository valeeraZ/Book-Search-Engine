package com.sorbonne.book_search_engine.algorithms.keyword;

import org.tartarus.snowball.SnowballStemmer;

import java.io.*;
import java.util.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;

/**
 * Created by Sylvain in 2022/01.
 */
public class KeywordsExtractor {
    private final SnowballStemmer stemmer;
    private final Set<Character> alphabet = new HashSet<>();
    private final Set<String> stopWords = new HashSet<>();
    private final HashMultiset<String> stemmedWords = HashMultiset.create();
    private final Multimap<String, String> wordsByStem = ArrayListMultimap.create();
    private final StringBuilder currentWord = new StringBuilder();

    public KeywordsExtractor(StemmerLanguage stemmerLanguage){
        this.stemmer = stemmerLanguage.getStemmer();
        this.readAlphabet(stemmerLanguage);
        this.readStopWords(stemmerLanguage);
    }

    /**
     * read alphabet from file by language
     */
    private void readAlphabet(StemmerLanguage stemmerLanguage){
        String filename = "language/" + stemmerLanguage.getCode() + "/alphabet.txt";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String line = reader.readLine();
            while (line != null) {
                alphabet.add(line.charAt(0));
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * read stop words from file by language
     */
    private void readStopWords(StemmerLanguage stemmerLanguage){
        String filename = "language/" + stemmerLanguage.getCode() + "/stopwords.txt";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String line = reader.readLine();
            while (line != null) {
                stopWords.add(line.trim());
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extract a list of keywords for a book
     * @param text the book in format of text
     * @return list of main keywords
     * @throws IOException when error while reading from stream occurs
     */
    public List<Keyword> extract(FileReader text) throws IOException {
        BufferedReader reader = new BufferedReader(text);

        stemmedWords.clear();
        wordsByStem.clear();
        currentWord.setLength(0);

        int ci = reader.read();
        boolean isNewSentence = true;
        boolean stemCurrentWord = true;
        String word;
        while (ci != -1) { //browsing the text, char by char
            char c = (char) Character.toLowerCase(ci);
            if (alphabet.contains(c)) { //if current char is in the alphabet (which is a space)
                if (!isNewSentence && Character.isUpperCase(ci)) {
                    stemCurrentWord = false;
                }
                currentWord.append(c); //it is the next char of the current word
                isNewSentence = false;
            } else {                   //else we have a word!
                word = currentWord.toString();
                currentWord.setLength(0);
                if (!word.isEmpty() && !stopWords.contains(word)) { //if the word is not a stop word
                    String stemmedWord = word;
                    if (stemCurrentWord) {
                        stemmer.setCurrent(word);
                        stemmer.stem();
                        stemmedWord = stemmer.getCurrent();
                    }
                    wordsByStem.put(stemmedWord, word);
                    stemmedWords.add(stemmedWord);
                }

                if (c == '.') {
                    isNewSentence = true;
                }
                stemCurrentWord = true;
            }
            ci = reader.read();
        }
        //counting words and computing relevance
        int totalWordsCount = stemmedWords.size();
        List<Keyword> result = new ArrayList<>();
        for (String stem : stemmedWords.elementSet()) {
            Keyword keyword = new Keyword();
            keyword.setStem(stem);
            keyword.setRelevance(stemmedWords.count(stem) / (double) totalWordsCount);
            keyword.setWords(new HashSet<>(wordsByStem.get(stem)));
            result.add(keyword);
        }
        Collections.sort(result);
        double relevanceSum = 0;
        int i = 0;
        //keeping only the 0.5 specified percentile of keywords
        while (relevanceSum < 0.5) {
            relevanceSum += result.get(i).getRelevance();
            i++;
        }
        return result.subList(0, Math.max(1, i - 1));
    }
}

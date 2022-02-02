package com.sorbonne.book_search_engine.config;

import com.sorbonne.book_search_engine.algorithms.keyword.Keyword;
import com.sorbonne.book_search_engine.algorithms.keyword.config.KeywordDictionary;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sylvain in 2022/01.
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GraphRankingConfig {
    private final KeywordDictionary keywordDictionary;
    static HashMap<Integer, HashMap<Integer, Double>> jaccardDistanceMap;

    @Bean
    public HashMap<Integer, Integer> jaccardMapNeighbor() throws IOException, ClassNotFoundException {
        if (new File("jaccardNeighbor.ser").exists()){
            log.info("Loading Jaccard Map Neighbors from file to memory...");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("jaccardNeighbor.ser"));
            HashMap<Integer, Integer> jaccardMapNeighbor = (HashMap<Integer, Integer>) inputStream.readObject();
            inputStream.close();
            return jaccardMapNeighbor;
        }

        log.info("Charging Jaccard Map Neighbors...");
        HashMap<Integer, Integer> jaccardMapNeighbor = new HashMap<>();
        if (Objects.isNull(jaccardDistanceMap))
            jaccardDistanceMap = calculateJaccardDistanceMap();
        for (Map.Entry<Integer, HashMap<Integer, Double>> jaccardDistance: jaccardDistanceMap.entrySet()){
            int id = jaccardDistance.getKey();
            HashMap<Integer, Double> distances = jaccardDistance.getValue();
            distances.remove(id);
            int idNeighbor = Collections.min(distances.entrySet(), Map.Entry.comparingByValue()).getKey();
            jaccardMapNeighbor.put(id, idNeighbor);
        }

        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("jaccardNeighbor.ser"));
        outputStream.writeObject(jaccardMapNeighbor);
        outputStream.flush();
        outputStream.close();

        return jaccardMapNeighbor;
    }

    /**
     * create jaccard distance matrix
     * {Book_id_1, map{Book_id2, distance_book1_book2}}
     * @return the jaccard distance matrix map
     */
    private HashMap<Integer, HashMap<Integer, Double>> calculateJaccardDistanceMap() throws IOException, ClassNotFoundException {

        if (new File("jaccard.ser").exists()){
            log.info("Loading Jaccard Distance Matrix from file to memory...");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("jaccard.ser"));
            HashMap<Integer, HashMap<Integer, Double>> jaccardDistanceMap = (HashMap<Integer, HashMap<Integer, Double>>) inputStream.readObject();
            inputStream.close();
            return jaccardDistanceMap;
        }

        log.info("Charging Jaccard Distance Matrix...");

        HashMap<Integer, HashMap<Integer, Double>> jaccardDistanceMap = new HashMap<>();

        HashMap<Integer, HashMap<String, Double>> keywordBookTable = keywordDictionary.getKeywordBookTable();

        for (int id1: keywordBookTable.keySet()){
            for (int id2: keywordBookTable.keySet()){
                HashMap<String, Double> table1 = keywordBookTable.get(id1);
                HashMap<String, Double> table2 = keywordBookTable.get(id2);
                double distance = jaccardDistanceBetweenTable(table1, table2);
                if (jaccardDistanceMap.containsKey(id1)){
                    HashMap<Integer, Double> distanceId1 = jaccardDistanceMap.get(id1);
                    distanceId1.put(id2, distance);
                    jaccardDistanceMap.put(id1, distanceId1);
                }else {
                    HashMap<Integer, Double> distanceId1 = new HashMap<>();
                    distanceId1.put(id2, distance);
                    jaccardDistanceMap.put(id1, distanceId1);
                }
            }
        }

        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("jaccard.ser"));
        outputStream.writeObject(jaccardDistanceMap);
        outputStream.flush();
        outputStream.close();
        return jaccardDistanceMap;

    }

    /**
     * create Bean of map of closeness centrality
     * {Book id, closeness centrality value}
     * @return the map of closeness centrality
     */
    @Bean
    public Map<Integer, Double> closenessCentrality() throws IOException, ClassNotFoundException {
        if (new File("closeness.ser").exists()){
            log.info("Loading Closeness Centrality Ranking from file to memory...");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("closeness.ser"));
            Map<Integer, Double> closenessMap = (Map<Integer, Double>) inputStream.readObject();
            inputStream.close();
            return closenessMap;
        }

        log.info("Charging Closeness Centrality Ranking...");
        if (Objects.isNull(jaccardDistanceMap))
            jaccardDistanceMap = calculateJaccardDistanceMap();
        int numberBooks = jaccardDistanceMap.size();
        HashMap<Integer, Double> closenessMap = new HashMap<>();
        for (Map.Entry<Integer, HashMap<Integer, Double>> jaccardDistance: jaccardDistanceMap.entrySet()){
            int id = jaccardDistance.getKey();
            HashMap<Integer, Double> distances = jaccardDistance.getValue();
            double sumDistance = distances.values().stream().mapToDouble(Double::doubleValue).sum();
            double closeness = (numberBooks - 1) / sumDistance;
            closenessMap.put(id, closeness);
        }
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(closenessMap.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        Map<Integer, Double> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<Integer, Double>> it = list.listIterator(); it.hasNext();){
            Map.Entry<Integer, Double> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("closeness.ser"));
        outputStream.writeObject(result);
        outputStream.flush();
        outputStream.close();

        return result;
    }

    private static Double jaccardDistanceBetweenTable(HashMap<String, Double> table1, HashMap<String, Double> table2){
        double dividend = 0;
        double divisor = 0;

        HashMap<String, Double> tableSmaller, tableBigger;
        if (table1.size() > table2.size()){
            tableBigger = table1;
            tableSmaller = table2;
        }else {
            tableBigger = table2;
            tableSmaller = table1;
        }

        int i = 0;
        for (Map.Entry<String, Double> entry: tableBigger.entrySet()){
            String stem = entry.getKey();
            Double relevance1 = entry.getValue();
            Double relevance2 = tableSmaller.get(stem);
            if (relevance2 != null){
                dividend += Math.max(relevance1, relevance2) - Math.min(relevance1, relevance2);
                divisor += Math.max(relevance1, relevance2);
            }
        }

        /*
        for (int i = 0; i < Math.min(table1.size(), table2.size()); i++){
            String stem1 = table1.get(i).getKey();
            String stem2 = table2.get(i).getKey();
            if (!stem1.equals(stem2)){
                System.out.println("count: " + i);
                break;
            }
            double relevance1 = table1.get(i).getValue();
            double relevance2 = table2.get(i).getValue();
            dividend += Math.max(relevance1, relevance2) - Math.min(relevance1, relevance2);
            divisor += Math.max(relevance1, relevance2);
        }
         */
        if (divisor == 0)
            return 1.0;
        return dividend / divisor;

    }
}

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

    /**
     * create Bean of jaccard distance matrix
     * {Book_id_1, map{Book_id2, distance_book1_book2}}
     * @return the jaccard distance matrix map
     */
    @Bean
    public HashMap<Integer, HashMap<Integer, Double>> jaccardDistanceMap(KeywordDictionary keywordDictionary) throws IOException, ClassNotFoundException {

        if (new File("jaccard.ser").exists()){
            log.info("Loading Jaccard Distance Matrix from file to memory...");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("jaccard.ser"));
            HashMap<Integer, HashMap<Integer, Double>> jaccardDistanceMap = (HashMap<Integer, HashMap<Integer, Double>>) inputStream.readObject();
            inputStream.close();
            return jaccardDistanceMap;
        }

        log.info("Charging Jaccard Distance Matrix...");

        HashMap<Integer, HashMap<Integer, Double>> jaccardDistanceMap = new HashMap<>();

        HashMap<Integer, List<Pair<String, Double>>> keywordBookTable = keywordDictionary.getKeywordBookTable();

        for (int id1: keywordBookTable.keySet()){
            for (int id2: keywordBookTable.keySet()){
                List<Pair<String, Double>> table1 = keywordBookTable.get(id1);
                List<Pair<String, Double>> table2 = keywordBookTable.get(id2);
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

    @Bean
    public Map<Integer, Double> closenessCentrality(HashMap<Integer, HashMap<Integer, Double>> jaccardDistanceMap) throws IOException, ClassNotFoundException {
        if (new File("closeness.ser").exists()){
            log.info("Loading Closeness Centrality Ranking from file to memory...");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("closeness.ser"));
            Map<Integer, Double> closenessMap = (Map<Integer, Double>) inputStream.readObject();
            inputStream.close();
            return closenessMap;
        }

        log.info("Charging Closeness Centrality Ranking...");

        HashMap<Integer, Double> closenessMap = new HashMap<>();
        for (Map.Entry<Integer, HashMap<Integer, Double>> jaccardDistance: jaccardDistanceMap.entrySet()){
            int id = jaccardDistance.getKey();
            HashMap<Integer, Double> distances = jaccardDistance.getValue();
            double closeness = 1 / distances.values().stream().mapToDouble(Double::doubleValue).sum();
            closenessMap.put(id, closeness);
        }
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(closenessMap.entrySet());
        Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
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

    private static Double jaccardDistanceBetweenTable(List<Pair<String, Double>> table1, List<Pair<String, Double>> table2){
        table1.sort(Comparator.comparing(Pair::getKey));
        table2.sort(Comparator.comparing(Pair::getKey));

        double dividend = 0;
        double divisor = 0;

        for (int i = 0; i < Math.min(table1.size(), table2.size()); i++){
            double relevance1 = table1.get(i).getValue();
            double relevance2 = table2.get(i).getValue();
            dividend += Math.max(relevance1, relevance2) - Math.min(relevance1, relevance2);
            divisor += Math.max(relevance1, relevance2);
        }

        return dividend / divisor;

    }
}

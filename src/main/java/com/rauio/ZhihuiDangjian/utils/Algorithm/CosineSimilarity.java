package com.rauio.ZhihuiDangjian.utils.Algorithm;

import java.util.HashMap;
import java.util.Map;

public class CosineSimilarity {
    public static double cosineSimilarity(String text1,String text2){
        Map<String,Integer> map1 = getWordFrequencyMap(text1);
        Map<String,Integer> map2 = getWordFrequencyMap(text2);

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for(String key: map1.keySet()){
            int freq1 = map1.get(key);
            dotProduct += freq1 * map2.getOrDefault(key,0);
            norm1 += freq1 * freq1;
        }

        for (int freqB : map2.values()){
            norm2 += freqB * freqB;
        }

        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        if(norm1 == 0.0 || norm2 == 0.0){
            return 0.0;
        }
        return dotProduct / (norm1 * norm2);
    }

    public static Map<String,Integer> getWordFrequencyMap(String text){
        Map<String,Integer> map = new HashMap<>();
        String[] words = text.toLowerCase().split("[,;，；。、 ]");

        for(String word:  words){
            map.put(word,map.getOrDefault(word,0)+1);
        }

        return map;
    }
}

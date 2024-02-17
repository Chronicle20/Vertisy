package tools;

import java.util.*;
import java.util.Map.Entry;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 17, 2016
 */
public class MapUtil{

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map){
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>(){

			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2){
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
		Map<K, V> result = new LinkedHashMap<K, V>();
		for(Map.Entry<K, V> entry : list){
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static Map<Integer, Integer> sortByComparator(Map<Integer, Integer> unsortMap, final boolean order){
		List<Entry<Integer, Integer>> list = new LinkedList<Entry<Integer, Integer>>(unsortMap.entrySet());
		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<Integer, Integer>>(){

			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2){
				if(order){
					return o1.getValue().compareTo(o2.getValue());
				}else{
					return o2.getValue().compareTo(o1.getValue());
				}
			}
		});
		// Maintaining insertion order with the help of LinkedList
		Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
		for(Entry<Integer, Integer> entry : list){
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}

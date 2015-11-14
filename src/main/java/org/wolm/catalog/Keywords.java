package org.wolm.catalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Helps discover keywords
 * 
 * @author wolm
 */
public class Keywords {
	private Map<String, Integer> keywords = new HashMap<>();
	private List<String> keywordList = new ArrayList<>();

	// list of stop words longer than 2 characters
	private List<String> stopWords = Arrays.asList(new String[] { "and", "are", "for", "from", "has", "its", "that",
			"the", "was", "were", "will", "with", "part", "pastor" });

	/**
	 * Given a string, extracts keywords from it and adds it to the collection
	 * 
	 * @param s
	 */
	public void add(String keywordString) {
		if (keywordString == null) return;

		for (String s : keywordString.toLowerCase().split("[^\\p{Alnum}]")) {
			if (StringUtils.isBlank(s)) continue;
			addKeyword(s);
			if (s.length() < 3) continue;
			if (stopWords.contains(s)) continue;
			addKeyword(s, 1);
		}
	}

	/**
	 * Given a collection of Strings, extracts keywords from it and adds it to the collection
	 * 
	 * @param s
	 */
	public void add(Collection<String> keywordStrings) {
		for (String s : keywordStrings)
			add(s);
	}

	/**
	 * Add all the keywords from another Keywords instance to this instance
	 * 
	 * @param other
	 */
	public void add(Keywords other) {
		// concatenate the list
		keywordList.addAll(other.getKeywordList());

		// add the histograms
		Map<String, Integer> otherMap = other.getKeywordMap();
		for (String otherKeyword : otherMap.keySet())
			addKeyword(otherKeyword, otherMap.get(otherKeyword));
	}

	/**
	 * @return All unique keywords in a random order
	 */
	public Set<String> getKeywordSet() {
		return keywords.keySet();
	}

	/**
	 * @return All keywords in a <String,Integer> map which indicates how many times each keyword was used
	 */
	public Map<String, Integer> getKeywordMap() {
		return keywords;
	}

	/**
	 * @return A list of all the keywords in the order they were received. This does not eliminate any stop words or
	 * duplicates. This is what is necessary for phrase searches
	 */
	public List<String> getKeywordList() {
		return keywordList;
	}

	/**
	 * Adds a single keyword to the list of keywords. Does not add the keyword to the histogram
	 * 
	 * @param keyword Keyword to add to map
	 */
	private void addKeyword(String keyword) {
		keywordList.add(keyword);
	}

	/**
	 * Adds a keyword to the histogram with the number of times it is added
	 * 
	 * @param keyword Keyword to add to map
	 * @param count Number of times to add the keyword
	 */
	private void addKeyword(String keyword, int count) {
		Integer total = keywords.get(keyword);
		total = Integer.valueOf(total == null ? count : total + count);
		keywords.put(keyword, total);
	}

}

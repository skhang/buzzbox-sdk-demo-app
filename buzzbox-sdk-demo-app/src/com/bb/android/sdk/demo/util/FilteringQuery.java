package com.bb.android.sdk.demo.util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import com.bb.android.sdk.demo.model.Topic;

/**
 * A FilteringQuery is one of the queries in the advance filtering
 * it's parsed into keywords ("windows 7" would be 1 keyword, TODO)
 * and it recognize stopwords (1-long keywords and common words)
 */
public class FilteringQuery {
	public final static String TOPIC_TOKENIZER_SEPARATORS = " “‘’”»«`~!@#$€%^&*()_+-—–=[]\\|}{;'\":,.…/<>?\n\r\f\t";

	HashSet<Topic> savedTopics = new HashSet<Topic>(); 
	
	public HashSet<String> originalKeywordsLowercased = new HashSet<String>(3);
	public HashSet<String> expandedKeywords = new HashSet<String>(3);
	public boolean containsStopWords = false;
	public boolean hasValidKeywords = false;
	
	public HashSet<String> stopWordsKeywords = new HashSet<String>(3);
	
	public final static HashSet<String> clStopWords = new HashSet<String>(); 
	static {
		clStopWords.add("and");
		clStopWords.add("or");
		clStopWords.add("in");
		clStopWords.add("is");
		clStopWords.add("be");
		clStopWords.add("it");
		clStopWords.add("on");
		clStopWords.add("to");
		clStopWords.add("the");		
	}
	
	
	
	public static FilteringQuery parse(String queryText){
		if (queryText==null || queryText.trim().length()==0){
			return null;
		}
		
		FilteringQuery q = new FilteringQuery();
		
		StringTokenizer st = new StringTokenizer(queryText,"\t");
		while (st.hasMoreElements()){
			String tokenEncoded = st.nextToken();
			
			String token = tokenEncoded.substring(1);
			boolean enabled = tokenEncoded.charAt(0) == '1';
			Topic t = new Topic(token);
			t.notificationEnabled = enabled;
			q.savedTopics.add(t);
			
			q.originalKeywordsLowercased.add(token.toLowerCase());
			if (token.length()<2 || clStopWords.contains(token)) {
				q.stopWordsKeywords.addAll( toCases(token) );
				q.containsStopWords = true;
			} else {
				q.hasValidKeywords = true;
			}
			if (enabled) {
				q.expandedKeywords.addAll( toCases(token) );
			}
		}
		
		return q;
	}

	

	private static Collection<String> toCases(String token) {
		if (token==null) return null;
		if (token.length()==0) return Collections.emptyList();
		HashSet<String> differentCasesForToken = new HashSet<String>(3);
		
		// TOKEN
		// Token
		// token
		
		// what about iPhone?! or xBox XpressMusic , 64Gigs, WiFi, T-Mobile
		
		String lower = token.toLowerCase();
		differentCasesForToken.add(lower);
		differentCasesForToken.add(token.toUpperCase());
		
		if (token.indexOf(" ")<0) {
			String firstUpper = token.substring(0,1).toUpperCase()+lower.substring(1);
			differentCasesForToken.add(firstUpper);
			
			if (token.length()>3){
				String secondUpper = lower.substring(0,1)+lower.substring(1,2).toUpperCase()+token.substring(2);
				differentCasesForToken.add(secondUpper);
			}
		}
		return differentCasesForToken;
	}


	/**
	 * 
	 * @return 
	 * 0  - if nothing is found
	 * 1  - if text contains at least 1 keyword in inTextQuery (and a notification should be shown)
	 * -1 - if text contains at least 1 keyword in notInTextQuey (and the story should be removed)
	 */
	public static FilteringQueryResult processText(
			String text,
			FilteringQuery inTextQuery,
			FilteringQuery notInTextQuery) {
		FilteringQueryResult res = new FilteringQueryResult();
		if (text == null || text.trim().length()==0) return res; // not filtered
		if (inTextQuery==null && notInTextQuery==null) return res;
		
		// TODO reimplement with regex and ignore case \\bxxx\\b|\\byyy zzz\\b
		if (inTextQuery.expandedKeywords.size()==0 && notInTextQuery==null) return res; // everything is disabled
				
		StringTokenizer tokenizer = new StringTokenizer(text, TOPIC_TOKENIZER_SEPARATORS);
		while (tokenizer.hasMoreElements()) {
			// TODO what if contains BOTH.. stop or notify? 
			
			String token = tokenizer.nextToken();
			if (notInTextQuery!=null && notInTextQuery.expandedKeywords.contains(token)){
				res.setFiltered(true);
				res.setKeywordsFound(token);
				return res;
			}
			
			if (inTextQuery!=null && inTextQuery.expandedKeywords.contains(token)) {
				res.setNotification(true);
				res.setKeywordsFound(token);
				return res;
			}			
		}
		
		return res;
	}
	
	/**
	 * return true if text contains at least 1 keyword in the notInTextQuery
	 * of if does not contains ALL the keywords in inTextQuery 
	 */
	public static boolean filterString(
			String text,
			FilteringQuery inTextQuery,
			FilteringQuery notInTextQuery) {
		
		if (text == null || text.trim().length()==0) return false; // not filtered
		if (inTextQuery==null && notInTextQuery==null) return false;
		
		// TODO reimplement with regex and ignore case \\bxxx\\b|\\byyy zzz\\b
		
		HashSet<String> copyOfOriginal = inTextQuery==null? null : new HashSet<String>(inTextQuery.originalKeywordsLowercased);
		
		StringTokenizer tokenizer = new StringTokenizer(text, TOPIC_TOKENIZER_SEPARATORS);
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			if (notInTextQuery!=null && notInTextQuery.expandedKeywords.contains(token)) return true;
			
			if (inTextQuery!=null && inTextQuery.expandedKeywords.contains(token)) {
				copyOfOriginal.remove(token.toLowerCase());
				if (notInTextQuery==null && copyOfOriginal.isEmpty()) return false;
			}			
		}
		
		if (copyOfOriginal!=null && copyOfOriginal.isEmpty()) return false;
		if (copyOfOriginal!=null && !copyOfOriginal.isEmpty()) return true;
		
		return false;
	}

	
	@Override
	public String toString() {
		return this.originalKeywordsLowercased.toString();
	}
	
	public List<Topic> getKeywords() {
		ArrayList<Topic> copy = new ArrayList<Topic>(savedTopics);
		Collections.sort(copy);
		return copy;
	}
	
	public static void main(String[] args) {
		
		System.out.println(FilteringQuery.filterString("test", FilteringQuery.parse("test1 test2"), null));
		
		System.out.println(FilteringQuery.filterString("test", FilteringQuery.parse("test1 test2"), FilteringQuery.parse("test91 test92")));
		System.out.println(FilteringQuery.filterString("test test", FilteringQuery.parse("test1 test2"), FilteringQuery.parse("test91 test92")));
		System.out.println(FilteringQuery.filterString("test test1 test", FilteringQuery.parse("test1 test2"), FilteringQuery.parse("test91 test92")));
		System.out.println(FilteringQuery.filterString("test test2 test", FilteringQuery.parse("test1 test2"), FilteringQuery.parse("test91 test92")));
		System.out.println(FilteringQuery.filterString("test test91 test1 test", FilteringQuery.parse("test1 test2"), FilteringQuery.parse("test91 test92")));
	
		
		System.out.println(FilteringQuery.filterString("test test", null, FilteringQuery.parse("test1 test2")));
		System.out.println(FilteringQuery.filterString("test test1 test", null, FilteringQuery.parse("test1 test2")));
		System.out.println(FilteringQuery.filterString("test test2 test test91 test92 test91", FilteringQuery.parse("test91 test92"), FilteringQuery.parse("test1 test2")));
		System.out.println(FilteringQuery.filterString("test test91 Test1 test",  FilteringQuery.parse("test"), FilteringQuery.parse("test1 test2")));
	
		System.out.println(FilteringQuery.filterString("test test91 test1 test",  FilteringQuery.parse("test"), FilteringQuery.parse("testx testY")));
		System.out.println(FilteringQuery.filterString("TEST test91 test1 test",  FilteringQuery.parse("test"), FilteringQuery.parse("testx testY")));
		
	}
	public static class FilteringQueryResult {
		String keywordsFound = null;
		boolean notification = false;
		boolean filtered = false;
		public FilteringQueryResult() {
		
		}
		
		public String getKeywordsFound() {
			return keywordsFound;
		}
		public void setKeywordsFound(String keywordsFound) {
			this.keywordsFound = keywordsFound;
		}
		public boolean isNotification() {
			return notification;
		}
		public void setNotification(boolean notification) {
			this.notification = notification;
		}
		public boolean isFiltered() {
			return filtered;
		}
		public void setFiltered(boolean filtered) {
			this.filtered = filtered;
		}
	}
}


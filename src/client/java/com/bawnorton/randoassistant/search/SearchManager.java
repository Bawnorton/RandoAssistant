package com.bawnorton.randoassistant.search;

import com.bawnorton.randoassistant.config.Config;

import java.util.*;

public class SearchManager<T extends Searchable> {
    private final Map<String, T> searchMap;
    private final List<String> searchList;
    private Config.SearchType searchType;
    private String cachedQuery = "";
    private Config.SearchType cachedSearchType = Config.SearchType.CONTAINS;
    private List<Optional<T>> cachedMatches = List.of(Optional.empty());

    public SearchManager(List<T> searchList) {
        this.searchMap = new HashMap<>();
        this.searchList = new ArrayList<>();
        for (T searchable : searchList) {
            String searchableString = filter(searchable.getSearchableString());
            if (searchableString != null) {
                this.searchMap.put(searchableString, searchable);
                this.searchList.add(searchableString);
            }
        }
        this.searchType = Config.getInstance().searchType;
    }

    public static int levenshteinDistance(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    private String filter(String in) {
        in = in.toLowerCase().replaceAll("\\s+", "");
        if (in.isEmpty()) return null;
        return in;
    }

    private T linearSearch(String query) {
        for (String match : searchList) {
            if (match.equals(query)) return searchMap.get(match);
        }
        return null;
    }

    public List<Optional<T>> getBestMatch(String query) {
        if (query.equals(cachedQuery) && cachedSearchType.equals(searchType)) return cachedMatches;
        cachedSearchType = searchType;
        cachedQuery = query;
        String adjustedQuery = filter(query);
        if (adjustedQuery == null) return List.of(Optional.empty());
        if (searchType == Config.SearchType.EXACT) {
            cachedMatches = List.of(Optional.ofNullable(linearSearch(adjustedQuery)));
        } else if (searchType == Config.SearchType.CONTAINS) {
            List<Optional<T>> matches = new ArrayList<>();
            for (String match : searchList) {
                if (match.contains(adjustedQuery)) {
                    matches.add(Optional.of(searchMap.get(match)));
                }
            }
            cachedMatches = matches;
        } else if (searchType == Config.SearchType.FUZZY) {
            int bestDistance = Integer.MAX_VALUE;
            List<Optional<T>> matches = new ArrayList<>();
            for (String match : searchList) {
                int distance = levenshteinDistance(adjustedQuery, match);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    matches.add(Optional.of(searchMap.get(match)));
                }
            }
            cachedMatches = matches;
        }
        cachedMatches = removeDuplicates(cachedMatches);
        return cachedMatches;
    }

    private List<Optional<T>> removeDuplicates(List<Optional<T>> list) {
        return new ArrayList<>(new HashSet<>(list));
    }

    public Config.SearchType getSearchType() {
        return searchType;
    }

    public void nextSearchType() {
        searchType = searchType.next();
        Config.getInstance().searchType = searchType;
    }
}

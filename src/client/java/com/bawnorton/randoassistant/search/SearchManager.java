package com.bawnorton.randoassistant.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SearchManager<T extends Searchable> {
    private final Map<String, T> searchMap;
    private final Set<String> searchSet;

    public SearchManager(Set<T> searchSet) {
        this.searchMap = new HashMap<>();
        this.searchSet = new HashSet<>();
        searchSet.forEach(searchable -> searchable.getSearchableStrings().forEach(string -> {
            string = filter(string);
            searchMap.put(string, searchable);
            this.searchSet.add(string);
        }));
    }

    private String filter(String in) {
        in = in.toLowerCase().replaceAll("^[a-z]", "");
        if (in.isEmpty()) return null;
        return in;
    }

    public Set<T> getMatches(String query) {
        String adjustedQuery = filter(query);
        if (adjustedQuery == null) return Set.of();
        Set<T> matches = new HashSet<>();
        searchSet.forEach(match -> {
            if (match.contains(adjustedQuery)) {
                matches.add(searchMap.get(match));
            }
        });
        return matches;
    }
}

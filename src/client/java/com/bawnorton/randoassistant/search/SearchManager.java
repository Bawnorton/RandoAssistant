package com.bawnorton.randoassistant.search;

import java.util.*;
import java.util.stream.Collectors;

public class SearchManager<T extends Searchable> {
    private final Map<String, T> searchMap;
    private final Set<String> searchSet;
    private String cachedQuery = "";
    private Set<T> cachedMatches = Set.of();

    public SearchManager(Set<T> searchSet) {
        this.searchMap = new HashMap<>();
        this.searchSet = new HashSet<>();
        searchSet.forEach(searchable -> {
            searchable.getSearchableStrings().forEach(string -> {
                searchMap.put(string, searchable);
                this.searchSet.add(string);
            });
        });
    }

    private String filter(String in) {
        in = in.toLowerCase().replaceAll("\\s+", "");
        if (in.isEmpty()) return null;
        return in;
    }

    public Set<T> getMatches(String query) {
        if (query.equals(cachedQuery)) return cachedMatches;
        cachedQuery = query;
        String adjustedQuery = filter(query);
        if (adjustedQuery == null) return Set.of();
        Set<T> matches = new HashSet<>();
        searchSet.forEach(match -> {
            if (match.contains(adjustedQuery)) {
                matches.add(searchMap.get(match));
            }
        });
        cachedMatches = matches;
        return matches;
    }
}

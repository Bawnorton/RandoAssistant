package com.bawnorton.randoassistant.tracking.graph;

import com.bawnorton.randoassistant.tracking.Tracker;
import com.bawnorton.randoassistant.tracking.trackable.Trackable;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// class that crawls up a trackable tree and builds a graph of the trackables it finds
public abstract class TrackableCrawler {
    private static final Map<Identifier, TrackingGraph> graphCache = new HashMap<>();

    public static TrackingGraph crawl(Identifier target) {
        if(graphCache.containsKey(target)) return graphCache.get(target);
        TrackingGraph graph = new TrackingGraph();
        crawl(target, graph);
        graphCache.put(target, graph);
        return graph;
    }

    private static void crawl(Identifier target, TrackingGraph graph) {
        graph.add(target);
        Set<Trackable<Identifier>> sources = Tracker.getInstance().getTracked(target);
        if(sources == null) return;
        for(Trackable<Identifier> source : sources) {
            if(!source.isEnabled()) continue;
            Identifier sourceId = source.getIdentifier();
            if (graphCache.containsKey(sourceId)) {
                graph.connect(sourceId, target);
                graph.merge(graphCache.get(sourceId));
                continue;
            }
            if(graph.contains(sourceId)) continue;
            graph.add(sourceId);
            graph.connect(sourceId, target);
            crawl(sourceId, graph);
        }
    }
}

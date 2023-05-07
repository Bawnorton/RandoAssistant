package com.bawnorton.randoassistant.tracking.graph;

import com.bawnorton.randoassistant.tracking.trackable.Trackable;

// class that crawls up a trackable tree and builds a graph of the trackables it finds
public abstract class TrackableCrawler {
    public static TrackingGraph crawl(Trackable<?> trackable) {
        TrackingGraph graph = new TrackingGraph();
        crawl(trackable, graph);
        return graph;
    }

    private static void crawl(Trackable<?> trackable, TrackingGraph graph) {
        graph.add(trackable);
        for(Trackable<?> source : trackable.getEnabledSources()) {
            if(graph.contains(source)) continue;
            graph.add(source);
            graph.connect(source, trackable);
            crawl(source, graph);
        }
    }
}

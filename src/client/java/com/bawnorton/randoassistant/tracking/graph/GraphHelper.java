package com.bawnorton.randoassistant.tracking.graph;

import com.bawnorton.randoassistant.util.NaturalBlocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class GraphHelper {
    public static Identifier getBestSource(TrackingGraph graph, TrackingGraph.Vertex vertex) {
        TrackingGraph.Vertex bestNaturalParent = getClosestNaturallyFoundParent(graph, vertex);
        if(bestNaturalParent != null) {
            return bestNaturalParent.getIdentifier();
        }
        TrackingGraph.Vertex cloesestRoot = getClosestRoot(graph, vertex);
        if(cloesestRoot != null) {
            return cloesestRoot.getIdentifier();
        }
        Set<TrackingGraph.Vertex> roots = graph.getRoots();
        if(roots.size() == 1) {
            return roots.iterator().next().getIdentifier();
        }
        return vertex.getIdentifier();
    }

    public static TrackingGraph.Vertex getClosestRoot(TrackingGraph graph, TrackingGraph.Vertex vertex) {
        Set<TrackingGraph.Vertex> roots = graph.getRoots();
        int cloesestDistance = Integer.MAX_VALUE;
        TrackingGraph.Vertex cloesestRoot = null;
        for(TrackingGraph.Vertex root : roots) {
            int distance = graph.distanceBetween(root, vertex);
            if(distance < cloesestDistance) {
                cloesestDistance = distance;
                cloesestRoot = root;
            }
        }
        return cloesestRoot;
    }

    public static TrackingGraph.Vertex getClosestNaturallyFoundParent(TrackingGraph graph, TrackingGraph.Vertex vertex) {
        if(vertex.getIdentifier().getPath().contains("acacia_button"))
            System.out.println("test");
        Set<TrackingGraph.Vertex> vertices = new HashSet<>(graph.vertexSet());
        vertices.remove(vertex);
        int cloesestDistance = Integer.MAX_VALUE;
        TrackingGraph.Vertex cloesestParent = null;
        for(TrackingGraph.Vertex parent : vertices) {
            if(isNaturallyFound(parent)) {
                int distance = graph.distanceBetween(parent, vertex);
                if(distance < cloesestDistance) {
                    cloesestDistance = distance;
                    cloesestParent = parent;
                }
            }
        }
        return cloesestParent;
    }

    public static boolean isNaturallyFound(TrackingGraph.Vertex vertex) {
        Identifier identifier = vertex.getIdentifier();
        if(Registries.ENTITY_TYPE.containsId(identifier)) {
            return true;
        }
        if(Registries.BLOCK.containsId(identifier)) {
            return NaturalBlocks.isNatural(Registries.BLOCK.get(identifier));
        }
        return false;
    }
}

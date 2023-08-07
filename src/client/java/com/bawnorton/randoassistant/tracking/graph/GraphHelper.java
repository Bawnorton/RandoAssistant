package com.bawnorton.randoassistant.tracking.graph;

import com.bawnorton.randoassistant.util.NaturalBlocks;
import com.bawnorton.randoassistant.util.tuples.Pair;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class GraphHelper {
    public static Pair<Identifier, Integer> getBestSource(TrackingGraph graph, TrackingGraph.Vertex vertex) {
        Pair<Identifier, Integer> bestNaturalParent = getClosestNaturallyFoundParent(graph, vertex);
        if(bestNaturalParent != null) {
            return bestNaturalParent;
        }
        Pair<Identifier, Integer> cloesestRoot = getClosestRoot(graph, vertex);
        if(cloesestRoot != null) {
            return cloesestRoot;
        }
        Set<TrackingGraph.Vertex> roots = graph.getRoots();
        if(roots.size() == 1) {
            TrackingGraph.Vertex root = roots.iterator().next();
            return Pair.of(root.getContent(), graph.distanceBetween(root, vertex));
        }
        return Pair.of(vertex.getContent(), 0);
    }

    public static Pair<Identifier, Integer> getClosestRoot(TrackingGraph graph, TrackingGraph.Vertex vertex) {
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
        if(cloesestRoot == null) return null;
        return Pair.of(cloesestRoot.getContent(), cloesestDistance);
    }

    public static Pair<Identifier, Integer> getClosestNaturallyFoundParent(TrackingGraph graph, TrackingGraph.Vertex vertex) {
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
        if(cloesestParent == null) return null;
        return Pair.of(cloesestParent.getContent(), cloesestDistance);
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

    public static Pair<Identifier, Integer> getFurthestTarget(TrackingGraph graph, TrackingGraph.Vertex vertex) {
        Set<TrackingGraph.Vertex> targets = new HashSet<>(graph.vertexSet());
        int furthestDistance = Integer.MIN_VALUE;
        TrackingGraph.Vertex furthestTarget = null;
        for(TrackingGraph.Vertex target : targets) {
            int distance = graph.distanceBetween(vertex, target);
            if(distance > furthestDistance) {
                furthestDistance = distance;
                furthestTarget = target;
            }
        }
        if(furthestTarget == null) return Pair.of(vertex.getContent(), 0);
        return Pair.of(furthestTarget.getContent(), furthestDistance);
    }
}

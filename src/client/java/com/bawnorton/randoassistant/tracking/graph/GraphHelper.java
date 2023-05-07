package com.bawnorton.randoassistant.tracking.graph;

import com.bawnorton.randoassistant.tracking.trackable.Trackable;
import com.bawnorton.randoassistant.util.NaturalBlocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;

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
        Set<TrackingGraph.Vertex> roots = graph.getRootsOf(vertex);
        if(roots.size() == 1) {
            return roots.iterator().next().getIdentifier();
        }
        return vertex.getIdentifier();
    }

    public static TrackingGraph.Vertex getClosestRoot(TrackingGraph graph, TrackingGraph.Vertex vertex) {
        Set<TrackingGraph.Vertex> roots = graph.getRootsOf(vertex);
        int cloesestDistance = Integer.MAX_VALUE;
        TrackingGraph.Vertex cloesestRoot = null;
        for(TrackingGraph.Vertex root : roots) {
            int distance = graph.distanceToChild(root, vertex);
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
        Set<TrackingGraph.Vertex> parents = graph.getParentVertices(vertex);
        int cloesestDistance = Integer.MAX_VALUE;
        TrackingGraph.Vertex cloesestParent = null;
        for(TrackingGraph.Vertex parent : parents) {
            if(isNaturallyFound(parent)) {
                int distance = graph.distanceToChild(parent, vertex);
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

    public static void removeDisabled(TrackingGraph graph, List<Trackable<?>> disabled) {
        for(Trackable<?> trackable : disabled) {
            graph.removeVertexAndParents(trackable.getIdentifier());
        }
    }
}

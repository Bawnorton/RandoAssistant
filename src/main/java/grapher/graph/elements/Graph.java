package grapher.graph.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * /**
 * A graph consisting of a set of vertices of type <code>V</code>
 * and a set of edges of type <code>E</code>.
 *
 * @param <V> The vertex type
 * @param <E> The edge type
 * @author Renata
 */
public class Graph<V extends Vertex, E extends Edge<V>> {

    protected final List<V> vertices;
    protected final List<E> edges;
    protected final boolean directed = false;

    /**
     * An adjacent list contains a list of all the edges leaving the vertex
     */
    protected final Map<V, List<E>> adjacentLists;

    /**
     * Map of all edges leaving vertices
     */
    protected final Map<V, List<E>> outgoingEdges;

    /**
     * Map of all edges entering vertices
     */
    protected final Map<V, List<E>> incomingEdges;

    /**
     * Vertex by content map
     */
    protected final Map<Object, V> vertexByContentMap;

    /**
     * Creates a graph by creating empty lists of edges, vertices and other properties
     * By default, the graph is undirected
     */
    public Graph() {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        adjacentLists = new HashMap<>();
        vertexByContentMap = new HashMap<>();
        outgoingEdges = new HashMap<>();
        incomingEdges = new HashMap<>();
    }

    /**
     * Checks if the graph contains a certain vertex
     *
     * @param v Vertex
     * @return {@code true} if the graph contains {@code v}, {@code false} otherwise
     */
    public boolean hasVertex(V v) {
        return !vertices.contains(v);
    }

    /**
     * Add one vertex to the graph
     *
     * @param v Vertex to add
     */
    public void addVertex(V v) {
        if (vertices.contains(v))
            return;
        vertices.add(v);
        adjacentLists.put(v, new ArrayList<>());
        vertexByContentMap.put(v.getContent(), v);
    }


    @SuppressWarnings("unchecked")
    public void addEdge(E... edge) {

        for (E e : edge) {
            if (edges.contains(e))
                continue;
            edges.add(e);

            V origin = e.getOrigin();
            V destination = e.getDestination();

            if (adjacentLists.get(origin) != null) {
                adjacentLists.get(origin).add(e);
            }
            //add it even if the graph is directed
            if (adjacentLists.get(e.getDestination()) != null) {
                adjacentLists.get(e.getDestination()).add(e);
            }

            if (!incomingEdges.containsKey(destination))
                incomingEdges.put(destination, new ArrayList<>());

            if (!outgoingEdges.containsKey(origin))
                outgoingEdges.put(origin, new ArrayList<>());

            incomingEdges.get(destination).add(e);
            outgoingEdges.get(origin).add(e);

        }
    }


    /**
     * @return Graph's vertices
     */
    public List<V> getVertices() {
        return vertices;
    }

    /**
     * @return Graph's edges
     */
    public List<E> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return "Graph [vertices=" + vertices + ", edges=" + edges + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (directed ? 1231 : 1237);
        result = prime * result + edges.hashCode();
        result = prime * result
                + vertices.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Graph<?, ?> other = (Graph<?, ?>) obj;
        if (other.getVertices() == null)
            return false;
        if (other.getVertices() != null) {
            if (vertices.size() != other.getVertices().size())
                return false;
            for (V v1 : vertices) {
                boolean found = false;
                for (Object v2 : other.getVertices()) {
                    if (v1.equals(v2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }

        if (other.getEdges() == null)
            return false;
        if (other.getEdges() != null) {
            if (edges.size() != other.getEdges().size())
                return false;
            for (E e1 : edges) {
                boolean found = false;
                for (Object e2 : other.getEdges()) {
                    if (e1.equals(e2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }
}

package grapher.graph.elements;


/**
 * Edge ofItems the graph
 *
 * @param <V> The vertex type
 * @author Renata
 */
public interface Edge<V extends Vertex> {

    /**
     * @return Origin ofItems the edge
     */
    V getOrigin();

    /**
     * @return Destination ofItems the edge
     */
    V getDestination();
}

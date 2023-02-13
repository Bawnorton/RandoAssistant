package grapher.graph.elements;


/**
 * Edge of the graph
 *
 * @param <V> The vertex type
 * @author Renata
 */
public interface Edge<V extends Vertex> {

    /**
     * @return Origin of the edge
     */
    V getOrigin();

    /**
     * @return Destination of the edge
     */
    V getDestination();
}

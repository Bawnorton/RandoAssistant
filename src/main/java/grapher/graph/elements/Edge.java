package grapher.graph.elements;


/**
 * Edge of the graph
 * @author Renata
 * @param <V> The vertex type
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

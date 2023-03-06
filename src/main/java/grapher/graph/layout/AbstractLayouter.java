package grapher.graph.layout;

import grapher.graph.drawing.Drawing;
import grapher.graph.elements.Edge;
import grapher.graph.elements.Graph;
import grapher.graph.elements.Vertex;

/**
 * Abstract layouter class meant to be extended by all layouters.
 *
 * @param <V> The vertex type
 * @param <E> The edge type
 * @author Renata
 */
public abstract class AbstractLayouter<V extends Vertex, E extends Edge<V>> {

    /**
     * Indicates if the given algorithm lays out the whole graph, even if it
     * consists ofItems more than one 1-connected component
     */
    protected final boolean oneGraph = true;
    /**
     * Indicates if the algorithm also routes the edges
     */
    protected boolean positionsEdges = false;

    /**
     * Lays out the graph, taking into account given properties
     *
     * @param graph            Graph that should be laid out
     * @param layoutProperties Properties ofItems the layout algorithm
     * @return Drawing
     */
    public abstract Drawing<V, E> layout(Graph<V, E> graph, GraphLayoutProperties layoutProperties);

    /**
     * @return Indicator ofItems weather the given algorithm lays out the whole graph, even if it
     * consists ofItems more than one 1-connected component
     */
    public boolean isOneGraph() {
        return oneGraph;
    }

}

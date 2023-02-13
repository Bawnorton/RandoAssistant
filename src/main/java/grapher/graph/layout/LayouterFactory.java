package grapher.graph.layout;

import grapher.graph.elements.Edge;
import grapher.graph.elements.Vertex;
import grapher.graph.layout.organic.JGraphHierarchicalLayouter;

/**
 * Factory class used to create an instance of the layouter class
 *
 * @param <V> The vertex type
 * @param <E> The edge type
 * @author Renata
 */
public class LayouterFactory<V extends Vertex, E extends Edge<V>> {

    /**
     * Creates the appropriate layouter instance
     *
     * @param algorithm Desired algorithm represented by an enum value
     * @return Layouter
     */
    public AbstractLayouter<V, E> createLayouter(LayoutAlgorithms algorithm) {

        AbstractLayouter<V, E> layouter = null;

        if (algorithm == LayoutAlgorithms.HIERARCHICAL)
            layouter = new JGraphHierarchicalLayouter<>();

        return layouter;

    }


}

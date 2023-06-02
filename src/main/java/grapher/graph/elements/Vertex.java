package grapher.graph.elements;

import java.awt.*;

/**
 * Represent vertex ofItems the graph
 *
 * @author Renata
 */
public interface Vertex {

    /**
     * @return Size ofItems the vertex
     */
    Dimension getSize();

    /**
     * @return Content ofItems the vertex
     */
    Object getContent();
}

package grapher.graph.layout;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;
import grapher.graph.drawing.Drawing;
import grapher.graph.elements.Edge;
import grapher.graph.elements.Graph;
import grapher.graph.elements.Vertex;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains general code used for calling JGraphX layout algorithms
 *
 * @param <V> The vertex type
 * @param <E> The edge type
 * @author Renata
 */
public abstract class AbstractJGraphXLayouter<V extends Vertex, E extends Edge<V>> extends AbstractLayouter<V, E> {


    /**
     * Maps vertices ofItems the supplied graph to vertices ofItems the JGraphX graph
     */
    protected final Map<V, Object> verticesMap = new HashMap<>();
    /**
     * Maps edges ofItems the supplied graph to edges ofItems the JGraphX graph
     */
    protected final Map<E, Object> edgesMap = new HashMap<>();
    /**
     * JGraphX layout algorithm
     */
    protected mxGraphLayout layouter;
    /**
     * JGraphX graph which has to be instantiated
     */
    protected mxGraph jGraphXGraph;

    /**
     * Converts the given graph into a JGraphX graph
     */
    protected void createJGraphXGraph(Graph<V, E> graph) {
        jGraphXGraph = new mxGraph();
        jGraphXGraph.getModel().beginUpdate();
        Object parent = jGraphXGraph.getDefaultParent();
        mxIGraphModel model = jGraphXGraph.getModel();
        try {
            for (V v : graph.getVertices()) {
                Dimension size;
                if (v.getSize() == null)
                    size = new Dimension(10, 10);
                else
                    size = v.getSize();

                Object jgraphxVertex = jGraphXGraph.insertVertex(parent, null, v, 0, 0,
                        size.getWidth(), size.getHeight());
                model.getGeometry(jgraphxVertex).setHeight(size.getHeight());
                model.getGeometry(jgraphxVertex).setWidth(size.getWidth()); //Doesn't make much difference...
                verticesMap.put(v, jgraphxVertex);
            }
            for (E e : graph.getEdges()) {
                Object v1 = verticesMap.get(e.getOrigin());
                Object v2 = verticesMap.get(e.getDestination());
                Object jGraphXEdge = jGraphXGraph.insertEdge(parent, null, null, v1, v2);
                edgesMap.put(e, jGraphXEdge);
            }
        } finally {
            jGraphXGraph.getModel().endUpdate();
        }
    }

    public Drawing<V, E> layout(Graph<V, E> graph, GraphLayoutProperties layoutProperties) {
        createJGraphXGraph(graph);
        initLayouter(layoutProperties);
        return createDrawing();
    }

    /**
     * Executes the layout algorithm and creates the drawing ofItems the graph
     * (mappings ofItems its vertices and edges to their positions)
     *
     * @return Drawing ofItems the graph
     */
    protected Drawing<V, E> createDrawing() {

        Object parent = jGraphXGraph.getDefaultParent();
        layouter.execute(parent);
        Drawing<V, E> drawing = new Drawing<>();

        mxIGraphModel model = jGraphXGraph.getModel();
        for (V v : verticesMap.keySet()) {
            mxGeometry geometry = model.getGeometry(verticesMap.get(v));
            drawing.setVertexPosition(v, geometry.getPoint());
        }

        if (positionsEdges) {
            for (E e : edgesMap.keySet()) {
                mxGeometry geometry = model.getGeometry(edgesMap.get(e));
                if (geometry != null && geometry.getPoints() != null) {
                    List<Point2D> points = new ArrayList<>();
                    Point2D originPoint = drawing.getVertexMappings().get(e.getOrigin());
                    points.add(new Point2D.Double(originPoint.getX(), originPoint.getY()));
                    for (int i = 1; i < geometry.getPoints().size() - 1; i++)
                        points.add(geometry.getPoints().get(i).getPoint());
                    Point2D destinationPoint = drawing.getVertexMappings().get(e.getDestination());
                    points.add(new Point2D.Double(destinationPoint.getX(), destinationPoint.getY()));
                    drawing.getEdgeMappings().put(e, points);
                }
            }

            return drawing;
        }


        return drawing;
    }

    /**
     * Initializes the appropriate algorithm (layouter)
     */
    protected abstract void initLayouter(GraphLayoutProperties layoutProperties);

}

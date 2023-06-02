package grapher.graph.layout;

import grapher.graph.drawing.Drawing;
import grapher.graph.elements.Edge;
import grapher.graph.elements.Graph;
import grapher.graph.elements.Vertex;
import grapher.graph.elements.exception.CannotBeAppliedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Layouter accepts lists ofItems veritces and edges which might in fact form more than one graph
 * It then forms the graphs which can later be layouted using the desired method
 *
 * @param <V> The vertex type
 * @param <E> The edge type
 * @author Renata
 */
public class Layouter<V extends Vertex, E extends Edge<V>> {


    /**
     * Factory used to create an instance ofItems the appropriate algorithm
     */
    private final LayouterFactory<V, E> layouterFactory;
    /**
     * Edges ofItems the graph (diagram) that is to be laid out
     */
    private List<E> edges;
    /**
     * Vertices ofItems the graph (diagram) that is to be laid out
     */
    private List<V> vertices;
    /**
     * Names the layout algorithm to be applied
     */
    private LayoutAlgorithms algorithm;
    /**
     * Properties ofItems the algorithm that should be set
     */
    private GraphLayoutProperties layoutProperties;

    /**
     * Constructs the layouter without populating list ofItems edges and vertices
     */
    @SuppressWarnings("unused")
    public Layouter() {
        layouterFactory = new LayouterFactory<>();
    }

    /**
     * Constructs the layouter and sets lists ofItems vertices and edges, as well
     * as the layout algorithm which should be used
     *
     * @param vertices  A list ofItems vertices
     * @param edges     A list ofItems edges
     * @param algorithm Layout algorithm
     */
    public Layouter(List<V> vertices, List<E> edges, LayoutAlgorithms algorithm) {
        this.edges = edges;
        this.vertices = vertices;
        this.algorithm = algorithm;
        layouterFactory = new LayouterFactory<>();
    }

    /**
     * Constructs the layouter and sets lists ofItems vertices and edges, as well
     * as the layout algorithm which should be used and its properties
     *
     * @param vertices         A list ofItems vertices
     * @param edges            A list ofItems edges
     * @param algorithm        Layout algorithm
     * @param layoutProperties Algorithm's proeprties
     */
    public Layouter(List<V> vertices, List<E> edges, LayoutAlgorithms algorithm, GraphLayoutProperties layoutProperties) {
        this(vertices, edges, algorithm);
        this.layoutProperties = layoutProperties;
    }

    public Layouter(Set<V> vertices, Set<E> edges, LayoutAlgorithms algorithm, GraphLayoutProperties layoutProperties) {
        this(new ArrayList<>(vertices), new ArrayList<>(edges), algorithm, layoutProperties);
    }

    @SuppressWarnings("unchecked")
    private Graph<V, E> formOneGraph(List<V> vertices, List<E> edges) {
        Graph<V, E> graph = new Graph<>();

        for (V v : vertices)
            graph.addVertex(v);

        for (E e : edges)
            graph.addEdge(e);

        return graph;
    }

    /**
     * Forms one graph for each 1-connected component
     *
     * @return A list ofItems formed graphs
     */
    private List<Graph<V, E>> formGraphs(List<V> vertices, List<E> edges) {

        List<Graph<V, E>> graphs = new ArrayList<>();
        List<V> coveredVertices = new ArrayList<>();
        List<E> coveredEdges = new ArrayList<>();
        Graph<V, E> notConnected = null;
        List<V> verticesWithEdges = null;

        if (algorithm == LayoutAlgorithms.AUTOMATIC) {
            verticesWithEdges = new ArrayList<>();
            for (E e : edges) { //find vertices that don't belong to any ofItems the graphs
                if (!verticesWithEdges.contains(e.getOrigin()))
                    verticesWithEdges.add(e.getOrigin());
                if (!verticesWithEdges.contains(e.getDestination()))
                    verticesWithEdges.add(e.getDestination());
            }
            if (verticesWithEdges.size() < vertices.size())
                notConnected = new Graph<>();
        }

        for (V v : vertices) {
            if (coveredVertices.contains(v))
                continue;
            if (notConnected != null && !verticesWithEdges.contains(v)) {
                notConnected.addVertex(v);
                continue;
            }

            Graph<V, E> graph = new Graph<>();
            formGraph(graph, v, coveredVertices, coveredEdges);
            graphs.add(graph);
        }

        if (notConnected != null)
            graphs.add(notConnected);

        return graphs;
    }

    @SuppressWarnings("unchecked")
    private void formGraph(Graph<V, E> graph, V v, List<V> coveredVertices, List<E> coveredEdges) {
        coveredVertices.add(v);
        graph.addVertex(v);

        for (E e : findAllEdgesContainigVertex(v)) {

            //avoid infinite recursion
            if (coveredEdges.contains(e))
                continue;

            coveredEdges.add(e);

            V origin = e.getOrigin();
            V desitnation = e.getDestination();

            if (graph.hasVertex(origin))
                graph.addVertex(origin);
            if (graph.hasVertex(desitnation))
                graph.addVertex(desitnation);

            graph.addEdge(e);

            //call formGraph with the other vertex as argument

            if (origin != v)
                formGraph(graph, origin, coveredVertices, coveredEdges);
            else if (desitnation != v)
                formGraph(graph, desitnation, coveredVertices, coveredEdges);
        }
    }


    private List<E> findAllEdgesContainigVertex(V v) {
        List<E> ret = new ArrayList<>();
        for (E e : edges)
            if (e.getOrigin() == v || e.getDestination() == v)
                ret.add(e);

        return ret;
    }

    /**
     * Lays out the graph and returns an instance ofItems the drawing object
     *
     * @return Laid out drawing (mapping ofItems vertices and edges to their calculated positions)
     * @throws CannotBeAppliedException If the specified layout algorithm cannot be applied
     */
    public Drawing<V, E> layout() throws CannotBeAppliedException {

        int startX = 200;
        int startY = 200;

        int spaceX = 200;
        int spaceY = 200;
        int numInRow = 4;
        int currentIndex = 1;

        int currentStartPositionX = startX;
        int currentStartPositionY = startY;

        int maxYInRow = 0;

        Drawing<V, E> ret = new Drawing<>();

        Drawing<V, E> drawing;

        AbstractLayouter<V, E> layouter = layouterFactory.createLayouter(algorithm);

        if (layouter.isOneGraph()) {
            try {
                drawing = layouter.layout(formOneGraph(vertices, edges), layoutProperties);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CannotBeAppliedException("Algorithm cannot be applied. " + ex.getMessage());
            }

            //if (!layouter.isPositionsEdges())
            drawing.positionEdges(edges);

            return drawing;
        }

        List<Graph<V, E>> graphs = formGraphs(vertices, edges);
        for (Graph<V, E> graph : graphs) {
            try {
                drawing = layouter.layout(graph, layoutProperties);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CannotBeAppliedException("Algorithm cannot be applied. " + ex.getMessage());
            }


            if (graphs.size() > 1) {


                int currentLeftmost = drawing.findLeftmostPosition();
                int currentTop = drawing.findTop();


                //leftmost should start at point currentStartPositionX
                int moveByX = currentStartPositionX - currentLeftmost;

                //top should start at point currentStartPositionY
                int moveByY = currentStartPositionY - currentTop;

                drawing.moveByIncludingEdges(moveByX, moveByY);

                int[] bounds = drawing.getBounds();
                if (bounds[1] > maxYInRow)
                    maxYInRow = bounds[1];

                currentStartPositionX += bounds[0] + spaceX;

                if (currentIndex % numInRow == 0) {
                    currentStartPositionY += maxYInRow + spaceY;
                    maxYInRow = 0;
                    currentStartPositionX = startX;
                }
            }


            ret.getVertexMappings().putAll(drawing.getVertexMappings());
            ret.getEdgeMappings().putAll(drawing.getEdgeMappings());

            currentIndex++;
        }

        //if (!layouter.isPositionsEdges())
        ret.positionEdges(edges);

        return ret;
    }
}




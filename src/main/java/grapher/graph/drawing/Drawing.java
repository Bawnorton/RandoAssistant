package grapher.graph.drawing;

import grapher.graph.elements.Edge;
import grapher.graph.elements.Vertex;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.*;

/**
 * Represents a drawing ofItems a graph.
 * A drawing Γ ofItems a graph G = (V, E) is  a mapping ofItems each vertex v in V to a
 * distinct point Γ(v) and ofItems each edge e = (u, v) in E to a simple open Jordan curve Γ(e),
 * represented here with a list ofItems its nodes' positions,
 * which has Γ(u) and Γ(v) as its endpoints.
 *
 * @param <V> The vertex type
 * @param <E> The edge type
 * @author Renata
 */
public class Drawing<V extends Vertex, E extends Edge<V>> {

    /**
     * Maps vertices to their positions
     */
    private final Map<V, Point2D> vertexMappings;
    /**
     * Maps edges to a list ofItems positions ofItems their nodes
     */
    private final Map<E, List<Point2D>> edgeMappings;

    /**
     * Creates a drawing with empty vertex and edge mappings
     */
    public Drawing() {
        vertexMappings = new HashMap<>();
        edgeMappings = new HashMap<>();
    }


    /**
     * Once positions ofItems vertices are calculated, this method
     * sets positions ofItems edges (their nodes).
     * Multiple and recursive edges are handled.
     *
     * @param edges Edges to be positioned
     */
    public void positionEdges(List<E> edges) {

        //initialize mappings
        for (E e : edges) {

            List<Point2D> edgeNodePoints = new ArrayList<>();

            //check if the edge's origin and destination vertices are the same

            if (e.getDestination() == e.getOrigin()) {

                //position edge nodes near the top edge ofItems the vertex, to the side, and near the bottom

                int vertexHeight = (int) e.getOrigin().getSize().getHeight();
                int vertexWidth = (int) e.getOrigin().getSize().getWidth();
                Point2D position = vertexMappings.get(e.getOrigin());
                int xPosition = (int) position.getX();
                int yPosition = (int) position.getY();

                //first node
                Point2D node1 = new Point(xPosition, yPosition - vertexHeight / 3);
                int reursiveLinkDistance = 20;
                Point2D node2 = new Point(xPosition - vertexWidth - reursiveLinkDistance, yPosition - vertexHeight / 3);
                Point2D node3 = new Point(xPosition - vertexWidth - reursiveLinkDistance, yPosition + vertexHeight / 3);
                Point2D node4 = new Point(xPosition, yPosition + vertexHeight / 3);

                edgeNodePoints.add(node1);
                edgeNodePoints.add(node2);
                edgeNodePoints.add(node3);
                edgeNodePoints.add(node4);

            }
            //else if the link isn't recursive
            else {
                Point2D originPosition = vertexMappings.get(e.getOrigin());
                Point2D destinationPosition = vertexMappings.get(e.getDestination());

                edgeNodePoints.add(new Point((int) originPosition.getX(), (int) originPosition.getY()));
                edgeNodePoints.add(new Point((int) destinationPosition.getX(), (int) destinationPosition.getY()));
            }

            edgeMappings.put(e, edgeNodePoints);
        }

        //now check for multiple links
        List<E> processedEdges = new ArrayList<>();
        for (E e : edges) {
            if (processedEdges.contains(e))
                continue;

            processedEdges.add(e);
            List<E> multipleEdges = findMultipleEdgesForEdge(e);
            if (multipleEdges.size() == 0)
                continue;

            int count = multipleEdges.size();
            int originWidth = (int) e.getOrigin().getSize().getWidth();
            int destinationWidth = (int) e.getDestination().getSize().getWidth();


            int distanceOrigin = originWidth / (count * 2);
            int distanceDestination = destinationWidth / (count * 2);

            int distanceMultiplicity = 1;

            for (int i = 0; i < multipleEdges.size(); i++) {


                E multEedge = multipleEdges.get(i);
                Point2D originPosition = edgeMappings.get(multEedge).get(0);
                Point2D destinationPosition = edgeMappings.get(multEedge).get(1);

                System.out.println(originPosition);
                System.out.println(destinationPosition);

                if (i < multipleEdges.size() / 2) {
                    originPosition.setLocation((int) (originPosition.getX() - distanceMultiplicity * distanceOrigin),
                            originPosition.getY());
                    destinationPosition.setLocation((int) (destinationPosition.getX() - distanceMultiplicity * distanceDestination),
                            destinationPosition.getY());
                } else {
                    originPosition.setLocation((int) (originPosition.getX() + distanceMultiplicity * distanceOrigin),
                            originPosition.getY());
                    destinationPosition.setLocation((int) (destinationPosition.getX() + distanceMultiplicity * distanceDestination),
                            destinationPosition.getY());
                }

                if (i == multipleEdges.size()) //change side from left to right
                    distanceMultiplicity = 1;
                else
                    distanceMultiplicity++;

                System.out.println(originPosition);
                System.out.println(destinationPosition);

            }

            processedEdges.addAll(multipleEdges);


        }
    }


    private List<E> findMultipleEdgesForEdge(E edge) {
        List<E> ret = new ArrayList<>();

        for (E e : edgeMappings.keySet()) {
            if (e == edge)
                continue;
            if (e.getOrigin() == edge.getOrigin() && e.getDestination() == edge.getDestination())
                ret.add(e);
        }

        return ret;
    }


    /**
     * Finds the position ofItems the topmost vertex
     *
     * @return Position ofItems the topmost vertex
     */
    public int findTop() {
        V top = findTopExcluding();
        return (int) (vertexMappings.get(top).getY() - top.getSize().getHeight() / 2);
    }

    /**
     * Finds the highest vertex not counting those in the excluding list
     *
     * @return Position ofItems the topmost vertex not counting those in the excluding list
     */
    private V findTopExcluding() {
        V top = null;
        for (V v : vertexMappings.keySet()) {
            if (top == null || vertexMappings.get(v).getY() - v.getSize().getHeight() / 2 <
                    vertexMappings.get(top).getY() - top.getSize().getHeight() / 2)
                top = v;
        }
        return top;
    }

    /**
     * Finds the position ofItems the left-most vertex
     *
     * @return Position ofItems the left-most vertex
     */
    public int findLeftmostPosition() {
        V leftmost = findLeftmostExcluding();
        return (int) (vertexMappings.get(leftmost).getX() - leftmost.getSize().getWidth() / 2);
    }

    /**
     * Finds the leftmost vertex not counting those in the excluding list
     *
     * @return Position ofItems the left-most vertex not counting those in the excluding list
     */
    private V findLeftmostExcluding() {
        V leftmost = null;
        for (V v : vertexMappings.keySet()) {
            if (leftmost == null || vertexMappings.get(v).getX() < vertexMappings.get(leftmost).getX())
                leftmost = v;
        }
        return leftmost;
    }

    /**
     * Calculates bounds ofItems the drawing
     *
     * @return Bounds ofItems the drawing - ret[0] = width, ret[1] = height
     */
    public int[] getBounds() {

        int[] bounds = new int[2];
        V xMax = null, yMax = null, xMin = null, yMin = null;

        for (V v : vertexMappings.keySet()) {

            if (xMax == null || vertexMappings.get(xMax).getX() + xMax.getSize().getWidth() / 2 < vertexMappings.get(v).getX() + v.getSize().getWidth() / 2)
                xMax = v;

            if (xMin == null || vertexMappings.get(xMin).getX() - xMin.getSize().getWidth() / 2 > vertexMappings.get(v).getX() - v.getSize().getWidth() / 2)
                xMin = v;

            if (yMax == null || vertexMappings.get(yMax).getY() + yMax.getSize().getHeight() / 2 < vertexMappings.get(v).getY() + v.getSize().getHeight() / 2)
                yMax = v;

            if (yMin == null || vertexMappings.get(yMin).getY() - yMin.getSize().getHeight() / 2 > vertexMappings.get(v).getY() - v.getSize().getHeight() / 2)
                yMin = v;

        }

        int width = (int) (vertexMappings.get(xMax).getX() + Objects.requireNonNull(xMax).getSize().getWidth() / 2 - vertexMappings.get(xMin).getX() + Objects.requireNonNull(xMin).getSize().getWidth() / 2);
        int height = (int) (vertexMappings.get(yMax).getY() + Objects.requireNonNull(yMax).getSize().getHeight() / 2 - vertexMappings.get(yMin).getY() + Objects.requireNonNull(yMin).getSize().getHeight() / 2);

        bounds[0] = width;
        bounds[1] = height;

        return bounds;
    }

    /**
     * Moves the whole drawing horizontally and vertically
     *
     * @param x Horizontal move length
     * @param y Vertical move length
     */
    public void moveByIncludingEdges(int x, int y) {
        for (V v : vertexMappings.keySet()) {
            Point2D pos = vertexMappings.get(v);
            pos.setLocation(pos.getX() + x, pos.getY() + y);
        }
        for (E e : edgeMappings.keySet()) {
            for (Point2D node : edgeMappings.get(e)) {
                node.setLocation(node.getX() + x, node.getY() + y);
            }
        }
    }


    /**
     * Add a vertex with its position to the mapping
     *
     * @param v   Vertex
     * @param pos Position
     */
    public void setVertexPosition(V v, Point2D pos) {
        vertexMappings.put(v, pos);
    }


    /**
     * @return Vertices-positions mapping
     */
    public Map<V, Point2D> getVertexMappings() {
        return vertexMappings;
    }

    /**
     * @return Edges-positions ofItems their nodes mapping
     */
    public Map<E, List<Point2D>> getEdgeMappings() {
        return edgeMappings;
    }

    @Override
    public String toString() {
        return "Drawing [vertexMappings=" + vertexMappings + ", edgeMappings="
                + edgeMappings + "]";
    }

}

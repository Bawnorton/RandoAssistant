package grapher.graph.layout.organic;

import grapher.graph.elements.Edge;
import grapher.graph.elements.Vertex;
import grapher.graph.layout.AbstractJGraphXLayouter;
import grapher.graph.layout.GraphLayoutProperties;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import grapher.graph.layout.PropertyEnums;

/**
 * Layouter which uses JGraphX's hierarchical layout
 * @author Renata
 * @param <V> The vertex type
 * @param <E> The edge type 
 */
public class JGraphHierarchicalLayouter<V extends Vertex, E extends Edge<V>> extends AbstractJGraphXLayouter<V, E> {

	public JGraphHierarchicalLayouter(){
		positionsEdges = true;
	}

	@Override
	protected void initLayouter(GraphLayoutProperties layoutProperties) {
		mxHierarchicalLayout hierarchicalLayouter = new mxHierarchicalLayout(jGraphXGraph);

		if (layoutProperties != null){
			Object resizeParent = layoutProperties.getProperty(PropertyEnums.HierarchicalProperties.RESIZE_PARENT);
			if (resizeParent != null){
				hierarchicalLayouter.setResizeParent((boolean) resizeParent);

				if ((boolean) resizeParent){
					Object moveParent = layoutProperties.getProperty(PropertyEnums.HierarchicalProperties.MOVE_PARENT);
					Object parentBorder = layoutProperties.getProperty(PropertyEnums.HierarchicalProperties.PARENT_BORDER);
					if (moveParent != null)
						hierarchicalLayouter.setMoveParent((boolean) moveParent);
					if (parentBorder != null)
						hierarchicalLayouter.setParentBorder((int) parentBorder);
				}
			}

			Object intraCellSpacing =layoutProperties.getProperty(PropertyEnums.HierarchicalProperties.INTRA_CELL_SPACING);
			if (intraCellSpacing != null)
				hierarchicalLayouter.setIntraCellSpacing((double) intraCellSpacing);

			Object interRankCellSpacing = layoutProperties.getProperty(PropertyEnums.HierarchicalProperties.INTER_RANK_CELL_SPACING);
			if (interRankCellSpacing != null)
				hierarchicalLayouter.setInterRankCellSpacing((double) interRankCellSpacing);

			Object parentBorder = layoutProperties.getProperty(PropertyEnums.HierarchicalProperties.INTER_HIERARCHY_SPACING);
			if (parentBorder != null)
				hierarchicalLayouter.setParentBorder(((Double)parentBorder).intValue());

			Object interHierarchySpacing = layoutProperties.getProperty(PropertyEnums.HierarchicalProperties.PARALLEL_EDGE_SPACING);
			if (interHierarchySpacing != null)
				hierarchicalLayouter.setInterHierarchySpacing((double) interHierarchySpacing);

			Object orientation = layoutProperties.getProperty(PropertyEnums.HierarchicalProperties.ORIENTATION);
			if (orientation != null)
				hierarchicalLayouter.setOrientation((int) orientation);

			Object fineTuning = layoutProperties.getProperty(PropertyEnums.HierarchicalProperties.FINE_TUNING);
			if (fineTuning != null)
				hierarchicalLayouter.setFineTuning((boolean)fineTuning);
		}
		
		layouter = hierarchicalLayouter;

	}

}

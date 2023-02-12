package grapher.graph.layout;

/**
 * List of all properties for each implemented layout algorithm
 * Each enum contains values used for generic implementation
 * of the user interface
 * @author Renata
 */
public interface PropertyEnums {


	/**
	 * All configurable properties of the hierarchical layout algorithm
	 * @author Renata
	 */
	enum HierarchicalProperties implements PropertyEnums{
		RESIZE_PARENT(),
		MOVE_PARENT(),
		PARENT_BORDER(),
		INTRA_CELL_SPACING(),
		INTER_RANK_CELL_SPACING(),
		INTER_HIERARCHY_SPACING(),
		PARALLEL_EDGE_SPACING(),
		ORIENTATION(),
		FINE_TUNING();

		HierarchicalProperties (){
		}

	}
}

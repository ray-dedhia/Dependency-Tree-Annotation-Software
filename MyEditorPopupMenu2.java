package dt;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import com.mxgraph.examples.swing.editor.BasicGraphEditor;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxResources;
import dt.MyEditorActions2.*;

/**Latest version.*/
public class MyEditorPopupMenu2 extends JPopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

	public MyEditorPopupMenu2(BasicGraphEditor editor, DTE9 dte) {
		boolean canDel = false, canSplit, areCells = false, group = false;
		
		/** Checks conditions that are used to determine whether or not to enable certain options **/
		
		// If there are cells, set are cells to true
		if (editor.getGraphComponent().getGraph().getModel().getChildCount(editor.getGraphComponent().getGraph().getDefaultParent())>0) {
			areCells = true;
		}
		
		// Holds the value of the first selected cell
		Object selCell = editor.getGraphComponent().getGraph().getSelectionCell();
		
		// If no cells are selected, the split option should be disabled
		if (selCell == null) {
			canSplit = false;
		// Otherwise, if the cell's parent is equal to the default parent (the cell is not a child of another cell, which would indicate that
		// it is a word part in a split word, and thus cannot be split) the split option should be disabled. Otherwise, the split option
		// should be enabled.
		} else {
			canSplit = (((mxCell) selCell).getParent() == editor.getGraphComponent().getGraph().getDefaultParent());
			// if the child count of the cell is greater than 0, it is a split word (known as a group, because it is a container for other cells, which represent
			// the word parts)
			if (((mxCell)selCell).getChildCount()>0) {
				group = true;
			}
		}
		// If at least one cell is selected, and allow edge deletion is true, and all selected cells are edges that are not within a split word 
		// the option for deleting edges should be enabled.
		if (((MyMxGraphComponent2) editor.getGraphComponent()).mAllowEdgeDeletion.getValue()) {
			if (selCell != null) {
				canDel = true;
				for (Object sel : editor.getGraphComponent().getGraph().getSelectionCells()) {
					// If one selected cell is not an edge or is a child
					if (!((mxCell) sel).isEdge() || (((mxCell) sel).getParent() != editor.getGraphComponent().getGraph().getDefaultParent())) {
						canDel = false;
						break;
					}
				}
			}
		} 
		boolean oneNode = false, oneEdge = false;
		
		// If one and only one cell is selected
		if (editor.getGraphComponent().getGraph().getSelectionCells().length == 1) {
			// and if that cell is a vertex, oneNode is set to true
			if (((mxCell) selCell).isVertex() && ((mxCell) selCell).getChildCount()==0)
				oneNode = true;
			// else if that cell is an edge, oneEdge is set to true
			else if (((mxCell) selCell).isEdge() && ((mxCell) selCell).getParent() == editor.getGraphComponent().getGraph().getDefaultParent())
				oneEdge = true;
		}

		add(editor.bind(mxResources.get("delete"), new DeleteSelectedEdgesAction(), "/com/mxgraph/examples/swing/images/delete.gif")).setEnabled(canDel);

		addSeparator();

		add(editor.bind(mxResources.get("selectVertices"), mxGraphActions.getSelectVerticesAction())).setEnabled(areCells);
		add(editor.bind(mxResources.get("selectEdges"), mxGraphActions.getSelectEdgesAction())).setEnabled(areCells);
		add(editor.bind(mxResources.get("selectAll"), mxGraphActions.getSelectAllAction())).setEnabled(areCells);
		
		addSeparator();
		
		JMenu setPT = (JMenu) add(new JMenu("Set POS Tag"));
		setPT.setEnabled(oneNode);
		for (String s : dte.graphComponent.mPOSTags) {
			setPT.add(editor.bind(s, new SetPOSTagAction(s, selCell)));
		}
		
		JMenu setAT = (JMenu) add(new JMenu("Set Arrow Tag"));
		setAT.setEnabled(oneEdge);
		for (String s : dte.graphComponent.mArrowLabels) {
			setAT.add(editor.bind(s, new SetArrowTagAction(s, selCell)));
		}
		
		addSeparator();
		
		add(editor.bind("Split Word", new SplitWordAction())).setEnabled(oneNode && canSplit);
		add(editor.bind("Unsplit Word", new UnsplitWordAction())).setEnabled(group);
		
		addSeparator();
		
		add(editor.bind("Format", new FormatAction())).setEnabled(areCells);

		
	}

}
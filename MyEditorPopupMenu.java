package dt;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import com.mxgraph.examples.swing.editor.BasicGraphEditor;
import com.mxgraph.examples.swing.editor.EditorActions.HistoryAction;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxResources;
import dt.MyEditorActions.*;

public class MyEditorPopupMenu extends JPopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

	public MyEditorPopupMenu(BasicGraphEditor editor, DTE8 dte) {
		boolean canDel = false, canSplit, areCells = false, group = false;
		if (editor.getGraphComponent().getGraph().getModel().getChildCount(editor.getGraphComponent().getGraph().getDefaultParent())>0) {
			areCells = true;
		}
		Object selCell = editor.getGraphComponent().getGraph().getSelectionCell();
		if (selCell == null) {
			canSplit = false;	
		} else {
			canSplit = (((mxCell) selCell).getParent() == editor.getGraphComponent().getGraph().getDefaultParent());	
			if (((mxCell)selCell).getChildCount()>0) {
				group = true;
			}
		}
		if (((MymxGraphComponent) editor.getGraphComponent()).mAllowEdgeDeletion.getValue()) {
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
		
		if (editor.getGraphComponent().getGraph().getSelectionCells().length == 1) {
			if (((mxCell) selCell).isVertex() && ((mxCell) selCell).getChildCount()==0)
				oneNode = true;
			else if (((mxCell) selCell).isEdge() && ((mxCell) selCell).getParent() == editor.getGraphComponent().getGraph().getDefaultParent())
				oneEdge = true;
		}

		add(editor.bind(mxResources.get("undo"), new HistoryAction(true), "/com/mxgraph/examples/swing/images/undo.gif"));
		add(editor.bind(mxResources.get("redo"), new HistoryAction(false), "/com/mxgraph/examples/swing/images/redo.gif"));

		addSeparator();

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
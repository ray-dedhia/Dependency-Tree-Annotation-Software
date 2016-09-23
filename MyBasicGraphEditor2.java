package dt;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.mxgraph.examples.swing.editor.BasicGraphEditor;
import com.mxgraph.examples.swing.editor.EditorToolBar;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;

/**Latest version.*/
public class MyBasicGraphEditor2 extends BasicGraphEditor {

	private static final long serialVersionUID = -4794248771386982058L;
	/** Stores the index of MyEditorToolBar. If equal to -1, the toolbar has not been installed yet.*/
	public int indexOfEditorToolBar = -1;
	/** Maps mxGraphs to mxUndoMangers so that when the graph is changed, the current mxUndoManger can be set
	 * to the undo manager associated with that graph.
	 */
	public HashMap<mxGraph, mxUndoManager> undomanagers = new HashMap<>();
	private DTE9 dte;
	
	public MyBasicGraphEditor2(final mxGraphComponent component, DTE9 d) {
		super("DTE", component);
		dte = d;
	}

	/**
	 * mxRubberband is the blue selection box that comes up when the mouse is clicked and dragged within the graph. 
	 * It starts off disabled because "Drag Mouse to Move Nodes" is initially set to true (see MyEditorMenuBar2 => Options).
	 * When "Drag Mouse to Move Nodes" is true, the blue selection box shouldn't appear, and when the blue selection box is 
	 * supposed to appear, the nodes shouldn't move when the mouse is dragged, so that the user is able to use the selection box
	 * to easily select cells.
	 */
	@Override
	protected void installHandlers() {
		rubberband = new mxRubberband(graphComponent);
		rubberband.setEnabled(false);
		keyboardHandler = new MyKeyboardHandler(graphComponent);
	}
	
	/**
	 * Used in MyEditorMenuBar2 when "Drag Mouse to Move Nodes" is set to true (in which case
	 * the parameter enabled is equal to false) and when "Drag Mouse to Move Nodes" is set to false
	 * (in which case the parameter enabled is equal to true).
	 */
	public void setRubberBandEnabled(boolean enabled) {
		rubberband.setEnabled(enabled);
	}
	
	/** Overrided so that it uses the customized keyboard handler MyKeyboardHandler. */
	@Override
	public void setLookAndFeel(String clazz) {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null) {
			try {
				UIManager.setLookAndFeel(clazz);
				SwingUtilities.updateComponentTreeUI(frame);

				// Needs to assign the key bindings again
				keyboardHandler = new MyKeyboardHandler(graphComponent);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/** Overrided so that it uses the customized pop-up menu MyEditorPopupMenu2*/
	@Override
	protected void showGraphPopupMenu(MouseEvent e)
	{
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),
				graphComponent);
		MyEditorPopupMenu2 menu = new MyEditorPopupMenu2(MyBasicGraphEditor2.this, dte);
		menu.show(graphComponent, pt.x, pt.y);

		e.consume();
	}
	
	/** 
	 * Stores the index of the editor tool bar so that it can be removed and a new one can be installed 
	 * when the user switches between sentences. As a result of this, each sentence's graph has its
	 * own undoManager that allows the user to undo and redo events specific to each graph.
	 */
	@Override
	protected void installToolBar() {
		MyEditorToolBar etb = new MyEditorToolBar(this, JToolBar.HORIZONTAL, dte);
		add(etb, BorderLayout.NORTH);
		for (int i = 0; i < getComponentCount(); i++) {
			if (getComponent(i) == etb) {
				System.out.println("Index of editor tool bar is: " + i);
				indexOfEditorToolBar = i;
			}
		}
	}
	
	/** If undomanagers contains the graph, get the mxUndoManager associated with that graph. Otherwise, create a new mxUndoManger and map it
	 * to the graph in undomanagers. Then install a new toolbar that uses the new mxUndoManager.
	 */
	protected void installNewToolBar(final mxGraphComponent graphComponent) {
		System.out.println("installNewToolBar called");
		
		if (undomanagers.containsKey(graphComponent.getGraph())) {
			undoManager = undomanagers.get(graphComponent.getGraph());
		} else {
			undoManager = new mxUndoManager();

			graphComponent.getGraph().getModel().addListener(mxEvent.CHANGE, changeTracker);
	
			// Adds the command history to the model and view
			graphComponent.getGraph().getModel().addListener(mxEvent.UNDO, undoHandler);
			graphComponent.getGraph().getView().addListener(mxEvent.UNDO, undoHandler);
	
			// Keeps the selection in sync with the command history
			mxIEventListener undoHandler = new mxIEventListener() {
				public void invoke(Object source, mxEventObject evt) {
					List<mxUndoableChange> changes = ((mxUndoableEdit) evt.getProperty("edit")).getChanges();
					graphComponent.getGraph().setSelectionCells(graphComponent.getGraph().getSelectionCellsForChanges(changes));
				}
			};
	
			undoManager.addListener(mxEvent.UNDO, undoHandler);
			undoManager.addListener(mxEvent.REDO, undoHandler);
			
			undomanagers.put(graphComponent.getGraph(), undoManager);
		}

		if (indexOfEditorToolBar != -1) {
			remove(indexOfEditorToolBar);
			installToolBar();
		}
	}

}

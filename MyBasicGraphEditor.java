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
import com.mxgraph.examples.swing.editor.EditorPopupMenu;
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

public class MyBasicGraphEditor extends BasicGraphEditor {

	private static final long serialVersionUID = -4794248771386982058L;
	public int indexOfEditorToolBar = -1;
	public HashMap<mxGraph, mxUndoManager> undomanagers = new HashMap<>();
	private DTE8 dte;
	
	public MyBasicGraphEditor(final mxGraphComponent component, DTE8 d) {
		super("DTE", component);
		dte = d;
	}

	@Override
	protected void installHandlers() {
		rubberband = new mxRubberband(graphComponent);
		keyboardHandler = new MyKeyboardHandler(graphComponent);
	}

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
	
	@Override
	protected void showGraphPopupMenu(MouseEvent e)
	{
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),
				graphComponent);
		MyEditorPopupMenu menu = new MyEditorPopupMenu(MyBasicGraphEditor.this, dte);
		menu.show(graphComponent, pt.x, pt.y);

		e.consume();
	}

	protected void installToolBar() {
		EditorToolBar etb = new EditorToolBar(this, JToolBar.HORIZONTAL);
		add(etb, BorderLayout.NORTH);
		for (int i = 0; i < getComponentCount(); i++) {
			if (getComponent(i) == etb) {
				System.out.println("Index of editor tool bar is: " + i);
				indexOfEditorToolBar = i;
			}
		}
	}

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

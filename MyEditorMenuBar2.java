package dt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.examples.swing.editor.BasicGraphEditor;
import com.mxgraph.examples.swing.editor.EditorActions.ColorAction;
import com.mxgraph.examples.swing.editor.EditorActions.ExitAction;
import com.mxgraph.examples.swing.editor.EditorActions.KeyValueAction;
import com.mxgraph.examples.swing.editor.EditorActions.OpenAction;
import com.mxgraph.examples.swing.editor.EditorActions.PrintAction;
import com.mxgraph.examples.swing.editor.EditorActions.PromptValueAction;
import com.mxgraph.examples.swing.editor.EditorActions.SaveAction;
import com.mxgraph.examples.swing.editor.EditorActions.ScaleAction;
import com.mxgraph.examples.swing.editor.EditorActions.SetLabelPositionAction;
import com.mxgraph.examples.swing.editor.EditorActions.ToggleOutlineItem;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxResources;
import javax.swing.JCheckBoxMenuItem;
import dt.MyEditorActions2.*;

/**Latest version.*/
public class MyEditorMenuBar2 extends JMenuBar {
	private static final long serialVersionUID = 4060203894740766714L;

	public MyEditorMenuBar2(final BasicGraphEditor editor, final DTE9 dte) {
		final mxGraphComponent graphComponent = editor.getGraphComponent();
		graphComponent.getGraph();
		new mxAnalysisGraph();

		new MyEditorActions2(dte);

		JMenu menu = null;
		JMenu submenu = null;

		// Creates the file menu
		menu = add(new JMenu(mxResources.get("file")));

		menu.add(editor.bind(mxResources.get("openFile"), new OpenAction(), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Open .conll File", new OpenConllAction(), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Open .sconll File", new OpenSpecialConllAction(), "/com/mxgraph/examples/swing/images/open.gif"));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("save"), new SaveAction(false), "/com/mxgraph/examples/swing/images/save.gif"));
		menu.add(editor.bind(mxResources.get("saveAs"), new SaveAction(true), "/com/mxgraph/examples/swing/images/saveas.gif"));
		menu.add(editor.bind("Save As .conll File", new SaveAsConllAction(), "/com/mxgraph/examples/swing/images/saveas.gif"));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("print"), new PrintAction(), "/com/mxgraph/examples/swing/images/print.gif"));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("exit"), new ExitAction()));

		// Creates the edit menu
		menu = add(new JMenu(mxResources.get("edit")));

		menu.add(editor.bind(mxResources.get("delete"), new DeleteSelectedEdgesAction(), "/com/mxgraph/examples/swing/images/delete.gif"));

		menu.addSeparator();

		menu.add(editor.bind("Split Word", new SplitWordAction()));
		menu.add(editor.bind("Unsplit Word", new UnsplitWordAction()));

		menu.addSeparator();

		menu.add(editor.bind("Add Arrow Tags With File", new AddTagsAction(true, true), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Set Arrow Tags With File", new AddTagsAction(false, true), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Type New Arrow Tag", new TypeTagAction(true)));

		menu.addSeparator();

		menu.add(editor.bind("Add POS Tags With File", new AddTagsAction(true, false), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Set POS Tags With File", new AddTagsAction(false, false), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Type New POS Tag", new TypeTagAction(false)));

		menu.addSeparator();

		menu.add(editor.bind("Add Sentences With File", new AddSentencesAction(), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Type New Sentence", new TypeSentenceAction()));

		menu.addSeparator();

		menu.add(editor.bind("Format", new FormatAction()));

		// Creates the options menu
		menu = add(new JMenu("Options"));

		menu.add(editor.bind("Settings", new SettingsAction()));
		menu.add(editor.bind("Increase Distance Between Nodes", new ChangeDistanceAction("incr")));
		menu.add(editor.bind("Decrease Distance Between Nodes", new ChangeDistanceAction("decr")));
		menu.add(editor.bind("Set Distance Between Nodes", new ChangeDistanceAction("set")));

		menu.addSeparator();

		final JCheckBoxMenuItem autoFormat = new JCheckBoxMenuItem("Automatically Format", false);
		autoFormat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.autoFormat.setValue(autoFormat.getState());
			}
		});
		menu.add(autoFormat);
		// //
		final JCheckBoxMenuItem moveCellsOnDrag = new JCheckBoxMenuItem("Move Cells on Mouse Drag", true);
		moveCellsOnDrag.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.graphComponent.mMoveCellsOnDrag.setValue(moveCellsOnDrag.getState());
				dte.getBasicGraphEditor().setRubberBandEnabled(!moveCellsOnDrag.getState());
			}
		});
		menu.add(moveCellsOnDrag);
		// //
		final JCheckBoxMenuItem conOnOverlap = new JCheckBoxMenuItem("Connect On Overlap", true);
		conOnOverlap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.conOnOverlap.setValue(conOnOverlap.getState());
			}
		});
		menu.add(conOnOverlap);
		// //
		final JCheckBoxMenuItem dragAndDrop = new JCheckBoxMenuItem("Drag and Drop Arrows", true);
		dragAndDrop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.graphComponent.setConnectable(dragAndDrop.getState());
			}
		});
		menu.add(dragAndDrop);
		// //
		final JCheckBoxMenuItem allowEdgeMovement = new JCheckBoxMenuItem("Delete Arrows by Pulling off of Tree", true);
		allowEdgeMovement.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.allowEdgeMovement.setValue(allowEdgeMovement.getState());
				dte.graphComponent.getGraph().setCellsDisconnectable(allowEdgeMovement.getState());
			}
		});
		menu.add(allowEdgeMovement);
		// //
		final JCheckBoxMenuItem allowEdgeDeletion = new JCheckBoxMenuItem("Allow Arrow Deletion", true);
		allowEdgeDeletion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.graphComponent.mAllowEdgeDeletion.setValue(allowEdgeDeletion.getState());
				if (!allowEdgeDeletion.getState()) {
					allowEdgeMovement.setState(false);
					dte.allowEdgeMovement.setValue(false);
					dte.graphComponent.getGraph().setCellsDisconnectable(false);
				}
				allowEdgeMovement.setEnabled(allowEdgeDeletion.getState());
			}
		});
		menu.add(allowEdgeDeletion);
		// //
		final JCheckBoxMenuItem formatRAA = new JCheckBoxMenuItem("Format With Right Angle Arrows", true);
		formatRAA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.rightAngleArrows.setValue(formatRAA.getState());
				dte.graphComponent.refresh();
				dte.graphComponent.formatAsNeeded();
			}
		});
		menu.add(formatRAA);
		// //
		final JCheckBoxMenuItem doubleClick = new JCheckBoxMenuItem("Double Click To Expand/Collapse Node", true);
		doubleClick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.graphComponent.mDoubleClick.setValue(doubleClick.getState());
			}
		});
		menu.add(doubleClick);
		final JCheckBoxMenuItem plusMinus = new JCheckBoxMenuItem("Click Plus/Minus Icon To Expand/Collapse Node", true);
		plusMinus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.graphComponent.mPlusMinus.setValue(plusMinus.getState());
				dte.graphComponent.refresh();
			}
		});
		menu.add(plusMinus);

		// Creates the view menu
		menu = add(new JMenu(mxResources.get("view")));

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("zoom")));

		submenu.add(editor.bind("400%", new ScaleAction(4)));
		submenu.add(editor.bind("200%", new ScaleAction(2)));
		submenu.add(editor.bind("150%", new ScaleAction(1.5)));
		submenu.add(editor.bind("100%", new ScaleAction(1)));
		submenu.add(editor.bind("75%", new ScaleAction(0.75)));
		submenu.add(editor.bind("50%", new ScaleAction(0.5)));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("custom"), new ScaleAction(0)));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("zoomIn"), mxGraphActions.getZoomInAction()));
		menu.add(editor.bind(mxResources.get("zoomOut"), mxGraphActions.getZoomOutAction()));

		menu.addSeparator();

		menu.add(new ToggleOutlineItem(editor, mxResources.get("outline")));

		// Creates the shape menu
		menu = add(new JMenu(mxResources.get("shape")));
		menu.add(editor.bind(mxResources.get("toBack"), mxGraphActions.getToBackAction(), "/com/mxgraph/examples/swing/images/toback.gif"));
		menu.add(editor.bind(mxResources.get("toFront"), mxGraphActions.getToFrontAction(), "/com/mxgraph/examples/swing/images/tofront.gif"));

		// Creates the format menu
		menu = add(new JMenu(mxResources.get("format")));
		populateFormatMenu(menu, editor);

		// Creates the help menu
		menu = add(new JMenu(mxResources.get("help")));

		JMenuItem item = menu.add(new JMenuItem(mxResources.get("aboutGraphEditor")));
		item.addActionListener(new ActionListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				editor.about();
			}
		});
	}

	/**
	 * Adds menu items to the given format menu
	 */
	public static void populateFormatMenu(JMenu menu, BasicGraphEditor editor) {
		JMenu submenu;

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("label")));

		submenu.add(editor.bind(mxResources.get("fontcolor"), new ColorAction("Fontcolor", mxConstants.STYLE_FONTCOLOR), "/com/mxgraph/examples/swing/images/fontcolor.gif"));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("labelFill"), new ColorAction("Label Fill", mxConstants.STYLE_LABEL_BACKGROUNDCOLOR)));

		submenu.addSeparator();

		JMenu subsubmenu = (JMenu) submenu.add(new JMenu(mxResources.get("position")));

		subsubmenu.add(editor.bind(mxResources.get("top"), new SetLabelPositionAction(mxConstants.ALIGN_TOP, mxConstants.ALIGN_BOTTOM)));
		subsubmenu.add(editor.bind(mxResources.get("middle"), new SetLabelPositionAction(mxConstants.ALIGN_MIDDLE, mxConstants.ALIGN_MIDDLE)));
		subsubmenu.add(editor.bind(mxResources.get("bottom"), new SetLabelPositionAction(mxConstants.ALIGN_BOTTOM, mxConstants.ALIGN_TOP)));

		subsubmenu.addSeparator();

		subsubmenu.add(editor.bind(mxResources.get("left"), new SetLabelPositionAction(mxConstants.ALIGN_LEFT, mxConstants.ALIGN_RIGHT)));
		subsubmenu.add(editor.bind(mxResources.get("center"), new SetLabelPositionAction(mxConstants.ALIGN_CENTER, mxConstants.ALIGN_CENTER)));
		subsubmenu.add(editor.bind(mxResources.get("right"), new SetLabelPositionAction(mxConstants.ALIGN_RIGHT, mxConstants.ALIGN_LEFT)));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("wordWrap"), new KeyValueAction(mxConstants.STYLE_WHITE_SPACE, "wrap")));
		submenu.add(editor.bind(mxResources.get("noWordWrap"), new KeyValueAction(mxConstants.STYLE_WHITE_SPACE, null)));

		submenu.addSeparator();

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("line")));

		submenu.add(editor.bind(mxResources.get("linecolor"), new ColorAction("Linecolor", mxConstants.STYLE_STROKECOLOR), "/com/mxgraph/examples/swing/images/linecolor.gif"));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("linewidth"), new PromptValueAction(mxConstants.STYLE_STROKEWIDTH, "Linewidth")));

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("lineend")));

		submenu.add(editor.bind(mxResources.get("open"), new KeyValueAction(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN), "/com/mxgraph/examples/swing/images/open_end.gif"));
		submenu.add(editor.bind(mxResources.get("classic"), new KeyValueAction(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC), "/com/mxgraph/examples/swing/images/classic_end.gif"));
		submenu.add(editor.bind(mxResources.get("block"), new KeyValueAction(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_BLOCK), "/com/mxgraph/examples/swing/images/block_end.gif"));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("size"), new PromptValueAction(mxConstants.STYLE_ENDSIZE, "Lineend Size")));

		menu.addSeparator();

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("alignment")));

		submenu.add(editor.bind(mxResources.get("left"), new KeyValueAction(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_RIGHT), "/com/mxgraph/examples/swing/images/left.gif"));
		submenu.add(editor.bind(mxResources.get("center"), new KeyValueAction(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER), "/com/mxgraph/examples/swing/images/center.gif"));
		submenu.add(editor.bind(mxResources.get("right"), new KeyValueAction(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_LEFT), "/com/mxgraph/examples/swing/images/right.gif"));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("top"), new KeyValueAction(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_BOTTOM), "/com/mxgraph/examples/swing/images/top.gif"));
		submenu.add(editor.bind(mxResources.get("middle"), new KeyValueAction(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE), "/com/mxgraph/examples/swing/images/middle.gif"));
		submenu.add(editor.bind(mxResources.get("bottom"), new KeyValueAction(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP), "/com/mxgraph/examples/swing/images/bottom.gif"));

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("spacing")));

		submenu.add(editor.bind(mxResources.get("top"), new PromptValueAction(mxConstants.STYLE_SPACING_TOP, "Top Spacing")));
		submenu.add(editor.bind(mxResources.get("right"), new PromptValueAction(mxConstants.STYLE_SPACING_RIGHT, "Right Spacing")));
		submenu.add(editor.bind(mxResources.get("bottom"), new PromptValueAction(mxConstants.STYLE_SPACING_BOTTOM, "Bottom Spacing")));
		submenu.add(editor.bind(mxResources.get("left"), new PromptValueAction(mxConstants.STYLE_SPACING_LEFT, "Left Spacing")));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("global"), new PromptValueAction(mxConstants.STYLE_SPACING, "Spacing")));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("sourceSpacing"), new PromptValueAction(mxConstants.STYLE_SOURCE_PERIMETER_SPACING, mxResources.get("sourceSpacing"))));
		submenu.add(editor.bind(mxResources.get("targetSpacing"), new PromptValueAction(mxConstants.STYLE_TARGET_PERIMETER_SPACING, mxResources.get("targetSpacing"))));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("perimeter"), new PromptValueAction(mxConstants.STYLE_PERIMETER_SPACING, "Perimeter Spacing")));
	}

};
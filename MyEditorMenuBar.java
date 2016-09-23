package dt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import com.mxgraph.examples.swing.editor.BasicGraphEditor;
import com.mxgraph.examples.swing.editor.EditorMenuBar;
import com.mxgraph.model.mxCell;

import dt.MyEditorActions.*;

@SuppressWarnings("serial")
public class MyEditorMenuBar extends EditorMenuBar {

	public MyEditorMenuBar(final BasicGraphEditor editor, final DTE8 dte) {
		super(editor);
				
		new MyEditorActions(dte);
		JMenu menu = null;

		// Creates the file menu
		menu = add(new JMenu("Special File"));
		menu.add(editor.bind("Open .conll File", new OpenConllAction(), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Open .sconll File", new OpenSpecialConllAction(), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Save As .conll File", new SaveAsConllAction(), "/com/mxgraph/examples/swing/images/saveas.gif"));
		
		menu.addSeparator();
		
		menu.add(editor.bind("Add Arrow Tags With File", new AddTagsAction(true, true), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Set Arrow Tags With File", new AddTagsAction(false, true), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Type In New Arrow Tag", new TypeTagAction(true)));
		
		menu.addSeparator();
		
		menu.add(editor.bind("Add POS Tags With File", new AddTagsAction(true, false), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Set POS Tags With File", new AddTagsAction(false, false), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Type In New POS Tag", new TypeTagAction(false)));
		
		menu.addSeparator();
		
		menu.add(editor.bind("Add Sentences With File", new AddSentencesAction(), "/com/mxgraph/examples/swing/images/open.gif"));
		menu.add(editor.bind("Type In New Sentence", new TypeSentenceAction()));

		// Creates the edit menu
		menu = add(new JMenu("Special Editing"));
		menu.add(editor.bind("Settings", new SettingsAction()));
		menu.add(editor.bind("Format", new FormatAction()));
		menu.add(editor.bind("Split Word", new SplitWordAction()));
		menu.add(editor.bind("Unsplit Word", new UnsplitWordAction()));
		
		menu.addSeparator();
		/**/
		final JCheckBoxMenuItem autoFormat = new JCheckBoxMenuItem("Automatically Format", false);
		autoFormat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.autoFormat.setValue(autoFormat.getState());
			}
		});
		menu.add(autoFormat);
		/**/
		final JCheckBoxMenuItem conOnOverlap = new JCheckBoxMenuItem("Connect On Overlap", true);
		conOnOverlap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.conOnOverlap.setValue(conOnOverlap.getState());
			}
		});
		menu.add(conOnOverlap);
		/**/
		final JCheckBoxMenuItem dragAndDrop = new JCheckBoxMenuItem("Drag and Drop Arrows", true);
		dragAndDrop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.graphComponent.setConnectable(dragAndDrop.getState());
			}
		});
		menu.add(dragAndDrop);
		/**/
		final JCheckBoxMenuItem allowEdgeMovement = new JCheckBoxMenuItem("Delete Edges by Pulling Off of Tree", true);
		allowEdgeMovement.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.allowEdgeMovement.setValue(allowEdgeMovement.getState());
				dte.graphComponent.getGraph().setAllowDanglingEdges(allowEdgeMovement.getState());
			}
		});
		menu.add(allowEdgeMovement);
		/**/
		final JCheckBoxMenuItem allowEdgeDeletion = new JCheckBoxMenuItem("Allow Arrow Deletion", true);
		allowEdgeDeletion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.graphComponent.mAllowEdgeDeletion.setValue(allowEdgeDeletion.getState());
			}
		});
		menu.add(allowEdgeDeletion);
		/**/
		final JCheckBoxMenuItem formatRAA = new JCheckBoxMenuItem("Format With Right Angle Arrows", true);
		formatRAA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.rightAngleArrows.setValue(formatRAA.getState());
			}
		});
		menu.add(formatRAA);
		/**/
		final JCheckBoxMenuItem allowCE = new JCheckBoxMenuItem("Allow Node Collapsing and Expanding", true);
		allowCE.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dte.graphComponent.mAllowCollapsingAndExpanding.setValue(allowCE.getState());
			}
		});
		menu.add(allowCE);				
	}
}

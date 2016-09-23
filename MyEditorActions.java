package dt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mxgraph.examples.swing.editor.EditorActions;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

@SuppressWarnings("serial")
public class MyEditorActions extends EditorActions {
	public static DTE8 dte;

	public MyEditorActions(DTE8 d) {
		dte = d;
	}

	public static class OpenConllAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked open .conll file");
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter conllfilter = new FileNameExtensionFilter("conll files (*.conll)", "conll");
			fileChooser.setFileFilter(conllfilter);
			fileChooser.setDialogTitle("Open .conll File");
			fileChooser.setFileFilter(conllfilter);
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				String filename = fileChooser.getSelectedFile().getAbsolutePath();
				System.out.println("Opening .conll file: " + filename);
				dte.openConllFile(filename);
			}
		}
	}
	
	public static class OpenSpecialConllAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked open .sconll file");
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter conllfilter = new FileNameExtensionFilter("sconll files (*.sconll)", "sconll");
			fileChooser.setFileFilter(conllfilter);
			fileChooser.setDialogTitle("Open .sconll File");
			fileChooser.setFileFilter(conllfilter);
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				String filename = fileChooser.getSelectedFile().getAbsolutePath();
				System.out.println("Opening .sconll file: " + filename);
				dte.specialConll.setValue(true);
				dte.openConllFile(filename);
			}
		}
	}

	public static class SaveAsConllAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked save as .conll file");
			int reply = 0;
			if (dte.specialConll.getValue())
				reply = JOptionPane.showConfirmDialog(dte.graphComponent, "Since you split a word, this file cannot be saved as a standard conll file (.conll). \nDo you want to save it as a special conll file (.sconll)?", "Cannot Save As .conll File", JOptionPane.YES_NO_OPTION);
		    if (reply == JOptionPane.YES_OPTION) {
		    	if (dte.cwOfVerts.size() > 0) {
		    		dte.saveAsConll(dte.cwOfVerts.get(dte.graphs.indexOf(dte.graphComponent.getGraph())));
		    	} else {
		    		JOptionPane.showMessageDialog(dte.graphComponent, "NO CELLS FOUND IN GRAPH", "ERROR", JOptionPane.ERROR_MESSAGE);
					return;
		    	}
		    }
		}
	}
	
	/*public static class RemoveUnusedTags extends AbstractAction {
		protected boolean arrow;
		
		public RemoveUnusedTags(boolean edge) {
			arrow = edge;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked remove unused tags");
			ArrayList<String> usedTags = new ArrayList<>();
			if (arrow) {
				for (Object edge : dte.graphComponent.getGraph().getChildEdges(dte.graphComponent.getGraph().getDefaultParent())) {
					usedTags.add(((mxCell) edge).getValue().toString());
				}
			} else {
				for (String posTag : dte.hoverText.values()) {
					usedTags.add(posTag);
				}
			}
			for (String tag : (arrow?dte.graphComponent.mArrowLabels:dte.graphComponent.mPOSTags)) {
				
			}
		}
	}*/

	public static class AddTagsAction extends AbstractAction {
		protected boolean keepOld, arrow;
		
		public AddTagsAction(boolean keep, boolean edge) {
			keepOld = keep;
			arrow = edge;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked add arrow tags");
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter conllfilter = new FileNameExtensionFilter("txt files (*.txt)", "txt");
			fileChooser.setFileFilter(conllfilter);
			fileChooser.setDialogTitle("Open .txt File");
			fileChooser.setFileFilter(conllfilter);
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				Collection<String> labels = new TreeSet<String>(Collator.getInstance());
				String filename = fileChooser.getSelectedFile().getAbsolutePath();
				try {
					BufferedReader in = new BufferedReader(new FileReader(filename));
					String thisLine = "";
					while ((thisLine = in.readLine()) != null) {
						if (!thisLine.equals("")) {
							labels.add(thisLine);
						}
					}
					in.close();
					if (arrow) {
						if (keepOld)
							dte.graphComponent.addArrowLabels(labels);
						else
							dte.graphComponent.setArrowLabels(labels);
					} else {
						if (keepOld) {
							dte.graphComponent.addPOSTags(labels);
							//for (String label : labels) {
							//	dte.graphComponent.mPOSTagsToShapesAndColors.put(label, new String[] {"rectangle", "white"});
							//}
						}
						else {
							dte.graphComponent.setPOSTags(labels);
							dte.graphComponent.mPOSTagsToShapesAndColors.clear();
							//for (String label : labels) {
							//	dte.graphComponent.mPOSTagsToShapesAndColors.put(label, new String[] {"rectangle", "white"});
							//}
						}
					}
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				
			}

		}

	}
	
	public static class TypeTagAction extends AbstractAction {
		protected boolean edge;
		public TypeTagAction(boolean arrow) {
			edge = arrow;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked type tag");
			final JTextField tf = new JTextField(10);
			JButton add = new JButton("Add "+ (edge?"Arrow":"POS") +" Tag");
			
			Action addTag = new AbstractAction() {
			    @Override
			    public void actionPerformed(ActionEvent e) {
			    	System.out.println("You clicked add tag");
					String[] text = {tf.getText()};
					tf.setText("");
					String tag = "";
					for (String s : text)
						tag += s;
					ArrayList<String> tags = new ArrayList<String>();
					tags.add(tag);
					if (edge)
						dte.graphComponent.addArrowLabels(tags);
					else
						dte.graphComponent.addPOSTags(tags);
			    }
			};
			
			tf.addActionListener(addTag);
			add.addActionListener(addTag);
			
			JPanel jp = new JPanel();
			jp.add(tf);
			jp.add(add);
			JFrame jf = new JFrame();
			jf.add(jp);
			jf.setSize(300, 100);
			jf.setVisible(true);
			jf.setLocationRelativeTo(null);
		}
		
	}
	
	public static class TypeSentenceAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked type sentence");
			final JTextField tf = new JTextField(10);
			JButton add = new JButton("Add Sentence");
			
			Action addSent = new AbstractAction() {
			    @Override
			    public void actionPerformed(ActionEvent e)  {
			    	System.out.println("You clicked add sentence");
					String[] text = {tf.getText()};
					tf.setText("");
					String sentence = "";
					for (String s : text)
						sentence += s;
					String[] arr = new String[1];
					arr[0] = sentence;
					System.out.println(dte);
					dte.addSentences(arr);
				}
			};
			
			tf.addActionListener(addSent);
			add.addActionListener(addSent);			
			
			JPanel jp = new JPanel();
			jp.add(tf);
			jp.add(add);
			JFrame jf = new JFrame();
			jf.add(jp);
			jf.setSize(300, 100);
			jf.setVisible(true);
			jf.setLocationRelativeTo(null);
		}
		
	}
	
	public static class AddSentencesAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked add sentences");
			JRadioButton paragraphB = new JRadioButton("Paragraph Format");
			paragraphB.setActionCommand("Paragraph");
			paragraphB.setSelected(true);
			setChoice("Paragraph");

		    JRadioButton sentenceB = new JRadioButton("List of Sentences");
		    sentenceB.setActionCommand("Sentences");

		    JRadioButton nodeB = new JRadioButton("List of Nodes");
		    nodeB.setActionCommand("Nodes");

		    //Group the radio buttons.
		    ButtonGroup group = new ButtonGroup();
		    group.add(paragraphB);
		    group.add(sentenceB);
		    group.add(nodeB);
		    
		    final JLabel picture = new JLabel(new ImageIcon("/home/rheadedhia/Paragraph.png"));
		    
		    ActionListener listener = new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
			    	String ac = e.getActionCommand();
			        picture.setIcon(new ImageIcon("/home/rheadedhia/"+ ac +".png"));
			    	System.out.println("You selected: " + ac);
			    	setChoice(e.getActionCommand());
			    }
			};
		    
		    //Register a listener for the radio buttons.
		    paragraphB.addActionListener(listener);
		    sentenceB.addActionListener(listener);
		    nodeB.addActionListener(listener);
		    
		    //Put the radio buttons in a column in a panel.
	        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
	        radioPanel.add(paragraphB);
	        radioPanel.add(sentenceB);
	        radioPanel.add(nodeB);
	        
	        JPanel panel = new JPanel(); 
	        panel.add(radioPanel, BorderLayout.LINE_START);
	        panel.add(picture, BorderLayout.CENTER);
	        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
	        
	        JComponent contentPane = panel;
	        contentPane.setOpaque(true);
	        
	        //Create window
	        final JFrame frame = new JFrame("Select An Option");
	        		
			JButton add = new JButton("Choose File");
			add.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("You clicked choose file");
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
					addSentences();
				}
			});
			
			JPanel button = new JPanel();
			button.add(add);
			
			//Set up the window.
	        frame.setContentPane(contentPane);
	        frame.add(button);
	        frame.pack();
	        frame.setSize(500, 450);
	        frame.setResizable(false);
	        frame.setVisible(true);
	        frame.setLocationRelativeTo(null);
		}
		
		String choice = "";
		
		public void setChoice(String s) {
			choice = s;
		}
		
		public void addSentences() {
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter conllfilter = new FileNameExtensionFilter("txt files (*.txt)", "txt");
			fileChooser.setFileFilter(conllfilter);
			fileChooser.setDialogTitle("Open .txt File");
			fileChooser.setFileFilter(conllfilter);
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				
				String filename = fileChooser.getSelectedFile().getAbsolutePath();
				if (choice.equals("Sentences")) {
					ArrayList<String> sents = new ArrayList<String>();
					try {
						BufferedReader in = new BufferedReader(new FileReader(filename));
						String thisLine = "";
						while ((thisLine = in.readLine()) != null) {
							if (!thisLine.equals("")) {
								sents.add(thisLine);
							}
						}
						in.close();
						String[] sentsToAdd = new String[sents.size()];
						sentsToAdd = sents.toArray(sentsToAdd);
						dte.addSentences(sentsToAdd);
						
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				} else if (choice.equals("Paragraph")) {
					try {
						String paragraph = "";
						BufferedReader in = new BufferedReader(new FileReader(filename));
						String thisLine = "";
						while ((thisLine = in.readLine()) != null) {
							if (!thisLine.equals("")) {
								paragraph += thisLine;
							}
						}
						in.close();
						
						String[] sentences = paragraph.split("(?<=[!?.]\")|(?<=[!?.](?!\"))");
						for (String sent : sentences) {
							sent = sent.trim();
							System.out.println(sent);
						}
						
						dte.addSentences(sentences);
						
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				} else if (choice.equals("Nodes")) {
					ArrayList<ArrayList<String>> sentences = new ArrayList<ArrayList<String>>();
					ArrayList<String> sent = new ArrayList<String>();
					try {
						BufferedReader in = new BufferedReader(new FileReader(filename));
						String thisLine = "";
						while ((thisLine = in.readLine()) != null) {
							if (!thisLine.equals("")) {
								sent.add(thisLine);
							} else {
								sentences.add(sent);
								sent = new ArrayList<String>();
							}
						}
						in.close();
						dte.addSentences(sentences);
						
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				}
			}
		}		
	}
	
	public static class FormatAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked Format");
			dte.graphComponent.format();
		}
	}
	
	public static class SplitWordAction extends AbstractAction {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked Split Word");
			final Object cell = dte.graphComponent.getGraph().getSelectionCell();
			if (cell == null) {
				JOptionPane.showMessageDialog(dte.graphComponent, "NO NODE SELECTED", "ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			} else if (((mxCell) cell).getParent() != dte.graphComponent.getGraph().getDefaultParent()) {
				JOptionPane.showMessageDialog(dte.graphComponent, "CANNOT SPLIT WORD PART", "ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			} else if (((mxCell) cell).getChildCount()>0) {
				JOptionPane.showMessageDialog(dte.graphComponent, "CANNOT SPLIT GROUP", "ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			}
		    // display the JOptionPane showConfirmDialog if specialConll is false (confirm dialog has not been showed before)
			int reply = JOptionPane.YES_OPTION;
			if (!dte.specialConll.getValue()) {
				reply = JOptionPane.showConfirmDialog(null, "If you do, when you when you click \"Save As .conll File\", it will be saved as a special \nconll file (.sconll) instead of as a standard conll file (.conll)", "Are you sure you want to split this word?", JOptionPane.YES_NO_OPTION);
			}
		    if (reply == JOptionPane.YES_OPTION) {
		    	
				// Get cell info				
				if (cell != null) {
					final String word = ((mxCell) cell).getValue().toString();
					JLabel jl = new JLabel("Add a space where you want to split the string:");
					
					final JTextField tf = new JTextField(word);
					JButton where = new JButton("Split");
					final JFrame jf = new JFrame();
					
					Action split = new AbstractAction() {
					    @Override
					    public void actionPerformed(ActionEvent e) {
					    	System.out.println("You clicked split");
							String wordToSplit = tf.getText();
							if (wordToSplit.replaceAll("\\s+", "").equals(word)) {
								dte.specialConll.setValue(true);
								dte.splitWord(wordToSplit, cell);								
								jf.dispatchEvent(new WindowEvent(jf, WindowEvent.WINDOW_CLOSING));
							} else {
								JOptionPane.showMessageDialog(dte.graphComponent, "CANNOT CHANGE WORD", "ERROR", JOptionPane.ERROR_MESSAGE);
							}
					    }
					};
					
					tf.addActionListener(split);
					where.addActionListener(split);
					
					JPanel jp = new JPanel();
					jp.add(jl);
					jp.add(tf);
					jp.add(where);
					jf.add(jp);
					jf.setSize(300, 100);
					jf.setVisible(true);
					jf.setLocationRelativeTo(null);
				}
			}
		}
	}
	
	public static class DeleteSelectedEdgesAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked delete selected edges.");
			if (dte.graphComponent.mAllowEdgeDeletion.getValue()) {
				mxGraph graph = dte.graphComponent.getGraph();
				for (Object o : graph.getSelectionCells()) {
					if (graph.getModel().isEdge(o) && ((mxCell) o).getParent() == graph.getDefaultParent()) {
						graph.getModel().remove(o);
					}
				}
				dte.graphComponent.formatAsNeeded();
			}
		}
	}
	
	public static class SetPOSTagAction extends AbstractAction {
		protected String tag;
		protected Object node;
		public SetPOSTagAction(String t, Object vert) {
			tag = t;
			node = vert;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked set pos tag to " + tag);
			// Maps node to its POS tag in hoverText
			dte.hoverText.put(node, tag);
			
			// Gets index of current graph within graphs
			int currIndex = dte.graphs.indexOf(dte.graphComponent.getGraph());
			
			// If cwOfVerts contains the vertex, set its corresponding ConllWord's POS tag equal to the input tag			
			if (dte.cwOfVerts.get(currIndex).containsKey(node)) {
				dte.cwOfVerts.get(currIndex).get(node).setPOS(tag);
			} 
			// Otherwise (the vertex is a child of a group) do nothing; when saving as .CONLL file, the program will use the set POS tag
			// within hoverText.
			
			// Sets the shape and color of the vertex if it has been set; otherwise leave as white rectangle (default)
			if (dte.graphComponent.mPOSTagsToShapesAndColors.keySet().contains(tag)) {
				dte.graphComponent.getGraph().setCellStyles(mxConstants.STYLE_SHAPE, dte.graphComponent.mPOSTagsToShapesAndColors.get(tag)[0], new Object[] { node });
				List<Object> children = new ArrayList<>();
				if ((children = dte.graphComponent.getCV(node)).size()>0 && !((mxCell) children.get(0)).isVisible()) {
					dte.graphComponent.getGraph().setCellStyles(mxConstants.STYLE_FILLCOLOR, dte.graphComponent.mColors.get(dte.graphComponent.mPOSTagsToShapesAndColors.get(tag)[1])[1], new Object[] { node });
				} else {
					dte.graphComponent.getGraph().setCellStyles(mxConstants.STYLE_FILLCOLOR, dte.graphComponent.mColors.get(dte.graphComponent.mPOSTagsToShapesAndColors.get(tag)[1])[0], new Object[] { node });
				}
			}
		}
	}
	
	public static class SetArrowTagAction extends AbstractAction {
		protected String tag;
		protected Object edge;
		public SetArrowTagAction(String t, Object arrow) {
			tag = t;
			edge = arrow;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked set arrow tag to " + tag);
			dte.graphComponent.labelChanged(edge, tag, null);
		}
	}
	
	public static class SettingsAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked settings");
			JFrame frame = new JFrame();
			int height = 180;
			
			// Maps POS Tags to drop down menus with all possible shapes && creates vector<String> that holds these shapes
			final Map<String, JComboBox<String>> shapesDDM = new HashMap<>();
			Vector<String> allShapes = new Vector<>();
			for (String shape : dte.graphComponent.mAllShapes) {
				allShapes.add(shape);
			}
			
			// Maps POS Tags to drop down menus with all colors && creates vector<String> that holds these colors
			final Map<String, JComboBox<String>> colorsDDM = new HashMap<>();
			Vector<String> colorNames = new Vector<>();
			for (String colorName : dte.graphComponent.mColors.keySet()) {
				colorNames.add(colorName);
			}
			
			// Creates panel and box to hold components vertically
			JPanel panel = new JPanel();
			frame.getContentPane().add(panel);
			Box vertical = Box.createVerticalBox();
			panel.add(vertical);
			
			// Gives user information about purpose of JFrame
			JPanel title = new JPanel();
			vertical.add(title);
			title.add(new JLabel("<html><h2>Set Which Shape Shows Up For Each POS Tag</h2></html>"));
			JPanel info = new JPanel();
			vertical.add(info);
			info.add(new JLabel("<html><i>Note: The default (if no part of speech tag has been set) is a white rectangle.</i></html>"));
			
			// Adds Separator 
			vertical.add(Box.createRigidArea(new Dimension(0, 5)));
			vertical.add(new JSeparator());
			vertical.add(Box.createRigidArea(new Dimension(0, 5)));
			
			// Adds drop down menu to each POS Tag
			for (String pt : dte.graphComponent.mPOSTags) {
				JPanel hold = new JPanel();
				vertical.add(hold);
				hold.add(new JLabel(pt));
				
				JComboBox<String> sddm = new JComboBox<>(allShapes);
				JComboBox<String> cddm = new JComboBox<>(colorNames);
				System.out.println(pt);
				
				if (dte.graphComponent.mPOSTagsToShapesAndColors.containsKey(pt)) {
					sddm.setSelectedItem(dte.graphComponent.mPOSTagsToShapesAndColors.get(pt)[0]);
					cddm.setSelectedItem(dte.graphComponent.mPOSTagsToShapesAndColors.get(pt)[1]);
				} else {
					sddm.setSelectedItem("rectangle");
					cddm.setSelectedItem("white");
				}
				
				hold.add(sddm);
				hold.add(cddm);
				shapesDDM.put(pt, sddm);
				colorsDDM.put(pt, cddm);
				height += 34;
			}
			
			// Adds Set Button
			JPanel button = new JPanel();
			vertical.add(button);
			JButton set = new JButton("Set");
			button.add(set);
			set.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MymxGraphComponent gc = dte.graphComponent;
					for (String pos : shapesDDM.keySet()) {
						String shape = shapesDDM.get(pos).getSelectedItem().toString();
						String colorName = colorsDDM.get(pos).getSelectedItem().toString();
						String rgb = gc.mColors.get(colorName)[0];
						System.out.println("pos: " + pos + ", shape: " + shape + ", color: " + colorName + ", rgb: " + rgb);
						// mColors.get(colorName) returns colorRGB value
						gc.mPOSTagsToShapesAndColors.put(pos, new String[] {shape, colorName});
					}
					// Update all vertices that are not groups
					for (Object vert : gc.getGraph().getChildVertices(gc.getGraph().getDefaultParent())) {
						if (!dte.groups.contains(vert)) {
							
							String posTag = dte.cwOfVerts.get(dte.graphs.indexOf(gc.getGraph())).get(vert).getPOS();
							String[] color = dte.graphComponent.mColors.get("white"); //default color
							String shape = "rectangle"; // default shape
							
							if (gc.mPOSTagsToShapesAndColors.containsKey(posTag)) {
								color = gc.mColors.get(gc.mPOSTagsToShapesAndColors.get(posTag)[1]);
								shape = gc.mPOSTagsToShapesAndColors.get(posTag)[0];
							}
								
							gc.getGraph().setCellStyles(mxConstants.STYLE_SHAPE, shape, new Object[] {vert});
							List<Object> children = new ArrayList<>();
							if ((children = dte.graphComponent.getCV(vert)).size()>0 && !((mxCell) children.get(0)).isVisible()) {
								gc.getGraph().setCellStyles(mxConstants.STYLE_FILLCOLOR, color[1], new Object[] {vert});
							} else {
								gc.getGraph().setCellStyles(mxConstants.STYLE_FILLCOLOR, color[0], new Object[] {vert});
							}
						}
					}
					// Update all child vertices
					for (Object group : dte.groups) {
						for (Object child : gc.getGraph().getChildVertices(group)) {
							String posTag = dte.hoverText.get(child);
							String color = "ffffff"; // default color
							String shape = "rectangle"; //default shape
							
							if (gc.mPOSTagsToShapesAndColors.containsKey(posTag)) {
								color = gc.mColors.get(gc.mPOSTagsToShapesAndColors.get(posTag)[1])[0];
								shape = gc.mPOSTagsToShapesAndColors.get(posTag)[0];
							}
								
							gc.getGraph().setCellStyles(mxConstants.STYLE_SHAPE, shape, new Object[] {child});
							gc.getGraph().setCellStyles(mxConstants.STYLE_FILLCOLOR, color, new Object[] {child});

						}
					}
				}
			});
			
			frame.setVisible(true);
			frame.setLocationRelativeTo(null);
			frame.setSize(550, height);
		}
	}
	
	public static class UnsplitWordAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("You clicked upsplit word.");
			mxGraph graph = dte.graphComponent.getGraph();
			Object groupCell = graph.getSelectionCell();
			if (groupCell != null) {
				if (((mxCell)groupCell).getChildCount()>0) {
					// Gets ConllWord that represents the group cell
					ConllWord groupCW = dte.cwOfVerts.get(dte.graphs.indexOf(graph)).get(groupCell);
					
					// Removes the child cells from the ConllWord
					List<Object> children = groupCW.removeChildCells();
					
					// Re-maps group in cwOfVerts to updated ConllWord
					dte.cwOfVerts.get(dte.graphs.indexOf(graph)).put(groupCell, groupCW);
					
					// Removes the child cells from hoverText && from the graph
					// Changes cell style of group to match nodes && adds word && resizes group
					graph.getModel().beginUpdate();
					try {
						graph.setCellsDeletable(true);
						graph.removeCells(graph.getChildVertices(groupCell));
						graph.setCellsDeletable(false);
						for (Object child : children) {
							dte.hoverText.remove(child);		
						}
						graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "white", new Object[] { groupCell });
						graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "black", new Object[] { groupCell });
						dte.addText(groupCW.getWord(), groupCell);
						dte.size(groupCell, 10);
					} finally {
						graph.getModel().endUpdate();
						dte.graphComponent.refresh();
					}
					dte.graphComponent.formatAsNeeded();
					
					// Removes the group cell from hoverText && the list of groups
					dte.hoverText.remove(groupCell);
					dte.groups.remove(groupCell);
					
					if(dte.groups.size()==0) {
						dte.specialConll.setValue(false);
						JOptionPane.showMessageDialog(dte.graphComponent, "The file will now save as a standard (.conll) file.", "Notice", JOptionPane.INFORMATION_MESSAGE);
					}
					
				} else {
					JOptionPane.showMessageDialog(dte.graphComponent, "CELL IS NOT A GROUP", "ERROR", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				JOptionPane.showMessageDialog(dte.graphComponent, "NO CELL SELECTED", "ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
	}
	
}

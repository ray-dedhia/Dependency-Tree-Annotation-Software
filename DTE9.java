package dt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.mxgraph.examples.swing.editor.BasicGraphEditor;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;

/**Latest version.*/
public class DTE9 {

	public List<List<ConllWord>> sentences = new ArrayList<>(); //list of sentences, with each sentence represented as a list of words
	public ArrayList<String> sentSideBar = new ArrayList<>(); //list of sentences (not broken up); displayed in side bar (sentences tab)
	public List<mxGraph> graphs = new ArrayList<>(); //stores all the graphs
	public List<Object> groups = new ArrayList<>(); //stores all groups created (by splitting words)
	public ArrayList<HashMap<Object, ConllWord>> cwOfVerts = new ArrayList<HashMap<Object, ConllWord>>(); // maps vertices with their respective conllwords
	public HashMap<Object, String> hoverText = new HashMap<Object, String>(); // maps vertices to their hover text (part of speech tags from conll file)
	public ArrayList<ConllWord> conllOutput = new ArrayList<>(); //holds ConllWords by their order in their original sentence (for outputting into a new conll file)
	public MyMxGraphComponent2 graphComponent;
	private MyBasicGraphEditor2 editor;
	// hold values of formatting options chosen by user
	public MutableBoolean autoFormat = new MutableBoolean(false), conOnOverlap = new MutableBoolean(true), specialConll = new MutableBoolean(false);
	public MutableBoolean allowEdgeMovement = new MutableBoolean(true), rightAngleArrows = new MutableBoolean(true);

	public DTE9() {
		List<String> arrowTags = new ArrayList<>(); // arrow labels list
		List<String> posTags = new ArrayList<>();
		mxGraph mxgraph = createAndConfigureMxGraph();
		
		arrowTags.addAll(Arrays.asList(new String[] { "Subject", "Object", "Relative Clause", "Object Complement", "Indirect Object", "Direct Object"}));
		posTags.addAll(Arrays.asList(new String[] {"Noun", "Adjective", "Adverbial Modifier", "Conjunction", "Determiner", "Preposition", "Pronoun", "Verb"}));

		graphComponent = new MyMxGraphComponent2(this, mxgraph, arrowTags, posTags); // create the mxGraphComponent
		
		// Alters keyboard handler
		editor = new MyBasicGraphEditor2(graphComponent, this);
		editor.createFrame(new MyEditorMenuBar2(editor, this)).setVisible(true);
		
	}
	
	/** Returns the basic graph editor */
	public MyBasicGraphEditor2 getBasicGraphEditor() {
		return editor;
	}
	
	/** Called to open .conll Files */
	public void openConllFile(String filename) { 
		ArrayList<ArrayList<ConllWord>> conllSents = new ArrayList<ArrayList<ConllWord>>();
		// List<String> arrowLabels = new ArrayList<>();
		Collection<String> arrowLabels = new TreeSet<String>(Collator.getInstance());

		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String thisLine = "";
			ArrayList<ConllWord> sentCW = new ArrayList<ConllWord>();
			boolean skipWord = false;
			while ((thisLine = in.readLine()) != null) {
				if (!thisLine.equals("")) {
					String[] word = thisLine.split("\\t");
					// If saving as special conll file (.sconll) 
					if (specialConll.getValue()) {
						// if the word contains a dash followed by a digit between 1 and 9, it is a word part that is part of a split word
						if (word[0].matches(".*-[1-9].*")) {
							String childPOSTag = word[4].equals("_")?word[3]:word[4];
							if (!graphComponent.mPOSTags.contains(childPOSTag) && !childPOSTag.equals("_")) {
								graphComponent.mPOSTags.add(childPOSTag);
							}
							// get the ConllWord at the word's parent index (represents the entire word) and add the word part and its childPOSTag to the ConllWord's ArrayList childValues
							sentCW.get((Integer.parseInt(word[6].replace("-0", "")))-1).addChildValue(word[1], childPOSTag);
							// do not add word parts to cwOfVerts
							skipWord = true;
						}
					}
					// if the word is not a word part, add it to cwOfVerts
					if (!skipWord) {
						sentCW.add(new ConllWord(word[0], word[1], word[3], word[4], word[6], word[7]));
						if (!arrowLabels.contains(word[7]))
							arrowLabels.add(word[7]);
						// If the fine grain POS tag is not equal to "_", that POS tag is added. Otherwise, 
						// the coarse grain POS tag is added.
						String pt = word[4].equals("_")?word[3]:word[4];
						if (!graphComponent.mPOSTags.contains(pt) && !pt.equals("_")) {
							graphComponent.mPOSTags.add(pt);
							//graphComponent.mPOSTagsToShapesAndColors.put(pt, new String[] {"rectangle", "white"});
							System.out.println("adding: " + pt);
						}
					} else {
						skipWord = false;
					}
				} else {
					conllSents.add(sentCW);
					sentCW = new ArrayList<ConllWord>();
				}
			}
			in.close();
			if (arrowLabels.size() > 0) {
				// java.util.Collections.sort(arrowLabels);
				graphComponent.addArrowLabels(arrowLabels);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		addConllSentences(conllSents);
	}
	
	/** Draws ConllWords; called after a sentence from a .conll file is clicked on in the sentence bar (from createConllGraph); this method only draws the root node;
	 * then it calls a helper method that calls the rest of the nodes. */
	public void drawConllWords(List<ConllWord> sent, mxGraph graph, int index) {
		HashMap<Object, ConllWord> hm = new HashMap<>();
		for (ConllWord cw : sent) {
			if (cw.getPI() == -1) {
				String shape = "", fillcolor = "", hovText = "";
				System.out.println(cw.isGroup());
				
				// If POS tag associated with this ConllWord has been mapped to a shape and color, make those the shape and color of the vertex to be drawn
				if (graphComponent.mPOSTagsToShapesAndColors.keySet().contains(cw.getPOS())) {
					shape = graphComponent.mPOSTagsToShapesAndColors.get(cw.getPOS())[0];
					fillcolor = graphComponent.mColors.get(graphComponent.mPOSTagsToShapesAndColors.get(cw.getPOS())[1])[0];
				// otherwise make the vertex a white rectangle (default)
				} else {
					shape = "rectangle";
					fillcolor = "white";
				}				
				
				Object v;
				
				// If CW is group, create children and add them as children of group
				if (cw.isGroup()) {
					Object[] children = new Object[cw.getChildValues().size()];
					double x = 90;
					Object prevNode = null;
					int loop = 0;
					for (String[] childInfo : cw.getChildValues()) {
						Object child = graph.insertVertex(graph.getDefaultParent(), null, childInfo[0], x, 10, 80, 30, "fillColor=white;strokeColor=black;fontColor=black;strokeWidth=0.5");
						// Set group's hovertext equal to its children's word parts with "--" between them
						hovText += childInfo[0] + (loop==cw.getChildValues().size()-1?"":"--");
						if (prevNode != null) {
							graph.insertEdge(graph.getDefaultParent(), null, null, prevNode, child, "endArrow=none");
						}
						cw.addChildCell(child);
						children[loop] = child;
						hoverText.put(child, childInfo[1]);
						x = size(child, x);
						loop++;
						prevNode = child;
					}
					v = graph.groupCells(null, 15, children);
					graphComponent.getGraph().setCellStyles(mxConstants.STYLE_STROKECOLOR, "black", new Object[] { v });
					graphComponent.getGraph().setCellStyles(mxConstants.STYLE_STROKEWIDTH, "0.5", new Object[] { v });
					graphComponent.getGraph().setCellStyles(mxConstants.STYLE_FILLCOLOR, "a8c6cf", new Object[] { v });
				} else {
				// Otherwise just draw the vertex and size it and set its hoverText
					v = graph.insertVertex(graph.getDefaultParent(), null, cw.getWord(), 90, 10, 80, 30, "fillColor=" + fillcolor + ";strokeColor=black;fontColor=black;strokeWidth=0.5;shape="+shape);
					size(v, 90);
					hovText += cw.getPOS().equals("_") ? "" : cw.getPOS();
				}
				
				System.out.println("added vertex to conll graph");
				hm.put(v, cw);
				hoverText.put(v, hovText);
				hm = drawChildren(cw.getIndex(), sent, v, graph, hm);
			}
		}
		if (hm.size() > 0)
			cwOfVerts.set(index, hm);

	}
	
	/** Helper method for drawConllWodrs that draws the rest of the nodes after the root has been drawn */
	public HashMap<Object, ConllWord> drawChildren(int index, List<ConllWord> sent, Object p, mxGraph graph, HashMap<Object, ConllWord> hm) {
		for (ConllWord cw : sent) {
			if (cw.getPI() == index) {
				String hovText = "", shape = "", fillcolor = "";
				System.out.println(cw.isGroup());
				
				if (graphComponent.mPOSTagsToShapesAndColors.keySet().contains(cw.getPOS())) {
					shape = graphComponent.mPOSTagsToShapesAndColors.get(cw.getPOS())[0];
					fillcolor = graphComponent.mColors.get(graphComponent.mPOSTagsToShapesAndColors.get(cw.getPOS())[1])[0];
				} else {
					shape = "rectangle";
					fillcolor = "white";
				}
				
				Object c;
				
				// If CW is group, create children and add them as children of the previously created vertex
				if (cw.isGroup()) {
					Object[] children = new Object[cw.getChildValues().size()];
					double x = 90;
					Object prevNode = null;
					int loop = 0;
					for (String[] childInfo : cw.getChildValues()) {
						Object child = graph.insertVertex(graph.getDefaultParent(), null, childInfo[0], x, 10, 80, 30, "fillColor=white;strokeColor=black;fontColor=black;strokeWidth=0.5");
						hovText += childInfo[0] + (loop==cw.getChildValues().size()-1?"":"--");
						children[loop] = child;
						if (prevNode != null) {
							graphComponent.getGraph().insertEdge(graph.getDefaultParent(), null, null, prevNode, child, "endArrow=none;strokeColor=black");
						}
						cw.addChildCell(child);
						hoverText.put(child, childInfo[1]);
						x = size(child, x);
						loop++;
						prevNode = child;
					}
					c = graph.groupCells(null, 15, children);
					graphComponent.getGraph().setCellStyles(mxConstants.STYLE_STROKECOLOR, "black", new Object[] { c });
					graphComponent.getGraph().setCellStyles(mxConstants.STYLE_STROKEWIDTH, "0.5", new Object[] { c });
					graphComponent.getGraph().setCellStyles(mxConstants.STYLE_FILLCOLOR, "a8c6cf", new Object[] { c });
				} else {
					c = graph.insertVertex(graph.getDefaultParent(), null, cw.getWord(), 90, 10, 80, 30, "fillColor=" + fillcolor + ";strokeColor=black;fontColor=black;strokeWidth=0.5;shape="+shape);
					size(c, 90);
					hovText += cw.getPOS().equals("_") ? "" : cw.getPOS();
				}
				
				hm.put(c, cw);

				hoverText.put(c, hovText);
				graph.insertEdge(graph.getDefaultParent(), null, cw.getAL().equals("_") ? null : cw.getAL(), p, c, "strokeColor=black;fontColor=black");
				//if arrow label is equal to _ then don't add a label; else add the arrow label
				drawChildren(cw.getIndex(), sent, c, graph, hm);
			}
		}
		return hm;
	}
	
	/** Saves current dependency tree as a .conll file as long as it has one and only one root; calls the helper method getChildren after it has found the root; this fills up conllOutput with the ConllWords
	 * associated with the vertices, sorted in the order that they were originally in the sentence; then conllOutput is used to create the .conll file */
	public void saveAsConll(HashMap<Object, ConllWord> hm) {
		conllOutput = new ArrayList<ConllWord>();
		int numOfRoots = 0;
		Object root = null;
		Object[] verts = graphComponent.getGraph().getChildVertices(graphComponent.getGraph().getDefaultParent());
		for (int i = 0; i < verts.length; i++) {
			System.out.println("vertex " + i + ": " + graphComponent.getGraph().getModel().getValue(verts[i]).toString());
			conllOutput.add(new ConllWord());
			if (graphComponent.getGraph().getIncomingEdges(verts[i]).length == 0) {
				root = verts[i];
				numOfRoots++;
				// System.out.println("ROOT: " + ((mxCell)
				// verts[i]).getValue().toString());
			}
		}
		if (numOfRoots == 1) {
			hm.get(root).setAL("ROOT");
			hm.get(root).setPI(-1);
			conllOutput.set(hm.get(root).getIndex(), hm.get(root));
			getChildren(root, hm.get(root).getIndex(), hm);
		} else if (numOfRoots > 1) {
			JOptionPane.showMessageDialog(graphComponent, "THERE CANNOT BE MORE THAN ONE ROOT", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			JOptionPane.showMessageDialog(graphComponent, "NO ROOTS FOUND", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		

		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("/home/me/Documents"));
		int retrival = chooser.showSaveDialog(null);
		if (retrival == JFileChooser.APPROVE_OPTION) {
			try {
				// if specialConll, save as .SCONLL; otherwise save as .CONLL
				String ending = (specialConll.getValue()?".s":".")+"conll";
				PrintStream ps = new PrintStream(chooser.getSelectedFile() + ending);
				if (conllOutput.size() > 0) {
					for (ConllWord cw : conllOutput) {
						if (specialConll.getValue()) {
							// If conllWord is not a group, output normally
							if (!cw.isGroup()) {
								ps.println(cw.getIndex() + 1 + "\t" + cw.getWord() + "\t" + "_" + "\t" + cw.getCG() + "\t" + cw.getFG() + "\t" + "_" + "\t" + (cw.getPI() + 1) + "\t" + cw.getAL() + "\t" + "_" + "\t" + "_");
							} else {
							// If conllWord is a group, output index-0 && complete word && parent index && arrow label (everything else is "_")
								ps.println(cw.getIndex() + 1 + "-0" + "\t" + cw.getWord() + "\t" + "_" + "\t" + "_" + "\t" + "_" + "\t" + "_" + "\t" + (cw.getPI() + 1) + "\t" + cw.getAL() + "\t" + "_" + "\t" + "_");
								List<Object> ccs = cw.getChildCells();
								for (int i = 0; i < ccs.size(); i++) {
									// For each word part, output word-(index of mxCell within child cell list, which corresponds to its location relative to the other word parts)
									// && the value of the word part && its part of speech tag (get from hoverText map, which stores POS tags of all cells). Everything else is "_".
									ps.println(cw.getIndex() + 1 + "-" + (i + 1) + "\t" + ((mxCell) ccs.get(i)).getValue().toString() + "\t" + "_" + "\t" + (hoverText.containsKey(ccs.get(i))?hoverText.get(ccs.get(i)):"_") + "\t" + "_" + "\t" + "_" + "\t" + (cw.getIndex() + 1 + "-0") + "\t" + "_" + "\t" + "_" + "\t" + "_");
								}
							}
						} else {
							ps.println((cw.getIndex() + 1) + "\t" + cw.getWord() + "\t" + "_" + "\t" + cw.getCG() + "\t" + cw.getFG() + "\t" + "_" + "\t" + (cw.getIndex() + 1 + "-0") + "\t" + "_" + "\t" + "_" + "\t" + "_");
						}
					}
					ps.println();
				}
				ps.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/** Helper method for saveAsConll that fills conllOutput with the ConllWords associated with the nodes in the graph, in the order that they were originally in the sentence. */
	public void getChildren(Object v, int parInd, HashMap<Object, ConllWord> hm) {
		if (graphComponent.getCV(v) != null) {
			// System.out.println();
			for (Object cv : graphComponent.getCV(v)) {
				// System.out.print("Child: " + ((mxCell)
				// cv).getValue().toString() + ", Parent Index: " + parInd);
				if (graphComponent.getGraph().getModel().getParent(cv) == graphComponent.getGraph().getDefaultParent()) {
					if (hm.get(cv).getIndex() != -1) {
						//If edges going towards the vertex have a value and that value is not ""
						//set the arrow label of its corresponding ConllWord equal to that value
						if (((mxCell) graphComponent.getGraph().getIncomingEdges(cv)[0]).getValue() != null) {
							if (!((mxCell) graphComponent.getGraph().getIncomingEdges(cv)[0]).getValue().toString().equals(""))
								hm.get(cv).setAL(((mxCell) graphComponent.getGraph().getIncomingEdges(cv)[0]).getValue().toString());
						}
						conllOutput.set(hm.get(cv).getIndex(), hm.get(cv).setPI(parInd));
					}
					getChildren(cv, hm.get(cv).getIndex(), hm);
				}
			}
			// System.out.println();
		}
	}
	
	/** Creates mxGraphs and configures by overriding certain methods and enabling and disabling certain features */
	public mxGraph createAndConfigureMxGraph() {
		// Create the mxGraph
		final mxGraph mxgraph = new mxGraph() {
			@Override
			public boolean isValidDropTarget(Object target, Object[] cells) {
				return false;
			}

			@Override
			public String getToolTipForCell(Object cell) {
				if (getModel().getValue(cell) != null) {
					if (getModel().isVertex(cell)) {
						if (hoverText.containsKey(cell)) {
							return hoverText.get(cell);
						}
					} else if (getModel().isEdge(cell)) {
						return getModel().getValue(cell).toString();
					}
				}
				return "";
			}
			
			@Override
			public boolean isCellMovable(Object cell) {
				if (getModel().isVertex(cell)) {
					return true;
				} else if (!allowEdgeMovement.getValue()) {
					return false;
				} else if (((mxCell) cell).getParent() != graphComponent.getGraph().getDefaultParent()) {
					return false;
				} else {
					return true;
				}
			}
			
			/** Makes sure that arrow cannot be drawn from a child cell to any of its parents, grandparents, etc. */
			@Override
			public boolean isCellConnectable(Object cell) {
				// if cell is a vertex
				if (((mxCell)cell).isVertex()) {
					// and arrowFrom is null, return true
					if (graphComponent.arrowFrom == null) {
						return true;
					}
					// otherwise return true if allParents of arrowFrom (vertex that arrow is being drawn from) don't contain cell and false otherwise
					return !graphComponent.getAllParents(graphComponent.arrowFrom, new ArrayList<Object>()).contains(cell);
				}
				// otherwise (cell is not a vertex) return false
				return false;
			}
		};
		
		mxgraph.setCellsResizable(false);
		mxgraph.setKeepEdgesInForeground(true);
		mxgraph.setCellsDeletable(false);
		mxgraph.setHtmlLabels(true);
		mxgraph.setEdgeLabelsMovable(false);
		mxgraph.setCellsBendable(false);
		mxgraph.setKeepEdgesInBackground(true);
		
		mxgraph.addListener(mxEvent.MOVE_CELLS, new mxIEventListener() {
			public void invoke(Object sender, mxEventObject evt) {
				Object[] cells = ((Object[]) evt.getProperty("cells"));
				for (Object cell : cells) {
					if (mxgraph.getModel().isEdge(cell)) {
						Object target = graphComponent.getGraph().getModel().getTerminal(cell, false);
						if (target == null) {
							graphComponent.getGraph().getModel().remove(cell);
						}
					} 
				}
				
				graphComponent.cellsMoved();
			}
		});
		mxgraph.getSelectionModel().addListener(mxEvent.CHANGE, new mxIEventListener() {
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				
				graphComponent.formatAsNeeded();
			}
		});
		return mxgraph;
	}
	
	/**method that can be used to change the label text on a node or edge*/
	public void addText(String s, Object c) {
		graphComponent.labelChanged(c, s, null);
	}

	/**Adds sentences that have not been broken up (each String is one sentence)*/
	public void addSentences(String[] sents, boolean splitVC, boolean splitOther) {
		for (String sent : sents) {
			sent = sent.trim();
			sentSideBar.add(sent);
			String regex;
			if (splitVC && splitOther) {
				regex = " |(?=n't|'[a-su-z]+|[;,$%\"]|[!.?]$)|(?<=\\$)|(?<=\")";
			} else if (splitVC) {
				regex = " |(?=n't|'[a-su-z]+|[;,\"]|[!.?]$)|(?<=\")";
			} else if (splitOther) {
				regex = " |(?=[;,$%\"]|[!.?]$)|(?<=\\$)|(?<=\")";
			} else {
				regex = " |(?=[;,\"]|[!.?]$)|(?<=\")";
			}
			
			String[] arrWords = sent.split(regex);
			List<ConllWord> sentWords = new ArrayList<ConllWord>();
			
			for (int i = 0; i < arrWords.length; i++) {
				//arrWords[i] = arrWords[i].replaceAll("\\s", "");
				if (i != arrWords.length-1) {
					if (arrWords[i].equalsIgnoreCase("ca") && arrWords[i+1].equalsIgnoreCase("n't")) {
						arrWords[i] = arrWords[i] + "n";
					} else if (arrWords[i].equalsIgnoreCase("wo") && arrWords[i+1].equalsIgnoreCase("n't")) {
						arrWords[i] = "will";
					}
				}
				if (!arrWords[i].equals("")) {
					sentWords.add(new ConllWord(arrWords[i]));
				} 
			}
			
			// mxGraph graph = createGraph(sentWords);
			sentences.add(sentWords);
			// graphs.add(graph);
			graphs.add(null);
			cwOfVerts.add(null);
		}
		createSentenceView();
	}

	/**Adds sentences that have been broken up (each sentence is an array list of words/parts of words)*/
	public void addSentences(ArrayList<ArrayList<String>> sents) {
		for (ArrayList<String> sent : sents) {
			List<ConllWord> cwSent = new ArrayList<ConllWord>();
			graphs.add(null);
			cwOfVerts.add(null);
			StringBuilder buf = new StringBuilder();
			for (String w : sent) {
				cwSent.add(new ConllWord(w));
				buf.append(w).append(" ");
			}
			sentences.add(cwSent);
			cwSent = new ArrayList<ConllWord>();
			sentSideBar.add(buf.toString());
		}

		createSentenceView();
	}

	/**Adds sentences that come from conll files(*/
	public void addConllSentences(ArrayList<ArrayList<ConllWord>> conllSents) {
		for (int i = 0; i < conllSents.size(); i++) {
			graphs.add(null);
			cwOfVerts.add(null);
			String sentence = "";
			sentences.add(conllSents.get(i));
			for (ConllWord cw : conllSents.get(i)) {
				sentence += cw.getWord() + " ";
			}
			sentSideBar.add(sentence);
		}
		createSentenceView();
	}

	/**creates the JList of sentences to place in the sentence tab of the side bar
	the new sentences aren't added to the old ones; they're all redrawn together*/
	public void createSentenceView() {
		String[] sents = new String[sentSideBar.size()];
		sents = sentSideBar.toArray(sents);
		final JList<String> jl = new JList<String>(sents);
		jl.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {// This line prevents double events
					boolean format = false, conll = false;
					if (!sentences.get(jl.getSelectedIndex()).get(0).isString()) {
						conll = true;
					}
					if (graphs.get(jl.getSelectedIndex()) == null) {
						System.out.println("graph still has to be drawn");
						if (conll) {
							graphs.set(jl.getSelectedIndex(), createConllGraph(jl.getSelectedIndex()));
							System.out.println("called create conll graph");
							format = true;
						} else {
							graphs.set(jl.getSelectedIndex(), createGraph(sentences.get(jl.getSelectedIndex()), jl.getSelectedIndex()));
							System.out.println("called create regular graph");
						}						
					}
					mxGraph graph = graphs.get(jl.getSelectedIndex());
					graphComponent.setGraph(graph);
					graphComponent.getSelectionCellsHandler().refresh();
					graphComponent.repaint();
					graphComponent.sendEdgesBack();
					((MyBasicGraphEditor2) editor).installNewToolBar(graphComponent);
					System.out.println("Changed graph to: ");
					for (ConllWord word : sentences.get(jl.getSelectedIndex())) {
						System.out.print(word.getWord() + " ");
					}
					System.out.println();
					if (format)
						graphComponent.format();
				}
			}
		});
		createSideBar(jl);
	}

	/** Creates the sentence side bar; first it removes the old one*/
	public void createSideBar(JList<String> l) {
		if (editor.getLibraryPane().getTabCount() > 0)
			editor.getLibraryPane().removeTabAt(0);
		System.out.println("creating sentence side bar");
		final JScrollPane scrollPane = new JScrollPane(l);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		editor.getLibraryPane().add("Sentences", scrollPane);
	}

	/** method that splits word when split word button is clicked in editing side bar*/
	public void splitWord(String word, Object cell) {
		
		//Gets ConllWord mapped to the vertex that's going to be deleted within cwOfVerts.get(currIndex)
		int currIndex = graphs.indexOf(graphComponent.getGraph());
		ConllWord hold = cwOfVerts.get(currIndex).get(cell);
		
		// Holds all the word parts
		String[] parts = word.split(" ");
		
		// Holds x and y value of the original node
		double x = graphComponent.getGraph().getModel().getGeometry(cell).getX();
		double y = graphComponent.getGraph().getModel().getGeometry(cell).getY();
		
		// Array of all cells to be formatted & placed in a group
		Object[] children = new Object[parts.length];
		
		// Holds value of previous vertex created in below loop if there is one (otherwise holds the value null)
		Object prevNode = null;
		
		// Draws all child cells to hold all the word parts
		for (int i = 0; i < parts.length; i++) {
			Object v = graphComponent.getGraph().insertVertex(graphComponent.getGraph().getDefaultParent(), null, parts[i], x, y, 80, 30, "fillColor=white;strokeColor=black;fontColor=black;strokeWidth=0.5");
			children[i] = v;
			
			// Connect to previous node (if there was one)
			if (prevNode != null) {
				graphComponent.getGraph().insertEdge(graphComponent.getGraph().getDefaultParent(), null, null, prevNode, v, "endArrow=none");
			}
			
			// Makes cell not connect-able
			((mxCell) v).setConnectable(false);
			
			// Sizes cell based on the size of the word part it contains and then adds padding
			x = size(v, x);

			// Stores node so that it can be connected to the next node (if there is one)
			prevNode = v;
		}
		
		// Store sources and targets of edges going to and from the cell to be deleted && their arrow labels
		HashMap<Object, Object> sources = getPVandEL(cell);
		HashMap<Object, Object> targets = getCVandEL(cell);

		// Remove cell
		Object deleted = graphComponent.getGraph().getModel().remove(cell);
		System.out.println("removed: " + ((mxCell) deleted).getValue().toString());
		
		// Creates group & sets cell styles
		Object group = graphComponent.getGraph().groupCells(null, 15, children);
		((mxCell) group).setConnectable(true);
		graphComponent.getGraph().setCellStyles(mxConstants.STYLE_STROKECOLOR, "black", new Object[] {  group });
		graphComponent.getGraph().setCellStyles(mxConstants.STYLE_STROKEWIDTH, "0.5", new Object[] {  group });
		graphComponent.getGraph().setCellStyles(mxConstants.STYLE_FILLCOLOR, "a8c6cf", new Object[] { group });
		
		// Updates maps and lists
		cwOfVerts.get(currIndex).remove(cell); //Removes deleted cell from cwOfVerts map
		hoverText.remove(cell); // Removes deleted cell from hoverText
		hold.setPOS("_"); // Group cannot hold a POS tag; set equal to "_"
		hold.addChildCells(children); // Adds child nodes to the childCells ArrayList in the ConllWord that represents the group
		hoverText.put(group, word.replaceAll(" ", "--")); // Sets tool tip text equal to the word with the spaces (replaced with dashes ("--")) added by the user
		cwOfVerts.get(currIndex).put(group, hold); // Maps group to the ConllWord hold (the ConllWord that the deleted vertex was mapped to)
		groups.add(group); // Adds the group to the list of groups
		
		
		// Reconnect new group to targets/sources
		for (Object s : sources.keySet()) {
			graphComponent.getGraph().insertEdge(graphComponent.getGraph().getDefaultParent(), null, sources.get(s)==null?"":sources.get(s).toString(), s, group, "strokeColor=black;fontColor=black");
		}
		for (Object t : targets.keySet()) {
			graphComponent.getGraph().insertEdge(graphComponent.getGraph().getDefaultParent(), null, targets.get(t)==null?"":targets.get(t).toString(), group, t, "strokeColor=black;fontColor=black");
		}
	}
	
	/** Sizes vertex based on size of word, and adds padding; returns x*/
	public double size(Object v, double x) {
		graphComponent.getGraph().updateCellSize(v);
		double width = graphComponent.getGraph().getModel().getGeometry(v).getWidth() + 20;
		graphComponent.getGraph().getModel().getGeometry(v).setWidth(width);
		graphComponent.getGraph().getModel().getGeometry(v).setHeight(graphComponent.getGraph().getModel().getGeometry(v).getHeight() + 10);
		x += width + 20; // increments the x position of the next vertex to be added by the width of this vertex plus 20
		return x;
	}
	
	/**draws the nodes in a graph that has a sentence taken from a conll file*/
	public mxGraph createConllGraph(int index) {
		// List of objects added to graph
		// Make nodes from list of strings
		mxGraph graph = createAndConfigureMxGraph();
		graph.getModel().beginUpdate();
		try {
			drawConllWords(sentences.get(index), graph, index);
			System.out.println("called draw conll words");
		} finally {
			graph.getModel().endUpdate();
			graphComponent.refresh();
		}

		return graph;
	}

	/**draws the nodes (from a regular sentence) in a graph at the top of the screen, one after the other*/
	public mxGraph createGraph(List<ConllWord> words, int index) {
		HashMap<Object, ConllWord> hm = new HashMap<>();
		// List of objects added to graph
		// Make nodes from list of strings
		mxGraph graph = createAndConfigureMxGraph();
		final int numWords = words.size();
		double x = 0; // every time a word is added, its width plus 20 is added to this
		for (int j = 0; j < numWords; j++) {
			// "<html><body><table><tr><td>"+words.get(j)+"</td></tr><tr><td>BLAH</td></tr></table></body>"
			// "<table><tr><td>"+words.get(j)+"</td></tr><tr><td>BLAH</td></tr></table>"
			graph.getModel().beginUpdate();
			try {
				Object v = graph.insertVertex(graph.getDefaultParent(), null, words.get(j).getWord(), x, 10, 80, 30, "fillColor=white;strokeColor=black;fontColor=black;strokeWidth=0.5"); //shape=triangle;
				x = size(v, x);
				hm.put(v, new ConllWord(j, words.get(j).getWord(), "_", "_", 0, "_"));
			} finally {
				graph.getModel().endUpdate();
				graphComponent.refresh();
			}
		}
		if (hm.size() > 0)
			cwOfVerts.set(index, hm);
		return graph;
	}

	/** returns all parent/source vertices with the object values of their outgoing edges (to childVertex)*/
	public HashMap<Object, Object> getPVandEL(Object childVertex) {
		HashMap<Object, Object> parents = new HashMap<Object, Object>();
		for (Object p : graphComponent.getGraph().getIncomingEdges(childVertex)) {
			mxICell vertSource = ((mxCell) p).getSource();
			parents.put(vertSource, graphComponent.getGraph().getModel().getValue(p));
		}
		return parents;
	}

	/** gets all child/target vertices with the object values of their incoming edges (from parentVertex)*/
	public HashMap<Object, Object> getCVandEL(Object parentVertex) {
		HashMap<Object, Object> children = new HashMap<>();
		for (Object c : graphComponent.getGraph().getOutgoingEdges(parentVertex)) {
			mxICell tV = ((mxCell) c).getTarget();
			children.put(tV, graphComponent.getGraph().getModel().getValue(c));
		}
		return children;
	}

}

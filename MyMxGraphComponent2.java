package dt;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.examples.swing.editor.BasicGraphEditor;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellHandler;
import com.mxgraph.swing.handler.mxElbowEdgeHandler;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.handler.mxVertexHandler;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;
import com.mxgraph.view.mxEdgeStyle.mxEdgeStyleFunction;

/**Latest version.*/
public class MyMxGraphComponent2 extends mxGraphComponent {

	private static final long serialVersionUID = 1L;
	private DTE9 dte;
	
	/** While drag arrows from one cell to another is true, this holds the value of the vertex that an arrow is being drawn from */
	public Object arrowFrom = null;
	
	/** Stores the value of the basic graph editor */
	public MyBasicGraphEditor2 editor;
	
	/** MutableBooleans that store the value of options that can be enabled or disabled (see MyEditorMenuBar2 => Options) */
	public MutableBoolean mAllowEdgeDeletion = new MutableBoolean(true), mDoubleClick = new MutableBoolean(true), mPlusMinus = new MutableBoolean(true), mMoveCellsOnDrag = new MutableBoolean(true);
	
	/** Lists that hold the value of all the arrow labels, all the POS tags, and all the possible shapes */
	public List<String> mArrowLabels, mPOSTags, mAllShapes = new ArrayList<>(); 
	
	/** <b>mPOSTagsToShapesAndColors</b> maps POS tags to their corresponding shape names.*/ 
	public Map<String, String[]> mPOSTagsToShapesAndColors = new HashMap<>();
	
	/** <b>mColors</b> maps color names to their ligher and darker RGB values (darker values used for nodes with collapsed chlidren)*/ 
	public Map<String, String[]> mColors = new HashMap<>();
	
	/** 
	 * Used to determine whether or not a cell is being dragged. Cannot use mouseDragged because it doesn't work. This happens because as a result of to platform-dependent 
	 * Drag&Drop implementations, MOUSE_DRAGGED events may not be delivered during a native Drag&Drop operation.
	 * 
	 * @TODO RESEARCH TRANSFER HANDLER and DRAP AND DROP IN JAVA and use that instead of creating a new thread and using that to check whether or not
	 * the mouse is still being dragged
	 */
	volatile private boolean mouseDown = false, isRunning = false;
	private int x, y, distance = 20;
	private Object selected = null, over = null;

	public MyMxGraphComponent2(DTE9 DTE9, mxGraph graph, List<String> arrowLabels, List<String> posTags) {
		super(graph);
		mArrowLabels = arrowLabels;
		mPOSTags = posTags;
		dte = DTE9;
		setToolTips(true);
		getGraphHandler().setRemoveCellsFromParent(false);
		// populates mAllShapes and mColors
		populateShapesAndColors();
		
		getGraphControl().addMouseMotionListener(new MouseMotionListener() {
			@Override        
			public void mouseDragged(MouseEvent e) {
				// if mMoveCellsOnDrag is true (see MyEditorMenuBar2 => Options)  and no cell is selected and the 
				// mouse wasn't over a cell when the mouse was first pressed down (over and selected are obtained 
				// in the method mousePressed) then move all the cells with the mouse by translating them the distance that
				// the mouse was dragged (x and y, the coordinates of the mouse, are first obtained in the method mousePressed
				// and are updated below
				if (over == null && selected == null && mMoveCellsOnDrag.getValue()) {
					getGraph().moveCells(getGraph().getChildCells(getGraph().getDefaultParent()), e.getX()-x, e.getY()-y);
					x = e.getX();
					y = e.getY();
				}
			}        
			@Override        
			public void mouseMoved(MouseEvent e) {
				// do nothing
			}    
	});
		
		// when an arrow is drawn between two nodes, this is called
		getConnectionHandler().addListener(mxEvent.CONNECT, new mxIEventListener() {
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				System.out.println("mxEvent.CONNECT called");
				
				// gets the edge that was created, its source node and its target node
				mxIGraphModel model = getGraph().getModel();
				Object edge = evt.getProperty("cell");
				Object source = model.getTerminal(edge, true);
				Object target = model.getTerminal(edge, false);
				
				// if the target is not null
				if (target != null) {
					System.out.println("parent vertex: " + source + " child vertex: " + target);
					// Calls orderChildren, which, if the source node has more than one child, removes all the children,
					// redraws them in initial order in the sentence, calls arrowDrawn on the new child, and returns true.
					// If orderChildren returns false, arrowDrawn is called on the new child.
					if (!dte.graphComponent.orderChildren(source, target)) {
						arrowDrawn(target, edge);
					}
				// if the target is null, remove the edge
				} else {
					model.remove(edge);
				}
				
				// format if automatically format is true
				formatAsNeeded();
			}
		});
		
	}
	 
	/**Each node can only have one parent; removes other edges going towards the vertex that aren't
		the edge that was just created. Makes font color & line color of the arrow black. */
	private void arrowDrawn(Object target, Object edge) {
		getGraph().setCellStyles(mxConstants.STYLE_FONTCOLOR, "black", new Object[] { edge });
		getGraph().setCellStyles(mxConstants.STYLE_STROKECOLOR, "black", new Object[] { edge });
				
		if (getGraph().getIncomingEdges(target).length>1) {
			System.out.println("more than one incoming edge");
			for (Object ie : getGraph().getIncomingEdges(target)) {
				if (ie != edge) {
					System.out.println("incoming edge not equal to edge just created; to be deleted");
					getGraph().getModel().remove(ie);
				}
			}
		}
	}
	
	/** Populates the list mAllShapes with all possible shapes, and populates mColors with the names of the colors used
	 * by the program and maps them to their lighter and darker RGB values*/
	public void populateShapesAndColors() {
		mAllShapes.addAll(Arrays.asList(new String[] {"rectangle", "cylinder", "doubleEllipse", "doubleRectangle", "ellipse", "hexagon", "cloud", "rhombus", "triangle"}));
		String[] colorRGBs = {"ffffff", "ffb5b5", "ffcc99", "ffffcc", "ccffcc", "cceeff", "ccccff", "ffddff"};
		String[] darkRGBs = {"dddddd", "ff8888", "ff9955", "ffff88", "99dd99", "88bbff", "9999ff", "ff99ff"};
		String[] colorNames = {"white", "red", "orange", "yellow", "green", "blue",  "purple",  "pink"};
		for (int i = 0; i < colorNames.length; i++) {
			mColors.put(colorNames[i], new String[] {colorRGBs[i], darkRGBs[i]});
		}
	}
	
	/** Called by DTE9 when cells are moved */
	public void cellsMoved() {
		formatAsNeeded();
		mouseDown = false;
		refresh();
	}
	
	/** Adds new arrow labels to the list of all arrow labels */
	public void addArrowLabels(Collection<String> arrowLabels) {
		mArrowLabels.addAll(arrowLabels);
	}
	
	/** Removes old arrow labels and adds new arrow labels to the list of all arrow labels */
	public void setArrowLabels(Collection<String> arrowLabels) {
		mArrowLabels = new ArrayList<String>();
		mArrowLabels.addAll(arrowLabels);
	}
	
	/** Adds new POS tags to the list of all POS tags */
	public void addPOSTags(Collection<String> pos) {
		mPOSTags.addAll(pos);
	}
	
	/** Removes old POS tags and adds new POS tags to the list of all POS tags */
	public void setPOSTags(Collection<String> pos) {
		mPOSTags = new ArrayList<String>();
		mPOSTags.addAll(pos);
	}
	
	/** 
	 * Adds a JMenuItem with the value of <b>String s</b> to the pop-up menu that is displayed when the user
	 * double clicks on an arrow. When an item is clicked on, the label of the selected edge (<b>Object c</b>) is changed to <b>s</b>.
	 */
	public JMenuItem addJMI(final String s, final Object c) {
		JMenuItem jmi = new JMenuItem(s);
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent b) {
				MyMxGraphComponent2.this.labelChanged(c, s, null);
			}
		});
		return jmi;
	}
	
	/** Checks if the user has double clicked over the graphComponent*/
	@Override
	protected void installDoubleClickHandler() {
		graphControl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!e.isConsumed() && isEditEvent(e)) {
					final Object cell = getCellAt(e.getX(), e.getY(), false);
					// Do nothing if no cell has been clicked on or if the cell that was clicked on is not a child of the default parent
					// which means that it is an cell within a split word
					if (cell == null) {
						return;
					} else if (((mxCell) cell).getParent() != getGraph().getDefaultParent()) {
						return;
					}
					
					System.out.println("x: " + e.getX() + " y: " + e.getY());
					
					if (getGraph().isCellEditable(cell)) {
						System.out.println("double-clicked on " + graph.getLabel(cell));
						
						// If the cell is an edge, create a pop up menu with the list of all edge labels. 
						// addJMI generates a JMenuItem for each edge label and gives it an actionListener
						// so that if a label is clicked on, the edge's label is changed to that edge label.
						if (getGraph().getModel().isEdge(cell)) {
							System.out.println("cell is edge");
							JPopupMenu jp = new JPopupMenu();
							
						    for (String s : mArrowLabels)
								jp.add(addJMI(s, cell));
						    
							jp.show(MyMxGraphComponent2.this, e.getX(), e.getY());
						} else {
							getGraph().getModel().beginUpdate();
							try {
								// if double click to collapse cells is true, and the cell that was double clicked on has more than one outgoing edge
								// (which means it has "children" that can be collapsed or expanded)
								if (hasCV(cell) && mDoubleClick.getValue()) {
									// Get the colors to change the cell to. If it is has cells within it (its child count is greater than 0, which means that 
									// it is a split cell) then it will always be a default color. Otherwise, if its children are being collapsed it must be set 
									// to a darker color and if its children are being expanded it must be set to a lighter color.
									String[] colors;
									if (graph.getModel().getChildCount(cell) == 0) {
										String posTag = dte.cwOfVerts.get(dte.graphs.indexOf(getGraph())).get(cell).getPOS();
										if (mPOSTagsToShapesAndColors.containsKey(posTag))
											colors = mColors.get(mPOSTagsToShapesAndColors.get(posTag)[1]);
										else
											colors = mColors.get("white");
									} else {
										colors = new String[2];
										colors[0] = "a8c6cf";
										colors[1] = "a8c6cf";
									}
									// If the cell's "children" are expanded, make them visible and set the cell's fillcolor to the darker shade
									// and make its strokewidth thicker
									if (!isCollapsed(cell)) {
										vis(false, cell, false);
										graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, colors[1], new Object[] { cell });
										graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1.5", new Object[] { cell });
									} else {
										// Otherwise, make them invisible and set the cell's fillcolor to the lighter shade
										// and make its strokewidth thinner
										vis(true, cell, false);
										graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, colors[0], new Object[] { cell });
										graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "0.5", new Object[] { cell });
									}
								}
							} finally {
								getGraph().getModel().endUpdate();
								refresh();
							}
						}
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				Object cell = getCellAt(e.getX(), e.getY(), false);
				Object sel = getGraph().getSelectionCell();
				// If drag and drop arrows from one node to another is true (isConnectable) and the mouse clicked above a cell 
				// and didn't select it, an arrow will be drawn from that cell. This saves that cell so that isCellConnectable(Object cell) 
				// can make sure an arrow isn't drawn from it to its parent, grandparent, etc.
				if (isConnectable() && cell != null && sel == null) {
					arrowFrom = cell;
				}
				
				// if move cells on drag is true, store these values to be used when the mouse is dragged
				if (mMoveCellsOnDrag.getValue()) {
					selected = sel;
					over = cell;
					x = e.getX();
					y = e.getY();
				}
				
				// If connect on overlap is true, there is only one selected cell and it is a vertex that is equal to the vertex the mouse is over and the vertex is not a child of a group
				if (cell != null && ((mxCell) cell).isVertex() && cell == sel && dte.conOnOverlap.getValue() && getGraph().getSelectionCells().length==1 && getGraph().getDefaultParent()==((mxCell) cell).getParent()) {
					//System.out.println("selected (x,y): " + getGraph().getCellGeometry(cell).getX() + ", " + getGraph().getCellGeometry(cell).getY());
					mouseDown = true;
					List<Object> children = getAllChildren(sel, new ArrayList<Object>());
					initThread(sel, children);
				} 
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mouseDown = false;
				arrowFrom = null;
				//System.out.println("mouse released (x,y): " + e.getX() + ", " + e.getY());
				formatAsNeeded();
			}
		});
	}
	
	/** 
	 * Method called by initThread, which is run while cells are being dragged in order to implement the drag and drop cells to connect them feature when
	 *  this feature is enabled (see MyEditorMenuBar2 => Options) 
	 */
	private synchronized boolean checkAndMark() {
	    if (isRunning) return false;
	    isRunning = true;
	    return true;
	}
	
	/** 
	 * Method that is called when while cells are being dragged in order to implement the drag and drop cells to connect them feature when
	 * this feature is enabled (see MyEditorMenuBar2 => Options). Since mouseDragged does not work in certain instances, it must check for itself
	 * whether or not the mouse is being dragged, so it has to create a new thread.
	 */
	private void initThread(final Object sel, final List<Object> children) {
	    if (checkAndMark()) {
	        new Thread() {
	        	// Holds the value of the cell closest to the cell being dragged (sel).
	        	Object thick = null;
	            public void run() {
	                while (mouseDown) {
	                	final Point p = getMousePosition();
	                	if (p == null) {
	                		mouseDown = false;
	                		break;
	                	} else {
	                		SwingUtilities.invokeLater(new Runnable() {						
	    						@Override
	    						public void run() {
	    							double x = p.getX(), y = p.getY(); 
	    							// Gets the cell closest to the cell being dragged. If no cell is close to the cell being dragged,
	    							// returns null. This cell (if not null) is given a thick border to indicate to the user that this is the cell that the 
	    							// cell being dragged will be made a "child" of.
	    			                Object over = getOver(sel, x, y, children);

	    			                if (thick == null && over != null) {
	    			                	thick = over;
	    			                	getGraph().setCellStyles(mxConstants.STYLE_STROKEWIDTH, "4", new Object[] { thick });
	    			                // if thick is not null and over is, reset the strokewidth of the cell that was prevoiusly thick
	    			                // and set thick equal to null
	    			                } else if (thick != null && over == null) {
	    			                	setSW(thick);
	    			                	thick = over;
	    			                // and if neither thick nor over are null and they aren't equal
	    			                } else if (thick != null && over != null && thick != over) {
	    			                	// reset the strokewidth of the cell that was previously thick
	    			                	setSW(thick);
	    			                	// set thick equal to over
	    			                	thick = over;
	    			                	// and increase the strokewidth of the new cell that is closest to sel
	    			                	getGraph().setCellStyles(mxConstants.STYLE_STROKEWIDTH, "4", new Object[] { thick });
	    			                }
	    						}
	    					});
			                
	                	}
	                }
	                isRunning = false;
	                SwingUtilities.invokeLater(new Runnable() {						
						@Override
						public void run() {
							// draws the arrow from thick to sel if thick is not null
							drawArrow(thick, sel);
						}
					});
	            }
	        }.start();
	    }
	}
	
	/** Draws an arrow to the cell being dragged (sel) from the cell it was dropped over (thick) and resets the border width of thick. */
	private void drawArrow(Object thick, Object sel) {
		if (thick != null) {
        	getGraph().getModel().beginUpdate();
			try {
				setSW(thick);
				if (!orderChildren(thick, sel)) {
					Object edge = getGraph().insertEdge(getGraph().getDefaultParent(), null, "", thick, sel);
					if (!dte.autoFormat.getValue()) {
						checkOverlap(sel);
					}
					arrowDrawn(sel, edge);
				}
			} finally {
				getGraph().getModel().endUpdate();
				refresh();
			}
			formatAsNeeded();
        }
	}
	
	/** When a cell gets a new "child" cell, this method orders the children of the parent cell so that they are in the order they are in their original sentence. */
	private boolean orderChildren(Object parent, Object newChild) {
		if (hasCV(parent) && dte.autoFormat.getValue()) {
			// list of all children of node that cell was added to
			List<Object> children = getCV(parent);
			children.add(newChild);
			Map<Integer, Object> orderedChildren = new TreeMap<>();
			
			HashMap<Object, ConllWord> cells;
			HashMap<Object, String> arrowTags = new HashMap<>();
			for (Object cell : (cells = dte.cwOfVerts.get(dte.graphs.indexOf(getGraph()))).keySet()) {
				if (children.contains(cell)) {
					orderedChildren.put(new Integer(cells.get(cell).getIndex()), cell);
					children.remove(cell);
					if (cell != newChild) {
						Object value = ((mxCell) getGraph().getIncomingEdges(cell)[0]).getValue();
						arrowTags.put(cell, value==null?"":value.toString());
					} else {
						arrowTags.put(cell, "");
					}
				}
			}
			
			// remove all edges connecting children to parent (will redraw when ordering)
			getGraph().setCellsDeletable(true);
			getGraph().removeCells(getGraph().getOutgoingEdges(parent));
			getGraph().setCellsDeletable(false);
			
			for (Object cell : orderedChildren.values()) {
				Object edge = getGraph().insertEdge(getGraph().getDefaultParent(), null, arrowTags.get(cell), parent, cell, "strokeColor=black;fontColor=black");
				if (cell == newChild) {
					arrowDrawn(newChild, edge); 
				}
			}
			return true;
		} 
		return false;
	}
	
	/** Gets the cell closest to the cell being dragged. If no cell is close to the cell being dragged, returns null. */
	private Object getOver(Object sel, double xM, double yM, List<Object> children) {
		HashMap<Double, Object> near = new HashMap<>();
		
		// Holds value of translation & scale amounts
		Point previousTranslate = canvas.getTranslate();
		double previousScale = canvas.getScale();
		
		try {
			// Accounts for change in x,y coordinates if the graph has been zoomed in or out
			canvas.setScale(graph.getView().getScale());
			canvas.setTranslate(0, 0);
			
			mxGraphView view = graph.getView();
			
			// Gets all the cells that are within 20 units from the cell being dragged (sel)
			// and calculates how far they are from sel using the pythagorean theorem
			for (Object cell : getGraph().getChildVertices(getGraph().getDefaultParent())) {	
				// if the cell is visible and not equal to sel and isn't one of the "children" of the cell
				if (graph.isCellVisible(cell) && sel != cell && !children.contains(cell)) {
					mxCellState state = view.getState(cell);
					double x = state.getX(), y = state.getY(), w = state.getWidth(), h = state.getHeight();
					double l = x - 20, r = x + w + 20;
					double t = y - 20, b = y + h + 20;
					if (l<=xM && r>=xM && t<=yM && b>=yM) {
						double cX = state.getCenterX(), cY = state.getCenterY();
						double distX = Math.abs(xM - cX), distY = Math.abs(yM - cY);
						double dist = Math.sqrt(distX*distX + distY*distY);
	           			near.put(new Double(dist), cell);
	           		} 
				}
				
			}
			
		} finally {
			// Resets translation & scale variables to original values
			canvas.setScale(previousScale);
			canvas.setTranslate(previousTranslate.x, previousTranslate.y);
		}
		
		// If any cells were added to near, return the closest one
		if (near.size()>0) {
			Double min = Collections.min(near.keySet());
			return near.get(min);
		// Otherwise return null
		} else {
			return null;
		}		
	}
	
	/** Makes all "children" of Object v invisible if they are being collapsed or visible if they are being expanded
	 * <br><br>If the minus icon was clicked on to collapse the cell's "children" (indicated by the fact that the boolean gac
	 * will be true), use getAllChildren(v) to loop through because using getCV(v) doens't work for some strange reason even 
	 * though it works when the cell is double clicked on to expand or collapse its "children" and when the plus icon is 
	 * clicked on to expand the cell's "children" */
	private void vis(boolean b, Object v, boolean gac) {
		for (Object cV : gac?getAllChildren(v, new ArrayList<>()):getCV(v)) {
			graph.getModel().setVisible(cV, b);
			
			if (b) {
				String posTag = dte.cwOfVerts.get(dte.graphs.indexOf(getGraph())).get(cV).getPOS();
				String color;
				if (graph.getModel().getChildCount(cV) == 0) {
					color = mColors.get("white")[0]; // default color
				} else {
					color = "a8c6cf";
				}
				String shape = "rectangle";
				if (mPOSTagsToShapesAndColors.containsKey(posTag)) {
					color = mColors.get(mPOSTagsToShapesAndColors.get(posTag)[1])[0];
					shape = mPOSTagsToShapesAndColors.get(posTag)[0];
				}
				graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, color, new Object[] { cV });
				graph.setCellStyles(mxConstants.STYLE_SHAPE, shape, new Object[] { cV });
				graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "0.5", new Object[] { cV });
			}
			
			System.out.println("ALERT: " + ((mxCell) cV).getValue().toString() + " is visible: " + ((mxCell) cV).isVisible());
			vis(b, cV, gac);
		}
	}
	
	/** returns all "children" of the cell given as a parameter; recursive */
	public List<Object> getAllChildren(Object parVert, List<Object> children) {
		for (Object child : getCV(parVert)) {
			children.add(child);
			children = getAllChildren(child, children);
		}
		return children;		
	}
	
	/** returns all "parents" of the cell given as a parameter; recursive */
	public List<Object> getAllParents(Object childVert, List<Object> parents) {
		for (Object parent : getPV(childVert)) {
			parents.add(parent);
			parents = getAllParents(parent, parents);
		}
		return parents;
	}

	/** gets all child/target vertices */
	public List<Object> getCV(Object parentVertex) {
		List<Object> children = new ArrayList<>();
		mxGraph graph = getGraph();
		for (Object oe : graph.getOutgoingEdges(parentVertex)) {
			children.add(graph.getModel().getTerminal(oe, false));
		}
		return children;
	}
	
	/** returns whether or not a cell has child vertices */
	public boolean hasCV(Object pV) {
		if (graph.getOutgoingEdges(pV).length>0)
			return true;
		else
			return false;
	}
	
	/** Returns whether or not a cell's "children" (if it has any) are collapsed (visible); if the cell has no "children", returns false. */
	public boolean isCollapsed(Object cell) {
		mxGraph graph = getGraph();
		Object[] outgoingEdges = graph.getOutgoingEdges(cell);
		if (outgoingEdges.length>0) {
			Object child = graph.getModel().getTerminal(outgoingEdges[0], false);
			if (child == null)
				return false;
			return !((mxCell) child).isVisible();
		}
		
		return false;
	}
	
	/** gets all parent/source vertices */
	public ArrayList<Object> getPV(Object childVertex) {
		ArrayList<Object> parents = new ArrayList<>();
		mxGraph graph = getGraph();
		for (Object ie : graph.getIncomingEdges(childVertex)) {
			parents.add(graph.getModel().getTerminal(ie, true));
		}
		return parents;
	}
	
	/** If the cell has "children" and they are collapsed, set the strokewidth to 1.5; otherwise, set it to 0.5 */
	public void setSW(Object v) {
		if (isCollapsed(v))
			getGraph().setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1.5", new Object[] {v});
		else
			getGraph().setCellStyles(mxConstants.STYLE_STROKEWIDTH, "0.5", new Object[] {v});
	}

	/** 
	 * After a cell is dropped into another one, if autoFormat is not enabled, the cell that was dropped is moved downward so that the user can see
	 * that an arrow has been drawn between the cells. Recursively moves the cell downward if it is overlapping with other cells.
	 */
	public void checkOverlap(Object moved) {
		mxGraph gr = getGraph();
		mxIGraphModel m = gr.getModel();
		mxGeometry mg = m.getGeometry(moved);
		mg.translate(0, 70);
		System.out.println("moving down");
		for (Object v : getGraph().getChildVertices(getGraph().getDefaultParent())) {
			if (moved != v) {
				double xV = getGraph().getModel().getGeometry(v).getCenterX(), yV = getGraph().getModel().getGeometry(v).getCenterY();
				double xMoved = getGraph().getModel().getGeometry(moved).getCenterX(), yMoved = getGraph().getModel().getGeometry(moved).getCenterY();
				if ((xV+20) > xMoved && (xV-20 < xMoved) && (yV+20) > yMoved && (yV-20 < yMoved))
					checkOverlap(moved);
			}
		}
	}
	
	/** Formats the grpah if autoFormat is enabled */
	public void formatAsNeeded() {
		if (dte.autoFormat.getValue())
			format();
	}
	
	/** 
	 * Sends edges backwards after the graph is drawn so that they aren't drawn over the expand/collapse icons (if any) 
	 * (mxGraph.setKeepEdgesInBackground(true) doesn't work) 
	 */
	public void sendEdgesBack() {
		for (Object edge : getGraph().getChildEdges(getGraph().getDefaultParent())) {
			System.out.println("toback");
			getGraph().orderCells(true, new Object[] {edge});
		}
	}
	
	/** Formats the graph */
	public void format() {
		mxGraph graph = getGraph();
		mxCompactTreeLayout ctl = new mxCompactTreeLayout(graph, false);
		if (!dte.rightAngleArrows.getValue())
			ctl.setEdgeRouting(false);
		ctl.setNodeDistance(distance);
		ctl.execute(graph.getDefaultParent(), null);
	}
	
	/** Returns the fixed distance between nodes when the graph is formatted */
	public int getDistance() {
		return distance;
	}
	
	/** Sets the fixed distance between nodes when the graph is formatted */
	public void setDistance(int d) {
		distance = d;
	}
	
	/** Overrides method so that it can return the customized edge handlers if the cell is an edge or a standard vertex handler otherwise */
	public mxCellHandler createHandler(mxCellState state) {
		if (getGraph().getModel().isVertex(state.getCell())) {
			return new mxVertexHandler(this, state);
		} else if (getGraph().getModel().isEdge(state.getCell())) {
			mxEdgeStyleFunction style = getGraph().getView().getEdgeStyle(state, null, null, null);

			if (getGraph().isLoop(state) || style == mxEdgeStyle.ElbowConnector || style == mxEdgeStyle.SideToSide || style == mxEdgeStyle.TopToBottom) {
				return new mxElbowEdgeHandler(this, state);
			}

			return new MyEdgeHandler(this, state, dte.allowEdgeMovement);
		}

		return new mxCellHandler(this, state);
	}
	
	/** 
	 * Moves folding icon to the bottom left hand corner of the cell so that it can be easily clicked on without
	 * accidentally clicking on an edge.
	 */
	@Override
	public Rectangle getFoldingIconBounds(mxCellState state, ImageIcon icon) {
		//System.out.println("get folding icon bounds being called");
		double scale = getGraph().getView().getScale();

		int x = (int) Math.round(state.getX() + state.getWidth() - 10 * scale);
		//int y = (int) Math.round(state.getY() + state.getHeight() - 20 * scale);
		int y = (int) Math.round(state.getY() + state.getHeight()/(4/3) - 10 * scale);
		int w = (int) Math.max(8, icon.getIconWidth() * scale * 1.5);
		int h = (int) Math.max(8, icon.getIconHeight() * scale * 1.5);

		return new Rectangle(x, y, w, h);
	}
	
	/** Returns the folding icon if and only if expanding and collapsing using a plus/minus icon is enabled, the cell is a vertex, and it has "children" */
	@Override
	public ImageIcon getFoldingIcon(mxCellState state) {
		//System.out.println("called get folding icon");	
		Object cell = state.getCell();
		boolean collapsed;
		if (mPlusMinus.getValue() && getGraph().getModel().isVertex(cell) && hasCV(cell)) {
			if (isCollapsed(cell)) {
				collapsed = true;
			} else {
				collapsed = false;
			}
			return (collapsed) ? collapsedIcon : expandedIcon;
		}		

		return null;
	}
	
	/** Overrides method so that it returns the customized class MyMxGraphControl (created below) */
	@Override
	protected mxGraphControl createGraphControl() {
		//System.out.println("calling create graph control");
		return new MyMxGraphControl();
	}
	
	/** Overrides method so that it returns the customized class MyMxGraphHandler (created below) */
	@Override
		protected mxGraphHandler createGraphHandler() {
			return new MyMxGraphHandler(this);
		}
	
	public class MyMxGraphControl extends mxGraphControl {
		/**
		 * 
		 */
		private static final long serialVersionUID = -91498184176465155L;
		
		/** Called when a cell is drawn */
		@Override	
		protected void cellDrawn(mxICanvas canvas, mxCellState state) {
			//System.out.println("cell drawn method being called");
			mxIGraphModel model = graph.getModel();
			mxGraphics2DCanvas g2c = (mxGraphics2DCanvas) canvas;
			Graphics2D g2 = g2c.getGraphics();
	
			// Draws the collapse/expand icons
			if (mPlusMinus.getValue() && model.isVertex(state.getCell()) && ((mxCell) state.getCell()).getParent() == getGraph().getDefaultParent() &&  canvas instanceof mxGraphics2DCanvas) {
				ImageIcon icon = getFoldingIcon(state);
	
				if (icon != null) {
					Rectangle bounds = getFoldingIconBounds(state, icon);
					g2.drawImage(icon.getImage(), bounds.x, bounds.y, bounds.width, bounds.height, this);
				}
			} 
		}
	}
	
	public class MyMxGraphHandler extends mxGraphHandler {

		public MyMxGraphHandler(mxGraphComponent graphComponent) {
			super(graphComponent);
		}
		
		/** Called when an icon is clicked on; collapses or expands the cell */
		@Override
		protected void fold(Object cell) {
			if (mPlusMinus.getValue()) {
				System.out.println("folding " + ((mxCell) cell).getValue().toString());
				String[] colors;
				if (graph.getModel().getChildCount(cell) == 0) {
					String posTag = dte.cwOfVerts.get(dte.graphs.indexOf(getGraph())).get(cell).getPOS();
					if (mPOSTagsToShapesAndColors.containsKey(posTag))
						colors = mColors.get(mPOSTagsToShapesAndColors.get(posTag)[1]);
					else
						colors = mColors.get("white");
				} else {
					colors = new String[2];
					colors[0] = "a8c6cf";
					colors[1] = "a8c6cf";
				}
				
				if (getGraph().getModel().isVertex(cell) && hasCV(cell)) {
					if (!isCollapsed(cell)) {
						vis(false, cell, true);
						graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, colors[1], new Object[] { cell });
						graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1.5", new Object[] { cell });
					} else {
						vis(true, cell, false);
						graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, colors[0], new Object[] { cell });
						graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "0.5", new Object[] { cell });
					}
				}	
			}
		}
	}
	
}

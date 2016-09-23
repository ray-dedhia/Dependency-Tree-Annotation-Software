package dt;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.mxgraph.examples.swing.editor.BasicGraphEditor;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellHandler;
import com.mxgraph.swing.handler.mxElbowEdgeHandler;
import com.mxgraph.swing.handler.mxVertexHandler;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxEdgeStyle.mxEdgeStyleFunction;
import dt.MyEditorActions.*;

public class MymxGraphComponent extends mxGraphComponent {

	private static final long serialVersionUID = 1L;
	private DTE8 dte;
	public Object savedTarget = null, savedSource = null;
	public BasicGraphEditor editor;
	public MutableBoolean mAllowEdgeDeletion = new MutableBoolean(true), mAllowCollapsingAndExpanding = new MutableBoolean(true);
	public List<String> mArrowLabels, mPOSTags, mAllShapes = new ArrayList<>(); 
	/** Maps POS tags to their corresponding shape names and color RGB values && darker RGB values (when children collapsed) */
	public Map<String, String[]> mPOSTagsToShapesAndColors = new HashMap<>(), mColors = new HashMap<>();
	/** Maps RGB color values to their corresponding color names */
	volatile private boolean mouseDown = false;
	volatile private boolean isRunning = false;


	public MymxGraphComponent(DTE8 dte8, mxGraph graph, List<String> arrowLabels, List<String> posTags) {
		super(graph);
		mArrowLabels = arrowLabels;
		mPOSTags = posTags;
		dte = dte8;
		setToolTips(true);
		getGraphHandler().setRemoveCellsFromParent(false);
		defaultPopulateShapes();

		/*addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				mxGraph graph = getGraph();
				if (graph.getSelectionCells().length > 0) {
					/*for (Object o : graph.getSelectionCells()) {
						int key = e.getKeyCode();
						//can include check for if within some boundaries (check mouse mover)
						if (key == KeyEvent.VK_LEFT)
							graph.translateCell(o, -5, 0);
						if (key == KeyEvent.VK_RIGHT)
							graph.translateCell(o, 5, 0);
						if (key == KeyEvent.VK_UP)
							graph.translateCell(o, 0, -5);
						if (key == KeyEvent.VK_DOWN)
							graph.translateCell(o, 0, 5);
					}*/
					/*if (e.getKeyCode() == KeyEvent.VK_DELETE && mAllowEdgeDeletion.getValue()) {
						for (Object o : graph.getSelectionCells()) {
							if (getGraph().getModel().isEdge(o)) {
								getGraph().getModel().remove(o);
							}
						}
					}
				}
			}
		});
		*/
		
		getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "delete");
		getActionMap().put("delete", new DeleteSelectedEdgesAction());

		getConnectionHandler().addListener(mxEvent.CONNECT, new mxIEventListener() {
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				System.out.println("mxEvent.CONNECT called");
				mxIGraphModel model = getGraph().getModel();
				Object edge = evt.getProperty("cell");
				Object source = model.getTerminal(edge, true);
				Object target = model.getTerminal(edge, false);
				if (target != null) {
					System.out.println("parent vertex: " + source + " child vertex: " + target);
					arrowDrawn(target, edge);
				} else {
					model.remove(edge);
				}
				
				formatAsNeeded();
			}
		});

		/*getGraphControl().addMouseMotionListener(new MouseMotionListener() { //or new mxMouseAdapter
			@Override        
			public void mouseDragged(MouseEvent e) {
				//Object cell = getCellAt(e.getX(), e.getY(), false);
				//if (getGraph().getSelectionCell() != null) mxGraph
					System.out.println("drag");        
			}        
			@Override        
			public void mouseMoved(MouseEvent e) {                    
				// do nothing
			}    
		});*/
		
	}
	 /**Each node can only have one parent; removes other edges going towards the vertex that aren't
		the edge that was just created. Makes font color of arrow black. */
	private void arrowDrawn(Object target, Object edge) {
		getGraph().setCellStyles(mxConstants.STYLE_FONTCOLOR, "black", new Object[] { edge });
		getGraph().setCellStyles(mxConstants.STYLE_STROKECOLOR, "black", new Object[] { edge });
		//getGraph().setCellStyles(mxConstants.STYLE_EXIT_X, "0.5", new Object[] { edge });
		//getGraph().setCellStyles(mxConstants.STYLE_EXIT_Y, "1", new Object[] { edge });
		//getGraph().setCellStyles(mxConstants.STYLE_EXIT_PERIMETER, "1", new Object[] { edge });
		//getGraph().setCellStyles(mxConstants.STYLE_ENTRY_X, "0", new Object[] { edge });
		//getGraph().setCellStyles(mxConstants.STYLE_ENTRY_Y, "0", new Object[] { edge });
		//getGraph().setCellStyles(mxConstants.STYLE_ENTRY_PERIMETER, "1", new Object[] { edge });
		//exitX=0.5;exitY=1;exitPerimeter=1;entryX=0;entryY=0;entryPerimeter=1 FixedPoints
		
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
	
	public void defaultPopulateShapes() {
		mAllShapes.addAll(Arrays.asList(new String[] {"rectangle", "cylinder", "doubleEllipse", "doubleRectangle", "ellipse", "hexagon", "cloud", "rhombus", "triangle"}));
		//String [] defaultPosTags = {"Noun", "Adjective", "Adverbial Modifier", "Conjunction", "Determiner", "Preposition", "Pronoun", "Verb"};
		String[] colorRGBs = {"ffffff", "ffb5b5", "ffcc99", "ffffcc", "ccffcc", "cceeff", "ccccff", "ffddff"};
		String[] darkRGBs = {"dddddd", "ff8888", "ff9955", "ffff88", "99dd99", "88bbff", "9999ff", "ff99ff"};
		String[] colorNames = {"white", "red", "orange", "yellow", "green", "blue",  "purple",  "pink"};
		for (int i = 0; i < colorNames.length; i++) {
			mColors.put(colorNames[i], new String[] {colorRGBs[i], darkRGBs[i]});
		}
		//for (String pt : mPOSTags) {
		//	mPOSTagsToShapesAndColors.put(pt, new String[] {"rectangle", "white"});
		//	System.out.println("adding: " + pt);
		//}
	}
	
	public void cellsMoved() {
		formatAsNeeded();
		mouseDown = false;
	}
	
	public void addArrowLabels(Collection<String> arrowLabels) {
		mArrowLabels.addAll(arrowLabels);
	}
	
	public void setArrowLabels(Collection<String> arrowLabels) {
		mArrowLabels = new ArrayList<String>();
		mArrowLabels.addAll(arrowLabels);
	}
	
	public void addPOSTags(Collection<String> pos) {
		mPOSTags.addAll(pos);
	}
	
	public void setPOSTags(Collection<String> pos) {
		mPOSTags = new ArrayList<String>();
		mPOSTags.addAll(pos);
	}
	
	public JMenuItem addJMI(final String s, final Object c) {
		JMenuItem jmi = new JMenuItem(s);
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent b) {
				MymxGraphComponent.this.labelChanged(c, s, null);
			}
		});
		return jmi;
	}
	
	@Override
	protected void installDoubleClickHandler() {
		graphControl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//System.out.println("graph has cells: " + (graph.getChildCells(graph.getDefaultParent()).length > 0));
				// System.out.println("info is empty: " + (mInfo.size() == 0));
				if (!e.isConsumed() && isEditEvent(e)) {
					final Object cell = getCellAt(e.getX(), e.getY(), false);
					if (cell == null) {
						return;
					} else if (((mxCell) cell).getParent() != getGraph().getDefaultParent()) {
						return;
					}
					System.out.println("x: " + e.getX() + " y: " + e.getY());
					if (getGraph().isCellEditable(cell)) {
						System.out.println("double-clicked on " + graph.getLabel(cell));
						if (getGraph().getModel().isEdge(cell)) {
							
							System.out.println("cell is edge");
							JPopupMenu jp = new JPopupMenu();
							
						    for (String s : mArrowLabels)
								jp.add(addJMI(s, cell));
						    
							jp.show(MymxGraphComponent.this, e.getX(), e.getY());
						} else {
							getGraph().getModel().beginUpdate();
							try {
								if (getGraph().getOutgoingEdges(cell).length > 0 && mAllowCollapsingAndExpanding.getValue()) {
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
									if (getGraph().getModel().isVisible(getCV(cell).get(0))) {
										vis(false, cell);
										graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, colors[1], new Object[] { cell });
										graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1.5", new Object[] { cell });
									} else {
										vis(true, cell);
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
				if (sel != null && ((mxCell)sel).isEdge()) {
					savedTarget = getGraph().getModel().getTerminal(cell, false);
					savedSource = getGraph().getModel().getTerminal(cell, true);
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
				//System.out.println("mouse released (x,y): " + e.getX() + ", " + e.getY());
				formatAsNeeded();
				for (Object cell : getGraph().getChildEdges(getGraph().getDefaultParent())) {
					Object target = getGraph().getModel().getTerminal(cell, false);
					Object source = getGraph().getModel().getTerminal(cell, true);
					if (target == null) {
						if (savedTarget == null)
							getGraph().getModel().remove(cell);
						else
							getGraph().getModel().setTerminal(cell,  savedTarget,  false);
					} else if (source == null) {
						if (savedSource == null)
							getGraph().getModel().remove(cell);
						else
							getGraph().getModel().setTerminal(cell, savedSource, true);
					}
				}
			}
		});
	}
	
	
	private synchronized boolean checkAndMark() {
	    if (isRunning) return false;
	    isRunning = true;
	    return true;
	}
	
	private void initThread(final Object sel, final List<Object> children) {
	    if (checkAndMark()) {
	        new Thread() {
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
	    			                Object over = getOver(sel, x, y, children);
	    			                if (thick == null && over != null) {
	    			                	thick = over;
	    			                	getGraph().setCellStyles(mxConstants.STYLE_STROKEWIDTH, "4", new Object[] { thick });
	    			                } else if (thick != null && over == null) {
	    			                	setSW(thick);
	    			                	thick = over;
	    			                } else if (thick != null && over != null && thick != over) {
	    			                	setSW(thick);
	    			                	thick = over;
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
							drawArrow(thick, sel);
						}
					});
	            }
	        }.start();
	    }
	}
	
	private void drawArrow(Object thick, Object sel) {
		if (thick != null) {
        	getGraph().getModel().beginUpdate();
			try {
				setSW(thick);
				Object edge = getGraph().insertEdge(getGraph().getDefaultParent(), null, "", thick, sel);
				checkOverlap(sel);
				arrowDrawn(sel, edge);
			} finally {
				getGraph().getModel().endUpdate();
				refresh();
			}
			formatAsNeeded();
        }
	}

	private Object getOver(Object sel, double x, double y, List<Object> children) {
		HashMap<Double, Object> near = new HashMap<>();
		for (Object cell : getGraph().getChildVertices(getGraph().getDefaultParent())) {
        	if (cell != sel && !children.contains(cell)) { //If cell isn't the same as the one selected && isn't a child of the selected cell
           		double l = getGraph().getModel().getGeometry(cell).getX()-20;
           		double r = getGraph().getModel().getGeometry(cell).getX()+getGraph().getModel().getGeometry(cell).getWidth()+20;
           		double t = getGraph().getModel().getGeometry(cell).getY()-20;
           		double b = getGraph().getModel().getGeometry(cell).getY()+getGraph().getModel().getGeometry(cell).getHeight()+20;
           		if (l<=x && r>=x && t<=y && b>=y) {
           			near.put(new Double(Math.abs(getGraph().getModel().getGeometry(cell).getCenterX()-x) + Math.abs(getGraph().getModel().getGeometry(cell).getCenterY()-y)), cell);
           		} 
        	}
        }
		
		/*for (Object cell : dte.groups) { //DON'T NEED TO DO THIS; GROUPS ALREADY CHILD VERTICES OF DEFAULT PARENT
        	if (cell != sel) {
           		double l = getGraph().getModel().getGeometry(cell).getX()-20;
           		double r = getGraph().getModel().getGeometry(cell).getX()+getGraph().getModel().getGeometry(cell).getWidth()+20;
           		double t = getGraph().getModel().getGeometry(cell).getY()-20;
           		double b = getGraph().getModel().getGeometry(cell).getY()+getGraph().getModel().getGeometry(cell).getHeight()+20;
           		if (l<=x && r>=x && t<=y && b>=y) {
           			near.put(new Double(Math.abs((l+20)-x + (t+20)-y)), cell);
           		} 
        	}
        }*/
		
		if (near.size()>0) {
			Double min = Collections.min(near.keySet());
			return near.get(min);
		} else {
			return null;
		}		
	}
	
	private void vis(boolean b, Object v) {
		/*if (b) {
			String posTag = dte.cwOfVerts.get(dte.graphs.indexOf(getGraph())).get(v).getPOS();
			String color = mColors.get("white")[0]; // default color
			if (mPOSTagsToShapesAndColors.containsKey(posTag))
				color = mColors.get(mPOSTagsToShapesAndColors.get(posTag)[1])[0];
			graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, color, new Object[] { v });
			graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "0.5", new Object[] { v });
		}
		boolean last = getCV(v).size()==0;*/
		for (Object cV : getCV(v)) {
			graph.getModel().setVisible(cV, b);
			//if (last) {
				String posTag = dte.cwOfVerts.get(dte.graphs.indexOf(getGraph())).get(cV).getPOS();
				String color = mColors.get("white")[0]; // default color
				String shape = "rectangle";
				if (mPOSTagsToShapesAndColors.containsKey(posTag)) {
					color = mColors.get(mPOSTagsToShapesAndColors.get(posTag)[1])[0];
					shape = mPOSTagsToShapesAndColors.get(posTag)[0];
				}
				graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, color, new Object[] { cV });
				graph.setCellStyles(mxConstants.STYLE_SHAPE, shape, new Object[] { cV });
				graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "0.5", new Object[] { cV });
			//}
			vis(b, cV);
		}
	}
	
	public List<Object> getAllChildren(Object parVert, List<Object> children) {
		for (Object child : getCV(parVert)) {
			children.add(child);
			children = getAllChildren(child, children);
		}
		return children;		
	}

	// gets all child/target vertices
	public List<Object> getCV(Object parentVertex) {
		List<Object> children = new ArrayList<>();
		mxGraph graph = getGraph();
		for (Object oe : graph.getOutgoingEdges(parentVertex)) {
			children.add(graph.getModel().getTerminal(oe, false));
		}
		return children;
	}
	
	// gets all parent/source vertices
	public ArrayList<Object> getPV(Object childVertex) {
		ArrayList<Object> parents = new ArrayList<>();
		for (Object ie : getGraph().getIncomingEdges(childVertex)) {
			parents.add(graph.getModel().getTerminal(ie, false));
		}
		return parents;
	}
	
	public void setSW(Object v) {
		if (getGraph().getOutgoingEdges(v).length>0 && !((mxCell) getCV(v).get(0)).isVisible())
			getGraph().setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1.5", new Object[] {v});
		else
			getGraph().setCellStyles(mxConstants.STYLE_STROKEWIDTH, "0.5", new Object[] {v});
	}

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
		/*for (Object g : dte.groups) { //DON'T NEED TO DO THIS; GROUPS ALREADY CHILD VERTICES OF DEFAULT PARENT
			if (moved != g) {
				if (getGraph().getModel().getGeometry(g).getX() == getGraph().getModel().getGeometry(moved).getX() && getGraph().getModel().getGeometry(g).getY() == getGraph().getModel().getGeometry(moved).getY())
					checkOverlap(moved);
			}
		}*/
	}

	public void formatAsNeeded() {
		if (dte.autoFormat.getValue())
			format();
	}

	public void format() {
		mxGraph graph = getGraph();
		mxCompactTreeLayout ctl = new mxCompactTreeLayout(graph, false);
		if (!dte.rightAngleArrows.getValue())
			ctl.setEdgeRouting(false);
		ctl.setNodeDistance(20);
		ctl.execute(graph.getDefaultParent(), null);
	}

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

	public ImageIcon getFoldingIcon(mxCellState state) {
		return null;
	}
}

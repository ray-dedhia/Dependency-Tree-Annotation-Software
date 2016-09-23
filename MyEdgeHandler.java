package dt;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxEdgeHandler;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxConnectionConstraint;
import com.mxgraph.view.mxGraphView;

/**Latest version.*/
public class MyEdgeHandler extends mxEdgeHandler {
	MutableBoolean allowEdgeMovement;

	public MyEdgeHandler(mxGraphComponent graphComponent, mxCellState state, MutableBoolean aem) {
		super(graphComponent, state);
		allowEdgeMovement = aem;
	}
	
	public void mouseDragged(MouseEvent e)
	{
		if (allowEdgeMovement.getValue()) 
		{
			if (!e.isConsumed() && first != null)
			{
				boolean isSource = isSource(index);
				boolean isTarget = isTarget(index);
				
				if (isSource || isTarget) {
					return;
				}
				
				gridEnabledEvent = graphComponent.isGridEnabledEvent(e);
				constrainedEvent = graphComponent.isConstrainedEvent(e);				
	
				Object source = null;
				Object target = null;
	
				if (isLabel(index))
				{
					mxPoint abs = state.getAbsoluteOffset();
					double dx = abs.getX() - first.x;
					double dy = abs.getY() - first.y;
	
					mxPoint pt = new mxPoint(e.getPoint());
	
					if (gridEnabledEvent)
					{
						pt = graphComponent.snapScaledPoint(pt, dx, dy);
					}
	
					if (constrainedEvent)
					{
						if (Math.abs(e.getX() - first.x) > Math.abs(e.getY()
								- first.y))
						{
							pt.setY(abs.getY());
						}
						else
						{
							pt.setX(abs.getX());
						}
					}
	
					Rectangle rect = getPreviewBounds();
					rect.translate((int) Math.round(pt.getX() - first.x),
							(int) Math.round(pt.getY() - first.y));
					preview.setBounds(rect);
				}
				else
				{
					// Clones the cell state and updates the absolute points using
					// the current state of this handle. This is required for
					// computing the correct perimeter points and edge style.
					mxGeometry geometry = graphComponent.getGraph()
							.getCellGeometry(state.getCell());
					mxCellState clone = (mxCellState) state.clone();
					List<mxPoint> points = geometry.getPoints();
					mxGraphView view = clone.getView();
	
					if (isSource || isTarget)
					{
						marker.process(e);
						mxCellState currentState = marker.getValidState();
						target = state.getVisibleTerminal(!isSource);
	
						if (currentState != null)
						{
							source = currentState.getCell();
						}
						else
						{
							mxPoint pt = new mxPoint(e.getPoint());
	
							if (gridEnabledEvent)
							{
								pt = graphComponent.snapScaledPoint(pt);
							}
	
							clone.setAbsoluteTerminalPoint(pt, isSource);
						}
	
						if (!isSource)
						{
							Object tmp = source;
							source = target;
							target = tmp;
						}
					}
					else
					{
						mxPoint point = convertPoint(new mxPoint(e.getPoint()),
								gridEnabledEvent);
	
						if (points == null)
						{
							points = Arrays.asList(new mxPoint[] { point });
						}
						else if (index - 1 < points.size())
						{
							points = new ArrayList<mxPoint>(points);
							points.set(index - 1, point);
						}
	
						source = view.getVisibleTerminal(state.getCell(), true);
						target = view.getVisibleTerminal(state.getCell(), false);
					}
	
					// Computes the points for the edge style and terminals
					mxCellState sourceState = view.getState(source);
					mxCellState targetState = view.getState(target);
	
					mxConnectionConstraint sourceConstraint = graphComponent
							.getGraph().getConnectionConstraint(clone, sourceState,
									true);
					mxConnectionConstraint targetConstraint = graphComponent
							.getGraph().getConnectionConstraint(clone, targetState,
									false);
	
					if (!isSource || sourceState != null)
					{
						view.updateFixedTerminalPoint(clone, sourceState, true,
								sourceConstraint);
					}
	
					if (!isTarget || targetState != null)
					{
						view.updateFixedTerminalPoint(clone, targetState, false,
								targetConstraint);
					}
	
					view.updatePoints(clone, points, sourceState, targetState);
					view.updateFloatingTerminalPoints(clone, sourceState,
							targetState);
	
					// Uses the updated points from the cloned state to draw the preview
					p = createPoints(clone);
					preview.setBounds(getPreviewBounds());
				}
	
				if (!preview.isVisible()
						&& graphComponent.isSignificant(e.getX() - first.x,
								e.getY() - first.y))
				{
					preview.setVisible(true);
				}
				else if (preview.isVisible())
				{
					preview.repaint();
				}
	
				e.consume();
			}
		}
	}
}

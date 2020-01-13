package weka.gui.graphvisualizer;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import weka.core.FastVector;

public abstract interface LayoutEngine
{
  public abstract void layoutGraph();
  
  public abstract void setNodesEdges(FastVector paramFastVector1, FastVector paramFastVector2);
  
  public abstract void setNodeSize(int paramInt1, int paramInt2);
  
  public abstract FastVector getNodes();
  
  public abstract JPanel getControlPanel();
  
  public abstract JProgressBar getProgressBar();
  
  public abstract void addLayoutCompleteEventListener(LayoutCompleteEventListener paramLayoutCompleteEventListener);
  
  public abstract void removeLayoutCompleteEventListener(LayoutCompleteEventListener paramLayoutCompleteEventListener);
  
  public abstract void fireLayoutCompleteEvent(LayoutCompleteEvent paramLayoutCompleteEvent);
}

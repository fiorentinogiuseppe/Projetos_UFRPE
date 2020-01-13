package weka.clusterers.forOPTICSAndDBScan.OPTICS_GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JComponent;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.DataObject;
import weka.core.FastVector;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;









































































public class GraphPanel
  extends JComponent
  implements RevisionHandler
{
  private static final long serialVersionUID = 7917937528738361470L;
  private FastVector resultVector;
  private int verticalAdjustment;
  private Color coreDistanceColor;
  private Color reachabilityDistanceColor;
  private int widthSlider;
  private boolean showCoreDistances;
  private boolean showReachabilityDistances;
  private int recentIndex = -1;
  






  public GraphPanel(FastVector resultVector, int verticalAdjustment, boolean showCoreDistances, boolean showReachbilityDistances)
  {
    this.resultVector = resultVector;
    this.verticalAdjustment = verticalAdjustment;
    coreDistanceColor = new Color(100, 100, 100);
    reachabilityDistanceColor = Color.orange;
    widthSlider = 5;
    this.showCoreDistances = showCoreDistances;
    showReachabilityDistances = showReachbilityDistances;
    
    addMouseMotionListener(new MouseHandler(null));
  }
  







  protected void paintComponent(Graphics g)
  {
    if (isOpaque()) {
      Dimension size = getSize();
      g.setColor(getBackground());
      g.fillRect(0, 0, width, height);
    }
    
    int stepSize = 0;
    int cDist = 0;
    int rDist = 0;
    
    for (int vectorIndex = 0; vectorIndex < resultVector.size(); vectorIndex++) {
      double coreDistance = ((DataObject)resultVector.elementAt(vectorIndex)).getCoreDistance();
      double reachDistance = ((DataObject)resultVector.elementAt(vectorIndex)).getReachabilityDistance();
      
      if (coreDistance == 2.147483647E9D) {
        cDist = getHeight();
      } else {
        cDist = (int)(coreDistance * verticalAdjustment);
      }
      if (reachDistance == 2.147483647E9D) {
        rDist = getHeight();
      } else {
        rDist = (int)(reachDistance * verticalAdjustment);
      }
      int x = vectorIndex + stepSize;
      
      if (isShowCoreDistances())
      {


        g.setColor(coreDistanceColor);
        g.fillRect(x, getHeight() - cDist, widthSlider, cDist);
      }
      
      if (isShowReachabilityDistances()) {
        int sizer = widthSlider;
        if (!isShowCoreDistances()) { sizer = 0;
        }
        

        g.setColor(reachabilityDistanceColor);
        g.fillRect(x + sizer, getHeight() - rDist, widthSlider, rDist);
      }
      
      if ((isShowCoreDistances()) && (isShowReachabilityDistances())) {
        stepSize += widthSlider * 2;
      } else {
        stepSize += widthSlider;
      }
    }
  }
  


  public void setResultVector(FastVector resultVector)
  {
    this.resultVector = resultVector;
  }
  



  public void setNewToolTip(String toolTip)
  {
    setToolTipText(toolTip);
  }
  



  public void adjustSize(SERObject serObject)
  {
    int i = 0;
    if ((isShowCoreDistances()) && (isShowReachabilityDistances())) {
      i = 10;
    } else if (((isShowCoreDistances()) && (!isShowReachabilityDistances())) || ((!isShowCoreDistances()) && (isShowReachabilityDistances())))
    {
      i = 5; }
    setSize(new Dimension(i * serObject.getDatabaseSize() + serObject.getDatabaseSize(), getHeight()));
    
    setPreferredSize(new Dimension(i * serObject.getDatabaseSize() + serObject.getDatabaseSize(), getHeight()));
  }
  




  public boolean isShowCoreDistances()
  {
    return showCoreDistances;
  }
  



  public void setShowCoreDistances(boolean showCoreDistances)
  {
    this.showCoreDistances = showCoreDistances;
  }
  



  public boolean isShowReachabilityDistances()
  {
    return showReachabilityDistances;
  }
  



  public void setShowReachabilityDistances(boolean showReachabilityDistances)
  {
    this.showReachabilityDistances = showReachabilityDistances;
  }
  



  public void setVerticalAdjustment(int verticalAdjustment)
  {
    this.verticalAdjustment = verticalAdjustment;
  }
  



  public void setCoreDistanceColor(Color coreDistanceColor)
  {
    this.coreDistanceColor = coreDistanceColor;
    repaint();
  }
  



  public void setReachabilityDistanceColor(Color reachabilityDistanceColor)
  {
    this.reachabilityDistanceColor = reachabilityDistanceColor;
    repaint();
  }
  



  private class MouseHandler
    extends MouseMotionAdapter
    implements RevisionHandler
  {
    private MouseHandler() {}
    


    public void mouseMoved(MouseEvent e)
    {
      showToolTip(e.getX());
    }
    




    private boolean showToolTip(int x)
    {
      int i = 0;
      if ((isShowCoreDistances()) && (isShowReachabilityDistances())) {
        i = 11;
      } else if (((isShowCoreDistances()) && (!isShowReachabilityDistances())) || ((!isShowCoreDistances()) && (isShowReachabilityDistances())) || ((!isShowCoreDistances()) && (!isShowReachabilityDistances())))
      {

        i = 6; }
      if (x / i == recentIndex) {
        return false;
      }
      recentIndex = (x / i);
      DataObject dataObject = null;
      try {
        dataObject = (DataObject)resultVector.elementAt(recentIndex);
      }
      catch (Exception e) {}
      if (dataObject != null) {
        if ((!isShowCoreDistances()) && (!isShowReachabilityDistances())) {
          setNewToolTip("<html><body><b>Please select a distance</b></body></html>");
        }
        else
        {
          setNewToolTip("<html><body><table><tr><td>DataObject:</td><td>" + dataObject + "</td></tr>" + "<tr><td>Key:</td><td>" + dataObject.getKey() + "</td></tr>" + "<tr><td>" + (isShowCoreDistances() ? "<b>" : "") + "Core-Distance:" + (isShowCoreDistances() ? "</b>" : "") + "</td><td>" + (isShowCoreDistances() ? "<b>" : "") + (dataObject.getCoreDistance() == 2.147483647E9D ? "UNDEFINED" : Utils.doubleToString(dataObject.getCoreDistance(), 3, 5)) + (isShowCoreDistances() ? "</b>" : "") + "</td></tr>" + "<tr><td>" + (isShowReachabilityDistances() ? "<b>" : "") + "Reachability-Distance:" + (isShowReachabilityDistances() ? "</b>" : "") + "</td><td>" + (isShowReachabilityDistances() ? "<b>" : "") + (dataObject.getReachabilityDistance() == 2.147483647E9D ? "UNDEFINED" : Utils.doubleToString(dataObject.getReachabilityDistance(), 3, 5)) + (isShowReachabilityDistances() ? "</b>" : "") + "</td></tr>" + "</table></body></html>");
        }
      }
      




















      return true;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.4 $");
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

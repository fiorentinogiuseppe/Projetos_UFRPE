package weka.clusterers.forOPTICSAndDBScan.OPTICS_GUI;

import javax.swing.table.AbstractTableModel;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.DataObject;
import weka.core.FastVector;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;


















































public class ResultVectorTableModel
  extends AbstractTableModel
  implements RevisionHandler
{
  private static final long serialVersionUID = -7732711470435549210L;
  private FastVector resultVector;
  
  public ResultVectorTableModel(FastVector resultVector)
  {
    this.resultVector = resultVector;
  }
  








  public int getRowCount()
  {
    if (resultVector == null) {
      return 0;
    }
    return resultVector.size();
  }
  




  public int getColumnCount()
  {
    if (resultVector == null) {
      return 0;
    }
    return 4;
  }
  





  public Object getValueAt(int row, int column)
  {
    DataObject dataObject = (DataObject)resultVector.elementAt(row);
    
    switch (column) {
    case 0: 
      return dataObject.getKey();
    case 1: 
      return dataObject;
    case 2: 
      return dataObject.getCoreDistance() == 2.147483647E9D ? "UNDEFINED" : Utils.doubleToString(dataObject.getCoreDistance(), 3, 5);
    

    case 3: 
      return dataObject.getReachabilityDistance() == 2.147483647E9D ? "UNDEFINED" : Utils.doubleToString(dataObject.getReachabilityDistance(), 3, 5);
    }
    
    
    return "";
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

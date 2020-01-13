package weka.classifiers.trees.adtree;

import java.util.Enumeration;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;











































public class TwoWayNumericSplit
  extends Splitter
{
  private static final long serialVersionUID = 449769177903158283L;
  private int attIndex;
  private double splitPoint;
  private PredictionNode[] children;
  
  public TwoWayNumericSplit(int _attIndex, double _splitPoint)
  {
    attIndex = _attIndex;
    splitPoint = _splitPoint;
    children = new PredictionNode[2];
  }
  





  public int getNumOfBranches()
  {
    return 2;
  }
  







  public int branchInstanceGoesDown(Instance inst)
  {
    if (inst.isMissing(attIndex)) return -1;
    if (inst.value(attIndex) < splitPoint) return 0;
    return 1;
  }
  









  public ReferenceInstances instancesDownBranch(int branch, Instances instances)
  {
    ReferenceInstances filteredInstances = new ReferenceInstances(instances, 1);
    Enumeration e; Enumeration e; if (branch == -1) {
      for (e = instances.enumerateInstances(); e.hasMoreElements();) {
        Instance inst = (Instance)e.nextElement();
        if (inst.isMissing(attIndex)) filteredInstances.addReference(inst);
      } } else { Enumeration e;
      if (branch == 0) {
        for (e = instances.enumerateInstances(); e.hasMoreElements();) {
          Instance inst = (Instance)e.nextElement();
          if ((!inst.isMissing(attIndex)) && (inst.value(attIndex) < splitPoint))
            filteredInstances.addReference(inst);
        }
      } else
        for (e = instances.enumerateInstances(); e.hasMoreElements();) {
          Instance inst = (Instance)e.nextElement();
          if ((!inst.isMissing(attIndex)) && (inst.value(attIndex) >= splitPoint))
            filteredInstances.addReference(inst);
        }
    }
    return filteredInstances;
  }
  







  public String attributeString(Instances dataset)
  {
    return dataset.attribute(attIndex).name();
  }
  








  public String comparisonString(int branchNum, Instances dataset)
  {
    return (branchNum == 0 ? "< " : ">= ") + Utils.doubleToString(splitPoint, 3);
  }
  






  public boolean equalTo(Splitter compare)
  {
    if ((compare instanceof TwoWayNumericSplit)) {
      TwoWayNumericSplit compareSame = (TwoWayNumericSplit)compare;
      return (attIndex == attIndex) && (splitPoint == splitPoint);
    }
    return false;
  }
  






  public void setChildForBranch(int branchNum, PredictionNode childPredictor)
  {
    children[branchNum] = childPredictor;
  }
  






  public PredictionNode getChildForBranch(int branchNum)
  {
    return children[branchNum];
  }
  





  public Object clone()
  {
    TwoWayNumericSplit clone = new TwoWayNumericSplit(attIndex, splitPoint);
    orderAdded = orderAdded;
    if (children[0] != null)
      clone.setChildForBranch(0, (PredictionNode)children[0].clone());
    if (children[1] != null)
      clone.setChildForBranch(1, (PredictionNode)children[1].clone());
    return clone;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}

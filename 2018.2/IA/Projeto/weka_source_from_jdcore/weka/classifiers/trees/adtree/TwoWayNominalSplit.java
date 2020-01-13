package weka.classifiers.trees.adtree;

import java.util.Enumeration;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;












































public class TwoWayNominalSplit
  extends Splitter
{
  private static final long serialVersionUID = -4598366190152721355L;
  private int attIndex;
  private int trueSplitValue;
  private PredictionNode[] children;
  
  public TwoWayNominalSplit(int _attIndex, int _trueSplitValue)
  {
    attIndex = _attIndex;trueSplitValue = _trueSplitValue;
    children = new PredictionNode[2];
  }
  





  public int getNumOfBranches()
  {
    return 2;
  }
  







  public int branchInstanceGoesDown(Instance inst)
  {
    if (inst.isMissing(attIndex)) return -1;
    if (inst.value(attIndex) == trueSplitValue) return 0;
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
          if ((!inst.isMissing(attIndex)) && (inst.value(attIndex) == trueSplitValue))
            filteredInstances.addReference(inst);
        }
      } else
        for (e = instances.enumerateInstances(); e.hasMoreElements();) {
          Instance inst = (Instance)e.nextElement();
          if ((!inst.isMissing(attIndex)) && (inst.value(attIndex) != trueSplitValue))
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
    Attribute att = dataset.attribute(attIndex);
    if (att.numValues() != 2)
      return (branchNum == 0 ? "= " : "!= ") + att.value(trueSplitValue);
    return "= " + (branchNum == 0 ? att.value(trueSplitValue) : att.value(trueSplitValue == 0 ? 1 : 0));
  }
  








  public boolean equalTo(Splitter compare)
  {
    if ((compare instanceof TwoWayNominalSplit)) {
      TwoWayNominalSplit compareSame = (TwoWayNominalSplit)compare;
      return (attIndex == attIndex) && (trueSplitValue == trueSplitValue);
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
    TwoWayNominalSplit clone = new TwoWayNominalSplit(attIndex, trueSplitValue);
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

package weka.classifiers.trees.adtree;

import java.io.Serializable;
import java.util.Enumeration;
import weka.classifiers.trees.ADTree;
import weka.core.FastVector;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;








































public final class PredictionNode
  implements Serializable, Cloneable, RevisionHandler
{
  private static final long serialVersionUID = 6018958856358698814L;
  private double value;
  private FastVector children;
  
  public PredictionNode(double newValue)
  {
    value = newValue;
    children = new FastVector();
  }
  





  public final void setValue(double newValue)
  {
    value = newValue;
  }
  





  public final double getValue()
  {
    return value;
  }
  





  public final FastVector getChildren()
  {
    return children;
  }
  





  public final Enumeration children()
  {
    return children.elements();
  }
  








  public final void addChild(Splitter newChild, ADTree addingTo)
  {
    Splitter oldEqual = null;
    for (Enumeration e = children(); e.hasMoreElements();) {
      Splitter split = (Splitter)e.nextElement();
      if (newChild.equalTo(split)) { oldEqual = split; break;
      } }
    if (oldEqual == null) {
      Splitter addChild = (Splitter)newChild.clone();
      setOrderAddedSubtree(addChild, addingTo);
      children.addElement(addChild);
    }
    else {
      for (int i = 0; i < newChild.getNumOfBranches(); i++) {
        PredictionNode oldPred = oldEqual.getChildForBranch(i);
        PredictionNode newPred = newChild.getChildForBranch(i);
        if ((oldPred != null) && (newPred != null)) {
          oldPred.merge(newPred, addingTo);
        }
      }
    }
  }
  




  public final Object clone()
  {
    PredictionNode clone = new PredictionNode(value);
    for (Enumeration e = children.elements(); e.hasMoreElements();)
      children.addElement((Splitter)((Splitter)e.nextElement()).clone());
    return clone;
  }
  







  public final void merge(PredictionNode merger, ADTree mergingTo)
  {
    value += value;
    for (Enumeration e = merger.children(); e.hasMoreElements();) {
      addChild((Splitter)e.nextElement(), mergingTo);
    }
  }
  






  private final void setOrderAddedSubtree(Splitter addChild, ADTree addingTo)
  {
    orderAdded = addingTo.nextSplitAddedOrder();
    Enumeration e; for (int i = 0; i < addChild.getNumOfBranches(); i++) {
      PredictionNode node = addChild.getChildForBranch(i);
      if (node != null) {
        for (e = node.children(); e.hasMoreElements();) {
          setOrderAddedSubtree((Splitter)e.nextElement(), addingTo);
        }
      }
    }
  }
  


  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}

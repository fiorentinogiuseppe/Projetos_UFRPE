package weka.classifiers.bayes.net.search;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;







































public class SearchAlgorithm
  implements OptionHandler, Serializable, RevisionHandler
{
  static final long serialVersionUID = 6164792240778525312L;
  protected int m_nMaxNrOfParents = 1;
  



  protected boolean m_bInitAsNaiveBayes = true;
  




  protected boolean m_bMarkovBlanketClassifier = false;
  








  public SearchAlgorithm() {}
  







  protected boolean addArcMakesSense(BayesNet bayesNet, Instances instances, int iAttributeHead, int iAttributeTail)
  {
    if (iAttributeHead == iAttributeTail) {
      return false;
    }
    

    if (isArc(bayesNet, iAttributeHead, iAttributeTail)) {
      return false;
    }
    

    int nNodes = instances.numAttributes();
    boolean[] bDone = new boolean[nNodes];
    
    for (int iNode = 0; iNode < nNodes; iNode++) {
      bDone[iNode] = false;
    }
    

    bayesNet.getParentSet(iAttributeHead).addParent(iAttributeTail, instances);
    
    for (int iNode = 0; iNode < nNodes; iNode++)
    {

      boolean bFound = false;
      
      for (int iNode2 = 0; (!bFound) && (iNode2 < nNodes); iNode2++) {
        if (bDone[iNode2] == 0) {
          boolean bHasNoParents = true;
          
          for (int iParent = 0; iParent < bayesNet.getParentSet(iNode2).getNrOfParents(); iParent++) {
            if (bDone[bayesNet.getParentSet(iNode2).getParent(iParent)] == 0) {
              bHasNoParents = false;
            }
          }
          
          if (bHasNoParents) {
            bDone[iNode2] = true;
            bFound = true;
          }
        }
      }
      
      if (!bFound) {
        bayesNet.getParentSet(iAttributeHead).deleteLastParent(instances);
        
        return false;
      }
    }
    
    bayesNet.getParentSet(iAttributeHead).deleteLastParent(instances);
    
    return true;
  }
  














  protected boolean reverseArcMakesSense(BayesNet bayesNet, Instances instances, int iAttributeHead, int iAttributeTail)
  {
    if (iAttributeHead == iAttributeTail) {
      return false;
    }
    

    if (!isArc(bayesNet, iAttributeHead, iAttributeTail)) {
      return false;
    }
    

    int nNodes = instances.numAttributes();
    boolean[] bDone = new boolean[nNodes];
    
    for (int iNode = 0; iNode < nNodes; iNode++) {
      bDone[iNode] = false;
    }
    

    bayesNet.getParentSet(iAttributeTail).addParent(iAttributeHead, instances);
    
    for (int iNode = 0; iNode < nNodes; iNode++)
    {

      boolean bFound = false;
      
      for (int iNode2 = 0; (!bFound) && (iNode2 < nNodes); iNode2++) {
        if (bDone[iNode2] == 0) {
          ParentSet parentSet = bayesNet.getParentSet(iNode2);
          boolean bHasNoParents = true;
          for (int iParent = 0; iParent < parentSet.getNrOfParents(); iParent++) {
            if (bDone[parentSet.getParent(iParent)] == 0)
            {

              if ((iNode2 != iAttributeHead) || (parentSet.getParent(iParent) != iAttributeTail)) {
                bHasNoParents = false;
              }
            }
          }
          
          if (bHasNoParents) {
            bDone[iNode2] = true;
            bFound = true;
          }
        }
      }
      
      if (!bFound) {
        bayesNet.getParentSet(iAttributeTail).deleteLastParent(instances);
        return false;
      }
    }
    
    bayesNet.getParentSet(iAttributeTail).deleteLastParent(instances);
    return true;
  }
  







  protected boolean isArc(BayesNet bayesNet, int iAttributeHead, int iAttributeTail)
  {
    for (int iParent = 0; iParent < bayesNet.getParentSet(iAttributeHead).getNrOfParents(); iParent++) {
      if (bayesNet.getParentSet(iAttributeHead).getParent(iParent) == iAttributeTail) {
        return true;
      }
    }
    
    return false;
  }
  




  public Enumeration listOptions()
  {
    return new Vector(0).elements();
  }
  





  public void setOptions(String[] options)
    throws Exception
  {}
  




  public String[] getOptions()
  {
    return new String[0];
  }
  




  public String toString()
  {
    return "SearchAlgorithm\n";
  }
  









  public void buildStructure(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    if (m_bInitAsNaiveBayes) {
      int iClass = instances.classIndex();
      

      for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
        if (iAttribute != iClass) {
          bayesNet.getParentSet(iAttribute).addParent(iClass, instances);
        }
      }
    }
    search(bayesNet, instances);
    if (m_bMarkovBlanketClassifier) {
      doMarkovBlanketCorrection(bayesNet, instances);
    }
  }
  








  protected void search(BayesNet bayesNet, Instances instances)
    throws Exception
  {}
  







  protected void doMarkovBlanketCorrection(BayesNet bayesNet, Instances instances)
  {
    int iClass = instances.classIndex();
    ParentSet ancestors = new ParentSet();
    int nOldSize = 0;
    ancestors.addParent(iClass, instances);
    while (nOldSize != ancestors.getNrOfParents()) {
      nOldSize = ancestors.getNrOfParents();
      for (int iNode = 0; iNode < nOldSize; iNode++) {
        int iCurrent = ancestors.getParent(iNode);
        ParentSet p = bayesNet.getParentSet(iCurrent);
        for (int iParent = 0; iParent < p.getNrOfParents(); iParent++) {
          if (!ancestors.contains(p.getParent(iParent))) {
            ancestors.addParent(p.getParent(iParent), instances);
          }
        }
      }
    }
    for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
      boolean bIsInMarkovBoundary = (iAttribute == iClass) || (bayesNet.getParentSet(iAttribute).contains(iClass)) || (bayesNet.getParentSet(iClass).contains(iAttribute));
      

      for (int iAttribute2 = 0; (!bIsInMarkovBoundary) && (iAttribute2 < instances.numAttributes()); iAttribute2++) {
        bIsInMarkovBoundary = (bayesNet.getParentSet(iAttribute2).contains(iAttribute)) && (bayesNet.getParentSet(iAttribute2).contains(iClass));
      }
      

      if (!bIsInMarkovBoundary) {
        if (ancestors.contains(iAttribute)) {
          if (bayesNet.getParentSet(iClass).getCardinalityOfParents() < 1024) {
            bayesNet.getParentSet(iClass).addParent(iAttribute, instances);
          }
          
        }
        else {
          bayesNet.getParentSet(iAttribute).addParent(iClass, instances);
        }
      }
    }
  }
  



  protected void setMarkovBlanketClassifier(boolean bMarkovBlanketClassifier)
  {
    m_bMarkovBlanketClassifier = bMarkovBlanketClassifier;
  }
  



  protected boolean getMarkovBlanketClassifier()
  {
    return m_bMarkovBlanketClassifier;
  }
  


  public String maxNrOfParentsTipText()
  {
    return "Set the maximum number of parents a node in the Bayes net can have. When initialized as Naive Bayes, setting this parameter to 1 results in a Naive Bayes classifier. When set to 2, a Tree Augmented Bayes Network (TAN) is learned, and when set >2, a Bayes Net Augmented Bayes Network (BAN) is learned. By setting it to a value much larger than the number of nodes in the network (the default of 100000 pretty much guarantees this), no restriction on the number of parents is enforced";
  }
  








  public String initAsNaiveBayesTipText()
  {
    return "When set to true (default), the initial network used for structure learning is a Naive Bayes Network, that is, a network with an arrow from the classifier node to each other node. When set to false, an empty network is used as initial network structure";
  }
  





  protected String markovBlanketClassifierTipText()
  {
    return "When set to true (default is false), after a network structure is learned a Markov Blanket correction is applied to the network structure. This ensures that all nodes in the network are part of the Markov blanket of the classifier node.";
  }
  







  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
}

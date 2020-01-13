package weka.classifiers.bayes.net.search.local;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;




































































public class HillClimber
  extends LocalScoreSearchAlgorithm
{
  static final long serialVersionUID = 4322783593818122403L;
  public HillClimber() {}
  
  class Operation
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -4880888790432547895L;
    static final int OPERATION_ADD = 0;
    static final int OPERATION_DEL = 1;
    static final int OPERATION_REVERSE = 2;
    public int m_nTail;
    public int m_nHead;
    public int m_nOperation;
    
    public Operation() {}
    
    public Operation(int nTail, int nHead, int nOperation)
    {
      m_nHead = nHead;
      m_nTail = nTail;
      m_nOperation = nOperation;
    }
    


    public boolean equals(Operation other)
    {
      if (other == null) {
        return false;
      }
      return (m_nOperation == m_nOperation) && (m_nHead == m_nHead) && (m_nTail == m_nTail);
    }
    












    public double m_fDeltaScore = -1.0E100D;
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.9 $");
    }
  }
  


  class Cache
    implements RevisionHandler
  {
    double[][] m_fDeltaScoreAdd;
    
    double[][] m_fDeltaScoreDel;
    

    Cache(int nNrOfNodes)
    {
      m_fDeltaScoreAdd = new double[nNrOfNodes][nNrOfNodes];
      m_fDeltaScoreDel = new double[nNrOfNodes][nNrOfNodes];
    }
    



    public void put(HillClimber.Operation oOperation, double fValue)
    {
      if (m_nOperation == 0) {
        m_fDeltaScoreAdd[m_nTail][m_nHead] = fValue;
      } else {
        m_fDeltaScoreDel[m_nTail][m_nHead] = fValue;
      }
    }
    



    public double get(HillClimber.Operation oOperation)
    {
      switch (m_nOperation) {
      case 0: 
        return m_fDeltaScoreAdd[m_nTail][m_nHead];
      case 1: 
        return m_fDeltaScoreDel[m_nTail][m_nHead];
      case 2: 
        return m_fDeltaScoreDel[m_nTail][m_nHead] + m_fDeltaScoreAdd[m_nHead][m_nTail];
      }
      
      
      return 0.0D;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.9 $");
    }
  }
  

  Cache m_Cache = null;
  

  boolean m_bUseArcReversal = false;
  







  protected void search(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    initCache(bayesNet, instances);
    

    Operation oOperation = getOptimalOperation(bayesNet, instances);
    while ((oOperation != null) && (m_fDeltaScore > 0.0D)) {
      performOperation(bayesNet, instances, oOperation);
      oOperation = getOptimalOperation(bayesNet, instances);
    }
    

    m_Cache = null;
  }
  








  void initCache(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    double[] fBaseScores = new double[instances.numAttributes()];
    int nNrOfAtts = instances.numAttributes();
    
    m_Cache = new Cache(nNrOfAtts);
    
    for (int iAttribute = 0; iAttribute < nNrOfAtts; iAttribute++) {
      updateCache(iAttribute, nNrOfAtts, bayesNet.getParentSet(iAttribute));
    }
    

    for (int iAttribute = 0; iAttribute < nNrOfAtts; iAttribute++) {
      fBaseScores[iAttribute] = calcNodeScore(iAttribute);
    }
    
    for (int iAttributeHead = 0; iAttributeHead < nNrOfAtts; iAttributeHead++) {
      for (int iAttributeTail = 0; iAttributeTail < nNrOfAtts; iAttributeTail++) {
        if (iAttributeHead != iAttributeTail) {
          Operation oOperation = new Operation(iAttributeTail, iAttributeHead, 0);
          m_Cache.put(oOperation, calcScoreWithExtraParent(iAttributeHead, iAttributeTail) - fBaseScores[iAttributeHead]);
        }
      }
    }
  }
  






  boolean isNotTabu(Operation oOperation)
  {
    return true;
  }
  







  Operation getOptimalOperation(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    Operation oBestOperation = new Operation();
    

    oBestOperation = findBestArcToAdd(bayesNet, instances, oBestOperation);
    
    oBestOperation = findBestArcToDelete(bayesNet, instances, oBestOperation);
    
    if (getUseArcReversal()) {
      oBestOperation = findBestArcToReverse(bayesNet, instances, oBestOperation);
    }
    

    if (m_fDeltaScore == -1.0E100D) {
      return null;
    }
    
    return oBestOperation;
  }
  








  void performOperation(BayesNet bayesNet, Instances instances, Operation oOperation)
    throws Exception
  {
    switch (m_nOperation) {
    case 0: 
      applyArcAddition(bayesNet, m_nHead, m_nTail, instances);
      if (bayesNet.getDebug()) {
        System.out.print("Add " + m_nHead + " -> " + m_nTail);
      }
      break;
    case 1: 
      applyArcDeletion(bayesNet, m_nHead, m_nTail, instances);
      if (bayesNet.getDebug()) {
        System.out.print("Del " + m_nHead + " -> " + m_nTail);
      }
      break;
    case 2: 
      applyArcDeletion(bayesNet, m_nHead, m_nTail, instances);
      applyArcAddition(bayesNet, m_nTail, m_nHead, instances);
      if (bayesNet.getDebug()) {
        System.out.print("Rev " + m_nHead + " -> " + m_nTail);
      }
      


      break;
    }
    
  }
  



  void applyArcAddition(BayesNet bayesNet, int iHead, int iTail, Instances instances)
  {
    ParentSet bestParentSet = bayesNet.getParentSet(iHead);
    bestParentSet.addParent(iTail, instances);
    updateCache(iHead, instances.numAttributes(), bestParentSet);
  }
  






  void applyArcDeletion(BayesNet bayesNet, int iHead, int iTail, Instances instances)
  {
    ParentSet bestParentSet = bayesNet.getParentSet(iHead);
    bestParentSet.deleteParent(iTail, instances);
    updateCache(iHead, instances.numAttributes(), bestParentSet);
  }
  










  Operation findBestArcToAdd(BayesNet bayesNet, Instances instances, Operation oBestOperation)
  {
    int nNrOfAtts = instances.numAttributes();
    
    for (int iAttributeHead = 0; iAttributeHead < nNrOfAtts; iAttributeHead++) {
      if (bayesNet.getParentSet(iAttributeHead).getNrOfParents() < m_nMaxNrOfParents) {
        for (int iAttributeTail = 0; iAttributeTail < nNrOfAtts; iAttributeTail++) {
          if (addArcMakesSense(bayesNet, instances, iAttributeHead, iAttributeTail)) {
            Operation oOperation = new Operation(iAttributeTail, iAttributeHead, 0);
            if ((m_Cache.get(oOperation) > m_fDeltaScore) && 
              (isNotTabu(oOperation))) {
              oBestOperation = oOperation;
              m_fDeltaScore = m_Cache.get(oOperation);
            }
          }
        }
      }
    }
    
    return oBestOperation;
  }
  








  Operation findBestArcToDelete(BayesNet bayesNet, Instances instances, Operation oBestOperation)
  {
    int nNrOfAtts = instances.numAttributes();
    
    for (int iNode = 0; iNode < nNrOfAtts; iNode++) {
      ParentSet parentSet = bayesNet.getParentSet(iNode);
      for (int iParent = 0; iParent < parentSet.getNrOfParents(); iParent++) {
        Operation oOperation = new Operation(parentSet.getParent(iParent), iNode, 1);
        if ((m_Cache.get(oOperation) > m_fDeltaScore) && 
          (isNotTabu(oOperation))) {
          oBestOperation = oOperation;
          m_fDeltaScore = m_Cache.get(oOperation);
        }
      }
    }
    
    return oBestOperation;
  }
  









  Operation findBestArcToReverse(BayesNet bayesNet, Instances instances, Operation oBestOperation)
  {
    int nNrOfAtts = instances.numAttributes();
    
    for (int iNode = 0; iNode < nNrOfAtts; iNode++) {
      ParentSet parentSet = bayesNet.getParentSet(iNode);
      for (int iParent = 0; iParent < parentSet.getNrOfParents(); iParent++) {
        int iTail = parentSet.getParent(iParent);
        
        if ((reverseArcMakesSense(bayesNet, instances, iNode, iTail)) && (bayesNet.getParentSet(iTail).getNrOfParents() < m_nMaxNrOfParents))
        {

          Operation oOperation = new Operation(parentSet.getParent(iParent), iNode, 2);
          if ((m_Cache.get(oOperation) > m_fDeltaScore) && 
            (isNotTabu(oOperation))) {
            oBestOperation = oOperation;
            m_fDeltaScore = m_Cache.get(oOperation);
          }
        }
      }
    }
    
    return oBestOperation;
  }
  







  void updateCache(int iAttributeHead, int nNrOfAtts, ParentSet parentSet)
  {
    double fBaseScore = calcNodeScore(iAttributeHead);
    int nNrOfParents = parentSet.getNrOfParents();
    for (int iAttributeTail = 0; iAttributeTail < nNrOfAtts; iAttributeTail++) {
      if (iAttributeTail != iAttributeHead) {
        if (!parentSet.contains(iAttributeTail))
        {
          if (nNrOfParents < m_nMaxNrOfParents) {
            Operation oOperation = new Operation(iAttributeTail, iAttributeHead, 0);
            m_Cache.put(oOperation, calcScoreWithExtraParent(iAttributeHead, iAttributeTail) - fBaseScore);
          }
        }
        else {
          Operation oOperation = new Operation(iAttributeTail, iAttributeHead, 1);
          m_Cache.put(oOperation, calcScoreWithMissingParent(iAttributeHead, iAttributeTail) - fBaseScore);
        }
      }
    }
  }
  





  public void setMaxNrOfParents(int nMaxNrOfParents)
  {
    m_nMaxNrOfParents = nMaxNrOfParents;
  }
  




  public int getMaxNrOfParents()
  {
    return m_nMaxNrOfParents;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tMaximum number of parents", "P", 1, "-P <nr of parents>"));
    newVector.addElement(new Option("\tUse arc reversal operation.\n\t(default false)", "R", 0, "-R"));
    newVector.addElement(new Option("\tInitial structure is empty (instead of Naive Bayes)", "N", 0, "-N"));
    
    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  




























  public void setOptions(String[] options)
    throws Exception
  {
    setUseArcReversal(Utils.getFlag('R', options));
    
    setInitAsNaiveBayes(!Utils.getFlag('N', options));
    
    String sMaxNrOfParents = Utils.getOption('P', options);
    if (sMaxNrOfParents.length() != 0) {
      setMaxNrOfParents(Integer.parseInt(sMaxNrOfParents));
    } else {
      setMaxNrOfParents(100000);
    }
    
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[7 + superOptions.length];
    int current = 0;
    if (getUseArcReversal()) {
      options[(current++)] = "-R";
    }
    
    if (!getInitAsNaiveBayes()) {
      options[(current++)] = "-N";
    }
    
    options[(current++)] = "-P";
    options[(current++)] = ("" + m_nMaxNrOfParents);
    

    for (int iOption = 0; iOption < superOptions.length; iOption++) {
      options[(current++)] = superOptions[iOption];
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public void setInitAsNaiveBayes(boolean bInitAsNaiveBayes)
  {
    m_bInitAsNaiveBayes = bInitAsNaiveBayes;
  }
  




  public boolean getInitAsNaiveBayes()
  {
    return m_bInitAsNaiveBayes;
  }
  


  public boolean getUseArcReversal()
  {
    return m_bUseArcReversal;
  }
  


  public void setUseArcReversal(boolean bUseArcReversal)
  {
    m_bUseArcReversal = bUseArcReversal;
  }
  



  public String globalInfo()
  {
    return "This Bayes Network learning algorithm uses a hill climbing algorithm adding, deleting and reversing arcs. The search is not restricted by an order on the variables (unlike K2). The difference with B and B2 is that this hill climber also considers arrows part of the naive Bayes structure for deletion.";
  }
  





  public String useArcReversalTipText()
  {
    return "When set to true, the arc reversal operation is used in the search.";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
}

package weka.classifiers.bayes.net.search.global;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.classifiers.bayes.net.search.SearchAlgorithm;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
























































public class GlobalScoreSearchAlgorithm
  extends SearchAlgorithm
{
  static final long serialVersionUID = 7341389867906199781L;
  BayesNet m_BayesNet;
  boolean m_bUseProb = true;
  

  int m_nNrOfFolds = 10;
  

  static final int LOOCV = 0;
  

  static final int KFOLDCV = 1;
  
  static final int CUMCV = 2;
  
  public static final Tag[] TAGS_CV_TYPE = { new Tag(0, "LOO-CV"), new Tag(1, "k-Fold-CV"), new Tag(2, "Cumulative-CV") };
  







  int m_nCVType = 0;
  


  public GlobalScoreSearchAlgorithm() {}
  


  public double calcScore(BayesNet bayesNet)
    throws Exception
  {
    switch (m_nCVType) {
    case 0: 
      return leaveOneOutCV(bayesNet);
    case 2: 
      return cumulativeCV(bayesNet);
    case 1: 
      return kFoldCV(bayesNet, m_nNrOfFolds);
    }
    throw new Exception("Unrecognized cross validation type encountered: " + m_nCVType);
  }
  







  public double calcScoreWithExtraParent(int nNode, int nCandidateParent)
    throws Exception
  {
    ParentSet oParentSet = m_BayesNet.getParentSet(nNode);
    Instances instances = m_BayesNet.m_Instances;
    

    for (int iParent = 0; iParent < oParentSet.getNrOfParents(); iParent++) {
      if (oParentSet.getParent(iParent) == nCandidateParent) {
        return -1.0E100D;
      }
    }
    

    oParentSet.addParent(nCandidateParent, instances);
    

    double fAccuracy = calcScore(m_BayesNet);
    

    oParentSet.deleteLastParent(instances);
    
    return fAccuracy;
  }
  







  public double calcScoreWithMissingParent(int nNode, int nCandidateParent)
    throws Exception
  {
    ParentSet oParentSet = m_BayesNet.getParentSet(nNode);
    Instances instances = m_BayesNet.m_Instances;
    

    if (!oParentSet.contains(nCandidateParent)) {
      return -1.0E100D;
    }
    

    int iParent = oParentSet.deleteParent(nCandidateParent, instances);
    

    double fAccuracy = calcScore(m_BayesNet);
    

    oParentSet.addParent(nCandidateParent, iParent, instances);
    
    return fAccuracy;
  }
  






  public double calcScoreWithReversedParent(int nNode, int nCandidateParent)
    throws Exception
  {
    ParentSet oParentSet = m_BayesNet.getParentSet(nNode);
    ParentSet oParentSet2 = m_BayesNet.getParentSet(nCandidateParent);
    Instances instances = m_BayesNet.m_Instances;
    

    if (!oParentSet.contains(nCandidateParent)) {
      return -1.0E100D;
    }
    

    int iParent = oParentSet.deleteParent(nCandidateParent, instances);
    oParentSet2.addParent(nNode, instances);
    

    double fAccuracy = calcScore(m_BayesNet);
    

    oParentSet2.deleteLastParent(instances);
    oParentSet.addParent(nCandidateParent, iParent, instances);
    
    return fAccuracy;
  }
  






  public double leaveOneOutCV(BayesNet bayesNet)
    throws Exception
  {
    m_BayesNet = bayesNet;
    double fAccuracy = 0.0D;
    double fWeight = 0.0D;
    Instances instances = m_Instances;
    bayesNet.estimateCPTs();
    for (int iInstance = 0; iInstance < instances.numInstances(); iInstance++) {
      Instance instance = instances.instance(iInstance);
      instance.setWeight(-instance.weight());
      bayesNet.updateClassifier(instance);
      fAccuracy += accuracyIncrease(instance);
      fWeight += instance.weight();
      instance.setWeight(-instance.weight());
      bayesNet.updateClassifier(instance);
    }
    return fAccuracy / fWeight;
  }
  








  public double cumulativeCV(BayesNet bayesNet)
    throws Exception
  {
    m_BayesNet = bayesNet;
    double fAccuracy = 0.0D;
    double fWeight = 0.0D;
    Instances instances = m_Instances;
    bayesNet.initCPTs();
    for (int iInstance = 0; iInstance < instances.numInstances(); iInstance++) {
      Instance instance = instances.instance(iInstance);
      fAccuracy += accuracyIncrease(instance);
      bayesNet.updateClassifier(instance);
      fWeight += instance.weight();
    }
    return fAccuracy / fWeight;
  }
  






  public double kFoldCV(BayesNet bayesNet, int nNrOfFolds)
    throws Exception
  {
    m_BayesNet = bayesNet;
    double fAccuracy = 0.0D;
    double fWeight = 0.0D;
    Instances instances = m_Instances;
    
    bayesNet.estimateCPTs();
    int nFoldStart = 0;
    int nFoldEnd = instances.numInstances() / nNrOfFolds;
    int iFold = 1;
    while (nFoldStart < instances.numInstances())
    {
      for (int iInstance = nFoldStart; iInstance < nFoldEnd; iInstance++) {
        Instance instance = instances.instance(iInstance);
        instance.setWeight(-instance.weight());
        bayesNet.updateClassifier(instance);
      }
      

      for (int iInstance = nFoldStart; iInstance < nFoldEnd; iInstance++) {
        Instance instance = instances.instance(iInstance);
        instance.setWeight(-instance.weight());
        fAccuracy += accuracyIncrease(instance);
        fWeight += instance.weight();
        instance.setWeight(-instance.weight());
      }
      

      for (int iInstance = nFoldStart; iInstance < nFoldEnd; iInstance++) {
        Instance instance = instances.instance(iInstance);
        instance.setWeight(-instance.weight());
        bayesNet.updateClassifier(instance);
      }
      

      nFoldStart = nFoldEnd;
      iFold++;
      nFoldEnd = iFold * instances.numInstances() / nNrOfFolds;
    }
    return fAccuracy / fWeight;
  }
  





  double accuracyIncrease(Instance instance)
    throws Exception
  {
    if (m_bUseProb) {
      double[] fProb = m_BayesNet.distributionForInstance(instance);
      return fProb[((int)instance.classValue())] * instance.weight();
    }
    if (m_BayesNet.classifyInstance(instance) == instance.classValue()) {
      return instance.weight();
    }
    
    return 0.0D;
  }
  


  public boolean getUseProb()
  {
    return m_bUseProb;
  }
  


  public void setUseProb(boolean useProb)
  {
    m_bUseProb = useProb;
  }
  



  public void setCVType(SelectedTag newCVType)
  {
    if (newCVType.getTags() == TAGS_CV_TYPE) {
      m_nCVType = newCVType.getSelectedTag().getID();
    }
  }
  



  public SelectedTag getCVType()
  {
    return new SelectedTag(m_nCVType, TAGS_CV_TYPE);
  }
  



  public void setMarkovBlanketClassifier(boolean bMarkovBlanketClassifier)
  {
    super.setMarkovBlanketClassifier(bMarkovBlanketClassifier);
  }
  



  public boolean getMarkovBlanketClassifier()
  {
    return super.getMarkovBlanketClassifier();
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tApplies a Markov Blanket correction to the network structure, \n\tafter a network structure is learned. This ensures that all \n\tnodes in the network are part of the Markov blanket of the \n\tclassifier node.", "mbc", 0, "-mbc"));
    





    newVector.addElement(new Option("\tScore type (LOO-CV,k-Fold-CV,Cumulative-CV)", "S", 1, "-S [LOO-CV|k-Fold-CV|Cumulative-CV]"));
    





    newVector.addElement(new Option("\tUse probabilistic or 0/1 scoring.\n\t(default probabilistic scoring)", "Q", 0, "-Q"));
    
    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  























  public void setOptions(String[] options)
    throws Exception
  {
    setMarkovBlanketClassifier(Utils.getFlag("mbc", options));
    
    String sScore = Utils.getOption('S', options);
    
    if (sScore.compareTo("LOO-CV") == 0) {
      setCVType(new SelectedTag(0, TAGS_CV_TYPE));
    }
    if (sScore.compareTo("k-Fold-CV") == 0) {
      setCVType(new SelectedTag(1, TAGS_CV_TYPE));
    }
    if (sScore.compareTo("Cumulative-CV") == 0) {
      setCVType(new SelectedTag(2, TAGS_CV_TYPE));
    }
    setUseProb(!Utils.getFlag('Q', options));
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[4 + superOptions.length];
    int current = 0;
    
    if (getMarkovBlanketClassifier()) {
      options[(current++)] = "-mbc";
    }
    options[(current++)] = "-S";
    
    switch (m_nCVType) {
    case 0: 
      options[(current++)] = "LOO-CV";
      break;
    case 1: 
      options[(current++)] = "k-Fold-CV";
      break;
    case 2: 
      options[(current++)] = "Cumulative-CV";
    }
    
    
    if (!getUseProb()) {
      options[(current++)] = "-Q";
    }
    

    for (int iOption = 0; iOption < superOptions.length; iOption++) {
      options[(current++)] = superOptions[iOption];
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  


  public String CVTypeTipText()
  {
    return "Select cross validation strategy to be used in searching for networks.LOO-CV = Leave one out cross validation\nk-Fold-CV = k fold cross validation\nCumulative-CV = cumulative cross validation.";
  }
  






  public String useProbTipText()
  {
    return "If set to true, the probability of the class if returned in the estimate of the accuracy. If set to false, the accuracy estimate is only increased if the classifier returns exactly the correct class.";
  }
  





  public String globalInfo()
  {
    return "This Bayes Network learning algorithm uses cross validation to estimate classification accuracy.";
  }
  



  public String markovBlanketClassifierTipText()
  {
    return super.markovBlanketClassifierTipText();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
}

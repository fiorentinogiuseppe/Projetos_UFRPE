package weka.classifiers.bayes.net.search.global;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;






































































public class RepeatedHillClimber
  extends HillClimber
{
  static final long serialVersionUID = -7359197180460703069L;
  int m_nRuns = 10;
  
  int m_nSeed = 1;
  

  Random m_random;
  


  public RepeatedHillClimber() {}
  

  protected void search(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    m_random = new Random(getSeed());
    

    double fCurrentScore = calcScore(bayesNet);
    




    double fBestScore = fCurrentScore;
    BayesNet bestBayesNet = new BayesNet();
    m_Instances = instances;
    bestBayesNet.initStructure();
    copyParentSets(bestBayesNet, bayesNet);
    


    for (int iRun = 0; iRun < m_nRuns; iRun++)
    {
      generateRandomNet(bayesNet, instances);
      

      super.search(bayesNet, instances);
      

      fCurrentScore = calcScore(bayesNet);
      

      if (fCurrentScore > fBestScore) {
        fBestScore = fCurrentScore;
        copyParentSets(bestBayesNet, bayesNet);
      }
    }
    

    copyParentSets(bayesNet, bestBayesNet);
    

    bestBayesNet = null;
  }
  




  void generateRandomNet(BayesNet bayesNet, Instances instances)
  {
    int nNodes = instances.numAttributes();
    
    for (int iNode = 0; iNode < nNodes; iNode++) {
      ParentSet parentSet = bayesNet.getParentSet(iNode);
      while (parentSet.getNrOfParents() > 0) {
        parentSet.deleteLastParent(instances);
      }
    }
    

    if (getInitAsNaiveBayes()) {
      int iClass = instances.classIndex();
      

      for (int iNode = 0; iNode < nNodes; iNode++) {
        if (iNode != iClass) {
          bayesNet.getParentSet(iNode).addParent(iClass, instances);
        }
      }
    }
    

    int nNrOfAttempts = m_random.nextInt(nNodes * nNodes);
    for (int iAttempt = 0; iAttempt < nNrOfAttempts; iAttempt++) {
      int iTail = m_random.nextInt(nNodes);
      int iHead = m_random.nextInt(nNodes);
      if ((bayesNet.getParentSet(iHead).getNrOfParents() < getMaxNrOfParents()) && (addArcMakesSense(bayesNet, instances, iHead, iTail)))
      {
        bayesNet.getParentSet(iHead).addParent(iTail, instances);
      }
    }
  }
  





  void copyParentSets(BayesNet dest, BayesNet source)
  {
    int nNodes = source.getNrOfNodes();
    
    for (int iNode = 0; iNode < nNodes; iNode++) {
      dest.getParentSet(iNode).copy(source.getParentSet(iNode));
    }
  }
  





  public int getRuns()
  {
    return m_nRuns;
  }
  




  public void setRuns(int nRuns)
  {
    m_nRuns = nRuns;
  }
  




  public int getSeed()
  {
    return m_nSeed;
  }
  




  public void setSeed(int nSeed)
  {
    m_nSeed = nSeed;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tNumber of runs", "U", 1, "-U <integer>"));
    newVector.addElement(new Option("\tRandom number seed", "A", 1, "-A <seed>"));
    
    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  






































  public void setOptions(String[] options)
    throws Exception
  {
    String sRuns = Utils.getOption('U', options);
    if (sRuns.length() != 0) {
      setRuns(Integer.parseInt(sRuns));
    }
    
    String sSeed = Utils.getOption('A', options);
    if (sSeed.length() != 0) {
      setSeed(Integer.parseInt(sSeed));
    }
    
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[7 + superOptions.length];
    int current = 0;
    
    options[(current++)] = "-U";
    options[(current++)] = ("" + getRuns());
    
    options[(current++)] = "-A";
    options[(current++)] = ("" + getSeed());
    

    for (int iOption = 0; iOption < superOptions.length; iOption++) {
      options[(current++)] = superOptions[iOption];
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String globalInfo()
  {
    return "This Bayes Network learning algorithm repeatedly uses hill climbing starting with a randomly generated network structure and return the best structure of the various runs.";
  }
  




  public String runsTipText()
  {
    return "Sets the number of times hill climbing is performed.";
  }
  


  public String seedTipText()
  {
    return "Initialization value for random number generator. Setting the seed allows replicability of experiments.";
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}

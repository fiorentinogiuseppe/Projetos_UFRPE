package weka.classifiers.bayes.net.search.local;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;















































































public class SimulatedAnnealing
  extends LocalScoreSearchAlgorithm
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 6951955606060513191L;
  double m_fTStart = 10.0D;
  

  double m_fDelta = 0.999D;
  

  int m_nRuns = 10000;
  

  boolean m_bUseArcReversal = false;
  

  int m_nSeed = 1;
  


  Random m_random;
  



  public SimulatedAnnealing() {}
  


  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.PHDTHESIS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "R.R. Bouckaert");
    result.setValue(TechnicalInformation.Field.YEAR, "1995");
    result.setValue(TechnicalInformation.Field.TITLE, "Bayesian Belief Networks: from Construction to Inference");
    result.setValue(TechnicalInformation.Field.INSTITUTION, "University of Utrecht");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Utrecht, Netherlands");
    
    return result;
  }
  




  public void search(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    m_random = new Random(m_nSeed);
    

    double[] fBaseScores = new double[instances.numAttributes()];
    double fCurrentScore = 0.0D;
    for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
      fBaseScores[iAttribute] = calcNodeScore(iAttribute);
      fCurrentScore += fBaseScores[iAttribute];
    }
    

    double fBestScore = fCurrentScore;
    BayesNet bestBayesNet = new BayesNet();
    m_Instances = instances;
    bestBayesNet.initStructure();
    copyParentSets(bestBayesNet, bayesNet);
    
    double fTemp = m_fTStart;
    for (int iRun = 0; iRun < m_nRuns; iRun++) {
      boolean bRunSucces = false;
      double fDeltaScore = 0.0D;
      while (!bRunSucces)
      {
        int iTailNode = Math.abs(m_random.nextInt()) % instances.numAttributes();
        int iHeadNode = Math.abs(m_random.nextInt()) % instances.numAttributes();
        while (iTailNode == iHeadNode) {
          iHeadNode = Math.abs(m_random.nextInt()) % instances.numAttributes();
        }
        if (isArc(bayesNet, iHeadNode, iTailNode)) {
          bRunSucces = true;
          
          bayesNet.getParentSet(iHeadNode).deleteParent(iTailNode, instances);
          double fScore = calcNodeScore(iHeadNode);
          fDeltaScore = fScore - fBaseScores[iHeadNode];
          
          if (fTemp * Math.log(Math.abs(m_random.nextInt()) % 10000 / 10000.0D + 1.0E-100D) < fDeltaScore)
          {
            fCurrentScore += fDeltaScore;
            fBaseScores[iHeadNode] = fScore;
          }
          else {
            bayesNet.getParentSet(iHeadNode).addParent(iTailNode, instances);
          }
          
        }
        else if (addArcMakesSense(bayesNet, instances, iHeadNode, iTailNode)) {
          bRunSucces = true;
          double fScore = calcScoreWithExtraParent(iHeadNode, iTailNode);
          fDeltaScore = fScore - fBaseScores[iHeadNode];
          
          if (fTemp * Math.log(Math.abs(m_random.nextInt()) % 10000 / 10000.0D + 1.0E-100D) < fDeltaScore)
          {
            bayesNet.getParentSet(iHeadNode).addParent(iTailNode, instances);
            fBaseScores[iHeadNode] = fScore;
            fCurrentScore += fDeltaScore;
          }
        }
      }
      
      if (fCurrentScore > fBestScore) {
        copyParentSets(bestBayesNet, bayesNet);
      }
      fTemp *= m_fDelta;
    }
    
    copyParentSets(bayesNet, bestBayesNet);
  }
  



  void copyParentSets(BayesNet dest, BayesNet source)
  {
    int nNodes = source.getNrOfNodes();
    
    for (int iNode = 0; iNode < nNodes; iNode++) {
      dest.getParentSet(iNode).copy(source.getParentSet(iNode));
    }
  }
  


  public double getDelta()
  {
    return m_fDelta;
  }
  


  public double getTStart()
  {
    return m_fTStart;
  }
  


  public int getRuns()
  {
    return m_nRuns;
  }
  



  public void setDelta(double fDelta)
  {
    m_fDelta = fDelta;
  }
  



  public void setTStart(double fTStart)
  {
    m_fTStart = fTStart;
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
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tStart temperature", "A", 1, "-A <float>"));
    newVector.addElement(new Option("\tNumber of runs", "U", 1, "-U <integer>"));
    newVector.addElement(new Option("\tDelta temperature", "D", 1, "-D <float>"));
    newVector.addElement(new Option("\tRandom number seed", "R", 1, "-R <seed>"));
    
    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  






























  public void setOptions(String[] options)
    throws Exception
  {
    String sTStart = Utils.getOption('A', options);
    if (sTStart.length() != 0) {
      setTStart(Double.parseDouble(sTStart));
    }
    String sRuns = Utils.getOption('U', options);
    if (sRuns.length() != 0) {
      setRuns(Integer.parseInt(sRuns));
    }
    String sDelta = Utils.getOption('D', options);
    if (sDelta.length() != 0) {
      setDelta(Double.parseDouble(sDelta));
    }
    String sSeed = Utils.getOption('R', options);
    if (sSeed.length() != 0) {
      setSeed(Integer.parseInt(sSeed));
    }
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[8 + superOptions.length];
    int current = 0;
    options[(current++)] = "-A";
    options[(current++)] = ("" + getTStart());
    
    options[(current++)] = "-U";
    options[(current++)] = ("" + getRuns());
    
    options[(current++)] = "-D";
    options[(current++)] = ("" + getDelta());
    
    options[(current++)] = "-R";
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
    return "This Bayes Network learning algorithm uses the general purpose search method of simulated annealing to find a well scoring network structure.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  






  public String TStartTipText()
  {
    return "Sets the start temperature of the simulated annealing search. The start temperature determines the probability that a step in the 'wrong' direction in the search space is accepted. The higher the temperature, the higher the probability of acceptance.";
  }
  




  public String runsTipText()
  {
    return "Sets the number of iterations to be performed by the simulated annealing search.";
  }
  


  public String deltaTipText()
  {
    return "Sets the factor with which the temperature (and thus the acceptance probability of steps in the wrong direction in the search space) is decreased in each iteration.";
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

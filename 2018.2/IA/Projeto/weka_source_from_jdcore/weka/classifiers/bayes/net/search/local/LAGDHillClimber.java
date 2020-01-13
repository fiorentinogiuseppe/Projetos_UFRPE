package weka.classifiers.bayes.net.search.local;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;

































































public class LAGDHillClimber
  extends HillClimber
{
  static final long serialVersionUID = 7217437499439184344L;
  int m_nNrOfLookAheadSteps = 2;
  

  int m_nNrOfGoodOperations = 5;
  


  public LAGDHillClimber() {}
  

  protected void search(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    int k = m_nNrOfLookAheadSteps;
    int l = m_nNrOfGoodOperations;
    lookAheadInGoodDirectionsSearch(bayesNet, instances, k, l);
  }
  









  protected void lookAheadInGoodDirectionsSearch(BayesNet bayesNet, Instances instances, int nrOfLookAheadSteps, int nrOfGoodOperations)
    throws Exception
  {
    System.out.println("Initializing Cache");
    initCache(bayesNet, instances);
    
    while (nrOfLookAheadSteps > 1) {
      System.out.println("Look Ahead Depth: " + nrOfLookAheadSteps);
      boolean legalSequence = true;
      double sequenceDeltaScore = 0.0D;
      HillClimber.Operation[] bestOperation = new HillClimber.Operation[nrOfLookAheadSteps];
      
      bestOperation = getOptimalOperations(bayesNet, instances, nrOfLookAheadSteps, nrOfGoodOperations);
      for (int i = 0; i < nrOfLookAheadSteps; i++) {
        if (bestOperation[i] == null) {
          legalSequence = false;
        } else {
          sequenceDeltaScore += m_fDeltaScore;
        }
      }
      while ((legalSequence) && (sequenceDeltaScore > 0.0D)) {
        System.out.println("Next Iteration..........................");
        for (int i = 0; i < nrOfLookAheadSteps; i++) {
          performOperation(bayesNet, instances, bestOperation[i]);
        }
        bestOperation = getOptimalOperations(bayesNet, instances, nrOfLookAheadSteps, nrOfGoodOperations);
        sequenceDeltaScore = 0.0D;
        for (int i = 0; i < nrOfLookAheadSteps; i++) {
          if (bestOperation[i] != null) {
            System.out.println(m_nOperation + " " + m_nHead + " " + m_nTail);
            sequenceDeltaScore += m_fDeltaScore;
          } else {
            legalSequence = false;
          }
          
          System.out.println("DeltaScore: " + sequenceDeltaScore);
        }
      }
      nrOfLookAheadSteps--;
    }
    

    HillClimber.Operation oOperation = getOptimalOperation(bayesNet, instances);
    while ((oOperation != null) && (m_fDeltaScore > 0.0D)) {
      performOperation(bayesNet, instances, oOperation);
      System.out.println("Performing last greedy steps");
      oOperation = getOptimalOperation(bayesNet, instances);
    }
    
    m_Cache = null;
  }
  





  protected HillClimber.Operation getAntiOperation(HillClimber.Operation oOperation)
    throws Exception
  {
    if (m_nOperation == 0) {
      return new HillClimber.Operation(this, m_nTail, m_nHead, 1);
    }
    if (m_nOperation == 1) {
      return new HillClimber.Operation(this, m_nTail, m_nHead, 0);
    }
    return new HillClimber.Operation(this, m_nHead, m_nTail, 2);
  }
  










  protected HillClimber.Operation[] getGoodOperations(BayesNet bayesNet, Instances instances, int nrOfGoodOperations)
    throws Exception
  {
    HillClimber.Operation[] goodOperations = new HillClimber.Operation[nrOfGoodOperations];
    for (int i = 0; i < nrOfGoodOperations; i++) {
      goodOperations[i] = getOptimalOperation(bayesNet, instances);
      if (goodOperations[i] != null)
        m_Cache.put(goodOperations[i], -1.0E100D); else
        i = nrOfGoodOperations;
    }
    for (int i = 0; i < nrOfGoodOperations; i++) {
      if (goodOperations[i] != null) {
        if (m_nOperation != 2) {
          m_Cache.put(goodOperations[i], m_fDeltaScore);
        } else
          m_Cache.put(goodOperations[i], m_fDeltaScore - m_Cache.m_fDeltaScoreAdd[m_nHead][m_nTail]);
      } else
        i = nrOfGoodOperations;
    }
    return goodOperations;
  }
  








  protected HillClimber.Operation[] getOptimalOperations(BayesNet bayesNet, Instances instances, int nrOfLookAheadSteps, int nrOfGoodOperations)
    throws Exception
  {
    if (nrOfLookAheadSteps == 1) {
      HillClimber.Operation[] bestOperation = new HillClimber.Operation[1];
      bestOperation[0] = getOptimalOperation(bayesNet, instances);
      return bestOperation;
    }
    double bestDeltaScore = 0.0D;
    double currentDeltaScore = 0.0D;
    HillClimber.Operation[] bestOperation = new HillClimber.Operation[nrOfLookAheadSteps];
    HillClimber.Operation[] goodOperations = new HillClimber.Operation[nrOfGoodOperations];
    HillClimber.Operation[] tempOperation = new HillClimber.Operation[nrOfLookAheadSteps - 1];
    goodOperations = getGoodOperations(bayesNet, instances, nrOfGoodOperations);
    for (int i = 0; i < nrOfGoodOperations; i++)
      if (goodOperations[i] != null) {
        performOperation(bayesNet, instances, goodOperations[i]);
        tempOperation = getOptimalOperations(bayesNet, instances, nrOfLookAheadSteps - 1, nrOfGoodOperations);
        currentDeltaScore = m_fDeltaScore;
        for (int j = 0; j < nrOfLookAheadSteps - 1; j++) {
          if (tempOperation[j] != null) {
            currentDeltaScore += m_fDeltaScore;
          }
        }
        performOperation(bayesNet, instances, getAntiOperation(goodOperations[i]));
        if (currentDeltaScore > bestDeltaScore) {
          bestDeltaScore = currentDeltaScore;
          bestOperation[0] = goodOperations[i];
          for (int j = 1; j < nrOfLookAheadSteps; j++)
            bestOperation[j] = tempOperation[(j - 1)];
        }
      } else {
        i = nrOfGoodOperations;
      }
    return bestOperation;
  }
  






  public void setMaxNrOfParents(int nMaxNrOfParents)
  {
    m_nMaxNrOfParents = nMaxNrOfParents;
  }
  




  public int getMaxNrOfParents()
  {
    return m_nMaxNrOfParents;
  }
  




  public void setNrOfLookAheadSteps(int nNrOfLookAheadSteps)
  {
    m_nNrOfLookAheadSteps = nNrOfLookAheadSteps;
  }
  




  public int getNrOfLookAheadSteps()
  {
    return m_nNrOfLookAheadSteps;
  }
  




  public void setNrOfGoodOperations(int nNrOfGoodOperations)
  {
    m_nNrOfGoodOperations = nNrOfGoodOperations;
  }
  




  public int getNrOfGoodOperations()
  {
    return m_nNrOfGoodOperations;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tLook Ahead Depth", "L", 2, "-L <nr of look ahead steps>"));
    newVector.addElement(new Option("\tNr of Good Operations", "G", 5, "-G <nr of good operations>"));
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      newVector.addElement(enm.nextElement());
    }
    return newVector.elements();
  }
  


































  public void setOptions(String[] options)
    throws Exception
  {
    String sNrOfLookAheadSteps = Utils.getOption('L', options);
    if (sNrOfLookAheadSteps.length() != 0) {
      setNrOfLookAheadSteps(Integer.parseInt(sNrOfLookAheadSteps));
    } else {
      setNrOfLookAheadSteps(2);
    }
    
    String sNrOfGoodOperations = Utils.getOption('G', options);
    if (sNrOfGoodOperations.length() != 0) {
      setNrOfGoodOperations(Integer.parseInt(sNrOfGoodOperations));
    } else {
      setNrOfGoodOperations(5);
    }
    
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[9 + superOptions.length];
    int current = 0;
    
    options[(current++)] = "-L";
    options[(current++)] = ("" + m_nNrOfLookAheadSteps);
    
    options[(current++)] = "-G";
    options[(current++)] = ("" + m_nNrOfGoodOperations);
    

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
    return "This Bayes Network learning algorithm uses a Look Ahead Hill Climbing algorithm called LAGD Hill Climbing. Unlike Greedy Hill Climbing it doesn't calculate a best greedy operation (adding, deleting or reversing an arc) but a sequence of nrOfLookAheadSteps operations, which leads to a network structure whose score is most likely higher in comparison to the network obtained by performing a sequence of nrOfLookAheadSteps greedy operations. The search is not restricted by an order on the variables (unlike K2). The difference with B and B2 is that this hill climber also considers arrows part of the naive Bayes structure for deletion.";
  }
  








  public String nrOfLookAheadStepsTipText()
  {
    return "Sets the Number of Look Ahead Steps. 'nrOfLookAheadSteps = 2' means that all network structures in a distance of 2 (from the current network structure) are taken into account for the decision which arcs to add, remove or reverse. 'nrOfLookAheadSteps = 1' results in Greedy Hill Climbing.";
  }
  




  public String nrOfGoodOperationsTipText()
  {
    return "Sets the Number of Good Operations per Look Ahead Step. 'nrOfGoodOperations = 5' means that for the next Look Ahead Step only the 5 best Operations (adding, deleting or reversing an arc) are taken into account for the calculation of the best sequence consisting of nrOfLookAheadSteps operations.";
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}

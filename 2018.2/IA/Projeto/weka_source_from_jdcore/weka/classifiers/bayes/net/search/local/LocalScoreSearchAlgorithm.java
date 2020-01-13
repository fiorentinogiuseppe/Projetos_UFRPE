package weka.classifiers.bayes.net.search.local;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ADNode;
import weka.classifiers.bayes.net.ParentSet;
import weka.classifiers.bayes.net.search.SearchAlgorithm;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Statistics;
import weka.core.Tag;
import weka.core.Utils;

























































public class LocalScoreSearchAlgorithm
  extends SearchAlgorithm
{
  static final long serialVersionUID = 3325995552474190374L;
  BayesNet m_BayesNet;
  
  public LocalScoreSearchAlgorithm() {}
  
  public LocalScoreSearchAlgorithm(BayesNet bayesNet, Instances instances)
  {
    m_BayesNet = bayesNet;
  }
  




  double m_fAlpha = 0.5D;
  

  public static final Tag[] TAGS_SCORE_TYPE = { new Tag(0, "BAYES"), new Tag(1, "BDeu"), new Tag(2, "MDL"), new Tag(3, "ENTROPY"), new Tag(4, "AIC") };
  









  int m_nScoreType = 0;
  






  public double logScore(int nType)
  {
    if (m_BayesNet.m_Distributions == null) return 0.0D;
    if (nType < 0) {
      nType = m_nScoreType;
    }
    
    double fLogScore = 0.0D;
    
    Instances instances = m_BayesNet.m_Instances;
    
    for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
      int nCardinality = m_BayesNet.getParentSet(iAttribute).getCardinalityOfParents();
      for (int iParent = 0; iParent < nCardinality; iParent++) {
        fLogScore += ((Scoreable)m_BayesNet.m_Distributions[iAttribute][iParent]).logScore(nType, nCardinality);
      }
      
      switch (nType)
      {
      case 2: 
        fLogScore -= 0.5D * m_BayesNet.getParentSet(iAttribute).getCardinalityOfParents() * (instances.attribute(iAttribute).numValues() - 1) * Math.log(instances.numInstances());
        



        break;
      
      case 4: 
        fLogScore -= m_BayesNet.getParentSet(iAttribute).getCardinalityOfParents() * (instances.attribute(iAttribute).numValues() - 1);
      }
      
    }
    


    return fLogScore;
  }
  







  public void buildStructure(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    m_BayesNet = bayesNet;
    super.buildStructure(bayesNet, instances);
  }
  






  public double calcNodeScore(int nNode)
  {
    if ((m_BayesNet.getUseADTree()) && (m_BayesNet.getADTree() != null)) {
      return calcNodeScoreADTree(nNode);
    }
    return calcNodeScorePlain(nNode);
  }
  






  private double calcNodeScoreADTree(int nNode)
  {
    Instances instances = m_BayesNet.m_Instances;
    ParentSet oParentSet = m_BayesNet.getParentSet(nNode);
    
    int nNrOfParents = oParentSet.getNrOfParents();
    int[] nNodes = new int[nNrOfParents + 1];
    for (int iParent = 0; iParent < nNrOfParents; iParent++) {
      nNodes[iParent] = oParentSet.getParent(iParent);
    }
    nNodes[nNrOfParents] = nNode;
    

    int[] nOffsets = new int[nNrOfParents + 1];
    int nOffset = 1;
    nOffsets[nNrOfParents] = 1;
    nOffset *= instances.attribute(nNode).numValues();
    for (int iNode = nNrOfParents - 1; iNode >= 0; iNode--) {
      nOffsets[iNode] = nOffset;
      nOffset *= instances.attribute(nNodes[iNode]).numValues();
    }
    

    for (int iNode = 1; iNode < nNodes.length; iNode++) {
      int iNode2 = iNode;
      while ((iNode2 > 0) && (nNodes[iNode2] < nNodes[(iNode2 - 1)])) {
        int h = nNodes[iNode2];
        nNodes[iNode2] = nNodes[(iNode2 - 1)];
        nNodes[(iNode2 - 1)] = h;
        h = nOffsets[iNode2];
        nOffsets[iNode2] = nOffsets[(iNode2 - 1)];
        nOffsets[(iNode2 - 1)] = h;
        iNode2--;
      }
    }
    

    int nCardinality = oParentSet.getCardinalityOfParents();
    int numValues = instances.attribute(nNode).numValues();
    int[] nCounts = new int[nCardinality * numValues];
    

    m_BayesNet.getADTree().getCounts(nCounts, nNodes, nOffsets, 0, 0, false);
    
    return calcScoreOfCounts(nCounts, nCardinality, numValues, instances);
  }
  
  private double calcNodeScorePlain(int nNode) {
    Instances instances = m_BayesNet.m_Instances;
    ParentSet oParentSet = m_BayesNet.getParentSet(nNode);
    

    int nCardinality = oParentSet.getCardinalityOfParents();
    int numValues = instances.attribute(nNode).numValues();
    int[] nCounts = new int[nCardinality * numValues];
    

    for (int iParent = 0; iParent < nCardinality * numValues; iParent++) {
      nCounts[iParent] = 0;
    }
    

    Enumeration enumInsts = instances.enumerateInstances();
    
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance)enumInsts.nextElement();
      

      double iCPT = 0.0D;
      
      for (int iParent = 0; iParent < oParentSet.getNrOfParents(); iParent++) {
        int nParent = oParentSet.getParent(iParent);
        
        iCPT = iCPT * instances.attribute(nParent).numValues() + instance.value(nParent);
      }
      
      nCounts[(numValues * (int)iCPT + (int)instance.value(nNode))] += 1;
    }
    
    return calcScoreOfCounts(nCounts, nCardinality, numValues, instances);
  }
  











  protected double calcScoreOfCounts(int[] nCounts, int nCardinality, int numValues, Instances instances)
  {
    double fLogScore = 0.0D;
    
    for (int iParent = 0; iParent < nCardinality; iParent++) {
      switch (m_nScoreType)
      {

      case 0: 
        double nSumOfCounts = 0.0D;
        
        for (int iSymbol = 0; iSymbol < numValues; iSymbol++) {
          if (m_fAlpha + nCounts[(iParent * numValues + iSymbol)] != 0.0D) {
            fLogScore += Statistics.lnGamma(m_fAlpha + nCounts[(iParent * numValues + iSymbol)]);
            nSumOfCounts += m_fAlpha + nCounts[(iParent * numValues + iSymbol)];
          }
        }
        
        if (nSumOfCounts != 0.0D) {
          fLogScore -= Statistics.lnGamma(nSumOfCounts);
        }
        
        if (m_fAlpha != 0.0D) {
          fLogScore -= numValues * Statistics.lnGamma(m_fAlpha);
          fLogScore += Statistics.lnGamma(numValues * m_fAlpha);
        }
        

        break;
      
      case 1: 
        double nSumOfCounts = 0.0D;
        
        for (int iSymbol = 0; iSymbol < numValues; iSymbol++) {
          if (m_fAlpha + nCounts[(iParent * numValues + iSymbol)] != 0.0D) {
            fLogScore += Statistics.lnGamma(1.0D / (numValues * nCardinality) + nCounts[(iParent * numValues + iSymbol)]);
            nSumOfCounts += 1.0D / (numValues * nCardinality) + nCounts[(iParent * numValues + iSymbol)];
          }
        }
        fLogScore -= Statistics.lnGamma(nSumOfCounts);
        
        fLogScore -= numValues * Statistics.lnGamma(1.0D / (numValues * nCardinality));
        fLogScore += Statistics.lnGamma(1.0D / nCardinality);
        
        break;
      



      case 2: 
      case 3: 
      case 4: 
        double nSumOfCounts = 0.0D;
        
        for (int iSymbol = 0; iSymbol < numValues; iSymbol++) {
          nSumOfCounts += nCounts[(iParent * numValues + iSymbol)];
        }
        
        for (int iSymbol = 0; iSymbol < numValues; iSymbol++) {
          if (nCounts[(iParent * numValues + iSymbol)] > 0) {
            fLogScore += nCounts[(iParent * numValues + iSymbol)] * Math.log(nCounts[(iParent * numValues + iSymbol)] / nSumOfCounts);
          }
        }
      }
      
    }
    







    switch (m_nScoreType)
    {

    case 2: 
      fLogScore -= 0.5D * nCardinality * (numValues - 1) * Math.log(instances.numInstances());
      



      break;
    

    case 4: 
      fLogScore -= nCardinality * (numValues - 1);
    }
    
    


    return fLogScore;
  }
  

  protected double calcScoreOfCounts2(int[][] nCounts, int nCardinality, int numValues, Instances instances)
  {
    double fLogScore = 0.0D;
    
    for (int iParent = 0; iParent < nCardinality; iParent++) {
      switch (m_nScoreType)
      {

      case 0: 
        double nSumOfCounts = 0.0D;
        
        for (int iSymbol = 0; iSymbol < numValues; iSymbol++) {
          if (m_fAlpha + nCounts[iParent][iSymbol] != 0.0D) {
            fLogScore += Statistics.lnGamma(m_fAlpha + nCounts[iParent][iSymbol]);
            nSumOfCounts += m_fAlpha + nCounts[iParent][iSymbol];
          }
        }
        
        if (nSumOfCounts != 0.0D) {
          fLogScore -= Statistics.lnGamma(nSumOfCounts);
        }
        
        if (m_fAlpha != 0.0D) {
          fLogScore -= numValues * Statistics.lnGamma(m_fAlpha);
          fLogScore += Statistics.lnGamma(numValues * m_fAlpha);
        }
        

        break;
      

      case 1: 
        double nSumOfCounts = 0.0D;
        
        for (int iSymbol = 0; iSymbol < numValues; iSymbol++) {
          if (m_fAlpha + nCounts[iParent][iSymbol] != 0.0D) {
            fLogScore += Statistics.lnGamma(1.0D / (numValues * nCardinality) + nCounts[iParent][iSymbol]);
            nSumOfCounts += 1.0D / (numValues * nCardinality) + nCounts[iParent][iSymbol];
          }
        }
        fLogScore -= Statistics.lnGamma(nSumOfCounts);
        
        fLogScore -= numValues * Statistics.lnGamma(1.0D / (nCardinality * numValues));
        fLogScore += Statistics.lnGamma(1.0D / nCardinality);
        
        break;
      



      case 2: 
      case 3: 
      case 4: 
        double nSumOfCounts = 0.0D;
        
        for (int iSymbol = 0; iSymbol < numValues; iSymbol++) {
          nSumOfCounts += nCounts[iParent][iSymbol];
        }
        
        for (int iSymbol = 0; iSymbol < numValues; iSymbol++) {
          if (nCounts[iParent][iSymbol] > 0) {
            fLogScore += nCounts[iParent][iSymbol] * Math.log(nCounts[iParent][iSymbol] / nSumOfCounts);
          }
        }
      }
      
    }
    







    switch (m_nScoreType)
    {

    case 2: 
      fLogScore -= 0.5D * nCardinality * (numValues - 1) * Math.log(instances.numInstances());
      



      break;
    

    case 4: 
      fLogScore -= nCardinality * (numValues - 1);
    }
    
    


    return fLogScore;
  }
  







  public double calcScoreWithExtraParent(int nNode, int nCandidateParent)
  {
    ParentSet oParentSet = m_BayesNet.getParentSet(nNode);
    

    if (oParentSet.contains(nCandidateParent)) {
      return -1.0E100D;
    }
    

    oParentSet.addParent(nCandidateParent, m_BayesNet.m_Instances);
    

    double logScore = calcNodeScore(nNode);
    

    oParentSet.deleteLastParent(m_BayesNet.m_Instances);
    
    return logScore;
  }
  







  public double calcScoreWithMissingParent(int nNode, int nCandidateParent)
  {
    ParentSet oParentSet = m_BayesNet.getParentSet(nNode);
    

    if (!oParentSet.contains(nCandidateParent)) {
      return -1.0E100D;
    }
    

    int iParent = oParentSet.deleteParent(nCandidateParent, m_BayesNet.m_Instances);
    

    double logScore = calcNodeScore(nNode);
    

    oParentSet.addParent(nCandidateParent, iParent, m_BayesNet.m_Instances);
    
    return logScore;
  }
  




  public void setScoreType(SelectedTag newScoreType)
  {
    if (newScoreType.getTags() == TAGS_SCORE_TYPE) {
      m_nScoreType = newScoreType.getSelectedTag().getID();
    }
  }
  



  public SelectedTag getScoreType()
  {
    return new SelectedTag(m_nScoreType, TAGS_SCORE_TYPE);
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
    





    newVector.addElement(new Option("\tScore type (BAYES, BDeu, MDL, ENTROPY and AIC)", "S", 1, "-S [BAYES|MDL|ENTROPY|AIC|CROSS_CLASSIC|CROSS_BAYES]"));
    





    return newVector.elements();
  }
  



















  public void setOptions(String[] options)
    throws Exception
  {
    setMarkovBlanketClassifier(Utils.getFlag("mbc", options));
    
    String sScore = Utils.getOption('S', options);
    
    if (sScore.compareTo("BAYES") == 0) {
      setScoreType(new SelectedTag(0, TAGS_SCORE_TYPE));
    }
    if (sScore.compareTo("BDeu") == 0) {
      setScoreType(new SelectedTag(1, TAGS_SCORE_TYPE));
    }
    if (sScore.compareTo("MDL") == 0) {
      setScoreType(new SelectedTag(2, TAGS_SCORE_TYPE));
    }
    if (sScore.compareTo("ENTROPY") == 0) {
      setScoreType(new SelectedTag(3, TAGS_SCORE_TYPE));
    }
    if (sScore.compareTo("AIC") == 0) {
      setScoreType(new SelectedTag(4, TAGS_SCORE_TYPE));
    }
  }
  




  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[3 + superOptions.length];
    int current = 0;
    
    if (getMarkovBlanketClassifier()) {
      options[(current++)] = "-mbc";
    }
    options[(current++)] = "-S";
    
    switch (m_nScoreType)
    {
    case 0: 
      options[(current++)] = "BAYES";
      break;
    
    case 1: 
      options[(current++)] = "BDeu";
      break;
    
    case 2: 
      options[(current++)] = "MDL";
      break;
    
    case 3: 
      options[(current++)] = "ENTROPY";
      
      break;
    
    case 4: 
      options[(current++)] = "AIC";
    }
    
    

    for (int iOption = 0; iOption < superOptions.length; iOption++) {
      options[(current++)] = superOptions[iOption];
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  


  public String scoreTypeTipText()
  {
    return "The score type determines the measure used to judge the quality of a network structure. It can be one of Bayes, BDeu, Minimum Description Length (MDL), Akaike Information Criterion (AIC), and Entropy.";
  }
  




  public String markovBlanketClassifierTipText()
  {
    return super.markovBlanketClassifierTipText();
  }
  



  public String globalInfo()
  {
    return "The ScoreBasedSearchAlgorithm class supports Bayes net structure search algorithms that are based on maximizing scores (as opposed to for example conditional independence based search algorithms).";
  }
  








  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5196 $");
  }
}

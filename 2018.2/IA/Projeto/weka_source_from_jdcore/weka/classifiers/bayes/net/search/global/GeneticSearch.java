package weka.classifiers.bayes.net.search.global;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;


































































public class GeneticSearch
  extends GlobalScoreSearchAlgorithm
{
  static final long serialVersionUID = 4236165533882462203L;
  int m_nRuns;
  int m_nPopulationSize;
  int m_nDescendantPopulationSize;
  boolean m_bUseCrossOver;
  boolean m_bUseMutation;
  boolean m_bUseTournamentSelection;
  int m_nSeed;
  Random m_random;
  static boolean[] g_bIsSquare;
  
  public GeneticSearch()
  {
    m_nRuns = 10;
    

    m_nPopulationSize = 10;
    

    m_nDescendantPopulationSize = 100;
    

    m_bUseCrossOver = true;
    

    m_bUseMutation = true;
    

    m_bUseTournamentSelection = false;
    

    m_nSeed = 1;
    

    m_random = null;
  }
  




  class BayesNetRepresentation
    implements RevisionHandler
  {
    int m_nNodes = 0;
    


    boolean[] m_bits;
    


    double m_fScore = 0.0D;
    




    public double getScore()
    {
      return m_fScore;
    }
    




    BayesNetRepresentation(int nNodes)
    {
      m_nNodes = nNodes;
    }
    

    public void randomInit()
    {
      do
      {
        m_bits = new boolean[m_nNodes * m_nNodes];
        for (int i = 0; i < m_nNodes; i++) {
          int iPos;
          do {
            iPos = m_random.nextInt(m_nNodes * m_nNodes);
          } while (isSquare(iPos));
          m_bits[iPos] = true;
        }
      } while (hasCycles());
      calcGlobalScore();
    }
    



    void calcGlobalScore()
    {
      for (int iNode = 0; iNode < m_nNodes; iNode++) {
        ParentSet parentSet = m_BayesNet.getParentSet(iNode);
        while (parentSet.getNrOfParents() > 0) {
          parentSet.deleteLastParent(m_BayesNet.m_Instances);
        }
      }
      
      for (int iNode = 0; iNode < m_nNodes; iNode++) {
        ParentSet parentSet = m_BayesNet.getParentSet(iNode);
        for (int iNode2 = 0; iNode2 < m_nNodes; iNode2++) {
          if (m_bits[(iNode2 + iNode * m_nNodes)] != 0) {
            parentSet.addParent(iNode2, m_BayesNet.m_Instances);
          }
        }
      }
      try
      {
        m_fScore = calcScore(m_BayesNet);
      }
      catch (Exception e) {}
    }
    





    public boolean hasCycles()
    {
      boolean[] bDone = new boolean[m_nNodes];
      for (int iNode = 0; iNode < m_nNodes; iNode++)
      {

        boolean bFound = false;
        
        for (int iNode2 = 0; (!bFound) && (iNode2 < m_nNodes); iNode2++) {
          if (bDone[iNode2] == 0) {
            boolean bHasNoParents = true;
            for (int iParent = 0; iParent < m_nNodes; iParent++) {
              if ((m_bits[(iParent + iNode2 * m_nNodes)] != 0) && (bDone[iParent] == 0)) {
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
          return true;
        }
      }
      return false;
    }
    


    BayesNetRepresentation copy()
    {
      BayesNetRepresentation b = new BayesNetRepresentation(GeneticSearch.this, m_nNodes);
      m_bits = new boolean[m_bits.length];
      for (int i = 0; i < m_nNodes * m_nNodes; i++) {
        m_bits[i] = m_bits[i];
      }
      m_fScore = m_fScore;
      return b;
    }
    

    void mutate()
    {
      do
      {
        int iBit;
        do
        {
          iBit = m_random.nextInt(m_nNodes * m_nNodes);
        } while (isSquare(iBit));
        
        m_bits[iBit] = (m_bits[iBit] == 0 ? 1 : false);
      } while (hasCycles());
      
      calcGlobalScore();
    }
    



    void crossOver(BayesNetRepresentation other)
    {
      boolean[] bits = new boolean[m_bits.length];
      for (int i = 0; i < m_bits.length; i++) {
        bits[i] = m_bits[i];
      }
      int iCrossOverPoint = m_bits.length;
      do
      {
        for (int i = iCrossOverPoint; i < m_bits.length; i++) {
          m_bits[i] = bits[i];
        }
        
        iCrossOverPoint = m_random.nextInt(m_bits.length);
        for (int i = iCrossOverPoint; i < m_bits.length; i++) {
          m_bits[i] = m_bits[i];
        }
      } while (hasCycles());
      calcGlobalScore();
    }
    




    boolean isSquare(int nNum)
    {
      if ((GeneticSearch.g_bIsSquare == null) || (GeneticSearch.g_bIsSquare.length < nNum)) {
        GeneticSearch.g_bIsSquare = new boolean[m_nNodes * m_nNodes];
        for (int i = 0; i < m_nNodes; i++) {
          GeneticSearch.g_bIsSquare[(i * m_nNodes + i)] = true;
        }
      }
      return GeneticSearch.g_bIsSquare[nNum];
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.5 $");
    }
  }
  







  protected void search(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    if (getDescendantPopulationSize() < getPopulationSize()) {
      throw new Exception("Descendant PopulationSize should be at least Population Size");
    }
    if ((!getUseCrossOver()) && (!getUseMutation())) {
      throw new Exception("At least one of mutation or cross-over should be used");
    }
    
    m_random = new Random(m_nSeed);
    



    double fBestScore = calcScore(bayesNet);
    

    BayesNet bestBayesNet = new BayesNet();
    m_Instances = instances;
    bestBayesNet.initStructure();
    copyParentSets(bestBayesNet, bayesNet);
    


    BayesNetRepresentation[] population = new BayesNetRepresentation[getPopulationSize()];
    for (int i = 0; i < getPopulationSize(); i++) {
      population[i] = new BayesNetRepresentation(instances.numAttributes());
      population[i].randomInit();
      if (population[i].getScore() > fBestScore) {
        copyParentSets(bestBayesNet, bayesNet);
        fBestScore = population[i].getScore();
      }
    }
    


    for (int iRun = 0; iRun < m_nRuns; iRun++)
    {
      BayesNetRepresentation[] descendantPopulation = new BayesNetRepresentation[getDescendantPopulationSize()];
      for (int i = 0; i < getDescendantPopulationSize(); i++) {
        descendantPopulation[i] = population[m_random.nextInt(getPopulationSize())].copy();
        if (getUseMutation()) {
          if ((getUseCrossOver()) && (m_random.nextBoolean())) {
            descendantPopulation[i].crossOver(population[m_random.nextInt(getPopulationSize())]);
          } else {
            descendantPopulation[i].mutate();
          }
        }
        else {
          descendantPopulation[i].crossOver(population[m_random.nextInt(getPopulationSize())]);
        }
        
        if (descendantPopulation[i].getScore() > fBestScore) {
          copyParentSets(bestBayesNet, bayesNet);
          fBestScore = descendantPopulation[i].getScore();
        }
      }
      
      boolean[] bSelected = new boolean[getDescendantPopulationSize()];
      for (int i = 0; i < getPopulationSize(); i++) {
        int iSelected = 0;
        if (m_bUseTournamentSelection)
        {
          iSelected = m_random.nextInt(getDescendantPopulationSize());
          while (bSelected[iSelected] != 0) {
            iSelected = (iSelected + 1) % getDescendantPopulationSize();
          }
          int iSelected2 = m_random.nextInt(getDescendantPopulationSize());
          while (bSelected[iSelected2] != 0) {
            iSelected2 = (iSelected2 + 1) % getDescendantPopulationSize();
          }
          if (descendantPopulation[iSelected2].getScore() > descendantPopulation[iSelected].getScore()) {
            iSelected = iSelected2;
          }
        }
        else {
          while (bSelected[iSelected] != 0) {
            iSelected++;
          }
          double fScore = descendantPopulation[iSelected].getScore();
          for (int j = 0; j < getDescendantPopulationSize(); j++) {
            if ((bSelected[j] == 0) && (descendantPopulation[j].getScore() > fScore)) {
              fScore = descendantPopulation[j].getScore();
              iSelected = j;
            }
          }
        }
        population[i] = descendantPopulation[iSelected];
        bSelected[iSelected] = true;
      }
    }
    

    copyParentSets(bayesNet, bestBayesNet);
    

    bestBayesNet = null;
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
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(7);
    
    newVector.addElement(new Option("\tPopulation size", "L", 1, "-L <integer>"));
    newVector.addElement(new Option("\tDescendant population size", "A", 1, "-A <integer>"));
    newVector.addElement(new Option("\tNumber of runs", "U", 1, "-U <integer>"));
    newVector.addElement(new Option("\tUse mutation.\n\t(default true)", "M", 0, "-M"));
    newVector.addElement(new Option("\tUse cross-over.\n\t(default true)", "C", 0, "-C"));
    newVector.addElement(new Option("\tUse tournament selection (true) or maximum subpopulatin (false).\n\t(default false)", "O", 0, "-O"));
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
    String sPopulationSize = Utils.getOption('L', options);
    if (sPopulationSize.length() != 0) {
      setPopulationSize(Integer.parseInt(sPopulationSize));
    }
    String sDescendantPopulationSize = Utils.getOption('A', options);
    if (sDescendantPopulationSize.length() != 0) {
      setDescendantPopulationSize(Integer.parseInt(sDescendantPopulationSize));
    }
    String sRuns = Utils.getOption('U', options);
    if (sRuns.length() != 0) {
      setRuns(Integer.parseInt(sRuns));
    }
    String sSeed = Utils.getOption('R', options);
    if (sSeed.length() != 0) {
      setSeed(Integer.parseInt(sSeed));
    }
    setUseMutation(Utils.getFlag('M', options));
    setUseCrossOver(Utils.getFlag('C', options));
    setUseTournamentSelection(Utils.getFlag('O', options));
    
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[11 + superOptions.length];
    int current = 0;
    
    options[(current++)] = "-L";
    options[(current++)] = ("" + getPopulationSize());
    
    options[(current++)] = "-A";
    options[(current++)] = ("" + getDescendantPopulationSize());
    
    options[(current++)] = "-U";
    options[(current++)] = ("" + getRuns());
    
    options[(current++)] = "-R";
    options[(current++)] = ("" + getSeed());
    
    if (getUseMutation()) {
      options[(current++)] = "-M";
    }
    if (getUseCrossOver()) {
      options[(current++)] = "-C";
    }
    if (getUseTournamentSelection()) {
      options[(current++)] = "-O";
    }
    

    for (int iOption = 0; iOption < superOptions.length; iOption++) {
      options[(current++)] = superOptions[iOption];
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  


  public boolean getUseCrossOver()
  {
    return m_bUseCrossOver;
  }
  


  public boolean getUseMutation()
  {
    return m_bUseMutation;
  }
  


  public int getDescendantPopulationSize()
  {
    return m_nDescendantPopulationSize;
  }
  


  public int getPopulationSize()
  {
    return m_nPopulationSize;
  }
  


  public void setUseCrossOver(boolean bUseCrossOver)
  {
    m_bUseCrossOver = bUseCrossOver;
  }
  


  public void setUseMutation(boolean bUseMutation)
  {
    m_bUseMutation = bUseMutation;
  }
  


  public boolean getUseTournamentSelection()
  {
    return m_bUseTournamentSelection;
  }
  


  public void setUseTournamentSelection(boolean bUseTournamentSelection)
  {
    m_bUseTournamentSelection = bUseTournamentSelection;
  }
  


  public void setDescendantPopulationSize(int iDescendantPopulationSize)
  {
    m_nDescendantPopulationSize = iDescendantPopulationSize;
  }
  


  public void setPopulationSize(int iPopulationSize)
  {
    m_nPopulationSize = iPopulationSize;
  }
  


  public int getSeed()
  {
    return m_nSeed;
  }
  



  public void setSeed(int nSeed)
  {
    m_nSeed = nSeed;
  }
  



  public String globalInfo()
  {
    return "This Bayes Network learning algorithm uses genetic search for finding a well scoring Bayes network structure. Genetic search works by having a population of Bayes network structures and allow them to mutate and apply cross over to get offspring. The best network structure found during the process is returned.";
  }
  





  public String runsTipText()
  {
    return "Sets the number of generations of Bayes network structure populations.";
  }
  


  public String seedTipText()
  {
    return "Initialization value for random number generator. Setting the seed allows replicability of experiments.";
  }
  



  public String populationSizeTipText()
  {
    return "Sets the size of the population of network structures that is selected each generation.";
  }
  


  public String descendantPopulationSizeTipText()
  {
    return "Sets the size of the population of descendants that is created each generation.";
  }
  


  public String useMutationTipText()
  {
    return "Determines whether mutation is allowed. Mutation flips a bit in the bit representation of the network structure. At least one of mutation or cross-over should be used.";
  }
  




  public String useCrossOverTipText()
  {
    return "Determines whether cross-over is allowed. Cross over combined the bit representations of network structure by taking a random first k bits of oneand adding the remainder of the other. At least one of mutation or cross-over should be used.";
  }
  





  public String useTournamentSelectionTipText()
  {
    return "Determines the method of selecting a population. When set to true, tournament selection is used (pick two at random and the highest is allowed to continue). When set to false, the top scoring network structures are selected.";
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

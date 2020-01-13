package weka.attributeSelection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;




































































































public class ScatterSearchV1
  extends ASSearch
  implements OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -8512041420388121326L;
  private int m_numAttribs;
  private int m_classIndex;
  private double m_treshold;
  private double m_initialThreshold;
  int m_typeOfCombination;
  private Random m_random;
  private int m_seed;
  private boolean m_debug = false;
  


  private StringBuffer m_InformationReports;
  


  private int m_totalEvals;
  


  protected double m_bestMerit;
  


  private long m_processinTime;
  


  private List<Subset> m_population;
  


  private int m_popSize;
  


  private int m_initialPopSize;
  


  private int m_calculatedInitialPopSize;
  

  private transient List<Subset> m_ReferenceSet;
  

  private transient List<Subset> m_parentsCombination;
  

  private List<Subset> m_attributeRanking;
  

  private SubsetEvaluator ASEvaluator = null;
  
  protected static final int COMBINATION_NOT_REDUCED = 0;
  
  protected static final int COMBINATION_REDUCED = 1;
  
  public static final Tag[] TAGS_SELECTION = { new Tag(0, "Greedy Combination"), new Tag(1, "Reduced Greedy Combination") };
  







  public String globalInfo()
  {
    return "Scatter Search :\n\nPerforms an Scatter Search  through the space of attribute subsets. Start with a population of many significants and diverses subset  stops when the result is higher than a given treshold or there's not more improvement\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.BOOK);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Felix Garcia Lopez");
    result.setValue(TechnicalInformation.Field.MONTH, "October");
    result.setValue(TechnicalInformation.Field.YEAR, "2004");
    result.setValue(TechnicalInformation.Field.TITLE, "Solving feature subset selection problem by a Parallel Scatter Search");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Elsevier");
    result.setValue(TechnicalInformation.Field.LANGUAGE, "English");
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.0$");
  }
  
  public ScatterSearchV1() {
    resetOptions();
  }
  




  public String thresholdTipText()
  {
    return "Set the treshold that subsets most overcome to be considered as significants";
  }
  




  public void setThreshold(double threshold)
  {
    m_initialThreshold = threshold;
  }
  




  public double getThreshold()
  {
    return m_initialThreshold;
  }
  





  public String populationSizeTipText()
  {
    return "Set the number of subset to generate in the initial Population";
  }
  




  public void setPopulationSize(int size)
  {
    m_initialPopSize = size;
  }
  




  public int getPopulationSize()
  {
    return m_initialPopSize;
  }
  





  public String combinationTipText()
  {
    return "Set the kind of combination for using it to combine ReferenceSet subsets.";
  }
  




  public void setCombination(SelectedTag c)
  {
    if (c.getTags() == TAGS_SELECTION) {
      m_typeOfCombination = c.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getCombination()
  {
    return new SelectedTag(m_typeOfCombination, TAGS_SELECTION);
  }
  




  public String seedTipText()
  {
    return "Set the random seed.";
  }
  



  public void setSeed(int s)
  {
    m_seed = s;
  }
  



  public int getSeed()
  {
    return m_seed;
  }
  




  public String debugTipText()
  {
    return "Turn on verbose output for monitoring the search's progress.";
  }
  



  public void setDebug(boolean d)
  {
    m_debug = d;
  }
  



  public boolean getDebug()
  {
    return m_debug;
  }
  



  public Enumeration listOptions()
  {
    Vector newVector = new Vector(6);
    
    newVector.addElement(new Option("\tSpecify the number of subsets to generate \n\tin the initial population..", "Z", 1, "-Z <num>"));
    


    newVector.addElement(new Option("\tSpecify the treshold used for considering when a subset is significant.", "T", 1, "-T <threshold>"));
    

    newVector.addElement(new Option("\tSpecify the kind of combiantion \n\tfor using it in the combination method.", "R", 1, "-R <0 = greedy combination | 1 = reduced greedy combination >"));
    

    newVector.addElement(new Option("\tSet the random number seed.\n\t(default = 1)", "S", 1, "-S <seed>"));
    

    newVector.addElement(new Option("\tVerbose output for monitoring the search.", "D", 0, "-D"));
    
    return newVector.elements();
  }
  





























  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    String optionString = Utils.getOption('Z', options);
    if (optionString.length() != 0) {
      setPopulationSize(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('T', options);
    if (optionString.length() != 0) {
      setThreshold(Double.parseDouble(optionString));
    }
    
    optionString = Utils.getOption('R', options);
    if (optionString.length() != 0) {
      setCombination(new SelectedTag(Integer.parseInt(optionString), TAGS_SELECTION));
    }
    else {
      setCombination(new SelectedTag(0, TAGS_SELECTION));
    }
    
    optionString = Utils.getOption('S', options);
    if (optionString.length() != 0) {
      setSeed(Integer.parseInt(optionString));
    }
    
    setDebug(Utils.getFlag('D', options));
  }
  




  public String[] getOptions()
  {
    String[] options = new String[9];
    int current = 0;
    
    options[(current++)] = "-T";
    options[(current++)] = ("" + getThreshold());
    
    options[(current++)] = "-Z";
    options[(current++)] = ("" + getPopulationSize());
    
    options[(current++)] = "-R";
    options[(current++)] = ("" + String.valueOf(getCombination().getSelectedTag().getID()));
    
    options[(current++)] = "-S";
    options[(current++)] = ("" + getSeed());
    
    if (getDebug()) {
      options[(current++)] = "-D";
    }
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  



  public String toString()
  {
    StringBuffer FString = new StringBuffer();
    FString.append("\tScatter Search \n\tInit Population: " + m_calculatedInitialPopSize);
    

    FString.append("\n\tKind of Combination: " + getCombination().getSelectedTag().getReadable());
    

    FString.append("\n\tRandom number seed: " + m_seed);
    
    FString.append("\n\tDebug: " + m_debug);
    
    FString.append("\n\tTreshold: " + Utils.doubleToString(Math.abs(getThreshold()), 8, 3) + "\n");
    

    FString.append("\tTotal number of subsets evaluated: " + m_totalEvals + "\n");
    

    FString.append("\tMerit of best subset found: " + Utils.doubleToString(Math.abs(m_bestMerit), 8, 3) + "\n");
    




    if (m_debug) {
      return FString.toString() + "\n\n" + m_InformationReports.toString();
    }
    return FString.toString();
  }
  









  public int[] search(ASEvaluation ASEval, Instances data)
    throws Exception
  {
    m_totalEvals = 0;
    m_popSize = m_initialPopSize;
    m_calculatedInitialPopSize = m_initialPopSize;
    m_treshold = m_initialThreshold;
    m_processinTime = System.currentTimeMillis();
    m_InformationReports = new StringBuffer();
    
    m_numAttribs = data.numAttributes();
    m_classIndex = data.classIndex();
    
    if (m_popSize <= 0) {
      m_popSize = (m_numAttribs / 2);
      m_calculatedInitialPopSize = m_popSize;
    }
    
    ASEvaluator = ((SubsetEvaluator)ASEval);
    
    if (m_treshold < 0.0D) {
      m_treshold = calculateTreshhold();
      m_totalEvals += 1;
    }
    
    m_random = new Random(m_seed);
    
    m_attributeRanking = RankEachAttribute();
    
    CreatePopulation(m_popSize);
    
    int bestSolutions = m_popSize / 4;
    int divSolutions = m_popSize / 4;
    
    if (m_popSize < 4)
    {
      bestSolutions = m_popSize / 2;
      divSolutions = m_popSize / 2;
      
      if (m_popSize == 1) { return attributeList(m_population.get(0)).subset);
      }
    }
    
    m_ReferenceSet = new ArrayList();
    
    for (int i = 0; i < m_population.size(); i++) {
      m_ReferenceSet.add(m_population.get(i));
    }
    

    GenerateReferenceSet(m_ReferenceSet, bestSolutions, divSolutions);
    

    m_InformationReports.append("Population: " + m_population.size() + "\n");
    m_InformationReports.append("merit    \tsubset\n");
    
    for (int i = 0; i < m_population.size(); i++) {
      m_InformationReports.append(printSubset((Subset)m_population.get(i)));
    }
    
    m_ReferenceSet = m_ReferenceSet.subList(0, bestSolutions + divSolutions);
    


    m_InformationReports.append("\nReferenceSet:");
    m_InformationReports.append("\n----------------Most Significants Solutions--------------\n");
    for (int i = 0; i < m_ReferenceSet.size(); i++) {
      if (i == bestSolutions) m_InformationReports.append("----------------Most Diverses Solutions--------------\n");
      m_InformationReports.append(printSubset((Subset)m_ReferenceSet.get(i)));
    }
    

    Subset bestTemp = new Subset(new BitSet(m_numAttribs), 0.0D);
    
    while (!bestTemp.isEqual((Subset)m_ReferenceSet.get(0)))
    {
      CombineParents();
      ImproveSolutions();
      
      bestTemp = (Subset)m_ReferenceSet.get(0);
      
      int numBest = m_ReferenceSet.size() / 2;
      int numDiverses = m_ReferenceSet.size() / 2;
      
      UpdateReferenceSet(numBest, numDiverses);
      m_ReferenceSet = m_ReferenceSet.subList(0, numBest + numDiverses);
    }
    

    m_InformationReports.append("\nLast Reference Set Updated:\n");
    m_InformationReports.append("merit    \tsubset\n");
    
    for (int i = 0; i < m_ReferenceSet.size(); i++) {
      m_InformationReports.append(printSubset((Subset)m_ReferenceSet.get(i)));
    }
    
    m_bestMerit = merit;
    
    m_processinTime = (System.currentTimeMillis() - m_processinTime);
    
    return attributeList(subset);
  }
  









  public void GenerateReferenceSet(List<Subset> ReferenceSet, int bestSolutions, int divSolutions)
  {
    ReferenceSet = bubbleSubsetSort(ReferenceSet);
    

    BitSet allBits_RefSet = getAllBits(ReferenceSet.subList(0, bestSolutions));
    

    int refSetlength = bestSolutions;
    int total = bestSolutions + divSolutions;
    
    while (refSetlength < total)
    {
      List<Integer> aux = new ArrayList();
      
      for (int i = refSetlength; i < ReferenceSet.size(); i++) {
        aux.add(Integer.valueOf(SimetricDiference(((Subset)ReferenceSet.get(i)).clone(), allBits_RefSet)));
      }
      

      int mostDiv = getIndexofBiggest(aux);
      ReferenceSet.set(refSetlength, ReferenceSet.get(refSetlength + mostDiv));
      

      refSetlength++;
      
      allBits_RefSet = getAllBits(ReferenceSet.subList(0, refSetlength));
    }
    
    ReferenceSet = filterSubset(ReferenceSet, refSetlength);
  }
  






  public void UpdateReferenceSet(int numBestSolutions, int numDivsSolutions)
  {
    for (int i = 0; i < m_parentsCombination.size(); i++) { m_ReferenceSet.add(i, m_parentsCombination.get(i));
    }
    GenerateReferenceSet(m_ReferenceSet, numBestSolutions, numDivsSolutions);
  }
  




  public void ImproveSolutions()
    throws Exception
  {
    for (int i = 0; i < m_parentsCombination.size(); i++)
    {
      BitSet aux1 = (BitSet)m_parentsCombination.get(i)).subset.clone();
      List<Subset> ranking = new ArrayList();
      
















      for (int k = 0; k < m_attributeRanking.size(); k++) {
        Subset s1 = ((Subset)m_attributeRanking.get(k)).clone();
        BitSet b1 = (BitSet)subset.clone();
        
        Subset s2 = ((Subset)m_parentsCombination.get(i)).clone();
        BitSet b2 = (BitSet)subset.clone();
        
        if (!b2.get(b1.nextSetBit(0)))
        {
          b2.or(b1);
          double newMerit = ASEvaluator.evaluateSubset(b2);
          m_totalEvals += 1;
          
          if (newMerit <= merit)
            break;
          m_parentsCombination.set(i, new Subset(b2, newMerit));
        }
      }
      filterSubset(m_parentsCombination, m_ReferenceSet.size());
    }
  }
  





  public void CombineParents()
    throws Exception
  {
    m_parentsCombination = new ArrayList();
    

    for (int i = 0; i < m_ReferenceSet.size() - 1; i++) {
      for (int j = i + 1; j < m_ReferenceSet.size(); j++)
      {

        Subset parent1 = (Subset)m_ReferenceSet.get(i);
        Subset parent2 = (Subset)m_ReferenceSet.get(j);
        

        Subset child1 = intersectSubsets(parent1, parent2);
        Subset child2 = child1.clone();
        

        Subset simDif = simetricDif(parent1, parent2, getCombination().getSelectedTag().getID());
        
        BitSet aux = (BitSet)subset.clone();
        
        boolean improvement = true;
        
        while (improvement)
        {
          Subset best1 = getBestgen(child1, aux);
          Subset best2 = getBestgen(child2, aux);
          
          if ((best1 != null) || (best2 != null))
          {
            if (best2 == null) {
              child1 = best1.clone();

            }
            else if (best1 == null) {
              child2 = best2.clone();

            }
            else if ((best1 != null) && (best2 != null)) {
              double merit1 = merit;
              double merit2 = merit;
              
              if (merit1 > merit2) {
                child1 = best1.clone();

              }
              else if (merit1 < merit2) {
                child2 = best2.clone();

              }
              else if (merit1 == merit2) {
                if (subset.cardinality() > subset.cardinality()) {
                  child2 = best2.clone();
                  continue;
                }
                if (subset.cardinality() < subset.cardinality()) {
                  child1 = best1.clone();
                  continue;
                }
                if (subset.cardinality() == subset.cardinality()) {
                  double random = m_random.nextDouble();
                  if (random < 0.5D) { child1 = best1.clone(); continue; }
                  child2 = best2.clone();
                  continue;
                }
              }
            }
          }
          else {
            m_parentsCombination.add(child1);
            m_parentsCombination.add(child2);
            improvement = false;
          }
        }
      }
    }
    m_parentsCombination = filterSubset(m_parentsCombination, m_ReferenceSet.size());
    
    GenerateReferenceSet(m_parentsCombination, m_ReferenceSet.size() / 2, m_ReferenceSet.size() / 2);
    m_parentsCombination = m_parentsCombination.subList(0, m_ReferenceSet.size());
  }
  






  public void CreatePopulation(int popSize)
    throws Exception
  {
    InitPopulation(popSize);
    

    int segmentation = m_numAttribs / 2;
    







    for (int i = 0; i < m_popSize; i++)
    {
      List<Subset> attributeRankingCopy = new ArrayList();
      for (int j = 0; j < m_attributeRanking.size(); j++) { attributeRankingCopy.add(m_attributeRanking.get(j));
      }
      
      double last_evaluation = -999.0D;
      double current_evaluation = 0.0D;
      
      boolean doneAnew = true;
      

      for (;;)
      {
        int random_number = m_random.nextInt(segmentation + 1);
        
        if ((doneAnew) && (i <= segmentation)) random_number = i;
        doneAnew = false;
        
        Subset s1 = ((Subset)attributeRankingCopy.get(random_number)).clone();
        Subset s2 = ((Subset)m_population.get(i)).clone();
        


        Subset joiners = joinSubsets(s1, s2);
        
        current_evaluation = merit;
        
        if (current_evaluation <= last_evaluation) break;
        m_population.set(i, joiners);
        last_evaluation = current_evaluation;
        try
        {
          attributeRankingCopy.set(random_number, attributeRankingCopy.get(segmentation + 1));
          attributeRankingCopy.remove(segmentation + 1);
        } catch (IndexOutOfBoundsException ex) {
          attributeRankingCopy.set(random_number, new Subset(new BitSet(m_numAttribs), 0.0D));
        }
      }
    }
  }
  

















  public List<Subset> RankEachAttribute()
    throws Exception
  {
    List<Subset> result = new ArrayList();
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (i != m_classIndex)
      {
        BitSet an_Attribute = new BitSet(m_numAttribs);
        an_Attribute.set(i);
        
        double merit = ASEvaluator.evaluateSubset(an_Attribute);
        m_totalEvals += 1;
        
        result.add(new Subset(an_Attribute, merit));
      }
    }
    return bubbleSubsetSort(result);
  }
  










  public Subset getBestgen(Subset subset, BitSet gens)
    throws Exception
  {
    Subset result = null;
    
    double merit1 = merit;
    
    for (int i = gens.nextSetBit(0); i >= 0; i = gens.nextSetBit(i + 1)) {
      BitSet aux = (BitSet)subset.clone();
      
      if (!aux.get(i)) {
        aux.set(i);
        
        double merit2 = ASEvaluator.evaluateSubset(aux);
        m_totalEvals += 1;
        
        if (merit2 > merit1) {
          merit1 = merit2;
          result = new Subset(aux, merit1);
        }
      }
    }
    return result;
  }
  





  public List<Subset> bubbleSubsetSort(List<Subset> subsetList)
  {
    List<Subset> result = new ArrayList();
    
    for (int i = 0; i < subsetList.size() - 1; i++) {
      Subset subset1 = (Subset)subsetList.get(i);
      double merit1 = merit;
      
      for (int j = i + 1; j < subsetList.size(); j++) {
        Subset subset2 = (Subset)subsetList.get(j);
        double merit2 = merit;
        
        if (merit2 > merit1) {
          Subset temp = subset1;
          
          subsetList.set(i, subset2);
          subsetList.set(j, temp);
          
          subset1 = subset2;
          merit1 = merit;
        }
      }
    }
    return subsetList;
  }
  






  public int getIndexofBiggest(List<Integer> simDif)
  {
    int aux = -99999;
    int result1 = -1;
    List<Integer> equalSimDif = new ArrayList();
    
    if (simDif.size() == 0) { return -1;
    }
    for (int i = 0; i < simDif.size(); i++) {
      if (((Integer)simDif.get(i)).intValue() > aux) {
        aux = ((Integer)simDif.get(i)).intValue();
        result1 = i;
      }
    }
    
    for (int i = 0; i < simDif.size(); i++) {
      if (((Integer)simDif.get(i)).intValue() == aux) {
        equalSimDif.add(Integer.valueOf(i));
      }
    }
    
    int finalResult = ((Integer)equalSimDif.get(m_random.nextInt(equalSimDif.size()))).intValue();
    
    return finalResult;
  }
  





  public BitSet getAllBits(List<Subset> subsets)
  {
    BitSet result = new BitSet(m_numAttribs);
    
    for (int i = 0; i < subsets.size(); i++) {
      BitSet aux = getclonesubset;
      
      for (int j = aux.nextSetBit(0); j >= 0; j = aux.nextSetBit(j + 1)) {
        result.set(j);
      }
    }
    
    return result;
  }
  




  public void InitPopulation(int popSize)
  {
    m_population = new ArrayList();
    for (int i = 0; i < popSize; i++) { m_population.add(new Subset(new BitSet(m_numAttribs), 0.0D));
    }
  }
  






  public Subset joinSubsets(Subset subset1, Subset subset2)
    throws Exception
  {
    BitSet b1 = (BitSet)subset.clone();
    BitSet b2 = (BitSet)subset.clone();
    
    b1.or(b2);
    
    double newMerit = ASEvaluator.evaluateSubset(b1);
    m_totalEvals += 1;
    
    return new Subset((BitSet)b1.clone(), newMerit);
  }
  







  public Subset intersectSubsets(Subset subset1, Subset subset2)
    throws Exception
  {
    BitSet b1 = (BitSet)subset.clone();
    BitSet b2 = (BitSet)subset.clone();
    
    b1.and(b2);
    
    double newMerit = ASEvaluator.evaluateSubset(b1);
    m_totalEvals += 1;
    
    return new Subset((BitSet)b1.clone(), newMerit);
  }
  
  public Subset simetricDif(Subset subset1, Subset subset2, int mode) throws Exception
  {
    BitSet b1 = (BitSet)subset.clone();
    BitSet b2 = (BitSet)subset.clone();
    
    b1.xor(b2);
    
    double newMerit = ASEvaluator.evaluateSubset(b1);
    m_totalEvals += 1;
    
    Subset result = new Subset((BitSet)b1.clone(), newMerit);
    
    if (mode == 1)
    {
      double avgAcurracy = 0.0D;
      int totalSolutions = 0;
      List<Subset> weightVector = new ArrayList();
      
      BitSet res = subset;
      for (int i = res.nextSetBit(0); i >= 0; i = res.nextSetBit(i + 1))
      {
        double merits = 0.0D;
        int numSolutions = 0;
        Subset solution = null;
        
        for (int j = 0; j < m_ReferenceSet.size(); j++) {
          solution = (Subset)m_ReferenceSet.get(j);
          if (subset.get(i)) {
            merits += merit;
            numSolutions++;
          }
        }
        BitSet b = new BitSet(m_numAttribs);
        b.set(i);
        Subset s = new Subset(b, merits / numSolutions);
        weightVector.add(s);
        
        avgAcurracy += merits;
        totalSolutions++;
      }
      
      avgAcurracy /= totalSolutions;
      
      BitSet newResult = new BitSet(m_numAttribs);
      for (int i = 0; i < weightVector.size(); i++) {
        Subset aux = (Subset)weightVector.get(i);
        if (merit >= avgAcurracy) {
          newResult.or(subset);
        }
      }
      double merit = ASEvaluator.evaluateSubset(newResult);
      result = new Subset(newResult, merit);
    }
    

    return result;
  }
  
  public int generateRandomNumber(int limit)
  {
    return (int)Math.round(Math.random() * (limit + 0.4D));
  }
  







  public double calculateTreshhold()
    throws Exception
  {
    BitSet fullSet = new BitSet(m_numAttribs);
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (i != m_classIndex) {
        fullSet.set(i);
      }
    }
    return ASEvaluator.evaluateSubset(fullSet);
  }
  





  public int SimetricDiference(Subset subset, BitSet bitset)
  {
    BitSet aux = clonesubset;
    aux.xor(bitset);
    
    return aux.cardinality();
  }
  








  public List<Subset> filterSubset(List<Subset> subsetList, int preferredSize)
  {
    if ((subsetList.size() <= preferredSize) && (preferredSize != -1)) { return subsetList;
    }
    for (int i = 0; i < subsetList.size() - 1; i++) {
      for (int j = i + 1; j < subsetList.size(); j++) {
        Subset focus = (Subset)subsetList.get(i);
        if (focus.isEqual((Subset)subsetList.get(j))) {
          subsetList.remove(j);
          j--;
          
          if ((subsetList.size() <= preferredSize) && (preferredSize != -1)) return subsetList;
        }
      }
    }
    return subsetList;
  }
  



  public String printSubset(Subset subset)
  {
    StringBuffer bufferString = new StringBuffer();
    
    if (subset == null)
    {
      return "";
    }
    
    BitSet bits = subset;
    double merit = merit;
    List<Integer> indexes = new ArrayList();
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (bits.get(i))
      {
        indexes.add(Integer.valueOf(i + 1));
      }
    }
    
    bufferString.append(Utils.doubleToString(merit, 8, 5) + "\t " + indexes.toString() + "\n");
    

    return bufferString.toString();
  }
  

  protected void resetOptions()
  {
    m_popSize = -1;
    m_initialPopSize = -1;
    m_calculatedInitialPopSize = -1;
    m_treshold = -1.0D;
    m_typeOfCombination = 0;
    m_seed = 1;
    m_debug = true;
    m_totalEvals = 0;
    m_bestMerit = 0.0D;
    m_processinTime = 0L;
  }
  




  public int[] attributeList(BitSet group)
  {
    int count = 0;
    

    for (int i = 0; i < m_numAttribs; i++) {
      if (group.get(i)) {
        count++;
      }
    }
    
    int[] list = new int[count];
    count = 0;
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (group.get(i)) {
        list[(count++)] = i;
      }
    }
    
    return list;
  }
  

  public class Subset
    implements Serializable
  {
    double merit;
    BitSet subset;
    
    public Subset(BitSet subset, double merit)
    {
      this.subset = ((BitSet)subset.clone());
      this.merit = merit;
    }
    
    public boolean isEqual(Subset othersubset) {
      if (subset.equals(subset)) return true;
      return false;
    }
    
    public Subset clone() {
      return new Subset(ScatterSearchV1.this, (BitSet)subset.clone(), merit);
    }
  }
}

package weka.attributeSelection;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;













































































































































public class GeneticSearch
  extends ASSearch
  implements StartSetHandler, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -1618264232838472679L;
  private int[] m_starting;
  private Range m_startRange;
  private boolean m_hasClass;
  private int m_classIndex;
  private int m_numAttribs;
  private GABitSet[] m_population;
  private int m_popSize;
  private GABitSet m_best;
  private int m_bestFeatureCount;
  private int m_lookupTableSize;
  private Hashtable m_lookupTable;
  private Random m_random;
  private int m_seed;
  private double m_pCrossover;
  private double m_pMutation;
  private double m_sumFitness;
  private double m_maxFitness;
  private double m_minFitness;
  private double m_avgFitness;
  private int m_maxGenerations;
  private int m_reportFrequency;
  private StringBuffer m_generationReports;
  
  protected class GABitSet
    implements Cloneable, Serializable, RevisionHandler
  {
    static final long serialVersionUID = -2930607837482622224L;
    private BitSet m_chromosome;
    private double m_objective = -1.7976931348623157E308D;
    

    private double m_fitness;
    


    public GABitSet()
    {
      m_chromosome = new BitSet();
    }
    



    public Object clone()
      throws CloneNotSupportedException
    {
      GABitSet temp = new GABitSet(GeneticSearch.this);
      
      temp.setObjective(getObjective());
      temp.setFitness(getFitness());
      temp.setChromosome((BitSet)m_chromosome.clone());
      return temp;
    }
    




    public void setObjective(double objective)
    {
      m_objective = objective;
    }
    



    public double getObjective()
    {
      return m_objective;
    }
    



    public void setFitness(double fitness)
    {
      m_fitness = fitness;
    }
    



    public double getFitness()
    {
      return m_fitness;
    }
    



    public BitSet getChromosome()
    {
      return m_chromosome;
    }
    



    public void setChromosome(BitSet c)
    {
      m_chromosome = c;
    }
    



    public void clear(int bit)
    {
      m_chromosome.clear(bit);
    }
    



    public void set(int bit)
    {
      m_chromosome.set(bit);
    }
    




    public boolean get(int bit)
    {
      return m_chromosome.get(bit);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 6759 $");
    }
  }
  



  public Enumeration listOptions()
  {
    Vector newVector = new Vector(6);
    
    newVector.addElement(new Option("\tSpecify a starting set of attributes.\n\tEg. 1,3,5-7.If supplied, the starting set becomes\n\tone member of the initial random\n\tpopulation.", "P", 1, "-P <start set>"));
    





    newVector.addElement(new Option("\tSet the size of the population (even number).\n\t(default = 20).", "Z", 1, "-Z <population size>"));
    


    newVector.addElement(new Option("\tSet the number of generations.\n\t(default = 20)", "G", 1, "-G <number of generations>"));
    

    newVector.addElement(new Option("\tSet the probability of crossover.\n\t(default = 0.6)", "C", 1, "-C <probability of crossover>"));
    


    newVector.addElement(new Option("\tSet the probability of mutation.\n\t(default = 0.033)", "M", 1, "-M <probability of mutation>"));
    


    newVector.addElement(new Option("\tSet frequency of generation reports.\n\te.g, setting the value to 5 will \n\treport every 5th generation\n\t(default = number of generations)", "R", 1, "-R <report frequency>"));
    



    newVector.addElement(new Option("\tSet the random number seed.\n\t(default = 1)", "S", 1, "-S <seed>"));
    

    return newVector.elements();
  }
  












































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    String optionString = Utils.getOption('P', options);
    if (optionString.length() != 0) {
      setStartSet(optionString);
    }
    
    optionString = Utils.getOption('Z', options);
    if (optionString.length() != 0) {
      setPopulationSize(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('G', options);
    if (optionString.length() != 0) {
      setMaxGenerations(Integer.parseInt(optionString));
      setReportFrequency(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('C', options);
    if (optionString.length() != 0) {
      setCrossoverProb(new Double(optionString).doubleValue());
    }
    
    optionString = Utils.getOption('M', options);
    if (optionString.length() != 0) {
      setMutationProb(new Double(optionString).doubleValue());
    }
    
    optionString = Utils.getOption('R', options);
    if (optionString.length() != 0) {
      setReportFrequency(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('S', options);
    if (optionString.length() != 0) {
      setSeed(Integer.parseInt(optionString));
    }
  }
  




  public String[] getOptions()
  {
    String[] options = new String[14];
    int current = 0;
    
    if (!getStartSet().equals("")) {
      options[(current++)] = "-P";
      options[(current++)] = ("" + startSetToString());
    }
    options[(current++)] = "-Z";
    options[(current++)] = ("" + getPopulationSize());
    options[(current++)] = "-G";
    options[(current++)] = ("" + getMaxGenerations());
    options[(current++)] = "-C";
    options[(current++)] = ("" + getCrossoverProb());
    options[(current++)] = "-M";
    options[(current++)] = ("" + getMutationProb());
    options[(current++)] = "-R";
    options[(current++)] = ("" + getReportFrequency());
    options[(current++)] = "-S";
    options[(current++)] = ("" + getSeed());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String startSetTipText()
  {
    return "Set a start point for the search. This is specified as a comma seperated list off attribute indexes starting at 1. It can include ranges. Eg. 1,2,5-9,17. The start set becomes one of the population members of the initial population.";
  }
  









  public void setStartSet(String startSet)
    throws Exception
  {
    m_startRange.setRanges(startSet);
  }
  



  public String getStartSet()
  {
    return m_startRange.getRanges();
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
  




  public String reportFrequencyTipText()
  {
    return "Set how frequently reports are generated. Default is equal to the number of generations meaning that a report will be printed for initial and final generations. Setting the value to 5 will result in a report being printed every 5 generations.";
  }
  






  public void setReportFrequency(int f)
  {
    m_reportFrequency = f;
  }
  



  public int getReportFrequency()
  {
    return m_reportFrequency;
  }
  




  public String mutationProbTipText()
  {
    return "Set the probability of mutation occuring.";
  }
  



  public void setMutationProb(double m)
  {
    m_pMutation = m;
  }
  



  public double getMutationProb()
  {
    return m_pMutation;
  }
  




  public String crossoverProbTipText()
  {
    return "Set the probability of crossover. This is the probability that two population members will exchange genetic material.";
  }
  





  public void setCrossoverProb(double c)
  {
    m_pCrossover = c;
  }
  



  public double getCrossoverProb()
  {
    return m_pCrossover;
  }
  




  public String maxGenerationsTipText()
  {
    return "Set the number of generations to evaluate.";
  }
  



  public void setMaxGenerations(int m)
  {
    m_maxGenerations = m;
  }
  



  public int getMaxGenerations()
  {
    return m_maxGenerations;
  }
  




  public String populationSizeTipText()
  {
    return "Set the population size (even number), this is the number of individuals (attribute sets) in the population.";
  }
  




  public void setPopulationSize(int p)
  {
    if (p % 2 == 0) {
      m_popSize = p;
    } else {
      System.out.println("Population size needs to be an even number!");
    }
  }
  


  public int getPopulationSize()
  {
    return m_popSize;
  }
  




  public String globalInfo()
  {
    return "GeneticSearch:\n\nPerforms a search using the simple genetic algorithm described in Goldberg (1989).\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.BOOK);
    result.setValue(TechnicalInformation.Field.AUTHOR, "David E. Goldberg");
    result.setValue(TechnicalInformation.Field.YEAR, "1989");
    result.setValue(TechnicalInformation.Field.TITLE, "Genetic algorithms in search, optimization and machine learning");
    result.setValue(TechnicalInformation.Field.ISBN, "0201157675");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Addison-Wesley");
    
    return result;
  }
  


  public GeneticSearch()
  {
    resetOptions();
  }
  








  private String startSetToString()
  {
    StringBuffer FString = new StringBuffer();
    

    if (m_starting == null) {
      return getStartSet();
    }
    
    for (int i = 0; i < m_starting.length; i++) {
      boolean didPrint = false;
      
      if ((!m_hasClass) || ((m_hasClass == true) && (i != m_classIndex)))
      {
        FString.append(m_starting[i] + 1);
        didPrint = true;
      }
      
      if (i == m_starting.length - 1) {
        FString.append("");

      }
      else if (didPrint) {
        FString.append(",");
      }
    }
    

    return FString.toString();
  }
  



  public String toString()
  {
    StringBuffer GAString = new StringBuffer();
    GAString.append("\tGenetic search.\n\tStart set: ");
    
    if (m_starting == null) {
      GAString.append("no attributes\n");
    }
    else {
      GAString.append(startSetToString() + "\n");
    }
    GAString.append("\tPopulation size: " + m_popSize);
    GAString.append("\n\tNumber of generations: " + m_maxGenerations);
    GAString.append("\n\tProbability of crossover: " + Utils.doubleToString(m_pCrossover, 6, 3));
    
    GAString.append("\n\tProbability of mutation: " + Utils.doubleToString(m_pMutation, 6, 3));
    
    GAString.append("\n\tReport frequency: " + m_reportFrequency);
    GAString.append("\n\tRandom number seed: " + m_seed + "\n");
    GAString.append(m_generationReports.toString());
    return GAString.toString();
  }
  








  public int[] search(ASEvaluation ASEval, Instances data)
    throws Exception
  {
    m_best = null;
    m_generationReports = new StringBuffer();
    
    if (!(ASEval instanceof SubsetEvaluator)) {
      throw new Exception(ASEval.getClass().getName() + " is not a " + "Subset evaluator!");
    }
    


    if ((ASEval instanceof UnsupervisedSubsetEvaluator)) {
      m_hasClass = false;
    }
    else {
      m_hasClass = true;
      m_classIndex = data.classIndex();
    }
    
    SubsetEvaluator ASEvaluator = (SubsetEvaluator)ASEval;
    m_numAttribs = data.numAttributes();
    
    m_startRange.setUpper(m_numAttribs - 1);
    if (!getStartSet().equals("")) {
      m_starting = m_startRange.getSelection();
    }
    

    m_lookupTable = new Hashtable(m_lookupTableSize);
    m_random = new Random(m_seed);
    m_population = new GABitSet[m_popSize];
    

    initPopulation();
    evaluatePopulation(ASEvaluator);
    populationStatistics();
    scalePopulation();
    checkBest();
    m_generationReports.append(populationReport(0));
    

    for (int i = 1; i <= m_maxGenerations; i++) {
      generation();
      evaluatePopulation(ASEvaluator);
      populationStatistics();
      scalePopulation();
      
      boolean converged = checkBest();
      
      if ((i == m_maxGenerations) || (i % m_reportFrequency == 0) || (converged == true))
      {

        m_generationReports.append(populationReport(i));
        if (converged == true) {
          break;
        }
      }
    }
    return attributeList(m_best.getChromosome());
  }
  




  private int[] attributeList(BitSet group)
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
  






  private boolean checkBest()
    throws Exception
  {
    int lowestCount = m_numAttribs;
    double b = -1.7976931348623157E308D;
    GABitSet localbest = null;
    
    boolean converged = false;
    int oldcount = Integer.MAX_VALUE;
    
    if (m_maxFitness - m_minFitness > 0.0D)
    {
      for (int i = 0; i < m_popSize; i++) {
        if (m_population[i].getObjective() > b) {
          b = m_population[i].getObjective();
          localbest = m_population[i];
          oldcount = countFeatures(localbest.getChromosome());
        } else if (Utils.eq(m_population[i].getObjective(), b))
        {
          int count = countFeatures(m_population[i].getChromosome());
          if (count < oldcount) {
            b = m_population[i].getObjective();
            localbest = m_population[i];
            oldcount = count;
          }
        }
      }
    }
    
    for (int i = 0; i < m_popSize; i++) {
      BitSet temp = m_population[i].getChromosome();
      int count = countFeatures(temp);
      
      if (count < lowestCount) {
        lowestCount = count;
        localbest = m_population[i];
        b = localbest.getObjective();
      }
    }
    converged = true;
    


    int count = 0;
    BitSet temp = localbest.getChromosome();
    count = countFeatures(temp);
    

    if (m_best == null) {
      m_best = ((GABitSet)localbest.clone());
      m_bestFeatureCount = count;
    } else if (b > m_best.getObjective()) {
      m_best = ((GABitSet)localbest.clone());
      m_bestFeatureCount = count;
    } else if (Utils.eq(m_best.getObjective(), b))
    {
      if (count < m_bestFeatureCount) {
        m_best = ((GABitSet)localbest.clone());
        m_bestFeatureCount = count;
      }
    }
    return converged;
  }
  




  private int countFeatures(BitSet featureSet)
  {
    int count = 0;
    for (int i = 0; i < m_numAttribs; i++) {
      if (featureSet.get(i)) {
        count++;
      }
    }
    return count;
  }
  


  private void generation()
    throws Exception
  {
    int j = 0;
    double best_fit = -1.7976931348623157E308D;
    int old_count = 0;
    
    GABitSet[] newPop = new GABitSet[m_popSize];
    



    for (int i = 0; i < m_popSize; i++) {
      if (m_population[i].getFitness() > best_fit) {
        j = i;
        best_fit = m_population[i].getFitness();
        old_count = countFeatures(m_population[i].getChromosome());
      } else if (Utils.eq(m_population[i].getFitness(), best_fit)) {
        int count = countFeatures(m_population[i].getChromosome());
        if (count < old_count) {
          j = i;
          best_fit = m_population[i].getFitness();
          old_count = count;
        }
      }
    }
    newPop[0] = ((GABitSet)(GABitSet)m_population[j].clone());
    newPop[1] = newPop[0];
    
    for (j = 2; j < m_popSize; j += 2) {
      int parent1 = select();
      int parent2 = select();
      newPop[j] = ((GABitSet)(GABitSet)m_population[parent1].clone());
      newPop[(j + 1)] = ((GABitSet)(GABitSet)m_population[parent2].clone());
      
      if (parent1 == parent2)
      {
        if (m_hasClass) { int r;
          while ((r = m_random.nextInt(m_numAttribs)) == m_classIndex) {}
        }
        
        int r = m_random.nextInt(m_numAttribs);
        

        if (newPop[j].get(r)) {
          newPop[j].clear(r);
        }
        else {
          newPop[j].set(r);
        }
      }
      else {
        double r = m_random.nextDouble();
        if ((m_numAttribs >= 3) && 
          (r < m_pCrossover))
        {
          int cp = Math.abs(m_random.nextInt());
          
          cp %= (m_numAttribs - 2);
          cp++;
          
          for (i = 0; i < cp; i++) {
            if (m_population[parent1].get(i)) {
              newPop[(j + 1)].set(i);
            }
            else {
              newPop[(j + 1)].clear(i);
            }
            if (m_population[parent2].get(i)) {
              newPop[j].set(i);
            }
            else {
              newPop[j].clear(i);
            }
          }
        }
        


        for (int k = 0; k < 2; k++) {
          for (i = 0; i < m_numAttribs; i++) {
            r = m_random.nextDouble();
            if ((r < m_pMutation) && (
              (!m_hasClass) || (i != m_classIndex)))
            {


              if (newPop[(j + k)].get(i)) {
                newPop[(j + k)].clear(i);
              }
              else {
                newPop[(j + k)].set(i);
              }
            }
          }
        }
      }
    }
    


    m_population = newPop;
  }
  






  private int select()
  {
    double partsum = 0.0D;
    double r = m_random.nextDouble() * m_sumFitness;
    for (int i = 0; i < m_popSize; i++) {
      partsum += m_population[i].getFitness();
      if ((partsum >= r) || (i == m_popSize - 1)) {
        break;
      }
    }
    

    if (i == m_popSize) {
      i = 0;
    }
    return i;
  }
  










  private void evaluatePopulation(SubsetEvaluator ASEvaluator)
    throws Exception
  {
    for (int i = 0; i < m_popSize; i++)
    {
      if (!m_lookupTable.containsKey(m_population[i].getChromosome()))
      {
        double merit = ASEvaluator.evaluateSubset(m_population[i].getChromosome());
        m_population[i].setObjective(merit);
        m_lookupTable.put(m_population[i].getChromosome(), m_population[i]);
      } else {
        GABitSet temp = (GABitSet)m_lookupTable.get(m_population[i].getChromosome());
        
        m_population[i].setObjective(temp.getObjective());
      }
    }
  }
  







  private void initPopulation()
    throws Exception
  {
    int start = 0;
    

    if (m_starting != null) {
      m_population[0] = new GABitSet();
      for (int i = 0; i < m_starting.length; i++) {
        if (m_starting[i] != m_classIndex) {
          m_population[0].set(m_starting[i]);
        }
      }
      start = 1;
    }
    
    for (int i = start; i < m_popSize; i++) {
      m_population[i] = new GABitSet();
      
      int num_bits = m_random.nextInt();
      num_bits = num_bits % m_numAttribs - 1;
      if (num_bits < 0) {
        num_bits *= -1;
      }
      if (num_bits == 0) {
        num_bits = 1;
      }
      
      for (int j = 0; j < num_bits; j++) {
        boolean ok = false;
        int bit;
        do { bit = m_random.nextInt();
          if (bit < 0) {
            bit *= -1;
          }
          bit %= m_numAttribs;
          if (m_hasClass) {
            if (bit != m_classIndex) {
              ok = true;
            }
          }
          else {
            ok = true;
          }
        } while (!ok);
        
        if (bit > m_numAttribs) {
          throw new Exception("Problem in population init");
        }
        m_population[i].set(bit);
      }
    }
  }
  




  private void populationStatistics()
  {
    m_sumFitness = (this.m_minFitness = this.m_maxFitness = m_population[0].getObjective());
    

    for (int i = 1; i < m_popSize; i++) {
      m_sumFitness += m_population[i].getObjective();
      if (m_population[i].getObjective() > m_maxFitness) {
        m_maxFitness = m_population[i].getObjective();
      }
      else if (m_population[i].getObjective() < m_minFitness) {
        m_minFitness = m_population[i].getObjective();
      }
    }
    m_avgFitness = (m_sumFitness / m_popSize);
  }
  



  private void scalePopulation()
  {
    double a = 0.0D;
    double b = 0.0D;
    double fmultiple = 2.0D;
    


    if (m_minFitness > (fmultiple * m_avgFitness - m_maxFitness) / (fmultiple - 1.0D))
    {
      double delta = m_maxFitness - m_avgFitness;
      a = (fmultiple - 1.0D) * m_avgFitness / delta;
      b = m_avgFitness * (m_maxFitness - fmultiple * m_avgFitness) / delta;
    }
    else {
      double delta = m_avgFitness - m_minFitness;
      a = m_avgFitness / delta;
      b = -m_minFitness * m_avgFitness / delta;
    }
    

    m_sumFitness = 0.0D;
    for (int j = 0; j < m_popSize; j++) {
      if ((a == Double.POSITIVE_INFINITY) || (a == Double.NEGATIVE_INFINITY) || (b == Double.POSITIVE_INFINITY) || (b == Double.NEGATIVE_INFINITY))
      {
        m_population[j].setFitness(m_population[j].getObjective());
      } else {
        m_population[j].setFitness(Math.abs(a * m_population[j].getObjective() + b));
      }
      
      m_sumFitness += m_population[j].getFitness();
    }
  }
  




  private String populationReport(int genNum)
  {
    StringBuffer temp = new StringBuffer();
    
    if (genNum == 0) {
      temp.append("\nInitial population\n");
    }
    else {
      temp.append("\nGeneration: " + genNum + "\n");
    }
    temp.append("merit   \tscaled  \tsubset\n");
    
    for (int i = 0; i < m_popSize; i++) {
      temp.append(Utils.doubleToString(Math.abs(m_population[i].getObjective()), 8, 5) + "\t" + Utils.doubleToString(m_population[i].getFitness(), 8, 5) + "\t");
      






      temp.append(printPopMember(m_population[i].getChromosome()) + "\n");
    }
    return temp.toString();
  }
  




  private String printPopMember(BitSet temp)
  {
    StringBuffer text = new StringBuffer();
    
    for (int j = 0; j < m_numAttribs; j++) {
      if (temp.get(j)) {
        text.append(j + 1 + " ");
      }
    }
    return text.toString();
  }
  




  private String printPopChrom(BitSet temp)
  {
    StringBuffer text = new StringBuffer();
    
    for (int j = 0; j < m_numAttribs; j++) {
      if (temp.get(j)) {
        text.append("1");
      } else {
        text.append("0");
      }
    }
    return text.toString();
  }
  


  private void resetOptions()
  {
    m_population = null;
    m_popSize = 20;
    m_lookupTableSize = 1001;
    m_pCrossover = 0.6D;
    m_pMutation = 0.033D;
    m_maxGenerations = 20;
    m_reportFrequency = m_maxGenerations;
    m_starting = null;
    m_startRange = new Range();
    m_seed = 1;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6759 $");
  }
}

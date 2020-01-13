package weka.attributeSelection;

import java.io.PrintStream;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;




















































































































public class RandomSearch
  extends ASSearch
  implements StartSetHandler, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 7479392617377425484L;
  private int[] m_starting;
  private Range m_startRange;
  private BitSet m_bestGroup;
  private double m_bestMerit;
  private boolean m_onlyConsiderBetterAndSmaller;
  private boolean m_hasClass;
  private int m_classIndex;
  private int m_numAttribs;
  private int m_seed;
  private double m_searchSize;
  private int m_iterations;
  private Random m_random;
  private boolean m_verbose;
  
  public String globalInfo()
  {
    return "RandomSearch : \n\nPerforms a Random search in the space of attribute subsets. If no start set is supplied, Random search starts from a random point and reports the best subset found. If a start set is supplied, Random searches randomly for subsets that are as good or better than the start point with the same or or fewer attributes. Using RandomSearch in conjunction with a start set containing all attributes equates to the LVF algorithm of Liu and Setiono (ICML-96).\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  

















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "H. Liu and R. Setiono");
    result.setValue(TechnicalInformation.Field.TITLE, "A probabilistic approach to feature selection - A filter solution");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "13th International Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "1996");
    result.setValue(TechnicalInformation.Field.PAGES, "319-327");
    
    return result;
  }
  


  public RandomSearch()
  {
    resetOptions();
  }
  



  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tSpecify a starting set of attributes.\n\tEg. 1,3,5-7.\n\tIf a start point is supplied,\n\trandom search evaluates the start\n\tpoint and then randomly looks for\n\tsubsets that are as good as or better\n\tthan the start point with the same\n\tor lower cardinality.", "P", 1, "-P <start set>"));
    









    newVector.addElement(new Option("\tPercent of search space to consider.\n\t(default = 25%).", "F", 1, "-F <percent> "));
    


    newVector.addElement(new Option("\tOutput subsets as the search progresses.\n\t(default = false).", "V", 0, "-V"));
    


    newVector.addElement(new Option("\tRandom seed\n\t(default = 1)", "seed", 1, "-seed <num>"));
    
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
    
    optionString = Utils.getOption('F', options);
    if (optionString.length() != 0) {
      setSearchPercent(new Double(optionString).doubleValue());
    }
    
    setVerbose(Utils.getFlag('V', options));
    
    optionString = Utils.getOption("seed", options);
    if (optionString.length() > 0) {
      setSeed(Integer.parseInt(optionString));
    }
  }
  



  public String[] getOptions()
  {
    String[] options = new String[7];
    int current = 0;
    
    if (m_verbose) {
      options[(current++)] = "-V";
    }
    
    if (!getStartSet().equals("")) {
      options[(current++)] = "-P";
      options[(current++)] = ("" + startSetToString());
    }
    
    options[(current++)] = "-F";
    options[(current++)] = ("" + getSearchPercent());
    
    options[(current++)] = "-seed";
    options[(current++)] = ("" + getSeed());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  




  public String startSetTipText()
  {
    return "Set the start point for the search. This is specified as a comma seperated list off attribute indexes starting at 1. It can include ranges. Eg. 1,2,5-9,17. If specified, Random searches for subsets of attributes that are as good as or better than the start set with the same or lower cardinality.";
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
  




  public String verboseTipText()
  {
    return "Print progress information. Sends progress info to the terminal as the search progresses.";
  }
  




  public void setVerbose(boolean v)
  {
    m_verbose = v;
  }
  



  public boolean getVerbose()
  {
    return m_verbose;
  }
  




  public String searchPercentTipText()
  {
    return "Percentage of the search space to explore.";
  }
  



  public void setSearchPercent(double p)
  {
    p = Math.abs(p);
    if (p == 0.0D) {
      p = 25.0D;
    }
    
    if (p > 100.0D) {
      p = 100.0D;
    }
    
    m_searchSize = (p / 100.0D);
  }
  




  public String seedTipText()
  {
    return "Seed for the random number generator";
  }
  




  public void setSeed(int seed)
  {
    m_seed = seed;
  }
  




  public int getSeed()
  {
    return m_seed;
  }
  



  public double getSearchPercent()
  {
    return m_searchSize * 100.0D;
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
    StringBuffer text = new StringBuffer();
    
    text.append("\tRandom search.\n\tStart set: ");
    if (m_starting == null) {
      text.append("no attributes\n");
    }
    else {
      text.append(startSetToString() + "\n");
    }
    text.append("\tNumber of iterations: " + m_iterations + " (" + m_searchSize * 100.0D + "% of the search space)\n");
    
    text.append("\tMerit of best subset found: " + Utils.doubleToString(Math.abs(m_bestMerit), 8, 3) + "\n");
    

    return text.toString();
  }
  








  public int[] search(ASEvaluation ASEval, Instances data)
    throws Exception
  {
    int sizeOfBest = m_numAttribs;
    
    m_bestGroup = new BitSet(m_numAttribs);
    
    m_onlyConsiderBetterAndSmaller = false;
    if (!(ASEval instanceof SubsetEvaluator)) {
      throw new Exception(ASEval.getClass().getName() + " is not a " + "Subset evaluator!");
    }
    


    m_random = new Random(m_seed);
    
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
    
    double best_merit;
    if (m_starting != null) {
      for (int i = 0; i < m_starting.length; i++) {
        if (m_starting[i] != m_classIndex) {
          m_bestGroup.set(m_starting[i]);
        }
      }
      m_onlyConsiderBetterAndSmaller = true;
      double best_merit = ASEvaluator.evaluateSubset(m_bestGroup);
      sizeOfBest = countFeatures(m_bestGroup);
    }
    else {
      m_bestGroup = generateRandomSubset();
      best_merit = ASEvaluator.evaluateSubset(m_bestGroup);
    }
    
    if (m_verbose) {
      System.out.println("Initial subset (" + Utils.doubleToString(Math.abs(best_merit), 8, 5) + "): " + printSubset(m_bestGroup));
    }
    

    int i;
    

    if (m_hasClass) {
      i = m_numAttribs - 1;
    } else {
      i = m_numAttribs;
    }
    m_iterations = ((int)(m_searchSize * Math.pow(2.0D, i)));
    



    for (int i = 0; i < m_iterations; i++) {
      BitSet temp = generateRandomSubset();
      if (m_onlyConsiderBetterAndSmaller) {
        int tempSize = countFeatures(temp);
        if (tempSize <= sizeOfBest) {
          double tempMerit = ASEvaluator.evaluateSubset(temp);
          if (tempMerit >= best_merit) {
            sizeOfBest = tempSize;
            m_bestGroup = temp;
            best_merit = tempMerit;
            if (m_verbose) {
              System.out.print("New best subset (" + Utils.doubleToString(Math.abs(best_merit), 8, 5) + "): " + printSubset(m_bestGroup) + " :");
              


              System.out.println(Utils.doubleToString(i / m_iterations * 100.0D, 5, 1) + "% done");
            }
            
          }
          
        }
      }
      else
      {
        double tempMerit = ASEvaluator.evaluateSubset(temp);
        if (tempMerit > best_merit) {
          m_bestGroup = temp;
          best_merit = tempMerit;
          if (m_verbose) {
            System.out.print("New best subset (" + Utils.doubleToString(Math.abs(best_merit), 8, 5) + "): " + printSubset(m_bestGroup) + " :");
            

            System.out.println(Utils.doubleToString(i / m_iterations * 100.0D, 5, 1) + "% done");
          }
        }
      }
    }
    



    m_bestMerit = best_merit;
    return attributeList(m_bestGroup);
  }
  




  private String printSubset(BitSet temp)
  {
    StringBuffer text = new StringBuffer();
    
    for (int j = 0; j < m_numAttribs; j++) {
      if (temp.get(j)) {
        text.append(j + 1 + " ");
      }
    }
    return text.toString();
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
  



  private BitSet generateRandomSubset()
  {
    BitSet temp = new BitSet(m_numAttribs);
    

    for (int i = 0; i < m_numAttribs; i++) {
      double r = m_random.nextDouble();
      if ((r <= 0.5D) && (
        (!m_hasClass) || (i != m_classIndex)))
      {
        temp.set(i);
      }
    }
    
    return temp;
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
  


  private void resetOptions()
  {
    m_starting = null;
    m_startRange = new Range();
    m_searchSize = 0.25D;
    m_seed = 1;
    m_onlyConsiderBetterAndSmaller = false;
    m_verbose = false;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8949 $");
  }
}

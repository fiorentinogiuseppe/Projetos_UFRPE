package weka.attributeSelection;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;












































































public class Ranker
  extends ASSearch
  implements RankedOutputSearch, StartSetHandler, OptionHandler
{
  static final long serialVersionUID = -9086714848510751934L;
  private int[] m_starting;
  private Range m_startRange;
  private int[] m_attributeList;
  private double[] m_attributeMerit;
  private boolean m_hasClass;
  private int m_classIndex;
  private int m_numAttribs;
  private double m_threshold;
  private int m_numToSelect = -1;
  

  private int m_calculatedNumToSelect = -1;
  




  public String globalInfo()
  {
    return "Ranker : \n\nRanks attributes by their individual evaluations. Use in conjunction with attribute evaluators (ReliefF, GainRatio, Entropy etc).\n";
  }
  




  public Ranker()
  {
    resetOptions();
  }
  




  public String numToSelectTipText()
  {
    return "Specify the number of attributes to retain. The default value (-1) indicates that all attributes are to be retained. Use either this option or a threshold to reduce the attribute set.";
  }
  






  public void setNumToSelect(int n)
  {
    m_numToSelect = n;
  }
  



  public int getNumToSelect()
  {
    return m_numToSelect;
  }
  





  public int getCalculatedNumToSelect()
  {
    if (m_numToSelect >= 0) {
      m_calculatedNumToSelect = m_numToSelect;
    }
    return m_calculatedNumToSelect;
  }
  




  public String thresholdTipText()
  {
    return "Set threshold by which attributes can be discarded. Default value results in no attributes being discarded. Use either this option or numToSelect to reduce the attribute set.";
  }
  






  public void setThreshold(double threshold)
  {
    m_threshold = threshold;
  }
  



  public double getThreshold()
  {
    return m_threshold;
  }
  




  public String generateRankingTipText()
  {
    return "A constant option. Ranker is only capable of generating  attribute rankings.";
  }
  






  public void setGenerateRanking(boolean doRank) {}
  






  public boolean getGenerateRanking()
  {
    return true;
  }
  




  public String startSetTipText()
  {
    return "Specify a set of attributes to ignore.  When generating the ranking, Ranker will not evaluate the attributes  in this list. This is specified as a comma seperated list off attribute indexes starting at 1. It can include ranges. Eg. 1,2,5-9,17.";
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
  



  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tSpecify a starting set of attributes.\n\tEg. 1,3,5-7.\n\tAny starting attributes specified are\n\tignored during the ranking.", "P", 1, "-P <start set>"));
    





    newVector.addElement(new Option("\tSpecify a theshold by which attributes\n\tmay be discarded from the ranking.", "T", 1, "-T <threshold>"));
    



    newVector.addElement(new Option("\tSpecify number of attributes to select", "N", 1, "-N <num to select>"));
    



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
    
    optionString = Utils.getOption('T', options);
    if (optionString.length() != 0)
    {
      Double temp = Double.valueOf(optionString);
      setThreshold(temp.doubleValue());
    }
    
    optionString = Utils.getOption('N', options);
    if (optionString.length() != 0) {
      setNumToSelect(Integer.parseInt(optionString));
    }
  }
  




  public String[] getOptions()
  {
    String[] options = new String[6];
    int current = 0;
    
    if (!getStartSet().equals("")) {
      options[(current++)] = "-P";
      options[(current++)] = ("" + startSetToString());
    }
    
    options[(current++)] = "-T";
    options[(current++)] = ("" + getThreshold());
    
    options[(current++)] = "-N";
    options[(current++)] = ("" + getNumToSelect());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
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
  











  public int[] search(ASEvaluation ASEval, Instances data)
    throws Exception
  {
    if (!(ASEval instanceof AttributeEvaluator)) {
      throw new Exception(ASEval.getClass().getName() + " is not a" + "Attribute evaluator!");
    }
    


    m_numAttribs = data.numAttributes();
    
    if ((ASEval instanceof UnsupervisedAttributeEvaluator)) {
      m_hasClass = false;
    }
    else {
      m_classIndex = data.classIndex();
      if (m_classIndex >= 0) {
        m_hasClass = true;
      } else {
        m_hasClass = false;
      }
    }
    


    if ((ASEval instanceof AttributeTransformer)) {
      data = ((AttributeTransformer)ASEval).transformedHeader();
      if ((m_classIndex >= 0) && (data.classIndex() >= 0)) {
        m_classIndex = data.classIndex();
        m_hasClass = true;
      }
    }
    

    m_startRange.setUpper(m_numAttribs - 1);
    if (!getStartSet().equals("")) {
      m_starting = m_startRange.getSelection();
    }
    
    int sl = 0;
    if (m_starting != null) {
      sl = m_starting.length;
    }
    if ((m_starting != null) && (m_hasClass == true))
    {
      boolean ok = false;
      for (int i = 0; i < sl; i++) {
        if (m_starting[i] == m_classIndex) {
          ok = true;
          break;
        }
      }
      
      if (!ok) {
        sl++;
      }
      
    }
    else if (m_hasClass == true) {
      sl++;
    }
    


    m_attributeList = new int[m_numAttribs - sl];
    m_attributeMerit = new double[m_numAttribs - sl];
    

    int i = 0; for (int j = 0; i < m_numAttribs; i++) {
      if (!inStarting(i)) {
        m_attributeList[(j++)] = i;
      }
    }
    
    AttributeEvaluator ASEvaluator = (AttributeEvaluator)ASEval;
    
    for (i = 0; i < m_attributeList.length; i++) {
      m_attributeMerit[i] = ASEvaluator.evaluateAttribute(m_attributeList[i]);
    }
    
    double[][] tempRanked = rankedAttributes();
    int[] rankedAttributes = new int[m_attributeList.length];
    
    for (i = 0; i < m_attributeList.length; i++) {
      rankedAttributes[i] = ((int)tempRanked[i][0]);
    }
    
    return rankedAttributes;
  }
  








  public double[][] rankedAttributes()
    throws Exception
  {
    if ((m_attributeList == null) || (m_attributeMerit == null)) {
      throw new Exception("Search must be performed before a ranked attribute list can be obtained");
    }
    

    int[] ranked = Utils.sort(m_attributeMerit);
    
    double[][] bestToWorst = new double[ranked.length][2];
    
    int i = ranked.length - 1; for (int j = 0; i >= 0; i--) {
      bestToWorst[(j++)][0] = ranked[i];
    }
    

    for (i = 0; i < bestToWorst.length; i++) {
      int temp = (int)bestToWorst[i][0];
      bestToWorst[i][0] = m_attributeList[temp];
      bestToWorst[i][1] = m_attributeMerit[temp];
    }
    
    if (m_numToSelect > bestToWorst.length) {
      throw new Exception("More attributes requested than exist in the data");
    }
    
    if (m_numToSelect <= 0) {
      if (m_threshold == -1.7976931348623157E308D) {
        m_calculatedNumToSelect = bestToWorst.length;
      } else {
        determineNumToSelectFromThreshold(bestToWorst);
      }
    }
    



    return bestToWorst;
  }
  
  private void determineNumToSelectFromThreshold(double[][] ranking) {
    int count = 0;
    for (int i = 0; i < ranking.length; i++) {
      if (ranking[i][1] > m_threshold) {
        count++;
      }
    }
    m_calculatedNumToSelect = count;
  }
  
  private void determineThreshFromNumToSelect(double[][] ranking) throws Exception
  {
    if (m_numToSelect > ranking.length) {
      throw new Exception("More attributes requested than exist in the data");
    }
    
    if (m_numToSelect == ranking.length) {
      return;
    }
    
    m_threshold = ((ranking[(m_numToSelect - 1)][1] + ranking[m_numToSelect][1]) / 2.0D);
  }
  




  public String toString()
  {
    StringBuffer BfString = new StringBuffer();
    BfString.append("\tAttribute ranking.\n");
    
    if (m_starting != null) {
      BfString.append("\tIgnored attributes: ");
      
      BfString.append(startSetToString());
      BfString.append("\n");
    }
    
    if (m_threshold != -1.7976931348623157E308D) {
      BfString.append("\tThreshold for discarding attributes: " + Utils.doubleToString(m_threshold, 8, 4) + "\n");
    }
    

    return BfString.toString();
  }
  



  protected void resetOptions()
  {
    m_starting = null;
    m_startRange = new Range();
    m_attributeList = null;
    m_attributeMerit = null;
    m_threshold = -1.7976931348623157E308D;
  }
  

  private boolean inStarting(int feat)
  {
    if ((m_hasClass == true) && (feat == m_classIndex)) {
      return true;
    }
    
    if (m_starting == null) {
      return false;
    }
    
    for (int i = 0; i < m_starting.length; i++) {
      if (m_starting[i] == feat) {
        return true;
      }
    }
    
    return false;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.26 $");
  }
}

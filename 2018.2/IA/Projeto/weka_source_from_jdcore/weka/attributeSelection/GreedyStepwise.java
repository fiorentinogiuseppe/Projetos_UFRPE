package weka.attributeSelection;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;








































































































public class GreedyStepwise
  extends ASSearch
  implements RankedOutputSearch, StartSetHandler, OptionHandler
{
  static final long serialVersionUID = -6312951970168325471L;
  protected boolean m_hasClass;
  protected int m_classIndex;
  protected int m_numAttribs;
  protected boolean m_rankingRequested;
  protected boolean m_doRank;
  protected boolean m_doneRanking;
  protected double m_threshold;
  protected int m_numToSelect = -1;
  

  protected int m_calculatedNumToSelect;
  

  protected double m_bestMerit;
  

  protected double[][] m_rankedAtts;
  

  protected int m_rankedSoFar;
  
  protected BitSet m_best_group;
  
  protected ASEvaluation m_ASEval;
  
  protected Instances m_Instances;
  
  protected Range m_startRange;
  
  protected int[] m_starting;
  
  protected boolean m_backward = false;
  




  protected boolean m_conservativeSelection = false;
  


  public GreedyStepwise()
  {
    m_threshold = -1.7976931348623157E308D;
    m_doneRanking = false;
    m_startRange = new Range();
    m_starting = null;
    resetOptions();
  }
  





  public String globalInfo()
  {
    return "GreedyStepwise :\n\nPerforms a greedy forward or backward search through the space of attribute subsets. May start with no/all attributes or from an arbitrary point in the space. Stops when the addition/deletion of any remaining attributes results in a decrease in evaluation. Can also produce a ranked list of attributes by traversing the space from one side to the other and recording the order that attributes are selected.\n";
  }
  












  public String searchBackwardsTipText()
  {
    return "Search backwards rather than forwards.";
  }
  




  public void setSearchBackwards(boolean back)
  {
    m_backward = back;
    if (m_backward) {
      setGenerateRanking(false);
    }
  }
  




  public boolean getSearchBackwards()
  {
    return m_backward;
  }
  





  public String thresholdTipText()
  {
    return "Set threshold by which attributes can be discarded. Default value results in no attributes being discarded. Use in conjunction with generateRanking";
  }
  








  public void setThreshold(double threshold)
  {
    m_threshold = threshold;
  }
  




  public double getThreshold()
  {
    return m_threshold;
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
  





  public String generateRankingTipText()
  {
    return "Set to true if a ranked list is required.";
  }
  





  public void setGenerateRanking(boolean doRank)
  {
    m_rankingRequested = doRank;
  }
  







  public boolean getGenerateRanking()
  {
    return m_rankingRequested;
  }
  





  public String startSetTipText()
  {
    return "Set the start point for the search. This is specified as a comma seperated list off attribute indexes starting at 1. It can include ranges. Eg. 1,2,5-9,17.";
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
  





  public String conservativeForwardSelectionTipText()
  {
    return "If true (and forward search is selected) then attributes will continue to be added to the best subset as long as merit does not degrade.";
  }
  







  public void setConservativeForwardSelection(boolean c)
  {
    m_conservativeSelection = c;
  }
  




  public boolean getConservativeForwardSelection()
  {
    return m_conservativeSelection;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(5);
    
    newVector.addElement(new Option("\tUse conservative forward search", "-C", 0, "-C"));
    

    newVector.addElement(new Option("\tUse a backward search instead of a\n\tforward one.", "-B", 0, "-B"));
    

    newVector.addElement(new Option("\tSpecify a starting set of attributes.\n\tEg. 1,3,5-7.", "P", 1, "-P <start set>"));
    




    newVector.addElement(new Option("\tProduce a ranked list of attributes.", "R", 0, "-R"));
    
    newVector.addElement(new Option("\tSpecify a theshold by which attributes\n\tmay be discarded from the ranking.\n\tUse in conjuction with -R", "T", 1, "-T <threshold>"));
    




    newVector.addElement(new Option("\tSpecify number of attributes to select", "N", 1, "-N <num to select>"));
    



    return newVector.elements();
  }
  
















































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    setSearchBackwards(Utils.getFlag('B', options));
    
    setConservativeForwardSelection(Utils.getFlag('C', options));
    
    String optionString = Utils.getOption('P', options);
    if (optionString.length() != 0) {
      setStartSet(optionString);
    }
    
    setGenerateRanking(Utils.getFlag('R', options));
    
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
    String[] options = new String[9];
    int current = 0;
    
    if (getSearchBackwards()) {
      options[(current++)] = "-B";
    }
    
    if (getConservativeForwardSelection()) {
      options[(current++)] = "-C";
    }
    
    if (!getStartSet().equals("")) {
      options[(current++)] = "-P";
      options[(current++)] = ("" + startSetToString());
    }
    
    if (getGenerateRanking()) {
      options[(current++)] = "-R";
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
  








  protected String startSetToString()
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
    StringBuffer FString = new StringBuffer();
    FString.append("\tGreedy Stepwise (" + (m_backward ? "backwards)" : "forwards)") + ".\n\tStart set: ");
    



    if (m_starting == null) {
      if (m_backward) {
        FString.append("all attributes\n");
      } else {
        FString.append("no attributes\n");
      }
    }
    else {
      FString.append(startSetToString() + "\n");
    }
    if (!m_doneRanking) {
      FString.append("\tMerit of best subset found: " + Utils.doubleToString(Math.abs(m_bestMerit), 8, 3) + "\n");

    }
    else if (m_backward) {
      FString.append("\n\tRanking is the order that attributes were removed, starting \n\twith all attributes. The merit scores in the left\n\tcolumn are the goodness of the remaining attributes in the\n\tsubset after removing the corresponding in the right column\n\tattribute from the subset.\n");


    }
    else
    {

      FString.append("\n\tRanking is the order that attributes were added, starting \n\twith no attributes. The merit scores in the left column\n\tare the goodness of the subset after the adding the\n\tcorresponding attribute in the right column to the subset.\n");
    }
    






    if ((m_threshold != -1.7976931348623157E308D) && (m_doneRanking)) {
      FString.append("\tThreshold for discarding attributes: " + Utils.doubleToString(m_threshold, 8, 4) + "\n");
    }
    

    return FString.toString();
  }
  










  public int[] search(ASEvaluation ASEval, Instances data)
    throws Exception
  {
    double best_merit = -1.7976931348623157E308D;
    
    int temp_index = 0;
    

    if (data != null) {
      resetOptions();
      m_Instances = new Instances(data, 0);
    }
    m_ASEval = ASEval;
    
    m_numAttribs = m_Instances.numAttributes();
    
    if (m_best_group == null) {
      m_best_group = new BitSet(m_numAttribs);
    }
    
    if (!(m_ASEval instanceof SubsetEvaluator)) {
      throw new Exception(m_ASEval.getClass().getName() + " is not a " + "Subset evaluator!");
    }
    


    m_startRange.setUpper(m_numAttribs - 1);
    if (!getStartSet().equals("")) {
      m_starting = m_startRange.getSelection();
    }
    
    if ((m_ASEval instanceof UnsupervisedSubsetEvaluator)) {
      m_hasClass = false;
      m_classIndex = -1;
    }
    else {
      m_hasClass = true;
      m_classIndex = m_Instances.classIndex();
    }
    
    SubsetEvaluator ASEvaluator = (SubsetEvaluator)m_ASEval;
    
    if (m_rankedAtts == null) {
      m_rankedAtts = new double[m_numAttribs][2];
      m_rankedSoFar = 0;
    }
    
    int i;
    if ((m_starting != null) && (m_rankedSoFar <= 0)) {
      for (i = 0; i < m_starting.length;) {
        if (m_starting[i] != m_classIndex) {
          m_best_group.set(m_starting[i]);
        }
        i++; continue;
        




        if ((m_backward) && (m_rankedSoFar <= 0)) {
          for (int i = 0; i < m_numAttribs; i++) {
            if (i != m_classIndex) {
              m_best_group.set(i);
            }
          }
        }
      }
    }
    
    best_merit = ASEvaluator.evaluateSubset(m_best_group);
    

    boolean done = false;
    boolean addone = false;
    
    while (!done) {
      BitSet temp_group = (BitSet)m_best_group.clone();
      double temp_best = best_merit;
      if (m_doRank) {
        temp_best = -1.7976931348623157E308D;
      }
      done = true;
      addone = false;
      for (int i = 0; i < m_numAttribs; i++) { boolean z;
        boolean z; if (m_backward) {
          z = (i != m_classIndex) && (temp_group.get(i));
        } else {
          z = (i != m_classIndex) && (!temp_group.get(i));
        }
        if (z)
        {
          if (m_backward) {
            temp_group.clear(i);
          } else {
            temp_group.set(i);
          }
          double temp_merit = ASEvaluator.evaluateSubset(temp_group);
          if (m_backward) {
            z = temp_merit >= temp_best;
          }
          else if (m_conservativeSelection) {
            z = temp_merit >= temp_best;
          } else {
            z = temp_merit > temp_best;
          }
          

          if (z) {
            temp_best = temp_merit;
            temp_index = i;
            addone = true;
            done = false;
          }
          

          if (m_backward) {
            temp_group.set(i);
          } else {
            temp_group.clear(i);
          }
          if (m_doRank) {
            done = false;
          }
        }
      }
      if (addone) {
        if (m_backward) {
          m_best_group.clear(temp_index);
        } else {
          m_best_group.set(temp_index);
        }
        best_merit = temp_best;
        m_rankedAtts[m_rankedSoFar][0] = temp_index;
        m_rankedAtts[m_rankedSoFar][1] = best_merit;
        m_rankedSoFar += 1;
      }
    }
    m_bestMerit = best_merit;
    return attributeList(m_best_group);
  }
  














  public double[][] rankedAttributes()
    throws Exception
  {
    if ((m_rankedAtts == null) || (m_rankedSoFar == -1)) {
      throw new Exception("Search must be performed before attributes can be ranked.");
    }
    

    m_doRank = true;
    search(m_ASEval, null);
    
    double[][] final_rank = new double[m_rankedSoFar][2];
    for (int i = 0; i < m_rankedSoFar; i++) {
      final_rank[i][0] = m_rankedAtts[i][0];
      final_rank[i][1] = m_rankedAtts[i][1];
    }
    
    resetOptions();
    m_doneRanking = true;
    
    if (m_numToSelect > final_rank.length) {
      throw new Exception("More attributes requested than exist in the data");
    }
    
    if (m_numToSelect <= 0) {
      if (m_threshold == -1.7976931348623157E308D) {
        m_calculatedNumToSelect = final_rank.length;
      } else {
        determineNumToSelectFromThreshold(final_rank);
      }
    }
    
    return final_rank;
  }
  
  private void determineNumToSelectFromThreshold(double[][] ranking) {
    int count = 0;
    for (double[] element : ranking) {
      if (element[1] > m_threshold) {
        count++;
      }
    }
    m_calculatedNumToSelect = count;
  }
  





  protected int[] attributeList(BitSet group)
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
  


  protected void resetOptions()
  {
    m_doRank = false;
    m_best_group = null;
    m_ASEval = null;
    m_Instances = null;
    m_rankedSoFar = -1;
    m_rankedAtts = ((double[][])null);
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11229 $");
  }
}

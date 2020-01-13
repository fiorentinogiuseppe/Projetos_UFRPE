package weka.attributeSelection;

import java.io.PrintStream;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;











































































public class LinearForwardSelection
  extends ASSearch
  implements OptionHandler, StartSetHandler, TechnicalInformationHandler
{
  protected static final int SEARCH_METHOD_FORWARD = 0;
  protected static final int SEARCH_METHOD_FLOATING = 1;
  public static final Tag[] TAGS_SEARCH_METHOD = { new Tag(0, "Forward selection"), new Tag(1, "Floating forward selection") };
  

  protected static final int TYPE_FIXED_SET = 0;
  

  protected static final int TYPE_FIXED_WIDTH = 1;
  
  public static final Tag[] TAGS_TYPE = { new Tag(0, "Fixed-set"), new Tag(1, "Fixed-width") };
  


  protected int m_maxStale;
  


  protected int m_forwardSearchMethod;
  


  protected boolean m_performRanking;
  


  protected int m_numUsedAttributes;
  


  protected int m_linearSelectionType;
  


  protected int[] m_starting;
  


  protected Range m_startRange;
  

  protected boolean m_hasClass;
  

  protected int m_classIndex;
  

  protected int m_numAttribs;
  

  protected int m_totalEvals;
  

  protected boolean m_verbose;
  

  protected double m_bestMerit;
  

  protected int m_cacheSize;
  


  public LinearForwardSelection()
  {
    resetOptions();
  }
  





  public String globalInfo()
  {
    return "LinearForwardSelection:\n\nExtension of BestFirst. Takes a restricted number of k attributes into account. Fixed-set selects a fixed number k of attributes, whereas k is increased in each step when fixed-width is selected. The search uses either the initial ordering to select the top k attributes, or performs a ranking (with the same evalutator the search uses later on). The search direction can be forward, or floating forward selection (with opitional backward search steps).\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  


















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Martin Guetlein and Eibe Frank and Mark Hall and Andreas Karwath");
    result.setValue(TechnicalInformation.Field.YEAR, "2009");
    result.setValue(TechnicalInformation.Field.TITLE, "Large Scale Attribute Selection Using Wrappers");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Proc IEEE Symposium on Computational Intelligence and Data Mining");
    result.setValue(TechnicalInformation.Field.PAGES, "332-339");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "IEEE");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.MASTERSTHESIS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Martin Guetlein");
    additional.setValue(TechnicalInformation.Field.YEAR, "2006");
    additional.setValue(TechnicalInformation.Field.TITLE, "Large Scale Attribute Selection Using Wrappers");
    additional.setValue(TechnicalInformation.Field.SCHOOL, "Albert-Ludwigs-Universitaet");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "Freiburg, Germany");
    
    return result;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(8);
    
    newVector.addElement(new Option("\tSpecify a starting set of attributes.\n\tEg. 1,3,5-7.", "P", 1, "-P <start set>"));
    
    newVector.addElement(new Option("\tForward selection method. (default = 0).", "D", 1, "-D <0 = forward selection | 1 = floating forward selection>"));
    

    newVector.addElement(new Option("\tNumber of non-improving nodes to\n\tconsider before terminating search.", "N", 1, "-N <num>"));
    
    newVector.addElement(new Option("\tPerform initial ranking to select the\n\ttop-ranked attributes.", "I", 0, "-I"));
    
    newVector.addElement(new Option("\tNumber of top-ranked attributes that are \n\ttaken into account by the search.", "K", 1, "-K <num>"));
    

    newVector.addElement(new Option("\tType of Linear Forward Selection (default = 0).", "T", 1, "-T <0 = fixed-set | 1 = fixed-width>"));
    

    newVector.addElement(new Option("\tSize of lookup cache for evaluated subsets.\n\tExpressed as a multiple of the number of\n\tattributes in the data set. (default = 1)", "S", 1, "-S <num>"));
    


    newVector.addElement(new Option("\tverbose on/off", "Z", 0, "-Z"));
    
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
    
    optionString = Utils.getOption('D', options);
    
    if (optionString.length() != 0) {
      setForwardSelectionMethod(new SelectedTag(Integer.parseInt(optionString), TAGS_SEARCH_METHOD));
    }
    else {
      setForwardSelectionMethod(new SelectedTag(0, TAGS_SEARCH_METHOD));
    }
    

    optionString = Utils.getOption('N', options);
    
    if (optionString.length() != 0) {
      setSearchTermination(Integer.parseInt(optionString));
    }
    
    setPerformRanking(Utils.getFlag('I', options));
    
    optionString = Utils.getOption('K', options);
    
    if (optionString.length() != 0) {
      setNumUsedAttributes(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('T', options);
    
    if (optionString.length() != 0) {
      setType(new SelectedTag(Integer.parseInt(optionString), TAGS_TYPE));
    } else {
      setType(new SelectedTag(0, TAGS_TYPE));
    }
    
    optionString = Utils.getOption('S', options);
    
    if (optionString.length() != 0) {
      setLookupCacheSize(Integer.parseInt(optionString));
    }
    
    m_verbose = Utils.getFlag('Z', options);
  }
  







  public void setLookupCacheSize(int size)
  {
    if (size >= 0) {
      m_cacheSize = size;
    }
  }
  





  public int getLookupCacheSize()
  {
    return m_cacheSize;
  }
  





  public String lookupCacheSizeTipText()
  {
    return "Set the maximum size of the lookup cache of evaluated subsets. This is expressed as a multiplier of the number of attributes in the data set. (default = 1).";
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
  





  public String searchTerminationTipText()
  {
    return "Set the amount of backtracking. Specify the number of ";
  }
  







  public void setSearchTermination(int t)
    throws Exception
  {
    if (t < 1) {
      throw new Exception("Value of -N must be > 0.");
    }
    
    m_maxStale = t;
  }
  




  public int getSearchTermination()
  {
    return m_maxStale;
  }
  





  public String performRankingTipText()
  {
    return "Perform initial ranking to select top-ranked attributes.";
  }
  





  public void setPerformRanking(boolean b)
  {
    m_performRanking = b;
  }
  





  public boolean getPerformRanking()
  {
    return m_performRanking;
  }
  





  public String numUsedAttributesTipText()
  {
    return "Set the amount of top-ranked attributes that are taken into account by the search process.";
  }
  







  public void setNumUsedAttributes(int k)
    throws Exception
  {
    if (k < 2) {
      throw new Exception("Value of -K must be >= 2.");
    }
    
    m_numUsedAttributes = k;
  }
  





  public int getNumUsedAttributes()
  {
    return m_numUsedAttributes;
  }
  





  public String forwardSelectionMethodTipText()
  {
    return "Set the direction of the search.";
  }
  





  public void setForwardSelectionMethod(SelectedTag d)
  {
    if (d.getTags() == TAGS_SEARCH_METHOD) {
      m_forwardSearchMethod = d.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getForwardSelectionMethod()
  {
    return new SelectedTag(m_forwardSearchMethod, TAGS_SEARCH_METHOD);
  }
  





  public String typeTipText()
  {
    return "Set the type of the search.";
  }
  





  public void setType(SelectedTag t)
  {
    if (t.getTags() == TAGS_TYPE) {
      m_linearSelectionType = t.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getType()
  {
    return new SelectedTag(m_linearSelectionType, TAGS_TYPE);
  }
  





  public String verboseTipText()
  {
    return "Turn on verbose output for monitoring the search's progress.";
  }
  





  public void setVerbose(boolean b)
  {
    m_verbose = b;
  }
  




  public boolean getVerbose()
  {
    return m_verbose;
  }
  




  public String[] getOptions()
  {
    String[] options = new String[13];
    int current = 0;
    
    if (!getStartSet().equals("")) {
      options[(current++)] = "-P";
      options[(current++)] = ("" + startSetToString());
    }
    
    options[(current++)] = "-D";
    options[(current++)] = ("" + m_forwardSearchMethod);
    options[(current++)] = "-N";
    options[(current++)] = ("" + m_maxStale);
    
    if (m_performRanking) {
      options[(current++)] = "-I";
    }
    
    options[(current++)] = "-K";
    options[(current++)] = ("" + m_numUsedAttributes);
    options[(current++)] = "-T";
    options[(current++)] = ("" + m_linearSelectionType);
    
    if (m_verbose) {
      options[(current++)] = "-Z";
    }
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
  




  public String toString()
  {
    StringBuffer LFSString = new StringBuffer();
    
    LFSString.append("\tLinear Forward Selection.\n\tStart set: ");
    
    if (m_starting == null) {
      LFSString.append("no attributes\n");
    } else {
      LFSString.append(startSetToString() + "\n");
    }
    
    LFSString.append("\tForward selection method: ");
    
    if (m_forwardSearchMethod == 0) {
      LFSString.append("forward selection\n");
    } else {
      LFSString.append("floating forward selection\n");
    }
    
    LFSString.append("\tStale search after " + m_maxStale + " node expansions\n");
    

    LFSString.append("\tLinear Forward Selection Type: ");
    
    if (m_linearSelectionType == 0) {
      LFSString.append("fixed-set\n");
    } else {
      LFSString.append("fixed-width\n");
    }
    
    LFSString.append("\tNumber of top-ranked attributes that are used: " + m_numUsedAttributes + "\n");
    

    LFSString.append("\tTotal number of subsets evaluated: " + m_totalEvals + "\n");
    
    LFSString.append("\tMerit of best subset found: " + Utils.doubleToString(Math.abs(m_bestMerit), 8, 3) + "\n");
    

    return LFSString.toString();
  }
  










  public int[] search(ASEvaluation ASEval, Instances data)
    throws Exception
  {
    m_totalEvals = 0;
    
    if (!(ASEval instanceof SubsetEvaluator)) {
      throw new Exception(ASEval.getClass().getName() + " is not a " + "Subset evaluator!");
    }
    

    if ((ASEval instanceof UnsupervisedSubsetEvaluator)) {
      m_hasClass = false;
    } else {
      m_hasClass = true;
      m_classIndex = data.classIndex();
    }
    
    ASEval.buildEvaluator(data);
    
    m_numAttribs = data.numAttributes();
    
    if (m_numUsedAttributes > m_numAttribs) {
      System.out.println("Decreasing number of top-ranked attributes to total number of attributes: " + data.numAttributes());
      

      m_numUsedAttributes = m_numAttribs;
    }
    
    BitSet start_group = new BitSet(m_numAttribs);
    m_startRange.setUpper(m_numAttribs - 1);
    
    if (!getStartSet().equals("")) {
      m_starting = m_startRange.getSelection();
    }
    

    if (m_starting != null) {
      for (int i = 0; i < m_starting.length; i++) {
        if (m_starting[i] != m_classIndex) {
          start_group.set(m_starting[i]);
        }
      }
    }
    
    LFSMethods LFS = new LFSMethods();
    
    int[] ranking;
    int[] ranking;
    if (m_performRanking) {
      ranking = LFS.rankAttributes(data, (SubsetEvaluator)ASEval, m_verbose);
    } else {
      ranking = new int[m_numAttribs];
      
      for (int i = 0; i < ranking.length; i++) {
        ranking[i] = i;
      }
    }
    
    if (m_forwardSearchMethod == 0) {
      LFS.forwardSearch(m_cacheSize, start_group, ranking, m_numUsedAttributes, m_linearSelectionType == 1, m_maxStale, -1, data, (SubsetEvaluator)ASEval, m_verbose);

    }
    else if (m_forwardSearchMethod == 1) {
      LFS.floatingForwardSearch(m_cacheSize, start_group, ranking, m_numUsedAttributes, m_linearSelectionType == 1, m_maxStale, data, (SubsetEvaluator)ASEval, m_verbose);
    }
    


    m_totalEvals = LFS.getNumEvalsTotal();
    m_bestMerit = LFS.getBestMerit();
    
    return attributeList(LFS.getBestGroup());
  }
  


  protected void resetOptions()
  {
    m_maxStale = 5;
    m_forwardSearchMethod = 0;
    m_performRanking = true;
    m_numUsedAttributes = 50;
    m_linearSelectionType = 0;
    m_starting = null;
    m_startRange = new Range();
    m_classIndex = -1;
    m_totalEvals = 0;
    m_cacheSize = 1;
    m_verbose = false;
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
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6161 $");
  }
}

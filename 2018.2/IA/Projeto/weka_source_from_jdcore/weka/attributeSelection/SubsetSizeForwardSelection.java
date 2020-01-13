package weka.attributeSelection;

import java.io.PrintStream;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.Utils;





























































































































public class SubsetSizeForwardSelection
  extends ASSearch
  implements OptionHandler
{
  protected static final int TYPE_FIXED_SET = 0;
  protected static final int TYPE_FIXED_WIDTH = 1;
  public static final Tag[] TAGS_TYPE = { new Tag(0, "Fixed-set"), new Tag(1, "Fixed-width") };
  


  protected boolean m_performRanking;
  


  protected int m_numUsedAttributes;
  


  protected int m_linearSelectionType;
  


  private ASEvaluation m_setSizeEval;
  


  protected int m_numFolds;
  


  protected int m_seed;
  


  protected int m_numAttribs;
  


  protected int m_totalEvals;
  


  protected boolean m_verbose;
  

  protected double m_bestMerit;
  

  protected int m_cacheSize;
  


  public SubsetSizeForwardSelection()
  {
    resetOptions();
  }
  





  public String globalInfo()
  {
    return "SubsetSizeForwardSelection:\n\nExtension of LinearForwardSelection. The search performs an interior cross-validation (seed and number of folds can be specified). A LinearForwardSelection is performed on each foldto determine the optimal subset-size (using the given SubsetSizeEvaluator). Finally, a LinearForwardSelection up to the optimal subset-size is performed on the whole data.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  





















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Martin Guetlein and Eibe Frank and Mark Hall");
    
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
    Vector newVector = new Vector(9);
    
    newVector.addElement(new Option("\tPerform initial ranking to select the\n\ttop-ranked attributes.", "I", 0, "-I"));
    
    newVector.addElement(new Option("\tNumber of top-ranked attributes that are \n\ttaken into account by the search.", "K", 1, "-K <num>"));
    

    newVector.addElement(new Option("\tType of Linear Forward Selection (default = 0).", "T", 1, "-T <0 = fixed-set | 1 = fixed-width>"));
    

    newVector.addElement(new Option("\tSize of lookup cache for evaluated subsets.\n\tExpressed as a multiple of the number of\n\tattributes in the data set. (default = 1)", "S", 1, "-S <num>"));
    


    newVector.addElement(new Option("\tSubset-evaluator used for subset-size determination.-- -M", "E", 1, "-E <subset evaluator>"));
    

    newVector.addElement(new Option("\tNumber of cross validation folds\n\tfor subset size determination (default = 5).", "F", 1, "-F <num>"));
    
    newVector.addElement(new Option("\tSeed for cross validation\n\tsubset size determination. (default = 1)", "R", 1, "-R <num>"));
    
    newVector.addElement(new Option("\tverbose on/off", "Z", 0, "-Z"));
    
    if ((m_setSizeEval != null) && ((m_setSizeEval instanceof OptionHandler))) {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to evaluator " + m_setSizeEval.getClass().getName() + ":"));
      


      Enumeration enu = ((OptionHandler)m_setSizeEval).listOptions();
      
      while (enu.hasMoreElements()) {
        newVector.addElement(enu.nextElement());
      }
    }
    
    return newVector.elements();
  }
  

















































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    setPerformRanking(Utils.getFlag('I', options));
    
    String optionString = Utils.getOption('K', options);
    
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
    
    optionString = Utils.getOption('E', options);
    
    if (optionString.length() == 0) {
      System.out.println("No subset size evaluator given, using evaluator that is used for final search.");
      

      m_setSizeEval = null;
    } else {
      setSubsetSizeEvaluator(ASEvaluation.forName(optionString, Utils.partitionOptions(options)));
    }
    

    optionString = Utils.getOption('F', options);
    
    if (optionString.length() != 0) {
      setNumSubsetSizeCVFolds(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('R', options);
    
    if (optionString.length() != 0) {
      setSeed(Integer.parseInt(optionString));
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
  





  public String subsetSizeEvaluatorTipText()
  {
    return "Subset evaluator to use for subset size determination.";
  }
  




  public void setSubsetSizeEvaluator(ASEvaluation eval)
    throws Exception
  {
    if (!(eval instanceof SubsetEvaluator)) {
      throw new Exception(eval.getClass().getName() + " is no subset evaluator.");
    }
    

    m_setSizeEval = eval;
  }
  




  public ASEvaluation getSubsetSizeEvaluator()
  {
    return m_setSizeEval;
  }
  





  public String numSubsetSizeCVFoldsTipText()
  {
    return "Number of cross validation folds for subset size determination";
  }
  





  public void setNumSubsetSizeCVFolds(int f)
  {
    m_numFolds = f;
  }
  





  public int getNumSubsetSizeCVFolds()
  {
    return m_numFolds;
  }
  





  public String seedTipText()
  {
    return "Seed for cross validation subset size determination. (default = 1)";
  }
  




  public void setSeed(int s)
  {
    m_seed = s;
  }
  




  public int getSeed()
  {
    return m_seed;
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
    String[] evaluatorOptions = new String[0];
    
    if ((m_setSizeEval != null) && ((m_setSizeEval instanceof OptionHandler))) {
      evaluatorOptions = ((OptionHandler)m_setSizeEval).getOptions();
    }
    
    String[] options = new String[15 + evaluatorOptions.length];
    int current = 0;
    
    if (m_performRanking) {
      options[(current++)] = "-I";
    }
    
    options[(current++)] = "-K";
    options[(current++)] = ("" + m_numUsedAttributes);
    options[(current++)] = "-T";
    options[(current++)] = ("" + m_linearSelectionType);
    
    options[(current++)] = "-F";
    options[(current++)] = ("" + m_numFolds);
    options[(current++)] = "-S";
    options[(current++)] = ("" + m_seed);
    
    if (getVerbose()) {
      options[(current++)] = "-Z";
    }
    
    if (m_setSizeEval != null) {
      options[(current++)] = "-E";
      options[(current++)] = m_setSizeEval.getClass().getName();
    }
    
    options[(current++)] = "--";
    System.arraycopy(evaluatorOptions, 0, options, current, evaluatorOptions.length);
    
    current += evaluatorOptions.length;
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  





  public String toString()
  {
    StringBuffer LFSString = new StringBuffer();
    
    LFSString.append("\tSubset Size Forward Selection.\n");
    
    LFSString.append("\tLinear Forward Selection Type: ");
    
    if (m_linearSelectionType == 0) {
      LFSString.append("fixed-set\n");
    } else {
      LFSString.append("fixed-width\n");
    }
    
    LFSString.append("\tNumber of top-ranked attributes that are used: " + m_numUsedAttributes + "\n");
    

    LFSString.append("\tNumber of cross validation folds for subset size determination: " + m_numFolds + "\n");
    

    LFSString.append("\tSeed for cross validation subset size determination: " + m_seed + "\n");
    

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
    

    if (m_setSizeEval == null) {
      m_setSizeEval = ASEval;
    }
    
    m_numAttribs = data.numAttributes();
    
    if (m_numUsedAttributes > m_numAttribs) {
      System.out.println("Decreasing number of top-ranked attributes to total number of attributes: " + data.numAttributes());
      



      m_numUsedAttributes = m_numAttribs;
    }
    
    Instances[] trainData = new Instances[m_numFolds];
    Instances[] testData = new Instances[m_numFolds];
    LFSMethods[] searchResults = new LFSMethods[m_numFolds];
    
    Random random = new Random(m_seed);
    Instances dataCopy = new Instances(data);
    dataCopy.randomize(random);
    
    if (dataCopy.classAttribute().isNominal()) {
      dataCopy.stratify(m_numFolds);
    }
    
    for (int f = 0; f < m_numFolds; f++) {
      trainData[f] = dataCopy.trainCV(m_numFolds, f, random);
      testData[f] = dataCopy.testCV(m_numFolds, f);
    }
    
    LFSMethods LSF = new LFSMethods();
    
    int[] ranking;
    int[] ranking;
    if (m_performRanking) {
      ASEval.buildEvaluator(data);
      ranking = LSF.rankAttributes(data, (SubsetEvaluator)ASEval, m_verbose);
    } else {
      ranking = new int[m_numAttribs];
      
      for (int i = 0; i < ranking.length; i++) {
        ranking[i] = i;
      }
    }
    
    int maxSubsetSize = 0;
    
    for (int f = 0; f < m_numFolds; f++) {
      if (m_verbose) {
        System.out.println("perform search on internal fold: " + (f + 1) + "/" + m_numFolds);
      }
      

      m_setSizeEval.buildEvaluator(trainData[f]);
      searchResults[f] = new LFSMethods();
      searchResults[f].forwardSearch(m_cacheSize, new BitSet(m_numAttribs), ranking, m_numUsedAttributes, m_linearSelectionType == 1, 1, -1, trainData[f], (SubsetEvaluator)m_setSizeEval, m_verbose);
      



      maxSubsetSize = Math.max(maxSubsetSize, searchResults[f].getBestGroup().cardinality());
    }
    

    if (m_verbose) {
      System.out.println("continue searches on internal folds to maxSubsetSize (" + maxSubsetSize + ")");
    }
    


    for (int f = 0; f < m_numFolds; f++) {
      if (m_verbose) {
        System.out.print("perform search on internal fold: " + (f + 1) + "/" + m_numFolds + " with starting set ");
        
        LFSMethods.printGroup(searchResults[f].getBestGroup(), trainData[f].numAttributes());
      }
      

      if (searchResults[f].getBestGroup().cardinality() < maxSubsetSize) {
        m_setSizeEval.buildEvaluator(trainData[f]);
        searchResults[f].forwardSearch(m_cacheSize, searchResults[f].getBestGroup(), ranking, m_numUsedAttributes, m_linearSelectionType == 1, 1, maxSubsetSize, trainData[f], (SubsetEvaluator)m_setSizeEval, m_verbose);
      }
    }
    



    double[][] testMerit = new double[m_numFolds][maxSubsetSize + 1];
    
    for (int f = 0; f < m_numFolds; f++) {
      for (int s = 1; s <= maxSubsetSize; s++) {
        if (HoldOutSubsetEvaluator.class.isInstance(m_setSizeEval)) {
          m_setSizeEval.buildEvaluator(trainData[f]);
          testMerit[f][s] = ((HoldOutSubsetEvaluator)m_setSizeEval).evaluateSubset(searchResults[f].getBestGroupOfSize(s), testData[f]);

        }
        else
        {
          m_setSizeEval.buildEvaluator(testData[f]);
          testMerit[f][s] = ((SubsetEvaluator)m_setSizeEval).evaluateSubset(searchResults[f].getBestGroupOfSize(s));
        }
      }
    }
    



    double[] avgTestMerit = new double[maxSubsetSize + 1];
    int finalSubsetSize = -1;
    
    for (int s = 1; s <= maxSubsetSize; s++) {
      for (int f = 0; f < m_numFolds; f++) {
        avgTestMerit[s] = ((avgTestMerit[s] * f + testMerit[f][s]) / (f + 1));
      }
      

      if ((finalSubsetSize == -1) || (avgTestMerit[s] > avgTestMerit[finalSubsetSize]))
      {
        finalSubsetSize = s;
      }
      
      if (m_verbose) {
        System.out.println("average merit for subset-size " + s + ": " + avgTestMerit[s]);
      }
    }
    

    if (m_verbose) {
      System.out.println("performing final forward selection to subset-size: " + finalSubsetSize);
    }
    

    ASEval.buildEvaluator(data);
    LSF.forwardSearch(m_cacheSize, new BitSet(m_numAttribs), ranking, m_numUsedAttributes, m_linearSelectionType == 1, 1, finalSubsetSize, data, (SubsetEvaluator)ASEval, m_verbose);
    


    m_totalEvals = LSF.getNumEvalsTotal();
    m_bestMerit = LSF.getBestMerit();
    
    return attributeList(LSF.getBestGroup());
  }
  


  protected void resetOptions()
  {
    m_performRanking = true;
    m_numUsedAttributes = 50;
    m_linearSelectionType = 0;
    m_setSizeEval = new ClassifierSubsetEval();
    m_numFolds = 5;
    m_seed = 1;
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
    return RevisionUtils.extract("$Revision: 11198 $");
  }
}

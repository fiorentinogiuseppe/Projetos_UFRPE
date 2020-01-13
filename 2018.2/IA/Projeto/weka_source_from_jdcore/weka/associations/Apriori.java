package weka.associations;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
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
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;






















































































































































public class Apriori
  extends AbstractAssociator
  implements OptionHandler, CARuleMiner, TechnicalInformationHandler
{
  static final long serialVersionUID = 3277498842319212687L;
  protected double m_minSupport;
  protected double m_upperBoundMinSupport;
  protected double m_lowerBoundMinSupport;
  protected static final int CONFIDENCE = 0;
  protected static final int LIFT = 1;
  protected static final int LEVERAGE = 2;
  protected static final int CONVICTION = 3;
  public static final Tag[] TAGS_SELECTION = { new Tag(0, "Confidence"), new Tag(1, "Lift"), new Tag(2, "Leverage"), new Tag(3, "Conviction") };
  



  protected int m_metricType = 0;
  


  protected double m_minMetric;
  


  protected int m_numRules;
  


  protected double m_delta;
  


  protected double m_significanceLevel;
  


  protected int m_cycles;
  


  protected FastVector m_Ls;
  

  protected FastVector m_hashtables;
  

  protected FastVector[] m_allTheRules;
  

  protected Instances m_instances;
  

  protected boolean m_outputItemSets;
  

  protected boolean m_removeMissingCols;
  

  protected boolean m_verbose;
  

  protected Instances m_onlyClass;
  

  protected int m_classIndex;
  

  protected boolean m_car;
  


  public String globalInfo()
  {
    return "Class implementing an Apriori-type algorithm. Iteratively reduces the minimum support until it finds the required number of rules with the given minimum confidence.\nThe algorithm has an option to mine class association rules. It is adapted as explained in the second reference.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    Messages.getInstance();result.setValue(TechnicalInformation.Field.AUTHOR, Messages.getString("APRIORI_AUTHOR"));
    
    result.setValue(TechnicalInformation.Field.TITLE, "Fast Algorithms for Mining Association Rules in Large Databases");
    
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "20th International Conference on Very Large Data Bases");
    
    result.setValue(TechnicalInformation.Field.YEAR, "1994");
    result.setValue(TechnicalInformation.Field.PAGES, "478-499");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann, Los Altos, CA");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Bing Liu and Wynne Hsu and Yiming Ma");
    additional.setValue(TechnicalInformation.Field.TITLE, "Integrating Classification and Association Rule Mining");
    
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "Fourth International Conference on Knowledge Discovery and Data Mining");
    

    additional.setValue(TechnicalInformation.Field.YEAR, "1998");
    additional.setValue(TechnicalInformation.Field.PAGES, "80-86");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "AAAI Press");
    
    return result;
  }
  




  public Apriori()
  {
    resetOptions();
  }
  



  public void resetOptions()
  {
    m_removeMissingCols = false;
    m_verbose = false;
    m_delta = 0.05D;
    m_minMetric = 0.9D;
    m_numRules = 10;
    m_lowerBoundMinSupport = 0.1D;
    m_upperBoundMinSupport = 1.0D;
    m_significanceLevel = -1.0D;
    m_outputItemSets = false;
    m_car = false;
    m_classIndex = -1;
  }
  







  protected Instances removeMissingColumns(Instances instances)
    throws Exception
  {
    int numInstances = instances.numInstances();
    StringBuffer deleteString = new StringBuffer();
    int removeCount = 0;
    boolean first = true;
    int maxCount = 0;
    
    for (int i = 0; i < instances.numAttributes(); i++) {
      AttributeStats as = instances.attributeStats(i);
      if ((m_upperBoundMinSupport == 1.0D) && (maxCount != numInstances))
      {
        int[] counts = nominalCounts;
        if (counts[Utils.maxIndex(counts)] > maxCount) {
          maxCount = counts[Utils.maxIndex(counts)];
        }
      }
      if (missingCount == numInstances) {
        if (first) {
          deleteString.append(i + 1);
          first = false;
        } else {
          deleteString.append("," + (i + 1));
        }
        removeCount++;
      }
    }
    if (m_verbose) {
      System.err.println("Removed : " + removeCount + " columns with all missing " + "values.");
    }
    
    if ((m_upperBoundMinSupport == 1.0D) && (maxCount != numInstances)) {
      m_upperBoundMinSupport = (maxCount / numInstances);
      if (m_verbose) {
        System.err.println("Setting upper bound min support to : " + m_upperBoundMinSupport);
      }
    }
    

    if (deleteString.toString().length() > 0) {
      Remove af = new Remove();
      af.setAttributeIndices(deleteString.toString());
      af.setInvertSelection(false);
      af.setInputFormat(instances);
      Instances newInst = Filter.useFilter(instances, af);
      
      return newInst;
    }
    return instances;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NO_CLASS);
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  









  public void buildAssociations(Instances instances)
    throws Exception
  {
    int necSupport = 0;
    
    instances = new Instances(instances);
    
    if (m_removeMissingCols) {
      instances = removeMissingColumns(instances);
    }
    if ((m_car) && (m_metricType != 0)) {
      throw new Exception("For CAR-Mining metric type has to be confidence!");
    }
    
    if (m_car) {
      if (m_classIndex == -1) {
        instances.setClassIndex(instances.numAttributes() - 1);
      } else if ((m_classIndex <= instances.numAttributes()) && (m_classIndex > 0)) {
        instances.setClassIndex(m_classIndex - 1);
      } else {
        throw new Exception("Invalid class index.");
      }
    }
    

    getCapabilities().testWithFail(instances);
    
    m_cycles = 0;
    

    double lowerBoundMinSupportToUse = m_lowerBoundMinSupport * instances.numInstances() < 1.0D ? 1.0D / instances.numInstances() : m_lowerBoundMinSupport;
    


    if (m_car)
    {
      m_instances = LabeledItemSet.divide(instances, false);
      

      m_onlyClass = LabeledItemSet.divide(instances, true);
    } else {
      m_instances = instances;
    }
    if ((m_car) && (m_numRules == Integer.MAX_VALUE))
    {
      m_minSupport = lowerBoundMinSupportToUse;
    }
    else {
      m_minSupport = (m_upperBoundMinSupport - m_delta);
      m_minSupport = (m_minSupport < lowerBoundMinSupportToUse ? lowerBoundMinSupportToUse : m_minSupport);
    }
    


    do
    {
      m_Ls = new FastVector();
      m_hashtables = new FastVector();
      m_allTheRules = new FastVector[6];
      m_allTheRules[0] = new FastVector();
      m_allTheRules[1] = new FastVector();
      m_allTheRules[2] = new FastVector();
      if ((m_metricType != 0) || (m_significanceLevel != -1.0D)) {
        m_allTheRules[3] = new FastVector();
        m_allTheRules[4] = new FastVector();
        m_allTheRules[5] = new FastVector();
      }
      FastVector[] sortedRuleSet = new FastVector[6];
      sortedRuleSet[0] = new FastVector();
      sortedRuleSet[1] = new FastVector();
      sortedRuleSet[2] = new FastVector();
      if ((m_metricType != 0) || (m_significanceLevel != -1.0D)) {
        sortedRuleSet[3] = new FastVector();
        sortedRuleSet[4] = new FastVector();
        sortedRuleSet[5] = new FastVector();
      }
      if (!m_car)
      {
        findLargeItemSets();
        if ((m_significanceLevel != -1.0D) || (m_metricType != 0)) {
          findRulesBruteForce();
        } else
          findRulesQuickly();
      } else {
        findLargeCarItemSets();
        findCarRulesQuickly();
      }
      

      if (m_upperBoundMinSupport < 1.0D) {
        pruneRulesForUpperBoundSupport();
      }
      
















      int j = m_allTheRules[2].size() - 1;
      double[] supports = new double[m_allTheRules[2].size()];
      for (int i = 0; i < j + 1; i++) {
        supports[(j - i)] = (((ItemSet)m_allTheRules[1].elementAt(j - i)).support() * -1.0D);
      }
      int[] indices = Utils.stableSort(supports);
      for (int i = 0; i < j + 1; i++) {
        sortedRuleSet[0].addElement(m_allTheRules[0].elementAt(indices[(j - i)]));
        sortedRuleSet[1].addElement(m_allTheRules[1].elementAt(indices[(j - i)]));
        sortedRuleSet[2].addElement(m_allTheRules[2].elementAt(indices[(j - i)]));
        if ((m_metricType != 0) || (m_significanceLevel != -1.0D)) {
          sortedRuleSet[3].addElement(m_allTheRules[3].elementAt(indices[(j - i)]));
          
          sortedRuleSet[4].addElement(m_allTheRules[4].elementAt(indices[(j - i)]));
          
          sortedRuleSet[5].addElement(m_allTheRules[5].elementAt(indices[(j - i)]));
        }
      }
      


      m_allTheRules[0].removeAllElements();
      m_allTheRules[1].removeAllElements();
      m_allTheRules[2].removeAllElements();
      if ((m_metricType != 0) || (m_significanceLevel != -1.0D)) {
        m_allTheRules[3].removeAllElements();
        m_allTheRules[4].removeAllElements();
        m_allTheRules[5].removeAllElements();
      }
      double[] confidences = new double[sortedRuleSet[2].size()];
      int sortType = 2 + m_metricType;
      
      for (int i = 0; i < sortedRuleSet[2].size(); i++) {
        confidences[i] = ((Double)sortedRuleSet[sortType].elementAt(i)).doubleValue();
      }
      indices = Utils.stableSort(confidences);
      for (int i = sortedRuleSet[0].size() - 1; 
          (i >= sortedRuleSet[0].size() - m_numRules) && (i >= 0); i--) {
        m_allTheRules[0].addElement(sortedRuleSet[0].elementAt(indices[i]));
        m_allTheRules[1].addElement(sortedRuleSet[1].elementAt(indices[i]));
        m_allTheRules[2].addElement(sortedRuleSet[2].elementAt(indices[i]));
        if ((m_metricType != 0) || (m_significanceLevel != -1.0D)) {
          m_allTheRules[3].addElement(sortedRuleSet[3].elementAt(indices[i]));
          m_allTheRules[4].addElement(sortedRuleSet[4].elementAt(indices[i]));
          m_allTheRules[5].addElement(sortedRuleSet[5].elementAt(indices[i]));
        }
      }
      
      if ((m_verbose) && 
        (m_Ls.size() > 1)) {
        System.out.println(toString());
      }
      

      if ((m_minSupport == lowerBoundMinSupportToUse) || (m_minSupport - m_delta > lowerBoundMinSupportToUse))
      {
        m_minSupport -= m_delta;
      } else {
        m_minSupport = lowerBoundMinSupportToUse;
      }
      necSupport = Math.round((float)(m_minSupport * m_instances.numInstances() + 0.5D));
      

      m_cycles += 1;


    }
    while ((m_allTheRules[0].size() < m_numRules) && (Utils.grOrEq(m_minSupport, lowerBoundMinSupportToUse)) && (necSupport >= 1));
    m_minSupport += m_delta;
  }
  
  private void pruneRulesForUpperBoundSupport() {
    int necMaxSupport = (int)(m_upperBoundMinSupport * m_instances.numInstances() + 0.5D);
    

    FastVector[] prunedRules = new FastVector[6];
    for (int i = 0; i < 6; i++) {
      prunedRules[i] = new FastVector();
    }
    
    for (int i = 0; i < m_allTheRules[0].size(); i++) {
      if (((ItemSet)m_allTheRules[1].elementAt(i)).support() <= necMaxSupport) {
        prunedRules[0].addElement(m_allTheRules[0].elementAt(i));
        prunedRules[1].addElement(m_allTheRules[1].elementAt(i));
        prunedRules[2].addElement(m_allTheRules[2].elementAt(i));
        
        if (!m_car) {
          prunedRules[3].addElement(m_allTheRules[3].elementAt(i));
          prunedRules[4].addElement(m_allTheRules[4].elementAt(i));
          prunedRules[5].addElement(m_allTheRules[5].elementAt(i));
        }
      }
    }
    
    m_allTheRules[0] = prunedRules[0];
    m_allTheRules[1] = prunedRules[1];
    m_allTheRules[2] = prunedRules[2];
    m_allTheRules[3] = prunedRules[3];
    m_allTheRules[4] = prunedRules[4];
    m_allTheRules[5] = prunedRules[5];
  }
  








  public FastVector[] mineCARs(Instances data)
    throws Exception
  {
    m_car = true;
    buildAssociations(data);
    return m_allTheRules;
  }
  





  public Instances getInstancesNoClass()
  {
    return m_instances;
  }
  





  public Instances getInstancesOnlyClass()
  {
    return m_onlyClass;
  }
  





  public Enumeration listOptions()
  {
    String string1 = "\tThe required number of rules. (default = " + m_numRules + ")";
    String string2 = "\tThe minimum confidence of a rule. (default = " + m_minMetric + ")";
    String string3 = "\tThe delta by which the minimum support is decreased in\n";String string4 = "\teach iteration. (default = " + m_delta + ")";
    String string5 = "\tThe lower bound for the minimum support. (default = " + m_lowerBoundMinSupport + ")";
    String string6 = "\tIf used, rules are tested for significance at\n";String string7 = "\tthe given level. Slower. (default = no significance testing)";String string8 = "\tIf set the itemsets found are also output. (default = no)";String string9 = "\tIf set class association rules are mined. (default = no)";String string10 = "\tThe class index. (default = last)";String stringType = "\tThe metric type by which to rank rules. (default = confidence)";
    

    FastVector newVector = new FastVector(11);
    
    newVector.addElement(new Option(string1, "N", 1, "-N <required number of rules output>"));
    
    newVector.addElement(new Option(stringType, "T", 1, "-T <0=confidence | 1=lift | 2=leverage | 3=Conviction>"));
    
    newVector.addElement(new Option(string2, "C", 1, "-C <minimum metric score of a rule>"));
    
    newVector.addElement(new Option(string3 + string4, "D", 1, "-D <delta for minimum support>"));
    
    newVector.addElement(new Option("\tUpper bound for minimum support. (default = 1.0)", "U", 1, "-U <upper bound for minimum support>"));
    
    newVector.addElement(new Option(string5, "M", 1, "-M <lower bound for minimum support>"));
    
    newVector.addElement(new Option(string6 + string7, "S", 1, "-S <significance level>"));
    
    newVector.addElement(new Option(string8, "I", 0, "-I"));
    newVector.addElement(new Option("\tRemove columns that contain all missing values (default = no)", "R", 0, "-R"));
    
    newVector.addElement(new Option("\tReport progress iteratively. (default = no)", "V", 0, "-V"));
    
    newVector.addElement(new Option(string9, "A", 0, "-A"));
    newVector.addElement(new Option(string10, "c", 1, "-c <the class index>"));
    
    return newVector.elements();
  }
  









































































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    String numRulesString = Utils.getOption('N', options);String minConfidenceString = Utils.getOption('C', options);
    String deltaString = Utils.getOption('D', options);String maxSupportString = Utils.getOption('U', options);
    String minSupportString = Utils.getOption('M', options);
    String significanceLevelString = Utils.getOption('S', options);String classIndexString = Utils.getOption('c', options);
    
    String metricTypeString = Utils.getOption('T', options);
    if (metricTypeString.length() != 0) {
      setMetricType(new SelectedTag(Integer.parseInt(metricTypeString), TAGS_SELECTION));
    }
    

    if (numRulesString.length() != 0) {
      m_numRules = Integer.parseInt(numRulesString);
    }
    if (classIndexString.length() != 0) {
      if (classIndexString.equalsIgnoreCase("last")) {
        m_classIndex = -1;
      } else if (classIndexString.equalsIgnoreCase("first")) {
        m_classIndex = 0;
      } else {
        m_classIndex = Integer.parseInt(classIndexString);
      }
    }
    if (minConfidenceString.length() != 0) {
      m_minMetric = new Double(minConfidenceString).doubleValue();
    }
    if (deltaString.length() != 0) {
      m_delta = new Double(deltaString).doubleValue();
    }
    if (maxSupportString.length() != 0) {
      setUpperBoundMinSupport(new Double(maxSupportString).doubleValue());
    }
    if (minSupportString.length() != 0) {
      m_lowerBoundMinSupport = new Double(minSupportString).doubleValue();
    }
    if (significanceLevelString.length() != 0) {
      m_significanceLevel = new Double(significanceLevelString).doubleValue();
    }
    m_outputItemSets = Utils.getFlag('I', options);
    m_car = Utils.getFlag('A', options);
    m_verbose = Utils.getFlag('V', options);
    setRemoveAllMissingCols(Utils.getFlag('R', options));
  }
  





  public String[] getOptions()
  {
    String[] options = new String[20];
    int current = 0;
    
    if (m_outputItemSets) {
      options[(current++)] = "-I";
    }
    
    if (getRemoveAllMissingCols()) {
      options[(current++)] = "-R";
    }
    
    options[(current++)] = "-N";
    options[(current++)] = ("" + m_numRules);
    options[(current++)] = "-T";
    options[(current++)] = ("" + m_metricType);
    options[(current++)] = "-C";
    options[(current++)] = ("" + m_minMetric);
    options[(current++)] = "-D";
    options[(current++)] = ("" + m_delta);
    options[(current++)] = "-U";
    options[(current++)] = ("" + m_upperBoundMinSupport);
    options[(current++)] = "-M";
    options[(current++)] = ("" + m_lowerBoundMinSupport);
    options[(current++)] = "-S";
    options[(current++)] = ("" + m_significanceLevel);
    if (m_car)
      options[(current++)] = "-A";
    if (m_verbose)
      options[(current++)] = "-V";
    options[(current++)] = "-c";
    options[(current++)] = ("" + m_classIndex);
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  






  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_Ls.size() <= 1)
      return "\nNo large itemsets and rules found!\n";
    text.append("\nApriori\n=======\n\n");
    text.append("Minimum support: " + Utils.doubleToString(m_minSupport, 2) + " (" + (int)(m_minSupport * m_instances.numInstances() + 0.5D) + " instances)" + '\n');
    

    text.append("Minimum metric <");
    switch (m_metricType) {
    case 0: 
      text.append("confidence>: ");
      break;
    case 1: 
      text.append("lift>: ");
      break;
    case 2: 
      text.append("leverage>: ");
      break;
    case 3: 
      text.append("conviction>: ");
    }
    
    text.append(Utils.doubleToString(m_minMetric, 2) + '\n');
    
    if (m_significanceLevel != -1.0D) {
      text.append("Significance level: " + Utils.doubleToString(m_significanceLevel, 2) + '\n');
    }
    text.append("Number of cycles performed: " + m_cycles + '\n');
    text.append("\nGenerated sets of large itemsets:\n");
    if (!m_car) {
      for (int i = 0; i < m_Ls.size(); i++) {
        text.append("\nSize of set of large itemsets L(" + (i + 1) + "): " + ((FastVector)m_Ls.elementAt(i)).size() + '\n');
        
        if (m_outputItemSets) {
          text.append("\nLarge Itemsets L(" + (i + 1) + "):\n");
          for (int j = 0; j < ((FastVector)m_Ls.elementAt(i)).size(); j++) {
            text.append(((AprioriItemSet)((FastVector)m_Ls.elementAt(i)).elementAt(j)).toString(m_instances) + "\n");
          }
        }
      }
      text.append("\nBest rules found:\n\n");
      for (int i = 0; i < m_allTheRules[0].size(); i++) {
        text.append(Utils.doubleToString(i + 1.0D, (int)(Math.log(m_numRules) / Math.log(10.0D) + 1.0D), 0) + ". " + ((AprioriItemSet)m_allTheRules[0].elementAt(i)).toString(m_instances) + " ==> " + ((AprioriItemSet)m_allTheRules[1].elementAt(i)).toString(m_instances) + "    conf:(" + Utils.doubleToString(((Double)m_allTheRules[2].elementAt(i)).doubleValue(), 2) + ")");
        










        if ((m_metricType != 0) || (m_significanceLevel != -1.0D)) {
          text.append((m_metricType == 1 ? " <" : "") + " lift:(" + Utils.doubleToString(((Double)m_allTheRules[3].elementAt(i)).doubleValue(), 2) + ")" + (m_metricType == 1 ? ">" : ""));
          



          text.append((m_metricType == 2 ? " <" : "") + " lev:(" + Utils.doubleToString(((Double)m_allTheRules[4].elementAt(i)).doubleValue(), 2) + ")");
          



          text.append(" [" + (int)(((Double)m_allTheRules[4].elementAt(i)).doubleValue() * m_instances.numInstances()) + "]" + (m_metricType == 2 ? ">" : ""));
          


          text.append((m_metricType == 3 ? " <" : "") + " conv:(" + Utils.doubleToString(((Double)m_allTheRules[5].elementAt(i)).doubleValue(), 2) + ")" + (m_metricType == 3 ? ">" : ""));
        }
        



        text.append('\n');
      }
    } else {
      for (int i = 0; i < m_Ls.size(); i++) {
        text.append("\nSize of set of large itemsets L(" + (i + 1) + "): " + ((FastVector)m_Ls.elementAt(i)).size() + '\n');
        
        if (m_outputItemSets) {
          text.append("\nLarge Itemsets L(" + (i + 1) + "):\n");
          for (int j = 0; j < ((FastVector)m_Ls.elementAt(i)).size(); j++) {
            text.append(((ItemSet)((FastVector)m_Ls.elementAt(i)).elementAt(j)).toString(m_instances) + "\n");
            
            text.append(m_Ls.elementAt(i)).elementAt(j)).m_classLabel + "  ");
            
            text.append(((LabeledItemSet)((FastVector)m_Ls.elementAt(i)).elementAt(j)).support() + "\n");
          }
        }
      }
      
      text.append("\nBest rules found:\n\n");
      for (int i = 0; i < m_allTheRules[0].size(); i++) {
        text.append(Utils.doubleToString(i + 1.0D, (int)(Math.log(m_numRules) / Math.log(10.0D) + 1.0D), 0) + ". " + ((ItemSet)m_allTheRules[0].elementAt(i)).toString(m_instances) + " ==> " + ((ItemSet)m_allTheRules[1].elementAt(i)).toString(m_onlyClass) + "    conf:(" + Utils.doubleToString(((Double)m_allTheRules[2].elementAt(i)).doubleValue(), 2) + ")");
        









        text.append('\n');
      }
    }
    return text.toString();
  }
  






  public String metricString()
  {
    switch (m_metricType) {
    case 1: 
      return "lif";
    case 2: 
      return "leverage";
    case 3: 
      return "conviction";
    }
    return "conf";
  }
  






  public String removeAllMissingColsTipText()
  {
    return "Remove columns with all missing values.";
  }
  




  public void setRemoveAllMissingCols(boolean r)
  {
    m_removeMissingCols = r;
  }
  




  public boolean getRemoveAllMissingCols()
  {
    return m_removeMissingCols;
  }
  





  public String upperBoundMinSupportTipText()
  {
    return "Upper bound for minimum support. Start iteratively decreasing minimum support from this value.";
  }
  






  public double getUpperBoundMinSupport()
  {
    return m_upperBoundMinSupport;
  }
  





  public void setUpperBoundMinSupport(double v)
  {
    m_upperBoundMinSupport = v;
  }
  





  public void setClassIndex(int index)
  {
    m_classIndex = index;
  }
  





  public int getClassIndex()
  {
    return m_classIndex;
  }
  





  public String classIndexTipText()
  {
    return "Index of the class attribute. If set to -1, the last attribute is taken as class attribute.";
  }
  





  public void setCar(boolean flag)
  {
    m_car = flag;
  }
  




  public boolean getCar()
  {
    return m_car;
  }
  





  public String carTipText()
  {
    return "If enabled class association rules are mined instead of (general) association rules.";
  }
  





  public String lowerBoundMinSupportTipText()
  {
    return "Lower bound for minimum support.";
  }
  





  public double getLowerBoundMinSupport()
  {
    return m_lowerBoundMinSupport;
  }
  





  public void setLowerBoundMinSupport(double v)
  {
    m_lowerBoundMinSupport = v;
  }
  




  public SelectedTag getMetricType()
  {
    return new SelectedTag(m_metricType, TAGS_SELECTION);
  }
  





  public String metricTypeTipText()
  {
    return "Set the type of metric by which to rank rules. Confidence is the proportion of the examples covered by the premise that are also covered by the consequence(Class association rules can only be mined using confidence). Lift is confidence divided by the proportion of all examples that are covered by the consequence. This is a measure of the importance of the association that is independent of support. Leverage is the proportion of additional examples covered by both the premise and consequence above those expected if the premise and consequence were independent of each other. The total number of examples that this represents is presented in brackets following the leverage. Conviction is another measure of departure from independence. Conviction is given by P(premise)P(!consequence) / P(premise, !consequence).";
  }
  
















  public void setMetricType(SelectedTag d)
  {
    if (d.getTags() == TAGS_SELECTION) {
      m_metricType = d.getSelectedTag().getID();
    }
    
    if (m_metricType == 0) {
      setMinMetric(0.9D);
    }
    
    if ((m_metricType == 1) || (m_metricType == 3)) {
      setMinMetric(1.1D);
    }
    
    if (m_metricType == 2) {
      setMinMetric(0.1D);
    }
  }
  





  public String minMetricTipText()
  {
    return "Minimum metric score. Consider only rules with scores higher than this value.";
  }
  






  public double getMinMetric()
  {
    return m_minMetric;
  }
  





  public void setMinMetric(double v)
  {
    m_minMetric = v;
  }
  





  public String numRulesTipText()
  {
    return "Number of rules to find.";
  }
  





  public int getNumRules()
  {
    return m_numRules;
  }
  





  public void setNumRules(int v)
  {
    m_numRules = v;
  }
  





  public String deltaTipText()
  {
    return "Iteratively decrease support by this factor. Reduces support until min support is reached or required number of rules has been generated.";
  }
  







  public double getDelta()
  {
    return m_delta;
  }
  





  public void setDelta(double v)
  {
    m_delta = v;
  }
  





  public String significanceLevelTipText()
  {
    return "Significance level. Significance test (confidence metric only).";
  }
  





  public double getSignificanceLevel()
  {
    return m_significanceLevel;
  }
  





  public void setSignificanceLevel(double v)
  {
    m_significanceLevel = v;
  }
  




  public void setOutputItemSets(boolean flag)
  {
    m_outputItemSets = flag;
  }
  




  public boolean getOutputItemSets()
  {
    return m_outputItemSets;
  }
  





  public String outputItemSetsTipText()
  {
    return "If enabled the itemsets are output as well.";
  }
  




  public void setVerbose(boolean flag)
  {
    m_verbose = flag;
  }
  




  public boolean getVerbose()
  {
    return m_verbose;
  }
  





  public String verboseTipText()
  {
    return "If enabled the algorithm will be run in verbose mode.";
  }
  






  private void findLargeItemSets()
    throws Exception
  {
    int i = 0;
    



    int necSupport = (int)(m_minSupport * m_instances.numInstances() + 0.5D);
    int necMaxSupport = (int)(m_upperBoundMinSupport * m_instances.numInstances() + 0.5D);
    
    FastVector kSets = AprioriItemSet.singletons(m_instances);
    AprioriItemSet.upDateCounters(kSets, m_instances);
    kSets = AprioriItemSet.deleteItemSets(kSets, necSupport, m_instances.numInstances());
    
    if (kSets.size() == 0)
      return;
    do {
      m_Ls.addElement(kSets);
      FastVector kMinusOneSets = kSets;
      kSets = AprioriItemSet.mergeAllItemSets(kMinusOneSets, i, m_instances.numInstances());
      
      Hashtable hashtable = AprioriItemSet.getHashtable(kMinusOneSets, kMinusOneSets.size());
      
      m_hashtables.addElement(hashtable);
      kSets = AprioriItemSet.pruneItemSets(kSets, hashtable);
      AprioriItemSet.upDateCounters(kSets, m_instances);
      kSets = AprioriItemSet.deleteItemSets(kSets, necSupport, m_instances.numInstances());
      
      i++;
    } while (kSets.size() > 0);
  }
  







  private void findRulesBruteForce()
    throws Exception
  {
    for (int j = 1; j < m_Ls.size(); j++) {
      FastVector currentItemSets = (FastVector)m_Ls.elementAt(j);
      Enumeration enumItemSets = currentItemSets.elements();
      while (enumItemSets.hasMoreElements()) {
        AprioriItemSet currentItemSet = (AprioriItemSet)enumItemSets.nextElement();
        


        FastVector[] rules = currentItemSet.generateRulesBruteForce(m_minMetric, m_metricType, m_hashtables, j + 1, m_instances.numInstances(), m_significanceLevel);
        

        for (int k = 0; k < rules[0].size(); k++) {
          m_allTheRules[0].addElement(rules[0].elementAt(k));
          m_allTheRules[1].addElement(rules[1].elementAt(k));
          m_allTheRules[2].addElement(rules[2].elementAt(k));
          
          m_allTheRules[3].addElement(rules[3].elementAt(k));
          m_allTheRules[4].addElement(rules[4].elementAt(k));
          m_allTheRules[5].addElement(rules[5].elementAt(k));
        }
      }
    }
  }
  







  private void findRulesQuickly()
    throws Exception
  {
    for (int j = 1; j < m_Ls.size(); j++) {
      FastVector currentItemSets = (FastVector)m_Ls.elementAt(j);
      Enumeration enumItemSets = currentItemSets.elements();
      while (enumItemSets.hasMoreElements()) {
        AprioriItemSet currentItemSet = (AprioriItemSet)enumItemSets.nextElement();
        


        FastVector[] rules = currentItemSet.generateRules(m_minMetric, m_hashtables, j + 1);
        for (int k = 0; k < rules[0].size(); k++) {
          m_allTheRules[0].addElement(rules[0].elementAt(k));
          m_allTheRules[1].addElement(rules[1].elementAt(k));
          m_allTheRules[2].addElement(rules[2].elementAt(k));
        }
      }
    }
  }
  








  private void findLargeCarItemSets()
    throws Exception
  {
    int i = 0;
    



    double nextMinSupport = m_minSupport * m_instances.numInstances();
    double nextMaxSupport = m_upperBoundMinSupport * m_instances.numInstances();
    int necSupport; int necSupport; if (Math.rint(nextMinSupport) == nextMinSupport) {
      necSupport = (int)nextMinSupport;
    } else
      necSupport = Math.round((float)(nextMinSupport + 0.5D));
    int necMaxSupport;
    int necMaxSupport; if (Math.rint(nextMaxSupport) == nextMaxSupport) {
      necMaxSupport = (int)nextMaxSupport;
    } else {
      necMaxSupport = Math.round((float)(nextMaxSupport + 0.5D));
    }
    

    FastVector kSets = LabeledItemSet.singletons(m_instances, m_onlyClass);
    LabeledItemSet.upDateCounters(kSets, m_instances, m_onlyClass);
    

    kSets = LabeledItemSet.deleteItemSets(kSets, necSupport, m_instances.numInstances());
    
    if (kSets.size() == 0)
      return;
    do {
      m_Ls.addElement(kSets);
      FastVector kMinusOneSets = kSets;
      kSets = LabeledItemSet.mergeAllItemSets(kMinusOneSets, i, m_instances.numInstances());
      
      Hashtable hashtable = LabeledItemSet.getHashtable(kMinusOneSets, kMinusOneSets.size());
      
      kSets = LabeledItemSet.pruneItemSets(kSets, hashtable);
      LabeledItemSet.upDateCounters(kSets, m_instances, m_onlyClass);
      kSets = LabeledItemSet.deleteItemSets(kSets, necSupport, m_instances.numInstances());
      
      i++;
    } while (kSets.size() > 0);
  }
  







  private void findCarRulesQuickly()
    throws Exception
  {
    for (int j = 0; j < m_Ls.size(); j++) {
      FastVector currentLabeledItemSets = (FastVector)m_Ls.elementAt(j);
      Enumeration enumLabeledItemSets = currentLabeledItemSets.elements();
      while (enumLabeledItemSets.hasMoreElements()) {
        LabeledItemSet currentLabeledItemSet = (LabeledItemSet)enumLabeledItemSets.nextElement();
        
        FastVector[] rules = currentLabeledItemSet.generateRules(m_minMetric, false);
        for (int k = 0; k < rules[0].size(); k++) {
          m_allTheRules[0].addElement(rules[0].elementAt(k));
          m_allTheRules[1].addElement(rules[1].elementAt(k));
          m_allTheRules[2].addElement(rules[2].elementAt(k));
        }
      }
    }
  }
  





  public FastVector[] getAllTheRules()
  {
    return m_allTheRules;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9096 $");
  }
  




  public static void main(String[] args)
  {
    runAssociator(new Apriori(), args);
  }
}

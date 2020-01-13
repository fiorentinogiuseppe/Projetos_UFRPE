package weka.associations;

import java.awt.Button;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import weka.associations.tertius.AttributeValueLiteral;
import weka.associations.tertius.IndividualInstances;
import weka.associations.tertius.IndividualLiteral;
import weka.associations.tertius.Literal;
import weka.associations.tertius.Predicate;
import weka.associations.tertius.Rule;
import weka.associations.tertius.SimpleLinkedList;
import weka.associations.tertius.SimpleLinkedList.LinkedListInverseIterator;
import weka.associations.tertius.SimpleLinkedList.LinkedListIterator;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
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






















































































































































public class Tertius
  extends AbstractAssociator
  implements OptionHandler, Runnable, TechnicalInformationHandler
{
  static final long serialVersionUID = 5556726848380738179L;
  private SimpleLinkedList m_results;
  private int m_hypotheses;
  private int m_explored;
  private Date m_time;
  private TextField m_valuesText;
  private Instances m_instances;
  private ArrayList m_predicates;
  private int m_status;
  private static final int NORMAL = 0;
  private static final int MEMORY = 1;
  private static final int STOP = 2;
  private int m_best;
  private double m_frequencyThreshold;
  private double m_confirmationThreshold;
  private double m_noiseThreshold;
  private boolean m_repeat;
  private int m_numLiterals;
  private static final int NONE = 0;
  private static final int BODY = 1;
  private static final int HEAD = 2;
  private static final int ALL = 3;
  private static final Tag[] TAGS_NEGATION = { new Tag(0, "None"), new Tag(1, "Body"), new Tag(2, "Head"), new Tag(3, "Both") };
  


  private int m_negation;
  


  private boolean m_classification;
  


  private int m_classIndex;
  


  private boolean m_horn;
  

  private boolean m_equivalent;
  

  private boolean m_sameClause;
  

  private boolean m_subsumption;
  

  public static final int EXPLICIT = 0;
  

  public static final int IMPLICIT = 1;
  

  public static final int SIGNIFICANT = 2;
  

  private static final Tag[] TAGS_MISSING = { new Tag(0, "Matches all"), new Tag(1, "Matches none"), new Tag(2, "Significant") };
  


  private int m_missing;
  

  private boolean m_roc;
  

  private String m_partsString;
  

  private Instances m_parts;
  

  private static final int NO = 0;
  

  private static final int OUT = 1;
  

  private static final int WINDOW = 2;
  

  private static final Tag[] TAGS_VALUES = { new Tag(0, "No"), new Tag(1, "stdout"), new Tag(2, "Window") };
  




  private int m_printValues;
  




  public Tertius()
  {
    resetOptions();
  }
  





  public String globalInfo()
  {
    return "Finds rules according to confirmation measure (Tertius-type algorithm).\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "P. A. Flach and N. Lachiche");
    result.setValue(TechnicalInformation.Field.YEAR, "1999");
    result.setValue(TechnicalInformation.Field.TITLE, "Confirmation-Guided Discovery of first-order rules with Tertius");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "42");
    result.setValue(TechnicalInformation.Field.PAGES, "61-95");
    
    return result;
  }
  




  public void resetOptions()
  {
    m_best = 10;
    m_frequencyThreshold = 0.0D;
    m_confirmationThreshold = 0.0D;
    m_noiseThreshold = 1.0D;
    

    m_repeat = false;
    m_numLiterals = 4;
    m_negation = 0;
    m_classification = false;
    m_classIndex = 0;
    m_horn = false;
    

    m_equivalent = true;
    m_sameClause = true;
    m_subsumption = true;
    

    m_missing = 0;
    

    m_roc = false;
    

    m_partsString = "";
    m_parts = null;
    

    m_printValues = 0;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(17);
    

    newVector.addElement(new Option("\tSet maximum number of confirmation  values in the result. (default: 10)", "K", 1, "-K <number of values in result>"));
    

    newVector.addElement(new Option("\tSet frequency threshold for pruning. (default: 0)", "F", 1, "-F <frequency threshold>"));
    

    newVector.addElement(new Option("\tSet confirmation threshold. (default: 0)", "C", 1, "-C <confirmation threshold>"));
    

    newVector.addElement(new Option("\tSet noise threshold : maximum frequency of counter-examples.\n\t0 gives only satisfied rules. (default: 1)", "N", 1, "-N <noise threshold>"));
    




    newVector.addElement(new Option("\tAllow attributes to be repeated in a same rule.", "R", 0, "-R"));
    

    newVector.addElement(new Option("\tSet maximum number of literals in a rule. (default: 4)", "L", 1, "-L <number of literals>"));
    

    newVector.addElement(new Option("\tSet the negations in the rule. (default: 0)", "G", 1, "-G <0=no negation | 1=body | 2=head | 3=body and head>"));
    




    newVector.addElement(new Option("\tConsider only classification rules.", "S", 0, "-S"));
    
    newVector.addElement(new Option("\tSet index of class attribute. (default: last).", "c", 1, "-c <class index>"));
    

    newVector.addElement(new Option("\tConsider only horn clauses.", "H", 0, "-H"));
    


    newVector.addElement(new Option("\tKeep equivalent rules.", "E", 0, "-E"));
    
    newVector.addElement(new Option("\tKeep same clauses.", "M", 0, "-M"));
    
    newVector.addElement(new Option("\tKeep subsumed rules.", "T", 0, "-T"));
    


    newVector.addElement(new Option("\tSet the way to handle missing values. (default: 0)", "I", 1, "-I <0=always match | 1=never match | 2=significant>"));
    





    newVector.addElement(new Option("\tUse ROC analysis. ", "O", 0, "-O"));
    


    newVector.addElement(new Option("\tSet the file containing the parts of the individual for individual-based learning.", "p", 1, "-p <name of file>"));
    




    newVector.addElement(new Option("\tSet output of current values. (default: 0)", "P", 1, "-P <0=no output | 1=on stdout | 2=in separate window>"));
    




    return newVector.elements();
  }
  






























































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    

    String bestString = Utils.getOption('K', options);
    if (bestString.length() != 0) {
      try {
        m_best = Integer.parseInt(bestString);
      } catch (Exception e) {
        throw new Exception("Invalid value for -K option: " + e.getMessage() + ".");
      }
      
      if (m_best < 1) {
        throw new Exception("Number of confirmation values has to be greater than one!");
      }
    }
    
    String frequencyThresholdString = Utils.getOption('F', options);
    if (frequencyThresholdString.length() != 0) {
      try {
        m_frequencyThreshold = new Double(frequencyThresholdString).doubleValue();
      }
      catch (Exception e) {
        throw new Exception("Invalid value for -F option: " + e.getMessage() + ".");
      }
      
      if ((m_frequencyThreshold < 0.0D) || (m_frequencyThreshold > 1.0D)) {
        throw new Exception("Frequency threshold has to be between zero and one!");
      }
    }
    
    String confirmationThresholdString = Utils.getOption('C', options);
    if (confirmationThresholdString.length() != 0) {
      try {
        m_confirmationThreshold = new Double(confirmationThresholdString).doubleValue();
      }
      catch (Exception e) {
        throw new Exception("Invalid value for -C option: " + e.getMessage() + ".");
      }
      
      if ((m_confirmationThreshold < 0.0D) || (m_confirmationThreshold > 1.0D)) {
        throw new Exception("Confirmation threshold has to be between zero and one!");
      }
      
      if (bestString.length() != 0) {
        throw new Exception("Specifying both a number of confirmation values and a confirmation threshold doesn't make sense!");
      }
      

      if (m_confirmationThreshold != 0.0D) {
        m_best = 0;
      }
    }
    String noiseThresholdString = Utils.getOption('N', options);
    if (noiseThresholdString.length() != 0) {
      try {
        m_noiseThreshold = new Double(noiseThresholdString).doubleValue();
      } catch (Exception e) {
        throw new Exception("Invalid value for -N option: " + e.getMessage() + ".");
      }
      
      if ((m_noiseThreshold < 0.0D) || (m_noiseThreshold > 1.0D)) {
        throw new Exception("Noise threshold has to be between zero and one!");
      }
    }
    


    m_repeat = Utils.getFlag('R', options);
    String numLiteralsString = Utils.getOption('L', options);
    if (numLiteralsString.length() != 0) {
      try {
        m_numLiterals = Integer.parseInt(numLiteralsString);
      } catch (Exception e) {
        throw new Exception("Invalid value for -L option: " + e.getMessage() + ".");
      }
      
      if (m_numLiterals < 1) {
        throw new Exception("Number of literals has to be greater than one!");
      }
    }
    
    String negationString = Utils.getOption('G', options);
    if (negationString.length() != 0)
    {
      int tag;
      try {
        tag = Integer.parseInt(negationString);
      } catch (Exception e) {
        throw new Exception("Invalid value for -G option: " + e.getMessage() + ".");
      }
      SelectedTag selected;
      try {
        selected = new SelectedTag(tag, TAGS_NEGATION);
      } catch (Exception e) {
        throw new Exception("Value for -G option has to be between zero and three!");
      }
      
      setNegation(selected);
    }
    m_classification = Utils.getFlag('S', options);
    String classIndexString = Utils.getOption('c', options);
    if (classIndexString.length() != 0) {
      try {
        m_classIndex = Integer.parseInt(classIndexString);
      } catch (Exception e) {
        throw new Exception("Invalid value for -c option: " + e.getMessage() + ".");
      }
    }
    
    m_horn = Utils.getFlag('H', options);
    if ((m_horn) && (m_negation != 0)) {
      throw new Exception("Considering horn clauses doesn't make sense if negation allowed!");
    }
    


    m_equivalent = (!Utils.getFlag('E', options));
    m_sameClause = (!Utils.getFlag('M', options));
    m_subsumption = (!Utils.getFlag('T', options));
    

    String missingString = Utils.getOption('I', options);
    if (missingString.length() != 0)
    {
      int tag;
      try {
        tag = Integer.parseInt(missingString);
      } catch (Exception e) {
        throw new Exception("Invalid value for -I option: " + e.getMessage() + ".");
      }
      SelectedTag selected;
      try {
        selected = new SelectedTag(tag, TAGS_MISSING);
      } catch (Exception e) {
        throw new Exception("Value for -I option has to be between zero and two!");
      }
      
      setMissingValues(selected);
    }
    

    m_roc = Utils.getFlag('O', options);
    


    m_partsString = Utils.getOption('p', options);
    if (m_partsString.length() != 0) {
      Reader reader;
      try {
        reader = new BufferedReader(new FileReader(m_partsString));
      } catch (Exception e) {
        throw new Exception("Can't open file " + e.getMessage() + ".");
      }
      m_parts = new Instances(reader);
    }
    

    String printValuesString = Utils.getOption('P', options);
    if (printValuesString.length() != 0)
    {
      int tag;
      try {
        tag = Integer.parseInt(printValuesString);
      } catch (Exception e) {
        throw new Exception("Invalid value for -P option: " + e.getMessage() + ".");
      }
      SelectedTag selected;
      try {
        selected = new SelectedTag(tag, TAGS_VALUES);
      } catch (Exception e) {
        throw new Exception("Value for -P option has to be between zero and two!");
      }
      
      setValuesOutput(selected);
    }
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    

    if (m_best > 0) {
      result.add("-K");
      result.add("" + m_best);
    }
    
    result.add("-F");
    result.add("" + m_frequencyThreshold);
    
    if (m_confirmationThreshold > 0.0D) {
      result.add("-C");
      result.add("" + m_confirmationThreshold);
    }
    
    result.add("-N");
    result.add("" + m_noiseThreshold);
    

    if (m_repeat) {
      result.add("-R");
    }
    result.add("-L");
    result.add("" + m_numLiterals);
    
    result.add("-G");
    result.add("" + m_negation);
    
    if (m_classification) {
      result.add("-S");
    }
    result.add("-c");
    result.add("" + m_classIndex);
    
    if (m_horn) {
      result.add("-H");
    }
    
    if (!m_equivalent) {
      result.add("-E");
    }
    if (!m_sameClause) {
      result.add("-M");
    }
    if (!m_subsumption) {
      result.add("-T");
    }
    
    result.add("-I");
    result.add("" + m_missing);
    

    if (m_roc) {
      result.add("-O");
    }
    
    if (m_partsString.length() > 0) {
      result.add("-p");
      result.add("" + m_partsString);
    }
    

    result.add("-P");
    result.add("" + m_printValues);
    
    return (String[])result.toArray(new String[result.size()]);
  }
  






  public String confirmationValuesTipText()
  {
    return "Number of best confirmation values to find.";
  }
  





  public int getConfirmationValues()
  {
    return m_best;
  }
  





  public void setConfirmationValues(int v)
  {
    m_best = v;
  }
  






  public String frequencyThresholdTipText()
  {
    return "Minimum proportion of instances satisfying head and body of rules";
  }
  





  public double getFrequencyThreshold()
  {
    return m_frequencyThreshold;
  }
  





  public void setFrequencyThreshold(double v)
  {
    m_frequencyThreshold = v;
  }
  






  public String confirmationThresholdTipText()
  {
    return "Minimum confirmation of the rules.";
  }
  





  public double getConfirmationThreshold()
  {
    return m_confirmationThreshold;
  }
  





  public void setConfirmationThreshold(double v)
  {
    m_confirmationThreshold = v;
    if (v != 0.0D) {
      m_best = 0;
    }
  }
  






  public String noiseThresholdTipText()
  {
    return "Maximum proportion of counter-instances of rules. If set to 0, only satisfied rules will be given.";
  }
  






  public double getNoiseThreshold()
  {
    return m_noiseThreshold;
  }
  





  public void setNoiseThreshold(double v)
  {
    m_noiseThreshold = v;
  }
  






  public String repeatLiteralsTipText()
  {
    return "Repeated attributes allowed.";
  }
  





  public boolean getRepeatLiterals()
  {
    return m_repeat;
  }
  





  public void setRepeatLiterals(boolean v)
  {
    m_repeat = v;
  }
  






  public String numberLiteralsTipText()
  {
    return "Maximum number of literals in a rule.";
  }
  





  public int getNumberLiterals()
  {
    return m_numLiterals;
  }
  





  public void setNumberLiterals(int v)
  {
    m_numLiterals = v;
  }
  






  public String negationTipText()
  {
    return "Set the type of negation allowed in the rule. Negation can be allowed in the body, in the head, in both or in none.";
  }
  







  public SelectedTag getNegation()
  {
    return new SelectedTag(m_negation, TAGS_NEGATION);
  }
  





  public void setNegation(SelectedTag v)
  {
    if (v.getTags() == TAGS_NEGATION) {
      m_negation = v.getSelectedTag().getID();
    }
  }
  






  public String classificationTipText()
  {
    return "Find only rules with the class in the head.";
  }
  





  public boolean getClassification()
  {
    return m_classification;
  }
  





  public void setClassification(boolean v)
  {
    m_classification = v;
  }
  






  public String classIndexTipText()
  {
    return "Index of the class attribute. If set to 0, the class will be the last attribute.";
  }
  





  public int getClassIndex()
  {
    return m_classIndex;
  }
  





  public void setClassIndex(int v)
  {
    m_classIndex = v;
  }
  






  public String hornClausesTipText()
  {
    return "Find rules with a single conclusion literal only.";
  }
  





  public boolean getHornClauses()
  {
    return m_horn;
  }
  





  public void setHornClauses(boolean v)
  {
    m_horn = v;
  }
  






  public String equivalentTipText()
  {
    return "Keep equivalent rules. A rule r2 is equivalent to a rule r1 if the body of r2 is the negation of the head of r1, and the head of r2 is the negation of the body of r1.";
  }
  








  public boolean disabled_getEquivalent()
  {
    return !m_equivalent;
  }
  





  public void disabled_setEquivalent(boolean v)
  {
    m_equivalent = (!v);
  }
  






  public String sameClauseTipText()
  {
    return "Keep rules corresponding to the same clauses. If set to false, only the rule with the best confirmation value and rules with a lower number of counter-instances will be kept.";
  }
  








  public boolean disabled_getSameClause()
  {
    return !m_sameClause;
  }
  





  public void disabled_setSameClause(boolean v)
  {
    m_sameClause = (!v);
  }
  






  public String subsumptionTipText()
  {
    return "Keep subsumed rules. If set to false, subsumed rules will only be kept if they have a better confirmation or a lower number of counter-instances.";
  }
  







  public boolean disabled_getSubsumption()
  {
    return !m_subsumption;
  }
  





  public void disabled_setSubsumption(boolean v)
  {
    m_subsumption = (!v);
  }
  






  public String missingValuesTipText()
  {
    return "Set the way to handle missing values. Missing values can be set to match any value, or never match values or to be significant and possibly appear in rules.";
  }
  







  public SelectedTag getMissingValues()
  {
    return new SelectedTag(m_missing, TAGS_MISSING);
  }
  





  public void setMissingValues(SelectedTag v)
  {
    if (v.getTags() == TAGS_MISSING) {
      m_missing = v.getSelectedTag().getID();
    }
  }
  






  public String rocAnalysisTipText()
  {
    return "Return TP-rate and FP-rate for each rule found.";
  }
  





  public boolean getRocAnalysis()
  {
    return m_roc;
  }
  





  public void setRocAnalysis(boolean v)
  {
    m_roc = v;
  }
  






  public String partFileTipText()
  {
    return "Set file containing the parts of the individual for individual-based learning.";
  }
  






  public File disabled_getPartFile()
  {
    return new File(m_partsString);
  }
  





  public void disabled_setPartFile(File v)
    throws Exception
  {
    m_partsString = v.getAbsolutePath();
    if (m_partsString.length() != 0) {
      Reader reader;
      try {
        reader = new BufferedReader(new FileReader(m_partsString));
      } catch (Exception e) {
        throw new Exception("Can't open file " + e.getMessage() + ".");
      }
      m_parts = new Instances(reader);
    }
  }
  






  public String valuesOutputTipText()
  {
    return "Give visual feedback during the search. The current best and worst values can be output either to stdout or to a separate window.";
  }
  






  public SelectedTag getValuesOutput()
  {
    return new SelectedTag(m_printValues, TAGS_VALUES);
  }
  





  public void setValuesOutput(SelectedTag v)
  {
    if (v.getTags() == TAGS_VALUES) {
      m_printValues = v.getSelectedTag().getID();
    }
  }
  















  private Predicate buildPredicate(Instances instances, Attribute attr, boolean isClass)
    throws Exception
  {
    boolean individual = m_parts != null;
    int type = instances == m_parts ? IndividualLiteral.PART_PROPERTY : IndividualLiteral.INDIVIDUAL_PROPERTY;
    


    if (attr.isNumeric()) {
      throw new Exception("Can't handle numeric attributes!");
    }
    
    boolean missingValues = attributeStatsindexmissingCount > 0;
    Predicate predicate;
    Predicate predicate;
    if (individual) {
      predicate = new Predicate(instances.relationName() + "." + attr.name(), attr.index(), isClass);
    }
    else {
      predicate = new Predicate(attr.name(), attr.index(), isClass);
    }
    
    if ((attr.numValues() == 2) && ((!missingValues) || (m_missing == 0)))
    {
      Literal negation;
      Literal lit;
      Literal negation;
      if (individual) {
        Literal lit = new IndividualLiteral(predicate, attr.value(0), 0, 1, m_missing, type);
        
        negation = new IndividualLiteral(predicate, attr.value(1), 1, 1, m_missing, type);
      }
      else {
        lit = new AttributeValueLiteral(predicate, attr.value(0), 0, 1, m_missing);
        
        negation = new AttributeValueLiteral(predicate, attr.value(1), 1, 1, m_missing);
      }
      
      lit.setNegation(negation);
      negation.setNegation(lit);
      predicate.addLiteral(lit);
    }
    else {
      for (int i = 0; i < attr.numValues(); i++) { Literal lit;
        Literal lit; if (individual) {
          lit = new IndividualLiteral(predicate, attr.value(i), i, 1, m_missing, type);
        }
        else {
          lit = new AttributeValueLiteral(predicate, attr.value(i), i, 1, m_missing);
        }
        
        if (m_negation != 0) { Literal negation;
          Literal negation; if (individual) {
            negation = new IndividualLiteral(predicate, attr.value(i), i, 0, m_missing, type);
          }
          else {
            negation = new AttributeValueLiteral(predicate, attr.value(i), i, 0, m_missing);
          }
          
          lit.setNegation(negation);
          negation.setNegation(lit);
        }
        predicate.addLiteral(lit);
      }
      

      if ((missingValues) && (m_missing == 2)) { Literal lit;
        Literal lit; if (individual) {
          lit = new IndividualLiteral(predicate, "?", -1, 1, m_missing, type);
        }
        else {
          lit = new AttributeValueLiteral(predicate, "?", -1, 1, m_missing);
        }
        
        if (m_negation != 0) { Literal negation;
          Literal negation; if (individual) {
            negation = new IndividualLiteral(predicate, "?", -1, 0, m_missing, type);
          }
          else {
            negation = new AttributeValueLiteral(predicate, "?", -1, 0, m_missing);
          }
          
          lit.setNegation(negation);
          negation.setNegation(lit);
        }
        predicate.addLiteral(lit);
      }
    }
    return predicate;
  }
  






  private ArrayList buildPredicates()
    throws Exception
  {
    ArrayList predicates = new ArrayList();
    

    Enumeration attributes = m_instances.enumerateAttributes();
    boolean individual = m_parts != null;
    

    while (attributes.hasMoreElements()) {
      Attribute attr = (Attribute)attributes.nextElement();
      
      if ((!individual) || (!attr.name().equals("id"))) {
        Predicate predicate = buildPredicate(m_instances, attr, false);
        predicates.add(predicate);
      }
    }
    
    Attribute attr = m_instances.classAttribute();
    
    if ((!individual) || (!attr.name().equals("id"))) {
      Predicate predicate = buildPredicate(m_instances, attr, true);
      predicates.add(predicate);
    }
    

    if (individual) {
      attributes = m_parts.enumerateAttributes();
      while (attributes.hasMoreElements()) {
        attr = (Attribute)attributes.nextElement();
        
        if (!attr.name().equals("id")) {
          Predicate predicate = buildPredicate(m_parts, attr, false);
          predicates.add(predicate);
        }
      }
    }
    
    return predicates;
  }
  





  private int numValuesInResult()
  {
    int result = 0;
    SimpleLinkedList.LinkedListIterator iter = m_results.iterator();
    

    if (!iter.hasNext()) {
      return result;
    }
    Rule current = (Rule)iter.next();
    while (iter.hasNext()) {
      Rule next = (Rule)iter.next();
      if (current.getConfirmation() > next.getConfirmation()) {
        result++;
      }
      current = next;
    }
    return result + 1;
  }
  






  private boolean canRefine(Rule rule)
  {
    if (rule.isEmpty()) {
      return true;
    }
    if (m_best != 0) {
      if (numValuesInResult() < m_best) {
        return true;
      }
      Rule worstResult = (Rule)m_results.getLast();
      if (rule.getOptimistic() >= worstResult.getConfirmation()) {
        return true;
      }
      return false;
    }
    return true;
  }
  






  private boolean canCalculateOptimistic(Rule rule)
  {
    if ((rule.hasTrueBody()) || (rule.hasFalseHead())) {
      return false;
    }
    if (!rule.overFrequencyThreshold(m_frequencyThreshold)) {
      return false;
    }
    return true;
  }
  






  private boolean canExplore(Rule rule)
  {
    if (rule.getOptimistic() < m_confirmationThreshold) {
      return false;
    }
    if (m_best != 0) {
      if (numValuesInResult() < m_best) {
        return true;
      }
      Rule worstResult = (Rule)m_results.getLast();
      if (rule.getOptimistic() >= worstResult.getConfirmation()) {
        return true;
      }
      return false;
    }
    return true;
  }
  






  private boolean canStoreInNodes(Rule rule)
  {
    if (rule.getObservedNumber() == 0) {
      return false;
    }
    return true;
  }
  





  private boolean canCalculateConfirmation(Rule rule)
  {
    if (rule.getObservedFrequency() > m_noiseThreshold) {
      return false;
    }
    return true;
  }
  





  private boolean canStoreInResults(Rule rule)
  {
    if (rule.getConfirmation() < m_confirmationThreshold) {
      return false;
    }
    if (m_best != 0) {
      if (numValuesInResult() < m_best) {
        return true;
      }
      Rule worstResult = (Rule)m_results.getLast();
      if (rule.getConfirmation() >= worstResult.getConfirmation()) {
        return true;
      }
      return false;
    }
    return true;
  }
  









  private void addResult(Rule rule)
  {
    boolean added = false;
    

    SimpleLinkedList.LinkedListIterator iter = m_results.iterator();
    while (iter.hasNext()) {
      Rule current = (Rule)iter.next();
      if (Rule.confirmationThenObservedComparator.compare(current, rule) > 0) {
        iter.addBefore(rule);
        added = true;


      }
      else if (((m_subsumption) || (m_sameClause) || (m_equivalent)) && (current.subsumes(rule)))
      {
        if (current.numLiterals() == rule.numLiterals()) {
          if (current.equivalentTo(rule))
          {
            if (!m_equivalent) {
              break;
            }
            
          } else {
            if ((!m_sameClause) || (Rule.confirmationComparator.compare(current, rule) >= 0)) {
              break;
            }
            
          }
          
        }
        else if ((m_subsumption) && (Rule.observedComparator.compare(current, rule) <= 0))
        {
          return;
        }
      }
    }
    

    if (!added)
    {
      m_results.add(rule);
    }
    


    SimpleLinkedList.LinkedListInverseIterator inverse = m_results.inverseIterator();
    
    while (inverse.hasPrevious()) {
      Rule current = (Rule)inverse.previous();
      if (Rule.confirmationThenObservedComparator.compare(current, rule) < 0) {
        break;
      }
      if ((current != rule) && (rule.subsumes(current))) {
        if (current.numLiterals() == rule.numLiterals()) {
          if ((!current.equivalentTo(rule)) && 
          
            (m_sameClause) && (Rule.confirmationComparator.compare(current, rule) > 0))
          {
            inverse.remove();
          }
          

        }
        else if ((m_subsumption) && (Rule.observedComparator.compare(rule, current) <= 0))
        {
          inverse.remove();
        }
      }
    }
    



    if ((m_best != 0) && (numValuesInResult() > m_best)) {
      Rule worstRule = (Rule)m_results.getLast();
      inverse = m_results.inverseIterator();
      while (inverse.hasPrevious()) {
        Rule current = (Rule)inverse.previous();
        if (Rule.confirmationComparator.compare(current, worstRule) < 0) {
          break;
        }
        inverse.remove();
      }
    }
    

    printValues();
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
    Frame valuesFrame = null;
    

    if (m_parts == null) {
      m_instances = new Instances(instances);
    } else {
      m_instances = new IndividualInstances(new Instances(instances), m_parts);
    }
    m_results = new SimpleLinkedList();
    m_hypotheses = 0;
    m_explored = 0;
    m_status = 0;
    
    if (m_classIndex == -1) {
      m_instances.setClassIndex(m_instances.numAttributes() - 1);
    } else if ((m_classIndex < m_instances.numAttributes()) && (m_classIndex >= 0)) {
      m_instances.setClassIndex(m_classIndex);
    } else {
      throw new Exception("Invalid class index.");
    }
    
    getCapabilities().testWithFail(m_instances);
    

    if (m_printValues == 2) {
      m_valuesText = new TextField(37);
      m_valuesText.setEditable(false);
      m_valuesText.setFont(new Font("Monospaced", 0, 12));
      Label valuesLabel = new Label("Best and worst current values:");
      Button stop = new Button("Stop search");
      stop.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          m_status = 2;
        }
      });
      valuesFrame = new Frame("Tertius status");
      valuesFrame.setResizable(false);
      valuesFrame.add(m_valuesText, "Center");
      valuesFrame.add(stop, "South");
      valuesFrame.add(valuesLabel, "North");
      valuesFrame.pack();
      valuesFrame.setVisible(true);
    } else if (m_printValues == 1) {
      System.out.println("Best and worst current values:");
    }
    
    Date start = new Date();
    

    m_predicates = buildPredicates();
    beginSearch();
    
    Date end = new Date();
    
    if (m_printValues == 2) {
      valuesFrame.dispose();
    }
    
    m_time = new Date(end.getTime() - start.getTime());
  }
  

  public void run()
  {
    try
    {
      search();
    }
    catch (OutOfMemoryError e) {
      System.gc();
      m_status = 1;
    }
    endSearch();
  }
  



  private synchronized void beginSearch()
    throws Exception
  {
    Thread search = new Thread(this);
    search.start();
    try
    {
      wait();
    }
    catch (InterruptedException e) {
      m_status = 2;
    }
  }
  




  private synchronized void endSearch()
  {
    notify();
  }
  






  public void search()
  {
    SimpleLinkedList nodes = new SimpleLinkedList();
    



    boolean negBody = (m_negation == 1) || (m_negation == 3);
    boolean negHead = (m_negation == 2) || (m_negation == 3);
    

    nodes.add(new Rule(m_repeat, m_numLiterals, negBody, negHead, m_classification, m_horn));
    


    printValues();
    

    while ((m_status != 2) && (!nodes.isEmpty())) {
      Rule currentNode = (Rule)nodes.removeFirst();
      if (!canRefine(currentNode)) break;
      SimpleLinkedList children = currentNode.refine(m_predicates);
      SimpleLinkedList.LinkedListIterator iter = children.iterator();
      

      while (iter.hasNext()) {
        m_hypotheses += 1;
        Rule child = (Rule)iter.next();
        child.upDate(m_instances);
        if (canCalculateOptimistic(child)) {
          child.calculateOptimistic();
          if (canExplore(child)) {
            m_explored += 1;
            if (!canStoreInNodes(child))
            {
              iter.remove();
            }
            if (canCalculateConfirmation(child)) {
              child.calculateConfirmation();
              if (canStoreInResults(child)) {
                addResult(child);
              }
            }
          } else {
            iter.remove();
          }
        } else {
          iter.remove();
        }
      }
      

      children.sort(Rule.optimisticThenObservedComparator);
      nodes.merge(children, Rule.optimisticThenObservedComparator);
    }
  }
  









  public SimpleLinkedList getResults()
  {
    return m_results;
  }
  



  private void printValues()
  {
    if (m_printValues == 0) {
      return;
    }
    if (m_results.isEmpty()) {
      if (m_printValues == 1) {
        System.out.print("0.000000 0.000000 - 0.000000 0.000000");
      } else {
        m_valuesText.setText("0.000000 0.000000 - 0.000000 0.000000");
      }
    } else {
      Rule best = (Rule)m_results.getFirst();
      Rule worst = (Rule)m_results.getLast();
      String values = best.valuesToString() + " - " + worst.valuesToString();
      if (m_printValues == 1) {
        System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
        
        System.out.print(values);
      } else {
        m_valuesText.setText(values);
      }
    }
  }
  







  public String toString()
  {
    StringBuffer text = new StringBuffer();
    SimpleLinkedList.LinkedListIterator iter = m_results.iterator();
    int size = m_results.size();
    int i = 0;
    
    text.append("\nTertius\n=======\n\n");
    
    while (iter.hasNext()) {
      Rule current = (Rule)iter.next();
      text.append(Utils.doubleToString(i + 1.0D, (int)(Math.log(size) / Math.log(10.0D) + 1.0D), 0) + ". ");
      



      text.append("/* ");
      if (m_roc) {
        text.append(current.rocToString());
      } else {
        text.append(current.valuesToString());
      }
      text.append(" */ ");
      text.append(current.toString());
      text.append("\n");
      i++;
    }
    
    text.append("\nNumber of hypotheses considered: " + m_hypotheses);
    text.append("\nNumber of hypotheses explored: " + m_explored);
    
    if (m_status == 1) {
      text.append("\n\nNot enough memory to continue the search");
    } else if (m_status == 2) {
      text.append("\n\nSearch interrupted");
    }
    
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6368 $");
  }
  




  public static void main(String[] args)
  {
    runAssociator(new Tertius(), args);
  }
}

package weka.classifiers.bayes;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.net.ADNode;
import weka.classifiers.bayes.net.BIFReader;
import weka.classifiers.bayes.net.ParentSet;
import weka.classifiers.bayes.net.estimate.BayesNetEstimator;
import weka.classifiers.bayes.net.estimate.DiscreteEstimatorBayes;
import weka.classifiers.bayes.net.estimate.SimpleEstimator;
import weka.classifiers.bayes.net.search.SearchAlgorithm;
import weka.classifiers.bayes.net.search.local.K2;
import weka.classifiers.bayes.net.search.local.LocalScoreSearchAlgorithm;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.estimators.Estimator;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;








































































public class BayesNet
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, Drawable, AdditionalMeasureProducer
{
  static final long serialVersionUID = 746037443258775954L;
  protected ParentSet[] m_ParentSets;
  public Estimator[][] m_Distributions;
  protected Discretize m_DiscretizeFilter = null;
  

  int m_nNonDiscreteAttribute = -1;
  

  protected ReplaceMissingValues m_MissingValuesFilter = null;
  




  protected int m_NumClasses;
  




  public Instances m_Instances;
  



  ADNode m_ADTree;
  



  protected BIFReader m_otherBayesNet = null;
  



  boolean m_bUseADTree = false;
  



  SearchAlgorithm m_SearchAlgorithm = new K2();
  



  BayesNetEstimator m_BayesNetEstimator = new SimpleEstimator();
  

  public BayesNet() {}
  

  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  







  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    

    instances = normalizeDataSet(instances);
    

    m_Instances = new Instances(instances);
    

    m_NumClasses = instances.numClasses();
    

    if (m_bUseADTree) {
      m_ADTree = ADNode.makeADTree(instances);
    }
    


    initStructure();
    

    buildStructure();
    

    estimateCPTs();
    


    m_ADTree = null;
  }
  



  protected Instances normalizeDataSet(Instances instances)
    throws Exception
  {
    m_DiscretizeFilter = null;
    m_MissingValuesFilter = null;
    
    boolean bHasNonNominal = false;
    boolean bHasMissingValues = false;
    
    Enumeration enu = instances.enumerateAttributes();
    while (enu.hasMoreElements()) {
      Attribute attribute = (Attribute)enu.nextElement();
      if (attribute.type() != 1) {
        m_nNonDiscreteAttribute = attribute.index();
        bHasNonNominal = true;
      }
      
      Enumeration enum2 = instances.enumerateInstances();
      while (enum2.hasMoreElements()) {
        if (((Instance)enum2.nextElement()).isMissing(attribute)) {
          bHasMissingValues = true;
        }
      }
    }
    

    if (bHasNonNominal) {
      System.err.println("Warning: discretizing data set");
      m_DiscretizeFilter = new Discretize();
      m_DiscretizeFilter.setInputFormat(instances);
      instances = Filter.useFilter(instances, m_DiscretizeFilter);
    }
    
    if (bHasMissingValues) {
      System.err.println("Warning: filling in missing values in data set");
      m_MissingValuesFilter = new ReplaceMissingValues();
      m_MissingValuesFilter.setInputFormat(instances);
      instances = Filter.useFilter(instances, m_MissingValuesFilter);
    }
    return instances;
  }
  



  protected Instance normalizeInstance(Instance instance)
    throws Exception
  {
    if ((m_DiscretizeFilter != null) && (instance.attribute(m_nNonDiscreteAttribute).type() != 1))
    {
      m_DiscretizeFilter.input(instance);
      instance = m_DiscretizeFilter.output();
    }
    if (m_MissingValuesFilter != null) {
      m_MissingValuesFilter.input(instance);
      instance = m_MissingValuesFilter.output();
    }
    else
    {
      for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
        if ((iAttribute != instance.classIndex()) && (instance.isMissing(iAttribute))) {
          System.err.println("Warning: Found missing value in test set, filling in values.");
          m_MissingValuesFilter = new ReplaceMissingValues();
          m_MissingValuesFilter.setInputFormat(m_Instances);
          Filter.useFilter(m_Instances, m_MissingValuesFilter);
          m_MissingValuesFilter.input(instance);
          instance = m_MissingValuesFilter.output();
          iAttribute = m_Instances.numAttributes();
        }
      }
    }
    return instance;
  }
  









  public void initStructure()
    throws Exception
  {
    int nAttribute = 0;
    
    for (int iOrder = 1; iOrder < m_Instances.numAttributes(); iOrder++) {
      if (nAttribute == m_Instances.classIndex()) {
        nAttribute++;
      }
    }
    



    m_ParentSets = new ParentSet[m_Instances.numAttributes()];
    
    for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
      m_ParentSets[iAttribute] = new ParentSet(m_Instances.numAttributes());
    }
  }
  







  public void buildStructure()
    throws Exception
  {
    m_SearchAlgorithm.buildStructure(this, m_Instances);
  }
  




  public void estimateCPTs()
    throws Exception
  {
    m_BayesNetEstimator.estimateCPTs(this);
  }
  



  public void initCPTs()
    throws Exception
  {
    m_BayesNetEstimator.initCPTs(this);
  }
  





  public void updateClassifier(Instance instance)
    throws Exception
  {
    instance = normalizeInstance(instance);
    m_BayesNetEstimator.updateClassifier(this, instance);
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    instance = normalizeInstance(instance);
    return m_BayesNetEstimator.distributionForInstance(this, instance);
  }
  






  public double[] countsForInstance(Instance instance)
    throws Exception
  {
    double[] fCounts = new double[m_NumClasses];
    
    for (int iClass = 0; iClass < m_NumClasses; iClass++) {
      fCounts[iClass] = 0.0D;
    }
    
    for (int iClass = 0; iClass < m_NumClasses; iClass++) {
      double fCount = 0.0D;
      
      for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
        double iCPT = 0.0D;
        
        for (int iParent = 0; iParent < m_ParentSets[iAttribute].getNrOfParents(); iParent++) {
          int nParent = m_ParentSets[iAttribute].getParent(iParent);
          
          if (nParent == m_Instances.classIndex()) {
            iCPT = iCPT * m_NumClasses + iClass;
          } else {
            iCPT = iCPT * m_Instances.attribute(nParent).numValues() + instance.value(nParent);
          }
        }
        
        if (iAttribute == m_Instances.classIndex()) {
          fCount += ((DiscreteEstimatorBayes)m_Distributions[iAttribute][((int)iCPT)]).getCount(iClass);
        } else {
          fCount += ((DiscreteEstimatorBayes)m_Distributions[iAttribute][((int)iCPT)]).getCount(instance.value(iAttribute));
        }
      }
      


      fCounts[iClass] += fCount;
    }
    return fCounts;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tDo not use ADTree data structure\n", "D", 0, "-D"));
    newVector.addElement(new Option("\tBIF file to compare with\n", "B", 1, "-B <BIF file>"));
    newVector.addElement(new Option("\tSearch algorithm\n", "Q", 1, "-Q weka.classifiers.bayes.net.search.SearchAlgorithm"));
    newVector.addElement(new Option("\tEstimator algorithm\n", "E", 1, "-E weka.classifiers.bayes.net.estimate.SimpleEstimator"));
    
    return newVector.elements();
  }
  

























  public void setOptions(String[] options)
    throws Exception
  {
    m_bUseADTree = (!Utils.getFlag('D', options));
    
    String sBIFFile = Utils.getOption('B', options);
    if ((sBIFFile != null) && (!sBIFFile.equals(""))) {
      setBIFFile(sBIFFile);
    }
    
    String searchAlgorithmName = Utils.getOption('Q', options);
    if (searchAlgorithmName.length() != 0) {
      setSearchAlgorithm((SearchAlgorithm)Utils.forName(SearchAlgorithm.class, searchAlgorithmName, partitionOptions(options)));


    }
    else
    {

      setSearchAlgorithm(new K2());
    }
    

    String estimatorName = Utils.getOption('E', options);
    if (estimatorName.length() != 0) {
      setEstimator((BayesNetEstimator)Utils.forName(BayesNetEstimator.class, estimatorName, Utils.partitionOptions(options)));


    }
    else
    {

      setEstimator(new SimpleEstimator());
    }
    
    Utils.checkForRemainingOptions(options);
  }
  









  public static String[] partitionOptions(String[] options)
  {
    for (int i = 0; i < options.length; i++) {
      if (options[i].equals("--"))
      {
        int j = i;
        while ((j < options.length) && (!options[j].equals("-E"))) {
          j++;
        }
        


        options[(i++)] = "";
        String[] result = new String[options.length - i];
        j = i;
        while ((j < options.length) && (!options[j].equals("-E"))) {
          result[(j - i)] = options[j];
          options[j] = "";
          j++;
        }
        while (j < options.length) {
          result[(j - i)] = "";
          j++;
        }
        return result;
      }
    }
    return new String[0];
  }
  





  public String[] getOptions()
  {
    String[] searchOptions = m_SearchAlgorithm.getOptions();
    String[] estimatorOptions = m_BayesNetEstimator.getOptions();
    String[] options = new String[11 + searchOptions.length + estimatorOptions.length];
    int current = 0;
    
    if (!m_bUseADTree) {
      options[(current++)] = "-D";
    }
    
    if (m_otherBayesNet != null) {
      options[(current++)] = "-B";
      options[(current++)] = m_otherBayesNet.getFileName();
    }
    
    options[(current++)] = "-Q";
    options[(current++)] = ("" + getSearchAlgorithm().getClass().getName());
    options[(current++)] = "--";
    for (int iOption = 0; iOption < searchOptions.length; iOption++) {
      options[(current++)] = searchOptions[iOption];
    }
    
    options[(current++)] = "-E";
    options[(current++)] = ("" + getEstimator().getClass().getName());
    options[(current++)] = "--";
    for (int iOption = 0; iOption < estimatorOptions.length; iOption++) {
      options[(current++)] = estimatorOptions[iOption];
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  



  public void setSearchAlgorithm(SearchAlgorithm newSearchAlgorithm)
  {
    m_SearchAlgorithm = newSearchAlgorithm;
  }
  



  public SearchAlgorithm getSearchAlgorithm()
  {
    return m_SearchAlgorithm;
  }
  



  public void setEstimator(BayesNetEstimator newBayesNetEstimator)
  {
    m_BayesNetEstimator = newBayesNetEstimator;
  }
  



  public BayesNetEstimator getEstimator()
  {
    return m_BayesNetEstimator;
  }
  



  public void setUseADTree(boolean bUseADTree)
  {
    m_bUseADTree = bUseADTree;
  }
  



  public boolean getUseADTree()
  {
    return m_bUseADTree;
  }
  


  public void setBIFFile(String sBIFFile)
  {
    try
    {
      m_otherBayesNet = new BIFReader().processFile(sBIFFile);
    } catch (Throwable t) {
      m_otherBayesNet = null;
    }
  }
  



  public String getBIFFile()
  {
    if (m_otherBayesNet != null) {
      return m_otherBayesNet.getFileName();
    }
    return "";
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append("Bayes Network Classifier");
    text.append("\n" + (m_bUseADTree ? "Using " : "not using ") + "ADTree");
    
    if (m_Instances == null) {
      text.append(": No model built yet.");
    }
    else
    {
      text.append("\n#attributes=");
      text.append(m_Instances.numAttributes());
      text.append(" #classindex=");
      text.append(m_Instances.classIndex());
      text.append("\nNetwork structure (nodes followed by parents)\n");
      
      for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
        text.append(m_Instances.attribute(iAttribute).name() + "(" + m_Instances.attribute(iAttribute).numValues() + "): ");
        




        for (int iParent = 0; iParent < m_ParentSets[iAttribute].getNrOfParents(); iParent++) {
          text.append(m_Instances.attribute(m_ParentSets[iAttribute].getParent(iParent)).name() + " ");
        }
        
        text.append("\n");
      }
      






      text.append("LogScore Bayes: " + measureBayesScore() + "\n");
      text.append("LogScore BDeu: " + measureBDeuScore() + "\n");
      text.append("LogScore MDL: " + measureMDLScore() + "\n");
      text.append("LogScore ENTROPY: " + measureEntropyScore() + "\n");
      text.append("LogScore AIC: " + measureAICScore() + "\n");
      
      if (m_otherBayesNet != null) {
        text.append("Missing: " + m_otherBayesNet.missingArcs(this) + " Extra: " + m_otherBayesNet.extraArcs(this) + " Reversed: " + m_otherBayesNet.reversedArcs(this) + "\n");
        






        text.append("Divergence: " + m_otherBayesNet.divergence(this) + "\n");
      }
    }
    
    return text.toString();
  }
  





  public int graphType()
  {
    return 2;
  }
  



  public String graph()
    throws Exception
  {
    return toXMLBIF03();
  }
  
  public String getBIFHeader() {
    StringBuffer text = new StringBuffer();
    text.append("<?xml version=\"1.0\"?>\n");
    text.append("<!-- DTD for the XMLBIF 0.3 format -->\n");
    text.append("<!DOCTYPE BIF [\n");
    text.append("\t<!ELEMENT BIF ( NETWORK )*>\n");
    text.append("\t      <!ATTLIST BIF VERSION CDATA #REQUIRED>\n");
    text.append("\t<!ELEMENT NETWORK ( NAME, ( PROPERTY | VARIABLE | DEFINITION )* )>\n");
    text.append("\t<!ELEMENT NAME (#PCDATA)>\n");
    text.append("\t<!ELEMENT VARIABLE ( NAME, ( OUTCOME |  PROPERTY )* ) >\n");
    text.append("\t      <!ATTLIST VARIABLE TYPE (nature|decision|utility) \"nature\">\n");
    text.append("\t<!ELEMENT OUTCOME (#PCDATA)>\n");
    text.append("\t<!ELEMENT DEFINITION ( FOR | GIVEN | TABLE | PROPERTY )* >\n");
    text.append("\t<!ELEMENT FOR (#PCDATA)>\n");
    text.append("\t<!ELEMENT GIVEN (#PCDATA)>\n");
    text.append("\t<!ELEMENT TABLE (#PCDATA)>\n");
    text.append("\t<!ELEMENT PROPERTY (#PCDATA)>\n");
    text.append("]>\n");
    return text.toString();
  }
  





  public String toXMLBIF03()
  {
    if (m_Instances == null) {
      return "<!--No model built yet-->";
    }
    
    StringBuffer text = new StringBuffer();
    text.append(getBIFHeader());
    text.append("\n");
    text.append("\n");
    text.append("<BIF VERSION=\"0.3\">\n");
    text.append("<NETWORK>\n");
    text.append("<NAME>" + XMLNormalize(m_Instances.relationName()) + "</NAME>\n");
    for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
      text.append("<VARIABLE TYPE=\"nature\">\n");
      text.append("<NAME>" + XMLNormalize(m_Instances.attribute(iAttribute).name()) + "</NAME>\n");
      for (int iValue = 0; iValue < m_Instances.attribute(iAttribute).numValues(); iValue++) {
        text.append("<OUTCOME>" + XMLNormalize(m_Instances.attribute(iAttribute).value(iValue)) + "</OUTCOME>\n");
      }
      text.append("</VARIABLE>\n");
    }
    
    for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
      text.append("<DEFINITION>\n");
      text.append("<FOR>" + XMLNormalize(m_Instances.attribute(iAttribute).name()) + "</FOR>\n");
      for (int iParent = 0; iParent < m_ParentSets[iAttribute].getNrOfParents(); iParent++) {
        text.append("<GIVEN>" + XMLNormalize(m_Instances.attribute(m_ParentSets[iAttribute].getParent(iParent)).name()) + "</GIVEN>\n");
      }
      

      text.append("<TABLE>\n");
      for (int iParent = 0; iParent < m_ParentSets[iAttribute].getCardinalityOfParents(); iParent++) {
        for (int iValue = 0; iValue < m_Instances.attribute(iAttribute).numValues(); iValue++) {
          text.append(m_Distributions[iAttribute][iParent].getProbability(iValue));
          text.append(' ');
        }
        text.append('\n');
      }
      text.append("</TABLE>\n");
      text.append("</DEFINITION>\n");
    }
    text.append("</NETWORK>\n");
    text.append("</BIF>\n");
    return text.toString();
  }
  





  protected String XMLNormalize(String sStr)
  {
    StringBuffer sStr2 = new StringBuffer();
    for (int iStr = 0; iStr < sStr.length(); iStr++) {
      char c = sStr.charAt(iStr);
      switch (c) {
      case '&':  sStr2.append("&amp;"); break;
      case '\'':  sStr2.append("&apos;"); break;
      case '"':  sStr2.append("&quot;"); break;
      case '<':  sStr2.append("&lt;"); break;
      case '>':  sStr2.append("&gt;"); break;
      default: 
        sStr2.append(c);
      }
    }
    return sStr2.toString();
  }
  



  public String useADTreeTipText()
  {
    return "When ADTree (the data structure for increasing speed on counts, not to be confused with the classifier under the same name) is used learning time goes down typically. However, because ADTrees are memory intensive, memory problems may occur. Switching this option off makes the structure learning algorithms slower, and run with less memory. By default, ADTrees are used.";
  }
  







  public String searchAlgorithmTipText()
  {
    return "Select method used for searching network structures.";
  }
  



  public String estimatorTipText()
  {
    return "Select Estimator algorithm for finding the conditional probability tables of the Bayes Network.";
  }
  



  public String BIFFileTipText()
  {
    return "Set the name of a file in BIF XML format. A Bayes network learned from data can be compared with the Bayes network represented by the BIF file. Statistics calculated are o.a. the number of missing and extra arcs.";
  }
  





  public String globalInfo()
  {
    return "Bayes Network learning using various search algorithms and quality measures.\nBase class for a Bayes Network classifier. Provides datastructures (network structure, conditional probability distributions, etc.) and facilities common to Bayes Network learning algorithms like K2 and B.\n\nFor more information see:\n\nhttp://www.cs.waikato.ac.nz/~remco/weka.pdf";
  }
  












  public static void main(String[] argv)
  {
    runClassifier(new BayesNet(), argv);
  }
  


  public String getName()
  {
    return m_Instances.relationName();
  }
  


  public int getNrOfNodes()
  {
    return m_Instances.numAttributes();
  }
  



  public String getNodeName(int iNode)
  {
    return m_Instances.attribute(iNode).name();
  }
  



  public int getCardinality(int iNode)
  {
    return m_Instances.attribute(iNode).numValues();
  }
  




  public String getNodeValue(int iNode, int iValue)
  {
    return m_Instances.attribute(iNode).value(iValue);
  }
  



  public int getNrOfParents(int iNode)
  {
    return m_ParentSets[iNode].getNrOfParents();
  }
  




  public int getParent(int iNode, int iParent)
  {
    return m_ParentSets[iNode].getParent(iParent);
  }
  


  public ParentSet[] getParentSets()
  {
    return m_ParentSets;
  }
  


  public Estimator[][] getDistributions()
  {
    return m_Distributions;
  }
  



  public int getParentCardinality(int iNode)
  {
    return m_ParentSets[iNode].getCardinalityOfParents();
  }
  






  public double getProbability(int iNode, int iParent, int iValue)
  {
    return m_Distributions[iNode][iParent].getProbability(iValue);
  }
  



  public ParentSet getParentSet(int iNode)
  {
    return m_ParentSets[iNode];
  }
  

  public ADNode getADTree()
  {
    return m_ADTree;
  }
  





  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(4);
    newVector.addElement("measureExtraArcs");
    newVector.addElement("measureMissingArcs");
    newVector.addElement("measureReversedArcs");
    newVector.addElement("measureDivergence");
    newVector.addElement("measureBayesScore");
    newVector.addElement("measureBDeuScore");
    newVector.addElement("measureMDLScore");
    newVector.addElement("measureAICScore");
    newVector.addElement("measureEntropyScore");
    return newVector.elements();
  }
  
  public double measureExtraArcs() {
    if (m_otherBayesNet != null) {
      return m_otherBayesNet.extraArcs(this);
    }
    return 0.0D;
  }
  
  public double measureMissingArcs() {
    if (m_otherBayesNet != null) {
      return m_otherBayesNet.missingArcs(this);
    }
    return 0.0D;
  }
  
  public double measureReversedArcs() {
    if (m_otherBayesNet != null) {
      return m_otherBayesNet.reversedArcs(this);
    }
    return 0.0D;
  }
  
  public double measureDivergence() {
    if (m_otherBayesNet != null) {
      return m_otherBayesNet.divergence(this);
    }
    return 0.0D;
  }
  
  public double measureBayesScore() {
    LocalScoreSearchAlgorithm s = new LocalScoreSearchAlgorithm(this, m_Instances);
    return s.logScore(0);
  }
  
  public double measureBDeuScore() {
    LocalScoreSearchAlgorithm s = new LocalScoreSearchAlgorithm(this, m_Instances);
    return s.logScore(1);
  }
  
  public double measureMDLScore() {
    LocalScoreSearchAlgorithm s = new LocalScoreSearchAlgorithm(this, m_Instances);
    return s.logScore(2);
  }
  
  public double measureAICScore() {
    LocalScoreSearchAlgorithm s = new LocalScoreSearchAlgorithm(this, m_Instances);
    return s.logScore(4);
  }
  
  public double measureEntropyScore() {
    LocalScoreSearchAlgorithm s = new LocalScoreSearchAlgorithm(this, m_Instances);
    return s.logScore(3);
  }
  





  public double getMeasure(String measureName)
  {
    if (measureName.equals("measureExtraArcs")) {
      return measureExtraArcs();
    }
    if (measureName.equals("measureMissingArcs")) {
      return measureMissingArcs();
    }
    if (measureName.equals("measureReversedArcs")) {
      return measureReversedArcs();
    }
    if (measureName.equals("measureDivergence")) {
      return measureDivergence();
    }
    if (measureName.equals("measureBayesScore")) {
      return measureBayesScore();
    }
    if (measureName.equals("measureBDeuScore")) {
      return measureBDeuScore();
    }
    if (measureName.equals("measureMDLScore")) {
      return measureMDLScore();
    }
    if (measureName.equals("measureAICScore")) {
      return measureAICScore();
    }
    if (measureName.equals("measureEntropyScore")) {
      return measureEntropyScore();
    }
    return 0.0D;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5725 $");
  }
}

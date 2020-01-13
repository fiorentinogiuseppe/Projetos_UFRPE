package weka.classifiers.meta;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.trees.J48;
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































































































public class AttributeSelectedClassifier
  extends SingleClassifierEnhancer
  implements OptionHandler, Drawable, AdditionalMeasureProducer, WeightedInstancesHandler
{
  static final long serialVersionUID = -5951805453487947577L;
  protected AttributeSelection m_AttributeSelection = null;
  

  protected ASEvaluation m_Evaluator = new CfsSubsetEval();
  


  protected ASSearch m_Search = new BestFirst();
  


  protected Instances m_ReducedHeader;
  


  protected int m_numClasses;
  


  protected double m_numAttributesSelected;
  


  protected double m_selectionTime;
  

  protected double m_totalTime;
  


  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.J48";
  }
  


  public AttributeSelectedClassifier()
  {
    m_Classifier = new J48();
  }
  




  public String globalInfo()
  {
    return "Dimensionality of training and test data is reduced by attribute selection before being passed on to a classifier.";
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tFull class name of attribute evaluator, followed\n\tby its options.\n\teg: \"weka.attributeSelection.CfsSubsetEval -L\"\n\t(default weka.attributeSelection.CfsSubsetEval)", "E", 1, "-E <attribute evaluator specification>"));
    





    newVector.addElement(new Option("\tFull class name of search method, followed\n\tby its options.\n\teg: \"weka.attributeSelection.BestFirst -D 1\"\n\t(default weka.attributeSelection.BestFirst)", "S", 1, "-S <search method specification>"));
    





    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  





































































  public void setOptions(String[] options)
    throws Exception
  {
    String evaluatorString = Utils.getOption('E', options);
    if (evaluatorString.length() == 0)
      evaluatorString = CfsSubsetEval.class.getName();
    String[] evaluatorSpec = Utils.splitOptions(evaluatorString);
    if (evaluatorSpec.length == 0) {
      throw new Exception("Invalid attribute evaluator specification string");
    }
    String evaluatorName = evaluatorSpec[0];
    evaluatorSpec[0] = "";
    setEvaluator(ASEvaluation.forName(evaluatorName, evaluatorSpec));
    

    String searchString = Utils.getOption('S', options);
    if (searchString.length() == 0)
      searchString = BestFirst.class.getName();
    String[] searchSpec = Utils.splitOptions(searchString);
    if (searchSpec.length == 0) {
      throw new Exception("Invalid search specification string");
    }
    String searchName = searchSpec[0];
    searchSpec[0] = "";
    setSearch(ASSearch.forName(searchName, searchSpec));
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 4];
    
    int current = 0;
    

    options[(current++)] = "-E";
    options[(current++)] = ("" + getEvaluatorSpec());
    

    options[(current++)] = "-S";
    options[(current++)] = ("" + getSearchSpec());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    return options;
  }
  




  public String evaluatorTipText()
  {
    return "Set the attribute evaluator to use. This evaluator is used during the attribute selection phase before the classifier is invoked.";
  }
  






  public void setEvaluator(ASEvaluation evaluator)
  {
    m_Evaluator = evaluator;
  }
  




  public ASEvaluation getEvaluator()
  {
    return m_Evaluator;
  }
  






  protected String getEvaluatorSpec()
  {
    ASEvaluation e = getEvaluator();
    if ((e instanceof OptionHandler)) {
      return e.getClass().getName() + " " + Utils.joinOptions(((OptionHandler)e).getOptions());
    }
    
    return e.getClass().getName();
  }
  




  public String searchTipText()
  {
    return "Set the search method. This search method is used during the attribute selection phase before the classifier is invoked.";
  }
  






  public void setSearch(ASSearch search)
  {
    m_Search = search;
  }
  




  public ASSearch getSearch()
  {
    return m_Search;
  }
  






  protected String getSearchSpec()
  {
    ASSearch s = getSearch();
    if ((s instanceof OptionHandler)) {
      return s.getClass().getName() + " " + Utils.joinOptions(((OptionHandler)s).getOptions());
    }
    
    return s.getClass().getName();
  }
  


  public Capabilities getCapabilities()
  {
    Capabilities result;
    
    Capabilities result;
    
    if (getEvaluator() == null) {
      result = super.getCapabilities();
    } else {
      result = getEvaluator().getCapabilities();
    }
    
    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    return result;
  }
  




  public void buildClassifier(Instances data)
    throws Exception
  {
    if (m_Classifier == null) {
      throw new Exception("No base classifier has been set!");
    }
    
    if (m_Evaluator == null) {
      throw new Exception("No attribute evaluator has been set!");
    }
    
    if (m_Search == null) {
      throw new Exception("No search method has been set!");
    }
    

    getCapabilities().testWithFail(data);
    

    Instances newData = new Instances(data);
    newData.deleteWithMissingClass();
    
    if (newData.numInstances() == 0) {
      m_Classifier.buildClassifier(newData);
      return;
    }
    if (newData.classAttribute().isNominal()) {
      m_numClasses = newData.classAttribute().numValues();
    } else {
      m_numClasses = 1;
    }
    
    Instances resampledData = null;
    
    double weight = newData.instance(0).weight();
    boolean ok = false;
    for (int i = 1; i < newData.numInstances(); i++) {
      if (newData.instance(i).weight() != weight) {
        ok = true;
        break;
      }
    }
    
    if (ok) {
      if ((!(m_Evaluator instanceof WeightedInstancesHandler)) || (!(m_Classifier instanceof WeightedInstancesHandler)))
      {
        Random r = new Random(1L);
        for (int i = 0; i < 10; i++) {
          r.nextDouble();
        }
        resampledData = newData.resampleWithWeights(r);
      }
    }
    else {
      resampledData = newData;
    }
    
    m_AttributeSelection = new AttributeSelection();
    m_AttributeSelection.setEvaluator(m_Evaluator);
    m_AttributeSelection.setSearch(m_Search);
    long start = System.currentTimeMillis();
    m_AttributeSelection.SelectAttributes((m_Evaluator instanceof WeightedInstancesHandler) ? newData : resampledData);
    


    long end = System.currentTimeMillis();
    if ((m_Classifier instanceof WeightedInstancesHandler)) {
      newData = m_AttributeSelection.reduceDimensionality(newData);
      m_Classifier.buildClassifier(newData);
    } else {
      resampledData = m_AttributeSelection.reduceDimensionality(resampledData);
      m_Classifier.buildClassifier(resampledData);
    }
    
    long end2 = System.currentTimeMillis();
    m_numAttributesSelected = m_AttributeSelection.numberAttributesSelected();
    m_ReducedHeader = new Instances((m_Classifier instanceof WeightedInstancesHandler) ? newData : resampledData, 0);
    


    m_selectionTime = (end - start);
    m_totalTime = (end2 - start);
  }
  



  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    Instance newInstance;
    

    Instance newInstance;
    

    if (m_AttributeSelection == null)
    {
      newInstance = instance;
    } else {
      newInstance = m_AttributeSelection.reduceDimensionality(instance);
    }
    
    return m_Classifier.distributionForInstance(newInstance);
  }
  






  public int graphType()
  {
    if ((m_Classifier instanceof Drawable)) {
      return ((Drawable)m_Classifier).graphType();
    }
    return 0;
  }
  





  public String graph()
    throws Exception
  {
    if ((m_Classifier instanceof Drawable))
      return ((Drawable)m_Classifier).graph();
    throw new Exception("Classifier: " + getClassifierSpec() + " cannot be graphed");
  }
  





  public String toString()
  {
    if (m_AttributeSelection == null) {
      return "AttributeSelectedClassifier: No attribute selection possible.\n\n" + m_Classifier.toString();
    }
    

    StringBuffer result = new StringBuffer();
    result.append("AttributeSelectedClassifier:\n\n");
    result.append(m_AttributeSelection.toResultsString());
    result.append("\n\nHeader of reduced data:\n" + m_ReducedHeader.toString());
    result.append("\n\nClassifier Model\n" + m_Classifier.toString());
    
    return result.toString();
  }
  



  public double measureNumAttributesSelected()
  {
    return m_numAttributesSelected;
  }
  



  public double measureSelectionTime()
  {
    return m_selectionTime;
  }
  




  public double measureTime()
  {
    return m_totalTime;
  }
  



  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(3);
    newVector.addElement("measureNumAttributesSelected");
    newVector.addElement("measureSelectionTime");
    newVector.addElement("measureTime");
    if ((m_Classifier instanceof AdditionalMeasureProducer)) {
      Enumeration en = ((AdditionalMeasureProducer)m_Classifier).enumerateMeasures();
      
      while (en.hasMoreElements()) {
        String mname = (String)en.nextElement();
        newVector.addElement(mname);
      }
    }
    return newVector.elements();
  }
  





  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureNumAttributesSelected") == 0)
      return measureNumAttributesSelected();
    if (additionalMeasureName.compareToIgnoreCase("measureSelectionTime") == 0)
      return measureSelectionTime();
    if (additionalMeasureName.compareToIgnoreCase("measureTime") == 0)
      return measureTime();
    if ((m_Classifier instanceof AdditionalMeasureProducer)) {
      return ((AdditionalMeasureProducer)m_Classifier).getMeasure(additionalMeasureName);
    }
    
    throw new IllegalArgumentException(additionalMeasureName + " not supported (AttributeSelectedClassifier)");
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.26 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new AttributeSelectedClassifier(), argv);
  }
}

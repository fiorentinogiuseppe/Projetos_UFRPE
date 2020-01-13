package weka.filters.supervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.AttributeTransformer;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;






























































































public class AttributeSelection
  extends Filter
  implements SupervisedFilter, OptionHandler
{
  static final long serialVersionUID = -296211247688169716L;
  private weka.attributeSelection.AttributeSelection m_trainSelector;
  private ASEvaluation m_ASEvaluator;
  private ASSearch m_ASSearch;
  private String[] m_FilterOptions;
  private int[] m_SelectedAttributes;
  protected boolean m_hasClass;
  
  public String globalInfo()
  {
    return "A supervised attribute filter that can be used to select attributes. It is very flexible and allows various search and evaluation methods to be combined.";
  }
  





  public AttributeSelection()
  {
    resetOptions();
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(6);
    
    newVector.addElement(new Option("\tSets search method for subset evaluators.\n\teg. -S \"weka.attributeSelection.BestFirst -S 8\"", "S", 1, "-S <\"Name of search class [search options]\">"));
    




    newVector.addElement(new Option("\tSets attribute/subset evaluator.\n\teg. -E \"weka.attributeSelection.CfsSubsetEval -L\"", "E", 1, "-E <\"Name of attribute/subset evaluation class [evaluator options]\">"));
    




    if ((m_ASEvaluator != null) && ((m_ASEvaluator instanceof OptionHandler))) {
      Enumeration enu = ((OptionHandler)m_ASEvaluator).listOptions();
      
      newVector.addElement(new Option("", "", 0, "\nOptions specific to evaluator " + m_ASEvaluator.getClass().getName() + ":"));
      
      while (enu.hasMoreElements()) {
        newVector.addElement((Option)enu.nextElement());
      }
    }
    
    if ((m_ASSearch != null) && ((m_ASSearch instanceof OptionHandler))) {
      Enumeration enu = ((OptionHandler)m_ASSearch).listOptions();
      
      newVector.addElement(new Option("", "", 0, "\nOptions specific to search " + m_ASSearch.getClass().getName() + ":"));
      
      while (enu.hasMoreElements()) {
        newVector.addElement((Option)enu.nextElement());
      }
    }
    return newVector.elements();
  }
  

















































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    if (Utils.getFlag('X', options)) {
      throw new Exception("Cross validation is not a valid option when using attribute selection as a Filter.");
    }
    

    String optionString = Utils.getOption('E', options);
    if (optionString.length() != 0) {
      optionString = optionString.trim();
      
      int breakLoc = optionString.indexOf(' ');
      String evalClassName = optionString;
      String evalOptionsString = "";
      String[] evalOptions = null;
      if (breakLoc != -1) {
        evalClassName = optionString.substring(0, breakLoc);
        evalOptionsString = optionString.substring(breakLoc).trim();
        evalOptions = Utils.splitOptions(evalOptionsString);
      }
      setEvaluator(ASEvaluation.forName(evalClassName, evalOptions));
    }
    
    if ((m_ASEvaluator instanceof AttributeEvaluator)) {
      setSearch(new Ranker());
    }
    
    optionString = Utils.getOption('S', options);
    if (optionString.length() != 0) {
      optionString = optionString.trim();
      int breakLoc = optionString.indexOf(' ');
      String SearchClassName = optionString;
      String SearchOptionsString = "";
      String[] SearchOptions = null;
      if (breakLoc != -1) {
        SearchClassName = optionString.substring(0, breakLoc);
        SearchOptionsString = optionString.substring(breakLoc).trim();
        SearchOptions = Utils.splitOptions(SearchOptionsString);
      }
      setSearch(ASSearch.forName(SearchClassName, SearchOptions));
    }
    
    Utils.checkForRemainingOptions(options);
  }
  






  public String[] getOptions()
  {
    String[] EvaluatorOptions = new String[0];
    String[] SearchOptions = new String[0];
    int current = 0;
    
    if ((m_ASEvaluator instanceof OptionHandler)) {
      EvaluatorOptions = ((OptionHandler)m_ASEvaluator).getOptions();
    }
    
    if ((m_ASSearch instanceof OptionHandler)) {
      SearchOptions = ((OptionHandler)m_ASSearch).getOptions();
    }
    
    String[] setOptions = new String[10];
    setOptions[(current++)] = "-E";
    setOptions[(current++)] = (getEvaluator().getClass().getName() + " " + Utils.joinOptions(EvaluatorOptions));
    

    setOptions[(current++)] = "-S";
    setOptions[(current++)] = (getSearch().getClass().getName() + " " + Utils.joinOptions(SearchOptions));
    

    while (current < setOptions.length) {
      setOptions[(current++)] = "";
    }
    
    return setOptions;
  }
  






  public String evaluatorTipText()
  {
    return "Determines how attributes/attribute subsets are evaluated.";
  }
  




  public void setEvaluator(ASEvaluation evaluator)
  {
    m_ASEvaluator = evaluator;
  }
  






  public String searchTipText()
  {
    return "Determines the search method.";
  }
  




  public void setSearch(ASSearch search)
  {
    m_ASSearch = search;
  }
  





  public ASEvaluation getEvaluator()
  {
    return m_ASEvaluator;
  }
  





  public ASSearch getSearch()
  {
    return m_ASSearch;
  }
  



  public Capabilities getCapabilities()
  {
    Capabilities result;
    


    if (m_ASEvaluator == null) {
      Capabilities result = super.getCapabilities();
      result.disableAll();
    }
    else {
      result = m_ASEvaluator.getCapabilities();
      


      result.enable(Capabilities.Capability.NO_CLASS);
    }
    
    result.setMinimumNumberInstances(0);
    
    return result;
  }
  











  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    
    if (isOutputFormatDefined()) {
      convertInstance(instance);
      return true;
    }
    
    bufferInput(instance);
    return false;
  }
  








  public boolean batchFinished()
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    
    if (!isOutputFormatDefined()) {
      m_hasClass = (getInputFormat().classIndex() >= 0);
      
      m_trainSelector.setEvaluator(m_ASEvaluator);
      m_trainSelector.setSearch(m_ASSearch);
      m_trainSelector.SelectAttributes(getInputFormat());
      

      m_SelectedAttributes = m_trainSelector.selectedAttributes();
      if (m_SelectedAttributes == null) {
        throw new Exception("No selected attributes\n");
      }
      
      setOutputFormat();
      

      for (int i = 0; i < getInputFormat().numInstances(); i++) {
        convertInstance(getInputFormat().instance(i));
      }
      flushInput();
    }
    
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  






  protected void setOutputFormat()
    throws Exception
  {
    if (m_SelectedAttributes == null) {
      setOutputFormat(null);
      return;
    }
    
    FastVector attributes = new FastVector(m_SelectedAttributes.length);
    Instances informat;
    Instances informat;
    if ((m_ASEvaluator instanceof AttributeTransformer)) {
      informat = ((AttributeTransformer)m_ASEvaluator).transformedHeader();
    } else {
      informat = getInputFormat();
    }
    
    for (int i = 0; i < m_SelectedAttributes.length; i++) {
      attributes.addElement(informat.attribute(m_SelectedAttributes[i]).copy());
    }
    

    Instances outputFormat = new Instances(getInputFormat().relationName(), attributes, 0);
    




    if (m_hasClass) {
      outputFormat.setClassIndex(m_SelectedAttributes.length - 1);
    }
    
    setOutputFormat(outputFormat);
  }
  






  protected void convertInstance(Instance instance)
    throws Exception
  {
    double[] newVals = new double[getOutputFormat().numAttributes()];
    
    if ((m_ASEvaluator instanceof AttributeTransformer)) {
      Instance tempInstance = ((AttributeTransformer)m_ASEvaluator).convertInstance(instance);
      
      for (int i = 0; i < m_SelectedAttributes.length; i++) {
        int current = m_SelectedAttributes[i];
        newVals[i] = tempInstance.value(current);
      }
    } else {
      for (int i = 0; i < m_SelectedAttributes.length; i++) {
        int current = m_SelectedAttributes[i];
        newVals[i] = instance.value(current);
      }
    }
    if ((instance instanceof SparseInstance)) {
      push(new SparseInstance(instance.weight(), newVals));
    } else {
      push(new Instance(instance.weight(), newVals));
    }
  }
  



  protected void resetOptions()
  {
    m_trainSelector = new weka.attributeSelection.AttributeSelection();
    setEvaluator(new CfsSubsetEval());
    setSearch(new BestFirst());
    m_SelectedAttributes = null;
    m_FilterOptions = null;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10868 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new AttributeSelection(), argv);
  }
}

package weka.attributeSelection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.instance.SpreadSubsample;




















































public class FilteredAttributeEval
  extends ASEvaluation
  implements Serializable, AttributeEvaluator, OptionHandler
{
  static final long serialVersionUID = 2111121880778327334L;
  protected AttributeEvaluator m_evaluator = new InfoGainAttributeEval();
  

  protected Filter m_filter = new SpreadSubsample();
  
  protected Instances m_filteredInstances;
  
  public FilteredAttributeEval()
  {
    m_filteredInstances = null;
  }
  



  public Capabilities getCapabilities()
  {
    Capabilities result;
    

    if (getFilter() == null) {
      Capabilities result = super.getCapabilities();
      result.disableAll();
    } else {
      result = getFilter().getCapabilities();
    }
    

    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    
    return result;
  }
  



  public String globalInfo()
  {
    return "Class for running an arbitrary attribute evaluator on data that has been passed through an arbitrary filter (note: filters that alter the order or number of attributes are not allowed). Like the evaluator, the structure of the filter is based exclusively on the training data.";
  }
  







  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tFull name of base evaluator to use, followed by evaluator options.\n\teg: \"weka.attributeSelection.InfoGainAttributeEval -M\"", "W", 1, "-W <evaluator specification>"));
    




    newVector.addElement(new Option("\tFull class name of filter to use, followed\n\tby filter options.\n\teg: \"weka.filters.supervised.instance.SpreadSubsample -M 1\"", "F", 1, "-F <filter specification>"));
    




    return newVector.elements();
  }
  


















  public void setOptions(String[] options)
    throws Exception
  {
    String evaluator = Utils.getOption('W', options);
    
    if (evaluator.length() > 0) {
      String[] evaluatorSpec = Utils.splitOptions(evaluator);
      if (evaluatorSpec.length == 0) {
        throw new IllegalArgumentException("Invalid evaluator specification string");
      }
      
      String evaluatorName = evaluatorSpec[0];
      evaluatorSpec[0] = "";
      setAttributeEvaluator((ASEvaluation)Utils.forName(AttributeEvaluator.class, evaluatorName, evaluatorSpec));
    }
    else
    {
      setAttributeEvaluator(new InfoGainAttributeEval());
    }
    

    String filterString = Utils.getOption('F', options);
    if (filterString.length() > 0) {
      String[] filterSpec = Utils.splitOptions(filterString);
      if (filterSpec.length == 0) {
        throw new IllegalArgumentException("Invalid filter specification string");
      }
      String filterName = filterSpec[0];
      filterSpec[0] = "";
      setFilter((Filter)Utils.forName(Filter.class, filterName, filterSpec));
    } else {
      setFilter(new SpreadSubsample());
    }
  }
  




  public String[] getOptions()
  {
    ArrayList<String> options = new ArrayList();
    
    options.add("-W");
    options.add(getEvaluatorSpec());
    
    options.add("-F");
    options.add(getFilterSpec());
    
    return (String[])options.toArray(new String[0]);
  }
  




  protected String getEvaluatorSpec()
  {
    AttributeEvaluator a = m_evaluator;
    if ((a instanceof OptionHandler)) {
      return a.getClass().getName() + " " + Utils.joinOptions(((OptionHandler)a).getOptions());
    }
    
    return a.getClass().getName();
  }
  




  public String attributeEvaluatorTipText()
  {
    return "The attribute evaluator to be used.";
  }
  




  public void setAttributeEvaluator(ASEvaluation newEvaluator)
  {
    if (!(newEvaluator instanceof AttributeEvaluator)) {
      throw new IllegalArgumentException("Evaluator must be an AttributeEvaluator!");
    }
    m_evaluator = ((AttributeEvaluator)newEvaluator);
  }
  




  public ASEvaluation getAttributeEvaluator()
  {
    return (ASEvaluation)m_evaluator;
  }
  




  protected String getFilterSpec()
  {
    Filter c = getFilter();
    if ((c instanceof OptionHandler)) {
      return c.getClass().getName() + " " + Utils.joinOptions(((OptionHandler)c).getOptions());
    }
    
    return c.getClass().getName();
  }
  




  public String filterTipText()
  {
    return "The filter to be used.";
  }
  




  public void setFilter(Filter newFilter)
  {
    m_filter = newFilter;
  }
  




  public Filter getFilter()
  {
    return m_filter;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5562 $");
  }
  






  public void buildEvaluator(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    Instances original = new Instances(data, 0);
    
    m_filter.setInputFormat(data);
    data = Filter.useFilter(data, m_filter);
    


    if (data.numAttributes() != original.numAttributes()) {
      throw new Exception("Filter must not alter the number of attributes in the data!");
    }
    


    if ((original.classIndex() >= 0) && 
      (data.classIndex() != original.classIndex())) {
      throw new Exception("Filter must not change the class attribute!");
    }
    


    for (int i = 0; i < original.numAttributes(); i++) {
      if (!data.attribute(i).name().equals(original.attribute(i).name())) {
        throw new Exception("Filter must not alter the order of the attributes!");
      }
    }
    

    getAttributeEvaluator().getCapabilities().testWithFail(data);
    m_filteredInstances = data.stringFreeStructure();
    
    ((ASEvaluation)m_evaluator).buildEvaluator(data);
  }
  






  public double evaluateAttribute(int attribute)
    throws Exception
  {
    return m_evaluator.evaluateAttribute(attribute);
  }
  



  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_filteredInstances == null) {
      text.append("Filtered attribute evaluator has not been built");
    } else {
      text.append("Filtered Attribute Evaluator");
      text.append("\nFilter: " + getFilterSpec());
      text.append("\nAttribute evaluator: " + getEvaluatorSpec());
      text.append("\n\nFiltered header:\n");
      text.append(m_filteredInstances);
    }
    text.append("\n");
    return text.toString();
  }
  







  public static void main(String[] args)
  {
    runEvaluator(new FilteredAttributeEval(), args);
  }
}

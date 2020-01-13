package weka.classifiers.meta;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.trees.J48;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.attribute.Discretize;
























































































public class FilteredClassifier
  extends SingleClassifierEnhancer
  implements Drawable
{
  static final long serialVersionUID = -4523450618538717400L;
  protected Filter m_Filter = new AttributeSelection();
  


  protected Instances m_FilteredInstances;
  



  public String globalInfo()
  {
    return "Class for running an arbitrary classifier on data that has been passed through an arbitrary filter. Like the classifier, the structure of the filter is based exclusively on the training data and test instances will be processed by the filter without changing their structure.";
  }
  








  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.J48";
  }
  



  public FilteredClassifier()
  {
    m_Classifier = new J48();
    m_Filter = new Discretize();
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
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    newVector.addElement(new Option("\tFull class name of filter to use, followed\n\tby filter options.\n\teg: \"weka.filters.unsupervised.attribute.Remove -V -R 1,2\"", "F", 1, "-F <filter specification>"));
    




    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    return newVector.elements();
  }
  






























































  public void setOptions(String[] options)
    throws Exception
  {
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
      setFilter(new Discretize());
    }
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 2];
    int current = 0;
    
    options[(current++)] = "-F";
    options[(current++)] = ("" + getFilterSpec());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    
    return options;
  }
  




  public String filterTipText()
  {
    return "The filter to be used.";
  }
  





  public void setFilter(Filter filter)
  {
    m_Filter = filter;
  }
  





  public Filter getFilter()
  {
    return m_Filter;
  }
  






  protected String getFilterSpec()
  {
    Filter c = getFilter();
    if ((c instanceof OptionHandler)) {
      return c.getClass().getName() + " " + Utils.joinOptions(((OptionHandler)c).getOptions());
    }
    
    return c.getClass().getName();
  }
  


  public Capabilities getCapabilities()
  {
    Capabilities result;
    
    Capabilities result;
    
    if (getFilter() == null) {
      result = super.getCapabilities();
    } else {
      result = getFilter().getCapabilities();
    }
    
    result.disable(Capabilities.Capability.NO_CLASS);
    

    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    return result;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    if (m_Classifier == null) {
      throw new Exception("No base classifiers have been set!");
    }
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    






    m_Filter.setInputFormat(data);
    data = Filter.useFilter(data, m_Filter);
    


    getClassifier().getCapabilities().testWithFail(data);
    
    m_FilteredInstances = data.stringFreeStructure();
    m_Classifier.buildClassifier(data);
  }
  













  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_Filter.numPendingOutput() > 0) {
      throw new Exception("Filter output queue not empty!");
    }
    





    if (!m_Filter.input(instance)) {
      throw new Exception("Filter didn't make the test instance immediately available!");
    }
    
    m_Filter.batchFinished();
    Instance newInstance = m_Filter.output();
    





    return m_Classifier.distributionForInstance(newInstance);
  }
  





  public String toString()
  {
    if (m_FilteredInstances == null) {
      return "FilteredClassifier: No model built yet.";
    }
    
    String result = "FilteredClassifier using " + getClassifierSpec() + " on data filtered through " + getFilterSpec() + "\n\nFiltered Header\n" + m_FilteredInstances.toString() + "\n\nClassifier Model\n" + m_Classifier.toString();
    






    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.28 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new FilteredClassifier(), argv);
  }
}

package weka.clusterers;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.AllFilter;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;













































































public class FilteredClusterer
  extends SingleClustererEnhancer
{
  private static final long serialVersionUID = 1420005943163412943L;
  protected Filter m_Filter;
  protected Instances m_FilteredInstances;
  
  public FilteredClusterer()
  {
    m_Clusterer = new SimpleKMeans();
    m_Filter = new AllFilter();
  }
  





  public String globalInfo()
  {
    return "Class for running an arbitrary clusterer on data that has been passed through an arbitrary filter. Like the clusterer, the structure of the filter is based exclusively on the training data and test instances will be processed by the filter without changing their structure.";
  }
  








  protected String defaultFilterString()
  {
    return AllFilter.class.getName();
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tFull class name of filter to use, followed\n\tby filter options.\n\teg: \"weka.filters.unsupervised.attribute.Remove -V -R 1,2\"\n(default: " + defaultFilterString() + ")", "F", 1, "-F <filter specification>"));
    





    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    return result.elements();
  }
  










































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('F', options);
    if (tmpStr.length() > 0) {
      String[] tmpOptions = Utils.splitOptions(tmpStr);
      if (tmpOptions.length == 0)
        throw new IllegalArgumentException("Invalid filter specification string");
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setFilter((Filter)Utils.forName(Filter.class, tmpStr, tmpOptions));
    }
    else {
      setFilter(new AllFilter());
    }
    
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-F");
    result.add(getFilterSpec());
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String filterTipText()
  {
    return "The filter to be used.";
  }
  




  public void setFilter(Filter filter)
  {
    m_Filter = filter;
    
    if ((m_Filter instanceof SupervisedFilter)) {
      System.out.println("WARNING: you are using a supervised filter, which will leak information about the class attribute!");
    }
  }
  





  public Filter getFilter()
  {
    return m_Filter;
  }
  








  protected String getFilterSpec()
  {
    Filter filter = getFilter();
    String result = filter.getClass().getName();
    
    if ((filter instanceof OptionHandler)) {
      result = result + " " + Utils.joinOptions(((OptionHandler)filter).getOptions());
    }
    return result;
  }
  



  public Capabilities getCapabilities()
  {
    Capabilities result;
    

    if (getFilter() == null) {
      Capabilities result = super.getCapabilities();
      result.disableAll();
      result.enable(Capabilities.Capability.NO_CLASS);
    } else {
      result = getFilter().getCapabilities();
    }
    

    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    return result;
  }
  




  public void buildClusterer(Instances data)
    throws Exception
  {
    if (m_Clusterer == null) {
      throw new Exception("No base clusterer has been set!");
    }
    
    if (data.classIndex() > -1) {
      data = new Instances(data);
      data.deleteWithMissingClass();
    }
    
    m_Filter.setInputFormat(data);
    data = Filter.useFilter(data, m_Filter);
    

    getClusterer().getCapabilities().testWithFail(data);
    
    m_FilteredInstances = data.stringFreeStructure();
    m_Clusterer.buildClusterer(data);
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
    
    return m_Clusterer.distributionForInstance(newInstance);
  }
  


  public String toString()
  {
    String result;
    
    String result;
    
    if (m_FilteredInstances == null) {
      result = "FilteredClusterer: No model built yet.";
    } else {
      result = "FilteredClusterer using " + getClustererSpec() + " on data filtered through " + getFilterSpec() + "\n\nFiltered Header\n" + m_FilteredInstances.toString() + "\n\nClusterer Model\n" + m_Clusterer.toString();
    }
    






    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5538 $");
  }
  




  public static void main(String[] args)
  {
    runClusterer(new FilteredClusterer(), args);
  }
}

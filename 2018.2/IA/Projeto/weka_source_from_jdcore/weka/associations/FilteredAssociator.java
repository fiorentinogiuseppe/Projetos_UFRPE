package weka.associations;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;







































































































public class FilteredAssociator
  extends SingleAssociatorEnhancer
{
  static final long serialVersionUID = -4523450618538717400L;
  protected Filter m_Filter;
  protected Instances m_FilteredInstances;
  protected int m_ClassIndex;
  
  public FilteredAssociator()
  {
    m_Associator = new Apriori();
    m_Filter = new MultiFilter();
    ((MultiFilter)m_Filter).setFilters(new Filter[] { new ReplaceMissingValues() });
    
    m_ClassIndex = -1;
  }
  





  public String globalInfo()
  {
    return "Class for running an arbitrary associator on data that has been passed through an arbitrary filter. Like the associator, the structure of the filter is based exclusively on the training data and test instances will be processed by the filter without changing their structure.";
  }
  








  protected String defaultAssociatorString()
  {
    return Apriori.class.getName();
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tFull class name of filter to use, followed\n\tby filter options.\n\teg: \"weka.filters.unsupervised.attribute.Remove -V -R 1,2\"\n\t(default: weka.filters.MultiFilter with\n\tweka.filters.unsupervised.attribute.ReplaceMissingValues)", "F", 1, "-F <filter specification>"));
    






    result.addElement(new Option("\tThe class index.\n\t(default: -1, i.e. unset)", "c", 1, "-c <the class index>"));
    



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
      String[] filterSpec = Utils.splitOptions(tmpStr);
      if (filterSpec.length == 0)
        throw new IllegalArgumentException("Invalid filter specification string");
      String filterName = filterSpec[0];
      filterSpec[0] = "";
      setFilter((Filter)Utils.forName(Filter.class, filterName, filterSpec));
    }
    else {
      setFilter(new Discretize());
    }
    
    tmpStr = Utils.getOption('c', options);
    if (tmpStr.length() > 0) {
      if (tmpStr.equalsIgnoreCase("last")) {
        setClassIndex(0);
      } else if (tmpStr.equalsIgnoreCase("first")) {
        setClassIndex(1);
      } else {
        setClassIndex(Integer.parseInt(tmpStr));
      }
    } else {
      setClassIndex(-1);
    }
    
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-F");
    result.add("" + getFilterSpec());
    
    result.add("-c");
    result.add("" + getClassIndex());
    
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
  




  public void setFilter(Filter value)
  {
    m_Filter = value;
  }
  




  public Filter getFilter()
  {
    return m_Filter;
  }
  





  public String classIndexTipText()
  {
    return "Index of the class attribute. If set to -1, the last attribute is taken as class attribute.";
  }
  




  public void setClassIndex(int value)
  {
    m_ClassIndex = value;
  }
  




  public int getClassIndex()
  {
    return m_ClassIndex;
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
    
    result.enable(Capabilities.Capability.NO_CLASS);
    

    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    return result;
  }
  




  public void buildAssociations(Instances data)
    throws Exception
  {
    if (m_Associator == null) {
      throw new Exception("No base associator has been set!");
    }
    
    data = new Instances(data);
    if (getClassIndex() == 0) {
      data.setClassIndex(data.numAttributes() - 1);
    } else {
      data.setClassIndex(getClassIndex() - 1);
    }
    
    if (getClassIndex() != -1)
    {
      data.deleteWithMissingClass();
    }
    
    m_Filter.setInputFormat(data);
    data = Filter.useFilter(data, m_Filter);
    

    getAssociator().getCapabilities().testWithFail(data);
    
    m_FilteredInstances = data.stringFreeStructure();
    m_Associator.buildAssociations(data);
  }
  


  public String toString()
  {
    String result;
    
    String result;
    
    if (m_FilteredInstances == null) {
      result = "FilteredAssociator: No model built yet.";
    }
    else {
      result = "FilteredAssociator using " + getAssociatorSpec() + " on data filtered through " + getFilterSpec() + "\n\nFiltered Header\n" + m_FilteredInstances.toString() + "\n\nAssociator Model\n" + m_Associator.toString();
    }
    







    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5504 $");
  }
  




  public static void main(String[] args)
  {
    runAssociator(new FilteredAssociator(), args);
  }
}

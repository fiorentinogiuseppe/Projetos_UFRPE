package weka.filters;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
















































public class MultiFilter
  extends SimpleStreamFilter
{
  private static final long serialVersionUID = -6293720886005713120L;
  protected Filter[] m_Filters = { new AllFilter() };
  

  protected boolean m_Streamable = false;
  

  protected boolean m_StreamableChecked = false;
  

  public MultiFilter() {}
  

  public String globalInfo()
  {
    return "Applies several filters successively. In case all supplied filters are StreamableFilters, it will act as a streamable one, too.";
  }
  






  public Enumeration listOptions()
  {
    Vector result = new Vector();
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.add(enm.nextElement());
    }
    result.addElement(new Option("\tA filter to apply (can be specified multiple times).", "F", 1, "-F <classname [options]>"));
    


    return result.elements();
  }
  




















  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    Vector filters = new Vector();
    String tmpStr; while ((tmpStr = Utils.getOption("F", options)).length() != 0) {
      String[] options2 = Utils.splitOptions(tmpStr);
      String filter = options2[0];
      options2[0] = "";
      filters.add(Utils.forName(Filter.class, filter, options2));
    }
    

    if (filters.size() == 0) {
      filters.add(new AllFilter());
    }
    setFilters((Filter[])filters.toArray(new Filter[filters.size()]));
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    for (i = 0; i < getFilters().length; i++) {
      result.add("-F");
      result.add(getFilterSpec(getFilter(i)));
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public Capabilities getCapabilities()
  {
    if (getFilters().length == 0) {
      Capabilities result = super.getCapabilities();
      result.disableAll();
      
      return result;
    }
    return getFilters()[0].getCapabilities();
  }
  







  protected void reset()
  {
    super.reset();
    m_StreamableChecked = false;
  }
  







  public void setFilters(Filter[] filters)
  {
    m_Filters = filters;
    reset();
  }
  




  public Filter[] getFilters()
  {
    return m_Filters;
  }
  




  public String filtersTipText()
  {
    return "The base filters to be used.";
  }
  





  public Filter getFilter(int index)
  {
    return m_Filters[index];
  }
  


  protected String getFilterSpec(Filter filter)
  {
    String result;
    

    String result;
    
    if (filter == null) {
      result = "";
    }
    else {
      result = filter.getClass().getName();
      if ((filter instanceof OptionHandler)) {
        result = result + " " + Utils.joinOptions(((OptionHandler)filter).getOptions());
      }
    }
    
    return result;
  }
  






  public boolean isStreamableFilter()
  {
    if (!m_StreamableChecked) {
      m_Streamable = true;
      m_StreamableChecked = true;
      
      for (int i = 0; i < getFilters().length; i++) {
        if ((getFilter(i) instanceof MultiFilter)) {
          m_Streamable = ((MultiFilter)getFilter(i)).isStreamableFilter();
        } else if ((getFilter(i) instanceof StreamableFilter)) {
          m_Streamable = true;
        } else {
          m_Streamable = false;
        }
        if (!m_Streamable) {
          break;
        }
      }
      if (getDebug()) {
        System.out.println("Streamable: " + m_Streamable);
      }
    }
    return m_Streamable;
  }
  












  protected boolean hasImmediateOutputFormat()
  {
    return isStreamableFilter();
  }
  















  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    Instances result = getInputFormat();
    
    for (int i = 0; i < getFilters().length; i++) {
      if (!isFirstBatchDone())
        getFilter(i).setInputFormat(result);
      result = getFilter(i).getOutputFormat();
    }
    
    return result;
  }
  









  protected Instance process(Instance instance)
    throws Exception
  {
    Instance result = (Instance)instance.copy();
    
    for (int i = 0; i < getFilters().length; i++) {
      if (getFilter(i).input(result)) {
        result = getFilter(i).output();
      }
      else {
        result = null;
        break;
      }
    }
    
    return result;
  }
  













  protected Instances process(Instances instances)
    throws Exception
  {
    Instances result = instances;
    
    for (int i = 0; i < getFilters().length; i++) {
      if (!isFirstBatchDone())
        getFilter(i).setInputFormat(result);
      result = Filter.useFilter(result, getFilter(i));
    }
    
    return result;
  }
  









  public boolean batchFinished()
    throws Exception
  {
    super.batchFinished();
    
    for (int i = 0; i > getFilters().length; i++) {
      getFilter(i).batchFinished();
    }
    
    return numPendingOutput() != 0;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9718 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new MultiFilter(), args);
  }
}

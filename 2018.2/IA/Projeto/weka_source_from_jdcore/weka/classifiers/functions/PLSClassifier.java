package weka.classifiers.functions;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.PLSFilter;









































































public class PLSClassifier
  extends Classifier
{
  private static final long serialVersionUID = 4819775160590973256L;
  protected PLSFilter m_Filter = new PLSFilter();
  

  protected PLSFilter m_ActualFilter = null;
  


  public PLSClassifier() {}
  

  public String globalInfo()
  {
    return "A wrapper classifier for the PLSFilter, utilizing the PLSFilter's ability to perform predictions.";
  }
  









  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe PLS filter to use. Full classname of filter to include, \tfollowed by scheme options.\n\t(default: weka.filters.supervised.attribute.PLSFilter)", "filter", 1, "-filter <filter specification>"));
    




    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    if ((getFilter() instanceof OptionHandler)) {
      result.addElement(new Option("", "", 0, "\nOptions specific to filter " + getFilter().getClass().getName() + " ('-filter'):"));
      



      en = ((OptionHandler)getFilter()).listOptions();
      while (en.hasMoreElements()) {
        result.addElement(en.nextElement());
      }
    }
    return result.elements();
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-filter");
    if ((getFilter() instanceof OptionHandler)) {
      result.add(getFilter().getClass().getName() + " " + Utils.joinOptions(((OptionHandler)getFilter()).getOptions()));

    }
    else
    {
      result.add(getFilter().getClass().getName());
    }
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  















































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption("filter", options);
    String[] tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setFilter((Filter)Utils.forName(Filter.class, tmpStr, tmpOptions));
    }
  }
  





  public String filterTipText()
  {
    return "The PLS filter to be used (only used for setup).";
  }
  




  public void setFilter(Filter value)
    throws Exception
  {
    if (!(value instanceof PLSFilter)) {
      throw new Exception("Filter has to be PLSFilter!");
    }
    m_Filter = ((PLSFilter)value);
  }
  




  public Filter getFilter()
  {
    return m_Filter;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = getFilter().getCapabilities();
    

    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(1);
    
    return result;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    

    m_ActualFilter = ((PLSFilter)Filter.makeCopy(m_Filter));
    m_ActualFilter.setPerformPrediction(false);
    m_ActualFilter.setInputFormat(data);
    Filter.useFilter(data, m_ActualFilter);
    m_ActualFilter.setPerformPrediction(true);
  }
  










  public double classifyInstance(Instance instance)
    throws Exception
  {
    m_ActualFilter.input(instance);
    m_ActualFilter.batchFinished();
    Instance pred = m_ActualFilter.output();
    double result = pred.classValue();
    
    return result;
  }
  






  public String toString()
  {
    String result = getClass().getName() + "\n" + getClass().getName().replaceAll(".", "=") + "\n\n";
    
    result = result + "# Components..........: " + m_Filter.getNumComponents() + "\n";
    result = result + "Algorithm.............: " + m_Filter.getAlgorithm().getSelectedTag().getReadable() + "\n";
    result = result + "Replace missing values: " + (m_Filter.getReplaceMissing() ? "yes" : "no") + "\n";
    result = result + "Preprocessing.........: " + m_Filter.getPreprocessing().getSelectedTag().getReadable() + "\n";
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new PLSClassifier(), args);
  }
}

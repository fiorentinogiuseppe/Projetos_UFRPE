package weka.clusterers;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

































public abstract class SingleClustererEnhancer
  extends AbstractClusterer
  implements OptionHandler
{
  private static final long serialVersionUID = 4893928362926428671L;
  protected Clusterer m_Clusterer = new SimpleKMeans();
  

  public SingleClustererEnhancer() {}
  

  protected String defaultClustererString()
  {
    return SimpleKMeans.class.getName();
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tFull name of base clusterer.\n\t(default: " + defaultClustererString() + ")", "W", 1, "-W"));
    



    if ((m_Clusterer instanceof OptionHandler)) {
      result.addElement(new Option("", "", 0, "\nOptions specific to clusterer " + m_Clusterer.getClass().getName() + ":"));
      


      Enumeration enu = ((OptionHandler)m_Clusterer).listOptions();
      while (enu.hasMoreElements()) {
        result.addElement(enu.nextElement());
      }
    }
    
    return result.elements();
  }
  






  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() > 0)
    {

      setClusterer(AbstractClusterer.forName(tmpStr, null));
      setClusterer(AbstractClusterer.forName(tmpStr, Utils.partitionOptions(options)));

    }
    else
    {
      setClusterer(AbstractClusterer.forName(defaultClustererString(), null));
      setClusterer(AbstractClusterer.forName(defaultClustererString(), Utils.partitionOptions(options)));
    }
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-W");
    result.add(getClusterer().getClass().getName());
    
    if ((getClusterer() instanceof OptionHandler)) {
      result.add("--");
      String[] options = ((OptionHandler)getClusterer()).getOptions();
      for (int i = 0; i < options.length; i++) {
        result.add(options[i]);
      }
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String clustererTipText()
  {
    return "The base clusterer to be used.";
  }
  




  public void setClusterer(Clusterer value)
  {
    m_Clusterer = value;
  }
  




  public Clusterer getClusterer()
  {
    return m_Clusterer;
  }
  








  protected String getClustererSpec()
  {
    Clusterer clusterer = getClusterer();
    String result = clusterer.getClass().getName();
    
    if ((clusterer instanceof OptionHandler)) {
      result = result + " " + Utils.joinOptions(((OptionHandler)clusterer).getOptions());
    }
    return result;
  }
  


  public Capabilities getCapabilities()
  {
    Capabilities result;
    
    Capabilities result;
    
    if (getClusterer() == null) {
      result = super.getCapabilities();
    } else {
      result = getClusterer().getCapabilities();
    }
    
    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    return result;
  }
  





  public int numberOfClusters()
    throws Exception
  {
    return m_Clusterer.numberOfClusters();
  }
}

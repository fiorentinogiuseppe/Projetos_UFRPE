package weka.associations;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;



































public abstract class SingleAssociatorEnhancer
  extends AbstractAssociator
  implements OptionHandler
{
  private static final long serialVersionUID = -3665885256363525164L;
  protected Associator m_Associator = new Apriori();
  

  public SingleAssociatorEnhancer() {}
  

  protected String defaultAssociatorString()
  {
    return Apriori.class.getName();
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tFull name of base associator.\n\t(default: " + defaultAssociatorString() + ")", "W", 1, "-W"));
    



    if ((m_Associator instanceof OptionHandler)) {
      result.addElement(new Option("", "", 0, "\nOptions specific to associator " + m_Associator.getClass().getName() + ":"));
      



      Enumeration enm = ((OptionHandler)m_Associator).listOptions();
      while (enm.hasMoreElements()) {
        result.addElement(enm.nextElement());
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

      setAssociator(AbstractAssociator.forName(tmpStr, null));
      setAssociator(AbstractAssociator.forName(tmpStr, Utils.partitionOptions(options)));

    }
    else
    {
      setAssociator(AbstractAssociator.forName(defaultAssociatorString(), null));
      setAssociator(AbstractAssociator.forName(defaultAssociatorString(), Utils.partitionOptions(options)));
    }
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-W");
    result.add(getAssociator().getClass().getName());
    
    if ((getAssociator() instanceof OptionHandler)) {
      String[] options = ((OptionHandler)getAssociator()).getOptions();
      result.add("--");
      for (int i = 0; i < options.length; i++) {
        result.add(options[i]);
      }
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String associatorTipText()
  {
    return "The base associator to be used.";
  }
  




  public void setAssociator(Associator value)
  {
    m_Associator = value;
  }
  




  public Associator getAssociator()
  {
    return m_Associator;
  }
  





  protected String getAssociatorSpec()
  {
    Associator c = getAssociator();
    return c.getClass().getName() + " " + Utils.joinOptions(((OptionHandler)c).getOptions());
  }
  


  public Capabilities getCapabilities()
  {
    Capabilities result;
    

    Capabilities result;
    
    if (getAssociator() != null) {
      result = getAssociator().getCapabilities();
    } else {
      result = new Capabilities(this);
      result.disableAll();
    }
    

    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    result.setOwner(this);
    
    return result;
  }
}

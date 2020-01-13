package weka.datagenerators;

import java.io.Serializable;
import java.util.Enumeration;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.Utils;






































public abstract class ClusterDefinition
  implements Serializable, OptionHandler, RevisionHandler
{
  private static final long serialVersionUID = -5950001207047429961L;
  protected ClusterGenerator m_Parent;
  
  public ClusterDefinition()
  {
    this(null);
  }
  




  public ClusterDefinition(ClusterGenerator parent)
  {
    m_Parent = parent;
    try
    {
      setDefaults();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  





  protected abstract void setDefaults()
    throws Exception;
  




  public String globalInfo()
  {
    return "Contains informations about a certain cluster of a cluster generator.";
  }
  






  public abstract Enumeration listOptions();
  





  public abstract void setOptions(String[] paramArrayOfString)
    throws Exception;
  





  public abstract String[] getOptions();
  





  public ClusterGenerator getParent()
  {
    return m_Parent;
  }
  




  public void setParent(ClusterGenerator parent)
  {
    m_Parent = parent;
  }
  





  public String parentTipText()
  {
    return "The cluster generator this object belongs to.";
  }
  




  public String toString()
  {
    return getClass().getName() + ": " + Utils.joinOptions(getOptions());
  }
}

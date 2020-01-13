package weka.associations.tertius;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;























































public abstract class LiteralSet
  implements Serializable, Cloneable, RevisionHandler
{
  private static final long serialVersionUID = 6094536488654503152L;
  private ArrayList m_literals;
  private Literal m_lastLiteral;
  private int m_numInstances;
  private ArrayList m_counterInstances;
  private int m_counter;
  private int m_type;
  
  public LiteralSet()
  {
    m_literals = new ArrayList();
    m_lastLiteral = null;
    m_counterInstances = null;
    m_type = -1;
  }
  





  public LiteralSet(Instances instances)
  {
    this();
    m_numInstances = instances.numInstances();
    m_counterInstances = new ArrayList(m_numInstances);
    Enumeration enu = instances.enumerateInstances();
    while (enu.hasMoreElements()) {
      m_counterInstances.add(enu.nextElement());
    }
  }
  






  public Object clone()
  {
    Object result = null;
    try {
      result = super.clone();
      
      m_literals = ((ArrayList)m_literals.clone());
      if (m_counterInstances != null)
      {
        m_counterInstances = ((ArrayList)m_counterInstances.clone());
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.exit(0);
    }
    return result;
  }
  






  public void upDate(Instances instances)
  {
    Enumeration enu = instances.enumerateInstances();
    m_numInstances = instances.numInstances();
    m_counter = 0;
    while (enu.hasMoreElements()) {
      if (counterInstance((Instance)enu.nextElement())) {
        m_counter += 1;
      }
    }
  }
  





  public int getCounterInstancesNumber()
  {
    if (m_counterInstances != null) {
      return m_counterInstances.size();
    }
    return m_counter;
  }
  






  public double getCounterInstancesFrequency()
  {
    return getCounterInstancesNumber() / m_numInstances;
  }
  






  public boolean overFrequencyThreshold(double minFrequency)
  {
    return getCounterInstancesFrequency() >= minFrequency;
  }
  





  public boolean hasMaxCounterInstances()
  {
    return getCounterInstancesNumber() == m_numInstances;
  }
  





  public void addElement(Literal element)
  {
    m_literals.add(element);
    
    m_lastLiteral = element;
    
    if ((element instanceof IndividualLiteral)) {
      int type = ((IndividualLiteral)element).getType();
      if (type > m_type) {
        m_type = type;
      }
    }
    
    if (m_counterInstances != null) {
      for (int i = m_counterInstances.size() - 1; i >= 0; i--) {
        Instance current = (Instance)m_counterInstances.get(i);
        if (!canKeep(current, element)) {
          m_counterInstances.remove(i);
        }
      }
    }
  }
  





  public final boolean isEmpty()
  {
    return m_literals.size() == 0;
  }
  





  public final int numLiterals()
  {
    return m_literals.size();
  }
  





  public final Iterator enumerateLiterals()
  {
    return m_literals.iterator();
  }
  





  public Literal getLastLiteral()
  {
    return m_lastLiteral;
  }
  







  public boolean negationIncludedIn(LiteralSet otherSet)
  {
    Iterator iter = enumerateLiterals();
    while (iter.hasNext()) {
      Literal current = (Literal)iter.next();
      if (!otherSet.contains(current.getNegation())) {
        return false;
      }
    }
    return true;
  }
  






  public boolean contains(Literal lit)
  {
    return m_literals.contains(lit);
  }
  



  public int getType()
  {
    return m_type;
  }
  







  public boolean counterInstance(Instance individual, Instance part)
  {
    Iterator iter = enumerateLiterals();
    while (iter.hasNext()) {
      IndividualLiteral current = (IndividualLiteral)iter.next();
      if ((current.getType() == IndividualLiteral.INDIVIDUAL_PROPERTY) && (!canKeep(individual, current)))
      {
        return false; }
      if ((current.getType() == IndividualLiteral.PART_PROPERTY) && (!canKeep(part, current)))
      {
        return false;
      }
    }
    return true;
  }
  





  public boolean counterInstance(Instance instance)
  {
    if (((instance instanceof IndividualInstance)) && (m_type == IndividualLiteral.PART_PROPERTY))
    {






      Enumeration enu = ((IndividualInstance)instance).getParts().enumerateInstances();
      
      while (enu.hasMoreElements()) {
        if (counterInstance(instance, (Instance)enu.nextElement())) {
          return true;
        }
      }
      return false;
    }
    
    Iterator iter = enumerateLiterals();
    while (iter.hasNext()) {
      Literal current = (Literal)iter.next();
      if (!canKeep(instance, current)) {
        return false;
      }
    }
    return true;
  }
  
  public abstract boolean canKeep(Instance paramInstance, Literal paramLiteral);
  
  public abstract boolean isIncludedIn(Rule paramRule);
  
  public abstract String toString();
}

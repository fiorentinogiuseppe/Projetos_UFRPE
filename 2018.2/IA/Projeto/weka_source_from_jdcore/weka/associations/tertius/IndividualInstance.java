package weka.associations.tertius;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
































public class IndividualInstance
  extends Instance
{
  private static final long serialVersionUID = -7903938733476585114L;
  private Instances m_parts;
  
  public IndividualInstance(Instance individual, Instances parts)
  {
    super(individual);
    m_parts = parts;
  }
  
  public IndividualInstance(IndividualInstance instance)
  {
    super(instance);
    m_parts = m_parts;
  }
  
  public Object copy()
  {
    IndividualInstance result = new IndividualInstance(this);
    m_Dataset = m_Dataset;
    return result;
  }
  
  public Instances getParts()
  {
    return m_parts;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

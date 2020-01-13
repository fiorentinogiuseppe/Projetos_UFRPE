package weka.associations.tertius;

import java.util.Iterator;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;













































public class Body
  extends LiteralSet
{
  private static final long serialVersionUID = 4870689270432218016L;
  
  public Body() {}
  
  public Body(Instances instances)
  {
    super(instances);
  }
  









  public boolean canKeep(Instance instance, Literal newLit)
  {
    return newLit.satisfies(instance);
  }
  





  public boolean isIncludedIn(Rule otherRule)
  {
    Iterator iter = enumerateLiterals();
    while (iter.hasNext()) {
      Literal current = (Literal)iter.next();
      if ((!otherRule.bodyContains(current)) && (!otherRule.headContains(current.getNegation())))
      {
        return false;
      }
    }
    return true;
  }
  


  public String toString()
  {
    Iterator iter = enumerateLiterals();
    
    if (!iter.hasNext()) {
      return "TRUE";
    }
    StringBuffer text = new StringBuffer();
    while (iter.hasNext()) {
      text.append(iter.next().toString());
      if (iter.hasNext()) {
        text.append(" and ");
      }
    }
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}

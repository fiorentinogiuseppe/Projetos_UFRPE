package weka.associations.tertius;

import java.util.Iterator;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;













































public class Head
  extends LiteralSet
{
  private static final long serialVersionUID = 5068076274253706199L;
  
  public Head() {}
  
  public Head(Instances instances)
  {
    super(instances);
  }
  








  public boolean canKeep(Instance instance, Literal newLit)
  {
    return newLit.negationSatisfies(instance);
  }
  



  public boolean isIncludedIn(Rule otherRule)
  {
    Iterator iter = enumerateLiterals();
    while (iter.hasNext()) {
      Literal current = (Literal)iter.next();
      if ((!otherRule.headContains(current)) && (!otherRule.bodyContains(current.getNegation())))
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
      return "FALSE";
    }
    
    StringBuffer text = new StringBuffer();
    while (iter.hasNext()) {
      text.append(iter.next().toString());
      if (iter.hasNext()) {
        text.append(" or ");
      }
    }
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}

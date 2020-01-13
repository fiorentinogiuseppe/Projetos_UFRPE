package weka.classifiers.trees.m5;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;





































public final class Values
  implements RevisionHandler
{
  int numInstances;
  int missingInstances;
  int first;
  int last;
  int attr;
  double sum;
  double sqrSum;
  double va;
  double sd;
  
  public Values(int low, int high, int attribute, Instances inst)
  {
    int count = 0;
    

    numInstances = (high - low + 1);
    missingInstances = 0;
    first = low;
    last = high;
    attr = attribute;
    sum = 0.0D;
    sqrSum = 0.0D;
    for (int i = first; i <= last; i++) {
      if (!inst.instance(i).isMissing(attr)) {
        count++;
        double value = inst.instance(i).value(attr);
        sum += value;
        sqrSum += value * value;
      }
      
      if (count > 1) {
        va = ((sqrSum - sum * sum / count) / count);
        va = Math.abs(va);
        sd = Math.sqrt(va);
      } else {
        va = 0.0D;sd = 0.0D;
      }
    }
  }
  



  public final String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append("Print statistic values of instances (" + first + "-" + last + "\n");
    
    text.append("    Number of instances:\t" + numInstances + "\n");
    text.append("    NUmber of instances with unknowns:\t" + missingInstances + "\n");
    
    text.append("    Attribute:\t\t\t:" + attr + "\n");
    text.append("    Sum:\t\t\t" + sum + "\n");
    text.append("    Squared sum:\t\t" + sqrSum + "\n");
    text.append("    Stanard Deviation:\t\t" + sd + "\n");
    
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}

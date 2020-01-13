package weka.classifiers.trees.m5;

import java.io.Serializable;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;




































public final class YongSplitInfo
  implements Cloneable, Serializable, SplitEvaluate, RevisionHandler
{
  private static final long serialVersionUID = 1864267581079767881L;
  private int number;
  private int first;
  private int last;
  private int position;
  private double maxImpurity;
  private double leftAve;
  private double rightAve;
  private int splitAttr;
  private double splitValue;
  
  public YongSplitInfo(int low, int high, int attr)
  {
    number = (high - low + 1);
    first = low;
    last = high;
    position = -1;
    maxImpurity = -1.0E20D;
    splitAttr = attr;
    splitValue = 0.0D;
    Utils.SMALL = 1.0E-10D;
  }
  


  public final SplitEvaluate copy()
    throws Exception
  {
    YongSplitInfo s = (YongSplitInfo)clone();
    
    return s;
  }
  






  public final void initialize(int low, int high, int attr)
  {
    number = (high - low + 1);
    first = low;
    last = high;
    position = -1;
    maxImpurity = -1.0E20D;
    splitAttr = attr;
    splitValue = 0.0D;
  }
  




  public final String toString(Instances inst)
  {
    StringBuffer text = new StringBuffer();
    
    text.append("Print SplitInfo:\n");
    text.append("    Instances:\t\t" + number + " (" + first + "-" + position + "," + (position + 1) + "-" + last + ")\n");
    
    text.append("    Maximum Impurity Reduction:\t" + Utils.doubleToString(maxImpurity, 1, 4) + "\n");
    
    text.append("    Left average:\t" + leftAve + "\n");
    text.append("    Right average:\t" + rightAve + "\n");
    if (maxImpurity > 0.0D) {
      text.append("    Splitting function:\t" + inst.attribute(splitAttr).name() + " = " + splitValue + "\n");
    }
    else {
      text.append("    Splitting function:\tnull\n");
    }
    return text.toString();
  }
  







  public final void attrSplit(int attr, Instances inst)
    throws Exception
  {
    int low = 0;
    int high = inst.numInstances() - 1;
    initialize(low, high, attr);
    if (number < 4) {
      return;
    }
    
    int len = high - low + 1 < 5 ? 1 : (high - low + 1) / 5;
    
    position = low;
    
    int part = low + len - 1;
    Impurity imp = new Impurity(part, attr, inst, 5);
    
    int count = 0;
    for (int i = low + len; i <= high - len - 1; i++)
    {
      imp.incremental(inst.instance(i).classValue(), 1);
      
      if (!Utils.eq(inst.instance(i + 1).value(attr), inst.instance(i).value(attr)))
      {
        count = i;
        if (impurity > maxImpurity) {
          maxImpurity = impurity;
          splitValue = ((inst.instance(i).value(attr) + inst.instance(i + 1).value(attr)) * 0.5D);
          
          leftAve = (sl / nl);
          rightAve = (sr / nr);
          position = i;
        }
      }
    }
  }
  




  public double maxImpurity()
  {
    return maxImpurity;
  }
  




  public int splitAttr()
  {
    return splitAttr;
  }
  





  public int position()
  {
    return position;
  }
  




  public double splitValue()
  {
    return splitValue;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

package weka.classifiers.trees.m5;

import java.io.PrintStream;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;




































public final class Impurity
  implements RevisionHandler
{
  double n;
  int attr;
  double nl;
  double nr;
  double sl;
  double sr;
  double s2l;
  double s2r;
  double sdl;
  double sdr;
  double vl;
  double vr;
  double sd;
  double va;
  double impurity;
  int order;
  
  public Impurity(int partition, int attribute, Instances inst, int k)
  {
    Values values = new Values(0, inst.numInstances() - 1, inst.classIndex(), inst);
    attr = attribute;
    n = inst.numInstances();
    sd = sd;
    va = va;
    
    values = new Values(0, partition, inst.classIndex(), inst);
    nl = (partition + 1);
    sl = sum;
    s2l = sqrSum;
    
    values = new Values(partition + 1, inst.numInstances() - 1, inst.classIndex(), inst);
    nr = (inst.numInstances() - partition - 1);
    sr = sum;
    s2r = sqrSum;
    
    order = k;
    incremental(0.0D, 0);
  }
  




  public final String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append("Print impurity values:\n");
    text.append("    Number of total instances:\t" + n + "\n");
    text.append("    Splitting attribute:\t\t" + attr + "\n");
    text.append("    Number of the instances in the left:\t" + nl + "\n");
    text.append("    Number of the instances in the right:\t" + nr + "\n");
    text.append("    Sum of the left:\t\t\t" + sl + "\n");
    text.append("    Sum of the right:\t\t\t" + sr + "\n");
    text.append("    Squared sum of the left:\t\t" + s2l + "\n");
    text.append("    Squared sum of the right:\t\t" + s2r + "\n");
    text.append("    Standard deviation of the left:\t" + sdl + "\n");
    text.append("    Standard deviation of the right:\t" + sdr + "\n");
    text.append("    Variance of the left:\t\t" + vr + "\n");
    text.append("    Variance of the right:\t\t" + vr + "\n");
    text.append("    Overall standard deviation:\t\t" + sd + "\n");
    text.append("    Overall variance:\t\t\t" + va + "\n");
    text.append("    Impurity (order " + order + "):\t\t" + impurity + "\n");
    
    return text.toString();
  }
  




  public final void incremental(double value, int type)
  {
    double y = 0.0D;double yl = 0.0D;double yr = 0.0D;
    
    switch (type) {
    case 1: 
      nl += 1.0D;
      nr -= 1.0D;
      sl += value;
      sr -= value;
      s2l += value * value;
      s2r -= value * value;
      break;
    case -1: 
      nl -= 1.0D;
      nr += 1.0D;
      sl -= value;
      sr += value;
      s2l -= value * value;
      s2r += value * value;
      break;
    case 0: 
      break;
    default:  System.err.println("wrong type in Impurity.incremental().");
    }
    
    if (nl <= 0.0D) {
      vl = 0.0D;
      sdl = 0.0D;
    }
    else {
      vl = ((nl * s2l - sl * sl) / (nl * nl));
      vl = Math.abs(vl);
      sdl = Math.sqrt(vl);
    }
    if (nr <= 0.0D) {
      vr = 0.0D;
      sdr = 0.0D;
    }
    else {
      vr = ((nr * s2r - sr * sr) / (nr * nr));
      vr = Math.abs(vr);
      sdr = Math.sqrt(vr);
    }
    
    if (order <= 0) { System.err.println("Impurity order less than zero in Impurity.incremental()");
    } else if (order == 1) {
      y = va;yl = vl;yr = vr;
    } else {
      y = Math.pow(va, 1.0D / order);
      yl = Math.pow(vl, 1.0D / order);
      yr = Math.pow(vr, 1.0D / order);
    }
    
    if ((nl <= 0.0D) || (nr <= 0.0D)) {
      impurity = 0.0D;
    } else {
      impurity = (y - nl / n * yl - nr / n * yr);
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
}

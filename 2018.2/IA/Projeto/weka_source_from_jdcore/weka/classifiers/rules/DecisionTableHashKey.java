package weka.classifiers.rules;

import java.io.PrintStream;
import java.io.Serializable;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;







































public class DecisionTableHashKey
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = 5674163500154964602L;
  private double[] attributes;
  private boolean[] missing;
  private int key;
  
  public DecisionTableHashKey(Instance t, int numAtts, boolean ignoreClass)
    throws Exception
  {
    int cindex = t.classIndex();
    
    key = 64537;
    attributes = new double[numAtts];
    missing = new boolean[numAtts];
    for (int i = 0; i < numAtts; i++) {
      if ((i == cindex) && (!ignoreClass)) {
        missing[i] = true;
      }
      else if (!(missing[i] = t.isMissing(i))) {
        attributes[i] = t.value(i);
      }
    }
  }
  









  public String toString(Instances t, int maxColWidth)
  {
    int cindex = t.classIndex();
    StringBuffer text = new StringBuffer();
    
    for (int i = 0; i < attributes.length; i++) {
      if (i != cindex) {
        if (missing[i] != 0) {
          text.append("?");
          for (int j = 0; j < maxColWidth; j++) {
            text.append(" ");
          }
        } else {
          String ss = t.attribute(i).value((int)attributes[i]);
          StringBuffer sb = new StringBuffer(ss);
          
          for (int j = 0; j < maxColWidth - ss.length() + 1; j++) {
            sb.append(" ");
          }
          text.append(sb);
        }
      }
    }
    return text.toString();
  }
  






  public DecisionTableHashKey(double[] t)
  {
    int l = t.length;
    
    key = 64537;
    attributes = new double[l];
    missing = new boolean[l];
    for (int i = 0; i < l; i++) {
      if (t[i] == Double.MAX_VALUE) {
        missing[i] = true;
      } else {
        missing[i] = false;
        attributes[i] = t[i];
      }
    }
  }
  





  public int hashCode()
  {
    int hv = 0;
    
    if (key != 64537)
      return key;
    for (int i = 0; i < attributes.length; i++) {
      if (missing[i] != 0) {
        hv += i * 13;
      } else {
        hv = (int)(hv + i * 5 * (attributes[i] + 1.0D));
      }
    }
    if (key == 64537) {
      key = hv;
    }
    return hv;
  }
  






  public boolean equals(Object b)
  {
    if ((b == null) || (!b.getClass().equals(getClass()))) {
      return false;
    }
    boolean ok = true;
    
    if ((b instanceof DecisionTableHashKey)) {
      DecisionTableHashKey n = (DecisionTableHashKey)b;
      for (int i = 0; i < attributes.length; i++) {
        boolean l = missing[i];
        if ((missing[i] != 0) || (l)) {
          if (((missing[i] != 0) && (!l)) || ((missing[i] == 0) && (l))) {
            ok = false;
            break;
          }
        }
        else if (attributes[i] != attributes[i]) {
          ok = false;
          break;
        }
      }
    }
    else {
      return false;
    }
    return ok;
  }
  


  public void print_hash_code()
  {
    System.out.println("Hash val: " + hashCode());
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}

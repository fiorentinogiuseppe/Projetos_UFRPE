package weka.classifiers.evaluation;

import weka.classifiers.CostMatrix;
import weka.core.FastVector;
import weka.core.Matrix;
import weka.core.RevisionUtils;
import weka.core.Utils;





































public class ConfusionMatrix
  extends Matrix
{
  private static final long serialVersionUID = -181789981401504090L;
  protected String[] m_ClassNames;
  
  public ConfusionMatrix(String[] classNames)
  {
    super(classNames.length, classNames.length);
    m_ClassNames = ((String[])classNames.clone());
  }
  









  public ConfusionMatrix makeWeighted(CostMatrix costs)
    throws Exception
  {
    if (costs.size() != size()) {
      throw new Exception("Cost and confusion matrices must be the same size");
    }
    ConfusionMatrix weighted = new ConfusionMatrix(m_ClassNames);
    for (int row = 0; row < size(); row++) {
      for (int col = 0; col < size(); col++) {
        weighted.setElement(row, col, getElement(row, col) * costs.getElement(row, col));
      }
    }
    
    return weighted;
  }
  






  public Object clone()
  {
    ConfusionMatrix m = (ConfusionMatrix)super.clone();
    m_ClassNames = ((String[])m_ClassNames.clone());
    return m;
  }
  





  public int size()
  {
    return m_ClassNames.length;
  }
  






  public String className(int index)
  {
    return m_ClassNames[index];
  }
  






  public void addPrediction(NominalPrediction pred)
    throws Exception
  {
    if (pred.predicted() == NominalPrediction.MISSING_VALUE) {
      throw new Exception("No predicted value given.");
    }
    if (pred.actual() == NominalPrediction.MISSING_VALUE) {
      throw new Exception("No actual value given.");
    }
    addElement((int)pred.actual(), (int)pred.predicted(), pred.weight());
  }
  







  public void addPredictions(FastVector predictions)
    throws Exception
  {
    for (int i = 0; i < predictions.size(); i++) {
      addPrediction((NominalPrediction)predictions.elementAt(i));
    }
  }
  








  public TwoClassStats getTwoClassStats(int classIndex)
  {
    double fp = 0.0D;double tp = 0.0D;double fn = 0.0D;double tn = 0.0D;
    for (int row = 0; row < size(); row++) {
      for (int col = 0; col < size(); col++) {
        if (row == classIndex) {
          if (col == classIndex) {
            tp += getElement(row, col);
          } else {
            fn += getElement(row, col);
          }
        }
        else if (col == classIndex) {
          fp += getElement(row, col);
        } else {
          tn += getElement(row, col);
        }
      }
    }
    
    return new TwoClassStats(tp, fp, tn, fn);
  }
  







  public double correct()
  {
    double correct = 0.0D;
    for (int i = 0; i < size(); i++) {
      correct += getElement(i, i);
    }
    return correct;
  }
  







  public double incorrect()
  {
    double incorrect = 0.0D;
    for (int row = 0; row < size(); row++) {
      for (int col = 0; col < size(); col++) {
        if (row != col) {
          incorrect += getElement(row, col);
        }
      }
    }
    return incorrect;
  }
  







  public double total()
  {
    double total = 0.0D;
    for (int row = 0; row < size(); row++) {
      for (int col = 0; col < size(); col++) {
        total += getElement(row, col);
      }
    }
    return total;
  }
  





  public double errorRate()
  {
    return incorrect() / total();
  }
  





  public String toString()
  {
    return toString("=== Confusion Matrix ===\n");
  }
  








  public String toString(String title)
  {
    StringBuffer text = new StringBuffer();
    char[] IDChars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    


    boolean fractional = false;
    


    double maxval = 0.0D;
    for (int i = 0; i < size(); i++) {
      for (int j = 0; j < size(); j++) {
        double current = getElement(i, j);
        if (current < 0.0D) {
          current *= -10.0D;
        }
        if (current > maxval) {
          maxval = current;
        }
        double fract = current - Math.rint(current);
        if ((!fractional) && (Math.log(fract) / Math.log(10.0D) >= -2.0D))
        {
          fractional = true;
        }
      }
    }
    
    int IDWidth = 1 + Math.max((int)(Math.log(maxval) / Math.log(10.0D) + (fractional ? 3 : 0)), (int)(Math.log(size()) / Math.log(IDChars.length)));
    


    text.append(title).append("\n");
    for (int i = 0; i < size(); i++) {
      if (fractional) {
        text.append(" ").append(num2ShortID(i, IDChars, IDWidth - 3)).append("   ");
      }
      else {
        text.append(" ").append(num2ShortID(i, IDChars, IDWidth));
      }
    }
    text.append("     actual class\n");
    for (int i = 0; i < size(); i++) {
      for (int j = 0; j < size(); j++) {
        text.append(" ").append(Utils.doubleToString(getElement(i, j), IDWidth, fractional ? 2 : 0));
      }
      


      text.append(" | ").append(num2ShortID(i, IDChars, IDWidth)).append(" = ").append(m_ClassNames[i]).append("\n");
    }
    
    return text.toString();
  }
  






  private static String num2ShortID(int num, char[] IDChars, int IDWidth)
  {
    char[] ID = new char[IDWidth];
    

    for (int i = IDWidth - 1; i >= 0; i--) {
      ID[i] = IDChars[(num % IDChars.length)];
      num = num / IDChars.length - 1;
      if (num < 0) {
        break;
      }
    }
    for (i--; i >= 0; i--) {
      ID[i] = ' ';
    }
    
    return new String(ID);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
}

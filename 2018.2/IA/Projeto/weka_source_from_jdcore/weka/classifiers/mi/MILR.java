package weka.classifiers.mi;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.core.Optimization;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;










































public class MILR
  extends Classifier
  implements OptionHandler, MultiInstanceCapabilitiesHandler
{
  static final long serialVersionUID = 1996101190172373826L;
  protected double[] m_Par;
  protected int m_NumClasses;
  protected double m_Ridge;
  protected int[] m_Classes;
  protected double[][][] m_Data;
  protected Instances m_Attributes;
  protected double[] xMean;
  protected double[] xSD;
  protected int m_AlgorithmType;
  public static final int ALGORITHMTYPE_DEFAULT = 0;
  public static final int ALGORITHMTYPE_ARITHMETIC = 1;
  public static final int ALGORITHMTYPE_GEOMETRIC = 2;
  
  public MILR()
  {
    m_Ridge = 1.0E-6D;
    









    xMean = null;xSD = null;
    

    m_AlgorithmType = 0;
  }
  






  public static final Tag[] TAGS_ALGORITHMTYPE = { new Tag(0, "standard MI assumption"), new Tag(1, "collective MI assumption, arithmetic mean for posteriors"), new Tag(2, "collective MI assumption, geometric mean for posteriors") };
  









  public String globalInfo()
  {
    return "Uses either standard or collective multi-instance assumption, but within linear regression. For the collective assumption, it offers arithmetic or geometric mean for the posteriors.";
  }
  







  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tTurn on debugging output.", "D", 0, "-D"));
    


    result.addElement(new Option("\tSet the ridge in the log-likelihood.", "R", 1, "-R <ridge>"));
    


    result.addElement(new Option("\tDefines the type of algorithm:\n\t 0. standard MI assumption\n\t 1. collective MI assumption, arithmetic mean for posteriors\n\t 2. collective MI assumption, geometric mean for posteriors", "A", 1, "-A [0|1|2]"));
    





    return result.elements();
  }
  






  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    String tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0) {
      setRidge(Double.parseDouble(tmpStr));
    } else {
      setRidge(1.0E-6D);
    }
    tmpStr = Utils.getOption('A', options);
    if (tmpStr.length() != 0) {
      setAlgorithmType(new SelectedTag(Integer.parseInt(tmpStr), TAGS_ALGORITHMTYPE));
    } else {
      setAlgorithmType(new SelectedTag(0, TAGS_ALGORITHMTYPE));
    }
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getDebug()) {
      result.add("-D");
    }
    result.add("-R");
    result.add("" + getRidge());
    
    result.add("-A");
    result.add("" + m_AlgorithmType);
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String ridgeTipText()
  {
    return "The ridge in the log-likelihood.";
  }
  




  public void setRidge(double ridge)
  {
    m_Ridge = ridge;
  }
  




  public double getRidge()
  {
    return m_Ridge;
  }
  





  public String algorithmTypeTipText()
  {
    return "The mean type for the posteriors.";
  }
  




  public SelectedTag getAlgorithmType()
  {
    return new SelectedTag(m_AlgorithmType, TAGS_ALGORITHMTYPE);
  }
  




  public void setAlgorithmType(SelectedTag newType)
  {
    if (newType.getTags() == TAGS_ALGORITHMTYPE) {
      m_AlgorithmType = newType.getSelectedTag().getID();
    }
  }
  





  private class OptEng
    extends Optimization
  {
    private int m_Type;
    




    public OptEng(int type)
    {
      m_Type = type;
    }
    




    protected double objectiveFunction(double[] x)
    {
      double nll = 0.0D;
      
      switch (m_Type) {
      case 0: 
        for (int i = 0; i < m_Classes.length; i++) {
          int nI = m_Data[i][0].length;
          double bag = 0.0D;
          double prod = 0.0D;
          
          for (int j = 0; j < nI; j++) {
            double exp = 0.0D;
            for (int k = m_Data[i].length - 1; k >= 0; k--)
              exp += m_Data[i][k][j] * x[(k + 1)];
            exp += x[0];
            exp = Math.exp(exp);
            
            if (m_Classes[i] == 1) {
              prod -= Math.log(1.0D + exp);
            } else {
              bag += Math.log(1.0D + exp);
            }
          }
          if (m_Classes[i] == 1) {
            bag = -Math.log(1.0D - Math.exp(prod));
          }
          nll += bag;
        }
        break;
      
      case 1: 
        for (int i = 0; i < m_Classes.length; i++) {
          int nI = m_Data[i][0].length;
          double bag = 0.0D;
          
          for (int j = 0; j < nI; j++) {
            double exp = 0.0D;
            for (int k = m_Data[i].length - 1; k >= 0; k--)
              exp += m_Data[i][k][j] * x[(k + 1)];
            exp += x[0];
            exp = Math.exp(exp);
            
            if (m_Classes[i] == 1) {
              bag += 1.0D - 1.0D / (1.0D + exp);
            } else
              bag += 1.0D / (1.0D + exp);
          }
          bag /= nI;
          
          nll -= Math.log(bag);
        }
        break;
      
      case 2: 
        for (int i = 0; i < m_Classes.length; i++) {
          int nI = m_Data[i][0].length;
          double bag = 0.0D;
          
          for (int j = 0; j < nI; j++) {
            double exp = 0.0D;
            for (int k = m_Data[i].length - 1; k >= 0; k--)
              exp += m_Data[i][k][j] * x[(k + 1)];
            exp += x[0];
            
            if (m_Classes[i] == 1) {
              bag -= exp / nI;
            } else {
              bag += exp / nI;
            }
          }
          nll += Math.log(1.0D + Math.exp(bag));
        }
      }
      
      

      for (int r = 1; r < x.length; r++) {
        nll += m_Ridge * x[r] * x[r];
      }
      return nll;
    }
    




    protected double[] evaluateGradient(double[] x)
    {
      double[] grad = new double[x.length];
      
      switch (m_Type) {
      case 0: 
        for (int i = 0; i < m_Classes.length; i++) {
          int nI = m_Data[i][0].length;
          
          double denom = 0.0D;
          double[] bag = new double[grad.length];
          
          for (int j = 0; j < nI; j++)
          {
            double exp = 0.0D;
            for (int k = m_Data[i].length - 1; k >= 0; k--)
              exp += m_Data[i][k][j] * x[(k + 1)];
            exp += x[0];
            exp = Math.exp(exp) / (1.0D + Math.exp(exp));
            
            if (m_Classes[i] == 1)
            {

              denom -= Math.log(1.0D - exp);
            }
            
            for (int p = 0; p < x.length; p++) {
              double m = 1.0D;
              if (p > 0) m = m_Data[i][(p - 1)][j];
              bag[p] += m * exp;
            }
          }
          
          denom = Math.exp(denom);
          

          for (int q = 0; q < grad.length; q++) {
            if (m_Classes[i] == 1) {
              grad[q] -= bag[q] / (denom - 1.0D);
            } else
              grad[q] += bag[q];
          }
        }
        break;
      
      case 1: 
        for (int i = 0; i < m_Classes.length; i++) {
          int nI = m_Data[i][0].length;
          
          double denom = 0.0D;
          double[] numrt = new double[x.length];
          
          for (int j = 0; j < nI; j++)
          {
            double exp = 0.0D;
            for (int k = m_Data[i].length - 1; k >= 0; k--)
              exp += m_Data[i][k][j] * x[(k + 1)];
            exp += x[0];
            exp = Math.exp(exp);
            if (m_Classes[i] == 1) {
              denom += exp / (1.0D + exp);
            } else {
              denom += 1.0D / (1.0D + exp);
            }
            
            for (int p = 0; p < x.length; p++) {
              double m = 1.0D;
              if (p > 0) m = m_Data[i][(p - 1)][j];
              numrt[p] += m * exp / ((1.0D + exp) * (1.0D + exp));
            }
          }
          

          for (int q = 0; q < grad.length; q++) {
            if (m_Classes[i] == 1) {
              grad[q] -= numrt[q] / denom;
            } else
              grad[q] += numrt[q] / denom;
          }
        }
        break;
      
      case 2: 
        for (int i = 0; i < m_Classes.length; i++) {
          int nI = m_Data[i][0].length;
          double bag = 0.0D;
          double[] sumX = new double[x.length];
          for (int j = 0; j < nI; j++)
          {
            double exp = 0.0D;
            for (int k = m_Data[i].length - 1; k >= 0; k--)
              exp += m_Data[i][k][j] * x[(k + 1)];
            exp += x[0];
            
            if (m_Classes[i] == 1) {
              bag -= exp / nI;
              for (int q = 0; q < grad.length; q++) {
                double m = 1.0D;
                if (q > 0) m = m_Data[i][(q - 1)][j];
                sumX[q] -= m / nI;
              }
            }
            else {
              bag += exp / nI;
              for (int q = 0; q < grad.length; q++) {
                double m = 1.0D;
                if (q > 0) m = m_Data[i][(q - 1)][j];
                sumX[q] += m / nI;
              }
            }
          }
          
          for (int p = 0; p < x.length; p++) {
            grad[p] += Math.exp(bag) * sumX[p] / (1.0D + Math.exp(bag));
          }
        }
      }
      
      
      for (int r = 1; r < x.length; r++) {
        grad[r] += 2.0D * m_Ridge * x[r];
      }
      
      return grad;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9144 $");
    }
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.BINARY_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    
    return result;
  }
  






  public Capabilities getMultiInstanceCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.disableAllClasses();
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  






  public void buildClassifier(Instances train)
    throws Exception
  {
    getCapabilities().testWithFail(train);
    

    train = new Instances(train);
    train.deleteWithMissingClass();
    
    m_NumClasses = train.numClasses();
    
    int nR = train.attribute(1).relation().numAttributes();
    int nC = train.numInstances();
    
    m_Data = new double[nC][nR][];
    m_Classes = new int[nC];
    m_Attributes = train.attribute(1).relation();
    
    xMean = new double[nR];
    xSD = new double[nR];
    
    double sY1 = 0.0D;double sY0 = 0.0D;double totIns = 0.0D;
    int[] missingbags = new int[nR];
    
    if (m_Debug) {
      System.out.println("Extracting data...");
    }
    
    for (int h = 0; h < m_Data.length; h++) {
      Instance current = train.instance(h);
      m_Classes[h] = ((int)current.classValue());
      Instances currInsts = current.relationalValue(1);
      int nI = currInsts.numInstances();
      totIns += nI;
      
      for (int i = 0; i < nR; i++)
      {
        m_Data[h][i] = new double[nI];
        double avg = 0.0D;double std = 0.0D;double num = 0.0D;
        for (int k = 0; k < nI; k++) {
          if (!currInsts.instance(k).isMissing(i)) {
            m_Data[h][i][k] = currInsts.instance(k).value(i);
            avg += m_Data[h][i][k];
            std += m_Data[h][i][k] * m_Data[h][i][k];
            num += 1.0D;
          }
          else {
            m_Data[h][i][k] = NaN.0D;
          }
        }
        if (num > 0.0D) {
          xMean[i] += avg / num;
          xSD[i] += std / num;
        }
        else {
          missingbags[i] += 1;
        }
      }
      
      if (m_Classes[h] == 1) {
        sY1 += 1.0D;
      } else {
        sY0 += 1.0D;
      }
    }
    for (int j = 0; j < nR; j++) {
      xMean[j] /= (nC - missingbags[j]);
      xSD[j] = Math.sqrt(Math.abs(xSD[j] / (nC - missingbags[j] - 1.0D) - xMean[j] * xMean[j] * (nC - missingbags[j]) / (nC - missingbags[j] - 1.0D)));
    }
    


    if (m_Debug)
    {
      System.out.println("Descriptives...");
      System.out.println(sY0 + " bags have class 0 and " + sY1 + " bags have class 1");
      
      System.out.println("\n Variable     Avg       SD    ");
      for (int j = 0; j < nR; j++) {
        System.out.println(Utils.doubleToString(j, 8, 4) + Utils.doubleToString(xMean[j], 10, 4) + Utils.doubleToString(xSD[j], 10, 4));
      }
    }
    


    for (int i = 0; i < nC; i++) {
      for (int j = 0; j < nR; j++) {
        for (int k = 0; k < m_Data[i][j].length; k++) {
          if (xSD[j] != 0.0D) {
            if (!Double.isNaN(m_Data[i][j][k])) {
              m_Data[i][j][k] = ((m_Data[i][j][k] - xMean[j]) / xSD[j]);
            } else {
              m_Data[i][j][k] = 0.0D;
            }
          }
        }
      }
    }
    if (m_Debug) {
      System.out.println("\nIteration History...");
    }
    
    double[] x = new double[nR + 1];
    x[0] = Math.log((sY1 + 1.0D) / (sY0 + 1.0D));
    double[][] b = new double[2][x.length];
    b[0][0] = NaN.0D;
    b[1][0] = NaN.0D;
    for (int q = 1; q < x.length; q++) {
      x[q] = 0.0D;
      b[0][q] = NaN.0D;
      b[1][q] = NaN.0D;
    }
    
    OptEng opt = new OptEng(m_AlgorithmType);
    opt.setDebug(m_Debug);
    m_Par = opt.findArgmin(x, b);
    while (m_Par == null) {
      m_Par = opt.getVarbValues();
      if (m_Debug)
        System.out.println("200 iterations finished, not enough!");
      m_Par = opt.findArgmin(m_Par, b);
    }
    if (m_Debug) {
      System.out.println(" -------------<Converged>--------------");
    }
    
    if (m_AlgorithmType == 1) {
      double[] fs = new double[nR];
      for (int k = 1; k < nR + 1; k++)
        fs[(k - 1)] = Math.abs(m_Par[k]);
      int[] idx = Utils.sort(fs);
      double max = fs[idx[(idx.length - 1)]];
      for (int k = idx.length - 1; k >= 0; k--) {
        System.out.println(m_Attributes.attribute(idx[k]).name() + "\t" + fs[idx[k]] * 100.0D / max);
      }
    }
    
    for (int j = 1; j < nR + 1; j++) {
      if (xSD[(j - 1)] != 0.0D) {
        m_Par[j] /= xSD[(j - 1)];
        m_Par[0] -= m_Par[j] * xMean[(j - 1)];
      }
    }
  }
  








  public double[] distributionForInstance(Instance exmp)
    throws Exception
  {
    Instances ins = exmp.relationalValue(1);
    int nI = ins.numInstances();int nA = ins.numAttributes();
    double[][] dat = new double[nI][nA + 1];
    for (int j = 0; j < nI; j++) {
      dat[j][0] = 1.0D;
      int idx = 1;
      for (int k = 0; k < nA; k++) {
        if (!ins.instance(j).isMissing(k)) {
          dat[j][idx] = ins.instance(j).value(k);
        } else
          dat[j][idx] = xMean[(idx - 1)];
        idx++;
      }
    }
    

    double[] distribution = new double[2];
    switch (m_AlgorithmType) {
    case 0: 
      distribution[0] = 0.0D;
      
      for (int i = 0; i < nI; i++) {
        double exp = 0.0D;
        for (int r = 0; r < m_Par.length; r++)
          exp += m_Par[r] * dat[i][r];
        exp = Math.exp(exp);
        

        distribution[0] -= Math.log(1.0D + exp);
      }
      

      distribution[0] = Math.exp(distribution[0]);
      
      distribution[1] = (1.0D - distribution[0]);
      break;
    
    case 1: 
      distribution[0] = 0.0D;
      
      for (int i = 0; i < nI; i++) {
        double exp = 0.0D;
        for (int r = 0; r < m_Par.length; r++)
          exp += m_Par[r] * dat[i][r];
        exp = Math.exp(exp);
        

        distribution[0] += 1.0D / (1.0D + exp);
      }
      

      distribution[0] /= nI;
      
      distribution[1] = (1.0D - distribution[0]);
      break;
    
    case 2: 
      for (int i = 0; i < nI; i++) {
        double exp = 0.0D;
        for (int r = 0; r < m_Par.length; r++)
          exp += m_Par[r] * dat[i][r];
        distribution[1] += exp / nI;
      }
      

      distribution[1] = (1.0D / (1.0D + Math.exp(-distribution[1])));
      
      distribution[0] = (1.0D - distribution[1]);
    }
    
    
    return distribution;
  }
  





  public String toString()
  {
    String result = "Modified Logistic Regression";
    if (m_Par == null) {
      return result + ": No model built yet.";
    }
    
    result = result + "\nMean type: " + getAlgorithmType().getSelectedTag().getReadable() + "\n";
    result = result + "\nCoefficients...\nVariable      Coeff.\n";
    
    int j = 1; for (int idx = 0; j < m_Par.length; idx++) {
      result = result + m_Attributes.attribute(idx).name();
      result = result + " " + Utils.doubleToString(m_Par[j], 12, 4);
      result = result + "\n";j++;
    }
    


    result = result + "Intercept:";
    result = result + " " + Utils.doubleToString(m_Par[0], 10, 4);
    result = result + "\n";
    
    result = result + "\nOdds Ratios...\nVariable         O.R.\n";
    
    int j = 1; for (int idx = 0; j < m_Par.length; idx++) {
      result = result + " " + m_Attributes.attribute(idx).name();
      double ORc = Math.exp(m_Par[j]);
      result = result + " " + (ORc > 1.0E10D ? "" + ORc : Utils.doubleToString(ORc, 12, 4));j++;
    }
    

    result = result + "\n";
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9144 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new MILR(), argv);
  }
}

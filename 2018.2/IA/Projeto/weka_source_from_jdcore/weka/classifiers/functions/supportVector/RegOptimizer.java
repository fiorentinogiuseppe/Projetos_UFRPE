package weka.classifiers.functions.supportVector;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.functions.SMOreg;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;

































































public class RegOptimizer
  implements OptionHandler, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -2198266997254461814L;
  public double[] m_alpha;
  public double[] m_alphaStar;
  protected double m_b;
  protected double m_epsilon = 0.001D;
  

  protected double m_C = 1.0D;
  

  protected double[] m_target;
  

  protected Instances m_data;
  

  protected Kernel m_kernel;
  

  protected int m_classIndex = -1;
  

  protected int m_nInstances = -1;
  

  protected Random m_random;
  

  protected int m_nSeed = 1;
  

  protected SMOset m_supportVectors;
  

  protected long m_nEvals = 0L;
  

  protected int m_nCacheHits = -1;
  

  protected double[] m_weights;
  

  protected double[] m_sparseWeights;
  

  protected int[] m_sparseIndices;
  
  protected boolean m_bModelBuilt = false;
  

  protected SMOreg m_SVM = null;
  



  public RegOptimizer()
  {
    m_random = new Random(m_nSeed);
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe epsilon parameter in epsilon-insensitive loss function.\n\t(default 1.0e-3)", "L", 1, "-L <double>"));
    







    result.addElement(new Option("\tThe random number seed.\n\t(default 1)", "W", 1, "-W <double>"));
    



    return result.elements();
  }
  



















  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('L', options);
    if (tmpStr.length() != 0) {
      setEpsilonParameter(Double.parseDouble(tmpStr));
    } else {
      setEpsilonParameter(0.001D);
    }
    








    tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() != 0) {
      setSeed(Integer.parseInt(tmpStr));
    } else {
      setSeed(1);
    }
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-L");
    result.add("" + getEpsilonParameter());
    
    result.add("-W");
    result.add("" + getSeed());
    



    return (String[])result.toArray(new String[result.size()]);
  }
  




  public boolean modelBuilt()
  {
    return m_bModelBuilt;
  }
  




  public void setSMOReg(SMOreg value)
  {
    m_SVM = value;
  }
  




  public long getKernelEvaluations()
  {
    return m_nEvals;
  }
  




  public int getCacheHits()
  {
    return m_nCacheHits;
  }
  




  protected void init(Instances data)
    throws Exception
  {
    if (m_SVM == null) {
      throw new Exception("SVM not initialized in optimizer. Use RegOptimizer.setSVMReg()");
    }
    m_C = m_SVM.getC();
    m_data = data;
    m_classIndex = data.classIndex();
    m_nInstances = data.numInstances();
    

    m_kernel = Kernel.makeCopy(m_SVM.getKernel());
    m_kernel.buildKernel(data);
    

    m_target = new double[m_nInstances];
    for (int i = 0; i < m_nInstances; i++) {
      m_target[i] = data.instance(i).classValue();
    }
    
    m_random = new Random(m_nSeed);
    

    m_alpha = new double[m_target.length];
    m_alphaStar = new double[m_target.length];
    
    m_supportVectors = new SMOset(m_nInstances);
    
    m_b = 0.0D;
    m_nEvals = 0L;
    m_nCacheHits = -1;
  }
  




  protected void wrapUp()
    throws Exception
  {
    m_target = null;
    
    m_nEvals = m_kernel.numEvals();
    m_nCacheHits = m_kernel.numCacheHits();
    
    if (((m_SVM.getKernel() instanceof PolyKernel)) && (((PolyKernel)m_SVM.getKernel()).getExponent() == 1.0D))
    {
      double[] weights = new double[m_data.numAttributes()];
      for (int k = m_supportVectors.getNext(-1); k != -1; k = m_supportVectors.getNext(k)) {
        for (int j = 0; j < weights.length; j++) {
          if (j != m_classIndex) {
            weights[j] += (m_alpha[k] - m_alphaStar[k]) * m_data.instance(k).value(j);
          }
        }
      }
      m_weights = weights;
      

      m_alpha = null;
      m_alphaStar = null;
      m_kernel = null;
    }
    
    m_bModelBuilt = true;
  }
  




  protected double getScore()
    throws Exception
  {
    double res = 0.0D;
    double t = 0.0D;double t2 = 0.0D;
    double sumAlpha = 0.0D;
    for (int i = 0; i < m_nInstances; i++) {
      sumAlpha += m_alpha[i] - m_alphaStar[i];
      for (int j = 0; j < m_nInstances; j++) {
        t += (m_alpha[i] - m_alphaStar[i]) * (m_alpha[j] - m_alphaStar[j]) * m_kernel.eval(i, j, m_data.instance(i));
      }
      











      t2 += m_target[i] * (m_alpha[i] - m_alphaStar[i]) - m_epsilon * (m_alpha[i] + m_alphaStar[i]);
    }
    

    res += -0.5D * t + t2;
    return res;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    throw new Exception("Don't call this directly, use subclass instead");
  }
  


























  protected double SVMOutput(int index)
    throws Exception
  {
    double result = -m_b;
    for (int i = m_supportVectors.getNext(-1); i != -1; i = m_supportVectors.getNext(i)) {
      result += (m_alpha[i] - m_alphaStar[i]) * m_kernel.eval(index, i, m_data.instance(index));
    }
    return result;
  }
  





  public double SVMOutput(Instance inst)
    throws Exception
  {
    double result = -m_b;
    
    if (m_weights != null)
    {
      for (int i = 0; i < inst.numValues(); i++) {
        if (inst.index(i) != m_classIndex) {
          result += m_weights[inst.index(i)] * inst.valueSparse(i);
        }
      }
    } else {
      for (int i = m_supportVectors.getNext(-1); i != -1; i = m_supportVectors.getNext(i)) {
        result += (m_alpha[i] - m_alphaStar[i]) * m_kernel.eval(-1, i, inst);
      }
    }
    return result;
  }
  





  public String seedTipText()
  {
    return "Seed for random number generator.";
  }
  




  public int getSeed()
  {
    return m_nSeed;
  }
  




  public void setSeed(int value)
  {
    m_nSeed = value;
  }
  





  public String epsilonParameterTipText()
  {
    return "The epsilon parameter of the epsilon insensitive loss function.(default 0.001).";
  }
  




  public double getEpsilonParameter()
  {
    return m_epsilon;
  }
  




  public void setEpsilonParameter(double v)
  {
    m_epsilon = v;
  }
  




  public String toString()
  {
    StringBuffer text = new StringBuffer();
    text.append("SMOreg\n\n");
    if (m_weights != null) {
      text.append("weights (not support vectors):\n");
      
      for (int i = 0; i < m_data.numAttributes(); i++) {
        if (i != m_classIndex) {
          text.append((m_weights[i] >= 0.0D ? " + " : " - ") + Utils.doubleToString(Math.abs(m_weights[i]), 12, 4) + " * ");
          if (m_SVM.getFilterType().getSelectedTag().getID() == 1) {
            text.append("(standardized) ");
          } else if (m_SVM.getFilterType().getSelectedTag().getID() == 0) {
            text.append("(normalized) ");
          }
          text.append(m_data.attribute(i).name() + "\n");
        }
      }
    }
    else {
      text.append("Support vectors:\n");
      for (int i = 0; i < m_nInstances; i++) {
        if (m_alpha[i] > 0.0D) {
          text.append("+" + m_alpha[i] + " * k[" + i + "]\n");
        }
        if (m_alphaStar[i] > 0.0D) {
          text.append("-" + m_alphaStar[i] + " * k[" + i + "]\n");
        }
      }
    }
    
    text.append((m_b <= 0.0D ? " + " : " - ") + Utils.doubleToString(Math.abs(m_b), 12, 4) + "\n\n");
    
    text.append("\n\nNumber of kernel evaluations: " + m_nEvals);
    if ((m_nCacheHits >= 0) && (m_nEvals > 0L)) {
      double hitRatio = 1.0D - m_nEvals * 1.0D / (m_nCacheHits + m_nEvals);
      text.append(" (" + Utils.doubleToString(hitRatio * 100.0D, 7, 3).trim() + "% cached)");
    }
    
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11614 $");
  }
}

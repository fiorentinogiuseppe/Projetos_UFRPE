package weka.classifiers.mi;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.RandomizableClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.core.Optimization;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.Standardize;





























































































public class MIEMDD
  extends RandomizableClassifier
  implements OptionHandler, MultiInstanceCapabilitiesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 3899547154866223734L;
  protected int m_ClassIndex;
  protected double[] m_Par;
  protected int m_NumClasses;
  protected int[] m_Classes;
  protected double[][][] m_Data;
  protected Instances m_Attributes;
  protected double[][] m_emData;
  protected Filter m_Filter;
  protected int m_filterType;
  public static final int FILTER_NORMALIZE = 0;
  public static final int FILTER_STANDARDIZE = 1;
  public static final int FILTER_NONE = 2;
  public static final Tag[] TAGS_FILTER = { new Tag(0, "Normalize training data"), new Tag(1, "Standardize training data"), new Tag(2, "No normalization/standardization") };
  protected ReplaceMissingValues m_Missing;
  
  public MIEMDD()
  {
    m_Filter = null;
    

    m_filterType = 1;
    














    m_Missing = new ReplaceMissingValues();
  }
  




  public String globalInfo()
  {
    return "EMDD model builds heavily upon Dietterich's Diverse Density (DD) algorithm.\nIt is a general framework for MI learning of converting the MI problem to a single-instance setting using EM. In this implementation, we use most-likely cause DD model and only use 3 random selected postive bags as initial starting points of EM.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Qi Zhang and Sally A. Goldman");
    result.setValue(TechnicalInformation.Field.TITLE, "EM-DD: An Improved Multiple-Instance Learning Technique");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Advances in Neural Information Processing Systems 14");
    result.setValue(TechnicalInformation.Field.YEAR, "2001");
    result.setValue(TechnicalInformation.Field.PAGES, "1073-108");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "MIT Press");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tWhether to 0=normalize/1=standardize/2=neither.\n\t(default 1=standardize)", "N", 1, "-N <num>"));
    



    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    return result.elements();
  }
  























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setFilterType(new SelectedTag(Integer.parseInt(tmpStr), TAGS_FILTER));
    } else {
      setFilterType(new SelectedTag(1, TAGS_FILTER));
    }
    
    super.setOptions(options);
  }
  









  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-N");
    result.add("" + m_filterType);
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String filterTypeTipText()
  {
    return "The filter type for transforming the training data.";
  }
  





  public SelectedTag getFilterType()
  {
    return new SelectedTag(m_filterType, TAGS_FILTER);
  }
  






  public void setFilterType(SelectedTag newType)
  {
    if (newType.getTags() == TAGS_FILTER) {
      m_filterType = newType.getSelectedTag().getID();
    }
  }
  

  private class OptEng
    extends Optimization
  {
    private OptEng() {}
    
    protected double objectiveFunction(double[] x)
    {
      double nll = 0.0D;
      for (int i = 0; i < m_Classes.length; i++) {
        double ins = 0.0D;
        for (int k = 0; k < m_emData[i].length; k++) {
          ins += (m_emData[i][k] - x[(k * 2)]) * (m_emData[i][k] - x[(k * 2)]) * x[(k * 2 + 1)] * x[(k * 2 + 1)];
        }
        ins = Math.exp(-ins);
        
        if (m_Classes[i] == 1) {
          if (ins <= m_Zero) ins = m_Zero;
          nll -= Math.log(ins);
        }
        else {
          ins = 1.0D - ins;
          if (ins <= m_Zero) ins = m_Zero;
          nll -= Math.log(ins);
        }
      }
      return nll;
    }
    




    protected double[] evaluateGradient(double[] x)
    {
      double[] grad = new double[x.length];
      for (int i = 0; i < m_Classes.length; i++) {
        double[] numrt = new double[x.length];
        double exp = 0.0D;
        for (int k = 0; k < m_emData[i].length; k++) {
          exp += (m_emData[i][k] - x[(k * 2)]) * (m_emData[i][k] - x[(k * 2)]) * x[(k * 2 + 1)] * x[(k * 2 + 1)];
        }
        exp = Math.exp(-exp);
        

        for (int p = 0; p < m_emData[i].length; p++) {
          numrt[(2 * p)] = (2.0D * (x[(2 * p)] - m_emData[i][p]) * x[(p * 2 + 1)] * x[(p * 2 + 1)]);
          numrt[(2 * p + 1)] = (2.0D * (x[(2 * p)] - m_emData[i][p]) * (x[(2 * p)] - m_emData[i][p]) * x[(p * 2 + 1)]);
        }
        


        for (int q = 0; q < m_emData[i].length; q++) {
          if (m_Classes[i] == 1) {
            grad[(2 * q)] += numrt[(2 * q)];
            grad[(2 * q + 1)] += numrt[(2 * q + 1)];
          }
          else {
            grad[(2 * q)] -= numrt[(2 * q)] * exp / (1.0D - exp);
            grad[(2 * q + 1)] -= numrt[(2 * q + 1)] * exp / (1.0D - exp);
          }
        }
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
    
    m_ClassIndex = train.classIndex();
    m_NumClasses = train.numClasses();
    
    int nR = train.attribute(1).relation().numAttributes();
    int nC = train.numInstances();
    int[] bagSize = new int[nC];
    Instances datasets = new Instances(train.attribute(1).relation(), 0);
    
    m_Data = new double[nC][nR][];
    m_Classes = new int[nC];
    m_Attributes = datasets.stringFreeStructure();
    if (m_Debug) {
      System.out.println("\n\nExtracting data...");
    }
    
    for (int h = 0; h < nC; h++) {
      Instance current = train.instance(h);
      m_Classes[h] = ((int)current.classValue());
      Instances currInsts = current.relationalValue(1);
      for (int i = 0; i < currInsts.numInstances(); i++) {
        Instance inst = currInsts.instance(i);
        datasets.add(inst);
      }
      
      int nI = currInsts.numInstances();
      bagSize[h] = nI;
    }
    


    if (m_filterType == 1) {
      m_Filter = new Standardize();
    } else if (m_filterType == 0) {
      m_Filter = new Normalize();
    } else {
      m_Filter = null;
    }
    if (m_Filter != null) {
      m_Filter.setInputFormat(datasets);
      datasets = Filter.useFilter(datasets, m_Filter);
    }
    
    m_Missing.setInputFormat(datasets);
    datasets = Filter.useFilter(datasets, m_Missing);
    
    int instIndex = 0;
    int start = 0;
    for (int h = 0; h < nC; h++) {
      for (int i = 0; i < datasets.numAttributes(); i++)
      {
        m_Data[h][i] = new double[bagSize[h]];
        instIndex = start;
        for (int k = 0; k < bagSize[h]; k++) {
          m_Data[h][i][k] = datasets.instance(instIndex).value(i);
          instIndex++;
        }
      }
      start = instIndex;
    }
    
    if (m_Debug) {
      System.out.println("\n\nIteration History...");
    }
    
    m_emData = new double[nC][nR];
    m_Par = new double[2 * nR];
    
    double[] x = new double[nR * 2];
    double[] tmp = new double[x.length];
    double[] pre_x = new double[x.length];
    double[] best_hypothesis = new double[x.length];
    double[][] b = new double[2][x.length];
    

    double bestnll = Double.MAX_VALUE;
    double min_error = Double.MAX_VALUE;
    



    for (int t = 0; t < x.length; t++) {
      b[0][t] = NaN.0D;
      b[1][t] = NaN.0D;
    }
    

    Random r = new Random(getSeed());
    FastVector index = new FastVector();
    int n1;
    do {
      n1 = r.nextInt(nC - 1);
    } while (m_Classes[n1] == 0);
    index.addElement(new Integer(n1));
    int n2;
    do {
      n2 = r.nextInt(nC - 1);
    } while ((n2 == n1) || (m_Classes[n2] == 0));
    index.addElement(new Integer(n2));
    int n3;
    do {
      n3 = r.nextInt(nC - 1);
    } while ((n3 == n1) || (n3 == n2) || (m_Classes[n3] == 0));
    index.addElement(new Integer(n3));
    
    for (int s = 0; s < index.size(); s++) {
      int exIdx = ((Integer)index.elementAt(s)).intValue();
      if (m_Debug) {
        System.out.println("\nH0 at " + exIdx);
      }
      
      for (int p = 0; p < m_Data[exIdx][0].length; p++)
      {
        for (int q = 0; q < nR; q++) {
          x[(2 * q)] = m_Data[exIdx][q][p];
          x[(2 * q + 1)] = 1.0D;
        }
        
        double pre_nll = Double.MAX_VALUE;
        double nll = 1.7976931348623158E307D;
        int iterationCount = 0;
        
        while ((nll < pre_nll) && (iterationCount < 10)) {
          iterationCount++;
          pre_nll = nll;
          
          if (m_Debug) {
            System.out.println("\niteration: " + iterationCount);
          }
          
          for (int i = 0; i < m_Data.length; i++)
          {
            int insIndex = findInstance(i, x);
            
            for (int att = 0; att < m_Data[0].length; att++)
              m_emData[i][att] = m_Data[i][att][insIndex];
          }
          if (m_Debug) {
            System.out.println("E-step for new H' finished");
          }
          
          OptEng opt = new OptEng(null);
          tmp = opt.findArgmin(x, b);
          while (tmp == null) {
            tmp = opt.getVarbValues();
            if (m_Debug)
              System.out.println("200 iterations finished, not enough!");
            tmp = opt.findArgmin(tmp, b);
          }
          nll = opt.getMinFunction();
          
          pre_x = x;
          x = tmp;
        }
        















        double[] distribution = new double[2];
        int error = 0;
        if (nll > pre_nll) {
          m_Par = pre_x;
        } else {
          m_Par = x;
        }
        for (int i = 0; i < train.numInstances(); i++) {
          distribution = distributionForInstance(train.instance(i));
          if ((distribution[1] >= 0.5D) && (m_Classes[i] == 0)) {
            error++;
          } else if ((distribution[1] < 0.5D) && (m_Classes[i] == 1))
            error++;
        }
        if (error < min_error) {
          best_hypothesis = m_Par;
          min_error = error;
          if (nll > pre_nll) {
            bestnll = pre_nll;
          } else
            bestnll = nll;
          if (m_Debug)
            System.out.println("error= " + error + "  nll= " + bestnll);
        }
      }
      if (m_Debug) {
        System.out.println(exIdx + ":  -------------<Converged>--------------");
        System.out.println("current minimum error= " + min_error + "  nll= " + bestnll);
      }
    }
    m_Par = best_hypothesis;
  }
  












  protected int findInstance(int i, double[] x)
  {
    double min = Double.MAX_VALUE;
    int insIndex = 0;
    int nI = m_Data[i][0].length;
    
    for (int j = 0; j < nI; j++) {
      double ins = 0.0D;
      for (int k = 0; k < m_Data[i].length; k++) {
        ins += (m_Data[i][k][j] - x[(k * 2)]) * (m_Data[i][k][j] - x[(k * 2)]) * x[(k * 2 + 1)] * x[(k * 2 + 1)];
      }
      


      if (ins < min) {
        min = ins;
        insIndex = j;
      }
    }
    return insIndex;
  }
  









  public double[] distributionForInstance(Instance exmp)
    throws Exception
  {
    Instances ins = exmp.relationalValue(1);
    if (m_Filter != null) {
      ins = Filter.useFilter(ins, m_Filter);
    }
    ins = Filter.useFilter(ins, m_Missing);
    
    int nI = ins.numInstances();int nA = ins.numAttributes();
    double[][] dat = new double[nI][nA];
    for (int j = 0; j < nI; j++) {
      for (int k = 0; k < nA; k++) {
        dat[j][k] = ins.instance(j).value(k);
      }
    }
    
    double min = Double.MAX_VALUE;
    double maxProb = -1.0D;
    for (int j = 0; j < nI; j++) {
      double exp = 0.0D;
      for (int k = 0; k < nA; k++) {
        exp += (dat[j][k] - m_Par[(k * 2)]) * (dat[j][k] - m_Par[(k * 2)]) * m_Par[(k * 2 + 1)] * m_Par[(k * 2 + 1)];
      }
      
      if (exp < min) {
        min = exp;
        maxProb = Math.exp(-exp);
      }
    }
    

    double[] distribution = new double[2];
    distribution[1] = maxProb;
    distribution[0] = (1.0D - distribution[1]);
    
    return distribution;
  }
  






  public String toString()
  {
    String result = "MIEMDD";
    if (m_Par == null) {
      return result + ": No model built yet.";
    }
    
    result = result + "\nCoefficients...\nVariable       Point       Scale\n";
    
    int j = 0; for (int idx = 0; j < m_Par.length / 2; idx++) {
      result = result + m_Attributes.attribute(idx).name();
      result = result + " " + Utils.doubleToString(m_Par[(j * 2)], 12, 4);
      result = result + " " + Utils.doubleToString(m_Par[(j * 2 + 1)], 12, 4) + "\n";j++;
    }
    


    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9144 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new MIEMDD(), argv);
  }
}

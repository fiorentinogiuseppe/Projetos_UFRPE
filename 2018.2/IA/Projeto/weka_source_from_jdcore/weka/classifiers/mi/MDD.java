package weka.classifiers.mi;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
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





























































































public class MDD
  extends Classifier
  implements OptionHandler, MultiInstanceCapabilitiesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -7273119490545290581L;
  protected int m_ClassIndex;
  protected double[] m_Par;
  protected int m_NumClasses;
  protected int[] m_Classes;
  protected double[][][] m_Data;
  protected Instances m_Attributes;
  protected Filter m_Filter;
  protected int m_filterType;
  public static final int FILTER_NORMALIZE = 0;
  public static final int FILTER_STANDARDIZE = 1;
  public static final int FILTER_NONE = 2;
  public static final Tag[] TAGS_FILTER = { new Tag(0, "Normalize training data"), new Tag(1, "Standardize training data"), new Tag(2, "No normalization/standardization") };
  protected ReplaceMissingValues m_Missing;
  
  public MDD()
  {
    m_Filter = null;
    

    m_filterType = 1;
    














    m_Missing = new ReplaceMissingValues();
  }
  




  public String globalInfo()
  {
    return "Modified Diverse Density algorithm, with collective assumption.\n\nMore information about DD:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.PHDTHESIS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Oded Maron");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.TITLE, "Learning from ambiguity");
    result.setValue(TechnicalInformation.Field.SCHOOL, "Massachusetts Institute of Technology");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "O. Maron and T. Lozano-Perez");
    additional.setValue(TechnicalInformation.Field.YEAR, "1998");
    additional.setValue(TechnicalInformation.Field.TITLE, "A Framework for Multiple Instance Learning");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "Neural Information Processing Systems");
    additional.setValue(TechnicalInformation.Field.VOLUME, "10");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tTurn on debugging output.", "D", 0, "-D"));
    


    result.addElement(new Option("\tWhether to 0=normalize/1=standardize/2=neither.\n\t(default 1=standardize)", "N", 1, "-N <num>"));
    



    return result.elements();
  }
  




  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    String nString = Utils.getOption('N', options);
    if (nString.length() != 0) {
      setFilterType(new SelectedTag(Integer.parseInt(nString), TAGS_FILTER));
    } else {
      setFilterType(new SelectedTag(1, TAGS_FILTER));
    }
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getDebug()) {
      result.add("-D");
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
        int nI = m_Data[i][0].length;
        double bag = 0.0D;
        
        for (int j = 0; j < nI; j++) {
          double ins = 0.0D;
          for (int k = 0; k < m_Data[i].length; k++) {
            ins += (m_Data[i][k][j] - x[(k * 2)]) * (m_Data[i][k][j] - x[(k * 2)]) / (x[(k * 2 + 1)] * x[(k * 2 + 1)]);
          }
          
          ins = Math.exp(-ins);
          
          if (m_Classes[i] == 1) {
            bag += ins / nI;
          } else
            bag += (1.0D - ins) / nI;
        }
        if (bag <= m_Zero) bag = m_Zero;
        nll -= Math.log(bag);
      }
      
      return nll;
    }
    




    protected double[] evaluateGradient(double[] x)
    {
      double[] grad = new double[x.length];
      for (int i = 0; i < m_Classes.length; i++) {
        int nI = m_Data[i][0].length;
        
        double denom = 0.0D;
        double[] numrt = new double[x.length];
        
        for (int j = 0; j < nI; j++) {
          double exp = 0.0D;
          for (int k = 0; k < m_Data[i].length; k++) {
            exp += (m_Data[i][k][j] - x[(k * 2)]) * (m_Data[i][k][j] - x[(k * 2)]) / (x[(k * 2 + 1)] * x[(k * 2 + 1)]);
          }
          exp = Math.exp(-exp);
          if (m_Classes[i] == 1) {
            denom += exp;
          } else {
            denom += 1.0D - exp;
          }
          
          for (int p = 0; p < m_Data[i].length; p++) {
            numrt[(2 * p)] += exp * 2.0D * (x[(2 * p)] - m_Data[i][p][j]) / (x[(2 * p + 1)] * x[(2 * p + 1)]);
            
            numrt[(2 * p + 1)] += exp * (x[(2 * p)] - m_Data[i][p][j]) * (x[(2 * p)] - m_Data[i][p][j]) / (x[(2 * p + 1)] * x[(2 * p + 1)] * x[(2 * p + 1)]);
          }
        }
        


        if (denom <= m_Zero) {
          denom = m_Zero;
        }
        

        for (int q = 0; q < m_Data[i].length; q++) {
          if (m_Classes[i] == 1) {
            grad[(2 * q)] += numrt[(2 * q)] / denom;
            grad[(2 * q + 1)] -= numrt[(2 * q + 1)] / denom;
          } else {
            grad[(2 * q)] -= numrt[(2 * q)] / denom;
            grad[(2 * q + 1)] += numrt[(2 * q + 1)] / denom;
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
    double sY1 = 0.0D;double sY0 = 0.0D;
    
    if (m_Debug) {
      System.out.println("Extracting data...");
    }
    FastVector maxSzIdx = new FastVector();
    int maxSz = 0;
    
    for (int h = 0; h < nC; h++) {
      Instance current = train.instance(h);
      m_Classes[h] = ((int)current.classValue());
      Instances currInsts = current.relationalValue(1);
      int nI = currInsts.numInstances();
      bagSize[h] = nI;
      
      for (int i = 0; i < nI; i++) {
        Instance inst = currInsts.instance(i);
        datasets.add(inst);
      }
      
      if (m_Classes[h] == 1) {
        if (nI > maxSz) {
          maxSz = nI;
          maxSzIdx = new FastVector(1);
          maxSzIdx.addElement(new Integer(h));
        }
        else if (nI == maxSz) {
          maxSzIdx.addElement(new Integer(h));
        }
      }
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
      

      if (m_Classes[h] == 1) {
        sY1 += 1.0D;
      } else {
        sY0 += 1.0D;
      }
    }
    if (m_Debug) {
      System.out.println("\nIteration History...");
    }
    
    double[] x = new double[nR * 2];double[] tmp = new double[x.length];
    double[][] b = new double[2][x.length];
    

    double bestnll = Double.MAX_VALUE;
    for (int t = 0; t < x.length; t++) {
      b[0][t] = NaN.0D;
      b[1][t] = NaN.0D;
    }
    

    for (int s = 0; s < maxSzIdx.size(); s++) {
      int exIdx = ((Integer)maxSzIdx.elementAt(s)).intValue();
      for (int p = 0; p < m_Data[exIdx][0].length; p++) {
        for (int q = 0; q < nR; q++) {
          x[(2 * q)] = m_Data[exIdx][q][p];
          x[(2 * q + 1)] = 1.0D;
        }
        
        OptEng opt = new OptEng(null);
        tmp = opt.findArgmin(x, b);
        while (tmp == null) {
          tmp = opt.getVarbValues();
          if (m_Debug)
            System.out.println("200 iterations finished, not enough!");
          tmp = opt.findArgmin(tmp, b);
        }
        double nll = opt.getMinFunction();
        
        if (nll < bestnll) {
          bestnll = nll;
          m_Par = tmp;
          if (m_Debug)
            System.out.println("!!!!!!!!!!!!!!!!Smaller NLL found: " + nll);
        }
        if (m_Debug) {
          System.out.println(exIdx + ":  -------------<Converged>--------------");
        }
      }
    }
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
    

    double[] distribution = new double[2];
    distribution[1] = 0.0D;
    
    for (int i = 0; i < nI; i++) {
      double exp = 0.0D;
      for (int r = 0; r < nA; r++) {
        exp += (m_Par[(r * 2)] - dat[i][r]) * (m_Par[(r * 2)] - dat[i][r]) / (m_Par[(r * 2 + 1)] * m_Par[(r * 2 + 1)]);
      }
      exp = Math.exp(-exp);
      

      distribution[1] += exp / nI;
      distribution[0] += (1.0D - exp) / nI;
    }
    
    return distribution;
  }
  





  public String toString()
  {
    String result = "Modified Logistic Regression";
    if (m_Par == null) {
      return result + ": No model built yet.";
    }
    
    result = result + "\nCoefficients...\nVariable      Coeff.\n";
    
    int j = 0; for (int idx = 0; j < m_Par.length / 2; idx++)
    {
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
    runClassifier(new MDD(), argv);
  }
}

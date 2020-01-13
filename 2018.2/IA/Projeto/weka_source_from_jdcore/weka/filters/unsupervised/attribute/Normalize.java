package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.Sourcable;
import weka.filters.UnsupervisedFilter;


























































public class Normalize
  extends PotentialClassIgnorer
  implements UnsupervisedFilter, Sourcable, OptionHandler
{
  static final long serialVersionUID = -8158531150984362898L;
  protected double[] m_MinArray;
  protected double[] m_MaxArray;
  protected double m_Translation = 0.0D;
  

  protected double m_Scale = 1.0D;
  


  public Normalize() {}
  

  public String globalInfo()
  {
    return "Normalizes all numeric values in the given dataset (apart from the class attribute, if set). The resulting values are by default in [0,1] for the data used to compute the normalization intervals. But with the scale and translation parameters one can change that, e.g., with scale = 2.0 and translation = -1.0 you get values in the range [-1,+1].";
  }
  










  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tThe scaling factor for the output range.\n\t(default: 1.0)", "S", 1, "-S <num>"));
    



    result.addElement(new Option("\tThe translation of the output range.\n\t(default: 0.0)", "T", 1, "-T <num>"));
    



    return result.elements();
  }
  

























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setScale(Double.parseDouble(tmpStr));
    } else {
      setScale(1.0D);
    }
    tmpStr = Utils.getOption('T', options);
    if (tmpStr.length() != 0) {
      setTranslation(Double.parseDouble(tmpStr));
    } else {
      setTranslation(0.0D);
    }
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-S");
    result.add("" + getScale());
    
    result.add("-T");
    result.add("" + getTranslation());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  











  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    setOutputFormat(instanceInfo);
    m_MinArray = (this.m_MaxArray = null);
    return true;
  }
  








  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    if (m_MinArray == null) {
      bufferInput(instance);
      return false;
    }
    
    convertInstance(instance);
    return true;
  }
  








  public boolean batchFinished()
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_MinArray == null) {
      Instances input = getInputFormat();
      
      m_MinArray = new double[input.numAttributes()];
      m_MaxArray = new double[input.numAttributes()];
      for (int i = 0; i < input.numAttributes(); i++) {
        m_MinArray[i] = NaN.0D;
      }
      for (int j = 0; j < input.numInstances(); j++) {
        double[] value = input.instance(j).toDoubleArray();
        for (int i = 0; i < input.numAttributes(); i++) {
          if ((input.attribute(i).isNumeric()) && (input.classIndex() != i))
          {
            if (!Instance.isMissingValue(value[i])) {
              if (Double.isNaN(m_MinArray[i])) {
                double tmp165_164 = value[i];m_MaxArray[i] = tmp165_164;m_MinArray[i] = tmp165_164;
              }
              else {
                if (value[i] < m_MinArray[i])
                  m_MinArray[i] = value[i];
                if (value[i] > m_MaxArray[i]) {
                  m_MaxArray[i] = value[i];
                }
              }
            }
          }
        }
      }
      
      for (int i = 0; i < input.numInstances(); i++) {
        convertInstance(input.instance(i));
      }
    }
    flushInput();
    
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  





  protected void convertInstance(Instance instance)
    throws Exception
  {
    Instance inst = null;
    if ((instance instanceof SparseInstance)) {
      double[] newVals = new double[instance.numAttributes()];
      int[] newIndices = new int[instance.numAttributes()];
      double[] vals = instance.toDoubleArray();
      int ind = 0;
      for (int j = 0; j < instance.numAttributes(); j++)
      {
        if ((instance.attribute(j).isNumeric()) && (!Instance.isMissingValue(vals[j])) && (getInputFormat().classIndex() != j)) {
          double value;
          double value;
          if ((Double.isNaN(m_MinArray[j])) || (m_MaxArray[j] == m_MinArray[j]))
          {
            value = 0.0D;
          }
          else {
            value = (vals[j] - m_MinArray[j]) / (m_MaxArray[j] - m_MinArray[j]) * m_Scale + m_Translation;
            
            if (Double.isNaN(value)) {
              throw new Exception("A NaN value was generated while normalizing " + instance.attribute(j).name());
            }
          }
          

          if (value != 0.0D) {
            newVals[ind] = value;
            newIndices[ind] = j;
            ind++;
          }
        }
        else {
          double value = vals[j];
          if (value != 0.0D) {
            newVals[ind] = value;
            newIndices[ind] = j;
            ind++;
          }
        }
      }
      double[] tempVals = new double[ind];
      int[] tempInd = new int[ind];
      System.arraycopy(newVals, 0, tempVals, 0, ind);
      System.arraycopy(newIndices, 0, tempInd, 0, ind);
      inst = new SparseInstance(instance.weight(), tempVals, tempInd, instance.numAttributes());
    }
    else
    {
      double[] vals = instance.toDoubleArray();
      for (int j = 0; j < getInputFormat().numAttributes(); j++) {
        if ((instance.attribute(j).isNumeric()) && (!Instance.isMissingValue(vals[j])) && (getInputFormat().classIndex() != j))
        {

          if ((Double.isNaN(m_MinArray[j])) || (m_MaxArray[j] == m_MinArray[j]))
          {
            vals[j] = 0.0D;
          }
          else {
            vals[j] = ((vals[j] - m_MinArray[j]) / (m_MaxArray[j] - m_MinArray[j]) * m_Scale + m_Translation);
            
            if (Double.isNaN(vals[j])) {
              throw new Exception("A NaN value was generated while normalizing " + instance.attribute(j).name());
            }
          }
        }
      }
      

      inst = new Instance(instance.weight(), vals);
    }
    inst.setDataset(instance.dataset());
    push(inst);
  }
  






















  public String toSource(String className, Instances data)
    throws Exception
  {
    StringBuffer result = new StringBuffer();
    

    boolean[] process = new boolean[data.numAttributes()];
    for (int i = 0; i < data.numAttributes(); i++) {
      process[i] = ((data.attribute(i).isNumeric()) && (i != data.classIndex()) ? 1 : false);
    }
    result.append("class " + className + " {\n");
    result.append("\n");
    result.append("  /** lists which attributes will be processed */\n");
    result.append("  protected final static boolean[] PROCESS = new boolean[]{" + Utils.arrayToString(process) + "};\n");
    result.append("\n");
    result.append("  /** the minimum values for numeric values */\n");
    result.append("  protected final static double[] MIN = new double[]{" + Utils.arrayToString(m_MinArray).replaceAll("NaN", "Double.NaN") + "};\n");
    result.append("\n");
    result.append("  /** the maximum values for numeric values */\n");
    result.append("  protected final static double[] MAX = new double[]{" + Utils.arrayToString(m_MaxArray) + "};\n");
    result.append("\n");
    result.append("  /** the scale factor */\n");
    result.append("  protected final static double SCALE = " + m_Scale + ";\n");
    result.append("\n");
    result.append("  /** the translation */\n");
    result.append("  protected final static double TRANSLATION = " + m_Translation + ";\n");
    result.append("\n");
    result.append("  /**\n");
    result.append("   * filters a single row\n");
    result.append("   * \n");
    result.append("   * @param i the row to process\n");
    result.append("   * @return the processed row\n");
    result.append("   */\n");
    result.append("  public static Object[] filter(Object[] i) {\n");
    result.append("    Object[] result;\n");
    result.append("\n");
    result.append("    result = new Object[i.length];\n");
    result.append("    for (int n = 0; n < i.length; n++) {\n");
    result.append("      if (PROCESS[n] && (i[n] != null)) {\n");
    result.append("        if (Double.isNaN(MIN[n]) || (MIN[n] == MAX[n]))\n");
    result.append("          result[n] = 0;\n");
    result.append("        else\n");
    result.append("          result[n] = (((Double) i[n]) - MIN[n]) / (MAX[n] - MIN[n]) * SCALE + TRANSLATION;\n");
    result.append("      }\n");
    result.append("      else {\n");
    result.append("        result[n] = i[n];\n");
    result.append("      }\n");
    result.append("    }\n");
    result.append("\n");
    result.append("    return result;\n");
    result.append("  }\n");
    result.append("\n");
    result.append("  /**\n");
    result.append("   * filters multiple rows\n");
    result.append("   * \n");
    result.append("   * @param i the rows to process\n");
    result.append("   * @return the processed rows\n");
    result.append("   */\n");
    result.append("  public static Object[][] filter(Object[][] i) {\n");
    result.append("    Object[][] result;\n");
    result.append("\n");
    result.append("    result = new Object[i.length][];\n");
    result.append("    for (int n = 0; n < i.length; n++) {\n");
    result.append("      result[n] = filter(i[n]);\n");
    result.append("    }\n");
    result.append("\n");
    result.append("    return result;\n");
    result.append("  }\n");
    result.append("}\n");
    
    return result.toString();
  }
  




  public double[] getMinArray()
  {
    return m_MinArray;
  }
  




  public double[] getMaxArray()
  {
    return m_MaxArray;
  }
  





  public String scaleTipText()
  {
    return "The factor for scaling the output range (default: 1).";
  }
  




  public double getScale()
  {
    return m_Scale;
  }
  




  public void setScale(double value)
  {
    m_Scale = value;
  }
  





  public String translationTipText()
  {
    return "The translation of the output range (default: 0).";
  }
  




  public double getTranslation()
  {
    return m_Translation;
  }
  




  public void setTranslation(double value)
  {
    m_Translation = value;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new Normalize(), args);
  }
}

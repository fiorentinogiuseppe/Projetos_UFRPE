package weka.filters.unsupervised.attribute;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.Sourcable;
import weka.filters.UnsupervisedFilter;


















































public class Standardize
  extends PotentialClassIgnorer
  implements UnsupervisedFilter, Sourcable
{
  static final long serialVersionUID = -6830769026855053281L;
  private double[] m_Means;
  private double[] m_StdDevs;
  
  public Standardize() {}
  
  public String globalInfo()
  {
    return "Standardizes all numeric attributes in the given dataset to have zero mean and unit variance (apart from the class attribute, if set).";
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
    m_Means = (this.m_StdDevs = null);
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
    if (m_Means == null) {
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
    if (m_Means == null) {
      Instances input = getInputFormat();
      m_Means = new double[input.numAttributes()];
      m_StdDevs = new double[input.numAttributes()];
      for (int i = 0; i < input.numAttributes(); i++) {
        if ((input.attribute(i).isNumeric()) && (input.classIndex() != i))
        {
          m_Means[i] = input.meanOrMode(i);
          m_StdDevs[i] = Math.sqrt(input.variance(i));
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
  






  private void convertInstance(Instance instance)
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
        if ((instance.attribute(j).isNumeric()) && (!Instance.isMissingValue(vals[j])) && (getInputFormat().classIndex() != j))
        {
          double value;
          
          double value;
          if (m_StdDevs[j] > 0.0D) {
            value = (vals[j] - m_Means[j]) / m_StdDevs[j];
          } else {
            value = vals[j] - m_Means[j];
          }
          if (Double.isNaN(value)) {
            throw new Exception("A NaN value was generated while standardizing attribute " + instance.attribute(j).name());
          }
          

          if (value != 0.0D) {
            newVals[ind] = value;
            newIndices[ind] = j;
            ind++;
          }
        } else {
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
    else {
      double[] vals = instance.toDoubleArray();
      for (int j = 0; j < getInputFormat().numAttributes(); j++) {
        if ((instance.attribute(j).isNumeric()) && (!Instance.isMissingValue(vals[j])) && (getInputFormat().classIndex() != j))
        {



          if (m_StdDevs[j] > 0.0D) {
            vals[j] = ((vals[j] - m_Means[j]) / m_StdDevs[j]);
          } else {
            vals[j] -= m_Means[j];
          }
          if (Double.isNaN(vals[j])) {
            throw new Exception("A NaN value was generated while standardizing attribute " + instance.attribute(j).name());
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
    result.append("  /** the computed means */\n");
    result.append("  protected final static double[] MEANS = new double[]{" + Utils.arrayToString(m_Means) + "};\n");
    result.append("\n");
    result.append("  /** the computed standard deviations */\n");
    result.append("  protected final static double[] STDEVS = new double[]{" + Utils.arrayToString(m_StdDevs) + "};\n");
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
    result.append("        if (STDEVS[n] > 0)\n");
    result.append("          result[n] = (((Double) i[n]) - MEANS[n]) / STDEVS[n];\n");
    result.append("        else\n");
    result.append("          result[n] = ((Double) i[n]) - MEANS[n];\n");
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
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5547 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new Standardize(), argv);
  }
}

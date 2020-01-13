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













































public class ReplaceMissingValues
  extends PotentialClassIgnorer
  implements UnsupervisedFilter, Sourcable
{
  static final long serialVersionUID = 8349568310991609867L;
  private double[] m_ModesAndMeans = null;
  


  public ReplaceMissingValues() {}
  


  public String globalInfo()
  {
    return "Replaces all missing values for nominal and numeric attributes in a dataset with the modes and means from the training data.";
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
    m_ModesAndMeans = null;
    return true;
  }
  









  public boolean input(Instance instance)
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    if (m_ModesAndMeans == null) {
      bufferInput(instance);
      return false;
    }
    convertInstance(instance);
    return true;
  }
  









  public boolean batchFinished()
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    
    if (m_ModesAndMeans == null)
    {
      double sumOfWeights = getInputFormat().sumOfWeights();
      double[][] counts = new double[getInputFormat().numAttributes()][];
      for (int i = 0; i < getInputFormat().numAttributes(); i++) {
        if (getInputFormat().attribute(i).isNominal()) {
          counts[i] = new double[getInputFormat().attribute(i).numValues()];
          if (counts[i].length > 0)
            counts[i][0] = sumOfWeights;
        }
      }
      double[] sums = new double[getInputFormat().numAttributes()];
      for (int i = 0; i < sums.length; i++) {
        sums[i] = sumOfWeights;
      }
      double[] results = new double[getInputFormat().numAttributes()];
      for (int j = 0; j < getInputFormat().numInstances(); j++) {
        Instance inst = getInputFormat().instance(j);
        for (int i = 0; i < inst.numValues(); i++) {
          if (!inst.isMissingSparse(i)) {
            double value = inst.valueSparse(i);
            if (inst.attributeSparse(i).isNominal()) {
              if (counts[inst.index(i)].length > 0) {
                counts[inst.index(i)][((int)value)] += inst.weight();
                counts[inst.index(i)][0] -= inst.weight();
              }
            } else if (inst.attributeSparse(i).isNumeric()) {
              results[inst.index(i)] += inst.weight() * inst.valueSparse(i);
            }
          }
          else if (inst.attributeSparse(i).isNominal()) {
            if (counts[inst.index(i)].length > 0) {
              counts[inst.index(i)][0] -= inst.weight();
            }
          } else if (inst.attributeSparse(i).isNumeric()) {
            sums[inst.index(i)] -= inst.weight();
          }
        }
      }
      
      m_ModesAndMeans = new double[getInputFormat().numAttributes()];
      for (int i = 0; i < getInputFormat().numAttributes(); i++) {
        if (getInputFormat().attribute(i).isNominal()) {
          if (counts[i].length == 0) {
            m_ModesAndMeans[i] = Instance.missingValue();
          } else
            m_ModesAndMeans[i] = Utils.maxIndex(counts[i]);
        } else if ((getInputFormat().attribute(i).isNumeric()) && 
          (Utils.gr(sums[i], 0.0D))) {
          m_ModesAndMeans[i] = (results[i] / sums[i]);
        }
      }
      


      for (int i = 0; i < getInputFormat().numInstances(); i++) {
        convertInstance(getInputFormat().instance(i));
      }
    }
    
    flushInput();
    
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  






  private void convertInstance(Instance instance)
  {
    Instance inst = null;
    if ((instance instanceof SparseInstance)) {
      double[] vals = new double[instance.numValues()];
      int[] indices = new int[instance.numValues()];
      int num = 0;
      for (int j = 0; j < instance.numValues(); j++) {
        if ((instance.isMissingSparse(j)) && (getInputFormat().classIndex() != instance.index(j)) && ((instance.attributeSparse(j).isNominal()) || (instance.attributeSparse(j).isNumeric())))
        {


          if (m_ModesAndMeans[instance.index(j)] != 0.0D) {
            vals[num] = m_ModesAndMeans[instance.index(j)];
            indices[num] = instance.index(j);
            num++;
          }
        } else {
          vals[num] = instance.valueSparse(j);
          indices[num] = instance.index(j);
          num++;
        }
      }
      if (num == instance.numValues()) {
        inst = new SparseInstance(instance.weight(), vals, indices, instance.numAttributes());
      }
      else {
        double[] tempVals = new double[num];
        int[] tempInd = new int[num];
        System.arraycopy(vals, 0, tempVals, 0, num);
        System.arraycopy(indices, 0, tempInd, 0, num);
        inst = new SparseInstance(instance.weight(), tempVals, tempInd, instance.numAttributes());
      }
    }
    else {
      double[] vals = new double[getInputFormat().numAttributes()];
      for (int j = 0; j < instance.numAttributes(); j++) {
        if ((instance.isMissing(j)) && (getInputFormat().classIndex() != j) && ((getInputFormat().attribute(j).isNominal()) || (getInputFormat().attribute(j).isNumeric())))
        {


          vals[j] = m_ModesAndMeans[j];
        } else {
          vals[j] = instance.value(j);
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
    

    boolean[] numeric = new boolean[data.numAttributes()];
    boolean[] nominal = new boolean[data.numAttributes()];
    String[] modes = new String[data.numAttributes()];
    double[] means = new double[data.numAttributes()];
    for (int i = 0; i < data.numAttributes(); i++) {
      numeric[i] = ((data.attribute(i).isNumeric()) && (i != data.classIndex()) ? 1 : false);
      nominal[i] = ((data.attribute(i).isNominal()) && (i != data.classIndex()) ? 1 : false);
      
      if (numeric[i] != 0) {
        means[i] = m_ModesAndMeans[i];
      } else {
        means[i] = NaN.0D;
      }
      if (nominal[i] != 0) {
        modes[i] = data.attribute(i).value((int)m_ModesAndMeans[i]);
      } else {
        modes[i] = null;
      }
    }
    result.append("class " + className + " {\n");
    result.append("\n");
    result.append("  /** lists which numeric attributes will be processed */\n");
    result.append("  protected final static boolean[] NUMERIC = new boolean[]{" + Utils.arrayToString(numeric) + "};\n");
    result.append("\n");
    result.append("  /** lists which nominal attributes will be processed */\n");
    result.append("  protected final static boolean[] NOMINAL = new boolean[]{" + Utils.arrayToString(nominal) + "};\n");
    result.append("\n");
    result.append("  /** the means */\n");
    result.append("  protected final static double[] MEANS = new double[]{" + Utils.arrayToString(means).replaceAll("NaN", "Double.NaN") + "};\n");
    result.append("\n");
    result.append("  /** the modes */\n");
    result.append("  protected final static String[] MODES = new String[]{");
    for (i = 0; i < modes.length; i++) {
      if (i > 0)
        result.append(",");
      if (nominal[i] != 0) {
        result.append("\"" + Utils.quote(modes[i]) + "\"");
      } else
        result.append(modes[i]);
    }
    result.append("};\n");
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
    result.append("      if (i[n] == null) {\n");
    result.append("        if (NUMERIC[n])\n");
    result.append("          result[n] = MEANS[n];\n");
    result.append("        else if (NOMINAL[n])\n");
    result.append("          result[n] = MODES[n];\n");
    result.append("        else\n");
    result.append("          result[n] = i[n];\n");
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
    runFilter(new ReplaceMissingValues(), argv);
  }
}

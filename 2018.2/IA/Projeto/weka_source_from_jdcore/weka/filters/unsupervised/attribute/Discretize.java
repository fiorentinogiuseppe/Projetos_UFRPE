package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.filters.UnsupervisedFilter;












































































public class Discretize
  extends PotentialClassIgnorer
  implements UnsupervisedFilter, WeightedInstancesHandler
{
  static final long serialVersionUID = -1358531742174527279L;
  protected Range m_DiscretizeCols = new Range();
  

  protected int m_NumBins = 10;
  

  protected double m_DesiredWeightOfInstancesPerInterval = -1.0D;
  

  protected double[][] m_CutPoints = (double[][])null;
  

  protected boolean m_MakeBinary = false;
  

  protected boolean m_FindNumBins = false;
  

  protected boolean m_UseEqualFrequency = false;
  

  protected String m_DefaultCols;
  

  public Discretize()
  {
    m_DefaultCols = "first-last";
    setAttributeIndices("first-last");
  }
  





  public Discretize(String cols)
  {
    m_DefaultCols = cols;
    setAttributeIndices(cols);
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.add(enm.nextElement());
    }
    result.addElement(new Option("\tSpecifies the (maximum) number of bins to divide numeric attributes into.\n\t(default = 10)", "B", 1, "-B <num>"));
    




    result.addElement(new Option("\tSpecifies the desired weight of instances per bin for\n\tequal-frequency binning. If this is set to a positive\n\tnumber then the -B option will be ignored.\n\t(default = -1)", "M", 1, "-M <num>"));
    





    result.addElement(new Option("\tUse equal-frequency instead of equal-width discretization.", "F", 0, "-F"));
    


    result.addElement(new Option("\tOptimize number of bins using leave-one-out estimate\n\tof estimated entropy (for equal-width discretization).\n\tIf this is set then the -B option will be ignored.", "O", 0, "-O"));
    




    result.addElement(new Option("\tSpecifies list of columns to Discretize. First and last are valid indexes.\n\t(default: first-last)", "R", 1, "-R <col1,col2-col4,...>"));
    




    result.addElement(new Option("\tInvert matching sense of column indexes.", "V", 0, "-V"));
    


    result.addElement(new Option("\tOutput binary attributes for discretized attributes.", "D", 0, "-D"));
    


    return result.elements();
  }
  












































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    setMakeBinary(Utils.getFlag('D', options));
    setUseEqualFrequency(Utils.getFlag('F', options));
    setFindNumBins(Utils.getFlag('O', options));
    setInvertSelection(Utils.getFlag('V', options));
    
    String weight = Utils.getOption('M', options);
    if (weight.length() != 0) {
      setDesiredWeightOfInstancesPerInterval(new Double(weight).doubleValue());
    } else {
      setDesiredWeightOfInstancesPerInterval(-1.0D);
    }
    
    String numBins = Utils.getOption('B', options);
    if (numBins.length() != 0) {
      setBins(Integer.parseInt(numBins));
    } else {
      setBins(10);
    }
    
    String convertList = Utils.getOption('R', options);
    if (convertList.length() != 0) {
      setAttributeIndices(convertList);
    } else {
      setAttributeIndices(m_DefaultCols);
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if (getMakeBinary()) {
      result.add("-D");
    }
    if (getUseEqualFrequency()) {
      result.add("-F");
    }
    if (getFindNumBins()) {
      result.add("-O");
    }
    if (getInvertSelection()) {
      result.add("-V");
    }
    result.add("-B");
    result.add("" + getBins());
    
    result.add("-M");
    result.add("" + getDesiredWeightOfInstancesPerInterval());
    
    if (!getAttributeIndices().equals("")) {
      result.add("-R");
      result.add(getAttributeIndices());
    }
    
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
    if (!getMakeBinary()) {
      result.enable(Capabilities.Capability.NO_CLASS);
    }
    return result;
  }
  








  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    if ((m_MakeBinary) && (m_IgnoreClass)) {
      throw new IllegalArgumentException("Can't ignore class when changing the number of attributes!");
    }
    

    super.setInputFormat(instanceInfo);
    
    m_DiscretizeCols.setUpper(instanceInfo.numAttributes() - 1);
    m_CutPoints = ((double[][])null);
    
    if ((getFindNumBins()) && (getUseEqualFrequency())) {
      throw new IllegalArgumentException("Bin number optimization in conjunction with equal-frequency binning not implemented.");
    }
    



    return false;
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
    
    if (m_CutPoints != null) {
      convertInstance(instance);
      return true;
    }
    
    bufferInput(instance);
    return false;
  }
  








  public boolean batchFinished()
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_CutPoints == null) {
      calculateCutPoints();
      
      setOutputFormat();
      



      for (int i = 0; i < getInputFormat().numInstances(); i++) {
        convertInstance(getInputFormat().instance(i));
      }
    }
    flushInput();
    
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  






  public String globalInfo()
  {
    return "An instance filter that discretizes a range of numeric attributes in the dataset into nominal attributes. Discretization is by simple binning. Skips the class attribute if set.";
  }
  









  public String findNumBinsTipText()
  {
    return "Optimize number of equal-width bins using leave-one-out. Doesn't work for equal-frequency binning";
  }
  






  public boolean getFindNumBins()
  {
    return m_FindNumBins;
  }
  





  public void setFindNumBins(boolean newFindNumBins)
  {
    m_FindNumBins = newFindNumBins;
  }
  






  public String makeBinaryTipText()
  {
    return "Make resulting attributes binary.";
  }
  





  public boolean getMakeBinary()
  {
    return m_MakeBinary;
  }
  





  public void setMakeBinary(boolean makeBinary)
  {
    m_MakeBinary = makeBinary;
  }
  






  public String desiredWeightOfInstancesPerIntervalTipText()
  {
    return "Sets the desired weight of instances per interval for equal-frequency binning.";
  }
  





  public double getDesiredWeightOfInstancesPerInterval()
  {
    return m_DesiredWeightOfInstancesPerInterval;
  }
  




  public void setDesiredWeightOfInstancesPerInterval(double newDesiredNumber)
  {
    m_DesiredWeightOfInstancesPerInterval = newDesiredNumber;
  }
  






  public String useEqualFrequencyTipText()
  {
    return "If set to true, equal-frequency binning will be used instead of equal-width binning.";
  }
  






  public boolean getUseEqualFrequency()
  {
    return m_UseEqualFrequency;
  }
  





  public void setUseEqualFrequency(boolean newUseEqualFrequency)
  {
    m_UseEqualFrequency = newUseEqualFrequency;
  }
  






  public String binsTipText()
  {
    return "Number of bins.";
  }
  





  public int getBins()
  {
    return m_NumBins;
  }
  





  public void setBins(int numBins)
  {
    m_NumBins = numBins;
  }
  






  public String invertSelectionTipText()
  {
    return "Set attribute selection mode. If false, only selected (numeric) attributes in the range will be discretized; if true, only non-selected attributes will be discretized.";
  }
  







  public boolean getInvertSelection()
  {
    return m_DiscretizeCols.getInvert();
  }
  







  public void setInvertSelection(boolean invert)
  {
    m_DiscretizeCols.setInvert(invert);
  }
  





  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  








  public String getAttributeIndices()
  {
    return m_DiscretizeCols.getRanges();
  }
  










  public void setAttributeIndices(String rangeList)
  {
    m_DiscretizeCols.setRanges(rangeList);
  }
  










  public void setAttributeIndicesArray(int[] attributes)
  {
    setAttributeIndices(Range.indicesToRangeList(attributes));
  }
  







  public double[] getCutPoints(int attributeIndex)
  {
    if (m_CutPoints == null) {
      return null;
    }
    return m_CutPoints[attributeIndex];
  }
  

  protected void calculateCutPoints()
  {
    m_CutPoints = new double[getInputFormat().numAttributes()][];
    for (int i = getInputFormat().numAttributes() - 1; i >= 0; i--) {
      if ((m_DiscretizeCols.isInRange(i)) && (getInputFormat().attribute(i).isNumeric()) && (getInputFormat().classIndex() != i))
      {

        if (m_FindNumBins) {
          findNumBins(i);
        } else if (!m_UseEqualFrequency) {
          calculateCutPointsByEqualWidthBinning(i);
        } else {
          calculateCutPointsByEqualFrequencyBinning(i);
        }
      }
    }
  }
  






  protected void calculateCutPointsByEqualWidthBinning(int index)
  {
    double max = 0.0D;double min = 1.0D;
    
    for (int i = 0; i < getInputFormat().numInstances(); i++) {
      Instance currentInstance = getInputFormat().instance(i);
      if (!currentInstance.isMissing(index)) {
        double currentVal = currentInstance.value(index);
        if (max < min) {
          max = min = currentVal;
        }
        if (currentVal > max) {
          max = currentVal;
        }
        if (currentVal < min) {
          min = currentVal;
        }
      }
    }
    double binWidth = (max - min) / m_NumBins;
    double[] cutPoints = null;
    if ((m_NumBins > 1) && (binWidth > 0.0D)) {
      cutPoints = new double[m_NumBins - 1];
      for (int i = 1; i < m_NumBins; i++) {
        cutPoints[(i - 1)] = (min + binWidth * i);
      }
    }
    m_CutPoints[index] = cutPoints;
  }
  






  protected void calculateCutPointsByEqualFrequencyBinning(int index)
  {
    Instances data = new Instances(getInputFormat());
    

    data.sort(index);
    

    double sumOfWeights = 0.0D;
    for (int i = 0; i < data.numInstances(); i++) {
      if (data.instance(i).isMissing(index)) {
        break;
      }
      sumOfWeights += data.instance(i).weight();
    }
    

    double[] cutPoints = new double[m_NumBins - 1];
    double freq; if (getDesiredWeightOfInstancesPerInterval() > 0.0D) {
      double freq = getDesiredWeightOfInstancesPerInterval();
      cutPoints = new double[(int)(sumOfWeights / freq)];
    } else {
      freq = sumOfWeights / m_NumBins;
      cutPoints = new double[m_NumBins - 1];
    }
    

    double counter = 0.0D;double last = 0.0D;
    int cpindex = 0;int lastIndex = -1;
    for (int i = 0; i < data.numInstances() - 1; i++)
    {

      if (data.instance(i).isMissing(index)) {
        break;
      }
      counter += data.instance(i).weight();
      sumOfWeights -= data.instance(i).weight();
      

      if (data.instance(i).value(index) < data.instance(i + 1).value(index))
      {


        if (counter >= freq)
        {

          if ((freq - last < counter - freq) && (lastIndex != -1)) {
            cutPoints[cpindex] = ((data.instance(lastIndex).value(index) + data.instance(lastIndex + 1).value(index)) / 2.0D);
            
            counter -= last;
            last = counter;
            lastIndex = i;
          } else {
            cutPoints[cpindex] = ((data.instance(i).value(index) + data.instance(i + 1).value(index)) / 2.0D);
            
            counter = 0.0D;
            last = 0.0D;
            lastIndex = -1;
          }
          cpindex++;
          freq = (sumOfWeights + counter) / (cutPoints.length + 1 - cpindex);
        } else {
          lastIndex = i;
          last = counter;
        }
      }
    }
    

    if ((cpindex < cutPoints.length) && (lastIndex != -1)) {
      cutPoints[cpindex] = ((data.instance(lastIndex).value(index) + data.instance(lastIndex + 1).value(index)) / 2.0D);
      
      cpindex++;
    }
    

    if (cpindex == 0) {
      m_CutPoints[index] = null;
    } else {
      double[] cp = new double[cpindex];
      for (int i = 0; i < cpindex; i++) {
        cp[i] = cutPoints[i];
      }
      m_CutPoints[index] = cp;
    }
  }
  





  protected void findNumBins(int index)
  {
    double min = Double.MAX_VALUE;double max = -1.7976931348623157E308D;double binWidth = 0.0D;
    double bestEntropy = Double.MAX_VALUE;
    
    int bestNumBins = 1;
    


    for (int i = 0; i < getInputFormat().numInstances(); i++) {
      Instance currentInstance = getInputFormat().instance(i);
      if (!currentInstance.isMissing(index)) {
        double currentVal = currentInstance.value(index);
        if (currentVal > max) {
          max = currentVal;
        }
        if (currentVal < min) {
          min = currentVal;
        }
      }
    }
    

    for (int i = 0; i < m_NumBins; i++) {
      double[] distribution = new double[i + 1];
      binWidth = (max - min) / (i + 1);
      

      for (int j = 0; j < getInputFormat().numInstances(); j++) {
        Instance currentInstance = getInputFormat().instance(j);
        if (!currentInstance.isMissing(index)) {
          for (int k = 0; k < i + 1; k++) {
            if (currentInstance.value(index) <= min + (k + 1.0D) * binWidth)
            {
              distribution[k] += currentInstance.weight();
              break;
            }
          }
        }
      }
      

      double entropy = 0.0D;
      for (int k = 0; k < i + 1; k++) {
        if (distribution[k] < 2.0D) {
          entropy = Double.MAX_VALUE;
          break;
        }
        entropy -= distribution[k] * Math.log((distribution[k] - 1.0D) / binWidth);
      }
      


      if (entropy < bestEntropy) {
        bestEntropy = entropy;
        bestNumBins = i + 1;
      }
    }
    

    double[] cutPoints = null;
    if ((bestNumBins > 1) && (binWidth > 0.0D)) {
      cutPoints = new double[bestNumBins - 1];
      for (int i = 1; i < bestNumBins; i++) {
        cutPoints[(i - 1)] = (min + binWidth * i);
      }
    }
    m_CutPoints[index] = cutPoints;
  }
  




  protected void setOutputFormat()
  {
    if (m_CutPoints == null) {
      setOutputFormat(null);
      return;
    }
    FastVector attributes = new FastVector(getInputFormat().numAttributes());
    int classIndex = getInputFormat().classIndex();
    for (int i = 0; i < getInputFormat().numAttributes(); i++) {
      if ((m_DiscretizeCols.isInRange(i)) && (getInputFormat().attribute(i).isNumeric()) && (getInputFormat().classIndex() != i))
      {

        if (!m_MakeBinary) {
          FastVector attribValues = new FastVector(1);
          if (m_CutPoints[i] == null) {
            attribValues.addElement("'All'");
          } else {
            for (int j = 0; j <= m_CutPoints[i].length; j++) {
              if (j == 0) {
                attribValues.addElement("'(-inf-" + Utils.doubleToString(m_CutPoints[i][j], 6) + "]'");
              }
              else if (j == m_CutPoints[i].length) {
                attribValues.addElement("'(" + Utils.doubleToString(m_CutPoints[i][(j - 1)], 6) + "-inf)'");
              }
              else
              {
                attribValues.addElement("'(" + Utils.doubleToString(m_CutPoints[i][(j - 1)], 6) + "-" + Utils.doubleToString(m_CutPoints[i][j], 6) + "]'");
              }
            }
          }
          

          Attribute newAtt = new Attribute(getInputFormat().attribute(i).name(), attribValues);
          

          newAtt.setWeight(getInputFormat().attribute(i).weight());
          attributes.addElement(newAtt);
        }
        else if (m_CutPoints[i] == null) {
          FastVector attribValues = new FastVector(1);
          attribValues.addElement("'All'");
          Attribute newAtt = new Attribute(getInputFormat().attribute(i).name(), attribValues);
          

          newAtt.setWeight(getInputFormat().attribute(i).weight());
          attributes.addElement(newAtt);
        } else {
          if (i < getInputFormat().classIndex()) {
            classIndex += m_CutPoints[i].length - 1;
          }
          for (int j = 0; j < m_CutPoints[i].length; j++) {
            FastVector attribValues = new FastVector(2);
            attribValues.addElement("'(-inf-" + Utils.doubleToString(m_CutPoints[i][j], 6) + "]'");
            
            attribValues.addElement("'(" + Utils.doubleToString(m_CutPoints[i][j], 6) + "-inf)'");
            

            Attribute newAtt = new Attribute(getInputFormat().attribute(i).name() + "_" + (j + 1), attribValues);
            

            newAtt.setWeight(getInputFormat().attribute(i).weight());
            attributes.addElement(newAtt);
          }
        }
      }
      else {
        attributes.addElement(getInputFormat().attribute(i).copy());
      }
    }
    Instances outputFormat = new Instances(getInputFormat().relationName(), attributes, 0);
    
    outputFormat.setClassIndex(classIndex);
    setOutputFormat(outputFormat);
  }
  






  protected void convertInstance(Instance instance)
  {
    int index = 0;
    double[] vals = new double[outputFormatPeek().numAttributes()];
    
    for (int i = 0; i < getInputFormat().numAttributes(); i++) {
      if ((m_DiscretizeCols.isInRange(i)) && (getInputFormat().attribute(i).isNumeric()) && (getInputFormat().classIndex() != i))
      {


        double currentVal = instance.value(i);
        if (m_CutPoints[i] == null) {
          if (instance.isMissing(i)) {
            vals[index] = Instance.missingValue();
          } else {
            vals[index] = 0.0D;
          }
          index++;
        }
        else if (!m_MakeBinary) {
          if (instance.isMissing(i)) {
            vals[index] = Instance.missingValue();
          } else {
            for (int j = 0; j < m_CutPoints[i].length; j++) {
              if (currentVal <= m_CutPoints[i][j]) {
                break;
              }
            }
            vals[index] = j;
          }
          index++;
        } else {
          for (int j = 0; j < m_CutPoints[i].length; j++) {
            if (instance.isMissing(i)) {
              vals[index] = Instance.missingValue();
            } else if (currentVal <= m_CutPoints[i][j]) {
              vals[index] = 0.0D;
            } else {
              vals[index] = 1.0D;
            }
            index++;
          }
        }
      }
      else {
        vals[index] = instance.value(i);
        index++;
      }
    }
    
    Instance inst = null;
    if ((instance instanceof SparseInstance)) {
      inst = new SparseInstance(instance.weight(), vals);
    } else {
      inst = new Instance(instance.weight(), vals);
    }
    inst.setDataset(getOutputFormat());
    copyValues(inst, false, instance.dataset(), getOutputFormat());
    inst.setDataset(getOutputFormat());
    push(inst);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8284 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new Discretize(), argv);
  }
}

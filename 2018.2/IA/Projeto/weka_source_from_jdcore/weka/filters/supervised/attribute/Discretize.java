package weka.filters.supervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.ContingencyTables;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.SpecialFunctions;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;




























































































public class Discretize
  extends Filter
  implements SupervisedFilter, OptionHandler, WeightedInstancesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -3141006402280129097L;
  protected Range m_DiscretizeCols = new Range();
  

  protected double[][] m_CutPoints = (double[][])null;
  

  protected boolean m_MakeBinary = false;
  

  protected boolean m_UseBetterEncoding = false;
  

  protected boolean m_UseKononenko = false;
  

  public Discretize()
  {
    setAttributeIndices("first-last");
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(7);
    
    newVector.addElement(new Option("\tSpecifies list of columns to Discretize. First and last are valid indexes.\n\t(default none)", "R", 1, "-R <col1,col2-col4,...>"));
    




    newVector.addElement(new Option("\tInvert matching sense of column indexes.", "V", 0, "-V"));
    


    newVector.addElement(new Option("\tOutput binary attributes for discretized attributes.", "D", 0, "-D"));
    


    newVector.addElement(new Option("\tUse better encoding of split point for MDL.", "E", 0, "-E"));
    


    newVector.addElement(new Option("\tUse Kononenko's MDL criterion.", "K", 0, "-K"));
    


    return newVector.elements();
  }
  



























  public void setOptions(String[] options)
    throws Exception
  {
    setMakeBinary(Utils.getFlag('D', options));
    setUseBetterEncoding(Utils.getFlag('E', options));
    setUseKononenko(Utils.getFlag('K', options));
    setInvertSelection(Utils.getFlag('V', options));
    
    String convertList = Utils.getOption('R', options);
    if (convertList.length() != 0) {
      setAttributeIndices(convertList);
    } else {
      setAttributeIndices("first-last");
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  




  public String[] getOptions()
  {
    String[] options = new String[12];
    int current = 0;
    
    if (getMakeBinary()) {
      options[(current++)] = "-D";
    }
    if (getUseBetterEncoding()) {
      options[(current++)] = "-E";
    }
    if (getUseKononenko()) {
      options[(current++)] = "-K";
    }
    if (getInvertSelection()) {
      options[(current++)] = "-V";
    }
    if (!getAttributeIndices().equals("")) {
      options[(current++)] = "-R";options[(current++)] = getAttributeIndices();
    }
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  








  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    
    m_DiscretizeCols.setUpper(instanceInfo.numAttributes() - 1);
    m_CutPoints = ((double[][])null);
    


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
    return "An instance filter that discretizes a range of numeric attributes in the dataset into nominal attributes. Discretization is by Fayyad & Irani's MDL method (the default).\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Usama M. Fayyad and Keki B. Irani");
    result.setValue(TechnicalInformation.Field.TITLE, "Multi-interval discretization of continuousvalued attributes for classification learning");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Thirteenth International Joint Conference on Articial Intelligence");
    result.setValue(TechnicalInformation.Field.YEAR, "1993");
    result.setValue(TechnicalInformation.Field.VOLUME, "2");
    result.setValue(TechnicalInformation.Field.PAGES, "1022-1027");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann Publishers");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Igor Kononenko");
    additional.setValue(TechnicalInformation.Field.TITLE, "On Biases in Estimating Multi-Valued Attributes");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "14th International Joint Conference on Articial Intelligence");
    additional.setValue(TechnicalInformation.Field.YEAR, "1995");
    additional.setValue(TechnicalInformation.Field.PAGES, "1034-1040");
    additional.setValue(TechnicalInformation.Field.PS, "http://ai.fri.uni-lj.si/papers/kononenko95-ijcai.ps.gz");
    
    return result;
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
  






  public String useKononenkoTipText()
  {
    return "Use Kononenko's MDL criterion. If set to false uses the Fayyad & Irani criterion.";
  }
  






  public boolean getUseKononenko()
  {
    return m_UseKononenko;
  }
  





  public void setUseKononenko(boolean useKon)
  {
    m_UseKononenko = useKon;
  }
  






  public String useBetterEncodingTipText()
  {
    return "Uses a more efficient split point encoding.";
  }
  





  public boolean getUseBetterEncoding()
  {
    return m_UseBetterEncoding;
  }
  





  public void setUseBetterEncoding(boolean useBetterEncoding)
  {
    m_UseBetterEncoding = useBetterEncoding;
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
    Instances copy = null;
    
    m_CutPoints = new double[getInputFormat().numAttributes()][];
    for (int i = getInputFormat().numAttributes() - 1; i >= 0; i--) {
      if ((m_DiscretizeCols.isInRange(i)) && (getInputFormat().attribute(i).isNumeric()))
      {


        if (copy == null) {
          copy = new Instances(getInputFormat());
        }
        calculateCutPointsByMDL(i, copy);
      }
    }
  }
  








  protected void calculateCutPointsByMDL(int index, Instances data)
  {
    data.sort(data.attribute(index));
    

    int firstMissing = data.numInstances();
    for (int i = 0; i < data.numInstances(); i++) {
      if (data.instance(i).isMissing(index)) {
        firstMissing = i;
        break;
      }
    }
    m_CutPoints[index] = cutPointsForSubset(data, index, 0, firstMissing);
  }
  












  private boolean KononenkosMDL(double[] priorCounts, double[][] bestCounts, double numInstances, int numCutPoints)
  {
    double distAfter = 0.0D;double instAfter = 0.0D;
    



    int numClassesTotal = 0;
    for (int i = 0; i < priorCounts.length; i++) {
      if (priorCounts[i] > 0.0D) {
        numClassesTotal++;
      }
    }
    

    double distPrior = SpecialFunctions.log2Binomial(numInstances + numClassesTotal - 1.0D, numClassesTotal - 1);
    



    double instPrior = SpecialFunctions.log2Multinomial(numInstances, priorCounts);
    

    double before = instPrior + distPrior;
    

    for (int i = 0; i < bestCounts.length; i++) {
      double sum = Utils.sum(bestCounts[i]);
      distAfter += SpecialFunctions.log2Binomial(sum + numClassesTotal - 1.0D, numClassesTotal - 1);
      
      instAfter += SpecialFunctions.log2Multinomial(sum, bestCounts[i]);
    }
    


    double after = Utils.log2(numCutPoints) + distAfter + instAfter;
    

    return before > after;
  }
  


















  private boolean FayyadAndIranisMDL(double[] priorCounts, double[][] bestCounts, double numInstances, int numCutPoints)
  {
    double priorEntropy = ContingencyTables.entropy(priorCounts);
    

    double entropy = ContingencyTables.entropyConditionedOnRows(bestCounts);
    

    double gain = priorEntropy - entropy;
    

    int numClassesTotal = 0;
    for (int i = 0; i < priorCounts.length; i++) {
      if (priorCounts[i] > 0.0D) {
        numClassesTotal++;
      }
    }
    

    int numClassesLeft = 0;
    for (int i = 0; i < bestCounts[0].length; i++) {
      if (bestCounts[0][i] > 0.0D) {
        numClassesLeft++;
      }
    }
    

    int numClassesRight = 0;
    for (int i = 0; i < bestCounts[1].length; i++) {
      if (bestCounts[1][i] > 0.0D) {
        numClassesRight++;
      }
    }
    

    double entropyLeft = ContingencyTables.entropy(bestCounts[0]);
    double entropyRight = ContingencyTables.entropy(bestCounts[1]);
    

    double delta = Utils.log2(Math.pow(3.0D, numClassesTotal) - 2.0D) - (numClassesTotal * priorEntropy - numClassesRight * entropyRight - numClassesLeft * entropyLeft);
    




    return gain > (Utils.log2(numCutPoints) + delta) / numInstances;
  }
  













  private double[] cutPointsForSubset(Instances instances, int attIndex, int first, int lastPlusOne)
  {
    double currentCutPoint = -1.7976931348623157E308D;double bestCutPoint = -1.0D;
    
    int bestIndex = -1;int numCutPoints = 0;
    double numInstances = 0.0D;
    

    if (lastPlusOne - first < 2) {
      return null;
    }
    

    double[][] counts = new double[2][instances.numClasses()];
    for (int i = first; i < lastPlusOne; i++) {
      numInstances += instances.instance(i).weight();
      counts[1][((int)instances.instance(i).classValue())] += instances.instance(i).weight();
    }
    


    double[] priorCounts = new double[instances.numClasses()];
    System.arraycopy(counts[1], 0, priorCounts, 0, instances.numClasses());
    


    double priorEntropy = ContingencyTables.entropy(priorCounts);
    double bestEntropy = priorEntropy;
    

    double[][] bestCounts = new double[2][instances.numClasses()];
    for (int i = first; i < lastPlusOne - 1; i++) {
      counts[0][((int)instances.instance(i).classValue())] += instances.instance(i).weight();
      
      counts[1][((int)instances.instance(i).classValue())] -= instances.instance(i).weight();
      
      if (instances.instance(i).value(attIndex) < instances.instance(i + 1).value(attIndex))
      {
        currentCutPoint = (instances.instance(i).value(attIndex) + instances.instance(i + 1).value(attIndex)) / 2.0D;
        
        double currentEntropy = ContingencyTables.entropyConditionedOnRows(counts);
        if (currentEntropy < bestEntropy) {
          bestCutPoint = currentCutPoint;
          bestEntropy = currentEntropy;
          bestIndex = i;
          System.arraycopy(counts[0], 0, bestCounts[0], 0, instances.numClasses());
          
          System.arraycopy(counts[1], 0, bestCounts[1], 0, instances.numClasses());
        }
        
        numCutPoints++;
      }
    }
    

    if (!m_UseBetterEncoding) {
      numCutPoints = lastPlusOne - first - 1;
    }
    

    double gain = priorEntropy - bestEntropy;
    if (gain <= 0.0D) {
      return null;
    }
    

    if (((m_UseKononenko) && (KononenkosMDL(priorCounts, bestCounts, numInstances, numCutPoints))) || ((!m_UseKononenko) && (FayyadAndIranisMDL(priorCounts, bestCounts, numInstances, numCutPoints))))
    {




      double[] left = cutPointsForSubset(instances, attIndex, first, bestIndex + 1);
      double[] right = cutPointsForSubset(instances, attIndex, bestIndex + 1, lastPlusOne);
      
      double[] cutPoints;
      
      if ((left == null) && (right == null)) {
        double[] cutPoints = new double[1];
        cutPoints[0] = bestCutPoint;
      } else if (right == null) {
        double[] cutPoints = new double[left.length + 1];
        System.arraycopy(left, 0, cutPoints, 0, left.length);
        cutPoints[left.length] = bestCutPoint;
      } else if (left == null) {
        double[] cutPoints = new double[1 + right.length];
        cutPoints[0] = bestCutPoint;
        System.arraycopy(right, 0, cutPoints, 1, right.length);
      } else {
        cutPoints = new double[left.length + right.length + 1];
        System.arraycopy(left, 0, cutPoints, 0, left.length);
        cutPoints[left.length] = bestCutPoint;
        System.arraycopy(right, 0, cutPoints, left.length + 1, right.length);
      }
      
      return cutPoints;
    }
    return null;
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
      if ((m_DiscretizeCols.isInRange(i)) && (getInputFormat().attribute(i).isNumeric()))
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
          

          Attribute newA = new Attribute(getInputFormat().attribute(i).name(), attribValues);
          

          newA.setWeight(getInputFormat().attribute(i).weight());
          attributes.addElement(newA);

        }
        else if (m_CutPoints[i] == null) {
          FastVector attribValues = new FastVector(1);
          attribValues.addElement("'All'");
          Attribute newA = new Attribute(getInputFormat().attribute(i).name(), attribValues);
          

          newA.setWeight(getInputFormat().attribute(i).weight());
          attributes.addElement(newA);
        } else {
          if (i < getInputFormat().classIndex()) {
            classIndex += m_CutPoints[i].length - 1;
          }
          for (int j = 0; j < m_CutPoints[i].length; j++) {
            FastVector attribValues = new FastVector(2);
            attribValues.addElement("'(-inf-" + Utils.doubleToString(m_CutPoints[i][j], 6) + "]'");
            
            attribValues.addElement("'(" + Utils.doubleToString(m_CutPoints[i][j], 6) + "-inf)'");
            
            Attribute newA = new Attribute(getInputFormat().attribute(i).name() + "_" + (j + 1), attribValues);
            

            newA.setWeight(getInputFormat().attribute(i).weight());
            attributes.addElement(newA);
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
      if ((m_DiscretizeCols.isInRange(i)) && (getInputFormat().attribute(i).isNumeric()))
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
    return RevisionUtils.extract("$Revision: 9090 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new Discretize(), argv);
  }
}

package weka.filters.supervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.UnassignedClassException;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;



































































public class NominalToBinary
  extends Filter
  implements SupervisedFilter, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -5004607029857673950L;
  private int[][] m_Indices = (int[][])null;
  

  private boolean m_Numeric = true;
  

  private boolean m_TransformAll = false;
  

  private boolean m_needToTransform = false;
  


  public NominalToBinary() {}
  


  public String globalInfo()
  {
    return "Converts all nominal attributes into binary numeric attributes. An attribute with k values is transformed into k binary attributes if the class is nominal (using the one-attribute-per-value approach). Binary attributes are left binary, if option '-A' is not given.If the class is numeric, k - 1 new binary attributes are generated in the manner described in \"Classification and Regression Trees\" by Breiman et al. (i.e. taking the average class value associated with each attribute value into account)\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  

















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.BOOK);
    result.setValue(TechnicalInformation.Field.AUTHOR, "L. Breiman and J.H. Friedman and R.A. Olshen and C.J. Stone");
    result.setValue(TechnicalInformation.Field.TITLE, "Classification and Regression Trees");
    result.setValue(TechnicalInformation.Field.YEAR, "1984");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Wadsworth Inc");
    result.setValue(TechnicalInformation.Field.ISBN, "0412048418");
    
    return result;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  










  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    if (instanceInfo.classIndex() < 0) {
      throw new UnassignedClassException("No class has been assigned to the instances");
    }
    setOutputFormat();
    m_Indices = ((int[][])null);
    if (instanceInfo.classAttribute().isNominal()) {
      return true;
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
    if ((m_Indices != null) || (getInputFormat().classAttribute().isNominal()))
    {
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
    if ((m_Indices == null) && (getInputFormat().classAttribute().isNumeric()))
    {
      computeAverageClassValues();
      setOutputFormat();
      


      for (int i = 0; i < getInputFormat().numInstances(); i++) {
        convertInstance(getInputFormat().instance(i));
      }
    }
    flushInput();
    
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tSets if binary attributes are to be coded as nominal ones.", "N", 0, "-N"));
    


    newVector.addElement(new Option("\tFor each nominal value a new attribute is created, \n\tnot only if there are more than 2 values.", "A", 0, "-A"));
    



    return newVector.elements();
  }
  


















  public void setOptions(String[] options)
    throws Exception
  {
    setBinaryAttributesNominal(Utils.getFlag('N', options));
    
    setTransformAllValues(Utils.getFlag('A', options));
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  




  public String[] getOptions()
  {
    String[] options = new String[1];
    int current = 0;
    
    if (getBinaryAttributesNominal()) {
      options[(current++)] = "-N";
    }
    
    if (getTransformAllValues()) {
      options[(current++)] = "-A";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String binaryAttributesNominalTipText()
  {
    return "Whether resulting binary attributes will be nominal.";
  }
  





  public boolean getBinaryAttributesNominal()
  {
    return !m_Numeric;
  }
  





  public void setBinaryAttributesNominal(boolean bool)
  {
    m_Numeric = (!bool);
  }
  





  public String transformAllValuesTipText()
  {
    return "Whether all nominal values are turned into new attributes, not only if there are more than 2.";
  }
  






  public boolean getTransformAllValues()
  {
    return m_TransformAll;
  }
  






  public void setTransformAllValues(boolean bool)
  {
    m_TransformAll = bool;
  }
  





  private void computeAverageClassValues()
  {
    double[][] avgClassValues = new double[getInputFormat().numAttributes()][0];
    m_Indices = new int[getInputFormat().numAttributes()][0];
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if (att.isNominal()) {
        avgClassValues[j] = new double[att.numValues()];
        double[] counts = new double[att.numValues()];
        for (int i = 0; i < getInputFormat().numInstances(); i++) {
          Instance instance = getInputFormat().instance(i);
          if ((!instance.classIsMissing()) && (!instance.isMissing(j)))
          {
            counts[((int)instance.value(j))] += instance.weight();
            avgClassValues[j][((int)instance.value(j))] += instance.weight() * instance.classValue();
          }
        }
        
        double sum = Utils.sum(avgClassValues[j]);
        double totalCounts = Utils.sum(counts);
        if (Utils.gr(totalCounts, 0.0D)) {
          for (int k = 0; k < att.numValues(); k++) {
            if (Utils.gr(counts[k], 0.0D)) {
              avgClassValues[j][k] /= counts[k];
            } else {
              avgClassValues[j][k] = (sum / totalCounts);
            }
          }
        }
        m_Indices[j] = Utils.sort(avgClassValues[j]);
      }
    }
  }
  

  private void setOutputFormat()
  {
    if (getInputFormat().classAttribute().isNominal()) {
      setOutputFormatNominal();
    } else {
      setOutputFormatNumeric();
    }
  }
  






  private void convertInstance(Instance inst)
  {
    if (getInputFormat().classAttribute().isNominal()) {
      convertInstanceNominal(inst);
    } else {
      convertInstanceNumeric(inst);
    }
  }
  










  private void setOutputFormatNominal()
  {
    m_needToTransform = false;
    for (int i = 0; i < getInputFormat().numAttributes(); i++) {
      Attribute att = getInputFormat().attribute(i);
      if ((att.isNominal()) && (i != getInputFormat().classIndex()) && ((att.numValues() > 2) || (m_TransformAll) || (m_Numeric)))
      {
        m_needToTransform = true;
        break;
      }
    }
    
    if (!m_needToTransform) {
      setOutputFormat(getInputFormat());
      return;
    }
    
    int newClassIndex = getInputFormat().classIndex();
    FastVector newAtts = new FastVector();
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if ((!att.isNominal()) || (j == getInputFormat().classIndex()))
      {
        newAtts.addElement(att.copy());
      }
      else if ((att.numValues() <= 2) && (!m_TransformAll)) {
        if (m_Numeric) {
          newAtts.addElement(new Attribute(att.name()));
        } else {
          newAtts.addElement(att.copy());
        }
      }
      else {
        if (j < getInputFormat().classIndex()) {
          newClassIndex += att.numValues() - 1;
        }
        

        for (int k = 0; k < att.numValues(); k++) {
          StringBuffer attributeName = new StringBuffer(att.name() + "=");
          
          attributeName.append(att.value(k));
          if (m_Numeric) {
            newAtts.addElement(new Attribute(attributeName.toString()));
          }
          else {
            FastVector vals = new FastVector(2);
            vals.addElement("f");vals.addElement("t");
            newAtts.addElement(new Attribute(attributeName.toString(), vals));
          }
        }
      }
    }
    

    Instances outputFormat = new Instances(getInputFormat().relationName(), newAtts, 0);
    
    outputFormat.setClassIndex(newClassIndex);
    setOutputFormat(outputFormat);
  }
  



  private void setOutputFormatNumeric()
  {
    if (m_Indices == null) {
      setOutputFormat(null);
      return;
    }
    







    m_needToTransform = false;
    for (int i = 0; i < getInputFormat().numAttributes(); i++) {
      Attribute att = getInputFormat().attribute(i);
      if ((att.isNominal()) && ((att.numValues() > 2) || (m_Numeric) || (m_TransformAll))) {
        m_needToTransform = true;
        break;
      }
    }
    
    if (!m_needToTransform) {
      setOutputFormat(getInputFormat());
      return;
    }
    
    int newClassIndex = getInputFormat().classIndex();
    FastVector newAtts = new FastVector();
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if ((!att.isNominal()) || (j == getInputFormat().classIndex()))
      {
        newAtts.addElement(att.copy());
      } else {
        if (j < getInputFormat().classIndex()) {
          newClassIndex += att.numValues() - 2;
        }
        

        for (int k = 1; k < att.numValues(); k++) {
          StringBuffer attributeName = new StringBuffer(att.name() + "=");
          
          for (int l = k; l < att.numValues(); l++) {
            if (l > k) {
              attributeName.append(',');
            }
            attributeName.append(att.value(m_Indices[j][l]));
          }
          if (m_Numeric) {
            newAtts.addElement(new Attribute(attributeName.toString()));
          }
          else {
            FastVector vals = new FastVector(2);
            vals.addElement("f");vals.addElement("t");
            newAtts.addElement(new Attribute(attributeName.toString(), vals));
          }
        }
      }
    }
    
    Instances outputFormat = new Instances(getInputFormat().relationName(), newAtts, 0);
    
    outputFormat.setClassIndex(newClassIndex);
    setOutputFormat(outputFormat);
  }
  






  private void convertInstanceNominal(Instance instance)
  {
    if (!m_needToTransform) {
      push(instance);
      return;
    }
    
    double[] vals = new double[outputFormatPeek().numAttributes()];
    int attSoFar = 0;
    
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if ((!att.isNominal()) || (j == getInputFormat().classIndex())) {
        vals[attSoFar] = instance.value(j);
        attSoFar++;
      }
      else if ((att.numValues() <= 2) && (!m_TransformAll)) {
        vals[attSoFar] = instance.value(j);
        attSoFar++;
      } else {
        if (instance.isMissing(j)) {
          for (int k = 0; k < att.numValues(); k++) {
            vals[(attSoFar + k)] = instance.value(j);
          }
        } else {
          for (int k = 0; k < att.numValues(); k++) {
            if (k == (int)instance.value(j)) {
              vals[(attSoFar + k)] = 1.0D;
            } else {
              vals[(attSoFar + k)] = 0.0D;
            }
          }
        }
        attSoFar += att.numValues();
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
  






  private void convertInstanceNumeric(Instance instance)
  {
    if (!m_needToTransform) {
      push(instance);
      return;
    }
    
    double[] vals = new double[outputFormatPeek().numAttributes()];
    int attSoFar = 0;
    
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if ((!att.isNominal()) || (j == getInputFormat().classIndex())) {
        vals[attSoFar] = instance.value(j);
        attSoFar++;
      } else {
        if (instance.isMissing(j)) {
          for (int k = 0; k < att.numValues() - 1; k++) {
            vals[(attSoFar + k)] = instance.value(j);
          }
        } else {
          int k = 0;
          while ((int)instance.value(j) != m_Indices[j][k]) {
            vals[(attSoFar + k)] = 1.0D;
            k++;
          }
          while (k < att.numValues() - 1) {
            vals[(attSoFar + k)] = 0.0D;
            k++;
          }
        }
        attSoFar += att.numValues() - 1;
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
    return RevisionUtils.extract("$Revision: 8094 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new NominalToBinary(), argv);
  }
}

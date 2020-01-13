package weka.core;

import java.io.PrintStream;
import java.util.Enumeration;













































public class BinarySparseInstance
  extends SparseInstance
{
  private static final long serialVersionUID = -5297388762342528737L;
  
  public BinarySparseInstance(Instance instance)
  {
    m_Weight = m_Weight;
    m_Dataset = null;
    m_NumAttributes = instance.numAttributes();
    if ((instance instanceof SparseInstance)) {
      m_AttValues = null;
      m_Indices = m_Indices;
    } else {
      int[] tempIndices = new int[instance.numAttributes()];
      int vals = 0;
      for (int i = 0; i < instance.numAttributes(); i++) {
        if (instance.value(i) != 0.0D) {
          tempIndices[vals] = i;
          vals++;
        }
      }
      m_AttValues = null;
      m_Indices = new int[vals];
      System.arraycopy(tempIndices, 0, m_Indices, 0, vals);
    }
  }
  









  public BinarySparseInstance(SparseInstance instance)
  {
    m_AttValues = null;
    m_Indices = m_Indices;
    m_Weight = m_Weight;
    m_NumAttributes = m_NumAttributes;
    m_Dataset = null;
  }
  









  public BinarySparseInstance(double weight, double[] attValues)
  {
    m_Weight = weight;
    m_Dataset = null;
    m_NumAttributes = attValues.length;
    int[] tempIndices = new int[m_NumAttributes];
    int vals = 0;
    for (int i = 0; i < m_NumAttributes; i++) {
      if (attValues[i] != 0.0D) {
        tempIndices[vals] = i;
        vals++;
      }
    }
    m_AttValues = null;
    m_Indices = new int[vals];
    System.arraycopy(tempIndices, 0, m_Indices, 0, vals);
  }
  










  public BinarySparseInstance(double weight, int[] indices, int maxNumValues)
  {
    m_AttValues = null;
    m_Indices = indices;
    m_Weight = weight;
    m_NumAttributes = maxNumValues;
    m_Dataset = null;
  }
  







  public BinarySparseInstance(int numAttributes)
  {
    m_AttValues = null;
    m_NumAttributes = numAttributes;
    m_Indices = new int[numAttributes];
    for (int i = 0; i < m_Indices.length; i++) {
      m_Indices[i] = i;
    }
    m_Weight = 1.0D;
    m_Dataset = null;
  }
  






  public Object copy()
  {
    return new BinarySparseInstance(this);
  }
  







  public Instance mergeInstance(Instance inst)
  {
    int[] indices = new int[numValues() + inst.numValues()];
    
    int m = 0;
    for (int j = 0; j < numValues(); j++) {
      indices[(m++)] = index(j);
    }
    for (int j = 0; j < inst.numValues(); j++) {
      if (inst.valueSparse(j) != 0.0D) {
        indices[(m++)] = (numAttributes() + inst.index(j));
      }
    }
    
    if (m != indices.length)
    {
      int[] newInd = new int[m];
      System.arraycopy(indices, 0, newInd, 0, m);
      indices = newInd;
    }
    return new BinarySparseInstance(1.0D, indices, numAttributes() + inst.numAttributes());
  }
  










  public void replaceMissingValues(double[] array) {}
  









  public void setValue(int attIndex, double value)
  {
    int index = locateIndex(attIndex);
    
    if ((index >= 0) && (m_Indices[index] == attIndex)) {
      if (value == 0.0D) {
        int[] tempIndices = new int[m_Indices.length - 1];
        System.arraycopy(m_Indices, 0, tempIndices, 0, index);
        System.arraycopy(m_Indices, index + 1, tempIndices, index, m_Indices.length - index - 1);
        
        m_Indices = tempIndices;
      }
    }
    else if (value != 0.0D) {
      int[] tempIndices = new int[m_Indices.length + 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index + 1);
      tempIndices[(index + 1)] = attIndex;
      System.arraycopy(m_Indices, index + 1, tempIndices, index + 2, m_Indices.length - index - 1);
      
      m_Indices = tempIndices;
    }
  }
  











  public void setValueSparse(int indexOfIndex, double value)
  {
    if (value == 0.0D) {
      int[] tempIndices = new int[m_Indices.length - 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, indexOfIndex);
      System.arraycopy(m_Indices, indexOfIndex + 1, tempIndices, indexOfIndex, m_Indices.length - indexOfIndex - 1);
      
      m_Indices = tempIndices;
    }
  }
  





  public double[] toDoubleArray()
  {
    double[] newValues = new double[m_NumAttributes];
    for (int i = 0; i < m_Indices.length; i++) {
      newValues[m_Indices[i]] = 1.0D;
    }
    return newValues;
  }
  








  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append('{');
    for (int i = 0; i < m_Indices.length; i++) {
      if (i > 0) {
        text.append(",");
      }
      if (m_Dataset == null) {
        text.append(m_Indices[i] + " 1");
      }
      else if ((m_Dataset.attribute(m_Indices[i]).isNominal()) || (m_Dataset.attribute(m_Indices[i]).isString()))
      {
        text.append(m_Indices[i] + " " + Utils.quote(m_Dataset.attribute(m_Indices[i]).value(1)));
      }
      else
      {
        text.append(m_Indices[i] + " 1");
      }
    }
    
    text.append('}');
    if (m_Weight != 1.0D) {
      text.append(",{" + Utils.doubleToString(m_Weight, 6) + "}");
    }
    return text.toString();
  }
  








  public double value(int attIndex)
  {
    int index = locateIndex(attIndex);
    if ((index >= 0) && (m_Indices[index] == attIndex)) {
      return 1.0D;
    }
    return 0.0D;
  }
  










  public final double valueSparse(int indexOfIndex)
  {
    int index = m_Indices[indexOfIndex];
    return 1.0D;
  }
  






  void forceDeleteAttributeAt(int position)
  {
    int index = locateIndex(position);
    
    m_NumAttributes -= 1;
    if ((index >= 0) && (m_Indices[index] == position)) {
      int[] tempIndices = new int[m_Indices.length - 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index);
      for (int i = index; i < m_Indices.length - 1; i++) {
        tempIndices[i] = (m_Indices[(i + 1)] - 1);
      }
      m_Indices = tempIndices;
    } else {
      int[] tempIndices = new int[m_Indices.length];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index + 1);
      for (int i = index + 1; i < m_Indices.length - 1; i++) {
        tempIndices[i] = (m_Indices[i] - 1);
      }
      m_Indices = tempIndices;
    }
  }
  






  void forceInsertAttributeAt(int position)
  {
    int index = locateIndex(position);
    
    m_NumAttributes += 1;
    if ((index >= 0) && (m_Indices[index] == position)) {
      int[] tempIndices = new int[m_Indices.length + 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index);
      tempIndices[index] = position;
      for (int i = index; i < m_Indices.length; i++) {
        tempIndices[(i + 1)] = (m_Indices[i] + 1);
      }
      m_Indices = tempIndices;
    } else {
      int[] tempIndices = new int[m_Indices.length + 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index + 1);
      tempIndices[(index + 1)] = position;
      for (int i = index + 1; i < m_Indices.length; i++) {
        tempIndices[(i + 1)] = (m_Indices[i] + 1);
      }
      m_Indices = tempIndices;
    }
  }
  






  public static void main(String[] options)
  {
    try
    {
      Attribute length = new Attribute("length");
      Attribute weight = new Attribute("weight");
      

      FastVector my_nominal_values = new FastVector(3);
      my_nominal_values.addElement("first");
      my_nominal_values.addElement("second");
      

      Attribute position = new Attribute("position", my_nominal_values);
      

      FastVector attributes = new FastVector(3);
      attributes.addElement(length);
      attributes.addElement(weight);
      attributes.addElement(position);
      

      Instances race = new Instances("race", attributes, 0);
      

      race.setClassIndex(position.index());
      

      BinarySparseInstance inst = new BinarySparseInstance(3);
      

      inst.setValue(length, 5.3D);
      inst.setValue(weight, 300.0D);
      inst.setValue(position, "first");
      

      inst.setDataset(race);
      

      System.out.println("The instance: " + inst);
      

      System.out.println("First attribute: " + inst.attribute(0));
      

      System.out.println("Class attribute: " + inst.classAttribute());
      

      System.out.println("Class index: " + inst.classIndex());
      

      System.out.println("Class is missing: " + inst.classIsMissing());
      

      System.out.println("Class value (internal format): " + inst.classValue());
      

      SparseInstance copy = (SparseInstance)inst.copy();
      System.out.println("Shallow copy: " + copy);
      

      copy.setDataset(inst.dataset());
      System.out.println("Shallow copy with dataset set: " + copy);
      

      System.out.print("All stored values in internal format: ");
      for (int i = 0; i < inst.numValues(); i++) {
        if (i > 0) {
          System.out.print(",");
        }
        System.out.print(inst.valueSparse(i));
      }
      System.out.println();
      

      System.out.print("All values set to zero: ");
      while (inst.numValues() > 0) {
        inst.setValueSparse(0, 0.0D);
      }
      for (int i = 0; i < inst.numValues(); i++) {
        if (i > 0) {
          System.out.print(",");
        }
        System.out.print(inst.valueSparse(i));
      }
      System.out.println();
      

      System.out.print("All values set to one: ");
      for (int i = 0; i < inst.numAttributes(); i++) {
        inst.setValue(i, 1.0D);
      }
      for (int i = 0; i < inst.numValues(); i++) {
        if (i > 0) {
          System.out.print(",");
        }
        System.out.print(inst.valueSparse(i));
      }
      System.out.println();
      

      copy.setDataset(null);
      copy.deleteAttributeAt(0);
      copy.insertAttributeAt(0);
      copy.setDataset(inst.dataset());
      System.out.println("Copy with first attribute deleted and inserted: " + copy);
      

      copy.setDataset(null);
      copy.deleteAttributeAt(1);
      copy.insertAttributeAt(1);
      copy.setDataset(inst.dataset());
      System.out.println("Copy with second attribute deleted and inserted: " + copy);
      

      copy.setDataset(null);
      copy.deleteAttributeAt(2);
      copy.insertAttributeAt(2);
      copy.setDataset(inst.dataset());
      System.out.println("Copy with third attribute deleted and inserted: " + copy);
      

      System.out.println("Enumerating attributes (leaving out class):");
      Enumeration enu = inst.enumerateAttributes();
      while (enu.hasMoreElements()) {
        Attribute att = (Attribute)enu.nextElement();
        System.out.println(att);
      }
      

      System.out.println("Header of original and copy equivalent: " + inst.equalHeaders(copy));
      


      System.out.println("Length of copy missing: " + copy.isMissing(length));
      System.out.println("Weight of copy missing: " + copy.isMissing(weight.index()));
      System.out.println("Length of copy missing: " + Instance.isMissingValue(copy.value(length)));
      
      System.out.println("Missing value coded as: " + Instance.missingValue());
      

      System.out.println("Number of attributes: " + copy.numAttributes());
      System.out.println("Number of classes: " + copy.numClasses());
      

      double[] meansAndModes = { 2.0D, 3.0D, 0.0D };
      copy.replaceMissingValues(meansAndModes);
      System.out.println("Copy with missing value replaced: " + copy);
      

      copy.setClassMissing();
      System.out.println("Copy with missing class: " + copy);
      copy.setClassValue(0.0D);
      System.out.println("Copy with class value set to first value: " + copy);
      copy.setClassValue("second");
      System.out.println("Copy with class value set to \"second\": " + copy);
      copy.setMissing(1);
      System.out.println("Copy with second attribute set to be missing: " + copy);
      copy.setMissing(length);
      System.out.println("Copy with length set to be missing: " + copy);
      copy.setValue(0, 0.0D);
      System.out.println("Copy with first attribute set to 0: " + copy);
      copy.setValue(weight, 1.0D);
      System.out.println("Copy with weight attribute set to 1: " + copy);
      copy.setValue(position, "second");
      System.out.println("Copy with position set to \"second\": " + copy);
      copy.setValue(2, "first");
      System.out.println("Copy with last attribute set to \"first\": " + copy);
      System.out.println("Current weight of instance copy: " + copy.weight());
      copy.setWeight(2.0D);
      System.out.println("Current weight of instance copy (set to 2): " + copy.weight());
      System.out.println("Last value of copy: " + copy.toString(2));
      System.out.println("Value of position for copy: " + copy.toString(position));
      System.out.println("Last value of copy (internal format): " + copy.value(2));
      System.out.println("Value of position for copy (internal format): " + copy.value(position));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
}

package weka.core;

import java.io.PrintStream;
import java.util.Enumeration;



















































public class SparseInstance
  extends Instance
{
  private static final long serialVersionUID = -3579051291332630149L;
  protected int[] m_Indices;
  protected int m_NumAttributes;
  
  protected SparseInstance() {}
  
  public SparseInstance(Instance instance)
  {
    m_Weight = m_Weight;
    m_Dataset = null;
    m_NumAttributes = instance.numAttributes();
    if ((instance instanceof SparseInstance)) {
      m_AttValues = m_AttValues;
      m_Indices = m_Indices;
    } else {
      double[] tempValues = new double[instance.numAttributes()];
      int[] tempIndices = new int[instance.numAttributes()];
      int vals = 0;
      for (int i = 0; i < instance.numAttributes(); i++) {
        if (instance.value(i) != 0.0D) {
          tempValues[vals] = instance.value(i);
          tempIndices[vals] = i;
          vals++;
        }
      }
      m_AttValues = new double[vals];
      m_Indices = new int[vals];
      System.arraycopy(tempValues, 0, m_AttValues, 0, vals);
      System.arraycopy(tempIndices, 0, m_Indices, 0, vals);
    }
  }
  









  public SparseInstance(SparseInstance instance)
  {
    m_AttValues = m_AttValues;
    m_Indices = m_Indices;
    m_Weight = m_Weight;
    m_NumAttributes = m_NumAttributes;
    m_Dataset = null;
  }
  









  public SparseInstance(double weight, double[] attValues)
  {
    m_Weight = weight;
    m_Dataset = null;
    m_NumAttributes = attValues.length;
    double[] tempValues = new double[m_NumAttributes];
    int[] tempIndices = new int[m_NumAttributes];
    int vals = 0;
    for (int i = 0; i < m_NumAttributes; i++) {
      if (attValues[i] != 0.0D) {
        tempValues[vals] = attValues[i];
        tempIndices[vals] = i;
        vals++;
      }
    }
    m_AttValues = new double[vals];
    m_Indices = new int[vals];
    System.arraycopy(tempValues, 0, m_AttValues, 0, vals);
    System.arraycopy(tempIndices, 0, m_Indices, 0, vals);
  }
  














  public SparseInstance(double weight, double[] attValues, int[] indices, int maxNumValues)
  {
    int vals = 0;
    m_AttValues = new double[attValues.length];
    m_Indices = new int[indices.length];
    for (int i = 0; i < attValues.length; i++) {
      if (attValues[i] != 0.0D) {
        m_AttValues[vals] = attValues[i];
        m_Indices[vals] = indices[i];
        vals++;
      }
    }
    if (vals != attValues.length)
    {
      double[] newVals = new double[vals];
      System.arraycopy(m_AttValues, 0, newVals, 0, vals);
      m_AttValues = newVals;
      int[] newIndices = new int[vals];
      System.arraycopy(m_Indices, 0, newIndices, 0, vals);
      m_Indices = newIndices;
    }
    m_Weight = weight;
    m_NumAttributes = maxNumValues;
    m_Dataset = null;
  }
  







  public SparseInstance(int numAttributes)
  {
    m_AttValues = new double[numAttributes];
    m_NumAttributes = numAttributes;
    m_Indices = new int[numAttributes];
    for (int i = 0; i < m_AttValues.length; i++) {
      m_AttValues[i] = NaN.0D;
      m_Indices[i] = i;
    }
    m_Weight = 1.0D;
    m_Dataset = null;
  }
  








  public Attribute attributeSparse(int indexOfIndex)
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.attribute(m_Indices[indexOfIndex]);
  }
  








  public Object copy()
  {
    Instance result = new SparseInstance(this);
    m_Dataset = m_Dataset;
    return result;
  }
  






  public int index(int position)
  {
    return m_Indices[position];
  }
  





  public boolean isMissing(int attIndex)
  {
    if (Double.isNaN(value(attIndex))) {
      return true;
    }
    return false;
  }
  







  public int locateIndex(int index)
  {
    int min = 0;int max = m_Indices.length - 1;
    
    if (max == -1) {
      return -1;
    }
    

    while ((m_Indices[min] <= index) && (m_Indices[max] >= index)) {
      int current = (max + min) / 2;
      if (m_Indices[current] > index) {
        max = current - 1;
      } else if (m_Indices[current] < index) {
        min = current + 1;
      } else {
        return current;
      }
    }
    if (m_Indices[max] < index) {
      return max;
    }
    return min - 1;
  }
  








  public Instance mergeInstance(Instance inst)
  {
    double[] values = new double[numValues() + inst.numValues()];
    int[] indices = new int[numValues() + inst.numValues()];
    
    int m = 0;
    for (int j = 0; j < numValues(); m++) {
      values[m] = valueSparse(j);
      indices[m] = index(j);j++;
    }
    
    for (int j = 0; j < inst.numValues(); m++) {
      values[m] = inst.valueSparse(j);
      indices[m] = (numAttributes() + inst.index(j));j++;
    }
    

    return new SparseInstance(1.0D, values, indices, numAttributes() + inst.numAttributes());
  }
  






  public int numAttributes()
  {
    return m_NumAttributes;
  }
  





  public int numValues()
  {
    return m_Indices.length;
  }
  









  public void replaceMissingValues(double[] array)
  {
    if ((array == null) || (array.length != m_NumAttributes)) {
      throw new IllegalArgumentException("Unequal number of attributes!");
    }
    double[] tempValues = new double[m_AttValues.length];
    int[] tempIndices = new int[m_AttValues.length];
    int vals = 0;
    for (int i = 0; i < m_AttValues.length; i++) {
      if (isMissingValue(m_AttValues[i])) {
        if (array[m_Indices[i]] != 0.0D) {
          tempValues[vals] = array[m_Indices[i]];
          tempIndices[vals] = m_Indices[i];
          vals++;
        }
      } else {
        tempValues[vals] = m_AttValues[i];
        tempIndices[vals] = m_Indices[i];
        vals++;
      }
    }
    m_AttValues = new double[vals];
    m_Indices = new int[vals];
    System.arraycopy(tempValues, 0, m_AttValues, 0, vals);
    System.arraycopy(tempIndices, 0, m_Indices, 0, vals);
  }
  










  public void setValue(int attIndex, double value)
  {
    int index = locateIndex(attIndex);
    
    if ((index >= 0) && (m_Indices[index] == attIndex)) {
      if (value != 0.0D) {
        double[] tempValues = new double[m_AttValues.length];
        System.arraycopy(m_AttValues, 0, tempValues, 0, m_AttValues.length);
        tempValues[index] = value;
        m_AttValues = tempValues;
      } else {
        double[] tempValues = new double[m_AttValues.length - 1];
        int[] tempIndices = new int[m_Indices.length - 1];
        System.arraycopy(m_AttValues, 0, tempValues, 0, index);
        System.arraycopy(m_Indices, 0, tempIndices, 0, index);
        System.arraycopy(m_AttValues, index + 1, tempValues, index, m_AttValues.length - index - 1);
        
        System.arraycopy(m_Indices, index + 1, tempIndices, index, m_Indices.length - index - 1);
        
        m_AttValues = tempValues;
        m_Indices = tempIndices;
      }
    }
    else if (value != 0.0D) {
      double[] tempValues = new double[m_AttValues.length + 1];
      int[] tempIndices = new int[m_Indices.length + 1];
      System.arraycopy(m_AttValues, 0, tempValues, 0, index + 1);
      System.arraycopy(m_Indices, 0, tempIndices, 0, index + 1);
      tempIndices[(index + 1)] = attIndex;
      tempValues[(index + 1)] = value;
      System.arraycopy(m_AttValues, index + 1, tempValues, index + 2, m_AttValues.length - index - 1);
      
      System.arraycopy(m_Indices, index + 1, tempIndices, index + 2, m_Indices.length - index - 1);
      
      m_AttValues = tempValues;
      m_Indices = tempIndices;
    }
  }
  











  public void setValueSparse(int indexOfIndex, double value)
  {
    if (value != 0.0D) {
      double[] tempValues = new double[m_AttValues.length];
      System.arraycopy(m_AttValues, 0, tempValues, 0, m_AttValues.length);
      m_AttValues = tempValues;
      m_AttValues[indexOfIndex] = value;
    } else {
      double[] tempValues = new double[m_AttValues.length - 1];
      int[] tempIndices = new int[m_Indices.length - 1];
      System.arraycopy(m_AttValues, 0, tempValues, 0, indexOfIndex);
      System.arraycopy(m_Indices, 0, tempIndices, 0, indexOfIndex);
      System.arraycopy(m_AttValues, indexOfIndex + 1, tempValues, indexOfIndex, m_AttValues.length - indexOfIndex - 1);
      
      System.arraycopy(m_Indices, indexOfIndex + 1, tempIndices, indexOfIndex, m_Indices.length - indexOfIndex - 1);
      
      m_AttValues = tempValues;
      m_Indices = tempIndices;
    }
  }
  





  public double[] toDoubleArray()
  {
    double[] newValues = new double[m_NumAttributes];
    for (int i = 0; i < m_AttValues.length; i++) {
      newValues[m_Indices[i]] = m_AttValues[i];
    }
    return newValues;
  }
  








  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append('{');
    for (int i = 0; i < m_Indices.length; i++) {
      if (i > 0) text.append(",");
      if (isMissingValue(m_AttValues[i])) {
        text.append(m_Indices[i] + " ?");
      }
      else if (m_Dataset == null) {
        text.append(m_Indices[i] + " " + Utils.doubleToString(m_AttValues[i], 6));

      }
      else if ((m_Dataset.attribute(m_Indices[i]).isNominal()) || (m_Dataset.attribute(m_Indices[i]).isString()) || (m_Dataset.attribute(m_Indices[i]).isDate()))
      {
        try
        {
          text.append(m_Indices[i] + " " + Utils.quote(stringValue(m_Indices[i])));
        }
        catch (Exception e) {
          e.printStackTrace();
          System.err.println(new Instances(m_Dataset, 0));
          System.err.println("Att:" + m_Indices[i] + " Val:" + valueSparse(i));
          throw new Error("This should never happen!");
        }
      } else if (m_Dataset.attribute(m_Indices[i]).isRelationValued()) {
        try {
          text.append(m_Indices[i] + " " + Utils.quote(m_Dataset.attribute(m_Indices[i]).relation((int)valueSparse(i)).stringWithoutHeader()));

        }
        catch (Exception e)
        {
          e.printStackTrace();
          System.err.println(new Instances(m_Dataset, 0));
          System.err.println("Att:" + m_Indices[i] + " Val:" + valueSparse(i));
          throw new Error("This should never happen!");
        }
      } else {
        text.append(m_Indices[i] + " " + Utils.doubleToString(m_AttValues[i], 6));
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
      return m_AttValues[index];
    }
    return 0.0D;
  }
  







  void forceDeleteAttributeAt(int position)
  {
    int index = locateIndex(position);
    
    m_NumAttributes -= 1;
    if ((index >= 0) && (m_Indices[index] == position)) {
      int[] tempIndices = new int[m_Indices.length - 1];
      double[] tempValues = new double[m_AttValues.length - 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index);
      System.arraycopy(m_AttValues, 0, tempValues, 0, index);
      for (int i = index; i < m_Indices.length - 1; i++) {
        tempIndices[i] = (m_Indices[(i + 1)] - 1);
        tempValues[i] = m_AttValues[(i + 1)];
      }
      m_Indices = tempIndices;
      m_AttValues = tempValues;
    } else {
      int[] tempIndices = new int[m_Indices.length];
      double[] tempValues = new double[m_AttValues.length];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index + 1);
      System.arraycopy(m_AttValues, 0, tempValues, 0, index + 1);
      for (int i = index + 1; i < m_Indices.length; i++) {
        tempIndices[i] = (m_Indices[i] - 1);
        tempValues[i] = m_AttValues[i];
      }
      m_Indices = tempIndices;
      m_AttValues = tempValues;
    }
  }
  






  void forceInsertAttributeAt(int position)
  {
    int index = locateIndex(position);
    
    m_NumAttributes += 1;
    if ((index >= 0) && (m_Indices[index] == position)) {
      int[] tempIndices = new int[m_Indices.length + 1];
      double[] tempValues = new double[m_AttValues.length + 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index);
      System.arraycopy(m_AttValues, 0, tempValues, 0, index);
      tempIndices[index] = position;
      tempValues[index] = NaN.0D;
      for (int i = index; i < m_Indices.length; i++) {
        tempIndices[(i + 1)] = (m_Indices[i] + 1);
        tempValues[(i + 1)] = m_AttValues[i];
      }
      m_Indices = tempIndices;
      m_AttValues = tempValues;
    } else {
      int[] tempIndices = new int[m_Indices.length + 1];
      double[] tempValues = new double[m_AttValues.length + 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index + 1);
      System.arraycopy(m_AttValues, 0, tempValues, 0, index + 1);
      tempIndices[(index + 1)] = position;
      tempValues[(index + 1)] = NaN.0D;
      for (int i = index + 1; i < m_Indices.length; i++) {
        tempIndices[(i + 1)] = (m_Indices[i] + 1);
        tempValues[(i + 1)] = m_AttValues[i];
      }
      m_Indices = tempIndices;
      m_AttValues = tempValues;
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
      my_nominal_values.addElement("third");
      

      Attribute position = new Attribute("position", my_nominal_values);
      

      FastVector attributes = new FastVector(3);
      attributes.addElement(length);
      attributes.addElement(weight);
      attributes.addElement(position);
      

      Instances race = new Instances("race", attributes, 0);
      

      race.setClassIndex(position.index());
      

      SparseInstance inst = new SparseInstance(3);
      

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
      copy.setClassValue("third");
      System.out.println("Copy with class value set to \"third\": " + copy);
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
    return RevisionUtils.extract("$Revision: 5970 $");
  }
}

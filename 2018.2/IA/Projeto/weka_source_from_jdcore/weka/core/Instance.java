package weka.core;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;




















































































public class Instance
  implements Copyable, Serializable, RevisionHandler
{
  static final long serialVersionUID = 1482635194499365122L;
  protected static final double MISSING_VALUE = NaN.0D;
  protected Instances m_Dataset;
  protected double[] m_AttValues;
  protected double m_Weight;
  
  public Instance(Instance instance)
  {
    m_AttValues = m_AttValues;
    m_Weight = m_Weight;
    m_Dataset = null;
  }
  









  public Instance(double weight, double[] attValues)
  {
    m_AttValues = attValues;
    m_Weight = weight;
    m_Dataset = null;
  }
  









  public Instance(int numAttributes)
  {
    m_AttValues = new double[numAttributes];
    for (int i = 0; i < m_AttValues.length; i++) {
      m_AttValues[i] = NaN.0D;
    }
    m_Weight = 1.0D;
    m_Dataset = null;
  }
  









  public Attribute attribute(int index)
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.attribute(index);
  }
  










  public Attribute attributeSparse(int indexOfIndex)
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.attribute(indexOfIndex);
  }
  








  public Attribute classAttribute()
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.classAttribute();
  }
  








  public int classIndex()
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.classIndex();
  }
  








  public boolean classIsMissing()
  {
    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    return isMissing(classIndex());
  }
  











  public double classValue()
  {
    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    return value(classIndex());
  }
  











  public Object copy()
  {
    Instance result = new Instance(this);
    m_Dataset = m_Dataset;
    return result;
  }
  








  public Instances dataset()
  {
    return m_Dataset;
  }
  











  public void deleteAttributeAt(int position)
  {
    if (m_Dataset != null) {
      throw new RuntimeException("Instance has access to a dataset!");
    }
    forceDeleteAttributeAt(position);
  }
  








  public Enumeration enumerateAttributes()
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.enumerateAttributes();
  }
  










  public boolean equalHeaders(Instance inst)
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.equalHeaders(m_Dataset);
  }
  







  public boolean hasMissingValue()
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    for (int i = 0; i < numAttributes(); i++) {
      if ((i != classIndex()) && 
        (isMissing(i))) {
        return true;
      }
    }
    
    return false;
  }
  







  public int index(int position)
  {
    return position;
  }
  













  public void insertAttributeAt(int position)
  {
    if (m_Dataset != null) {
      throw new RuntimeException("Instance has accesss to a dataset!");
    }
    if ((position < 0) || (position > numAttributes()))
    {
      throw new IllegalArgumentException("Can't insert attribute: index out of range");
    }
    
    forceInsertAttributeAt(position);
  }
  






  public boolean isMissing(int attIndex)
  {
    if (Double.isNaN(m_AttValues[attIndex])) {
      return true;
    }
    return false;
  }
  







  public boolean isMissingSparse(int indexOfIndex)
  {
    if (Double.isNaN(m_AttValues[indexOfIndex])) {
      return true;
    }
    return false;
  }
  







  public boolean isMissing(Attribute att)
  {
    return isMissing(att.index());
  }
  






  public static boolean isMissingValue(double val)
  {
    return Double.isNaN(val);
  }
  







  public Instance mergeInstance(Instance inst)
  {
    int m = 0;
    double[] newVals = new double[numAttributes() + inst.numAttributes()];
    for (int j = 0; j < numAttributes(); m++) {
      newVals[m] = value(j);j++;
    }
    for (int j = 0; j < inst.numAttributes(); m++) {
      newVals[m] = inst.value(j);j++;
    }
    return new Instance(1.0D, newVals);
  }
  





  public static double missingValue()
  {
    return NaN.0D;
  }
  






  public int numAttributes()
  {
    return m_AttValues.length;
  }
  









  public int numClasses()
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.numClasses();
  }
  






  public int numValues()
  {
    return m_AttValues.length;
  }
  









  public void replaceMissingValues(double[] array)
  {
    if ((array == null) || (array.length != m_AttValues.length))
    {
      throw new IllegalArgumentException("Unequal number of attributes!");
    }
    freshAttributeVector();
    for (int i = 0; i < m_AttValues.length; i++) {
      if (isMissing(i)) {
        m_AttValues[i] = array[i];
      }
    }
  }
  










  public void setClassMissing()
  {
    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    setMissing(classIndex());
  }
  













  public void setClassValue(double value)
  {
    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    setValue(classIndex(), value);
  }
  















  public final void setClassValue(String value)
  {
    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    setValue(classIndex(), value);
  }
  








  public final void setDataset(Instances instances)
  {
    m_Dataset = instances;
  }
  







  public final void setMissing(int attIndex)
  {
    setValue(attIndex, NaN.0D);
  }
  







  public final void setMissing(Attribute att)
  {
    setMissing(att.index());
  }
  










  public void setValue(int attIndex, double value)
  {
    freshAttributeVector();
    m_AttValues[attIndex] = value;
  }
  











  public void setValueSparse(int indexOfIndex, double value)
  {
    freshAttributeVector();
    m_AttValues[indexOfIndex] = value;
  }
  

















  public final void setValue(int attIndex, String value)
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    if ((!attribute(attIndex).isNominal()) && (!attribute(attIndex).isString()))
    {
      throw new IllegalArgumentException("Attribute neither nominal nor string!");
    }
    int valIndex = attribute(attIndex).indexOfValue(value);
    if (valIndex == -1) {
      if (attribute(attIndex).isNominal()) {
        throw new IllegalArgumentException("Value not defined for given nominal attribute!");
      }
      attribute(attIndex).forceAddValue(value);
      valIndex = attribute(attIndex).indexOfValue(value);
    }
    
    setValue(attIndex, valIndex);
  }
  













  public final void setValue(Attribute att, double value)
  {
    setValue(att.index(), value);
  }
  















  public final void setValue(Attribute att, String value)
  {
    if ((!att.isNominal()) && (!att.isString()))
    {
      throw new IllegalArgumentException("Attribute neither nominal nor string!");
    }
    int valIndex = att.indexOfValue(value);
    if (valIndex == -1) {
      if (att.isNominal()) {
        throw new IllegalArgumentException("Value not defined for given nominal attribute!");
      }
      att.forceAddValue(value);
      valIndex = att.indexOfValue(value);
    }
    
    setValue(att.index(), valIndex);
  }
  





  public final void setWeight(double weight)
  {
    m_Weight = weight;
  }
  











  public final Instances relationalValue(int attIndex)
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return relationalValue(m_Dataset.attribute(attIndex));
  }
  











  public final Instances relationalValue(Attribute att)
  {
    int attIndex = att.index();
    if (att.isRelationValued()) {
      if (isMissing(attIndex)) {
        return null;
      }
      return att.relation((int)value(attIndex));
    }
    throw new IllegalArgumentException("Attribute isn't relation-valued!");
  }
  













  public final String stringValue(int attIndex)
  {
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return stringValue(m_Dataset.attribute(attIndex));
  }
  












  public final String stringValue(Attribute att)
  {
    int attIndex = att.index();
    if (isMissing(attIndex)) {
      return "?";
    }
    switch (att.type()) {
    case 1: 
    case 2: 
      return att.value((int)value(attIndex));
    case 3: 
      return att.formatDate(value(attIndex));
    case 4: 
      return att.relation((int)value(attIndex)).stringWithoutHeader();
    }
    throw new IllegalArgumentException("Attribute isn't nominal, string or date!");
  }
  






  public double[] toDoubleArray()
  {
    double[] newValues = new double[m_AttValues.length];
    System.arraycopy(m_AttValues, 0, newValues, 0, m_AttValues.length);
    
    return newValues;
  }
  








  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    for (int i = 0; i < m_AttValues.length; i++) {
      if (i > 0) text.append(",");
      text.append(toString(i));
    }
    
    if (m_Weight != 1.0D) {
      text.append(",{" + Utils.doubleToString(m_Weight, 6) + "}");
    }
    
    return text.toString();
  }
  












  protected String toStringNoWeight()
  {
    StringBuffer text = new StringBuffer();
    
    for (int i = 0; i < m_AttValues.length; i++) {
      if (i > 0) text.append(",");
      text.append(toString(i));
    }
    
    return text.toString();
  }
  










  public final String toString(int attIndex)
  {
    StringBuffer text = new StringBuffer();
    
    if (isMissing(attIndex)) {
      text.append("?");
    }
    else if (m_Dataset == null) {
      text.append(Utils.doubleToString(m_AttValues[attIndex], 6));
    } else {
      switch (m_Dataset.attribute(attIndex).type()) {
      case 1: 
      case 2: 
      case 3: 
      case 4: 
        text.append(Utils.quote(stringValue(attIndex)));
        break;
      case 0: 
        text.append(Utils.doubleToString(value(attIndex), 6));
        break;
      default: 
        throw new IllegalStateException("Unknown attribute type");
      }
      
    }
    return text.toString();
  }
  











  public final String toString(Attribute att)
  {
    return toString(att.index());
  }
  








  public double value(int attIndex)
  {
    return m_AttValues[attIndex];
  }
  









  public double valueSparse(int indexOfIndex)
  {
    return m_AttValues[indexOfIndex];
  }
  









  public double value(Attribute att)
  {
    return value(att.index());
  }
  





  public final double weight()
  {
    return m_Weight;
  }
  






  void forceDeleteAttributeAt(int position)
  {
    double[] newValues = new double[m_AttValues.length - 1];
    
    System.arraycopy(m_AttValues, 0, newValues, 0, position);
    if (position < m_AttValues.length - 1) {
      System.arraycopy(m_AttValues, position + 1, newValues, position, m_AttValues.length - (position + 1));
    }
    

    m_AttValues = newValues;
  }
  






  void forceInsertAttributeAt(int position)
  {
    double[] newValues = new double[m_AttValues.length + 1];
    
    System.arraycopy(m_AttValues, 0, newValues, 0, position);
    newValues[position] = NaN.0D;
    System.arraycopy(m_AttValues, position, newValues, position + 1, m_AttValues.length - position);
    
    m_AttValues = newValues;
  }
  




  protected Instance() {}
  




  private void freshAttributeVector()
  {
    m_AttValues = toDoubleArray();
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
      

      Instance inst = new Instance(3);
      

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
      

      Instance copy = (Instance)inst.copy();
      System.out.println("Shallow copy: " + copy);
      

      copy.setDataset(inst.dataset());
      System.out.println("Shallow copy with dataset set: " + copy);
      

      copy.setDataset(null);
      copy.deleteAttributeAt(0);
      copy.insertAttributeAt(0);
      copy.setDataset(inst.dataset());
      System.out.println("Copy with first attribute deleted and inserted: " + copy);
      

      System.out.println("Enumerating attributes (leaving out class):");
      Enumeration enu = inst.enumerateAttributes();
      while (enu.hasMoreElements()) {
        Attribute att = (Attribute)enu.nextElement();
        System.out.println(att);
      }
      

      System.out.println("Header of original and copy equivalent: " + inst.equalHeaders(copy));
      


      System.out.println("Length of copy missing: " + copy.isMissing(length));
      System.out.println("Weight of copy missing: " + copy.isMissing(weight.index()));
      System.out.println("Length of copy missing: " + isMissingValue(copy.value(length)));
      
      System.out.println("Missing value coded as: " + missingValue());
      

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
    return RevisionUtils.extract("$Revision: 9140 $");
  }
}

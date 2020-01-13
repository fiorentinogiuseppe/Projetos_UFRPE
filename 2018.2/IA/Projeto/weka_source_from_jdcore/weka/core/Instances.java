package weka.core;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.experiment.Stats;


























































































public class Instances
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = -19412345060742748L;
  public static final String FILE_EXTENSION = ".arff";
  public static final String SERIALIZED_OBJ_FILE_EXTENSION = ".bsi";
  public static final String ARFF_RELATION = "@relation";
  public static final String ARFF_DATA = "@data";
  protected String m_RelationName;
  protected FastVector m_Attributes;
  protected FastVector m_Instances;
  protected int m_ClassIndex;
  protected int m_Lines = 0;
  





  public Instances(Reader reader)
    throws IOException
  {
    ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
    Instances dataset = arff.getData();
    initialize(dataset, dataset.numInstances());
    dataset.copyInstances(0, this, dataset.numInstances());
    compactify();
  }
  

















  @Deprecated
  public Instances(Reader reader, int capacity)
    throws IOException
  {
    ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader, 0);
    Instances header = arff.getStructure();
    initialize(header, capacity);
    m_Lines = arff.getLineNo();
  }
  






  public Instances(Instances dataset)
  {
    this(dataset, dataset.numInstances());
    
    dataset.copyInstances(0, this, dataset.numInstances());
  }
  








  public Instances(Instances dataset, int capacity)
  {
    initialize(dataset, capacity);
  }
  






  protected void initialize(Instances dataset, int capacity)
  {
    if (capacity < 0) {
      capacity = 0;
    }
    


    m_ClassIndex = m_ClassIndex;
    m_RelationName = m_RelationName;
    m_Attributes = m_Attributes;
    m_Instances = new FastVector(capacity);
  }
  











  public Instances(Instances source, int first, int toCopy)
  {
    this(source, toCopy);
    
    if ((first < 0) || (first + toCopy > source.numInstances())) {
      throw new IllegalArgumentException("Parameters first and/or toCopy out of range");
    }
    
    source.copyInstances(first, this, toCopy);
  }
  












  public Instances(String name, FastVector attInfo, int capacity)
  {
    HashSet<String> names = new HashSet();
    StringBuffer nonUniqueNames = new StringBuffer();
    for (int i = 0; i < attInfo.size(); i++) {
      if (names.contains(((Attribute)attInfo.elementAt(i)).name())) {
        nonUniqueNames.append("'" + ((Attribute)attInfo.elementAt(i)).name() + "' ");
      }
      
      names.add(((Attribute)attInfo.elementAt(i)).name());
    }
    if (names.size() != attInfo.size()) {
      throw new IllegalArgumentException("Attribute names are not unique! Causes: " + nonUniqueNames.toString());
    }
    
    names.clear();
    
    m_RelationName = name;
    m_ClassIndex = -1;
    m_Attributes = attInfo;
    for (int i = 0; i < numAttributes(); i++) {
      attribute(i).setIndex(i);
    }
    m_Instances = new FastVector(capacity);
  }
  







  public Instances stringFreeStructure()
  {
    FastVector newAtts = new FastVector();
    for (int i = 0; i < m_Attributes.size(); i++) {
      Attribute att = (Attribute)m_Attributes.elementAt(i);
      if (att.type() == 2) {
        newAtts.addElement(new Attribute(att.name(), (FastVector)null, i));
      } else if (att.type() == 4) {
        newAtts.addElement(new Attribute(att.name(), new Instances(att.relation(), 0), i));
      }
    }
    
    if (newAtts.size() == 0) {
      return new Instances(this, 0);
    }
    FastVector atts = (FastVector)m_Attributes.copy();
    for (int i = 0; i < newAtts.size(); i++) {
      atts.setElementAt(newAtts.elementAt(i), ((Attribute)newAtts.elementAt(i)).index());
    }
    
    Instances result = new Instances(this, 0);
    m_Attributes = atts;
    return result;
  }
  








  public void add(Instance instance)
  {
    Instance newInstance = (Instance)instance.copy();
    
    newInstance.setDataset(this);
    m_Instances.addElement(newInstance);
  }
  









  public Attribute attribute(int index)
  {
    return (Attribute)m_Attributes.elementAt(index);
  }
  









  public Attribute attribute(String name)
  {
    for (int i = 0; i < numAttributes(); i++) {
      if (attribute(i).name().equals(name)) {
        return attribute(i);
      }
    }
    return null;
  }
  






  public boolean checkForAttributeType(int attType)
  {
    int i = 0;
    
    while (i < m_Attributes.size()) {
      if (attribute(i++).type() == attType) {
        return true;
      }
    }
    return false;
  }
  




  public boolean checkForStringAttributes()
  {
    return checkForAttributeType(2);
  }
  








  public boolean checkInstance(Instance instance)
  {
    if (instance.numAttributes() != numAttributes()) {
      return false;
    }
    for (int i = 0; i < numAttributes(); i++) {
      if (!instance.isMissing(i))
      {
        if ((attribute(i).isNominal()) || (attribute(i).isString())) {
          if (!Utils.eq(instance.value(i), (int)instance.value(i)))
            return false;
          if ((Utils.sm(instance.value(i), 0.0D)) || (Utils.gr(instance.value(i), attribute(i).numValues())))
          {
            return false; }
        }
      }
    }
    return true;
  }
  







  public Attribute classAttribute()
  {
    if (m_ClassIndex < 0) {
      throw new UnassignedClassException("Class index is negative (not set)!");
    }
    return attribute(m_ClassIndex);
  }
  







  public int classIndex()
  {
    return m_ClassIndex;
  }
  




  public void compactify()
  {
    m_Instances.trimToSize();
  }
  



  public void delete()
  {
    m_Instances = new FastVector();
  }
  






  public void delete(int index)
  {
    m_Instances.removeElementAt(index);
  }
  











  public void deleteAttributeAt(int position)
  {
    if ((position < 0) || (position >= m_Attributes.size())) {
      throw new IllegalArgumentException("Index out of range");
    }
    if (position == m_ClassIndex) {
      throw new IllegalArgumentException("Can't delete class attribute");
    }
    freshAttributeInfo();
    if (m_ClassIndex > position) {
      m_ClassIndex -= 1;
    }
    m_Attributes.removeElementAt(position);
    for (int i = position; i < m_Attributes.size(); i++) {
      Attribute current = (Attribute)m_Attributes.elementAt(i);
      current.setIndex(current.index() - 1);
    }
    for (int i = 0; i < numInstances(); i++) {
      instance(i).forceDeleteAttributeAt(position);
    }
  }
  







  public void deleteAttributeType(int attType)
  {
    int i = 0;
    while (i < m_Attributes.size()) {
      if (attribute(i).type() == attType) {
        deleteAttributeAt(i);
      } else {
        i++;
      }
    }
  }
  








  public void deleteStringAttributes()
  {
    deleteAttributeType(2);
  }
  







  public void deleteWithMissing(int attIndex)
  {
    FastVector newInstances = new FastVector(numInstances());
    
    for (int i = 0; i < numInstances(); i++) {
      if (!instance(i).isMissing(attIndex)) {
        newInstances.addElement(instance(i));
      }
    }
    m_Instances = newInstances;
  }
  






  public void deleteWithMissing(Attribute att)
  {
    deleteWithMissing(att.index());
  }
  





  public void deleteWithMissingClass()
  {
    if (m_ClassIndex < 0) {
      throw new UnassignedClassException("Class index is negative (not set)!");
    }
    deleteWithMissing(m_ClassIndex);
  }
  






  public Enumeration enumerateAttributes()
  {
    return m_Attributes.elements(m_ClassIndex);
  }
  





  public Enumeration enumerateInstances()
  {
    return m_Instances.elements();
  }
  








  public boolean equalHeaders(Instances dataset)
  {
    if (m_ClassIndex != m_ClassIndex) {
      return false;
    }
    if (m_Attributes.size() != m_Attributes.size()) {
      return false;
    }
    for (int i = 0; i < m_Attributes.size(); i++) {
      if (!attribute(i).equals(dataset.attribute(i))) {
        return false;
      }
    }
    return true;
  }
  






  public Instance firstInstance()
  {
    return (Instance)m_Instances.firstElement();
  }
  








  public Random getRandomNumberGenerator(long seed)
  {
    Random r = new Random(seed);
    r.setSeed(instance(r.nextInt(numInstances())).toStringNoWeight().hashCode() + seed);
    
    return r;
  }
  











  public void insertAttributeAt(Attribute att, int position)
  {
    if ((position < 0) || (position > m_Attributes.size())) {
      throw new IllegalArgumentException("Index out of range");
    }
    if (attribute(att.name()) != null) {
      throw new IllegalArgumentException("Attribute name '" + att.name() + "' already in use at position #" + attribute(att.name()).index());
    }
    
    att = (Attribute)att.copy();
    freshAttributeInfo();
    att.setIndex(position);
    m_Attributes.insertElementAt(att, position);
    for (int i = position + 1; i < m_Attributes.size(); i++) {
      Attribute current = (Attribute)m_Attributes.elementAt(i);
      current.setIndex(current.index() + 1);
    }
    for (int i = 0; i < numInstances(); i++) {
      instance(i).forceInsertAttributeAt(position);
    }
    if (m_ClassIndex >= position) {
      m_ClassIndex += 1;
    }
  }
  








  public Instance instance(int index)
  {
    return (Instance)m_Instances.elementAt(index);
  }
  







  public double kthSmallestValue(Attribute att, int k)
  {
    return kthSmallestValue(att.index(), k);
  }
  









  public double kthSmallestValue(int attIndex, int k)
  {
    if (!attribute(attIndex).isNumeric()) {
      throw new IllegalArgumentException("Instances: attribute must be numeric to compute kth-smallest value.");
    }
    

    if ((k < 1) || (k > numInstances())) {
      throw new IllegalArgumentException("Instances: value for k for computing kth-smallest value too large.");
    }
    

    double[] vals = new double[numInstances()];
    for (int i = 0; i < vals.length; i++) {
      double val = instance(i).value(attIndex);
      if (Instance.isMissingValue(val)) {
        vals[i] = Double.MAX_VALUE;
      } else {
        vals[i] = val;
      }
    }
    return Utils.kthSmallestValue(vals, k);
  }
  






  public Instance lastInstance()
  {
    return (Instance)m_Instances.lastElement();
  }
  











  public double meanOrMode(int attIndex)
  {
    if (attribute(attIndex).isNumeric()) { double found;
      double result = found = 0.0D;
      for (int j = 0; j < numInstances(); j++) {
        if (!instance(j).isMissing(attIndex)) {
          found += instance(j).weight();
          result += instance(j).weight() * instance(j).value(attIndex);
        }
      }
      if (found <= 0.0D) {
        return 0.0D;
      }
      return result / found;
    }
    if (attribute(attIndex).isNominal()) {
      int[] counts = new int[attribute(attIndex).numValues()];
      for (int j = 0; j < numInstances(); tmp159_158++) {
        if (!instance(j).isMissing(attIndex)) {
          int tmp159_158 = ((int)instance(j).value(attIndex)); int[] tmp159_146 = counts;tmp159_146[tmp159_158] = ((int)(tmp159_146[tmp159_158] + instance(tmp159_158).weight()));
        }
      }
      return Utils.maxIndex(counts);
    }
    return 0.0D;
  }
  









  public double meanOrMode(Attribute att)
  {
    return meanOrMode(att.index());
  }
  






  public int numAttributes()
  {
    return m_Attributes.size();
  }
  








  public int numClasses()
  {
    if (m_ClassIndex < 0) {
      throw new UnassignedClassException("Class index is negative (not set)!");
    }
    if (!classAttribute().isNominal()) {
      return 1;
    }
    return classAttribute().numValues();
  }
  











  public int numDistinctValues(int attIndex)
  {
    if (attribute(attIndex).isNumeric()) {
      double[] attVals = attributeToDoubleArray(attIndex);
      int[] sorted = Utils.sort(attVals);
      double prev = 0.0D;
      int counter = 0;
      for (int i = 0; i < sorted.length; i++) {
        Instance current = instance(sorted[i]);
        if (current.isMissing(attIndex)) {
          break;
        }
        if ((i == 0) || (current.value(attIndex) > prev)) {
          prev = current.value(attIndex);
          counter++;
        }
      }
      return counter;
    }
    return attribute(attIndex).numValues();
  }
  









  public int numDistinctValues(Attribute att)
  {
    return numDistinctValues(att.index());
  }
  






  public int numInstances()
  {
    return m_Instances.size();
  }
  





  public void randomize(Random random)
  {
    for (int j = numInstances() - 1; j > 0; j--) {
      swap(j, random.nextInt(j + 1));
    }
  }
  















  @Deprecated
  public boolean readInstance(Reader reader)
    throws IOException
  {
    ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader, this, m_Lines, 1);
    Instance inst = arff.readInstance(arff.getData(), false);
    m_Lines = arff.getLineNo();
    if (inst != null) {
      add(inst);
      return true;
    }
    return false;
  }
  







  public String relationName()
  {
    return m_RelationName;
  }
  






  public void renameAttribute(int att, String name)
  {
    for (int i = 0; i < numAttributes(); i++) {
      if (i != att)
      {

        if (attribute(i).name().equals(name)) {
          throw new IllegalArgumentException("Attribute name '" + name + "' already present at position #" + i);
        }
      }
    }
    
    Attribute newAtt = attribute(att).copy(name);
    FastVector newVec = new FastVector(numAttributes());
    for (int i = 0; i < numAttributes(); i++) {
      if (i == att) {
        newVec.addElement(newAtt);
      } else {
        newVec.addElement(attribute(i));
      }
    }
    m_Attributes = newVec;
  }
  






  public void renameAttribute(Attribute att, String name)
  {
    renameAttribute(att.index(), name);
  }
  








  public void renameAttributeValue(int att, int val, String name)
  {
    Attribute newAtt = (Attribute)attribute(att).copy();
    FastVector newVec = new FastVector(numAttributes());
    
    newAtt.setValue(val, name);
    for (int i = 0; i < numAttributes(); i++) {
      if (i == att) {
        newVec.addElement(newAtt);
      } else {
        newVec.addElement(attribute(i));
      }
    }
    m_Attributes = newVec;
  }
  








  public void renameAttributeValue(Attribute att, String val, String name)
  {
    int v = att.indexOfValue(val);
    if (v == -1) {
      throw new IllegalArgumentException(val + " not found");
    }
    renameAttributeValue(att.index(), v, name);
  }
  







  public Instances resample(Random random)
  {
    Instances newData = new Instances(this, numInstances());
    while (newData.numInstances() < numInstances()) {
      newData.add(instance(random.nextInt(numInstances())));
    }
    return newData;
  }
  








  public Instances resampleWithWeights(Random random)
  {
    double[] weights = new double[numInstances()];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = instance(i).weight();
    }
    return resampleWithWeights(random, weights);
  }
  










  public Instances resampleWithWeights(Random random, boolean[] sampled)
  {
    double[] weights = new double[numInstances()];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = instance(i).weight();
    }
    return resampleWithWeights(random, weights, sampled);
  }
  











  public Instances resampleWithWeights(Random random, double[] weights)
  {
    return resampleWithWeights(random, weights, null);
  }
  
















  public Instances resampleWithWeights(Random random, double[] weights, boolean[] sampled)
  {
    if (weights.length != numInstances()) {
      throw new IllegalArgumentException("weights.length != numInstances.");
    }
    
    Instances newData = new Instances(this, numInstances());
    if (numInstances() == 0) {
      return newData;
    }
    

    double[] P = new double[weights.length];
    System.arraycopy(weights, 0, P, 0, weights.length);
    Utils.normalize(P);
    double[] Q = new double[weights.length];
    int[] A = new int[weights.length];
    int[] W = new int[weights.length];
    int M = weights.length;
    int NN = -1;
    int NP = M;
    for (int I = 0; I < M; I++) {
      if (P[I] < 0.0D) {
        throw new IllegalArgumentException("Weights have to be positive.");
      }
      Q[I] = (M * P[I]);
      if (Q[I] < 1.0D) {
        W[(++NN)] = I;
      } else {
        W[(--NP)] = I;
      }
    }
    if ((NN > -1) && (NP < M)) {
      for (int S = 0; S < M - 1; S++) {
        int I = W[S];
        int J = W[NP];
        A[I] = J;
        Q[J] += Q[I] - 1.0D;
        if (Q[J] < 1.0D) {
          NP++;
        }
        if (NP >= M) {
          break;
        }
      }
    }
    

    for (int I = 0; I < M; I++) {
      Q[I] += I;
    }
    
    for (int i = 0; i < numInstances(); i++)
    {
      double U = M * random.nextDouble();
      int I = (int)U;
      int ALRV; int ALRV; if (U < Q[I]) {
        ALRV = I;
      } else {
        ALRV = A[I];
      }
      newData.add(instance(ALRV));
      if (sampled != null) {
        sampled[ALRV] = true;
      }
      newData.instance(newData.numInstances() - 1).setWeight(1.0D);
    }
    
    return newData;
  }
  





  public void setClass(Attribute att)
  {
    m_ClassIndex = att.index();
  }
  







  public void setClassIndex(int classIndex)
  {
    if (classIndex >= numAttributes()) {
      throw new IllegalArgumentException("Invalid class index: " + classIndex);
    }
    m_ClassIndex = classIndex;
  }
  





  public void setRelationName(String newName)
  {
    m_RelationName = newName;
  }
  









  public void sort(int attIndex)
  {
    double[] vals = new double[numInstances()];
    for (int i = 0; i < vals.length; i++) {
      double val = instance(i).value(attIndex);
      if (Instance.isMissingValue(val)) {
        vals[i] = Double.MAX_VALUE;
      } else {
        vals[i] = val;
      }
    }
    
    int[] sortOrder = Utils.sortWithNoMissingValues(vals);
    Instance[] backup = new Instance[vals.length];
    for (int i = 0; i < vals.length; i++) {
      backup[i] = instance(i);
    }
    for (int i = 0; i < vals.length; i++) {
      m_Instances.setElementAt(backup[sortOrder[i]], i);
    }
  }
  









  public void sort(Attribute att)
  {
    sort(att.index());
  }
  








  public void stratify(int numFolds)
  {
    if (numFolds <= 1) {
      throw new IllegalArgumentException("Number of folds must be greater than 1");
    }
    
    if (m_ClassIndex < 0) {
      throw new UnassignedClassException("Class index is negative (not set)!");
    }
    if (classAttribute().isNominal())
    {

      int index = 1;
      while (index < numInstances()) {
        Instance instance1 = instance(index - 1);
        for (int j = index; j < numInstances(); j++) {
          Instance instance2 = instance(j);
          if ((instance1.classValue() == instance2.classValue()) || ((instance1.classIsMissing()) && (instance2.classIsMissing())))
          {
            swap(index, j);
            index++;
          }
        }
        index++;
      }
      stratStep(numFolds);
    }
  }
  





  public double sumOfWeights()
  {
    double sum = 0.0D;
    
    for (int i = 0; i < numInstances(); i++) {
      sum += instance(i).weight();
    }
    return sum;
  }
  















  public Instances testCV(int numFolds, int numFold)
  {
    if (numFolds < 2) {
      throw new IllegalArgumentException("Number of folds must be at least 2!");
    }
    if (numFolds > numInstances()) {
      throw new IllegalArgumentException("Can't have more folds than instances!");
    }
    
    int numInstForFold = numInstances() / numFolds;
    int offset; int offset; if (numFold < numInstances() % numFolds) {
      numInstForFold++;
      offset = numFold;
    } else {
      offset = numInstances() % numFolds;
    }
    Instances test = new Instances(this, numInstForFold);
    int first = numFold * (numInstances() / numFolds) + offset;
    copyInstances(first, test, numInstForFold);
    return test;
  }
  







  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append("@relation").append(" ").append(Utils.quote(m_RelationName)).append("\n\n");
    
    for (int i = 0; i < numAttributes(); i++) {
      text.append(attribute(i)).append("\n");
    }
    text.append("\n").append("@data").append("\n");
    
    text.append(stringWithoutHeader());
    return text.toString();
  }
  







  protected String stringWithoutHeader()
  {
    StringBuffer text = new StringBuffer();
    
    for (int i = 0; i < numInstances(); i++) {
      text.append(instance(i));
      if (i < numInstances() - 1) {
        text.append('\n');
      }
    }
    return text.toString();
  }
  















  public Instances trainCV(int numFolds, int numFold)
  {
    if (numFolds < 2) {
      throw new IllegalArgumentException("Number of folds must be at least 2!");
    }
    if (numFolds > numInstances()) {
      throw new IllegalArgumentException("Can't have more folds than instances!");
    }
    
    int numInstForFold = numInstances() / numFolds;
    int offset; int offset; if (numFold < numInstances() % numFolds) {
      numInstForFold++;
      offset = numFold;
    } else {
      offset = numInstances() % numFolds;
    }
    Instances train = new Instances(this, numInstances() - numInstForFold);
    int first = numFold * (numInstances() / numFolds) + offset;
    copyInstances(0, train, first);
    copyInstances(first + numInstForFold, train, numInstances() - first - numInstForFold);
    

    return train;
  }
  















  public Instances trainCV(int numFolds, int numFold, Random random)
  {
    Instances train = trainCV(numFolds, numFold);
    train.randomize(random);
    return train;
  }
  







  public double variance(int attIndex)
  {
    double sum = 0.0D;double sumSquared = 0.0D;double sumOfWeights = 0.0D;
    
    if (!attribute(attIndex).isNumeric()) {
      throw new IllegalArgumentException("Can't compute variance because attribute is not numeric!");
    }
    
    for (int i = 0; i < numInstances(); i++) {
      if (!instance(i).isMissing(attIndex)) {
        sum += instance(i).weight() * instance(i).value(attIndex);
        sumSquared += instance(i).weight() * instance(i).value(attIndex) * instance(i).value(attIndex);
        
        sumOfWeights += instance(i).weight();
      }
    }
    if (sumOfWeights <= 1.0D) {
      return 0.0D;
    }
    double result = (sumSquared - sum * sum / sumOfWeights) / (sumOfWeights - 1.0D);
    


    if (result < 0.0D) {
      return 0.0D;
    }
    return result;
  }
  








  public double variance(Attribute att)
  {
    return variance(att.index());
  }
  








  public AttributeStats attributeStats(int index)
  {
    AttributeStats result = new AttributeStats();
    if (attribute(index).isNominal()) {
      nominalCounts = new int[attribute(index).numValues()];
    }
    if (attribute(index).isNumeric()) {
      numericStats = new Stats();
    }
    totalCount = numInstances();
    
    double[] attVals = attributeToDoubleArray(index);
    int[] sorted = Utils.sort(attVals);
    int currentCount = 0;
    double prev = Instance.missingValue();
    for (int j = 0; j < numInstances(); j++) {
      Instance current = instance(sorted[j]);
      if (current.isMissing(index)) {
        missingCount = (numInstances() - j);
        break;
      }
      if (current.value(index) == prev) {
        currentCount++;
      } else {
        result.addDistinct(prev, currentCount);
        currentCount = 1;
        prev = current.value(index);
      }
    }
    result.addDistinct(prev, currentCount);
    distinctCount -= 1;
    return result;
  }
  










  public double[] attributeToDoubleArray(int index)
  {
    double[] result = new double[numInstances()];
    for (int i = 0; i < result.length; i++) {
      result[i] = instance(i).value(index);
    }
    return result;
  }
  







  public String toSummaryString()
  {
    StringBuffer result = new StringBuffer();
    result.append("Relation Name:  ").append(relationName()).append('\n');
    result.append("Num Instances:  ").append(numInstances()).append('\n');
    result.append("Num Attributes: ").append(numAttributes()).append('\n');
    result.append('\n');
    
    result.append(Utils.padLeft("", 5)).append(Utils.padRight("Name", 25));
    result.append(Utils.padLeft("Type", 5)).append(Utils.padLeft("Nom", 5));
    result.append(Utils.padLeft("Int", 5)).append(Utils.padLeft("Real", 5));
    result.append(Utils.padLeft("Missing", 12));
    result.append(Utils.padLeft("Unique", 12));
    result.append(Utils.padLeft("Dist", 6)).append('\n');
    for (int i = 0; i < numAttributes(); i++) {
      Attribute a = attribute(i);
      AttributeStats as = attributeStats(i);
      result.append(Utils.padLeft("" + (i + 1), 4)).append(' ');
      result.append(Utils.padRight(a.name(), 25)).append(' ');
      
      switch (a.type()) {
      case 1: 
        result.append(Utils.padLeft("Nom", 4)).append(' ');
        percent = Math.round(100.0D * intCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
        result.append(Utils.padLeft("0", 3)).append("% ");
        percent = Math.round(100.0D * realCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
        break;
      case 0: 
        result.append(Utils.padLeft("Num", 4)).append(' ');
        result.append(Utils.padLeft("0", 3)).append("% ");
        percent = Math.round(100.0D * intCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
        percent = Math.round(100.0D * realCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
        break;
      case 3: 
        result.append(Utils.padLeft("Dat", 4)).append(' ');
        result.append(Utils.padLeft("0", 3)).append("% ");
        percent = Math.round(100.0D * intCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
        percent = Math.round(100.0D * realCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
        break;
      case 2: 
        result.append(Utils.padLeft("Str", 4)).append(' ');
        percent = Math.round(100.0D * intCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
        result.append(Utils.padLeft("0", 3)).append("% ");
        percent = Math.round(100.0D * realCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
        break;
      case 4: 
        result.append(Utils.padLeft("Rel", 4)).append(' ');
        percent = Math.round(100.0D * intCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
        result.append(Utils.padLeft("0", 3)).append("% ");
        percent = Math.round(100.0D * realCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
        break;
      default: 
        result.append(Utils.padLeft("???", 4)).append(' ');
        result.append(Utils.padLeft("0", 3)).append("% ");
        percent = Math.round(100.0D * intCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
        percent = Math.round(100.0D * realCount / totalCount);
        result.append(Utils.padLeft("" + percent, 3)).append("% ");
      }
      
      result.append(Utils.padLeft("" + missingCount, 5)).append(" /");
      long percent = Math.round(100.0D * missingCount / totalCount);
      result.append(Utils.padLeft("" + percent, 3)).append("% ");
      result.append(Utils.padLeft("" + uniqueCount, 5)).append(" /");
      percent = Math.round(100.0D * uniqueCount / totalCount);
      result.append(Utils.padLeft("" + percent, 3)).append("% ");
      result.append(Utils.padLeft("" + distinctCount, 5)).append(' ');
      result.append('\n');
    }
    return result.toString();
  }
  









  protected void copyInstances(int from, Instances dest, int num)
  {
    for (int i = 0; i < num; i++) {
      dest.add(instance(from + i));
    }
  }
  



  protected void freshAttributeInfo()
  {
    m_Attributes = ((FastVector)m_Attributes.copyElements());
  }
  






  protected String instancesAndWeights()
  {
    StringBuffer text = new StringBuffer();
    
    for (int i = 0; i < numInstances(); i++) {
      text.append(instance(i) + " " + instance(i).weight());
      if (i < numInstances() - 1) {
        text.append("\n");
      }
    }
    return text.toString();
  }
  





  protected void stratStep(int numFolds)
  {
    FastVector newVec = new FastVector(m_Instances.capacity());
    int start = 0;
    

    while (newVec.size() < numInstances()) {
      int j = start;
      while (j < numInstances()) {
        newVec.addElement(instance(j));
        j += numFolds;
      }
      start++;
    }
    m_Instances = newVec;
  }
  








  public void swap(int i, int j)
  {
    m_Instances.swap(i, j);
  }
  










  public static Instances mergeInstances(Instances first, Instances second)
  {
    if (first.numInstances() != second.numInstances()) {
      throw new IllegalArgumentException("Instance sets must be of the same size");
    }
    


    FastVector newAttributes = new FastVector();
    for (int i = 0; i < first.numAttributes(); i++) {
      newAttributes.addElement(first.attribute(i));
    }
    for (int i = 0; i < second.numAttributes(); i++) {
      newAttributes.addElement(second.attribute(i));
    }
    

    Instances merged = new Instances(first.relationName() + '_' + second.relationName(), newAttributes, first.numInstances());
    

    for (int i = 0; i < first.numInstances(); i++) {
      merged.add(first.instance(i).mergeInstance(second.instance(i)));
    }
    return merged;
  }
  









  public static void test(String[] argv)
  {
    Random random = new Random(2L);
    



    try
    {
      if (argv.length > 1) {
        throw new Exception("Usage: Instances [<filename>]");
      }
      

      FastVector testVals = new FastVector(2);
      testVals.addElement("first_value");
      testVals.addElement("second_value");
      FastVector testAtts = new FastVector(2);
      testAtts.addElement(new Attribute("nominal_attribute", testVals));
      testAtts.addElement(new Attribute("numeric_attribute"));
      Instances instances = new Instances("test_set", testAtts, 10);
      instances.add(new Instance(instances.numAttributes()));
      instances.add(new Instance(instances.numAttributes()));
      instances.add(new Instance(instances.numAttributes()));
      instances.setClassIndex(0);
      System.out.println("\nSet of instances created from scratch:\n");
      System.out.println(instances);
      
      if (argv.length == 1) {
        String filename = argv[0];
        Reader reader = new FileReader(filename);
        

        System.out.println("\nFirst five instances from file:\n");
        instances = new Instances(reader, 1);
        instances.setClassIndex(instances.numAttributes() - 1);
        int i = 0;
        while ((i < 5) && (instances.readInstance(reader))) {
          i++;
        }
        System.out.println(instances);
        

        reader = new FileReader(filename);
        instances = new Instances(reader);
        

        instances.setClassIndex(instances.numAttributes() - 1);
        

        System.out.println("\nDataset:\n");
        System.out.println(instances);
        System.out.println("\nClass index: " + instances.classIndex());
      }
      

      System.out.println("\nClass name: " + instances.classAttribute().name());
      System.out.println("\nClass index: " + instances.classIndex());
      System.out.println("\nClass is nominal: " + instances.classAttribute().isNominal());
      
      System.out.println("\nClass is numeric: " + instances.classAttribute().isNumeric());
      
      System.out.println("\nClasses:\n");
      for (int i = 0; i < instances.numClasses(); i++) {
        System.out.println(instances.classAttribute().value(i));
      }
      System.out.println("\nClass values and labels of instances:\n");
      for (i = 0; i < instances.numInstances(); i++) {
        Instance inst = instances.instance(i);
        System.out.print(inst.classValue() + "\t");
        System.out.print(inst.toString(inst.classIndex()));
        if (instances.instance(i).classIsMissing()) {
          System.out.println("\tis missing");
        } else {
          System.out.println();
        }
      }
      

      System.out.println("\nCreating random weights for instances.");
      for (i = 0; i < instances.numInstances(); i++) {
        instances.instance(i).setWeight(random.nextDouble());
      }
      

      System.out.println("\nInstances and their weights:\n");
      System.out.println(instances.instancesAndWeights());
      System.out.print("\nSum of weights: ");
      System.out.println(instances.sumOfWeights());
      

      Instances secondInstances = new Instances(instances);
      Attribute testAtt = new Attribute("Inserted");
      secondInstances.insertAttributeAt(testAtt, 0);
      System.out.println("\nSet with inserted attribute:\n");
      System.out.println(secondInstances);
      System.out.println("\nClass name: " + secondInstances.classAttribute().name());
      


      secondInstances.deleteAttributeAt(0);
      System.out.println("\nSet with attribute deleted:\n");
      System.out.println(secondInstances);
      System.out.println("\nClass name: " + secondInstances.classAttribute().name());
      


      System.out.println("\nHeaders equal: " + instances.equalHeaders(secondInstances) + "\n");
      


      System.out.println("\nData (internal values):\n");
      for (i = 0; i < instances.numInstances(); i++) {
        for (int j = 0; j < instances.numAttributes(); j++) {
          if (instances.instance(i).isMissing(j)) {
            System.out.print("? ");
          } else {
            System.out.print(instances.instance(i).value(j) + " ");
          }
        }
        System.out.println();
      }
      

      System.out.println("\nEmpty dataset:\n");
      Instances empty = new Instances(instances, 0);
      System.out.println(empty);
      System.out.println("\nClass name: " + empty.classAttribute().name());
      

      if (empty.classAttribute().isNominal()) {
        Instances copy = new Instances(empty, 0);
        copy.renameAttribute(copy.classAttribute(), "new_name");
        copy.renameAttributeValue(copy.classAttribute(), copy.classAttribute().value(0), "new_val_name");
        
        System.out.println("\nDataset with names changed:\n" + copy);
        System.out.println("\nOriginal dataset:\n" + empty);
      }
      

      int start = instances.numInstances() / 4;
      int num = instances.numInstances() / 2;
      System.out.print("\nSubset of dataset: ");
      System.out.println(num + " instances from " + (start + 1) + ". instance");
      secondInstances = new Instances(instances, start, num);
      System.out.println("\nClass name: " + secondInstances.classAttribute().name());
      


      System.out.println("\nInstances and their weights:\n");
      System.out.println(secondInstances.instancesAndWeights());
      System.out.print("\nSum of weights: ");
      System.out.println(secondInstances.sumOfWeights());
      


      System.out.println("\nTrain and test folds for 3-fold CV:");
      if (instances.classAttribute().isNominal()) {
        instances.stratify(3);
      }
      for (int j = 0; j < 3; j++) {
        Instances train = instances.trainCV(3, j, new Random(1L));
        Instances test = instances.testCV(3, j);
        

        System.out.println("\nTrain: ");
        System.out.println("\nInstances and their weights:\n");
        System.out.println(train.instancesAndWeights());
        System.out.print("\nSum of weights: ");
        System.out.println(train.sumOfWeights());
        System.out.println("\nClass name: " + train.classAttribute().name());
        System.out.println("\nTest: ");
        System.out.println("\nInstances and their weights:\n");
        System.out.println(test.instancesAndWeights());
        System.out.print("\nSum of weights: ");
        System.out.println(test.sumOfWeights());
        System.out.println("\nClass name: " + test.classAttribute().name());
      }
      

      System.out.println("\nRandomized dataset:");
      instances.randomize(random);
      

      System.out.println("\nInstances and their weights:\n");
      System.out.println(instances.instancesAndWeights());
      System.out.print("\nSum of weights: ");
      System.out.println(instances.sumOfWeights());
      


      System.out.print("\nInstances sorted according to first attribute:\n ");
      instances.sort(0);
      

      System.out.println("\nInstances and their weights:\n");
      System.out.println(instances.instancesAndWeights());
      System.out.print("\nSum of weights: ");
      System.out.println(instances.sumOfWeights());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
































  public static void main(String[] args)
  {
    try
    {
      if (args.length == 0) {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(System.in);
        Instances i = source.getDataSet();
        System.out.println(i.toSummaryString());

      }
      else if ((args.length == 1) && (!args[0].equals("-h")) && (!args[0].equals("help")))
      {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[0]);
        Instances i = source.getDataSet();
        System.out.println(i.toSummaryString());

      }
      else if ((args.length == 3) && (args[0].toLowerCase().equals("merge"))) {
        ConverterUtils.DataSource source1 = new ConverterUtils.DataSource(args[1]);
        ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(args[2]);
        Instances i = mergeInstances(source1.getDataSet(), source2.getDataSet());
        
        System.out.println(i);

      }
      else if ((args.length == 3) && (args[0].toLowerCase().equals("append"))) {
        ConverterUtils.DataSource source1 = new ConverterUtils.DataSource(args[1]);
        ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(args[2]);
        if (!source1.getStructure().equalHeaders(source2.getStructure())) {
          throw new Exception("The two datasets have different headers!");
        }
        Instances structure = source1.getStructure();
        System.out.println(source1.getStructure());
        while (source1.hasMoreElements(structure)) {
          System.out.println(source1.nextElement(structure));
        }
        structure = source2.getStructure();
        while (source2.hasMoreElements(structure)) {
          System.out.println(source2.nextElement(structure));
        }
        
      }
      else if ((args.length == 3) && (args[0].toLowerCase().equals("headers"))) {
        ConverterUtils.DataSource source1 = new ConverterUtils.DataSource(args[1]);
        ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(args[2]);
        if (source1.getStructure().equalHeaders(source2.getStructure())) {
          System.out.println("Headers match");
        } else {
          System.out.println("Headers don't match");
        }
        
      }
      else if ((args.length == 3) && (args[0].toLowerCase().equals("randomize")))
      {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[2]);
        Instances i = source.getDataSet();
        i.randomize(new Random(Integer.parseInt(args[1])));
        System.out.println(i);
      }
      else
      {
        System.err.println("\nUsage:\n\tweka.core.Instances help\n\tweka.core.Instances <filename>\n\tweka.core.Instances merge <filename1> <filename2>\n\tweka.core.Instances append <filename1> <filename2>\n\tweka.core.Instances headers <filename1> <filename2>\n\tweka.core.Instances randomize <seed> <filename>\n");
      }
      

    }
    catch (Exception ex)
    {

      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10497 $");
  }
}

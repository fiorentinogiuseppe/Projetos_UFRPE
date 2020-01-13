package weka.classifiers.mi;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;


























































public class CitationKNN
  extends Classifier
  implements OptionHandler, MultiInstanceCapabilitiesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -8435377743874094852L;
  protected int m_ClassIndex;
  protected int m_NumClasses;
  protected int m_IdIndex;
  protected boolean m_Debug;
  protected int[] m_Classes;
  protected Instances m_Attributes;
  protected int m_NumReferences;
  protected int m_NumCiters;
  protected Instances m_TrainBags;
  protected boolean m_CNNDebug;
  protected boolean m_CitersDebug;
  protected boolean m_ReferencesDebug;
  protected boolean m_HDistanceDebug;
  protected boolean m_NeighborListDebug;
  protected NeighborList[] m_CNN;
  protected int[] m_Citers;
  protected int[] m_References;
  protected int m_HDRank;
  private double[] m_Diffs;
  private double[] m_Min;
  private double m_MinNorm;
  private double[] m_Max;
  private double m_MaxNorm;
  
  public CitationKNN()
  {
    m_NumReferences = 1;
    

    m_NumCiters = 1;
    




    m_CNNDebug = false;
    
    m_CitersDebug = false;
    
    m_ReferencesDebug = false;
    
    m_HDistanceDebug = false;
    
    m_NeighborListDebug = false;
    










    m_HDRank = 1;
    





    m_MinNorm = 0.95D;
    


    m_MaxNorm = 1.05D;
  }
  




  public String globalInfo()
  {
    return "Modified version of the Citation kNN multi instance classifier.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Jun Wang and Zucker and Jean-Daniel");
    result.setValue(TechnicalInformation.Field.TITLE, "Solving Multiple-Instance Problem: A Lazy Learning Approach");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "17th International Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.EDITOR, "Pat Langley");
    result.setValue(TechnicalInformation.Field.YEAR, "2000");
    result.setValue(TechnicalInformation.Field.PAGES, "1119-1125");
    
    return result;
  }
  








  public void preprocessData()
  {
    for (int i = 0; i < m_Attributes.numAttributes(); i++) {
      double min = Double.POSITIVE_INFINITY;
      double max = Double.NEGATIVE_INFINITY;
      for (int j = 0; j < m_TrainBags.numInstances(); j++) {
        Instances instances = m_TrainBags.instance(j).relationalValue(1);
        for (int k = 0; k < instances.numInstances(); k++) {
          Instance instance = instances.instance(k);
          if (instance.value(i) < min)
            min = instance.value(i);
          if (instance.value(i) > max)
            max = instance.value(i);
        }
      }
      m_Min[i] = (min * m_MinNorm);
      m_Max[i] = (max * m_MaxNorm);
      m_Diffs[i] = (max * m_MaxNorm - min * m_MinNorm);
    }
  }
  






  public String HDRankTipText()
  {
    return "The rank associated to the Hausdorff distance.";
  }
  



  public void setHDRank(int hDRank)
  {
    m_HDRank = hDRank;
  }
  



  public int getHDRank()
  {
    return m_HDRank;
  }
  





  public String numReferencesTipText()
  {
    return "The number of references considered to estimate the class prediction of tests bags.";
  }
  






  public void setNumReferences(int numReferences)
  {
    m_NumReferences = numReferences;
  }
  




  public int getNumReferences()
  {
    return m_NumReferences;
  }
  





  public String numCitersTipText()
  {
    return "The number of citers considered to estimate the class prediction of test bags.";
  }
  






  public void setNumCiters(int numCiters)
  {
    m_NumCiters = numCiters;
  }
  




  public int getNumCiters()
  {
    return m_NumCiters;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    


    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    
    return result;
  }
  






  public Capabilities getMultiInstanceCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.disableAllClasses();
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  






  public void buildClassifier(Instances train)
    throws Exception
  {
    getCapabilities().testWithFail(train);
    

    train = new Instances(train);
    train.deleteWithMissingClass();
    
    m_TrainBags = train;
    m_ClassIndex = train.classIndex();
    m_IdIndex = 0;
    m_NumClasses = train.numClasses();
    
    m_Classes = new int[train.numInstances()];
    m_Attributes = train.instance(0).relationalValue(1).stringFreeStructure();
    
    m_Citers = new int[train.numClasses()];
    m_References = new int[train.numClasses()];
    
    m_Diffs = new double[m_Attributes.numAttributes()];
    m_Min = new double[m_Attributes.numAttributes()];
    m_Max = new double[m_Attributes.numAttributes()];
    
    preprocessData();
    
    buildCNN();
    
    if (m_CNNDebug) {
      System.out.println("########################################### ");
      System.out.println("###########CITATION######################## ");
      System.out.println("########################################### ");
      for (int i = 0; i < m_CNN.length; i++) {
        System.out.println("Bag: " + i);
        m_CNN[i].printReducedList();
      }
    }
  }
  





  public void buildCNN()
    throws Exception
  {
    int numCiters = 0;
    
    if ((m_NumCiters >= m_TrainBags.numInstances()) || (m_NumCiters < 0))
    {
      throw new Exception("Number of citers is out of the range [0, numInstances)");
    }
    numCiters = m_NumCiters;
    
    m_CNN = new NeighborList[m_TrainBags.numInstances()];
    

    for (int i = 0; i < m_TrainBags.numInstances(); i++) {
      Instance bag = m_TrainBags.instance(i);
      
      NeighborList neighborList = findNeighbors(bag, numCiters, m_TrainBags);
      m_CNN[i] = neighborList;
    }
  }
  





  public void countBagCiters(Instance bag)
  {
    for (int i = 0; i < m_TrainBags.numClasses(); i++) {
      m_Citers[i] = 0;
    }
    if (m_CitersDebug == true) {
      System.out.println("-------CITERS--------");
    }
    

    boolean stopSearch = false;
    




    double bagDistance = 0.0D;
    for (int i = 0; i < m_TrainBags.numInstances(); i++)
    {
      bagDistance = distanceSet(bag, m_TrainBags.instance(i));
      if (m_CitersDebug == true) {
        System.out.print("bag - bag(" + i + "): " + bagDistance);
        System.out.println("   <" + m_TrainBags.instance(i).classValue() + ">");
      }
      

      NeighborList neighborList = m_CNN[i];
      NeighborNode current = mFirst;
      
      while ((current != null) && (!stopSearch)) {
        if (m_CitersDebug == true)
          System.out.println("\t\tciter Distance: " + mDistance);
        if (mDistance < bagDistance) {
          current = mNext;
        } else {
          stopSearch = true;
          if (m_CitersDebug == true) {
            System.out.println("\t***");
          }
        }
      }
      
      if (stopSearch == true) {
        stopSearch = false;
        int index = (int)m_TrainBags.instance(i).classValue();
        m_Citers[index] += 1;
      }
    }
    

    if (m_CitersDebug == true) {
      for (int i = 0; i < m_Citers.length; i++) {
        System.out.println("[" + i + "]: " + m_Citers[i]);
      }
    }
  }
  





  public void countBagReferences(Instance bag)
  {
    int index = 0;int referencesIndex = 0;
    
    if (m_TrainBags.numInstances() < m_NumReferences) {
      referencesIndex = m_TrainBags.numInstances() - 1;
    } else {
      referencesIndex = m_NumReferences;
    }
    if (m_CitersDebug == true) {
      System.out.println("-------References (" + referencesIndex + ")--------");
    }
    
    for (int i = 0; i < m_References.length; i++) {
      m_References[i] = 0;
    }
    if (referencesIndex > 0)
    {
      NeighborList neighborList = findNeighbors(bag, referencesIndex, m_TrainBags);
      if (m_ReferencesDebug == true) {
        System.out.println("Bag: " + bag + " Neighbors: ");
        neighborList.printReducedList();
      }
      NeighborNode current = mFirst;
      while (current != null) {
        index = (int)mBag.classValue();
        m_References[index] += 1;
        current = mNext;
      }
    }
    if (m_ReferencesDebug == true) {
      System.out.println("References:");
      for (int j = 0; j < m_References.length; j++) {
        System.out.println("[" + j + "]: " + m_References[j]);
      }
    }
  }
  






  protected NeighborList findNeighbors(Instance bag, int kNN, Instances bags)
  {
    int index = 0;
    
    if (kNN > bags.numInstances()) {
      kNN = bags.numInstances() - 1;
    }
    NeighborList neighborList = new NeighborList(kNN);
    for (int i = 0; i < bags.numInstances(); i++) {
      if (bag != bags.instance(i)) {
        double distance = distanceSet(bag, bags.instance(i));
        if (m_NeighborListDebug)
          System.out.println("distance(bag, " + i + "): " + distance);
        if ((neighborList.isEmpty()) || (index < kNN) || (distance <= access$400mDistance))
          neighborList.insertSorted(distance, bags.instance(i), i);
        index++;
      }
    }
    
    if (m_NeighborListDebug) {
      System.out.println("bag neighbors:");
      neighborList.printReducedList();
    }
    
    return neighborList;
  }
  





  public double distanceSet(Instance first, Instance second)
  {
    double[] h_f = new double[first.relationalValue(1).numInstances()];
    


    for (int i = 0; i < h_f.length; i++) {
      h_f[i] = Double.MAX_VALUE;
    }
    
    int rank;
    
    int rank;
    if (m_HDRank >= first.relationalValue(1).numInstances()) {
      rank = first.relationalValue(1).numInstances(); } else { int rank;
      if (m_HDRank < 1) {
        rank = 1;
      } else
        rank = m_HDRank;
    }
    if (m_HDistanceDebug) {
      System.out.println("-------HAUSDORFF DISTANCE--------");
      System.out.println("rank: " + rank + "\nset of instances:");
      System.out.println("\tset 1:");
      for (int i = 0; i < first.relationalValue(1).numInstances(); i++) {
        System.out.println(first.relationalValue(1).instance(i));
      }
      System.out.println("\n\tset 2:");
      for (int i = 0; i < second.relationalValue(1).numInstances(); i++) {
        System.out.println(second.relationalValue(1).instance(i));
      }
      System.out.println("\n");
    }
    

    for (int i = 0; i < first.relationalValue(1).numInstances(); i++)
    {

      if (m_HDistanceDebug) {
        System.out.println("\nDistances:");
      }
      for (int j = 0; j < second.relationalValue(1).numInstances(); j++) {
        double distance = distance(first.relationalValue(1).instance(i), second.relationalValue(1).instance(j));
        if (distance < h_f[i])
          h_f[i] = distance;
        if (m_HDistanceDebug) {
          System.out.println("\tdist(" + i + ", " + j + "): " + distance + "  --> h_f[" + i + "]: " + h_f[i]);
        }
      }
    }
    int[] index_f = Utils.stableSort(h_f);
    
    if (m_HDistanceDebug) {
      System.out.println("\nRanks:\n");
      for (int i = 0; i < index_f.length; i++) {
        System.out.println("\trank " + (i + 1) + ": " + h_f[index_f[i]]);
      }
      System.out.println("\n\t\t>>>>> rank " + rank + ": " + h_f[index_f[(rank - 1)]] + " <<<<<");
    }
    
    return h_f[index_f[(rank - 1)]];
  }
  






  public double distance(Instance first, Instance second)
  {
    double sum = 0.0D;
    for (int i = 0; i < m_Attributes.numAttributes(); i++) {
      double diff = (first.value(i) - m_Min[i]) / m_Diffs[i] - (second.value(i) - m_Min[i]) / m_Diffs[i];
      
      sum += diff * diff;
    }
    return sum = Math.sqrt(sum);
  }
  







  public double[] distributionForInstance(Instance bag)
    throws Exception
  {
    if (m_TrainBags.numInstances() == 0) {
      throw new Exception("No training bags!");
    }
    updateNormalization(bag);
    

    countBagReferences(bag);
    

    countBagCiters(bag);
    
    return makeDistribution();
  }
  









  public void updateNormalization(Instance bag)
  {
    for (int i = 0; i < m_TrainBags.attribute(1).relation().numAttributes(); i++) {
      double min = m_Min[i] / m_MinNorm;
      double max = m_Max[i] / m_MaxNorm;
      
      Instances instances = bag.relationalValue(1);
      for (int k = 0; k < instances.numInstances(); k++) {
        Instance instance = instances.instance(k);
        if (instance.value(i) < min)
          min = instance.value(i);
        if (instance.value(i) > max)
          max = instance.value(i);
      }
      m_Min[i] = (min * m_MinNorm);
      m_Max[i] = (max * m_MaxNorm);
      m_Diffs[i] = (max * m_MaxNorm - min * m_MinNorm);
    }
  }
  





  public boolean equalExemplars(Instance exemplar1, Instance exemplar2)
  {
    if (exemplar1.relationalValue(1).numInstances() == exemplar2.relationalValue(1).numInstances())
    {
      Instances instances1 = exemplar1.relationalValue(1);
      Instances instances2 = exemplar2.relationalValue(1);
      for (int i = 0; i < instances1.numInstances(); i++) {
        Instance instance1 = instances1.instance(i);
        Instance instance2 = instances2.instance(i);
        for (int j = 0; j < instance1.numAttributes(); j++) {
          if (instance1.value(j) != instance2.value(j)) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }
  





  protected double[] makeDistribution()
    throws Exception
  {
    double total = 0.0D;
    double[] distribution = new double[m_TrainBags.numClasses()];
    boolean debug = false;
    
    total = m_TrainBags.numClasses() / Math.max(1, m_TrainBags.numInstances());
    
    for (int i = 0; i < m_TrainBags.numClasses(); i++) {
      distribution[i] = (1.0D / Math.max(1, m_TrainBags.numInstances()));
      if (debug) { System.out.println("distribution[" + i + "]: " + distribution[i]);
      }
    }
    if (debug) { System.out.println("total: " + total);
    }
    for (int i = 0; i < m_TrainBags.numClasses(); i++) {
      distribution[i] += m_References[i];
      distribution[i] += m_Citers[i];
    }
    
    total = 0.0D;
    
    for (int i = 0; i < m_TrainBags.numClasses(); i++) {
      total += distribution[i];
      if (debug) { System.out.println("distribution[" + i + "]: " + distribution[i]);
      }
    }
    for (int i = 0; i < m_TrainBags.numClasses(); i++) {
      distribution[i] /= total;
      if (debug) { System.out.println("distribution[" + i + "]: " + distribution[i]);
      }
    }
    
    return distribution;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tNumber of Nearest References (default 1)", "R", 0, "-R <number of references>"));
    


    result.addElement(new Option("\tNumber of Nearest Citers (default 1)", "C", 0, "-C <number of citers>"));
    


    result.addElement(new Option("\tRank of the Hausdorff Distance (default 1)", "H", 0, "-H <rank>"));
    


    return result.elements();
  }
  




















  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    String option = Utils.getOption('R', options);
    if (option.length() != 0) {
      setNumReferences(Integer.parseInt(option));
    } else {
      setNumReferences(1);
    }
    option = Utils.getOption('C', options);
    if (option.length() != 0) {
      setNumCiters(Integer.parseInt(option));
    } else {
      setNumCiters(1);
    }
    option = Utils.getOption('H', options);
    if (option.length() != 0) {
      setHDRank(Integer.parseInt(option));
    } else {
      setHDRank(1);
    }
  }
  




  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getDebug()) {
      result.add("-D");
    }
    result.add("-R");
    result.add("" + getNumReferences());
    
    result.add("-C");
    result.add("" + getNumCiters());
    
    result.add("-H");
    result.add("" + getHDRank());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  







  public String toString()
  {
    StringBuffer result = new StringBuffer();
    

    result.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
    result.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
    
    if (m_Citers == null) {
      result.append("no model built yet!\n");
    }
    else
    {
      result.append("Citers....: " + Utils.arrayToString(m_Citers) + "\n");
      
      result.append("References: " + Utils.arrayToString(m_References) + "\n");
      
      result.append("Min.......: ");
      for (int i = 0; i < m_Min.length; i++) {
        if (i > 0)
          result.append(",");
        result.append(Utils.doubleToString(m_Min[i], 3));
      }
      result.append("\n");
      
      result.append("Max.......: ");
      for (i = 0; i < m_Max.length; i++) {
        if (i > 0)
          result.append(",");
        result.append(Utils.doubleToString(m_Max[i], 3));
      }
      result.append("\n");
      
      result.append("Diffs.....: ");
      for (i = 0; i < m_Diffs.length; i++) {
        if (i > 0)
          result.append(",");
        result.append(Utils.doubleToString(m_Diffs[i], 3));
      }
      result.append("\n");
    }
    
    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9146 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new CitationKNN(), argv);
  }
  





  private class NeighborNode
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -3947320761906511289L;
    



    private Instance mBag;
    



    private double mDistance;
    



    private NeighborNode mNext;
    



    private int mBagPosition;
    




    public NeighborNode(double distance, Instance bag, int position, NeighborNode next)
    {
      mDistance = distance;
      mBag = bag;
      mNext = next;
      mBagPosition = position;
    }
    






    public NeighborNode(double distance, Instance bag, int position)
    {
      this(distance, bag, position, null);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9146 $");
    }
  }
  




  private class NeighborList
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 3432555644456217394L;
    


    private CitationKNN.NeighborNode mFirst;
    


    private CitationKNN.NeighborNode mLast;
    


    private int mLength = 1;
    




    public NeighborList(int length)
    {
      mLength = length;
    }
    



    public boolean isEmpty()
    {
      return mFirst == null;
    }
    




    public int currentLength()
    {
      int i = 0;
      CitationKNN.NeighborNode current = mFirst;
      while (current != null) {
        i++;
        current = CitationKNN.NeighborNode.access$200(current);
      }
      return i;
    }
    








    public void insertSorted(double distance, Instance bag, int position)
    {
      if (isEmpty()) {
        mFirst = (this.mLast = new CitationKNN.NeighborNode(CitationKNN.this, distance, bag, position));
      } else {
        CitationKNN.NeighborNode current = mFirst;
        if (distance < CitationKNN.NeighborNode.access$100(mFirst)) {
          mFirst = new CitationKNN.NeighborNode(CitationKNN.this, distance, bag, position, mFirst);
        } else {
          while ((CitationKNN.NeighborNode.access$200(current) != null) && (CitationKNN.NeighborNode.access$100(CitationKNN.NeighborNode.access$200(current)) < distance))
          {
            current = CitationKNN.NeighborNode.access$200(current); }
          CitationKNN.NeighborNode.access$202(current, new CitationKNN.NeighborNode(CitationKNN.this, distance, bag, position, CitationKNN.NeighborNode.access$200(current)));
          if (current.equals(mLast)) {
            mLast = CitationKNN.NeighborNode.access$200(current);
          }
        }
        


        int valcount = 0;
        for (current = mFirst; CitationKNN.NeighborNode.access$200(current) != null; 
            current = CitationKNN.NeighborNode.access$200(current)) {
          valcount++;
          if ((valcount >= mLength) && (CitationKNN.NeighborNode.access$100(current) != CitationKNN.NeighborNode.access$100(CitationKNN.NeighborNode.access$200(current))))
          {
            mLast = current;
            CitationKNN.NeighborNode.access$202(current, null);
            break;
          }
        }
      }
    }
    





    public void pruneToK(int k)
    {
      if (isEmpty())
        return;
      if (k < 1) {
        k = 1;
      }
      int currentK = 0;
      double currentDist = CitationKNN.NeighborNode.access$100(mFirst);
      for (CitationKNN.NeighborNode current = mFirst; 
          CitationKNN.NeighborNode.access$200(current) != null; current = CitationKNN.NeighborNode.access$200(current)) {
        currentK++;
        currentDist = CitationKNN.NeighborNode.access$100(current);
        if ((currentK >= k) && (currentDist != CitationKNN.NeighborNode.access$100(CitationKNN.NeighborNode.access$200(current)))) {
          mLast = current;
          CitationKNN.NeighborNode.access$202(current, null);
          break;
        }
      }
    }
    



    public void printList()
    {
      if (isEmpty()) {
        System.out.println("Empty list");
      } else {
        CitationKNN.NeighborNode current = mFirst;
        while (current != null) {
          System.out.print("Node: instance " + CitationKNN.NeighborNode.access$500(current) + "\n");
          System.out.println(CitationKNN.NeighborNode.access$300(current));
          System.out.println(", distance " + CitationKNN.NeighborNode.access$100(current));
          current = CitationKNN.NeighborNode.access$200(current);
        }
        System.out.println();
      }
    }
    


    public void printReducedList()
    {
      if (isEmpty()) {
        System.out.println("Empty list");
      } else {
        CitationKNN.NeighborNode current = mFirst;
        while (current != null) {
          System.out.print("Node: bag " + CitationKNN.NeighborNode.access$500(current) + "  (" + CitationKNN.NeighborNode.access$300(current).relationalValue(1).numInstances() + "): ");
          


          System.out.print("   <" + CitationKNN.NeighborNode.access$300(current).classValue() + ">");
          System.out.println("  (d: " + CitationKNN.NeighborNode.access$100(current) + ")");
          current = CitationKNN.NeighborNode.access$200(current);
        }
        System.out.println();
      }
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9146 $");
    }
  }
}

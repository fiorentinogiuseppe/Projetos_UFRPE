package weka.classifiers.trees;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.ContingencyTables;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;





































































public class RandomTree
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, Randomizable, Drawable
{
  static final long serialVersionUID = 8934314652175299374L;
  protected Tree m_Tree;
  protected Instances m_Info;
  protected double m_MinNum;
  protected int m_KValue;
  protected int m_randomSeed;
  protected int m_MaxDepth;
  protected int m_NumFolds;
  protected boolean m_AllowUnclassifiedInstances;
  protected Classifier m_zeroR;
  
  public RandomTree()
  {
    m_Tree = null;
    

    m_Info = null;
    

    m_MinNum = 1.0D;
    

    m_KValue = 0;
    

    m_randomSeed = 1;
    

    m_MaxDepth = 0;
    

    m_NumFolds = 0;
    

    m_AllowUnclassifiedInstances = false;
  }
  








  public String globalInfo()
  {
    return "Class for constructing a tree that considers K randomly  chosen attributes at each node. Performs no pruning. Also has an option to allow estimation of class probabilities based on a hold-out set (backfitting).";
  }
  








  public String minNumTipText()
  {
    return "The minimum total weight of the instances in a leaf.";
  }
  





  public double getMinNum()
  {
    return m_MinNum;
  }
  





  public void setMinNum(double newMinNum)
  {
    m_MinNum = newMinNum;
  }
  





  public String KValueTipText()
  {
    return "Sets the number of randomly chosen attributes. If 0, log_2(number_of_attributes) + 1 is used.";
  }
  





  public int getKValue()
  {
    return m_KValue;
  }
  





  public void setKValue(int k)
  {
    m_KValue = k;
  }
  





  public String seedTipText()
  {
    return "The random number seed used for selecting attributes.";
  }
  





  public void setSeed(int seed)
  {
    m_randomSeed = seed;
  }
  





  public int getSeed()
  {
    return m_randomSeed;
  }
  





  public String maxDepthTipText()
  {
    return "The maximum depth of the tree, 0 for unlimited.";
  }
  




  public int getMaxDepth()
  {
    return m_MaxDepth;
  }
  





  public String numFoldsTipText()
  {
    return "Determines the amount of data used for backfitting. One fold is used for backfitting, the rest for growing the tree. (Default: 0, no backfitting)";
  }
  






  public int getNumFolds()
  {
    return m_NumFolds;
  }
  





  public void setNumFolds(int newNumFolds)
  {
    m_NumFolds = newNumFolds;
  }
  





  public String allowUnclassifiedInstancesTipText()
  {
    return "Whether to allow unclassified instances.";
  }
  





  public boolean getAllowUnclassifiedInstances()
  {
    return m_AllowUnclassifiedInstances;
  }
  







  public void setAllowUnclassifiedInstances(boolean newAllowUnclassifiedInstances)
  {
    m_AllowUnclassifiedInstances = newAllowUnclassifiedInstances;
  }
  




  public void setMaxDepth(int value)
  {
    m_MaxDepth = value;
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tNumber of attributes to randomly investigate. (default 0)\n\t(<0 = int(log_2(#predictors)+1)).", "K", 1, "-K <number of attributes>"));
    



    newVector.addElement(new Option("\tSet minimum number of instances per leaf.", "M", 1, "-M <minimum number of instances>"));
    


    newVector.addElement(new Option("\tSeed for random number generator.\n\t(default 1)", "S", 1, "-S <num>"));
    

    newVector.addElement(new Option("\tThe maximum depth of the tree, 0 for unlimited.\n\t(default 0)", "depth", 1, "-depth <num>"));
    



    newVector.addElement(new Option("\tNumber of folds for backfitting (default 0, no backfitting).", "N", 1, "-N <num>"));
    
    newVector.addElement(new Option("\tAllow unclassified instances.", "U", 0, "-U"));
    

    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    return newVector.elements();
  }
  









  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-K");
    result.add("" + getKValue());
    
    result.add("-M");
    result.add("" + getMinNum());
    
    result.add("-S");
    result.add("" + getSeed());
    
    if (getMaxDepth() > 0) {
      result.add("-depth");
      result.add("" + getMaxDepth());
    }
    
    if (getNumFolds() > 0) {
      result.add("-N");
      result.add("" + getNumFolds());
    }
    
    if (getAllowUnclassifiedInstances()) {
      result.add("-U");
    }
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





















































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('K', options);
    if (tmpStr.length() != 0) {
      m_KValue = Integer.parseInt(tmpStr);
    } else {
      m_KValue = 0;
    }
    
    tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0) {
      m_MinNum = Double.parseDouble(tmpStr);
    } else {
      m_MinNum = 1.0D;
    }
    
    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setSeed(Integer.parseInt(tmpStr));
    } else {
      setSeed(1);
    }
    
    tmpStr = Utils.getOption("depth", options);
    if (tmpStr.length() != 0) {
      setMaxDepth(Integer.parseInt(tmpStr));
    } else {
      setMaxDepth(0);
    }
    String numFoldsString = Utils.getOption('N', options);
    if (numFoldsString.length() != 0) {
      m_NumFolds = Integer.parseInt(numFoldsString);
    } else {
      m_NumFolds = 0;
    }
    
    setAllowUnclassifiedInstances(Utils.getFlag('U', options));
    
    super.setOptions(options);
    
    Utils.checkForRemainingOptions(options);
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  







  public void buildClassifier(Instances data)
    throws Exception
  {
    if (m_KValue > data.numAttributes() - 1)
      m_KValue = (data.numAttributes() - 1);
    if (m_KValue < 1) {
      m_KValue = ((int)Utils.log2(data.numAttributes() - 1) + 1);
    }
    
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    

    if (data.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_zeroR = new ZeroR();
      m_zeroR.buildClassifier(data);
      return;
    }
    m_zeroR = null;
    


    Instances train = null;
    Instances backfit = null;
    Random rand = data.getRandomNumberGenerator(m_randomSeed);
    if (m_NumFolds <= 0) {
      train = data;
    } else {
      data.randomize(rand);
      data.stratify(m_NumFolds);
      train = data.trainCV(m_NumFolds, 1, rand);
      backfit = data.testCV(m_NumFolds, 1);
    }
    

    int[] attIndicesWindow = new int[data.numAttributes() - 1];
    int j = 0;
    for (int i = 0; i < attIndicesWindow.length; i++) {
      if (j == data.classIndex())
        j++;
      attIndicesWindow[i] = (j++);
    }
    

    double[] classProbs = new double[train.numClasses()];
    for (int i = 0; i < train.numInstances(); i++) {
      Instance inst = train.instance(i);
      classProbs[((int)inst.classValue())] += inst.weight();
    }
    

    m_Tree = new Tree();
    m_Info = new Instances(data, 0);
    m_Tree.buildTree(train, classProbs, attIndicesWindow, rand, 0);
    

    if (backfit != null) {
      m_Tree.backfitData(backfit);
    }
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_zeroR != null) {
      return m_zeroR.distributionForInstance(instance);
    }
    return m_Tree.distributionForInstance(instance);
  }
  








  public String toString()
  {
    if (m_zeroR != null) {
      StringBuffer buf = new StringBuffer();
      buf.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
      buf.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
      

      buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
      buf.append(m_zeroR.toString());
      return buf.toString();
    }
    
    if (m_Tree == null) {
      return "RandomTree: no model has been built yet.";
    }
    return "\nRandomTree\n==========\n" + m_Tree.toString(0) + "\n" + "\nSize of the tree : " + m_Tree.numNodes() + (getMaxDepth() > 0 ? "\nMax depth of tree: " + getMaxDepth() : "");
  }
  












  public String graph()
    throws Exception
  {
    if (m_Tree == null) {
      throw new Exception("RandomTree: No model built yet.");
    }
    StringBuffer resultBuff = new StringBuffer();
    m_Tree.toGraph(resultBuff, 0, null);
    String result = "digraph RandomTree {\nedge [style=bold]\n" + resultBuff.toString() + "\n}\n";
    
    return result;
  }
  




  public int graphType()
  {
    return 1;
  }
  


  public void generatePartition(Instances data)
    throws Exception
  {
    buildClassifier(data);
  }
  



  public double[] getMembershipValues(Instance instance)
    throws Exception
  {
    if (m_zeroR != null) {
      double[] m = new double[1];
      m[0] = instance.weight();
      return m;
    }
    

    double[] a = new double[numElements()];
    

    Queue<Double> queueOfWeights = new LinkedList();
    Queue<Tree> queueOfNodes = new LinkedList();
    queueOfWeights.add(Double.valueOf(instance.weight()));
    queueOfNodes.add(m_Tree);
    int index = 0;
    

    while (!queueOfNodes.isEmpty())
    {
      a[(index++)] = ((Double)queueOfWeights.poll()).doubleValue();
      Tree node = (Tree)queueOfNodes.poll();
      

      if (m_Attribute > -1)
      {



        double[] weights = new double[m_Successors.length];
        if (instance.isMissing(m_Attribute)) {
          System.arraycopy(m_Prop, 0, weights, 0, m_Prop.length);
        } else if (m_Info.attribute(m_Attribute).isNominal()) {
          weights[((int)instance.value(m_Attribute))] = 1.0D;
        }
        else if (instance.value(m_Attribute) < m_SplitPoint) {
          weights[0] = 1.0D;
        } else {
          weights[1] = 1.0D;
        }
        
        for (int i = 0; i < m_Successors.length; i++) {
          queueOfNodes.add(m_Successors[i]);
          queueOfWeights.add(Double.valueOf(a[(index - 1)] * weights[i]));
        }
      } }
    return a;
  }
  



  public int numElements()
    throws Exception
  {
    if (m_zeroR != null) {
      return 1;
    }
    return m_Tree.numNodes();
  }
  



  protected class Tree
    implements Serializable
  {
    private static final long serialVersionUID = 3549573538656522569L;
    

    protected Tree[] m_Successors;
    

    protected int m_Attribute = -1;
    

    protected double m_SplitPoint = NaN.0D;
    

    protected double[] m_Prop = null;
    

    protected double[] m_ClassDistribution = null;
    

    protected Tree() {}
    
    public void backfitData(Instances data)
      throws Exception
    {
      double[] classProbs = new double[data.numClasses()];
      for (int i = 0; i < data.numInstances(); i++) {
        Instance inst = data.instance(i);
        classProbs[((int)inst.classValue())] += inst.weight();
      }
      

      backfitData(data, classProbs);
    }
    






    public double[] distributionForInstance(Instance instance)
      throws Exception
    {
      double[] returnedDist = null;
      
      if (m_Attribute > -1)
      {

        if (instance.isMissing(m_Attribute))
        {

          returnedDist = new double[m_Info.numClasses()];
          

          for (int i = 0; i < m_Successors.length; i++) {
            double[] help = m_Successors[i].distributionForInstance(instance);
            if (help != null) {
              for (int j = 0; j < help.length; j++) {
                returnedDist[j] += m_Prop[i] * help[j];
              }
            }
          }
        } else if (m_Info.attribute(m_Attribute).isNominal())
        {

          returnedDist = m_Successors[((int)instance.value(m_Attribute))].distributionForInstance(instance);



        }
        else if (instance.value(m_Attribute) < m_SplitPoint) {
          returnedDist = m_Successors[0].distributionForInstance(instance);
        } else {
          returnedDist = m_Successors[1].distributionForInstance(instance);
        }
      }
      


      if ((m_Attribute == -1) || (returnedDist == null))
      {

        if (m_ClassDistribution == null) {
          if (getAllowUnclassifiedInstances()) {
            return new double[m_Info.numClasses()];
          }
          return null;
        }
        


        double[] normalizedDistribution = (double[])m_ClassDistribution.clone();
        Utils.normalize(normalizedDistribution);
        return normalizedDistribution;
      }
      return returnedDist;
    }
    








    public int toGraph(StringBuffer text, int num)
      throws Exception
    {
      int maxIndex = Utils.maxIndex(m_ClassDistribution);
      String classValue = Utils.backQuoteChars(m_Info.classAttribute().value(maxIndex));
      
      num++;
      if (m_Attribute == -1) {
        text.append("N" + Integer.toHexString(hashCode()) + " [label=\"" + num + ": " + classValue + "\"" + "shape=box]\n");
      }
      else {
        text.append("N" + Integer.toHexString(hashCode()) + " [label=\"" + num + ": " + classValue + "\"]\n");
        
        for (int i = 0; i < m_Successors.length; i++) {
          text.append("N" + Integer.toHexString(hashCode()) + "->" + "N" + Integer.toHexString(m_Successors[i].hashCode()) + " [label=\"" + Utils.backQuoteChars(m_Info.attribute(m_Attribute).name()));
          

          if (m_Info.attribute(m_Attribute).isNumeric()) {
            if (i == 0) {
              text.append(" < " + Utils.doubleToString(m_SplitPoint, 2));
            } else {
              text.append(" >= " + Utils.doubleToString(m_SplitPoint, 2));
            }
          } else {
            text.append(" = " + Utils.backQuoteChars(m_Info.attribute(m_Attribute).value(i)));
          }
          text.append("\"]\n");
          num = m_Successors[i].toGraph(text, num);
        }
      }
      
      return num;
    }
    





    protected String leafString()
      throws Exception
    {
      double sum = 0.0D;double maxCount = 0.0D;
      int maxIndex = 0;
      if (m_ClassDistribution != null) {
        sum = Utils.sum(m_ClassDistribution);
        maxIndex = Utils.maxIndex(m_ClassDistribution);
        maxCount = m_ClassDistribution[maxIndex];
      }
      return " : " + m_Info.classAttribute().value(maxIndex) + " (" + Utils.doubleToString(sum, 2) + "/" + Utils.doubleToString(sum - maxCount, 2) + ")";
    }
    







    protected String toString(int level)
    {
      try
      {
        StringBuffer text = new StringBuffer();
        
        if (m_Attribute == -1)
        {

          return leafString(); }
        if (m_Info.attribute(m_Attribute).isNominal())
        {

          for (int i = 0; i < m_Successors.length; i++) {
            text.append("\n");
            for (int j = 0; j < level; j++) {
              text.append("|   ");
            }
            text.append(m_Info.attribute(m_Attribute).name() + " = " + m_Info.attribute(m_Attribute).value(i));
            
            text.append(m_Successors[i].toString(level + 1));
          }
        }
        else
        {
          text.append("\n");
          for (int j = 0; j < level; j++) {
            text.append("|   ");
          }
          text.append(m_Info.attribute(m_Attribute).name() + " < " + Utils.doubleToString(m_SplitPoint, 2));
          
          text.append(m_Successors[0].toString(level + 1));
          text.append("\n");
          for (int j = 0; j < level; j++) {
            text.append("|   ");
          }
          text.append(m_Info.attribute(m_Attribute).name() + " >= " + Utils.doubleToString(m_SplitPoint, 2));
          
          text.append(m_Successors[1].toString(level + 1));
        }
        
        return text.toString();
      } catch (Exception e) {
        e.printStackTrace(); }
      return "RandomTree: tree can't be printed";
    }
    









    protected void backfitData(Instances data, double[] classProbs)
      throws Exception
    {
      if (data.numInstances() == 0) {
        m_Attribute = -1;
        m_ClassDistribution = null;
        m_Prop = null;
        return;
      }
      


      m_ClassDistribution = ((double[])classProbs.clone());
      









      if (m_Attribute > -1)
      {

        m_Prop = new double[m_Successors.length];
        for (int i = 0; i < data.numInstances(); i++) {
          Instance inst = data.instance(i);
          if (!inst.isMissing(m_Attribute)) {
            if (data.attribute(m_Attribute).isNominal()) {
              m_Prop[((int)inst.value(m_Attribute))] += inst.weight();
            } else {
              m_Prop[(inst.value(m_Attribute) < m_SplitPoint ? 0 : 1)] += inst.weight();
            }
          }
        }
        


        if (Utils.sum(m_Prop) <= 0.0D) {
          m_Attribute = -1;
          m_Prop = null;
          return;
        }
        

        Utils.normalize(m_Prop);
        

        Instances[] subsets = splitData(data);
        

        for (int i = 0; i < subsets.length; i++)
        {

          double[] dist = new double[data.numClasses()];
          for (int j = 0; j < subsets[i].numInstances(); j++) {
            dist[((int)subsets[i].instance(j).classValue())] += subsets[i].instance(j).weight();
          }
          


          m_Successors[i].backfitData(subsets[i], dist);
        }
        


        if (getAllowUnclassifiedInstances()) {
          m_ClassDistribution = null;
          return;
        }
        


        boolean emptySuccessor = false;
        for (int i = 0; i < subsets.length; i++) {
          if (m_Successors[i].m_ClassDistribution == null) {
            emptySuccessor = true;
            return;
          }
        }
        m_ClassDistribution = null;
      }
    }
    





















    protected void buildTree(Instances data, double[] classProbs, int[] attIndicesWindow, Random random, int depth)
      throws Exception
    {
      if (data.numInstances() == 0) {
        m_Attribute = -1;
        m_ClassDistribution = null;
        m_Prop = null;
        return;
      }
      


      m_ClassDistribution = ((double[])classProbs.clone());
      
      if ((Utils.sum(m_ClassDistribution) < 2.0D * m_MinNum) || (Utils.eq(m_ClassDistribution[Utils.maxIndex(m_ClassDistribution)], Utils.sum(m_ClassDistribution))) || ((getMaxDepth() > 0) && (depth >= getMaxDepth())))
      {



        m_Attribute = -1;
        m_Prop = null;
        return;
      }
      


      double val = -1.7976931348623157E308D;
      double split = -1.7976931348623157E308D;
      double[][] bestDists = (double[][])null;
      double[] bestProps = null;
      int bestIndex = 0;
      

      double[][] props = new double[1][0];
      double[][][] dists = new double[1][0][0];
      

      int attIndex = 0;
      int windowSize = attIndicesWindow.length;
      int k = m_KValue;
      boolean gainFound = false;
      while ((windowSize > 0) && ((k-- > 0) || (!gainFound)))
      {
        int chosenIndex = random.nextInt(windowSize);
        attIndex = attIndicesWindow[chosenIndex];
        

        attIndicesWindow[chosenIndex] = attIndicesWindow[(windowSize - 1)];
        attIndicesWindow[(windowSize - 1)] = attIndex;
        windowSize--;
        
        double currSplit = distribution(props, dists, attIndex, data);
        double currVal = gain(dists[0], priorVal(dists[0]));
        
        if (Utils.gr(currVal, 0.0D)) {
          gainFound = true;
        }
        if ((currVal > val) || ((currVal == val) && (attIndex < bestIndex))) {
          val = currVal;
          bestIndex = attIndex;
          split = currSplit;
          bestProps = props[0];
          bestDists = dists[0];
        }
      }
      

      m_Attribute = bestIndex;
      

      if (Utils.gr(val, 0.0D))
      {

        m_SplitPoint = split;
        m_Prop = bestProps;
        Instances[] subsets = splitData(data);
        m_Successors = new Tree[bestDists.length];
        for (int i = 0; i < bestDists.length; i++) {
          m_Successors[i] = new Tree(RandomTree.this);
          m_Successors[i].buildTree(subsets[i], bestDists[i], attIndicesWindow, random, depth + 1);
        }
        



        boolean emptySuccessor = false;
        for (int i = 0; i < subsets.length; i++) {
          if (m_Successors[i].m_ClassDistribution == null) {
            emptySuccessor = true;
            break;
          }
        }
        if (!emptySuccessor) {
          m_ClassDistribution = null;
        }
      }
      else
      {
        m_Attribute = -1;
      }
    }
    





    public int numNodes()
    {
      if (m_Attribute == -1) {
        return 1;
      }
      int size = 1;
      for (int i = 0; i < m_Successors.length; i++) {
        size += m_Successors[i].numNodes();
      }
      return size;
    }
    








    protected Instances[] splitData(Instances data)
      throws Exception
    {
      Instances[] subsets = new Instances[m_Prop.length];
      for (int i = 0; i < m_Prop.length; i++) {
        subsets[i] = new Instances(data, data.numInstances());
      }
      

      for (int i = 0; i < data.numInstances(); i++)
      {

        Instance inst = data.instance(i);
        

        if (inst.isMissing(m_Attribute))
        {

          for (int k = 0; k < m_Prop.length; k++) {
            if (m_Prop[k] > 0.0D) {
              Instance copy = (Instance)inst.copy();
              copy.setWeight(m_Prop[k] * inst.weight());
              subsets[k].add(copy);

            }
            

          }
          

        }
        else if (data.attribute(m_Attribute).isNominal()) {
          subsets[((int)inst.value(m_Attribute))].add(inst);





        }
        else if (data.attribute(m_Attribute).isNumeric()) {
          subsets[1].add(inst);


        }
        else
        {

          throw new IllegalArgumentException("Unknown attribute type");
        }
      }
      
      for (int i = 0; i < m_Prop.length; i++) {
        subsets[i].compactify();
      }
      

      return subsets;
    }
    









    protected double distribution(double[][] props, double[][][] dists, int att, Instances data)
      throws Exception
    {
      double splitPoint = NaN.0D;
      Attribute attribute = data.attribute(att);
      double[][] dist = (double[][])null;
      int indexOfFirstMissingValue = data.numInstances();
      
      if (attribute.isNominal())
      {

        dist = new double[attribute.numValues()][data.numClasses()];
        for (int i = 0; i < data.numInstances(); i++) {
          Instance inst = data.instance(i);
          if (inst.isMissing(att))
          {

            if (indexOfFirstMissingValue == data.numInstances()) {
              indexOfFirstMissingValue = i;
            }
          }
          else {
            dist[((int)inst.value(att))][((int)inst.classValue())] += inst.weight();
          }
        }
      }
      else {
        double[][] currDist = new double[2][data.numClasses()];
        dist = new double[2][data.numClasses()];
        

        data.sort(att);
        

        for (int j = 0; j < data.numInstances(); j++) {
          Instance inst = data.instance(j);
          if (inst.isMissing(att))
          {

            indexOfFirstMissingValue = j;
            break;
          }
          currDist[1][((int)inst.classValue())] += inst.weight();
        }
        

        double priorVal = priorVal(currDist);
        

        for (int j = 0; j < currDist.length; j++) {
          System.arraycopy(currDist[j], 0, dist[j], 0, dist[j].length);
        }
        

        double currSplit = data.instance(0).value(att);
        double bestVal = -1.7976931348623157E308D;
        for (int i = 0; i < indexOfFirstMissingValue; i++) {
          Instance inst = data.instance(i);
          

          if (inst.value(att) > currSplit)
          {

            double currVal = gain(currDist, priorVal);
            

            if (currVal > bestVal)
            {

              bestVal = currVal;
              

              splitPoint = (inst.value(att) + currSplit) / 2.0D;
              

              if (splitPoint <= currSplit) {
                splitPoint = inst.value(att);
              }
              

              for (int j = 0; j < currDist.length; j++) {
                System.arraycopy(currDist[j], 0, dist[j], 0, dist[j].length);
              }
            }
            currSplit = inst.value(att);
          }
          

          currDist[0][((int)inst.classValue())] += inst.weight();
          currDist[1][((int)inst.classValue())] -= inst.weight();
        }
      }
      

      props[0] = new double[dist.length];
      for (int k = 0; k < props[0].length; k++) {
        props[0][k] = Utils.sum(dist[k]);
      }
      if (Utils.eq(Utils.sum(props[0]), 0.0D)) {
        for (int k = 0; k < props[0].length; k++) {
          props[0][k] = (1.0D / props[0].length);
        }
      } else {
        Utils.normalize(props[0]);
      }
      

      for (int i = indexOfFirstMissingValue; i < data.numInstances(); i++) {
        Instance inst = data.instance(i);
        if (attribute.isNominal())
        {

          if (inst.isMissing(att)) {
            for (int j = 0; j < dist.length; j++) {
              dist[j][((int)inst.classValue())] += props[0][j] * inst.weight();
            }
            
          }
        }
        else {
          for (int j = 0; j < dist.length; j++) {
            dist[j][((int)inst.classValue())] += props[0][j] * inst.weight();
          }
        }
      }
      

      dists[0] = dist;
      return splitPoint;
    }
    






    protected double priorVal(double[][] dist)
    {
      return ContingencyTables.entropyOverColumns(dist);
    }
    







    protected double gain(double[][] dist, double priorVal)
    {
      return priorVal - ContingencyTables.entropyConditionedOnRows(dist);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 10993 $");
    }
    





    protected int toGraph(StringBuffer text, int num, Tree parent)
      throws Exception
    {
      
      



      if (m_Attribute == -1) {
        text.append("N" + Integer.toHexString(hashCode()) + " [label=\"" + num + leafString() + "\"" + " shape=box]\n");
      }
      else
      {
        text.append("N" + Integer.toHexString(hashCode()) + " [label=\"" + num + ": " + m_Info.attribute(m_Attribute).name() + "\"]\n");
        

        for (int i = 0; i < m_Successors.length; i++) {
          text.append("N" + Integer.toHexString(hashCode()) + "->" + "N" + Integer.toHexString(m_Successors[i].hashCode()) + " [label=\"");
          

          if (m_Info.attribute(m_Attribute).isNumeric()) {
            if (i == 0) {
              text.append(" < " + Utils.doubleToString(m_SplitPoint, 2));
            } else {
              text.append(" >= " + Utils.doubleToString(m_SplitPoint, 2));
            }
          } else {
            text.append(" = " + m_Info.attribute(m_Attribute).value(i));
          }
          text.append("\"]\n");
          num = m_Successors[i].toGraph(text, num, this);
        }
      }
      
      return num;
    }
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new RandomTree(), argv);
  }
}

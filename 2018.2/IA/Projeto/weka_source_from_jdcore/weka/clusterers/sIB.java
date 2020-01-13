package weka.clusterers;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.matrix.Matrix;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;











































































































public class sIB
  extends RandomizableClusterer
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = -8652125897352654213L;
  private Instances m_data;
  public sIB() {}
  
  private class Input
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -2464453171263384037L;
    private double[] Px;
    private double[] Py;
    private Matrix Pyx;
    private Matrix Py_x;
    private double Ixy;
    private double Hy;
    private double Hx;
    private double sumVals;
    
    private Input() {}
    
    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5538 $");
    }
  }
  



  private class Partition
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 4957194978951259946L;
    


    private int[] Pt_x;
    


    private double[] Pt;
    


    private double L;
    

    private int counter;
    

    private Matrix Py_t;
    


    public Partition()
    {
      Pt_x = new int[m_numInstances];
      for (int i = 0; i < m_numInstances; i++) {
        Pt_x[i] = -1;
      }
      Pt = new double[m_numCluster];
      Py_t = new Matrix(m_numAttributes, m_numCluster);
      counter = 0;
    }
    




    private ArrayList<Integer> find(int i)
    {
      ArrayList<Integer> indices = new ArrayList();
      for (int x = 0; x < Pt_x.length; x++) {
        if (Pt_x[x] == i) {
          indices.add(Integer.valueOf(x));
        }
      }
      return indices;
    }
    




    private int size(int i)
    {
      int count = 0;
      for (int x = 0; x < Pt_x.length; x++) {
        if (Pt_x[x] == i) {
          count++;
        }
      }
      return count;
    }
    



    private void copy(Partition T)
    {
      if (T == null) {
        T = new Partition(sIB.this);
      }
      System.arraycopy(Pt_x, 0, Pt_x, 0, Pt_x.length);
      System.arraycopy(Pt, 0, Pt, 0, Pt.length);
      L = L;
      counter = counter;
      
      double[][] mArray = Py_t.getArray();
      double[][] tgtArray = Py_t.getArray();
      for (int i = 0; i < mArray.length; i++) {
        System.arraycopy(mArray[i], 0, tgtArray[i], 0, mArray[0].length);
      }
    }
    




    public String toString()
    {
      StringBuffer text = new StringBuffer();
      text.append("score (L) : " + Utils.doubleToString(L, 4) + "\n");
      text.append("number of changes : " + counter + "\n");
      for (int i = 0; i < m_numCluster; i++) {
        text.append("\nCluster " + i + "\n");
        text.append("size : " + size(i) + "\n");
        text.append("prior prob : " + Utils.doubleToString(Pt[i], 4) + "\n");
      }
      return text.toString();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5538 $");
    }
  }
  




  private int m_numCluster = 2;
  

  private int m_numRestarts = 5;
  

  private boolean m_verbose = false;
  

  private boolean m_uniformPrior = true;
  

  private int m_maxLoop = 100;
  

  private int m_minChange = 0;
  


  private ReplaceMissingValues m_replaceMissing;
  


  private int m_numInstances;
  


  private int m_numAttributes;
  

  private Random random;
  

  private Partition bestT;
  

  private Input input;
  


  public void buildClusterer(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    m_replaceMissing = new ReplaceMissingValues();
    Instances instances = new Instances(data);
    instances.setClassIndex(-1);
    m_replaceMissing.setInputFormat(instances);
    data = Filter.useFilter(instances, m_replaceMissing);
    instances = null;
    

    m_data = data;
    m_numInstances = m_data.numInstances();
    m_numAttributes = m_data.numAttributes();
    random = new Random(getSeed());
    

    input = sIB_ProcessInput();
    

    bestT = new Partition();
    

    double bestL = Double.NEGATIVE_INFINITY;
    for (int k = 0; k < m_numRestarts; k++) {
      if (m_verbose) {
        System.out.format("restart number %s...\n", new Object[] { Integer.valueOf(k) });
      }
      

      Partition tmpT = sIB_InitT(input);
      tmpT = sIB_OptimizeT(tmpT, input);
      

      if (L > bestL) {
        tmpT.copy(bestT);
        bestL = bestT.L;
      }
      
      if (m_verbose) {
        System.out.println("\nPartition status : ");
        System.out.println("------------------");
        System.out.println(tmpT.toString() + "\n");
      }
    }
    
    if (m_verbose) {
      System.out.println("\nBest Partition");
      System.out.println("===============");
      System.out.println(bestT.toString());
    }
    

    m_data = new Instances(m_data, 0);
  }
  


  public int clusterInstance(Instance instance)
    throws Exception
  {
    double prior = 1.0D / input.sumVals;
    double[] distances = new double[m_numCluster];
    for (int i = 0; i < m_numCluster; i++) {
      double Pnew = bestT.Pt[i] + prior;
      double pi1 = prior / Pnew;
      double pi2 = bestT.Pt[i] / Pnew;
      distances[i] = (Pnew * JS(instance, i, pi1, pi2));
    }
    return Utils.minIndex(distances);
  }
  



  private Input sIB_ProcessInput()
  {
    double valSum = 0.0D;
    for (int i = 0; i < m_numInstances; i++) {
      valSum = 0.0D;
      for (int v = 0; v < m_data.instance(i).numValues(); v++) {
        valSum += m_data.instance(i).valueSparse(v);
      }
      if (valSum <= 0.0D) {
        if (m_verbose) {
          System.out.format("Instance %s sum of value = %s <= 0, removed.\n", new Object[] { Integer.valueOf(i), Double.valueOf(valSum) });
        }
        m_data.delete(i);
        m_numInstances -= 1;
      }
    }
    

    Input input = new Input(null);
    Py_x = getTransposedNormedMatrix(m_data);
    if (m_uniformPrior) {
      Pyx = Py_x.copy();
      normalizePrior(m_data);
    }
    else {
      Pyx = getTransposedMatrix(m_data);
    }
    sumVals = getTotalSum(m_data);
    Pyx.timesEquals(1.0D / sumVals);
    

    Px = new double[m_numInstances];
    for (int i = 0; i < m_numInstances; i++) {
      for (int j = 0; j < m_numAttributes; j++) {
        Px[i] += Pyx.get(j, i);
      }
    }
    

    Py = new double[m_numAttributes];
    for (int i = 0; i < Pyx.getRowDimension(); i++) {
      for (int j = 0; j < Pyx.getColumnDimension(); j++) {
        Py[i] += Pyx.get(i, j);
      }
    }
    
    MI(Pyx, input);
    return input;
  }
  




  private Partition sIB_InitT(Input input)
  {
    Partition T = new Partition();
    int avgSize = (int)Math.ceil(m_numInstances / m_numCluster);
    
    ArrayList<Integer> permInstsIdx = new ArrayList();
    ArrayList<Integer> unassigned = new ArrayList();
    for (int i = 0; i < m_numInstances; i++) {
      unassigned.add(Integer.valueOf(i));
    }
    while (unassigned.size() != 0) {
      int t = random.nextInt(unassigned.size());
      permInstsIdx.add(unassigned.get(t));
      unassigned.remove(t);
    }
    
    for (int i = 0; i < m_numCluster; i++) {
      int r2 = avgSize > permInstsIdx.size() ? permInstsIdx.size() : avgSize;
      for (int j = 0; j < r2; j++) {
        Pt_x[((Integer)permInstsIdx.get(j)).intValue()] = i;
      }
      for (int j = 0; j < r2; j++) {
        permInstsIdx.remove(0);
      }
    }
    


    for (int i = 0; i < m_numCluster; i++) {
      ArrayList<Integer> indices = T.find(i);
      for (int j = 0; j < indices.size(); j++) {
        Pt[i] += Px[((Integer)indices.get(j)).intValue()];
      }
      double[][] mArray = Pyx.getArray();
      for (int j = 0; j < m_numAttributes; j++) {
        double sum = 0.0D;
        for (int k = 0; k < indices.size(); k++) {
          sum += mArray[j][((Integer)indices.get(k)).intValue()];
        }
        sum /= Pt[i];
        Py_t.set(j, i, sum);
      }
    }
    
    if (m_verbose) {
      System.out.println("Initializing...");
    }
    return T;
  }
  





  private Partition sIB_OptimizeT(Partition tmpT, Input input)
  {
    boolean done = false;
    int change = 0;int loopCounter = 0;
    if (m_verbose) {
      System.out.println("Optimizing...");
      System.out.println("-------------");
    }
    while (!done) {
      change = 0;
      for (int i = 0; i < m_numInstances; i++) {
        int old_t = Pt_x[i];
        
        if (tmpT.size(old_t) == 1) {
          if (m_verbose) {
            System.out.format("cluster %s has only 1 doc remain\n", new Object[] { Integer.valueOf(old_t) });
          }
        }
        else
        {
          reduce_x(i, old_t, tmpT, input);
          

          int new_t = clusterInstance(i, input, tmpT);
          if (new_t != old_t) {
            change++;
            updateAssignment(i, new_t, tmpT, Px[i], Py_x);
          }
        }
      }
      Partition.access$1612(tmpT, change);
      if (m_verbose) {
        System.out.format("iteration %s , changes : %s\n", new Object[] { Integer.valueOf(loopCounter), Integer.valueOf(change) });
      }
      done = checkConvergence(change, loopCounter);
      loopCounter++;
    }
    

    L = sIB_local_MI(Py_t, Pt);
    if (m_verbose) {
      System.out.format("score (L) : %s \n", new Object[] { Utils.doubleToString(L, 4) });
    }
    return tmpT;
  }
  







  private void reduce_x(int instIdx, int t, Partition T, Input input)
  {
    ArrayList<Integer> indices = T.find(t);
    double sum = 0.0D;
    for (int i = 0; i < indices.size(); i++) {
      if (((Integer)indices.get(i)).intValue() != instIdx)
      {
        sum += Px[((Integer)indices.get(i)).intValue()]; }
    }
    Pt[t] = sum;
    
    if (Pt[t] < 0.0D) {
      System.out.format("Warning: probability < 0 (%s)\n", new Object[] { Double.valueOf(Pt[t]) });
      Pt[t] = 0.0D;
    }
    

    double[][] mArray = Pyx.getArray();
    for (int i = 0; i < m_numAttributes; i++) {
      sum = 0.0D;
      for (int j = 0; j < indices.size(); j++) {
        if (((Integer)indices.get(j)).intValue() != instIdx)
        {
          sum += mArray[i][((Integer)indices.get(j)).intValue()]; }
      }
      Py_t.set(i, t, sum / Pt[t]);
    }
  }
  






  private void updateAssignment(int instIdx, int newt, Partition T, double Px, Matrix Py_x)
  {
    Pt_x[instIdx] = newt;
    

    double mass = Px + Pt[newt];
    double pi1 = Px / mass;
    double pi2 = Pt[newt] / mass;
    for (int i = 0; i < m_numAttributes; i++) {
      Py_t.set(i, newt, pi1 * Py_x.get(i, instIdx) + pi2 * Py_t.get(i, newt));
    }
    
    Pt[newt] = mass;
  }
  





  private boolean checkConvergence(int change, int loops)
  {
    if ((change <= m_minChange) || (loops >= m_maxLoop)) {
      if (m_verbose) {
        System.out.format("\nsIB converged after %s iterations with %s changes\n", new Object[] { Integer.valueOf(loops), Integer.valueOf(change) });
      }
      
      return true;
    }
    return false;
  }
  






  private int clusterInstance(int instIdx, Input input, Partition T)
  {
    double[] distances = new double[m_numCluster];
    for (int i = 0; i < m_numCluster; i++) {
      double Pnew = Px[instIdx] + Pt[i];
      double pi1 = Px[instIdx] / Pnew;
      double pi2 = Pt[i] / Pnew;
      distances[i] = (Pnew * JS(instIdx, input, T, i, pi1, pi2));
    }
    return Utils.minIndex(distances);
  }
  









  private double JS(int instIdx, Input input, Partition T, int t, double pi1, double pi2)
  {
    if (Math.min(pi1, pi2) <= 0.0D) {
      System.out.format("Warning: zero or negative weights in JS calculation! (pi1 %s, pi2 %s)\n", new Object[] { Double.valueOf(pi1), Double.valueOf(pi2) });
      return 0.0D;
    }
    Instance inst = m_data.instance(instIdx);
    double kl1 = 0.0D;double kl2 = 0.0D;double tmp = 0.0D;
    for (int i = 0; i < inst.numValues(); i++) {
      tmp = Py_x.get(inst.index(i), instIdx);
      if (tmp != 0.0D) {
        kl1 += tmp * Math.log(tmp / (tmp * pi1 + pi2 * Py_t.get(inst.index(i), t)));
      }
    }
    for (int i = 0; i < m_numAttributes; i++) {
      if ((tmp = Py_t.get(i, t)) != 0.0D) {
        kl2 += tmp * Math.log(tmp / (Py_x.get(i, instIdx) * pi1 + pi2 * tmp));
      }
    }
    return pi1 * kl1 + pi2 * kl2;
  }
  







  private double JS(Instance inst, int t, double pi1, double pi2)
  {
    if (Math.min(pi1, pi2) <= 0.0D) {
      System.out.format("Warning: zero or negative weights in JS calculation! (pi1 %s, pi2 %s)\n", new Object[] { Double.valueOf(pi1), Double.valueOf(pi2) });
      return 0.0D;
    }
    double sum = Utils.sum(inst.toDoubleArray());
    double kl1 = 0.0D;double kl2 = 0.0D;double tmp = 0.0D;
    for (int i = 0; i < inst.numValues(); i++) {
      tmp = inst.valueSparse(i) / sum;
      if (tmp != 0.0D) {
        kl1 += tmp * Math.log(tmp / (tmp * pi1 + pi2 * bestT.Py_t.get(inst.index(i), t)));
      }
    }
    for (int i = 0; i < m_numAttributes; i++) {
      if ((tmp = bestT.Py_t.get(i, t)) != 0.0D) {
        kl2 += tmp * Math.log(tmp / (inst.value(i) * pi1 / sum + pi2 * tmp));
      }
    }
    return pi1 * kl1 + pi2 * kl2;
  }
  





  private double sIB_local_MI(Matrix m, double[] Pt)
  {
    double Hy = 0.0D;double Ht = 0.0D;
    for (int i = 0; i < Pt.length; i++) {
      Ht += Pt[i] * Math.log(Pt[i]);
    }
    Ht = -Ht;
    
    for (int i = 0; i < m_numAttributes; i++) {
      double Py = 0.0D;
      for (int j = 0; j < m_numCluster; j++) {
        Py += m.get(i, j) * Pt[j];
      }
      if (Py != 0.0D)
        Hy += Py * Math.log(Py);
    }
    Hy = -Hy;
    
    double Hyt = 0.0D;double tmp = 0.0D;
    for (int i = 0; i < m.getRowDimension(); i++) {
      for (int j = 0; j < m.getColumnDimension(); j++)
        if (((tmp = m.get(i, j)) != 0.0D) && (Pt[j] != 0.0D))
        {

          tmp *= Pt[j];
          Hyt += tmp * Math.log(tmp);
        }
    }
    return Hy + Ht + Hyt;
  }
  




  private double getTotalSum(Instances data)
  {
    double sum = 0.0D;
    for (int i = 0; i < data.numInstances(); i++) {
      for (int v = 0; v < data.instance(i).numValues(); v++) {
        sum += data.instance(i).valueSparse(v);
      }
    }
    return sum;
  }
  




  private Matrix getTransposedMatrix(Instances data)
  {
    double[][] temp = new double[data.numAttributes()][data.numInstances()];
    for (int i = 0; i < data.numInstances(); i++) {
      Instance inst = data.instance(i);
      for (int v = 0; v < inst.numValues(); v++) {
        temp[inst.index(v)][i] = inst.valueSparse(v);
      }
    }
    Matrix My_x = new Matrix(temp);
    return My_x;
  }
  



  private void normalizePrior(Instances data)
  {
    for (int i = 0; i < data.numInstances(); i++) {
      normalizeInstance(data.instance(i));
    }
  }
  




  private Instance normalizeInstance(Instance inst)
  {
    double[] vals = inst.toDoubleArray();
    double sum = Utils.sum(vals);
    for (int i = 0; i < vals.length; i++) {
      vals[i] /= sum;
    }
    return new Instance(inst.weight(), vals);
  }
  
  private Matrix getTransposedNormedMatrix(Instances data) {
    Matrix matrix = new Matrix(data.numAttributes(), data.numInstances());
    for (int i = 0; i < data.numInstances(); i++) {
      double[] vals = data.instance(i).toDoubleArray();
      double sum = Utils.sum(vals);
      for (int v = 0; v < vals.length; v++) {
        vals[v] /= sum;
        matrix.set(v, i, vals[v]);
      }
    }
    return matrix;
  }
  




  private void MI(Matrix m, Input input)
  {
    int minDimSize = m.getColumnDimension() < m.getRowDimension() ? m.getColumnDimension() : m.getRowDimension();
    if (minDimSize < 2) {
      System.err.println("Warning : This is not a JOINT distribution");
      Hx = Entropy(m);
      Hy = 0.0D;
      Ixy = 0.0D;
      return;
    }
    
    Hx = Entropy(Px);
    Hy = Entropy(Py);
    
    double entropy = Hx + Hy;
    for (int i = 0; i < m_numInstances; i++) {
      Instance inst = m_data.instance(i);
      for (int v = 0; v < inst.numValues(); v++) {
        double tmp = m.get(inst.index(v), i);
        if (tmp > 0.0D)
          entropy += tmp * Math.log(tmp);
      }
    }
    Ixy = entropy;
    if (m_verbose) {
      System.out.println("Ixy = " + Ixy);
    }
  }
  




  private double Entropy(double[] probs)
  {
    for (int i = 0; i < probs.length; i++) {
      if (probs[i] <= 0.0D) {
        if (m_verbose) {
          System.out.println("Warning: Negative probability.");
        }
        return NaN.0D;
      }
    }
    
    if (Math.abs(Utils.sum(probs) - 1.0D) >= 1.0E-6D) {
      if (m_verbose) {
        System.out.println("Warning: Not normalized.");
      }
      return NaN.0D;
    }
    
    double mi = 0.0D;
    for (int i = 0; i < probs.length; i++) {
      mi += probs[i] * Math.log(probs[i]);
    }
    mi = -mi;
    return mi;
  }
  




  private double Entropy(Matrix p)
  {
    double mi = 0.0D;
    for (int i = 0; i < p.getRowDimension(); i++) {
      for (int j = 0; j < p.getColumnDimension(); j++) {
        if (p.get(i, j) != 0.0D)
        {

          mi += p.get(i, j) + Math.log(p.get(i, j)); }
      }
    }
    mi = -mi;
    return mi;
  }
  





































  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('I', options);
    if (optionString.length() != 0) {
      setMaxIterations(Integer.parseInt(optionString));
    }
    optionString = Utils.getOption('M', options);
    if (optionString.length() != 0) {
      setMinChange(new Integer(optionString).intValue());
    }
    optionString = Utils.getOption('N', options);
    if (optionString.length() != 0) {
      setNumClusters(Integer.parseInt(optionString));
    }
    optionString = Utils.getOption('R', options);
    if (optionString.length() != 0) {
      setNumRestarts(new Integer(optionString).intValue());
    }
    setNotUnifyNorm(Utils.getFlag('U', options));
    setDebug(Utils.getFlag('V', options));
    
    super.setOptions(options);
  }
  



  public Enumeration listOptions()
  {
    Vector<Option> result = new Vector();
    result.addElement(new Option("\tmaximum number of iterations\n\t(default 100).", "I", 1, "-I <num>"));
    
    result.addElement(new Option("\tminimum number of changes in a single iteration\n\t(default 0).", "M", 1, "-M <num>"));
    

    result.addElement(new Option("\tnumber of clusters.\n\t(default 2).", "N", 1, "-N <num>"));
    
    result.addElement(new Option("\tnumber of restarts.\n\t(default 5).", "R", 1, "-R <num>"));
    
    result.addElement(new Option("\tset not to normalize the data\n\t(default true).", "U", 0, "-U"));
    
    result.addElement(new Option("\tset to output debug info\n\t(default false).", "V", 0, "-V"));
    

    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement((Option)en.nextElement());
    }
    return result.elements();
  }
  




  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    result.add("-I");
    result.add("" + getMaxIterations());
    result.add("-M");
    result.add("" + getMinChange());
    result.add("-N");
    result.add("" + getNumClusters());
    result.add("-R");
    result.add("" + getNumRestarts());
    if (getNotUnifyNorm()) {
      result.add("-U");
    }
    if (getDebug()) {
      result.add("-V");
    }
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String debugTipText()
  {
    return "If set to true, clusterer may output additional info to the console.";
  }
  




  public void setDebug(boolean v)
  {
    m_verbose = v;
  }
  



  public boolean getDebug()
  {
    return m_verbose;
  }
  



  public String maxIterationsTipText()
  {
    return "set maximum number of iterations (default 100)";
  }
  



  public void setMaxIterations(int i)
  {
    m_maxLoop = i;
  }
  



  public int getMaxIterations()
  {
    return m_maxLoop;
  }
  



  public String minChangeTipText()
  {
    return "set minimum number of changes (default 0)";
  }
  



  public void setMinChange(int m)
  {
    m_minChange = m;
  }
  



  public int getMinChange()
  {
    return m_minChange;
  }
  



  public String numClustersTipText()
  {
    return "set number of clusters (default 2)";
  }
  



  public void setNumClusters(int n)
  {
    m_numCluster = n;
  }
  



  public int getNumClusters()
  {
    return m_numCluster;
  }
  



  public int numberOfClusters()
  {
    return m_numCluster;
  }
  



  public String numRestartsTipText()
  {
    return "set number of restarts (default 5)";
  }
  



  public void setNumRestarts(int i)
  {
    m_numRestarts = i;
  }
  



  public int getNumRestarts()
  {
    return m_numRestarts;
  }
  



  public String notUnifyNormTipText()
  {
    return "set whether to normalize each instance to a unify prior probability (eg. 1).";
  }
  




  public void setNotUnifyNorm(boolean b)
  {
    m_uniformPrior = (!b);
  }
  




  public boolean getNotUnifyNorm()
  {
    return !m_uniformPrior;
  }
  




  public String globalInfo()
  {
    return "Cluster data using the sequential information bottleneck algorithm.\n\nNote: only hard clustering scheme is supported. sIB assign for each instance the cluster that have the minimum cost/distance to the instance. The trade-off beta is set to infinite so 1/beta is zero.\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Noam Slonim and Nir Friedman and Naftali Tishby");
    result.setValue(TechnicalInformation.Field.YEAR, "2002");
    result.setValue(TechnicalInformation.Field.TITLE, "Unsupervised document classification using sequential information maximization");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the 25th International ACM SIGIR Conference on Research and Development in Information Retrieval");
    result.setValue(TechnicalInformation.Field.PAGES, "129-136");
    
    return result;
  }
  



  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    result.enable(Capabilities.Capability.NO_CLASS);
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    return result;
  }
  
  public String toString() {
    StringBuffer text = new StringBuffer();
    text.append("\nsIB\n===\n");
    text.append("\nNumber of clusters: " + m_numCluster + "\n");
    
    for (int j = 0; j < m_numCluster; j++) {
      text.append("\nCluster: " + j + " Size : " + bestT.size(j) + " Prior probability: " + Utils.doubleToString(bestT.Pt[j], 4) + "\n\n");
      
      for (int i = 0; i < m_numAttributes; i++) {
        text.append("Attribute: " + m_data.attribute(i).name() + "\n");
        text.append("Probability given the cluster = " + Utils.doubleToString(bestT.Py_t.get(i, j), 4) + "\n");
      }
    }
    

    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5538 $");
  }
  
  public static void main(String[] argv) {
    runClusterer(new sIB(), argv);
  }
}

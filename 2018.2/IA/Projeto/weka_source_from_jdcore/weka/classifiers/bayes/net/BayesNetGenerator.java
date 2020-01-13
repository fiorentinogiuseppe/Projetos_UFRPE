package weka.classifiers.bayes.net;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.bayes.net.estimate.BayesNetEstimator;
import weka.classifiers.bayes.net.estimate.DiscreteEstimatorBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.estimators.Estimator;




































































public class BayesNetGenerator
  extends EditableBayesNet
{
  int m_nSeed = 1;
  



  Random random;
  


  static final long serialVersionUID = -7462571170596157720L;
  



  public BayesNetGenerator() {}
  



  public void generateRandomNetwork()
    throws Exception
  {
    if (m_otherBayesNet == null)
    {
      Init(m_nNrOfNodes, m_nCardinality);
      generateRandomNetworkStructure(m_nNrOfNodes, m_nNrOfArcs);
      generateRandomDistributions(m_nNrOfNodes, m_nCardinality);
    }
    else {
      m_nNrOfNodes = m_otherBayesNet.getNrOfNodes();
      m_ParentSets = m_otherBayesNet.getParentSets();
      m_Distributions = m_otherBayesNet.getDistributions();
      

      random = new Random(m_nSeed);
      
      FastVector attInfo = new FastVector(m_nNrOfNodes);
      

      for (int iNode = 0; iNode < m_nNrOfNodes; iNode++) {
        int nValues = m_otherBayesNet.getCardinality(iNode);
        FastVector nomStrings = new FastVector(nValues + 1);
        for (int iValue = 0; iValue < nValues; iValue++) {
          nomStrings.addElement(m_otherBayesNet.getNodeValue(iNode, iValue));
        }
        Attribute att = new Attribute(m_otherBayesNet.getNodeName(iNode), nomStrings);
        attInfo.addElement(att);
      }
      
      m_Instances = new Instances(m_otherBayesNet.getName(), attInfo, 100);
      m_Instances.setClassIndex(m_nNrOfNodes - 1);
    }
  }
  




  public void Init(int nNodes, int nValues)
    throws Exception
  {
    random = new Random(m_nSeed);
    
    FastVector attInfo = new FastVector(nNodes);
    
    FastVector nomStrings = new FastVector(nValues + 1);
    for (int iValue = 0; iValue < nValues; iValue++) {
      nomStrings.addElement("Value" + (iValue + 1));
    }
    
    for (int iNode = 0; iNode < nNodes; iNode++) {
      Attribute att = new Attribute("Node" + (iNode + 1), nomStrings);
      attInfo.addElement(att);
    }
    m_Instances = new Instances("RandomNet", attInfo, 100);
    m_Instances.setClassIndex(nNodes - 1);
    setUseADTree(false);
    

    initStructure();
    

    m_Distributions = new Estimator[nNodes][1];
    for (int iNode = 0; iNode < nNodes; iNode++) {
      m_Distributions[iNode][0] = new DiscreteEstimatorBayes(nValues, getEstimator().getAlpha());
    }
    
    m_nEvidence = new FastVector(nNodes);
    for (int i = 0; i < nNodes; i++) {
      m_nEvidence.addElement(Integer.valueOf(-1));
    }
    m_fMarginP = new FastVector(nNodes);
    for (int i = 0; i < nNodes; i++) {
      double[] P = new double[getCardinality(i)];
      m_fMarginP.addElement(P);
    }
    
    m_nPositionX = new FastVector(nNodes);
    m_nPositionY = new FastVector(nNodes);
    for (int iNode = 0; iNode < nNodes; iNode++) {
      m_nPositionX.addElement(Integer.valueOf(iNode % 10 * 50));
      m_nPositionY.addElement(Integer.valueOf(iNode / 10 * 50));
    }
  }
  






  public void generateRandomNetworkStructure(int nNodes, int nArcs)
    throws Exception
  {
    if (nArcs < nNodes - 1) {
      throw new Exception("Number of arcs should be at least (nNodes - 1) = " + (nNodes - 1) + " instead of " + nArcs);
    }
    if (nArcs > nNodes * (nNodes - 1) / 2) {
      throw new Exception("Number of arcs should be at most nNodes * (nNodes - 1) / 2 = " + nNodes * (nNodes - 1) / 2 + " instead of " + nArcs);
    }
    if (nArcs == 0) { return;
    }
    
    generateTree(nNodes);
    



    for (int iArc = nNodes - 1; iArc < nArcs; iArc++) {
      boolean bDone = false;
      while (!bDone) {
        int nNode1 = random.nextInt(nNodes);
        int nNode2 = random.nextInt(nNodes);
        if (nNode1 == nNode2) nNode2 = (nNode1 + 1) % nNodes;
        if (nNode2 < nNode1) { int h = nNode1;nNode1 = nNode2;nNode2 = h; }
        if (!m_ParentSets[nNode2].contains(nNode1)) {
          m_ParentSets[nNode2].addParent(nNode1, m_Instances);
          bDone = true;
        }
      }
    }
  }
  








  void generateTree(int nNodes)
  {
    boolean[] bConnected = new boolean[nNodes];
    
    int nNode1 = random.nextInt(nNodes);
    int nNode2 = random.nextInt(nNodes);
    if (nNode1 == nNode2) nNode2 = (nNode1 + 1) % nNodes;
    if (nNode2 < nNode1) { int h = nNode1;nNode1 = nNode2;nNode2 = h; }
    m_ParentSets[nNode2].addParent(nNode1, m_Instances);
    bConnected[nNode1] = true;
    bConnected[nNode2] = true;
    



    for (int iArc = 2; iArc < nNodes; iArc++) {
      int nNode = random.nextInt(nNodes);
      nNode1 = 0;
      while (nNode >= 0) {
        nNode1 = (nNode1 + 1) % nNodes;
        while (bConnected[nNode1] == 0) {
          nNode1 = (nNode1 + 1) % nNodes;
        }
        nNode--;
      }
      nNode = random.nextInt(nNodes);
      nNode2 = 0;
      while (nNode >= 0) {
        nNode2 = (nNode2 + 1) % nNodes;
        while (bConnected[nNode2] != 0) {
          nNode2 = (nNode2 + 1) % nNodes;
        }
        nNode--;
      }
      if (nNode2 < nNode1) { int h = nNode1;nNode1 = nNode2;nNode2 = h; }
      m_ParentSets[nNode2].addParent(nNode1, m_Instances);
      bConnected[nNode1] = true;
      bConnected[nNode2] = true;
    }
  }
  






  void generateRandomDistributions(int nNodes, int nValues)
  {
    int nMaxParentCardinality = 1;
    for (int iAttribute = 0; iAttribute < nNodes; iAttribute++) {
      if (m_ParentSets[iAttribute].getCardinalityOfParents() > nMaxParentCardinality) {
        nMaxParentCardinality = m_ParentSets[iAttribute].getCardinalityOfParents();
      }
    }
    

    m_Distributions = new Estimator[m_Instances.numAttributes()][nMaxParentCardinality];
    

    for (int iAttribute = 0; iAttribute < nNodes; iAttribute++) {
      int[] nPs = new int[nValues + 1];
      nPs[0] = 0;
      nPs[nValues] = 1000;
      for (int iParent = 0; iParent < m_ParentSets[iAttribute].getCardinalityOfParents(); iParent++)
      {
        for (int iValue = 1; iValue < nValues; iValue++) {
          nPs[iValue] = random.nextInt(1000);
        }
        
        for (int iValue = 1; iValue < nValues; iValue++) {
          for (int iValue2 = iValue + 1; iValue2 < nValues; iValue2++) {
            if (nPs[iValue2] < nPs[iValue]) {
              int h = nPs[iValue2];nPs[iValue2] = nPs[iValue];nPs[iValue] = h;
            }
          }
        }
        
        DiscreteEstimatorBayes d = new DiscreteEstimatorBayes(nValues, getEstimator().getAlpha());
        for (int iValue = 0; iValue < nValues; iValue++) {
          d.addValue(iValue, nPs[(iValue + 1)] - nPs[iValue]);
        }
        m_Distributions[iAttribute][iParent] = d;
      }
    }
  }
  





  public void generateInstances()
    throws Exception
  {
    int[] order = getOrder();
    for (int iInstance = 0; iInstance < m_nNrOfInstances; iInstance++) {
      int nNrOfAtts = m_Instances.numAttributes();
      Instance instance = new Instance(nNrOfAtts);
      instance.setDataset(m_Instances);
      for (int iAtt2 = 0; iAtt2 < nNrOfAtts; iAtt2++) {
        int iAtt = order[iAtt2];
        
        double iCPT = 0.0D;
        
        for (int iParent = 0; iParent < m_ParentSets[iAtt].getNrOfParents(); iParent++) {
          int nParent = m_ParentSets[iAtt].getParent(iParent);
          iCPT = iCPT * m_Instances.attribute(nParent).numValues() + instance.value(nParent);
        }
        
        double fRandom = random.nextInt(1000) / 1000.0F;
        int iValue = 0;
        while (fRandom > m_Distributions[iAtt][((int)iCPT)].getProbability(iValue)) {
          fRandom -= m_Distributions[iAtt][((int)iCPT)].getProbability(iValue);
          iValue++;
        }
        instance.setValue(iAtt, iValue);
      }
      m_Instances.add(instance);
    }
  }
  

  int[] getOrder()
    throws Exception
  {
    int nNrOfAtts = m_Instances.numAttributes();
    int[] order = new int[nNrOfAtts];
    boolean[] bDone = new boolean[nNrOfAtts];
    for (int iAtt = 0; iAtt < nNrOfAtts; iAtt++) {
      int iAtt2 = 0;
      boolean allParentsDone = false;
      while ((!allParentsDone) && (iAtt2 < nNrOfAtts)) {
        if (bDone[iAtt2] == 0) {
          allParentsDone = true;
          int iParent = 0;
          while ((allParentsDone) && (iParent < m_ParentSets[iAtt2].getNrOfParents())) {
            allParentsDone = bDone[m_ParentSets[iAtt].getParent(iParent++)];
          }
          if ((allParentsDone) && (iParent == m_ParentSets[iAtt2].getNrOfParents())) {
            order[iAtt] = iAtt2;
            bDone[iAtt2] = true;
          } else {
            iAtt2++;
          }
        } else {
          iAtt2++;
        }
      }
      if ((!allParentsDone) && (iAtt2 == nNrOfAtts)) {
        throw new Exception("There appears to be a cycle in the graph");
      }
    }
    return order;
  }
  




  public String toString()
  {
    if (m_bGenerateNet) {
      return toXMLBIF03();
    }
    return m_Instances.toString();
  }
  

  boolean m_bGenerateNet = false;
  int m_nNrOfNodes = 10;
  int m_nNrOfArcs = 10;
  int m_nNrOfInstances = 10;
  int m_nCardinality = 2;
  String m_sBIFFile = "";
  
  void setNrOfNodes(int nNrOfNodes) { m_nNrOfNodes = nNrOfNodes; }
  void setNrOfArcs(int nNrOfArcs) { m_nNrOfArcs = nNrOfArcs; }
  void setNrOfInstances(int nNrOfInstances) { m_nNrOfInstances = nNrOfInstances; }
  void setCardinality(int nCardinality) { m_nCardinality = nCardinality; }
  void setSeed(int nSeed) { m_nSeed = nSeed; }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(6);
    
    newVector.addElement(new Option("\tGenerate network (instead of instances)\n", "B", 0, "-B"));
    newVector.addElement(new Option("\tNr of nodes\n", "N", 1, "-N <integer>"));
    newVector.addElement(new Option("\tNr of arcs\n", "A", 1, "-A <integer>"));
    newVector.addElement(new Option("\tNr of instances\n", "M", 1, "-M <integer>"));
    newVector.addElement(new Option("\tCardinality of the variables\n", "C", 1, "-C <integer>"));
    newVector.addElement(new Option("\tSeed for random number generator\n", "S", 1, "-S <integer>"));
    newVector.addElement(new Option("\tThe BIF file to obtain the structure from.\n", "F", 1, "-F <file>"));
    
    return newVector.elements();
  }
  





































  public void setOptions(String[] options)
    throws Exception
  {
    m_bGenerateNet = Utils.getFlag('B', options);
    
    String sNrOfNodes = Utils.getOption('N', options);
    if (sNrOfNodes.length() != 0) {
      setNrOfNodes(Integer.parseInt(sNrOfNodes));
    } else {
      setNrOfNodes(10);
    }
    
    String sNrOfArcs = Utils.getOption('A', options);
    if (sNrOfArcs.length() != 0) {
      setNrOfArcs(Integer.parseInt(sNrOfArcs));
    } else {
      setNrOfArcs(10);
    }
    
    String sNrOfInstances = Utils.getOption('M', options);
    if (sNrOfInstances.length() != 0) {
      setNrOfInstances(Integer.parseInt(sNrOfInstances));
    } else {
      setNrOfInstances(10);
    }
    
    String sCardinality = Utils.getOption('C', options);
    if (sCardinality.length() != 0) {
      setCardinality(Integer.parseInt(sCardinality));
    } else {
      setCardinality(2);
    }
    
    String sSeed = Utils.getOption('S', options);
    if (sSeed.length() != 0) {
      setSeed(Integer.parseInt(sSeed));
    } else {
      setSeed(1);
    }
    
    String sBIFFile = Utils.getOption('F', options);
    if ((sBIFFile != null) && (sBIFFile != "")) {
      setBIFFile(sBIFFile);
    }
  }
  




  public String[] getOptions()
  {
    String[] options = new String[13];
    int current = 0;
    if (m_bGenerateNet) {
      options[(current++)] = "-B";
    }
    
    options[(current++)] = "-N";
    options[(current++)] = ("" + m_nNrOfNodes);
    
    options[(current++)] = "-A";
    options[(current++)] = ("" + m_nNrOfArcs);
    
    options[(current++)] = "-M";
    options[(current++)] = ("" + m_nNrOfInstances);
    
    options[(current++)] = "-C";
    options[(current++)] = ("" + m_nCardinality);
    
    options[(current++)] = "-S";
    options[(current++)] = ("" + m_nSeed);
    
    if (m_sBIFFile.length() != 0) {
      options[(current++)] = "-F";
      options[(current++)] = ("" + m_sBIFFile);
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  


  protected static void printOptions(OptionHandler o)
  {
    Enumeration enm = o.listOptions();
    
    System.out.println("Options for " + o.getClass().getName() + ":\n");
    
    while (enm.hasMoreElements()) {
      Option option = (Option)enm.nextElement();
      System.out.println(option.synopsis());
      System.out.println(option.description());
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.14 $");
  }
  




  public static void main(String[] args)
  {
    BayesNetGenerator b = new BayesNetGenerator();
    try {
      if ((args.length == 0) || (Utils.getFlag('h', args))) {
        printOptions(b);
        return;
      }
      b.setOptions(args);
      
      b.generateRandomNetwork();
      if (!m_bGenerateNet) {
        b.generateInstances();
      }
      System.out.println(b.toString());
    } catch (Exception e) {
      e.printStackTrace();
      printOptions(b);
    }
  }
}

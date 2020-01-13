package weka.classifiers.bayes.net;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;
import weka.classifiers.bayes.BayesNet;

public class MarginCalculator implements java.io.Serializable, weka.core.RevisionHandler
{
  private static final long serialVersionUID = 650278019241175534L;
  boolean m_debug;
  public JunctionTreeNode m_root;
  JunctionTreeNode[] jtNodes;
  double[][] m_Margins;
  
  public MarginCalculator()
  {
    m_debug = false;
    m_root = null;
  }
  
  public int getNode(String sNodeName) {
    int iNode = 0;
    while (iNode < m_root.m_bayesNet.m_Instances.numAttributes()) {
      if (m_root.m_bayesNet.m_Instances.attribute(iNode).name().equals(sNodeName)) {
        return iNode;
      }
      iNode++;
    }
    
    return -1; }
  
  public String toXMLBIF03() { return m_root.m_bayesNet.toXMLBIF03(); }
  






  public void calcMargins(BayesNet bayesNet)
    throws Exception
  {
    boolean[][] bAdjacencyMatrix = moralize(bayesNet);
    process(bAdjacencyMatrix, bayesNet);
  }
  
  public void calcFullMargins(BayesNet bayesNet) throws Exception
  {
    int nNodes = bayesNet.getNrOfNodes();
    boolean[][] bAdjacencyMatrix = new boolean[nNodes][nNodes];
    for (int iNode = 0; iNode < nNodes; iNode++) {
      for (int iNode2 = 0; iNode2 < nNodes; iNode2++) {
        bAdjacencyMatrix[iNode][iNode2] = 1;
      }
    }
    process(bAdjacencyMatrix, bayesNet);
  }
  
  public void process(boolean[][] bAdjacencyMatrix, BayesNet bayesNet) throws Exception
  {
    int[] order = getMaxCardOrder(bAdjacencyMatrix);
    bAdjacencyMatrix = fillIn(order, bAdjacencyMatrix);
    order = getMaxCardOrder(bAdjacencyMatrix);
    Set[] cliques = getCliques(order, bAdjacencyMatrix);
    Set[] separators = getSeparators(order, cliques);
    int[] parentCliques = getCliqueTree(order, cliques, separators);
    
    int nNodes = bAdjacencyMatrix.length;
    if (m_debug) {
      for (int i = 0; i < nNodes; i++) {
        int iNode = order[i];
        if (cliques[iNode] != null) {
          System.out.print("Clique " + iNode + " (");
          Iterator nodes = cliques[iNode].iterator();
          while (nodes.hasNext()) {
            int iNode2 = ((Integer)nodes.next()).intValue();
            System.out.print(iNode2 + " " + bayesNet.getNodeName(iNode2));
            if (nodes.hasNext()) {
              System.out.print(",");
            }
          }
          System.out.print(") S(");
          nodes = separators[iNode].iterator();
          while (nodes.hasNext()) {
            int iNode2 = ((Integer)nodes.next()).intValue();
            System.out.print(iNode2 + " " + bayesNet.getNodeName(iNode2));
            if (nodes.hasNext()) {
              System.out.print(",");
            }
          }
          System.out.println(") parent clique " + parentCliques[iNode]);
        }
      }
    }
    
    jtNodes = getJunctionTree(cliques, separators, parentCliques, order, bayesNet);
    m_root = null;
    for (int iNode = 0; iNode < nNodes; iNode++) {
      if ((parentCliques[iNode] < 0) && (jtNodes[iNode] != null)) {
        m_root = jtNodes[iNode];
        break;
      }
    }
    m_Margins = new double[nNodes][];
    initialize(jtNodes, order, cliques, separators, parentCliques);
    

    for (int i = 0; i < nNodes; i++) {
      int iNode = order[i];
      if ((cliques[iNode] != null) && 
        (parentCliques[iNode] == -1) && (separators[iNode].size() > 0)) {
        throw new Exception("Something wrong in clique tree");
      }
    }
    
    if (m_debug) {}
  }
  

  void initialize(JunctionTreeNode[] jtNodes, int[] order, Set[] cliques, Set[] separators, int[] parentCliques)
  {
    int nNodes = order.length;
    for (int i = nNodes - 1; i >= 0; i--) {
      int iNode = order[i];
      if (jtNodes[iNode] != null) {
        jtNodes[iNode].initializeUp();
      }
    }
    for (int i = 0; i < nNodes; i++) {
      int iNode = order[i];
      if (jtNodes[iNode] != null) {
        jtNodes[iNode].initializeDown(false);
      }
    }
  }
  
  JunctionTreeNode[] getJunctionTree(Set[] cliques, Set[] separators, int[] parentCliques, int[] order, BayesNet bayesNet) {
    int nNodes = order.length;
    JunctionTreeNode root = null;
    JunctionTreeNode[] jtns = new JunctionTreeNode[nNodes];
    boolean[] bDone = new boolean[nNodes];
    
    for (int i = 0; i < nNodes; i++) {
      int iNode = order[i];
      if (cliques[iNode] != null) {
        jtns[iNode] = new JunctionTreeNode(cliques[iNode], bayesNet, bDone);
      }
    }
    
    for (int i = 0; i < nNodes; i++) {
      int iNode = order[i];
      if (cliques[iNode] != null) {
        JunctionTreeNode parent = null;
        if (parentCliques[iNode] > 0) {
          parent = jtns[parentCliques[iNode]];
          JunctionTreeSeparator jts = new JunctionTreeSeparator(separators[iNode], bayesNet, jtns[iNode], parent);
          jtns[iNode].setParentSeparator(jts);
          jtns[parentCliques[iNode]].addChildClique(jtns[iNode]);
        } else {
          root = jtns[iNode];
        }
      }
    }
    return jtns;
  }
  
  public class JunctionTreeSeparator
    implements java.io.Serializable, weka.core.RevisionHandler
  {
    private static final long serialVersionUID = 6502780192411755343L;
    int[] m_nNodes;
    int m_nCardinality;
    double[] m_fiParent;
    double[] m_fiChild;
    MarginCalculator.JunctionTreeNode m_parentNode;
    MarginCalculator.JunctionTreeNode m_childNode;
    BayesNet m_bayesNet;
    
    JunctionTreeSeparator(Set separator, BayesNet bayesNet, MarginCalculator.JunctionTreeNode childNode, MarginCalculator.JunctionTreeNode parentNode)
    {
      m_nNodes = new int[separator.size()];
      int iPos = 0;
      m_nCardinality = 1;
      for (Iterator nodes = separator.iterator(); nodes.hasNext();) {
        int iNode = ((Integer)nodes.next()).intValue();
        m_nNodes[(iPos++)] = iNode;
        m_nCardinality *= bayesNet.getCardinality(iNode);
      }
      m_parentNode = parentNode;
      m_childNode = childNode;
      m_bayesNet = bayesNet;
    }
    



    public void updateFromParent()
    {
      double[] fis = update(m_parentNode);
      if (fis == null) {
        m_fiParent = null;
      } else {
        m_fiParent = fis;
        
        double sum = 0.0D;
        for (int iPos = 0; iPos < m_nCardinality; iPos++) {
          sum += m_fiParent[iPos];
        }
        for (int iPos = 0; iPos < m_nCardinality; iPos++) {
          m_fiParent[iPos] /= sum;
        }
      }
    }
    



    public void updateFromChild()
    {
      double[] fis = update(m_childNode);
      if (fis == null) {
        m_fiChild = null;
      } else {
        m_fiChild = fis;
        
        double sum = 0.0D;
        for (int iPos = 0; iPos < m_nCardinality; iPos++) {
          sum += m_fiChild[iPos];
        }
        for (int iPos = 0; iPos < m_nCardinality; iPos++) {
          m_fiChild[iPos] /= sum;
        }
      }
    }
    



    public double[] update(MarginCalculator.JunctionTreeNode node)
    {
      if (m_P == null) {
        return null;
      }
      double[] fi = new double[m_nCardinality];
      
      int[] values = new int[m_nNodes.length];
      int[] order = new int[m_bayesNet.getNrOfNodes()];
      for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
        order[m_nNodes[iNode]] = iNode;
      }
      
      for (int iPos = 0; iPos < m_nCardinality; iPos++) {
        int iNodeCPT = getCPT(m_nNodes, m_nNodes.length, values, order, m_bayesNet);
        int iSepCPT = getCPT(m_nNodes, m_nNodes.length, values, order, m_bayesNet);
        fi[iSepCPT] += m_P[iNodeCPT];
        
        int i = 0;
        values[i] += 1;
        while ((i < m_nNodes.length) && (values[i] == m_bayesNet.getCardinality(m_nNodes[i]))) {
          values[i] = 0;
          i++;
          if (i < m_nNodes.length) {
            values[i] += 1;
          }
        }
      }
      return fi;
    }
    




    public String getRevision()
    {
      return weka.core.RevisionUtils.extract("$Revision: 1.2 $");
    }
  }
  


  public class JunctionTreeNode
    implements java.io.Serializable, weka.core.RevisionHandler
  {
    private static final long serialVersionUID = 650278019241175536L;
    

    BayesNet m_bayesNet;
    
    public int[] m_nNodes;
    
    int m_nCardinality;
    
    double[] m_fi;
    
    double[] m_P;
    
    double[][] m_MarginalP;
    MarginCalculator.JunctionTreeSeparator m_parentSeparator;
    public java.util.Vector m_children;
    
    public void setParentSeparator(MarginCalculator.JunctionTreeSeparator parentSeparator) { m_parentSeparator = parentSeparator; }
    
    public void addChildClique(JunctionTreeNode child) { m_children.add(child); }
    
    public void initializeUp() {
      m_P = new double[m_nCardinality];
      for (int iPos = 0; iPos < m_nCardinality; iPos++) {
        m_P[iPos] = m_fi[iPos];
      }
      int[] values = new int[m_nNodes.length];
      int[] order = new int[m_bayesNet.getNrOfNodes()];
      for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
        order[m_nNodes[iNode]] = iNode;
      }
      for (Iterator child = m_children.iterator(); child.hasNext();) {
        JunctionTreeNode childNode = (JunctionTreeNode)child.next();
        MarginCalculator.JunctionTreeSeparator separator = m_parentSeparator;
        
        for (int iPos = 0; iPos < m_nCardinality; iPos++) {
          int iSepCPT = getCPT(m_nNodes, m_nNodes.length, values, order, m_bayesNet);
          int iNodeCPT = getCPT(m_nNodes, m_nNodes.length, values, order, m_bayesNet);
          m_P[iNodeCPT] *= m_fiChild[iSepCPT];
          
          int i = 0;
          values[i] += 1;
          while ((i < m_nNodes.length) && (values[i] == m_bayesNet.getCardinality(m_nNodes[i]))) {
            values[i] = 0;
            i++;
            if (i < m_nNodes.length) {
              values[i] += 1;
            }
          }
        }
      }
      
      double sum = 0.0D;
      for (int iPos = 0; iPos < m_nCardinality; iPos++) {
        sum += m_P[iPos];
      }
      for (int iPos = 0; iPos < m_nCardinality; iPos++) {
        m_P[iPos] /= sum;
      }
      
      if (m_parentSeparator != null) {
        m_parentSeparator.updateFromChild();
      }
    }
    
    public void initializeDown(boolean recursively) {
      if (m_parentSeparator == null) {
        calcMarginalProbabilities();
      } else {
        m_parentSeparator.updateFromParent();
        int[] values = new int[m_nNodes.length];
        int[] order = new int[m_bayesNet.getNrOfNodes()];
        for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
          order[m_nNodes[iNode]] = iNode;
        }
        


        for (int iPos = 0; iPos < m_nCardinality; iPos++) {
          int iSepCPT = getCPT(m_parentSeparator.m_nNodes, m_parentSeparator.m_nNodes.length, values, order, m_bayesNet);
          int iNodeCPT = getCPT(m_nNodes, m_nNodes.length, values, order, m_bayesNet);
          if (m_parentSeparator.m_fiChild[iSepCPT] > 0.0D) {
            m_P[iNodeCPT] *= m_parentSeparator.m_fiParent[iSepCPT] / m_parentSeparator.m_fiChild[iSepCPT];
          } else {
            m_P[iNodeCPT] = 0.0D;
          }
          
          int i = 0;
          values[i] += 1;
          while ((i < m_nNodes.length) && (values[i] == m_bayesNet.getCardinality(m_nNodes[i]))) {
            values[i] = 0;
            i++;
            if (i < m_nNodes.length) {
              values[i] += 1;
            }
          }
        }
        
        double sum = 0.0D;
        for (int iPos = 0; iPos < m_nCardinality; iPos++) {
          sum += m_P[iPos];
        }
        for (int iPos = 0; iPos < m_nCardinality; iPos++) {
          m_P[iPos] /= sum;
        }
        m_parentSeparator.updateFromChild();
        calcMarginalProbabilities(); }
      Iterator child;
      if (recursively) {
        for (child = m_children.iterator(); child.hasNext();) {
          JunctionTreeNode childNode = (JunctionTreeNode)child.next();
          childNode.initializeDown(true);
        }
      }
    }
    




    void calcMarginalProbabilities()
    {
      int[] values = new int[m_nNodes.length];
      int[] order = new int[m_bayesNet.getNrOfNodes()];
      m_MarginalP = new double[m_nNodes.length][];
      for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
        order[m_nNodes[iNode]] = iNode;
        m_MarginalP[iNode] = new double[m_bayesNet.getCardinality(m_nNodes[iNode])];
      }
      for (int iPos = 0; iPos < m_nCardinality; iPos++) {
        int iNodeCPT = getCPT(m_nNodes, m_nNodes.length, values, order, m_bayesNet);
        for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
          m_MarginalP[iNode][values[iNode]] += m_P[iNodeCPT];
        }
        
        int i = 0;
        values[i] += 1;
        while ((i < m_nNodes.length) && (values[i] == m_bayesNet.getCardinality(m_nNodes[i]))) {
          values[i] = 0;
          i++;
          if (i < m_nNodes.length) {
            values[i] += 1;
          }
        }
      }
      
      for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
        m_Margins[m_nNodes[iNode]] = m_MarginalP[iNode];
      }
    }
    
    public String toString() {
      StringBuffer buf = new StringBuffer();
      for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
        buf.append(m_bayesNet.getNodeName(m_nNodes[iNode]) + ": ");
        for (int iValue = 0; iValue < m_MarginalP[iNode].length; iValue++) {
          buf.append(m_MarginalP[iNode][iValue] + " ");
        }
        buf.append('\n');
      }
      for (Iterator child = m_children.iterator(); child.hasNext();) {
        JunctionTreeNode childNode = (JunctionTreeNode)child.next();
        buf.append("----------------\n");
        buf.append(childNode.toString());
      }
      return buf.toString();
    }
    
    void calculatePotentials(BayesNet bayesNet, Set clique, boolean[] bDone) {
      m_fi = new double[m_nCardinality];
      
      int[] values = new int[m_nNodes.length];
      int[] order = new int[bayesNet.getNrOfNodes()];
      for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
        order[m_nNodes[iNode]] = iNode;
      }
      
      boolean[] bIsContained = new boolean[m_nNodes.length];
      for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
        int nNode = m_nNodes[iNode];
        bIsContained[iNode] = (bDone[nNode] == 0 ? 1 : false);
        for (int iParent = 0; iParent < bayesNet.getNrOfParents(nNode); iParent++) {
          int nParent = bayesNet.getParent(nNode, iParent);
          if (!clique.contains(Integer.valueOf(nParent))) {
            bIsContained[iNode] = false;
          }
        }
        if (bIsContained[iNode] != 0) {
          bDone[nNode] = true;
          if (m_debug) {
            System.out.println("adding node " + nNode);
          }
        }
      }
      

      for (int iPos = 0; iPos < m_nCardinality; iPos++) {
        int iCPT = getCPT(m_nNodes, m_nNodes.length, values, order, bayesNet);
        m_fi[iCPT] = 1.0D;
        for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
          if (bIsContained[iNode] != 0) {
            int nNode = m_nNodes[iNode];
            int[] nNodes = bayesNet.getParentSet(nNode).getParents();
            int iCPT2 = getCPT(nNodes, bayesNet.getNrOfParents(nNode), values, order, bayesNet);
            double f = bayesNet.getDistributions()[nNode][iCPT2].getProbability(values[iNode]);
            m_fi[iCPT] *= f;
          }
        }
        

        int i = 0;
        values[i] += 1;
        while ((i < m_nNodes.length) && (values[i] == bayesNet.getCardinality(m_nNodes[i]))) {
          values[i] = 0;
          i++;
          if (i < m_nNodes.length) {
            values[i] += 1;
          }
        }
      }
    }
    
    JunctionTreeNode(Set clique, BayesNet bayesNet, boolean[] bDone) {
      m_bayesNet = bayesNet;
      m_children = new java.util.Vector();
      

      m_nNodes = new int[clique.size()];
      int iPos = 0;
      m_nCardinality = 1;
      for (Iterator nodes = clique.iterator(); nodes.hasNext();) {
        int iNode = ((Integer)nodes.next()).intValue();
        m_nNodes[(iPos++)] = iNode;
        m_nCardinality *= bayesNet.getCardinality(iNode);
      }
      

      calculatePotentials(bayesNet, clique, bDone);
    }
    


    boolean contains(int nNode)
    {
      for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
        if (m_nNodes[iNode] == nNode) {
          return true;
        }
      }
      return false;
    }
    
    public void setEvidence(int nNode, int iValue) throws Exception {
      int[] values = new int[m_nNodes.length];
      int[] order = new int[m_bayesNet.getNrOfNodes()];
      
      int nNodeIdx = -1;
      for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
        order[m_nNodes[iNode]] = iNode;
        if (m_nNodes[iNode] == nNode) {
          nNodeIdx = iNode;
        }
      }
      if (nNodeIdx < 0) {
        throw new Exception("setEvidence: Node " + nNode + " not found in this clique");
      }
      for (int iPos = 0; iPos < m_nCardinality; iPos++) {
        if (values[nNodeIdx] != iValue) {
          int iNodeCPT = getCPT(m_nNodes, m_nNodes.length, values, order, m_bayesNet);
          m_P[iNodeCPT] = 0.0D;
        }
        
        int i = 0;
        values[i] += 1;
        while ((i < m_nNodes.length) && (values[i] == m_bayesNet.getCardinality(m_nNodes[i]))) {
          values[i] = 0;
          i++;
          if (i < m_nNodes.length) {
            values[i] += 1;
          }
        }
      }
      
      double sum = 0.0D;
      for (int iPos = 0; iPos < m_nCardinality; iPos++) {
        sum += m_P[iPos];
      }
      for (int iPos = 0; iPos < m_nCardinality; iPos++) {
        m_P[iPos] /= sum;
      }
      calcMarginalProbabilities();
      updateEvidence(this);
    }
    
    void updateEvidence(JunctionTreeNode source) {
      if (source != this) {
        int[] values = new int[m_nNodes.length];
        int[] order = new int[m_bayesNet.getNrOfNodes()];
        for (int iNode = 0; iNode < m_nNodes.length; iNode++) {
          order[m_nNodes[iNode]] = iNode;
        }
        int[] nChildNodes = m_parentSeparator.m_nNodes;
        int nNumChildNodes = nChildNodes.length;
        for (int iPos = 0; iPos < m_nCardinality; iPos++) {
          int iNodeCPT = getCPT(m_nNodes, m_nNodes.length, values, order, m_bayesNet);
          int iChildCPT = getCPT(nChildNodes, nNumChildNodes, values, order, m_bayesNet);
          if (m_parentSeparator.m_fiParent[iChildCPT] != 0.0D) {
            m_P[iNodeCPT] *= m_parentSeparator.m_fiChild[iChildCPT] / m_parentSeparator.m_fiParent[iChildCPT];
          } else {
            m_P[iNodeCPT] = 0.0D;
          }
          
          int i = 0;
          values[i] += 1;
          while ((i < m_nNodes.length) && (values[i] == m_bayesNet.getCardinality(m_nNodes[i]))) {
            values[i] = 0;
            i++;
            if (i < m_nNodes.length) {
              values[i] += 1;
            }
          }
        }
        
        double sum = 0.0D;
        for (int iPos = 0; iPos < m_nCardinality; iPos++) {
          sum += m_P[iPos];
        }
        for (int iPos = 0; iPos < m_nCardinality; iPos++) {
          m_P[iPos] /= sum;
        }
        calcMarginalProbabilities();
      }
      for (Iterator child = m_children.iterator(); child.hasNext();) {
        JunctionTreeNode childNode = (JunctionTreeNode)child.next();
        if (childNode != source) {
          childNode.initializeDown(true);
        }
      }
      if (m_parentSeparator != null) {
        m_parentSeparator.updateFromChild();
        m_parentSeparator.m_parentNode.updateEvidence(this);
        m_parentSeparator.updateFromParent();
      }
    }
    




    public String getRevision()
    {
      return weka.core.RevisionUtils.extract("$Revision: 1.2 $");
    }
  }
  
  int getCPT(int[] nodeSet, int nNodes, int[] values, int[] order, BayesNet bayesNet)
  {
    int iCPTnew = 0;
    for (int iNode = 0; iNode < nNodes; iNode++) {
      int nNode = nodeSet[iNode];
      iCPTnew *= bayesNet.getCardinality(nNode);
      iCPTnew += values[order[nNode]];
    }
    return iCPTnew;
  }
  
  int[] getCliqueTree(int[] order, Set[] cliques, Set[] separators) {
    int nNodes = order.length;
    int[] parentCliques = new int[nNodes];
    
    for (int i = 0; i < nNodes; i++) {
      int iNode = order[i];
      parentCliques[iNode] = -1;
      if ((cliques[iNode] != null) && (separators[iNode].size() > 0))
      {
        for (int j = 0; j < nNodes; j++) {
          int iNode2 = order[j];
          if ((iNode != iNode2) && (cliques[iNode2] != null) && (cliques[iNode2].containsAll(separators[iNode]))) {
            parentCliques[iNode] = iNode2;
            j = i;
            j = 0;
            j = nNodes;
          }
        }
      }
    }
    
    return parentCliques;
  }
  





  Set[] getSeparators(int[] order, Set[] cliques)
  {
    int nNodes = order.length;
    Set[] separators = new java.util.HashSet[nNodes];
    Set processedNodes = new java.util.HashSet();
    
    for (int i = 0; i < nNodes; i++) {
      int iNode = order[i];
      if (cliques[iNode] != null) {
        Set separator = new java.util.HashSet();
        separator.addAll(cliques[iNode]);
        separator.retainAll(processedNodes);
        separators[iNode] = separator;
        processedNodes.addAll(cliques[iNode]);
      }
    }
    return separators;
  }
  





  Set[] getCliques(int[] order, boolean[][] bAdjacencyMatrix)
    throws Exception
  {
    int nNodes = bAdjacencyMatrix.length;
    Set[] cliques = new java.util.HashSet[nNodes];
    




    for (int i = nNodes - 1; i >= 0; i--) {
      int iNode = order[i];
      if (iNode == 22) {
        int h = 3;
        h++;
      }
      Set clique = new java.util.HashSet();
      clique.add(Integer.valueOf(iNode));
      for (int j = 0; j < i; j++) {
        int iNode2 = order[j];
        if (bAdjacencyMatrix[iNode][iNode2] != 0) {
          clique.add(Integer.valueOf(iNode2));
        }
      }
      





      cliques[iNode] = clique;
    }
    for (int iNode = 0; iNode < nNodes; iNode++) {
      for (int iNode2 = 0; iNode2 < nNodes; iNode2++) {
        if ((iNode != iNode2) && (cliques[iNode] != null) && (cliques[iNode2] != null) && (cliques[iNode].containsAll(cliques[iNode2]))) {
          cliques[iNode2] = null;
        }
      }
    }
    
    if (m_debug) {
      int[] nNodeSet = new int[nNodes];
      for (int iNode = 0; iNode < nNodes; iNode++) {
        if (cliques[iNode] != null) {
          Iterator it = cliques[iNode].iterator();
          int k = 0;
          while (it.hasNext()) {
            nNodeSet[(k++)] = ((Integer)it.next()).intValue();
          }
          for (int i = 0; i < cliques[iNode].size(); i++) {
            for (int j = 0; j < cliques[iNode].size(); j++) {
              if ((i != j) && (bAdjacencyMatrix[nNodeSet[i]][nNodeSet[j]] == 0)) {
                throw new Exception("Non clique" + i + " " + j);
              }
            }
          }
        }
      }
    }
    return cliques;
  }
  








  public boolean[][] moralize(BayesNet bayesNet)
  {
    int nNodes = bayesNet.getNrOfNodes();
    boolean[][] bAdjacencyMatrix = new boolean[nNodes][nNodes];
    for (int iNode = 0; iNode < nNodes; iNode++) {
      ParentSet parents = bayesNet.getParentSets()[iNode];
      moralizeNode(parents, iNode, bAdjacencyMatrix);
    }
    return bAdjacencyMatrix;
  }
  
  private void moralizeNode(ParentSet parents, int iNode, boolean[][] bAdjacencyMatrix) {
    for (int iParent = 0; iParent < parents.getNrOfParents(); iParent++) {
      int nParent = parents.getParent(iParent);
      if ((m_debug) && (bAdjacencyMatrix[iNode][nParent] == 0))
        System.out.println("Insert " + iNode + "--" + nParent);
      bAdjacencyMatrix[iNode][nParent] = 1;
      bAdjacencyMatrix[nParent][iNode] = 1;
      for (int iParent2 = iParent + 1; iParent2 < parents.getNrOfParents(); iParent2++) {
        int nParent2 = parents.getParent(iParent2);
        if ((m_debug) && (bAdjacencyMatrix[nParent2][nParent] == 0))
          System.out.println("Mary " + nParent + "--" + nParent2);
        bAdjacencyMatrix[nParent2][nParent] = 1;
        bAdjacencyMatrix[nParent][nParent2] = 1;
      }
    }
  }
  












  public boolean[][] fillIn(int[] order, boolean[][] bAdjacencyMatrix)
  {
    int nNodes = bAdjacencyMatrix.length;
    int[] inverseOrder = new int[nNodes];
    for (int iNode = 0; iNode < nNodes; iNode++) {
      inverseOrder[order[iNode]] = iNode;
    }
    
    for (int i = nNodes - 1; i >= 0; i--) {
      int iNode = order[i];
      
      for (int j = 0; j < i; j++) {
        int iNode2 = order[j];
        if (bAdjacencyMatrix[iNode][iNode2] != 0) {
          for (int k = j + 1; k < i; k++) {
            int iNode3 = order[k];
            if (bAdjacencyMatrix[iNode][iNode3] != 0)
            {
              if ((m_debug) && ((bAdjacencyMatrix[iNode2][iNode3] == 0) || (bAdjacencyMatrix[iNode3][iNode2] == 0)))
                System.out.println("Fill in " + iNode2 + "--" + iNode3);
              bAdjacencyMatrix[iNode2][iNode3] = 1;
              bAdjacencyMatrix[iNode3][iNode2] = 1;
            }
          }
        }
      }
    }
    return bAdjacencyMatrix;
  }
  










  int[] getMaxCardOrder(boolean[][] bAdjacencyMatrix)
  {
    int nNodes = bAdjacencyMatrix.length;
    int[] order = new int[nNodes];
    if (nNodes == 0) return order;
    boolean[] bDone = new boolean[nNodes];
    
    order[0] = 0;
    bDone[0] = true;
    
    for (int iNode = 1; iNode < nNodes; iNode++) {
      int nMaxCard = -1;
      int iBestNode = -1;
      
      for (int iNode2 = 0; iNode2 < nNodes; iNode2++) {
        if (bDone[iNode2] == 0) {
          int nCard = 0;
          
          for (int iNode3 = 0; iNode3 < nNodes; iNode3++) {
            if ((bAdjacencyMatrix[iNode2][iNode3] != 0) && (bDone[iNode3] != 0)) {
              nCard++;
            }
          }
          if (nCard > nMaxCard) {
            nMaxCard = nCard;
            iBestNode = iNode2;
          }
        }
      }
      order[iNode] = iBestNode;
      bDone[iBestNode] = true;
    }
    return order;
  }
  
  public void setEvidence(int nNode, int iValue) throws Exception {
    if (m_root == null) {
      throw new Exception("Junction tree not initialize yet");
    }
    int iJtNode = 0;
    while ((iJtNode < jtNodes.length) && ((jtNodes[iJtNode] == null) || (!jtNodes[iJtNode].contains(nNode)))) {
      iJtNode++;
    }
    if (jtNodes.length == iJtNode) {
      throw new Exception("Could not find node " + nNode + " in junction tree");
    }
    jtNodes[iJtNode].setEvidence(nNode, iValue);
  }
  
  public String toString() {
    return m_root.toString();
  }
  
  public double[] getMargin(int iNode)
  {
    return m_Margins[iNode];
  }
  




  public String getRevision()
  {
    return weka.core.RevisionUtils.extract("$Revision: 1.2 $");
  }
  
  public static void main(String[] args) {
    try {
      BIFReader bayesNet = new BIFReader();
      bayesNet.processFile(args[0]);
      
      MarginCalculator dc = new MarginCalculator();
      dc.calcMargins(bayesNet);
      int iNode = 2;
      int iValue = 0;
      int iNode2 = 4;
      int iValue2 = 0;
      dc.setEvidence(iNode, iValue);
      dc.setEvidence(iNode2, iValue2);
      System.out.print(dc.toString());
      

      dc.calcFullMargins(bayesNet);
      dc.setEvidence(iNode, iValue);
      dc.setEvidence(iNode2, iValue2);
      System.out.println("==============");
      System.out.print(dc.toString());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}

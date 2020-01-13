package weka.clusterers;

import java.io.PrintStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.CapabilitiesHandler;
import weka.core.DistanceFunction;
import weka.core.Drawable;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;


























































public class HierarchicalClusterer
  extends AbstractClusterer
  implements OptionHandler, CapabilitiesHandler, Drawable
{
  private static final long serialVersionUID = 1L;
  protected boolean m_bDebug;
  protected boolean m_bDistanceIsBranchLength;
  Instances m_instances;
  int m_nNumClusters;
  protected DistanceFunction m_DistanceFunction;
  static final int SINGLE = 0;
  static final int COMPLETE = 1;
  static final int AVERAGE = 2;
  static final int MEAN = 3;
  static final int CENTROID = 4;
  static final int WARD = 5;
  static final int ADJCOMLPETE = 6;
  static final int NEIGHBOR_JOINING = 7;
  
  public void setNumClusters(int nClusters) { m_nNumClusters = Math.max(1, nClusters); }
  public int getNumClusters() { return m_nNumClusters; }
  


  public DistanceFunction getDistanceFunction() { return m_DistanceFunction; }
  public void setDistanceFunction(DistanceFunction distanceFunction) { m_DistanceFunction = distanceFunction; }
  
  class Tuple { double m_fDist;
    int m_iCluster1;
    
    public Tuple(double d, int i, int j, int nSize1, int nSize2) { m_fDist = d;
      m_iCluster1 = i;
      m_iCluster2 = j;
      m_nClusterSize1 = nSize1;
      m_nClusterSize2 = nSize2;
    }
    
    int m_iCluster2;
    int m_nClusterSize1;
    int m_nClusterSize2;
  }
  
  class TupleComparator implements Comparator<HierarchicalClusterer.Tuple> {
    TupleComparator() {}
    
    public int compare(HierarchicalClusterer.Tuple o1, HierarchicalClusterer.Tuple o2) { if (m_fDist < m_fDist)
        return -1;
      if (m_fDist == m_fDist) {
        return 0;
      }
      return 1;
    }
  }
  









  public static final Tag[] TAGS_LINK_TYPE = { new Tag(0, "SINGLE"), new Tag(1, "COMPLETE"), new Tag(2, "AVERAGE"), new Tag(3, "MEAN"), new Tag(4, "CENTROID"), new Tag(5, "WARD"), new Tag(6, "ADJCOMLPETE"), new Tag(7, "NEIGHBOR_JOINING") };
  int m_nLinkType;
  boolean m_bPrintNewick;
  Node[] m_clusters;
  int[] m_nClusterNr;
  
  public HierarchicalClusterer()
  {
    m_bDebug = false;
    

    m_bDistanceIsBranchLength = false;
    




    m_nNumClusters = 2;
    



    m_DistanceFunction = new EuclideanDistance();
    




















































    m_nLinkType = 0;
    
    m_bPrintNewick = true; }
  public boolean getPrintNewick() { return m_bPrintNewick; }
  public void setPrintNewick(boolean bPrintNewick) { m_bPrintNewick = bPrintNewick; }
  
  public void setLinkType(SelectedTag newLinkType) {
    if (newLinkType.getTags() == TAGS_LINK_TYPE) {
      m_nLinkType = newLinkType.getSelectedTag().getID();
    }
  }
  

  public SelectedTag getLinkType() { return new SelectedTag(m_nLinkType, TAGS_LINK_TYPE); }
  
  class Node implements Serializable { Node m_left;
    Node m_right;
    Node m_parent;
    int m_iLeftInstance;
    int m_iRightInstance;
    
    Node() {}
    
    double m_fLeftLength = 0.0D;
    double m_fRightLength = 0.0D;
    double m_fHeight = 0.0D;
    
    public String toString(int attIndex) { NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "US"));
      DecimalFormat myFormatter = (DecimalFormat)nf;
      myFormatter.applyPattern("#.#####");
      
      if (m_left == null) {
        if (m_right == null) {
          return "(" + m_instances.instance(m_iLeftInstance).stringValue(attIndex) + ":" + myFormatter.format(m_fLeftLength) + "," + m_instances.instance(m_iRightInstance).stringValue(attIndex) + ":" + myFormatter.format(m_fRightLength) + ")";
        }
        
        return "(" + m_instances.instance(m_iLeftInstance).stringValue(attIndex) + ":" + myFormatter.format(m_fLeftLength) + "," + m_right.toString(attIndex) + ":" + myFormatter.format(m_fRightLength) + ")";
      }
      

      if (m_right == null) {
        return "(" + m_left.toString(attIndex) + ":" + myFormatter.format(m_fLeftLength) + "," + m_instances.instance(m_iRightInstance).stringValue(attIndex) + ":" + myFormatter.format(m_fRightLength) + ")";
      }
      
      return "(" + m_left.toString(attIndex) + ":" + myFormatter.format(m_fLeftLength) + "," + m_right.toString(attIndex) + ":" + myFormatter.format(m_fRightLength) + ")";
    }
    
    public String toString2(int attIndex)
    {
      NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "US"));
      DecimalFormat myFormatter = (DecimalFormat)nf;
      myFormatter.applyPattern("#.#####");
      
      if (m_left == null) {
        if (m_right == null) {
          return "(" + m_instances.instance(m_iLeftInstance).value(attIndex) + ":" + myFormatter.format(m_fLeftLength) + "," + m_instances.instance(m_iRightInstance).value(attIndex) + ":" + myFormatter.format(m_fRightLength) + ")";
        }
        
        return "(" + m_instances.instance(m_iLeftInstance).value(attIndex) + ":" + myFormatter.format(m_fLeftLength) + "," + m_right.toString2(attIndex) + ":" + myFormatter.format(m_fRightLength) + ")";
      }
      

      if (m_right == null) {
        return "(" + m_left.toString2(attIndex) + ":" + myFormatter.format(m_fLeftLength) + "," + m_instances.instance(m_iRightInstance).value(attIndex) + ":" + myFormatter.format(m_fRightLength) + ")";
      }
      
      return "(" + m_left.toString2(attIndex) + ":" + myFormatter.format(m_fLeftLength) + "," + m_right.toString2(attIndex) + ":" + myFormatter.format(m_fRightLength) + ")";
    }
    
    void setHeight(double fHeight1, double fHeight2)
    {
      m_fHeight = fHeight1;
      if (m_left == null) {
        m_fLeftLength = fHeight1;
      } else {
        m_fLeftLength = (fHeight1 - m_left.m_fHeight);
      }
      if (m_right == null) {
        m_fRightLength = fHeight2;
      } else
        m_fRightLength = (fHeight2 - m_right.m_fHeight);
    }
    
    void setLength(double fLength1, double fLength2) {
      m_fLeftLength = fLength1;
      m_fRightLength = fLength2;
      m_fHeight = fLength1;
      if (m_left != null) {
        m_fHeight += m_left.m_fHeight;
      }
    }
  }
  



  public void buildClusterer(Instances data)
    throws Exception
  {
    m_instances = data;
    int nInstances = m_instances.numInstances();
    if (nInstances == 0) {
      return;
    }
    m_DistanceFunction.setInstances(m_instances);
    

    Vector<Integer>[] nClusterID = new Vector[data.numInstances()];
    for (int i = 0; i < data.numInstances(); i++) {
      nClusterID[i] = new Vector();
      nClusterID[i].add(Integer.valueOf(i));
    }
    
    int nClusters = data.numInstances();
    

    Node[] clusterNodes = new Node[nInstances];
    if (m_nLinkType == 7) {
      neighborJoining(nClusters, nClusterID, clusterNodes);
    } else {
      doLinkClustering(nClusters, nClusterID, clusterNodes);
    }
    


    int iCurrent = 0;
    m_clusters = new Node[m_nNumClusters];
    m_nClusterNr = new int[nInstances];
    for (int i = 0; i < nInstances; i++) {
      if (nClusterID[i].size() > 0) {
        for (int j = 0; j < nClusterID[i].size(); j++) {
          m_nClusterNr[((Integer)nClusterID[i].elementAt(j)).intValue()] = iCurrent;
        }
        m_clusters[iCurrent] = clusterNodes[i];
        iCurrent++;
      }
    }
  }
  







  void neighborJoining(int nClusters, Vector<Integer>[] nClusterID, Node[] clusterNodes)
  {
    int n = m_instances.numInstances();
    
    double[][] fDist = new double[nClusters][nClusters];
    for (int i = 0; i < nClusters; i++) {
      fDist[i][i] = 0.0D;
      for (int j = i + 1; j < nClusters; j++) {
        fDist[i][j] = getDistance0(nClusterID[i], nClusterID[j]);
        fDist[j][i] = fDist[i][j];
      }
    }
    
    double[] fSeparationSums = new double[n];
    double[] fSeparations = new double[n];
    int[] nNextActive = new int[n];
    

    for (int i = 0; i < n; i++) {
      double fSum = 0.0D;
      for (int j = 0; j < n; j++) {
        fSum += fDist[i][j];
      }
      fSeparationSums[i] = fSum;
      fSeparations[i] = (fSum / (nClusters - 2));
      nNextActive[i] = (i + 1);
    }
    
    while (nClusters > 2)
    {
      int iMin1 = -1;
      int iMin2 = -1;
      double fMin = Double.MAX_VALUE;
      if (m_bDebug) {
        for (int i = 0; i < n; i++) {
          if (nClusterID[i].size() > 0) {
            double[] fRow = fDist[i];
            double fSep1 = fSeparations[i];
            for (int j = 0; j < n; j++) {
              if ((nClusterID[j].size() > 0) && (i != j)) {
                double fSep2 = fSeparations[j];
                double fVal = fRow[j] - fSep1 - fSep2;
                
                if (fVal < fMin)
                {
                  iMin1 = i;
                  iMin2 = j;
                  fMin = fVal;
                }
              }
            }
          }
        }
      } else {
        int i = 0;
        while (i < n) {
          double fSep1 = fSeparations[i];
          double[] fRow = fDist[i];
          int j = nNextActive[i];
          while (j < n) {
            double fSep2 = fSeparations[j];
            double fVal = fRow[j] - fSep1 - fSep2;
            if (fVal < fMin)
            {
              iMin1 = i;
              iMin2 = j;
              fMin = fVal;
            }
            j = nNextActive[j];
          }
          i = nNextActive[i];
        }
      }
      
      double fMinDistance = fDist[iMin1][iMin2];
      nClusters--;
      double fSep1 = fSeparations[iMin1];
      double fSep2 = fSeparations[iMin2];
      double fDist1 = 0.5D * fMinDistance + 0.5D * (fSep1 - fSep2);
      double fDist2 = 0.5D * fMinDistance + 0.5D * (fSep2 - fSep1);
      if (nClusters > 2)
      {
        double fNewSeparationSum = 0.0D;
        double fMutualDistance = fDist[iMin1][iMin2];
        double[] fRow1 = fDist[iMin1];
        double[] fRow2 = fDist[iMin2];
        for (int i = 0; i < n; i++) {
          if ((i == iMin1) || (i == iMin2) || (nClusterID[i].size() == 0)) {
            fRow1[i] = 0.0D;
          } else {
            double fVal1 = fRow1[i];
            double fVal2 = fRow2[i];
            double fDistance = (fVal1 + fVal2 - fMutualDistance) / 2.0D;
            fNewSeparationSum += fDistance;
            
            fSeparationSums[i] += fDistance - fVal1 - fVal2;
            fSeparationSums[i] /= (nClusters - 2);
            fRow1[i] = fDistance;
            fDist[i][iMin1] = fDistance;
          }
        }
        fSeparationSums[iMin1] = fNewSeparationSum;
        fSeparations[iMin1] = (fNewSeparationSum / (nClusters - 2));
        fSeparationSums[iMin2] = 0.0D;
        merge(iMin1, iMin2, fDist1, fDist2, nClusterID, clusterNodes);
        int iPrev = iMin2;
        
        while (nClusterID[iPrev].size() == 0) {
          iPrev--;
        }
        nNextActive[iPrev] = nNextActive[iMin2];
      } else {
        merge(iMin1, iMin2, fDist1, fDist2, nClusterID, clusterNodes);
        break;
      }
    }
    
    for (int i = 0; i < n; i++) {
      if (nClusterID[i].size() > 0) {
        for (int j = i + 1; j < n; j++) {
          if (nClusterID[j].size() > 0) {
            double fDist1 = fDist[i][j];
            if (nClusterID[i].size() == 1) {
              merge(i, j, fDist1, 0.0D, nClusterID, clusterNodes); break; }
            if (nClusterID[j].size() == 1) {
              merge(i, j, 0.0D, fDist1, nClusterID, clusterNodes); break;
            }
            merge(i, j, fDist1 / 2.0D, fDist1 / 2.0D, nClusterID, clusterNodes);
            
            break;
          }
        }
      }
    }
  }
  





  void doLinkClustering(int nClusters, Vector<Integer>[] nClusterID, Node[] clusterNodes)
  {
    int nInstances = m_instances.numInstances();
    PriorityQueue<Tuple> queue = new PriorityQueue(nClusters * nClusters / 2, new TupleComparator());
    double[][] fDistance0 = new double[nClusters][nClusters];
    double[][] fClusterDistance = (double[][])null;
    if (m_bDebug) {
      fClusterDistance = new double[nClusters][nClusters];
    }
    for (int i = 0; i < nClusters; i++) {
      fDistance0[i][i] = 0.0D;
      for (int j = i + 1; j < nClusters; j++) {
        fDistance0[i][j] = getDistance0(nClusterID[i], nClusterID[j]);
        fDistance0[j][i] = fDistance0[i][j];
        queue.add(new Tuple(fDistance0[i][j], i, j, 1, 1));
        if (m_bDebug) {
          fClusterDistance[i][j] = fDistance0[i][j];
          fClusterDistance[j][i] = fDistance0[i][j];
        }
      }
    }
    while (nClusters > m_nNumClusters) {
      int iMin1 = -1;
      int iMin2 = -1;
      
      if (m_bDebug)
      {
        double fMinDistance = Double.MAX_VALUE;
        for (int i = 0; i < nInstances; i++) {
          if (nClusterID[i].size() > 0) {
            for (int j = i + 1; j < nInstances; j++) {
              if (nClusterID[j].size() > 0) {
                double fDist = fClusterDistance[i][j];
                if (fDist < fMinDistance) {
                  fMinDistance = fDist;
                  iMin1 = i;
                  iMin2 = j;
                }
              }
            }
          }
        }
        merge(iMin1, iMin2, fMinDistance, fMinDistance, nClusterID, clusterNodes);
      }
      else {
        Tuple t;
        do {
          t = (Tuple)queue.poll();
        } while ((t != null) && ((nClusterID[m_iCluster1].size() != m_nClusterSize1) || (nClusterID[m_iCluster2].size() != m_nClusterSize2)));
        iMin1 = m_iCluster1;
        iMin2 = m_iCluster2;
        merge(iMin1, iMin2, m_fDist, m_fDist, nClusterID, clusterNodes);
      }
      


      for (int i = 0; i < nInstances; i++) {
        if ((i != iMin1) && (nClusterID[i].size() != 0)) {
          int i1 = Math.min(iMin1, i);
          int i2 = Math.max(iMin1, i);
          double fDistance = getDistance(fDistance0, nClusterID[i1], nClusterID[i2]);
          if (m_bDebug) {
            fClusterDistance[i1][i2] = fDistance;
            fClusterDistance[i2][i1] = fDistance;
          }
          queue.add(new Tuple(fDistance, i1, i2, nClusterID[i1].size(), nClusterID[i2].size()));
        }
      }
      
      nClusters--;
    }
  }
  
  void merge(int iMin1, int iMin2, double fDist1, double fDist2, Vector<Integer>[] nClusterID, Node[] clusterNodes) {
    if (m_bDebug) {
      System.err.println("Merging " + iMin1 + " " + iMin2 + " " + fDist1 + " " + fDist2);
    }
    if (iMin1 > iMin2) {
      int h = iMin1;iMin1 = iMin2;iMin2 = h;
      double f = fDist1;fDist1 = fDist2;fDist2 = f;
    }
    nClusterID[iMin1].addAll(nClusterID[iMin2]);
    nClusterID[iMin2].removeAllElements();
    

    Node node = new Node();
    if (clusterNodes[iMin1] == null) {
      m_iLeftInstance = iMin1;
    } else {
      m_left = clusterNodes[iMin1];
      m_parent = node;
    }
    if (clusterNodes[iMin2] == null) {
      m_iRightInstance = iMin2;
    } else {
      m_right = clusterNodes[iMin2];
      m_parent = node;
    }
    if (m_bDistanceIsBranchLength) {
      node.setLength(fDist1, fDist2);
    } else {
      node.setHeight(fDist1, fDist2);
    }
    clusterNodes[iMin1] = node;
  }
  
  double getDistance0(Vector<Integer> cluster1, Vector<Integer> cluster2)
  {
    double fBestDist = Double.MAX_VALUE;
    switch (m_nLinkType)
    {
    case 0: 
    case 1: 
    case 2: 
    case 3: 
    case 4: 
    case 6: 
    case 7: 
      Instance instance1 = (Instance)m_instances.instance(((Integer)cluster1.elementAt(0)).intValue()).copy();
      Instance instance2 = (Instance)m_instances.instance(((Integer)cluster2.elementAt(0)).intValue()).copy();
      fBestDist = m_DistanceFunction.distance(instance1, instance2);
      break;
    



    case 5: 
      double ESS1 = calcESS(cluster1);
      double ESS2 = calcESS(cluster2);
      Vector<Integer> merged = new Vector();
      merged.addAll(cluster1);
      merged.addAll(cluster2);
      double ESS = calcESS(merged);
      fBestDist = ESS * merged.size() - ESS1 * cluster1.size() - ESS2 * cluster2.size();
    }
    
    
    return fBestDist;
  }
  




  double getDistance(double[][] fDistance, Vector<Integer> cluster1, Vector<Integer> cluster2)
  {
    double fBestDist = Double.MAX_VALUE;
    switch (m_nLinkType)
    {

    case 0: 
      fBestDist = Double.MAX_VALUE;
      for (int i = 0; i < cluster1.size(); i++) {
        int i1 = ((Integer)cluster1.elementAt(i)).intValue();
        for (int j = 0; j < cluster2.size(); j++) {
          int i2 = ((Integer)cluster2.elementAt(j)).intValue();
          double fDist = fDistance[i1][i2];
          if (fBestDist > fDist) {
            fBestDist = fDist;
          }
        }
      }
      break;
    

    case 1: 
    case 6: 
      fBestDist = 0.0D;
      for (int i = 0; i < cluster1.size(); i++) {
        int i1 = ((Integer)cluster1.elementAt(i)).intValue();
        for (int j = 0; j < cluster2.size(); j++) {
          int i2 = ((Integer)cluster2.elementAt(j)).intValue();
          double fDist = fDistance[i1][i2];
          if (fBestDist < fDist) {
            fBestDist = fDist;
          }
        }
      }
      if (m_nLinkType != 1)
      {


        double fMaxDist = 0.0D;
        for (int i = 0; i < cluster1.size(); i++) {
          int i1 = ((Integer)cluster1.elementAt(i)).intValue();
          for (int j = i + 1; j < cluster1.size(); j++) {
            int i2 = ((Integer)cluster1.elementAt(j)).intValue();
            double fDist = fDistance[i1][i2];
            if (fMaxDist < fDist) {
              fMaxDist = fDist;
            }
          }
        }
        for (int i = 0; i < cluster2.size(); i++) {
          int i1 = ((Integer)cluster2.elementAt(i)).intValue();
          for (int j = i + 1; j < cluster2.size(); j++) {
            int i2 = ((Integer)cluster2.elementAt(j)).intValue();
            double fDist = fDistance[i1][i2];
            if (fMaxDist < fDist) {
              fMaxDist = fDist;
            }
          }
        }
        fBestDist -= fMaxDist; }
      break;
    
    case 2: 
      fBestDist = 0.0D;
      for (int i = 0; i < cluster1.size(); i++) {
        int i1 = ((Integer)cluster1.elementAt(i)).intValue();
        for (int j = 0; j < cluster2.size(); j++) {
          int i2 = ((Integer)cluster2.elementAt(j)).intValue();
          fBestDist += fDistance[i1][i2];
        }
      }
      fBestDist /= cluster1.size() * cluster2.size();
      break;
    

    case 3: 
      Vector<Integer> merged = new Vector();
      merged.addAll(cluster1);
      merged.addAll(cluster2);
      fBestDist = 0.0D;
      for (int i = 0; i < merged.size(); i++) {
        int i1 = ((Integer)merged.elementAt(i)).intValue();
        for (int j = i + 1; j < merged.size(); j++) {
          int i2 = ((Integer)merged.elementAt(j)).intValue();
          fBestDist += fDistance[i1][i2];
        }
      }
      int n = merged.size();
      fBestDist /= n * (n - 1.0D) / 2.0D;
      
      break;
    
    case 4: 
      double[] fValues1 = new double[m_instances.numAttributes()];
      for (int i = 0; i < cluster1.size(); i++) {
        Instance instance = m_instances.instance(((Integer)cluster1.elementAt(i)).intValue());
        for (int j = 0; j < m_instances.numAttributes(); j++) {
          fValues1[j] += instance.value(j);
        }
      }
      double[] fValues2 = new double[m_instances.numAttributes()];
      for (int i = 0; i < cluster2.size(); i++) {
        Instance instance = m_instances.instance(((Integer)cluster2.elementAt(i)).intValue());
        for (int j = 0; j < m_instances.numAttributes(); j++) {
          fValues2[j] += instance.value(j);
        }
      }
      for (int j = 0; j < m_instances.numAttributes(); j++) {
        fValues1[j] /= cluster1.size();
        fValues2[j] /= cluster2.size();
      }
      
      Instance instance1 = (Instance)m_instances.instance(0).copy();
      Instance instance2 = (Instance)m_instances.instance(0).copy();
      for (int j = 0; j < m_instances.numAttributes(); j++) {
        instance1.setValue(j, fValues1[j]);
        instance2.setValue(j, fValues2[j]);
      }
      fBestDist = m_DistanceFunction.distance(instance1, instance2);
      break;
    



    case 5: 
      double ESS1 = calcESS(cluster1);
      double ESS2 = calcESS(cluster2);
      Vector<Integer> merged = new Vector();
      merged.addAll(cluster1);
      merged.addAll(cluster2);
      double ESS = calcESS(merged);
      fBestDist = ESS * merged.size() - ESS1 * cluster1.size() - ESS2 * cluster2.size();
    }
    
    
    return fBestDist;
  }
  
  double calcESS(Vector<Integer> cluster)
  {
    double[] fValues1 = new double[m_instances.numAttributes()];
    for (int i = 0; i < cluster.size(); i++) {
      Instance instance = m_instances.instance(((Integer)cluster.elementAt(i)).intValue());
      for (int j = 0; j < m_instances.numAttributes(); j++) {
        fValues1[j] += instance.value(j);
      }
    }
    for (int j = 0; j < m_instances.numAttributes(); j++) {
      fValues1[j] /= cluster.size();
    }
    
    Instance centroid = (Instance)m_instances.instance(((Integer)cluster.elementAt(0)).intValue()).copy();
    for (int j = 0; j < m_instances.numAttributes(); j++) {
      centroid.setValue(j, fValues1[j]);
    }
    double fESS = 0.0D;
    for (int i = 0; i < cluster.size(); i++) {
      Instance instance = m_instances.instance(((Integer)cluster.elementAt(i)).intValue());
      fESS += m_DistanceFunction.distance(centroid, instance);
    }
    return fESS / cluster.size();
  }
  



  public int clusterInstance(Instance instance)
    throws Exception
  {
    if (m_instances.numInstances() == 0) {
      return 0;
    }
    double fBestDist = Double.MAX_VALUE;
    int iBestInstance = -1;
    for (int i = 0; i < m_instances.numInstances(); i++) {
      double fDist = m_DistanceFunction.distance(instance, m_instances.instance(i));
      if (fDist < fBestDist) {
        fBestDist = fDist;
        iBestInstance = i;
      }
    }
    return m_nClusterNr[iBestInstance];
  }
  


  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (numberOfClusters() == 0) {
      double[] p = new double[1];
      p[0] = 1.0D;
      return p;
    }
    double[] p = new double[numberOfClusters()];
    p[clusterInstance(instance)] = 1.0D;
    return p;
  }
  
  public Capabilities getCapabilities()
  {
    Capabilities result = new Capabilities(this);
    result.disableAll();
    result.enable(Capabilities.Capability.NO_CLASS);
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    result.enable(Capabilities.Capability.STRING_ATTRIBUTES);
    

    result.setMinimumNumberInstances(0);
    return result;
  }
  
  public int numberOfClusters() throws Exception
  {
    return Math.min(m_nNumClusters, m_instances.numInstances());
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(8);
    newVector.addElement(new Option("\tIf set, classifier is run in debug mode and\n\tmay output additional info to the console", "D", 0, "-D"));
    


    newVector.addElement(new Option("\tIf set, distance is interpreted as branch length\n\totherwise it is node height.", "B", 0, "-B"));
    



    newVector.addElement(new Option("\tnumber of clusters", "N", 1, "-N <Nr Of Clusters>"));
    

    newVector.addElement(new Option("\tFlag to indicate the cluster should be printed in Newick format.", "P", 0, "-P"));
    

    newVector.addElement(new Option("Link type (Single, Complete, Average, Mean, Centroid, Ward, Adjusted complete, Neighbor joining)", "L", 1, "-L [SINGLE|COMPLETE|AVERAGE|MEAN|CENTROID|WARD|ADJCOMLPETE|NEIGHBOR_JOINING]"));
    


    newVector.add(new Option("\tDistance function to use.\n\t(default: weka.core.EuclideanDistance)", "A", 1, "-A <classname and options>"));
    


    return newVector.elements();
  }
  









  public void setOptions(String[] options)
    throws Exception
  {
    m_bPrintNewick = Utils.getFlag('P', options);
    
    String optionString = Utils.getOption('N', options);
    if (optionString.length() != 0) {
      Integer temp = new Integer(optionString);
      setNumClusters(temp.intValue());
    }
    else {
      setNumClusters(2);
    }
    
    setDebug(Utils.getFlag('D', options));
    setDistanceIsBranchLength(Utils.getFlag('B', options));
    
    String sLinkType = Utils.getOption('L', options);
    

    if (sLinkType.compareTo("SINGLE") == 0) setLinkType(new SelectedTag(0, TAGS_LINK_TYPE));
    if (sLinkType.compareTo("COMPLETE") == 0) setLinkType(new SelectedTag(1, TAGS_LINK_TYPE));
    if (sLinkType.compareTo("AVERAGE") == 0) setLinkType(new SelectedTag(2, TAGS_LINK_TYPE));
    if (sLinkType.compareTo("MEAN") == 0) setLinkType(new SelectedTag(3, TAGS_LINK_TYPE));
    if (sLinkType.compareTo("CENTROID") == 0) setLinkType(new SelectedTag(4, TAGS_LINK_TYPE));
    if (sLinkType.compareTo("WARD") == 0) setLinkType(new SelectedTag(5, TAGS_LINK_TYPE));
    if (sLinkType.compareTo("ADJCOMLPETE") == 0) setLinkType(new SelectedTag(6, TAGS_LINK_TYPE));
    if (sLinkType.compareTo("NEIGHBOR_JOINING") == 0) { setLinkType(new SelectedTag(7, TAGS_LINK_TYPE));
    }
    String nnSearchClass = Utils.getOption('A', options);
    if (nnSearchClass.length() != 0) {
      String[] nnSearchClassSpec = Utils.splitOptions(nnSearchClass);
      if (nnSearchClassSpec.length == 0) {
        throw new Exception("Invalid DistanceFunction specification string.");
      }
      String className = nnSearchClassSpec[0];
      nnSearchClassSpec[0] = "";
      
      setDistanceFunction((DistanceFunction)Utils.forName(DistanceFunction.class, className, nnSearchClassSpec));

    }
    else
    {
      setDistanceFunction(new EuclideanDistance());
    }
    
    Utils.checkForRemainingOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] options = new String[14];
    int current = 0;
    
    options[(current++)] = "-N";
    options[(current++)] = ("" + getNumClusters());
    
    options[(current++)] = "-L";
    switch (m_nLinkType) {
    case 0:  options[(current++)] = "SINGLE"; break;
    case 1:  options[(current++)] = "COMPLETE"; break;
    case 2:  options[(current++)] = "AVERAGE"; break;
    case 3:  options[(current++)] = "MEAN"; break;
    case 4:  options[(current++)] = "CENTROID"; break;
    case 5:  options[(current++)] = "WARD"; break;
    case 6:  options[(current++)] = "ADJCOMLPETE"; break;
    case 7:  options[(current++)] = "NEIGHBOR_JOINING";
    }
    if (m_bPrintNewick) {
      options[(current++)] = "-P";
    }
    if (getDebug()) {
      options[(current++)] = "-D";
    }
    if (getDistanceIsBranchLength()) {
      options[(current++)] = "-B";
    }
    
    options[(current++)] = "-A";
    options[(current++)] = (m_DistanceFunction.getClass().getName() + " " + Utils.joinOptions(m_DistanceFunction.getOptions())).trim();
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  
  public String toString() { StringBuffer buf = new StringBuffer();
    int attIndex = m_instances.classIndex();
    if (attIndex < 0)
    {
      attIndex = 0;
      while ((attIndex < m_instances.numAttributes() - 1) && 
        (!m_instances.attribute(attIndex).isString()))
      {

        attIndex++;
      }
    }
    try {
      if ((m_bPrintNewick) && (numberOfClusters() > 0)) {
        for (int i = 0; i < m_clusters.length; i++) {
          if (m_clusters[i] != null) {
            buf.append("Cluster " + i + "\n");
            if (m_instances.attribute(attIndex).isString()) {
              buf.append(m_clusters[i].toString(attIndex));
            } else {
              buf.append(m_clusters[i].toString2(attIndex));
            }
            buf.append("\n\n");
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return buf.toString();
  }
  




  public void setDebug(boolean debug)
  {
    m_bDebug = debug;
  }
  





  public boolean getDebug()
  {
    return m_bDebug;
  }
  
  public boolean getDistanceIsBranchLength() { return m_bDistanceIsBranchLength; }
  
  public void setDistanceIsBranchLength(boolean bDistanceIsHeight) { m_bDistanceIsBranchLength = bDistanceIsHeight; }
  
  public String distanceIsBranchLengthTipText() {
    return "If set to false, the distance between clusters is interpreted as the height of the node linking the clusters. This is appropriate for example for single link clustering. However, for neighbor joining, the distance is better interpreted as branch length. Set this flag to get the latter interpretation.";
  }
  







  public String debugTipText()
  {
    return "If set to true, classifier may output additional info to the console.";
  }
  


  public String numClustersTipText()
  {
    return "Sets the number of clusters. If a single hierarchy is desired, set this to 1.";
  }
  



  public String printNewickTipText()
  {
    return "Flag to indicate whether the cluster should be print in Newick format. This can be useful for display in other programs. However, for large datasets a lot of text may be produced, which may not be a nuisance when the Newick format is not required";
  }
  





  public String distanceFunctionTipText()
  {
    return "Sets the distance function, which measures the distance between two individual. instances (or possibly the distance between an instance and the centroid of a clusterdepending on the Link type).";
  }
  




  public String linkTypeTipText()
  {
    return "Sets the method used to measure the distance between two clusters.\nSINGLE:\n find single link distance aka minimum link, which is the closest distance between any item in cluster1 and any item in cluster2\nCOMPLETE:\n find complete link distance aka maximum link, which is the largest distance between any item in cluster1 and any item in cluster2\nADJCOMLPETE:\n as COMPLETE, but with adjustment, which is the largest within cluster distance\nAVERAGE:\n finds average distance between the elements of the two clusters\nMEAN: \n calculates the mean distance of a merged cluster (akak Group-average agglomerative clustering)\nCENTROID:\n finds the distance of the centroids of the clusters\nWARD:\n finds the distance of the change in caused by merging the cluster. The information of a cluster is calculated as the error sum of squares of the centroids of the cluster and its members.\nNEIGHBOR_JOINING\n use neighbor joining algorithm.";
  }
  
























  public String globalInfo()
  {
    return "Hierarchical clustering class.\nImplements a number of classic agglomorative (i.e. bottom up) hierarchical clustering methodsbased on .";
  }
  


  public static void main(String[] argv)
  {
    runClusterer(new HierarchicalClusterer(), argv);
  }
  
  public String graph() throws Exception {
    if (numberOfClusters() == 0) {
      return "Newick:(no,clusters)";
    }
    int attIndex = m_instances.classIndex();
    if (attIndex < 0)
    {
      attIndex = 0;
      while ((attIndex < m_instances.numAttributes() - 1) && 
        (!m_instances.attribute(attIndex).isString()))
      {

        attIndex++;
      }
    }
    String sNewick = null;
    if (m_instances.attribute(attIndex).isString()) {
      sNewick = m_clusters[0].toString(attIndex);
    } else {
      sNewick = m_clusters[0].toString2(attIndex);
    }
    return "Newick:" + sNewick;
  }
  
  public int graphType() {
    return 3;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11330 $");
  }
}

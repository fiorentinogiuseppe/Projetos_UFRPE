package weka.classifiers.bayes.net;

import java.io.FileReader;
import java.io.PrintStream;
import java.io.Serializable;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;





































































public class ADNode
  implements Serializable, TechnicalInformationHandler, RevisionHandler
{
  static final long serialVersionUID = 397409728366910204L;
  static final int MIN_RECORD_SIZE = 0;
  public VaryNode[] m_VaryNodes;
  public Instance[] m_Instances;
  public int m_nCount;
  public int m_nStartNode;
  
  public ADNode() {}
  
  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Andrew W. Moore and Mary S. Lee");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.TITLE, "Cached Sufficient Statistics for Efficient Machine Learning with Large Datasets");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Journal of Artificial Intelligence Research");
    result.setValue(TechnicalInformation.Field.VOLUME, "8");
    result.setValue(TechnicalInformation.Field.PAGES, "67-91");
    
    return result;
  }
  





  public static VaryNode makeVaryNode(int iNode, FastVector nRecords, Instances instances)
  {
    VaryNode _VaryNode = new VaryNode(iNode);
    int nValues = instances.attribute(iNode).numValues();
    


    FastVector[] nChildRecords = new FastVector[nValues];
    for (int iChild = 0; iChild < nValues; iChild++) {
      nChildRecords[iChild] = new FastVector();
    }
    
    for (int iRecord = 0; iRecord < nRecords.size(); iRecord++) {
      int iInstance = ((Integer)nRecords.elementAt(iRecord)).intValue();
      nChildRecords[((int)instances.instance(iInstance).value(iNode))].addElement(new Integer(iInstance));
    }
    

    int nCount = nChildRecords[0].size();
    int nMCV = 0;
    for (int iChild = 1; iChild < nValues; iChild++) {
      if (nChildRecords[iChild].size() > nCount) {
        nCount = nChildRecords[iChild].size();
        nMCV = iChild;
      }
    }
    m_nMCV = nMCV;
    

    m_ADNodes = new ADNode[nValues];
    for (int iChild = 0; iChild < nValues; iChild++) {
      if ((iChild == nMCV) || (nChildRecords[iChild].size() == 0)) {
        m_ADNodes[iChild] = null;
      } else {
        m_ADNodes[iChild] = makeADTree(iNode + 1, nChildRecords[iChild], instances);
      }
    }
    return _VaryNode;
  }
  







  public static ADNode makeADTree(int iNode, FastVector nRecords, Instances instances)
  {
    ADNode _ADNode = new ADNode();
    m_nCount = nRecords.size();
    m_nStartNode = iNode;
    if (nRecords.size() < 0) {
      m_Instances = new Instance[nRecords.size()];
      for (int iInstance = 0; iInstance < nRecords.size(); iInstance++) {
        m_Instances[iInstance] = instances.instance(((Integer)nRecords.elementAt(iInstance)).intValue());
      }
    } else {
      m_VaryNodes = new VaryNode[instances.numAttributes() - iNode];
      for (int iNode2 = iNode; iNode2 < instances.numAttributes(); iNode2++) {
        m_VaryNodes[(iNode2 - iNode)] = makeVaryNode(iNode2, nRecords, instances);
      }
    }
    return _ADNode;
  }
  





  public static ADNode makeADTree(Instances instances)
  {
    FastVector nRecords = new FastVector(instances.numInstances());
    for (int iRecord = 0; iRecord < instances.numInstances(); iRecord++) {
      nRecords.addElement(new Integer(iRecord));
    }
    return makeADTree(0, nRecords, instances);
  }
  




















  public void getCounts(int[] nCounts, int[] nNodes, int[] nOffsets, int iNode, int iOffset, boolean bSubstract)
  {
    if (iNode >= nNodes.length) {
      if (bSubstract) {
        nCounts[iOffset] -= m_nCount;
      } else {
        nCounts[iOffset] += m_nCount;
      }
      return;
    }
    if (m_VaryNodes != null) {
      m_VaryNodes[(nNodes[iNode] - m_nStartNode)].getCounts(nCounts, nNodes, nOffsets, iNode, iOffset, this, bSubstract);
    } else {
      for (int iInstance = 0; iInstance < m_Instances.length; iInstance++) {
        int iOffset2 = iOffset;
        Instance instance = m_Instances[iInstance];
        for (int iNode2 = iNode; iNode2 < nNodes.length; iNode2++) {
          iOffset2 += nOffsets[iNode2] * (int)instance.value(nNodes[iNode2]);
        }
        if (bSubstract) {
          nCounts[iOffset2] -= 1;
        } else {
          nCounts[iOffset2] += 1;
        }
      }
    }
  }
  




  public void print()
  {
    String sTab = new String(); for (int i = 0; i < m_nStartNode; i++) {
      sTab = sTab + "  ";
    }
    System.out.println(sTab + "Count = " + m_nCount);
    if (m_VaryNodes != null) {
      for (int iNode = 0; iNode < m_VaryNodes.length; iNode++) {
        System.out.println(sTab + "Node " + (iNode + m_nStartNode));
        m_VaryNodes[iNode].print(sTab);
      }
    } else {
      System.out.println(m_Instances);
    }
  }
  



  public static void main(String[] argv)
  {
    try
    {
      Instances instances = new Instances(new FileReader("\\iris.2.arff"));
      ADNode ADTree = makeADTree(instances);
      int[] nCounts = new int[12];
      int[] nNodes = new int[3];
      int[] nOffsets = new int[3];
      nNodes[0] = 0;
      nNodes[1] = 3;
      nNodes[2] = 4;
      nOffsets[0] = 2;
      nOffsets[1] = 1;
      nOffsets[2] = 4;
      ADTree.print();
      ADTree.getCounts(nCounts, nNodes, nOffsets, 0, 0, false);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}

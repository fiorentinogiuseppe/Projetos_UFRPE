package weka.core.neighboursearch.kdtrees;

import java.io.PrintStream;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;






















































public class KMeansInpiredMethod
  extends KDTreeNodeSplitter
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = -866783749124714304L;
  
  public KMeansInpiredMethod() {}
  
  public String globalInfo()
  {
    return "The class that splits a node into two such that the overall sum of squared distances of points to their centres on both sides of the (axis-parallel) splitting plane is minimum.\n\nFor more information see also:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MASTERSTHESIS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Ashraf Masood Kibriya");
    result.setValue(TechnicalInformation.Field.TITLE, "Fast Algorithms for Nearest Neighbour Search");
    result.setValue(TechnicalInformation.Field.YEAR, "2007");
    result.setValue(TechnicalInformation.Field.SCHOOL, "Department of Computer Science, School of Computing and Mathematical Sciences, University of Waikato");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Hamilton, New Zealand");
    
    return result;
  }
  

















  public void splitNode(KDTreeNode node, int numNodesCreated, double[][] nodeRanges, double[][] universe)
    throws Exception
  {
    correctlyInitialized();
    
    int splitDim = -1;
    double splitVal = Double.NEGATIVE_INFINITY;
    
    double[] leftAttSum = new double[m_Instances.numAttributes()];
    double[] rightAttSum = new double[m_Instances.numAttributes()];
    double[] leftAttSqSum = new double[m_Instances.numAttributes()];
    double[] rightAttSqSum = new double[m_Instances.numAttributes()];
    
    double minSum = Double.POSITIVE_INFINITY;
    
    for (int dim = 0; dim < m_Instances.numAttributes(); dim++)
    {

      if ((m_NodeRanges[dim][2] != 0.0D) && (dim != m_Instances.classIndex()))
      {


        quickSort(m_Instances, m_InstList, dim, m_Start, m_End);
        
        for (int i = m_Start; i <= m_End; tmp286_285++) {
          for (int j = 0; j < m_Instances.numAttributes(); j++) {
            if (j != m_Instances.classIndex())
            {
              double val = m_Instances.instance(m_InstList[i]).value(j);
              if (m_NormalizeNodeWidth) {
                if ((Double.isNaN(universe[j][0])) || (universe[j][0] == universe[j][1]))
                {
                  val = 0.0D;
                } else {
                  val = (val - universe[j][0]) / universe[j][2];
                }
              }
              if (i == m_Start) {
                double tmp286_285 = (leftAttSqSum[j] = rightAttSqSum[j] = 0.0D);rightAttSum[j] = tmp286_285;leftAttSum[j] = tmp286_285;
              }
              rightAttSum[j] += val;
              rightAttSqSum[j] += val * val;
            }
          }
        }
        for (int i = m_Start; i <= m_End - 1; i++) {
          Instance inst = m_Instances.instance(m_InstList[i]);
          double rightSqSum; double leftSqSum = rightSqSum = 0.0D;
          for (int j = 0; j < m_Instances.numAttributes(); j++) {
            if (j != m_Instances.classIndex())
            {
              double val = inst.value(j);
              
              if (m_NormalizeNodeWidth) {
                if ((Double.isNaN(universe[j][0])) || (universe[j][0] == universe[j][1]))
                {
                  val = 0.0D;
                } else {
                  val = (val - universe[j][0]) / universe[j][2];
                }
              }
              
              leftAttSum[j] += val;
              rightAttSum[j] -= val;
              leftAttSqSum[j] += val * val;
              rightAttSqSum[j] -= val * val;
              double leftSqMean = leftAttSum[j] / (i - m_Start + 1);
              leftSqMean *= leftSqMean;
              double rightSqMean = rightAttSum[j] / (m_End - i);
              rightSqMean *= rightSqMean;
              
              leftSqSum += leftAttSqSum[j] - (i - m_Start + 1) * leftSqMean;
              rightSqSum += rightAttSqSum[j] - (m_End - i) * rightSqMean;
            }
          }
          if (minSum > leftSqSum + rightSqSum) {
            minSum = leftSqSum + rightSqSum;
            
            if (i < m_End) {
              splitVal = (m_Instances.instance(m_InstList[i]).value(dim) + m_Instances.instance(m_InstList[(i + 1)]).value(dim)) / 2.0D;
            }
            else {
              splitVal = m_Instances.instance(m_InstList[i]).value(dim);
            }
            splitDim = dim;
          }
        }
      }
    }
    int rightStart = rearrangePoints(m_InstList, m_Start, m_End, splitDim, splitVal);
    

    if ((rightStart == m_Start) || (rightStart > m_End)) {
      System.out.println("node.m_Start: " + m_Start + " node.m_End: " + m_End + " splitDim: " + splitDim + " splitVal: " + splitVal + " node.min: " + m_NodeRanges[splitDim][0] + " node.max: " + m_NodeRanges[splitDim][1] + " node.numInstances: " + node.numInstances());
      




      if (rightStart == m_Start) {
        throw new Exception("Left child is empty in node " + m_NodeNumber + ". Not possible with " + "KMeanInspiredMethod splitting method. Please " + "check code.");
      }
      

      throw new Exception("Right child is empty in node " + m_NodeNumber + ". Not possible with " + "KMeansInspiredMethod splitting method. Please " + "check code.");
    }
    


    m_SplitDim = splitDim;
    m_SplitValue = splitVal;
    m_Left = new KDTreeNode(numNodesCreated + 1, m_Start, rightStart - 1, m_EuclideanDistance.initializeRanges(m_InstList, m_Start, rightStart - 1));
    

    m_Right = new KDTreeNode(numNodesCreated + 2, rightStart, m_End, m_EuclideanDistance.initializeRanges(m_InstList, rightStart, m_End));
  }
  


















  protected static int partition(Instances insts, int[] index, int attidx, int l, int r)
  {
    double pivot = insts.instance(index[((l + r) / 2)]).value(attidx);
    

    while (l < r) {
      while ((insts.instance(index[l]).value(attidx) < pivot) && (l < r)) {
        l++;
      }
      while ((insts.instance(index[r]).value(attidx) > pivot) && (l < r)) {
        r--;
      }
      if (l < r) {
        int help = index[l];
        index[l] = index[r];
        index[r] = help;
        l++;
        r--;
      }
    }
    if ((l == r) && (insts.instance(index[r]).value(attidx) > pivot)) {
      r--;
    }
    
    return r;
  }
  
















  protected static void quickSort(Instances insts, int[] indices, int attidx, int left, int right)
  {
    if (left < right) {
      int middle = partition(insts, indices, attidx, left, right);
      quickSort(insts, indices, attidx, left, middle);
      quickSort(insts, indices, attidx, middle + 1, right);
    }
  }
  















  private static void checkSort(Instances insts, int[] indices, int attidx, int start, int end)
    throws Exception
  {
    for (int i = start + 1; i <= end; i++) {
      if (insts.instance(indices[(i - 1)]).value(attidx) > insts.instance(indices[i]).value(attidx))
      {
        System.out.println("value[i-1]: " + insts.instance(indices[(i - 1)]).value(attidx));
        System.out.println("value[i]: " + insts.instance(indices[i]).value(attidx));
        System.out.println("indices[i-1]: " + indices[(i - 1)]);
        System.out.println("indices[i]: " + indices[i]);
        System.out.println("i: " + i);
        if (insts.instance(indices[(i - 1)]).value(attidx) > insts.instance(indices[i]).value(attidx)) {
          System.out.println("value[i-1] > value[i]");
        }
        throw new Exception("Indices not sorted correctly.");
      }
    }
  }
  
















  protected int rearrangePoints(int[] indices, int startidx, int endidx, int splitDim, double splitVal)
  {
    int left = startidx - 1;
    for (int i = startidx; i <= endidx; i++) {
      if (m_EuclideanDistance.valueIsSmallerEqual(m_Instances.instance(indices[i]), splitDim, splitVal))
      {
        left++;
        int tmp = indices[left];
        indices[left] = indices[i];
        indices[i] = tmp;
      }
    }
    return left + 1;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

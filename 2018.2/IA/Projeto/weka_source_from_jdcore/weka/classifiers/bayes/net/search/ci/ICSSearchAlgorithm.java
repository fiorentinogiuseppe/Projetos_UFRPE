package weka.classifiers.bayes.net.search.ci;

import java.io.FileReader;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;

























































public class ICSSearchAlgorithm
  extends CISearchAlgorithm
{
  static final long serialVersionUID = -2510985917284798576L;
  private int m_nMaxCardinality;
  
  String name(int iAttribute)
  {
    return m_instances.attribute(iAttribute).name();
  }
  




  int maxn()
  {
    return m_instances.numAttributes();
  }
  
  public ICSSearchAlgorithm() {
    m_nMaxCardinality = 2;
  }
  



  public void setMaxCardinality(int nMaxCardinality)
  {
    m_nMaxCardinality = nMaxCardinality;
  }
  




  public int getMaxCardinality()
  {
    return m_nMaxCardinality;
  }
  


  class SeparationSet
    implements RevisionHandler
  {
    public int[] m_set;
    


    public SeparationSet()
    {
      m_set = new int[getMaxCardinality() + 1];
    }
    
    public boolean contains(int nItem) {
      for (int iItem = 0; (iItem < getMaxCardinality()) && (m_set[iItem] != -1); iItem++) {
        if (m_set[iItem] == nItem) {
          return true;
        }
      }
      return false;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.8 $");
    }
  }
  






  protected void search(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    m_BayesNet = bayesNet;
    m_instances = instances;
    
    boolean[][] edges = new boolean[maxn() + 1][];
    boolean[][] arrows = new boolean[maxn() + 1][];
    SeparationSet[][] sepsets = new SeparationSet[maxn() + 1][];
    for (int iNode = 0; iNode < maxn() + 1; iNode++) {
      edges[iNode] = new boolean[maxn()];
      arrows[iNode] = new boolean[maxn()];
      sepsets[iNode] = new SeparationSet[maxn()];
    }
    
    calcDependencyGraph(edges, sepsets);
    calcVeeNodes(edges, arrows, sepsets);
    calcArcDirections(edges, arrows);
    

    for (int iNode = 0; iNode < maxn(); iNode++)
    {
      ParentSet oParentSet = m_BayesNet.getParentSet(iNode);
      while (oParentSet.getNrOfParents() > 0) {
        oParentSet.deleteLastParent(m_instances);
      }
      for (int iParent = 0; iParent < maxn(); iParent++) {
        if (arrows[iParent][iNode] != 0) {
          oParentSet.addParent(iParent, m_instances);
        }
      }
    }
  }
  












  void calcDependencyGraph(boolean[][] edges, SeparationSet[][] sepsets)
  {
    for (int iNode1 = 0; iNode1 < maxn(); iNode1++)
    {
      for (int iNode2 = 0; iNode2 < maxn(); iNode2++) {
        edges[iNode1][iNode2] = 1;
      }
    }
    for (int iNode1 = 0; iNode1 < maxn(); iNode1++) {
      edges[iNode1][iNode1] = 0;
    }
    
    for (int iCardinality = 0; iCardinality <= getMaxCardinality(); iCardinality++) {
      for (int iNode1 = 0; iNode1 <= maxn() - 2; iNode1++) {
        for (int iNode2 = iNode1 + 1; iNode2 < maxn(); iNode2++) {
          if (edges[iNode1][iNode2] != 0) {
            SeparationSet oSepSet = existsSepSet(iNode1, iNode2, iCardinality, edges);
            if (oSepSet != null) {
              edges[iNode1][iNode2] = 0;
              edges[iNode2][iNode1] = 0;
              sepsets[iNode1][iNode2] = oSepSet;
              sepsets[iNode2][iNode1] = oSepSet;
              
              System.err.print("I(" + name(iNode1) + ", {");
              for (int iNode3 = 0; iNode3 < iCardinality; iNode3++) {
                System.err.print(name(m_set[iNode3]) + " ");
              }
              System.err.print("} ," + name(iNode2) + ")\n");
            }
          }
        }
      }
      
      System.err.print(iCardinality + " ");
      for (int iNode1 = 0; iNode1 < maxn(); iNode1++) {
        System.err.print(name(iNode1) + " ");
      }
      System.err.print('\n');
      for (int iNode1 = 0; iNode1 < maxn(); iNode1++) {
        for (int iNode2 = 0; iNode2 < maxn(); iNode2++) {
          if (edges[iNode1][iNode2] != 0) {
            System.err.print("X ");
          } else
            System.err.print(". ");
        }
        System.err.print(name(iNode1) + " ");
        System.err.print('\n');
      }
    }
  }
  













  SeparationSet existsSepSet(int iNode1, int iNode2, int nCardinality, boolean[][] edges)
  {
    SeparationSet Z = new SeparationSet();
    m_set[nCardinality] = -1;
    




    if (nCardinality > 0) {
      m_set[0] = next(-1, iNode1, iNode2, edges);
      int iNode3 = 1;
      while (iNode3 < nCardinality) {
        m_set[iNode3] = next(m_set[(iNode3 - 1)], iNode1, iNode2, edges);
        iNode3++;
      } }
    int iZ;
    int iZ;
    if (nCardinality > 0) {
      iZ = maxn() - m_set[(nCardinality - 1)] - 1;
    } else {
      iZ = 0;
    }
    
    label298:
    while (iZ >= 0)
    {

      if (isConditionalIndependent(iNode2, iNode1, m_set, nCardinality)) {
        return Z;
      }
      
      if (nCardinality > 0) {
        m_set[(nCardinality - 1)] = next(m_set[(nCardinality - 1)], iNode1, iNode2, edges);
      }
      iZ = nCardinality - 1;
      for (;;) { if ((iZ < 0) || (m_set[iZ] < maxn())) break label298;
        iZ = nCardinality - 1;
        while ((iZ >= 0) && (m_set[iZ] >= maxn())) {
          iZ--;
        }
        if (iZ < 0) {
          break;
        }
        m_set[iZ] = next(m_set[iZ], iNode1, iNode2, edges);
        for (int iNode3 = iZ + 1; iNode3 < nCardinality; iNode3++) {
          m_set[iNode3] = next(m_set[(iNode3 - 1)], iNode1, iNode2, edges);
        }
        iZ = nCardinality - 1;
      }
    }
    
    return null;
  }
  





  int next(int x, int iNode1, int iNode2, boolean[][] edges)
  {
    
    



    while ((x < maxn()) && ((edges[iNode1][x] == 0) || (edges[iNode2][x] == 0) || (x == iNode2))) {
      x++;
    }
    return x;
  }
  














  void calcVeeNodes(boolean[][] edges, boolean[][] arrows, SeparationSet[][] sepsets)
  {
    for (int iNode1 = 0; iNode1 < maxn(); iNode1++) {
      for (int iNode2 = 0; iNode2 < maxn(); iNode2++) {
        arrows[iNode1][iNode2] = 0;
      }
    }
    
    for (int iNode1 = 0; iNode1 < maxn() - 1; iNode1++) {
      for (int iNode2 = iNode1 + 1; iNode2 < maxn(); iNode2++) {
        if (edges[iNode1][iNode2] == 0) {
          for (int iNode3 = 0; iNode3 < maxn(); iNode3++) {
            if ((((iNode3 != iNode1) && (iNode3 != iNode2) && (edges[iNode1][iNode3] != 0) && (edges[iNode2][iNode3] != 0) ? 1 : 0) & (!sepsets[iNode1][iNode2].contains(iNode3) ? 1 : 0)) != 0)
            {



              arrows[iNode1][iNode3] = 1;
              arrows[iNode2][iNode3] = 1;
            }
          }
        }
      }
    }
  }
  











  void calcArcDirections(boolean[][] edges, boolean[][] arrows)
  {
    boolean bFound;
    









    do
    {
      bFound = false;
      


      for (int i = 0; i < maxn(); i++) {
        for (int j = 0; j < maxn(); j++) {
          if ((i != j) && (arrows[i][j] != 0)) {
            for (int k = 0; k < maxn(); k++) {
              if ((i != k) && (j != k) && (edges[j][k] != 0) && (edges[i][k] == 0) && (arrows[j][k] == 0) && (arrows[k][j] == 0))
              {




                arrows[j][k] = 1;
                bFound = true;
              }
            }
          }
        }
      }
      


      for (i = 0; i < maxn(); i++) {
        for (int j = 0; j < maxn(); j++) {
          if ((i != j) && (arrows[i][j] != 0)) {
            for (int k = 0; k < maxn(); k++) {
              if ((i != k) && (j != k) && (edges[i][k] != 0) && (arrows[j][k] != 0) && (arrows[i][k] == 0) && (arrows[k][i] == 0))
              {




                arrows[i][k] = 1;
                bFound = true;
              }
            }
          }
        }
      }
      






      for (i = 0; i < maxn(); i++) {
        for (int j = 0; j < maxn(); j++) {
          if ((i != j) && (arrows[i][j] != 0)) {
            for (int k = 0; k < maxn(); k++) {
              if ((k != i) && (k != j) && (arrows[k][j] != 0) && (edges[k][i] == 0))
              {


                for (int m = 0; m < maxn(); m++) {
                  if ((m != i) && (m != j) && (m != k) && (edges[m][i] != 0) && (arrows[m][i] == 0) && (arrows[i][m] == 0) && (edges[m][j] != 0) && (arrows[m][j] == 0) && (arrows[j][m] == 0) && (edges[m][k] != 0) && (arrows[m][k] == 0) && (arrows[k][m] == 0))
                  {










                    arrows[m][j] = 1;
                    bFound = true;
                  }
                }
              }
            }
          }
        }
      }
      






      for (i = 0; i < maxn(); i++) {
        for (int j = 0; j < maxn(); j++) {
          if ((i != j) && (arrows[j][i] != 0)) {
            for (int k = 0; k < maxn(); k++) {
              if ((k != i) && (k != j) && (edges[k][j] != 0) && (arrows[k][j] == 0) && (arrows[j][k] == 0) && (edges[k][i] != 0) && (arrows[k][i] == 0) && (arrows[i][k] == 0))
              {






                for (int m = 0; m < maxn(); m++) {
                  if ((m != i) && (m != j) && (m != k) && (edges[m][i] != 0) && (arrows[m][i] == 0) && (arrows[i][m] == 0) && (edges[m][k] != 0) && (arrows[m][k] == 0) && (arrows[k][m] == 0))
                  {







                    arrows[i][m] = 1;
                    arrows[k][m] = 1;
                    bFound = true;
                  }
                }
              }
            }
          }
        }
      }
      


      if (!bFound) {
        i = 0;
        while ((!bFound) && (i < maxn())) {
          int j = 0;
          while ((!bFound) && (j < maxn())) {
            if ((edges[i][j] != 0) && (arrows[i][j] == 0) && (arrows[j][i] == 0))
            {

              arrows[i][j] = 1;
              bFound = true;
            }
            j++;
          }
          i++;
        }
        
      }
      
    } while (bFound);
  }
  





  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tWhen determining whether an edge exists a search is performed \n\tfor a set Z that separates the nodes. MaxCardinality determines \n\tthe maximum size of the set Z. This greatly influences the \n\tlength of the search. (default 2)", "cardinality", 1, "-cardinality <num>"));
    





    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    return result.elements();
  }
  


























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption("cardinality", options);
    if (tmpStr.length() != 0) {
      setMaxCardinality(Integer.parseInt(tmpStr));
    } else {
      setMaxCardinality(2);
    }
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-cardinality");
    result.add("" + getMaxCardinality());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  



  public String maxCardinalityTipText()
  {
    return "When determining whether an edge exists a search is performed for a set Z that separates the nodes. MaxCardinality determines the maximum size of the set Z. This greatly influences the length of the search. Default value is 2.";
  }
  





  public String globalInfo()
  {
    return "This Bayes Network learning algorithm uses conditional independence tests to find a skeleton, finds V-nodes and applies a set of rules to find the directions of the remaining arrows.";
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  



  public static void main(String[] argv)
  {
    try
    {
      BayesNet b = new BayesNet();
      b.setSearchAlgorithm(new ICSSearchAlgorithm());
      Instances instances = new Instances(new FileReader("C:\\eclipse\\workspace\\weka\\data\\contact-lenses.arff"));
      instances.setClassIndex(instances.numAttributes() - 1);
      b.buildClassifier(instances);
      System.out.println(b.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

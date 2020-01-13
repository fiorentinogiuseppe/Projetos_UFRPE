package weka.clusterers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;































































public class CLOPE
  extends AbstractClusterer
  implements OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -567567567567588L;
  
  private class CLOPECluster
    implements Serializable
  {
    public int N = 0;
    



    public int W = 0;
    



    public int S = 0;
    



    public HashMap occ = new HashMap();
    

    private CLOPECluster() {}
    
    public void AddItem(String Item)
    {
      if (!occ.containsKey(Item)) {
        occ.put(Item, Integer.valueOf(1));
      } else {
        int count = ((Integer)occ.get(Item)).intValue();
        count++;
        occ.remove(Item);
        occ.put(Item, Integer.valueOf(count));
      }
      S += 1;
    }
    
    public void AddItem(Integer Item)
    {
      if (!occ.containsKey(Item)) {
        occ.put(Item, Integer.valueOf(1));
      } else {
        int count = ((Integer)occ.get(Item)).intValue();
        count++;
        occ.remove(Item);
        occ.put(Item, Integer.valueOf(count));
      }
      S += 1;
    }
    




    public void DeleteItem(String Item)
    {
      int count = ((Integer)occ.get(Item)).intValue();
      
      if (count == 1) {
        occ.remove(Item);
      }
      else {
        count--;
        occ.remove(Item);
        occ.put(Item, Integer.valueOf(count));
      }
      S -= 1;
    }
    

    public void DeleteItem(Integer Item)
    {
      int count = ((Integer)occ.get(Item)).intValue();
      
      if (count == 1) {
        occ.remove(Item);
      }
      else {
        count--;
        occ.remove(Item);
        occ.put(Item, Integer.valueOf(count));
      }
      S -= 1;
    }
    








    public double DeltaAdd(Instance inst, double r)
    {
      int S_new = 0;
      int W_new = occ.size();
      
      if ((inst instanceof SparseInstance))
      {
        for (int i = 0; i < inst.numValues(); i++) {
          S_new++;
          
          if ((Integer)occ.get(Integer.valueOf(inst.index(i))) == null) {
            W_new++;
          }
        }
      } else {
        for (int i = 0; i < inst.numAttributes(); i++) {
          if (!inst.isMissing(i)) {
            S_new++;
            if ((Integer)occ.get(i + inst.toString(i)) == null) {
              W_new++;
            }
          }
        }
      }
      S_new += S;
      double deltaprofit;
      double deltaprofit;
      if (N == 0) {
        deltaprofit = S_new / Math.pow(W_new, r);
      } else {
        double profit = S * N / Math.pow(W, r);
        double profit_new = S_new * (N + 1) / Math.pow(W_new, r);
        deltaprofit = profit_new - profit;
      }
      return deltaprofit;
    }
    


    public void AddInstance(Instance inst)
    {
      if ((inst instanceof SparseInstance))
      {
        for (int i = 0; i < inst.numValues(); i++) {
          AddItem(Integer.valueOf(inst.index(i)));
        }
        
      }
      else {
        for (int i = 0; i < inst.numAttributes(); i++)
        {
          if (!inst.isMissing(i))
          {
            AddItem(i + inst.toString(i));
          }
        }
      }
      W = occ.size();
      N += 1;
    }
    


    public void DeleteInstance(Instance inst)
    {
      if ((inst instanceof SparseInstance))
      {
        for (int i = 0; i < inst.numValues(); i++) {
          DeleteItem(Integer.valueOf(inst.index(i)));
        }
      } else {
        for (int i = 0; i <= inst.numAttributes() - 1; i++)
        {
          if (!inst.isMissing(i)) {
            DeleteItem(i + inst.toString(i));
          }
        }
      }
      W = occ.size();
      N -= 1;
    }
  }
  


  public ArrayList<CLOPECluster> clusters = new ArrayList();
  



  protected double m_RepulsionDefault = 2.6D;
  



  protected double m_Repulsion = m_RepulsionDefault;
  



  protected int m_numberOfClusters = -1;
  



  protected int m_processed_InstanceID;
  



  protected int m_numberOfInstances;
  



  protected ArrayList<Integer> m_clusterAssignments = new ArrayList();
  



  protected boolean m_numberOfClustersDetermined = false;
  
  public int numberOfClusters() {
    determineNumberOfClusters();
    return m_numberOfClusters;
  }
  
  protected void determineNumberOfClusters()
  {
    m_numberOfClusters = clusters.size();
    
    m_numberOfClustersDetermined = true;
  }
  
  public Enumeration listOptions() {
    Vector result = new Vector();
    result.addElement(new Option("\tRepulsion\n\t(default " + m_RepulsionDefault + ")", "R", 1, "-R <num>"));
    

    return result.elements();
  }
  















  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0) {
      setRepulsion(Double.parseDouble(tmpStr));
    } else {
      setRepulsion(m_RepulsionDefault);
    }
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-R");
    result.add("" + getRepulsion());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String repulsionTipText()
  {
    return "Repulsion to be used.";
  }
  





  public void setRepulsion(double value)
  {
    m_Repulsion = value;
  }
  




  public double getRepulsion()
  {
    return m_Repulsion;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    result.enable(Capabilities.Capability.NO_CLASS);
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    
    result.enable(Capabilities.Capability.MISSING_VALUES);
    
    return result;
  }
  



  public void buildClusterer(Instances data)
    throws Exception
  {
    clusters.clear();
    m_processed_InstanceID = 0;
    m_clusterAssignments.clear();
    m_numberOfInstances = data.numInstances();
    

    for (int i = 0; i < data.numInstances(); i++) {
      int clusterid = AddInstanceToBestCluster(data.instance(i));
      m_clusterAssignments.add(Integer.valueOf(clusterid));
    }
    boolean moved;
    do
    {
      moved = false;
      for (int i = 0; i < data.numInstances(); i++) {
        m_processed_InstanceID = i;
        int clusterid = MoveInstanceToBestCluster(data.instance(i));
        if (clusterid != ((Integer)m_clusterAssignments.get(i)).intValue()) {
          moved = true;
          m_clusterAssignments.set(i, Integer.valueOf(clusterid));
        }
      }
    } while (!moved);
    m_processed_InstanceID = 0;
  }
  





  public CLOPE() {}
  





  public int AddInstanceToBestCluster(Instance inst)
  {
    int clustermax = -1;
    if (clusters.size() > 0) {
      int tempS = 0;
      int tempW = 0;
      if ((inst instanceof SparseInstance)) {
        for (int i = 0; i < inst.numValues(); i++) {
          tempS++;
          tempW++;
        }
      } else {
        for (int i = 0; i < inst.numAttributes(); i++) {
          if (!inst.isMissing(i)) {
            tempS++;
            tempW++;
          }
        }
      }
      
      double deltamax = tempS / Math.pow(tempW, m_Repulsion);
      
      for (int i = 0; i < clusters.size(); i++) {
        CLOPECluster tempcluster = (CLOPECluster)clusters.get(i);
        double delta = tempcluster.DeltaAdd(inst, m_Repulsion);
        
        if (delta > deltamax) {
          deltamax = delta;
          clustermax = i;
        }
      }
    } else {
      CLOPECluster newcluster = new CLOPECluster(null);
      clusters.add(newcluster);
      newcluster.AddInstance(inst);
      return clusters.size() - 1;
    }
    double deltamax;
    if (clustermax == -1) {
      CLOPECluster newcluster = new CLOPECluster(null);
      clusters.add(newcluster);
      newcluster.AddInstance(inst);
      return clusters.size() - 1;
    }
    ((CLOPECluster)clusters.get(clustermax)).AddInstance(inst);
    return clustermax;
  }
  



  public int MoveInstanceToBestCluster(Instance inst)
  {
    ((CLOPECluster)clusters.get(((Integer)m_clusterAssignments.get(m_processed_InstanceID)).intValue())).DeleteInstance(inst);
    m_clusterAssignments.set(m_processed_InstanceID, Integer.valueOf(-1));
    

    int clustermax = -1;
    int tempS = 0;
    int tempW = 0;
    
    if ((inst instanceof SparseInstance)) {
      for (int i = 0; i < inst.numValues(); i++) {
        tempS++;
        tempW++;
      }
    } else {
      for (int i = 0; i < inst.numAttributes(); i++) {
        if (!inst.isMissing(i)) {
          tempS++;
          tempW++;
        }
      }
    }
    
    double deltamax = tempS / Math.pow(tempW, m_Repulsion);
    for (int i = 0; i < clusters.size(); i++) {
      CLOPECluster tempcluster = (CLOPECluster)clusters.get(i);
      double delta = tempcluster.DeltaAdd(inst, m_Repulsion);
      
      if (delta > deltamax) {
        deltamax = delta;
        clustermax = i;
      }
    }
    if (clustermax == -1) {
      CLOPECluster newcluster = new CLOPECluster(null);
      clusters.add(newcluster);
      newcluster.AddInstance(inst);
      return clusters.size() - 1;
    }
    ((CLOPECluster)clusters.get(clustermax)).AddInstance(inst);
    return clustermax;
  }
  






  public int clusterInstance(Instance instance)
    throws Exception
  {
    if (m_processed_InstanceID >= m_numberOfInstances) {
      m_processed_InstanceID = 0;
    }
    int i = ((Integer)m_clusterAssignments.get(m_processed_InstanceID)).intValue();
    m_processed_InstanceID += 1;
    return i;
  }
  




  public String toString()
  {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("CLOPE clustering results\n========================================================================================\n\n");
    
    stringBuffer.append("Clustered instances: " + m_clusterAssignments.size() + "\n");
    return stringBuffer.toString() + "\n";
  }
  



  public String globalInfo()
  {
    return getTechnicalInformation().toString();
  }
  








  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Yiling Yang and Xudong Guan and Jinyuan You");
    result.setValue(TechnicalInformation.Field.TITLE, "CLOPE: a fast and effective clustering algorithm for transactional data");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the eighth ACM SIGKDD international conference on Knowledge discovery and data mining");
    result.setValue(TechnicalInformation.Field.YEAR, "2002");
    result.setValue(TechnicalInformation.Field.PAGES, "682-687");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "ACM  New York, NY, USA");
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5538 $");
  }
  





  public static void main(String[] argv)
  {
    runClusterer(new CLOPE(), argv);
  }
}
